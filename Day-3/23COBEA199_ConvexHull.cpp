#include <SFML/Graphics.hpp>
#include <vector>
#include <string>
#include <algorithm>
#include <iostream>

using namespace std;

// --- 1. DATA STRUCTURES ---

struct Point {
    double x, y;
};

struct HullStep {
    vector<Point> stack;
    string actionText;
};

// Global reference point for sorting
Point p0;

// --- 2. MATH & GEOMETRY FUNCTIONS ---

inline double distSq(Point p1, Point p2) {
    return (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y);
}

inline double crossProductValue(Point p, Point q, Point r) {
    return (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
}

inline int orientation(Point p, Point q, Point r) {
    double val = crossProductValue(p, q, r);
    if (val == 0) return 0;
    return (val > 0) ? 1 : 2; 
}

inline bool comparePoints(Point p1, Point p2) {
    int o = orientation(p0, p1, p2);
    if (o == 0) return distSq(p0, p2) >= distSq(p0, p1);
    return (o == 1);
}

// --- 3. GRAHAM SCAN ALGORITHM (CLRS Logic) ---

vector<HullStep> getConvexHullHistory(vector<Point> points) {
    vector<HullStep> history;
    int n = points.size();
    
    if (n < 3) {
        if (n > 0) history.push_back({points, "Need at least 3 points to form a hull."});
        return history;
    }

    int ymin = points[0].y, min = 0;
    for (int i = 1; i < n; i++) {
        int y = points[i].y;
        if ((y > ymin) || (ymin == y && points[i].x < points[min].x)) {
            ymin = points[i].y;
            min = i;
        }
    }

    swap(points[0], points[min]);
    p0 = points[0];

    sort(points.begin() + 1, points.end(), comparePoints);

    int m = 1; 
    for (int i = 1; i < n; i++) {
        while (i < n - 1 && orientation(p0, points[i], points[i+1]) == 0) i++;
        points[m] = points[i];
        m++;
    }

    if (m < 3) {
        vector<Point> s;
        for(int i=0; i<m; i++) s.push_back(points[i]);
        history.push_back({s, "All points are collinear!"});
        return history;
    }

    vector<Point> S;
    S.push_back(points[0]);
    S.push_back(points[1]);
    S.push_back(points[2]);
    history.push_back({S, "Pushed first 3 points (P0, P1, P2) to stack."});

    for (int i = 3; i < m; i++) {
        while (S.size() > 1) {
            Point p = S[S.size()-2];
            Point q = S.back();
            Point r = points[i];
            
            double cp = crossProductValue(p, q, r);
            string text = "Checking Point... Cross Product: " + to_string((int)cp);

            if (orientation(p, q, r) != 1) { 
                text += "\nResult: RIGHT TURN! Popping top point.";
                S.pop_back();
                history.push_back({S, text});
            } else { 
                text += "\nResult: LEFT TURN! Valid boundary.";
                history.push_back({S, text}); 
                break;
            }
        }
        S.push_back(points[i]);
        history.push_back({S, "Pushed new point to stack."});
    }

    history.push_back({S, "Convex Hull Completed!"});
    return history;
}

// --- 4. GUI AND MAIN FUNCTION ---

void drawPoints(sf::RenderWindow& window, const vector<Point>& points) {
    for (size_t i = 0; i < points.size(); ++i) {
        sf::CircleShape circle(5);
        circle.setFillColor(sf::Color::White);
        circle.setOrigin(5, 5); 
        circle.setPosition(points[i].x, points[i].y);
        window.draw(circle);
    }
}

void drawHull(sf::RenderWindow& window, const vector<Point>& hull) {
    if (hull.size() < 2) return;
    
    sf::VertexArray lines(sf::LineStrip, hull.size());
    for (size_t i = 0; i < hull.size(); ++i) {
        lines[i].position = sf::Vector2f(hull[i].x, hull[i].y);
        lines[i].color = sf::Color::Green;
    }
    window.draw(lines);

    if (hull.size() > 2) {
        sf::VertexArray closeLine(sf::Lines, 2);
        closeLine[0].position = sf::Vector2f(hull.back().x, hull.back().y);
        closeLine[0].color = sf::Color::Green;
        closeLine[1].position = sf::Vector2f(hull[0].x, hull[0].y);
        closeLine[1].color = sf::Color::Green;
        window.draw(closeLine);
    }
}

enum AppMode { DRAWING_MODE, MANUAL_MODE, ANIMATION_MODE };

int main() {
    sf::VideoMode desktop = sf::VideoMode::getDesktopMode();
    sf::RenderWindow window(desktop, "Graham Scan - Interactive Visualizer", sf::Style::Fullscreen);
    window.setFramerateLimit(60);

    sf::Font font;
    bool fontLoaded = font.loadFromFile("arial.ttf"); 

    float screenWidth = desktop.width;

    // --- UI Setup ---
    sf::RectangleShape runBtn(sf::Vector2f(120, 40)); runBtn.setPosition(screenWidth - 140, 20); runBtn.setFillColor(sf::Color(50, 150, 50)); 
    sf::RectangleShape animBtn(sf::Vector2f(120, 40)); animBtn.setPosition(screenWidth - 140, 70); animBtn.setFillColor(sf::Color(50, 50, 150)); 
    sf::RectangleShape stopBtn(sf::Vector2f(120, 40)); stopBtn.setPosition(screenWidth - 140, 120); stopBtn.setFillColor(sf::Color(200, 100, 0)); 
    sf::RectangleShape resetBtn(sf::Vector2f(120, 40)); resetBtn.setPosition(screenWidth - 140, 170); resetBtn.setFillColor(sf::Color(150, 50, 50)); 
    sf::RectangleShape exitBtn(sf::Vector2f(120, 40)); exitBtn.setPosition(screenWidth - 140, 220); exitBtn.setFillColor(sf::Color(100, 100, 100)); 

    int animDelayMs = 500; // Default fast delay

    sf::RectangleShape decBtn(sf::Vector2f(55, 40)); decBtn.setPosition(screenWidth - 140, 310); decBtn.setFillColor(sf::Color(80, 80, 80));
    sf::RectangleShape incBtn(sf::Vector2f(55, 40)); incBtn.setPosition(screenWidth - 75, 310); incBtn.setFillColor(sf::Color(80, 80, 80));

    sf::Text runTxt, animTxt, stopTxt, resetTxt, exitTxt, infoText, instructionText, delayLabel, decTxt, incTxt;
    if (fontLoaded) {
        runTxt.setFont(font); runTxt.setString("RUN"); runTxt.setCharacterSize(20); runTxt.setPosition(screenWidth - 105, 28);
        animTxt.setFont(font); animTxt.setString("ANIMATE"); animTxt.setCharacterSize(20); animTxt.setPosition(screenWidth - 125, 78);
        stopTxt.setFont(font); stopTxt.setString("STOP"); stopTxt.setCharacterSize(20); stopTxt.setPosition(screenWidth - 105, 128);
        resetTxt.setFont(font); resetTxt.setString("RESET"); resetTxt.setCharacterSize(20); resetTxt.setPosition(screenWidth - 115, 178);
        exitTxt.setFont(font); exitTxt.setString("EXIT"); exitTxt.setCharacterSize(20); exitTxt.setPosition(screenWidth - 105, 228);
        
        infoText.setFont(font); infoText.setCharacterSize(22); infoText.setFillColor(sf::Color::Yellow); infoText.setPosition(20, 60);
        instructionText.setFont(font); instructionText.setCharacterSize(24); instructionText.setFillColor(sf::Color::Cyan); instructionText.setPosition(20, 20);

        delayLabel.setFont(font); delayLabel.setCharacterSize(18); delayLabel.setFillColor(sf::Color::White); delayLabel.setPosition(screenWidth - 140, 280);
        decTxt.setFont(font); decTxt.setString("-"); decTxt.setCharacterSize(30); decTxt.setPosition(screenWidth - 122, 310);
        incTxt.setFont(font); incTxt.setString("+"); incTxt.setCharacterSize(30); incTxt.setPosition(screenWidth - 57, 310);
    }

    vector<Point> points;
    vector<HullStep> hullHistory;
    int currentStep = 0;
    AppMode currentMode = DRAWING_MODE; 
    sf::Clock animationClock;

    while (window.isOpen()) {
        sf::Event event;
        while (window.pollEvent(event)) {
            if (event.type == sf::Event::Closed) window.close();
            if (event.type == sf::Event::KeyPressed && event.key.code == sf::Keyboard::Escape) window.close();

            if (event.type == sf::Event::MouseButtonPressed && event.mouseButton.button == sf::Mouse::Left) {
                sf::Vector2i pixelPos(event.mouseButton.x, event.mouseButton.y);
                sf::Vector2f worldPos = window.mapPixelToCoords(pixelPos);
                float mx = worldPos.x; float my = worldPos.y;

                if (decBtn.getGlobalBounds().contains(mx, my)) {
                    animDelayMs -= 50; if (animDelayMs < 50) animDelayMs = 50; 
                }
                else if (incBtn.getGlobalBounds().contains(mx, my)) {
                    animDelayMs += 50; if (animDelayMs > 3000) animDelayMs = 3000; 
                }
                else if (exitBtn.getGlobalBounds().contains(mx, my)) {
                    window.close();
                }
                else if (resetBtn.getGlobalBounds().contains(mx, my)) {
                    points.clear(); hullHistory.clear(); currentStep = 0; currentMode = DRAWING_MODE;
                }
                else if (stopBtn.getGlobalBounds().contains(mx, my)) {
                    hullHistory.clear(); currentStep = 0; currentMode = DRAWING_MODE;
                }
                else if (runBtn.getGlobalBounds().contains(mx, my) && currentMode == DRAWING_MODE) {
                    if (points.size() >= 3) {
                        hullHistory = getConvexHullHistory(points); currentStep = 0; currentMode = MANUAL_MODE;
                    }
                }
                else if (animBtn.getGlobalBounds().contains(mx, my) && currentMode == DRAWING_MODE) {
                    if (points.size() >= 3) {
                        hullHistory = getConvexHullHistory(points); currentStep = 0; currentMode = ANIMATION_MODE; animationClock.restart(); 
                    }
                }
                // DYNAMIC RECOMPUTATION FEATURE
                else if (mx < screenWidth - 150) { 
                    points.push_back({(double)mx, (double)my});
                    // If a point is added while an algorithm has already finished, immediately recompute and jump to the end.
                    if (currentMode != DRAWING_MODE && currentStep == hullHistory.size() - 1) {
                        hullHistory = getConvexHullHistory(points);
                        currentStep = hullHistory.size() - 1; 
                    }
                }
            }

            if (event.type == sf::Event::KeyPressed) {
                if (event.key.code == sf::Keyboard::Space && currentMode == MANUAL_MODE) {
                    if (!hullHistory.empty() && currentStep < (int)hullHistory.size() - 1) currentStep++;
                }
            }
        }

        if (currentMode == ANIMATION_MODE) {
            if (animationClock.getElapsedTime().asMilliseconds() >= animDelayMs) {
                if (!hullHistory.empty() && currentStep < (int)hullHistory.size() - 1) currentStep++;
                animationClock.restart(); 
            }
        }

        if (fontLoaded) delayLabel.setString("Delay: " + to_string(animDelayMs) + " ms");

        window.clear(sf::Color(30, 30, 30));
        
        drawPoints(window, points);

        window.draw(runBtn); window.draw(animBtn); window.draw(stopBtn); window.draw(resetBtn); window.draw(exitBtn); window.draw(decBtn); window.draw(incBtn);
        
        if (fontLoaded) {
            window.draw(runTxt); window.draw(animTxt); window.draw(stopTxt); window.draw(resetTxt); window.draw(exitTxt);
            window.draw(delayLabel); window.draw(decTxt); window.draw(incTxt);
        }

        if (currentMode == DRAWING_MODE) {
            if (fontLoaded) {
                instructionText.setString("Mode: DRAWING -> Add points, then press RUN (Manual) or ANIMATE (Auto).");
                window.draw(instructionText);
                if (points.size() < 3) window.draw(infoText); 
            }
        } 
        else if (currentMode == MANUAL_MODE || currentMode == ANIMATION_MODE) {
            if (fontLoaded) {
                if(currentMode == MANUAL_MODE) instructionText.setString("Mode: MANUAL -> Press Spacebar to step. Click to add points & recompute.");
                else instructionText.setString("Mode: ANIMATING -> Auto-advancing. Click to add points & recompute.");
                window.draw(instructionText);
                
                if (!hullHistory.empty() && currentStep < (int)hullHistory.size()) {
                    drawHull(window, hullHistory[currentStep].stack);
                    string displayText = "Step: " + to_string(currentStep + 1) + " / " + to_string(hullHistory.size()) + "\n" + hullHistory[currentStep].actionText;
                    infoText.setString(displayText);
                    window.draw(infoText);
                }
            }
        }

        window.display();
    }
    return 0;
}