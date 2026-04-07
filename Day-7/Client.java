import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 10000);
            System.out.println("Connected to Server\n");

            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("=========================================");
                System.out.println("Enter Query Details (type 'exit' to quit)");
                System.out.println("=========================================");

                // 1. SELECT FIELDS
                System.out.print("Select Fields (e.g., Name, Branch, CGPA) or * for all: ");
                String fields = sc.nextLine();
                if (fields.equalsIgnoreCase("exit")) break;
                if (fields.trim().isEmpty()) fields = "*";

                // 2. WHERE CONDITIONS (AND/OR)
                System.out.print("Condition (e.g., Name=Siddhant AND Branch=CSE) or (Faculty=F1 OR Faculty=F2) or * for all: ");
                String conditions = sc.nextLine();
                if (conditions.trim().isEmpty()) conditions = "*";

                // 3. LIMIT
                System.out.print("Limit records (e.g., 5): ");
                String limit = sc.nextLine();
                if (limit.trim().isEmpty()) limit = "10"; // default limit

                // Send to Server
                out.println(fields);
                out.println(conditions);
                out.println(limit);

                System.out.println("\n--- Result from Server ---");

                String line;
                int recordCount = 0;
                while (!(line = in.readLine()).equals("END")) {
                    if (line.equals("---NEW_RECORD---")) {
                        recordCount++;
                        System.out.println("\nRecord " + recordCount + ":");
                        continue;
                    }
                    System.out.println(line);
                }

                if (recordCount == 0)
                    System.out.println("\nNo records found.");
                else
                    System.out.println("\n" + recordCount + " Record(s) Found.");

                System.out.println();
            }
            s.close();
            System.out.println("Disconnected.");

        } catch (ConnectException e) {
            System.out.println("\n Unable to connect to Server!");
            System.out.println(" Please start Server first, then run Client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}