import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class NFAGUI extends JFrame {

    List<String> statesList;
    List<String> symbolsList;
    String startState;
    Set<String> finalStates;
    Map<String, Map<String, List<String>>> transitions = new HashMap<>();

    JTextField inputField;
    JButton runBtn, resetBtn;
    JTextArea animationArea;
    JLabel statusLabel, resultLabel;
    VisualPanel visualPanel;

    javax.swing.Timer timer;
    List<TraceStep> animationSteps = new ArrayList<>();
    int currentStepIndex = 0;

    String currentStateForVisual = "";
    String sourceStateForVisual = "";
    String activeSymbol = "";
    boolean isCurrentDeadEnd = false; // Trap detection

    class TraceStep {
        String from, to, sym, msg;
        boolean isFinal, isDead;

        TraceStep(String f, String t, String s, String m, boolean fin, boolean dead) {
            this.from = f;
            this.to = t;
            this.sym = s;
            this.msg = m;
            this.isFinal = fin;
            this.isDead = dead;
        }
    }

    public NFAGUI() {
        setTitle("NFA Professional Visualizer - Yatharth");
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        if (!loadNFA("nfa.txt")) {
            JOptionPane.showMessageDialog(this, "nfa.txt missing!");
            System.exit(1);
        }
        setupUI();
    }

    private void setupUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        topPanel.setBackground(new Color(30, 30, 30));
        inputField = new JTextField(20);
        inputField.setFont(new Font("Consolas", Font.BOLD, 18));
        runBtn = new JButton("START SIMULATION");
        resetBtn = new JButton("RESET");

        topPanel.add(new JLabel("INPUT:") {
            {
                setForeground(Color.WHITE);
            }
        });
        topPanel.add(inputField);
        topPanel.add(runBtn);
        topPanel.add(resetBtn);
        add(topPanel, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(350, 0));
        animationArea = new JTextArea();
        animationArea.setEditable(false);
        animationArea.setFont(new Font("Monospaced", Font.BOLD, 13));
        animationArea.setBackground(Color.WHITE);
        leftPanel.add(new JScrollPane(animationArea), BorderLayout.CENTER);
        leftPanel.setBorder(BorderFactory.createTitledBorder("PATH TRACING"));
        add(leftPanel, BorderLayout.WEST);

        visualPanel = new VisualPanel();
        add(visualPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        resultLabel = new JLabel("READY", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Verdana", Font.BOLD, 26));
        bottomPanel.add(resultLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        runBtn.addActionListener(e -> startAnimation());
        resetBtn.addActionListener(e -> resetApp());
        timer = new javax.swing.Timer(1000, e -> playNextFrame());
    }

    class VisualPanel extends JPanel {
        Map<String, Point> coords = new HashMap<>();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int r = 38;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int orbit = Math.min(centerX, centerY) - 120;

            for (int i = 0; i < statesList.size(); i++) {
                double angle = 2 * Math.PI * i / statesList.size();
                coords.put(statesList.get(i), new Point(centerX + (int) (orbit * Math.cos(angle)),
                        centerY + (int) (orbit * Math.sin(angle))));
            }

            // Draw Initial Edge (Incoming arrow to start state)
            Point startP = coords.get(startState);
            if (startP != null) {
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.BLUE);
                g2.drawLine(startP.x - 80, startP.y, startP.x - r - 5, startP.y);
                g2.fillPolygon(new int[] { startP.x - r - 5, startP.x - r - 15, startP.x - r - 15 },
                        new int[] { startP.y, startP.y - 5, startP.y + 5 }, 3);
            }

            // Draw Base Transitions
            for (String src : transitions.keySet()) {
                for (String sym : transitions.get(src).keySet()) {
                    for (String dest : transitions.get(src).get(sym)) {
                        drawEnhancedArrow(g2, coords.get(src), coords.get(dest), sym, Color.LIGHT_GRAY, 1, false);
                    }
                }
            }

            // Draw Glowing Active Transition
            if (!sourceStateForVisual.isEmpty() && !currentStateForVisual.isEmpty()) {
                drawEnhancedArrow(g2, coords.get(sourceStateForVisual), coords.get(currentStateForVisual), activeSymbol,
                        new Color(0, 180, 0), 4, true);
            }

            // Draw State Circles
            for (String s : statesList) {
                Point p = coords.get(s);
                boolean isCurrent = s.equals(currentStateForVisual);

                if (isCurrent) {
                    g2.setColor(isCurrentDeadEnd ? new Color(255, 200, 200) : new Color(200, 255, 200));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);

                // Border Color
                if (isCurrent) {
                    g2.setColor(isCurrentDeadEnd ? Color.RED : new Color(0, 150, 0));
                    g2.setStroke(new BasicStroke(5));
                } else {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(2));
                }

                g2.drawOval(p.x - r, p.y - r, 2 * r, 2 * r);
                if (finalStates.contains(s))
                    g2.drawOval(p.x - r + 6, p.y - r + 6, 2 * r - 12, 2 * r - 12);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                g2.drawString(s, p.x - 10, p.y + 5);
            }
        }

        private void drawEnhancedArrow(Graphics2D g2, Point p1, Point p2, String sym, Color c, int t, boolean active) {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(t));
            if (p1.equals(p2)) { // Self loop
                g2.drawArc(p1.x - 25, p1.y - 60, 50, 50, 0, 180);
                if (active)
                    g2.setColor(new Color(0, 150, 0));
                g2.drawString(sym, p1.x - 5, p1.y - 65);
                return;
            }
            double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
            int x1 = p1.x + (int) (38 * Math.cos(angle));
            int y1 = p1.y + (int) (38 * Math.sin(angle));
            int x2 = p2.x - (int) (45 * Math.cos(angle));
            int y2 = p2.y - (int) (45 * Math.sin(angle));

            g2.drawLine(x1, y1, x2, y2);
            // Arrowhead
            Path2D.Double head = new Path2D.Double();
            head.moveTo(x2, y2);
            head.lineTo(x2 - 12 * Math.cos(angle - 0.4), y2 - 12 * Math.sin(angle - 0.4));
            head.lineTo(x2 - 12 * Math.cos(angle + 0.4), y2 - 12 * Math.sin(angle + 0.4));
            head.closePath();
            g2.fill(head);

            // Symbol Visibility
            if (active) {
                g2.setColor(new Color(0, 120, 0));
                g2.setFont(new Font("Arial", Font.BOLD, 18));
            } else {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Arial", Font.PLAIN, 14));
            }
            g2.drawString(sym, (x1 + x2) / 2, (y1 + y2) / 2 - 10);
        }
    }

    private void startAnimation() {
        String input = inputField.getText().trim();
        if (input.isEmpty())
            return;
        animationSteps.clear();
        animationArea.setText("");
        resultLabel.setText("PROCESSING...");
        resultLabel.setForeground(Color.BLACK);

        simulate(startState, input, 0);
        currentStepIndex = 0;
        timer.start();
        runBtn.setEnabled(false);
    }

    private void playNextFrame() {
        if (currentStepIndex < animationSteps.size()) {
            TraceStep step = animationSteps.get(currentStepIndex);
            if (step.isFinal) {
                resultLabel.setText(step.msg);
                resultLabel.setForeground(step.msg.contains("ACCEPTED") ? new Color(0, 150, 0) : Color.RED);
                timer.stop();
                runBtn.setEnabled(true);
            } else {
                sourceStateForVisual = step.from;
                currentStateForVisual = step.to;
                activeSymbol = step.sym;
                isCurrentDeadEnd = step.isDead;
                animationArea.append(step.msg + "\n");
                visualPanel.repaint();
            }
            currentStepIndex++;
        }
    }

    private boolean simulate(String curr, String input, int idx) {
        // Check if we reached the end of the string
        if (idx == input.length()) {
            boolean ok = finalStates.contains(curr);
            animationSteps.add(new TraceStep("", curr, "",
                    "Checking final state " + curr + ": " + (ok ? "SUCCESS" : "FAILED"), false, !ok));
            return ok;
        }

        String sym = String.valueOf(input.charAt(idx));

        // Check if any transitions exist for this symbol
        if (transitions.get(curr) == null || transitions.get(curr).get(sym) == null) {
            animationSteps
                    .add(new TraceStep(curr, curr, sym, "DEAD END at " + curr + " for '" + sym + "'", false, true));
            return false;
        }

        List<String> nextStates = transitions.get(curr).get(sym);
        boolean foundPath = false;

        for (String next : nextStates) {
            // Log the attempt to go into this branch
            animationSteps.add(new TraceStep(curr, next, sym, "EXPLORING branch: " + curr + " --" + sym + "--> " + next,
                    false, false));

            if (simulate(next, input, idx + 1)) {
                foundPath = true;
                // Agar ek success mil gaya, tab bhi hum backtrack karke dikha sakte hain
                // ya sidha return kar sakte hain. NFA usually first success milte hi ruk jata
                // hai.
                return true;
            } else {
                // Agar ye rasta fail hua, toh explicit BACKTRACK dikhao
                animationSteps.add(new TraceStep(next, curr, "",
                        "PATH FAILED from " + next + ". Backtracking to " + curr, false, false));
            }
        }

        // Agar saare branches check ho gaye aur koi success nahi mila
        if (!foundPath && idx == 0) {
            animationSteps.add(new TraceStep("", "", "", "FINAL RESULT: REJECTED ❌", true, true));
        }

        return foundPath;
    }

    private boolean loadNFA(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            statesList = Arrays.asList(br.readLine().split(","));
            symbolsList = Arrays.asList(br.readLine().split(","));
            startState = br.readLine().trim();
            finalStates = new HashSet<>(Arrays.asList(br.readLine().split(",")));
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3) {
                    transitions.putIfAbsent(p[0], new HashMap<>());
                    transitions.get(p[0]).putIfAbsent(p[1], new ArrayList<>());
                    for (int i = 2; i < p.length; i++)
                        transitions.get(p[0]).get(p[1]).add(p[i]);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void resetApp() {
        timer.stop();
        animationArea.setText("");
        resultLabel.setText("READY");
        inputField.setText("");
        sourceStateForVisual = "";
        currentStateForVisual = "";
        isCurrentDeadEnd = false;
        runBtn.setEnabled(true);
        visualPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NFAGUI().setVisible(true));
    }
}