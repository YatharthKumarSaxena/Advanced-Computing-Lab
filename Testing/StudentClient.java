import java.io.*;
import java.net.*;
import java.util.Scanner;

public class StudentClient {
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Advanced Student Database Interface ===");
        
        // 1. Ask for Tailscale IP at runtime
        System.out.print("Enter Server IP Address (e.g., 100.x.x.x): ");
        String serverAddress = scanner.nextLine().trim();

        // 2. Establish connection once
        try (Socket socket = new Socket(serverAddress, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("\n[CLIENT] Successfully connected to server at " + serverAddress);
            System.out.println("Available fields: enrollment number, faculty number, name, course, hostler, address");
            System.out.println("---------------------------------------------------------------------------------");

            // 3. Enter interactive session loop
            while (true) {
                System.out.print("\nEnter field name (or type 'exit' to quit): ");
                String fieldName = scanner.nextLine().trim();

                if (fieldName.equalsIgnoreCase("exit")) {
                    out.println("EXIT"); // Tell server to cleanly close its thread
                    System.out.println("[CLIENT] Terminating connection. Goodbye.");
                    break;
                }

                System.out.print("Enter target value to search for: ");
                String targetValue = scanner.nextLine().trim();

                // Format the payload and send to server
                String payload = fieldName + ":" + targetValue;
                out.println(payload);

                // Wait for and print the server's response
                String response = in.readLine();
                
                System.out.println(">> SERVER RESPONSE:");
                System.out.println("   " + response);
            }

        } catch (UnknownHostException e) {
            System.err.println("[CLIENT] Error: Cannot resolve server IP address.");
        } catch (ConnectException e) {
            System.err.println("[CLIENT] Error: Connection refused. Ensure Tailscale is running and Server is online.");
        } catch (IOException e) {
            System.err.println("[CLIENT] Network I/O error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}