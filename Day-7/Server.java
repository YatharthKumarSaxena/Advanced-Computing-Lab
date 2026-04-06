
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner; // Yahan Scanner import kiya hai

public class Server {
    static int clientCounter = 0;

    // By default false rakha hai, run time par update hoga
    public static boolean isAdvancedSearch = false;

    static String time() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    static int countRecords() {
        int count = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader("students.csv"));
            br.readLine(); // skip header
            while (br.readLine() != null)
                count++;
            br.close();
        } catch (Exception e) {
        }
        return count;
    }

    public static void main(String[] args) {
        try {
            // SERVER BOOT CONFIGURATION (User Input)
            Scanner sc = new Scanner(System.in);
            System.out.println("=========================================");
            System.out.println("       SERVER CONFIGURATION SETUP        ");
            System.out.println("=========================================");
            System.out.print("Enable Advanced Search (Partial match, <, >)? (y/n): ");
            String choice = sc.nextLine();

            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
                isAdvancedSearch = true;
                System.out.println("ADVANCED Mode Activated!\n");
            } else {
                isAdvancedSearch = false;
                System.out.println("NORMAL Mode (Exact Match) Activated!\n");
            }

            int total = countRecords();
            System.out.println("[" + time() + "] CSV Loaded. Records = " + total);

            ServerSocket ss = new ServerSocket(5000);
            System.out.println("[" + time() + "] SERVER STARTED on PORT 5000");
            System.out.println("[" + time() + "] Waiting for client...");

            while (true) {
                Socket s = ss.accept();
                clientCounter++;
                int clientId = clientCounter;
                System.out.println("\n[" + time() + "] CLIENT-" + clientId + " CONNECTED");
                new ClientHandler(s, clientId).start();
            }
        } catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
    }
}

class ClientHandler extends Thread {
    Socket s;
    int clientId;

    ClientHandler(Socket s, int clientId) {
        this.s = s;
        this.clientId = clientId;
    }

    // Helper method to find column index dynamically
    private int getColIndex(String[] headers, String colName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(colName.trim()))
                return i;
        }
        return -1;
    }

    // Evaluate AND / OR Logic
    private boolean evaluateConditions(String[] headers, String[] data, String conditionStr) {
        if (conditionStr.equals("*"))
            return true;

        boolean isOr = conditionStr.contains(" OR ");
        String[] conditions = conditionStr.split(isOr ? " OR " : " AND ");

        boolean overallResult = !isOr; // True for AND, False for OR initially

        for (String c : conditions) {
            String operator = "";
            if (c.contains(">="))
                operator = ">=";
            else if (c.contains("<="))
                operator = "<=";
            else if (c.contains(">"))
                operator = ">";
            else if (c.contains("<"))
                operator = "<";
            else if (c.contains("="))
                operator = "=";

            if (operator.isEmpty())
                continue;

            int opIndex = c.indexOf(operator);
            String key = c.substring(0, opIndex).trim();
            String val = c.substring(opIndex + operator.length()).trim();

            int colIndex = getColIndex(headers, key);
            if (colIndex == -1 || colIndex >= data.length)
                continue;

            String dbValue = data[colIndex].trim();
            boolean match = false;

            // BOOLEAN CHECK: Normal vs Advanced Search + Comma (IN) Logic
            if (operator.equals("=")) {

                // Agar user ne comma (,) lagaya hai jaise "CSE, IT"
                if (val.contains(",")) {
                    String[] multiValues = val.split(",");
                    for (String v : multiValues) {
                        v = v.trim(); // Extra space hata do
                        if (!Server.isAdvancedSearch) {
                            if (dbValue.equalsIgnoreCase(v)) {
                                match = true;
                                break;
                            }
                        } else {
                            if (dbValue.toLowerCase().contains(v.toLowerCase())) {
                                match = true;
                                break;
                            }
                        }
                    }
                }
                // Agar single value hai jaise "CSE"
                else {
                    if (!Server.isAdvancedSearch) {
                        match = dbValue.equalsIgnoreCase(val);
                    } else {
                        match = dbValue.toLowerCase().contains(val.toLowerCase());
                    }
                }

            } else {
                // <, >, <=, >= MATH OPERATORS
                if (Server.isAdvancedSearch) {
                    try {
                        double dbNum = Double.parseDouble(dbValue);
                        double queryNum = Double.parseDouble(val);

                        if (operator.equals(">"))
                            match = dbNum > queryNum;
                        else if (operator.equals("<"))
                            match = dbNum < queryNum;
                        else if (operator.equals(">="))
                            match = dbNum >= queryNum;
                        else if (operator.equals("<="))
                            match = dbNum <= queryNum;
                    } catch (NumberFormatException e) {
                        match = false;
                    }
                }
            }

            if (isOr) {
                overallResult = overallResult || match;
            } else {
                overallResult = overallResult && match;
            }
        }
        return overallResult;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            while (true) {
                String fieldsStr = in.readLine();
                if (fieldsStr == null)
                    break;
                String conditionsStr = in.readLine();
                String limitStr = in.readLine();

                int limit = 10;
                try {
                    limit = Integer.parseInt(limitStr.trim());
                } catch (Exception e) {
                }

                // Handle 0 or Negative Limits
                if (limit <= 0) {
                    out.println("0 Record(s) Found (Limit is " + limit + ").");
                    out.println("END");
                    System.out.println("[" + Server.time() + "] Response sent to CLIENT-" + clientId);
                    continue; // Seedha agli query par jao, aage ka file handling skip karo
                }

                System.out.println("\n[" + Server.time() + "] CLIENT-" + clientId + " Request -> Fields: " + fieldsStr
                        + " | Cond: " + conditionsStr + " | Limit: " + limit);

                System.out.println("\n[" + Server.time() + "] CLIENT-" + clientId + " Request -> Fields: " + fieldsStr
                        + " | Cond: " + conditionsStr + " | Limit: " + limit);

                BufferedReader file = new BufferedReader(new FileReader("students.csv"));
                String headerLine = file.readLine(); // Read CSV Headers

                if (headerLine != null) {
                    String[] headers = headerLine.split(",");
                    String[] requestedFields = fieldsStr.trim().equals("*") ? headers : fieldsStr.split(",");

                    String line;
                    boolean foundAny = false;
                    int foundCount = 0;

                    while ((line = file.readLine()) != null) {
                        String[] data = line.split(",");

                        // Logic Check!
                        if (evaluateConditions(headers, data, conditionsStr)) {
                            foundAny = true;
                            out.println("---NEW_RECORD---");

                            // Print ONLY Selected Fields
                            for (String rf : requestedFields) {
                                int colIndex = getColIndex(headers, rf);
                                if (colIndex != -1 && colIndex < data.length) {
                                    out.println(headers[colIndex].trim() + ": " + data[colIndex].trim());
                                }
                            }

                            foundCount++;
                            if (foundCount >= limit)
                                break; // Apply Limit
                        }
                    }

                    if (!foundAny) {
                        out.println("No Record Found matching conditions.");
                    }
                }

                out.println("END");
                file.close();
                System.out.println("[" + Server.time() + "] Response sent to CLIENT-" + clientId);
            }

            s.close();
            System.out.println("[" + Server.time() + "] CLIENT-" + clientId + " DISCONNECTED");

        } catch (SocketException e) {
            System.out.println("[" + Server.time() + "] CLIENT-" + clientId + " disconnected unexpectedly.");
        } catch (Exception e) {
            System.out.println("[" + Server.time() + "] Error handling CLIENT-" + clientId);
            e.printStackTrace();
        }
    }
}