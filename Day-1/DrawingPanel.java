import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class DrawingPanel extends JPanel {
    
    private ArrayList<Node> nodes;
    private ArrayList<ArrayList<Edge>> adj;
    private ArrayList<RawInputEdge> rawEdges;

    public DrawingPanel(ArrayList<Node> nodes, ArrayList<ArrayList<Edge>> adj, ArrayList<RawInputEdge> rawEdges) {
        this.nodes = nodes;
        this.adj = adj;
        this.rawEdges = rawEdges;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Graphics provide basic drawing methods while Graphics2D provides more sophisticated control over geometry, coordinate transformations, color management, and text layout.
        Graphics2D g2 = (Graphics2D) g; // Convert Graphics to Graphics2D
        
        // Used to smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Used to smooth text
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set Brush Color to White
        g2.setColor(Color.WHITE);
        // Fill the entire panel with white color to clear previous drawings
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw edges first so that nodes are on top of edges
        if (adj.isEmpty()) {
            drawRawEdges(g2); 
        } else {
            drawFlowEdges(g2); // Works when we Click on Run Flow
        }

        // Draw Nodes One By One
        for (Node n : nodes) {
            // Bigger Nodes
            int r = 25; // Radius 25 (Diameter 50) in Pixels
            g2.setColor(new Color(100, 149, 237)); // Red , Green , Blue -> Here Color is Cornflower Blue
            // Left, Top Boundary, Width , Height
            g2.fillOval(n.x - r, n.y - r, 2*r, 2*r); // To fill inner circle
            // Border Color
            g2.setColor(Color.BLACK);
            // Border Thickness of 2 Pixels
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(n.x - r, n.y - r, 2*r, 2*r); // To draw outer circle
            
            // Node Text Color
            g2.setColor(Color.WHITE);
            // Writing style, Style, Size
            g2.setFont(new Font("Arial", Font.BOLD, 18)); // Font 18
            // Text Measurement Tool
            FontMetrics fm = g2.getFontMetrics();
            // Text Width
            int tw = fm.stringWidth(n.name);
            // Text Height
            int th = fm.getAscent();
            // Draw the Node Name at the Center of the Node
            g2.drawString(n.name, n.x - tw / 2, n.y + th / 2 - 2);
        }
    }

    private void drawRawEdges(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2)); // 2 Pixel Thickness
        for(RawInputEdge raw : rawEdges) {
            Node u = findNodeByName(raw.u);
            Node v = findNodeByName(raw.v);
            if (u != null && v != null) {
                g2.setColor(Color.GRAY);
                g2.drawLine(u.x, u.y, v.x, v.y);
                
                int tx = (int) (u.x + (v.x - u.x) * 0.35);
                int ty = (int) (u.y + (v.y - u.y) * 0.35);
                
                // Draw the weight label for the raw edge
                drawLabelAtPosition(g2, String.valueOf(raw.weight), tx, ty, Color.BLACK);
            }
        }
    }

    private void drawFlowEdges(Graphics2D g2) {
        // Duplicate Edge Control
        HashSet<String> drawnPairs = new HashSet<>();

        for (int uIdx = 0; uIdx < adj.size(); uIdx++) {
            for (Edge e : adj.get(uIdx)) {
                if (e.capacity > 0) {
                    Node u = nodes.get(e.u);
                    Node v = nodes.get(e.v);

                    int min = Math.min(e.u, e.v);
                    int max = Math.max(e.u, e.v);
                    String pairKey = min + "-" + max;

                    if (drawnPairs.contains(pairKey)) continue; 
                    drawnPairs.add(pairKey);

                    boolean hasFlow = e.flow > 0;
                    Edge reverseEdge = findReverseEdge(e);
                    boolean reverseFlow = (reverseEdge != null && reverseEdge.flow > 0);

                    if (e.isHighlighted || (reverseEdge != null && reverseEdge.isHighlighted)) {
                        g2.setColor(new Color(0, 200, 0)); 
                        g2.setStroke(new BasicStroke(5)); // Thicker Highlight
                    } else {
                        g2.setColor(Color.BLACK);
                        g2.setStroke(new BasicStroke(2));
                    }

                    g2.drawLine(u.x, u.y, v.x, v.y);

                    if (hasFlow) drawArrowHead(g2, u, v);
                    else if (reverseFlow) drawArrowHead(g2, v, u);
                    else if (isPurelyDirected(e)) drawArrowHead(g2, u, v);

                    String label;
                    if (hasFlow) label = e.flow + "/" + e.capacity;
                    else if (reverseFlow) label = reverseEdge.flow + "/" + reverseEdge.capacity;
                    else label = "0/" + e.capacity;

                    Color textColor = (hasFlow || reverseFlow) ? Color.BLUE : Color.DARK_GRAY;
                    
                    int tx = (int) (u.x + (v.x - u.x) * 0.35);
                    int ty = (int) (u.y + (v.y - u.y) * 0.35);
                    
                    drawLabelAtPosition(g2, label, tx, ty, textColor);
                }
            }
        }
    }

    private Edge findReverseEdge(Edge forward) {
        for(Edge e : adj.get(forward.v)) {
            if(e.v == forward.u && e.capacity > 0) return e; 
        }
        return null;
    }

    private boolean isPurelyDirected(Edge e) {
        return findReverseEdge(e) == null;
    }

    private void drawArrowHead(Graphics2D g2, Node u, Node v) {
        double angle = Math.atan2(v.y - u.y, v.x - u.x);
        int r = 25; // Adjusted for bigger node size
        int endX = (int) (v.x - r * Math.cos(angle));
        int endY = (int) (v.y - r * Math.sin(angle));
        
        // Arrow Spread Angle
        double arrowAngle = Math.PI / 6; // 30 degrees
        int arrowLen = 18; // Arrow Wings Length
        int x1 = (int) (endX - arrowLen * Math.cos(angle - arrowAngle));
        int y1 = (int) (endY - arrowLen * Math.sin(angle - arrowAngle));
        int x2 = (int) (endX - arrowLen * Math.cos(angle + arrowAngle));
        int y2 = (int) (endY - arrowLen * Math.sin(angle + arrowAngle));
        
        g2.drawLine(endX, endY, x1, y1);
        g2.drawLine(endX, endY, x2, y2);
    }

    private void drawLabelAtPosition(Graphics2D g2, String text, int x, int y, Color color) {
        // Bigger Font for Weights
        g2.setFont(new Font("Arial", Font.BOLD, 18)); 
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        int h = fm.getHeight();
        
        g2.setColor(new Color(255, 255, 255, 240));
        g2.fillRect(x - w/2 - 6, y - h/2 + 2, w + 12, h); // Bigger Box
        
        g2.setColor(color);
        g2.drawString(text, x - w/2, y + h/3);
    }

    private Node findNodeByName(String name) {
        for(Node n : nodes) if(n.name.equals(name)) return n;
        return null;
    }
}