import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class NFASimulator {
    static char[] states;
    static char[] symbols;
    static char startState;
    static char[] finalStates;
    static char[][][] transitions;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1. User se file path lena
        System.out.print("Enter the NFA configuration file path (e.g., nfa.txt): ");
        String fileName = sc.nextLine();

        if (loadNFA(fileName)) {
            System.out.println("\n--- NFA Config Loaded Successfully ---");
            System.out.println("States: " + String.join(", ", String.valueOf(states).split("(?<=.)")));
            System.out.println("Symbols: " + String.join(", ", String.valueOf(symbols).split("(?<=.)")));
            System.out.println("Final States: " + String.join(", ", String.valueOf(finalStates).split("(?<=.)")));
            System.out.println("Start State: " + startState);

            System.out.println("\nTransitions Table:");
            for (int i = 0; i < states.length; i++) {
                for (int j = 0; j < symbols.length; j++) {
                    if (transitions[i][j] != null) {
                        System.out.println(
                                "  " + states[i] + " --" + symbols[j] + "--> " +
                                        String.valueOf(transitions[i][j])
                                                .replaceAll("", ", ")
                                                .substring(2)
                                                .replaceAll(", $", ""));
                    }
                }
            }

            while (true) {
                System.out.print("\nEnter string to test (or 'exit' to quit): ");
                String inputString = sc.nextLine();
                if (inputString.equalsIgnoreCase("exit"))
                    break;

                System.out.println("\n--- TRACING PATHS ---");
                boolean result = simulate(startState, inputString, 0);
                System.out.println("\nFINAL RESULT: " + (result ? "ACCEPTED " : "REJECTED "));
            }
        } else {
            System.out.println("Error: Could not load file. Please check the path and format.");
        }
        sc.close();
    }

    static boolean simulate(char curr, String input, int idx) {
        if (idx == input.length()) {
            boolean isFinal = isFinalState(curr);
            System.out.println("Checking final state [" + curr + "]: " + (isFinal ? "SUCCESS" : "FAILED"));
            return isFinal;
        }

        char sym = input.charAt(idx);
        int currIdx = getStateIndex(curr);
        int symIdx = getSymbolIndex(sym);

        if (currIdx == -1 || symIdx == -1 || transitions[currIdx][symIdx] == null) {
            System.out.println("DEAD END at [" + curr + "] for symbol '" + sym + "'");
            return false;
        }

        char[] nextStates = transitions[currIdx][symIdx];
        for (char next : nextStates) {
            System.out.println("EXPLORING branch: " + curr + " --" + sym + "--> " + next);
            if (simulate(next, input, idx + 1))
                return true;
            System.out.println("PATH FAILED from [" + next + "]. Backtracking to [" + curr + "]");
        }
        return false;
    }

    static boolean loadNFA(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> transitionLines = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                if (line.startsWith("states=")) {
                    String val = line.split("=")[1];
                    String[] parts = val.split(",");
                    states = new char[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        states[i] = parts[i].trim().charAt(0);
                } else if (line.startsWith("symbols=")) {
                    String val = line.split("=")[1];
                    String[] parts = val.split(",");
                    symbols = new char[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        symbols[i] = parts[i].trim().charAt(0);
                } else if (line.startsWith("initialState=")) {
                    startState = line.split("=")[1].trim().charAt(0);
                } else if (line.startsWith("finalStates=")) {
                    String val = line.split("=")[1];
                    String[] parts = val.split(",");
                    finalStates = new char[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        finalStates[i] = parts[i].trim().charAt(0);
                } else {
                    // Agar koi label nahi hai, toh ise transition line maano
                    transitionLines.add(line);
                }
            }

            // Transitions array initialize karein (Metadata load hone ke baad)
            transitions = new char[states.length][symbols.length][];

            for (String tl : transitionLines) {
                String[] parts = tl.split(",");
                if (parts.length >= 3) {
                    int sIdx = getStateIndex(parts[0].trim().charAt(0));
                    int symIdx = getSymbolIndex(parts[1].trim().charAt(0));
                    if (sIdx != -1 && symIdx != -1) {
                        char[] dests = new char[parts.length - 2];
                        for (int i = 2; i < parts.length; i++) {
                            dests[i - 2] = parts[i].trim().charAt(0);
                        }
                        transitions[sIdx][symIdx] = dests;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("File Error: " + e.getMessage());
            return false;
        }
    }

    static int getStateIndex(char st) {
        for (int i = 0; i < states.length; i++)
            if (states[i] == st)
                return i;
        return -1;
    }

    static int getSymbolIndex(char sym) {
        for (int i = 0; i < symbols.length; i++)
            if (symbols[i] == sym)
                return i;
        return -1;
    }

    static boolean isFinalState(char st) {
        for (int i = 0; i < finalStates.length; i++)
            if (finalStates[i] == st)
                return true;
        return false;
    }
}