public class Edge {
    public int u, v;             // Indices of start and end nodes
    public int capacity;         // Max capacity
    public int flow;             // Current flow
    public int reverseEdgeIndex; // Pointer to the reverse edge (for residual graph)
    public boolean isHighlighted; // For animation (Green color)

    public Edge(int u, int v, int capacity) {
        this.u = u;
        this.v = v;
        this.capacity = capacity;
        this.flow = 0;
        this.reverseEdgeIndex = -1;
        this.isHighlighted = false;
    }
}