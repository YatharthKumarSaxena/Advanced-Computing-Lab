import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class FlowNetworkSolver extends JFrame {

    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<RawInputEdge> rawEdges = new ArrayList<>(); 
    private ArrayList<ArrayList<Edge>> adj = new ArrayList<>(); 
    
    private DrawingPanel canvas;
    private JTextArea logArea;
    private JTextField sourceField, sinkField, uField, vField, wField;
    private JButton runButton, clearButton, resetButton; 

    public FlowNetworkSolver() {
        setTitle("Flow Network: Conversion & Edmonds-Karp Visualizer");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Divide Window into 5 Zones: North, South, East, West, Center
        setLayout(new BorderLayout());

        // Top Panel for Inputs and Buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        
        // Title is placed on Top of Panel with a Border
        TitledBorder configBorder = BorderFactory.createTitledBorder("Configuration");
        configBorder.setTitleFont(new Font("Arial", Font.BOLD, 18)); // Made Title Bold
        topPanel.setBorder(configBorder); // Around Panel, fit borders
        
        Font labelFont = new Font("Arial", Font.PLAIN, 18);
        Font inputFont = new Font("Arial", Font.BOLD, 18);

        addLabel(topPanel, "Source:", labelFont);
        sourceField = addTextField(topPanel, "S", 3, inputFont);
        
        addLabel(topPanel, "Sink:", labelFont);
        sinkField = addTextField(topPanel, "T", 3, inputFont);
        
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));
        
        addLabel(topPanel, "From:", labelFont);
        uField = addTextField(topPanel, "", 3, inputFont);
        
        addLabel(topPanel, "To:", labelFont);
        vField = addTextField(topPanel, "", 3, inputFont);
        
        addLabel(topPanel, "Wt:", labelFont);
        wField = addTextField(topPanel, "", 4, inputFont);
        
        // --- BUTTONS ---
        JButton addBtn = createBigButton("ADD / UPDATE", new Color(230, 230, 230));
        topPanel.add(addBtn);
        
        runButton = createBigButton("RUN FLOW", new Color(100, 255, 100));
        topPanel.add(runButton);

        resetButton = createBigButton("RESET FLOW", new Color(100, 200, 255));
        topPanel.add(resetButton);

        clearButton = createBigButton("CLEAR ALL", new Color(255, 100, 100));
        clearButton.setForeground(Color.WHITE);
        topPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL (Drawing) ---
        canvas = new DrawingPanel(nodes, adj, rawEdges);
        add(canvas, BorderLayout.CENTER);

        // --- RIGHT PANEL (Logs) ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.BOLD, 20)); // Readable Font
        
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(480, 0)); // Wider for Path text
        
        TitledBorder logBorder = BorderFactory.createTitledBorder("Step-by-Step Log");
        logBorder.setTitleFont(new Font("Arial", Font.BOLD, 20));
        scroll.setBorder(logBorder);
        
        add(scroll, BorderLayout.EAST);

        // Mapping Buttons to Class Functions
        addBtn.addActionListener(e -> addInputEdge());
        runButton.addActionListener(e -> new Thread(this::executeFullProcess).start());
        clearButton.addActionListener(e -> clearGraph());
        resetButton.addActionListener(e -> resetFlow());
    }

    private void resetFlow() {
        adj.clear();
        logArea.setText(">>> Graph Reset. Ready to Edit.\n");
        runButton.setEnabled(true);
        canvas.repaint();
    }

    private void clearGraph() {
        nodes.clear();
        rawEdges.clear();
        adj.clear();
        logArea.setText("");
        runButton.setEnabled(true);
        canvas.repaint();
    }

    // GUI Helper Functions
    private void addLabel(JPanel p, String text, Font f) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        p.add(l);
    }

    private JTextField addTextField(JPanel p, String init, int cols, Font f) {
        JTextField tf = new JTextField(init, cols);
        tf.setFont(f);
        p.add(tf);
        return tf;
    }

    private JButton createBigButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 16)); 
        b.setBackground(bg);
        b.setPreferredSize(new Dimension(160, 45)); 
        b.setFocusPainted(false);
        return b;
    }

    // Input Logic: Add or Update Edges based on User Input
    private void addInputEdge() {
        String u = uField.getText().toUpperCase().trim();
        String v = vField.getText().toUpperCase().trim();
        String wStr = wField.getText().trim();

        if (u.isEmpty() || v.isEmpty() || wStr.isEmpty()) return;

        try {
            int weight = Integer.parseInt(wStr);
            boolean found = false;

            for(RawInputEdge e : rawEdges) {
                if((e.u.equals(u) && e.v.equals(v)) || (e.u.equals(v) && e.v.equals(u))) {
                    e.weight = weight;
                    e.u = u; e.v = v;
                    logArea.append("Updated: " + u + "-" + v + " (" + weight + ")\n");
                    found = true;
                    break;
                }
            }

            if(!found) {
                rawEdges.add(new RawInputEdge(u, v, weight));
                getOrCreateNodeIndex(u);
                getOrCreateNodeIndex(v);
                recalculateNodePositions();
                logArea.append("Added: " + u + "-" + v + " (" + weight + ")\n");
            }
            
            if (!adj.isEmpty()) {
                adj.clear();
                runButton.setEnabled(true);
                logArea.append(">>> Edited. Resetting Flow.\n");
            }

            canvas.repaint();
            uField.setText(""); vField.setText(""); wField.setText(""); uField.requestFocus();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Weight must be an integer.");
        }
    }

    // Edman Carp Algorithm Implementation with Logging and Animation
    private void executeFullProcess() {
        String sName = sourceField.getText().toUpperCase().trim();
        String tName = sinkField.getText().toUpperCase().trim();
        int s = getNodeIndex(sName);
        int t = getNodeIndex(tName);

        if (s == -1 || t == -1) {
            logArea.append("\nError: Source/Sink missing!\n");
            return;
        }
        runButton.setEnabled(false);

        logArea.append("\n=== CONVERTING... ===\n");
        adj.clear();
        for (int i = 0; i < nodes.size(); i++) adj.add(new ArrayList<>());

        for (RawInputEdge raw : rawEdges) {
            int uIdx = getNodeIndex(raw.u);
            int vIdx = getNodeIndex(raw.v);
            int cap = raw.weight;

            if (uIdx == s || vIdx == s) {
                int from = (uIdx == s) ? uIdx : vIdx;
                int to = (uIdx == s) ? vIdx : uIdx;
                addDirectedEdge(from, to, cap);
                // >>> LOG ADDED <<<
                logArea.append(" Rule 1 (Source): Fixed " + nodes.get(from).name + " -> " + nodes.get(to).name + "\n");
            } else if (uIdx == t || vIdx == t) {
                int from = (uIdx == t) ? vIdx : uIdx;
                int to = (uIdx == t) ? uIdx : vIdx;
                addDirectedEdge(from, to, cap);
                // >>> LOG ADDED <<<
                logArea.append(" Rule 2 (Sink): Fixed " + nodes.get(from).name + " -> " + nodes.get(to).name + "\n");
            } else {
                addDirectedEdge(uIdx, vIdx, cap);
                addDirectedEdge(vIdx, uIdx, cap);
                // >>> LOG ADDED <<<
                logArea.append(" Rule 3 (Internal): " + raw.u + " <-> " + raw.v + " (Bidirectional)\n");
            }
        }
        canvas.repaint();
        sleep(1000);

        logArea.append("\n=== RUNNING MAX FLOW ===\n");
        int maxFlow = 0;
        
        while (true) {
            Edge[] parentEdge = new Edge[nodes.size()];
            Arrays.fill(parentEdge, null);
            Queue<Integer> q = new LinkedList<>();
            q.add(s);
            boolean pathFound = false;
            
            while (!q.isEmpty()) {
                int u = q.poll();
                if (u == t) { pathFound = true; break; }
                for (Edge e : adj.get(u)) {
                    if (parentEdge[e.v] == null && e.v != s && e.capacity > e.flow) {
                        parentEdge[e.v] = e;
                        q.add(e.v);
                    }
                }
            }

            // Algorithm Stopping Condition
            if (!pathFound) {
                logArea.append("----------------------\n");
                logArea.append("Searching for path...\n");
                logArea.append(">>> NO MORE PATHS FOUND.\n"); 
                logArea.append("----------------------\n");
                break;
            }

            int pathFlow = Integer.MAX_VALUE;
            int curr = t;
            List<Edge> pathEdges = new ArrayList<>();
            
            // Reconstruct Path for Printing
            List<String> pathNames = new ArrayList<>();
            pathNames.add(nodes.get(t).name);

            while (curr != s) {
                Edge e = parentEdge[curr];
                pathEdges.add(e);
                pathFlow = Math.min(pathFlow, e.capacity - e.flow);
                curr = e.u;
                pathNames.add(nodes.get(curr).name);
            }
            Collections.reverse(pathNames);
            String pathString = String.join(" -> ", pathNames);
            
            for (Edge e : pathEdges) e.isHighlighted = true;
            canvas.repaint();
            sleep(1000);

            for (Edge e : pathEdges) {
                e.flow += pathFlow;
                Edge reverse = adj.get(e.v).get(e.reverseEdgeIndex);
                reverse.flow -= pathFlow; 
                e.isHighlighted = false;
            }
            maxFlow += pathFlow;
            
            // Log Printing
            logArea.append("Path Found:\n  " + pathString + "\n");
            logArea.append("  Flow Added: " + pathFlow + "\n");
            logArea.append("  Total Flow: " + maxFlow + "\n\n");
            
            canvas.repaint();
            sleep(800);
        }

        logArea.append("**********************\n");
        logArea.append(" MAX FLOW: " + maxFlow + "\n");
        logArea.append("**********************\n");
        JOptionPane.showMessageDialog(this, "Max Flow: " + maxFlow);
    }

    private void addDirectedEdge(int u, int v, int cap) {
        Edge forward = new Edge(u, v, cap);
        Edge backward = new Edge(v, u, 0);
        forward.reverseEdgeIndex = adj.get(v).size();
        backward.reverseEdgeIndex = adj.get(u).size();
        adj.get(u).add(forward);
        adj.get(v).add(backward);
    }

    private int getOrCreateNodeIndex(String name) {
        int idx = getNodeIndex(name);
        if (idx == -1) { nodes.add(new Node(name)); return nodes.size() - 1; }
        return idx;
    }
    private int getNodeIndex(String name) {
        for (int i = 0; i < nodes.size(); i++) if (nodes.get(i).name.equals(name)) return i;
        return -1;
    }
    
    private void recalculateNodePositions() {
        int n = nodes.size();
        int cx = canvas.getWidth() / 2, cy = canvas.getHeight() / 2;
        int r = 200; 
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            nodes.get(i).x = cx + (int) (r * Math.cos(angle));
            nodes.get(i).y = cy + (int) (r * Math.sin(angle));
        }
    }
    private void sleep(int ms) { try { Thread.sleep(ms); } catch (Exception e) {} }
    // Run GUI on Event Dispatch Thread (Swing GUI Main Worker Thread)
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new FlowNetworkSolver().setVisible(true)); }
}