public class Node {
    public String name;
    public int x, y;
/*
    On Swing canvas, position of node is determined by its (x, y) coordinates:
    x → horizontal position
    y → vertical position 
*/
    public Node(String name) {
        this.name = name;
    }
}