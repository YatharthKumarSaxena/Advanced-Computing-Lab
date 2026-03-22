import java.io.*;
import java.util.Scanner;

public class NFASimulator {
    static char[] states;
    static char[] symbols;
    static char startState;
    static char[] finalStates;
    static char[][][] transitions;

    public static void main(String[] args) {
        String fileName = "nfa.txt"; 
        Scanner sc = new Scanner(System.in);

        if (loadNFA(fileName)) {
            System.out.println("--- NFA Config Loaded (Strictly Char Array Based) ---");
            while (true) {
                System.out.print("\nEnter string: ");
                String inputString = sc.nextLine();
                if (inputString.equalsIgnoreCase("exit")) break;

                System.out.println("\n--- TRACING PATHS ---");
                
                boolean result = simulate(startState, inputString, 0);
                System.out.println("\nFINAL RESULT: " + (result ? "ACCEPTED " : "REJECTED "));
            }
        }
        sc.close();
    }

    static boolean simulate(char curr, String input, int idx) {
        // Base Case: String khatam ho gayi
        if (idx == input.length()) {
            boolean isFinal = isFinalState(curr);
            System.out.println("Checking final state [" + curr + "]: " + (isFinal ? "SUCCESS" : "FAILED"));
            return isFinal;
        }

        char sym = input.charAt(idx);
        int currIdx = getStateIndex(curr);
        int symIdx = getSymbolIndex(sym);

        // Dead End Check
        if (currIdx == -1 || symIdx == -1 || transitions[currIdx][symIdx] == null || transitions[currIdx][symIdx].length == 0) {
            System.out.println("DEAD END at [" + curr + "] for symbol '" + sym + "'");
            return false;
        }

        char[] nextStates = transitions[currIdx][symIdx];
        
        for (int i = 0; i < nextStates.length; i++) {
            char next = nextStates[i];
            
            // Log entry
            System.out.println("EXPLORING branch: " + curr + " --" + sym + "--> " + next);
            
            // Recursive call
            if (simulate(next, input, idx + 1)) {
                return true; // Agar ek bhi rasta success ho gaya, toh true return kar do
            } else {
                // Backtrack log
                System.out.println("PATH FAILED from [" + next + "]. Backtracking to [" + curr + "]");
            }
        }

        return false;
    }

    static boolean loadNFA(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String[] stStr = br.readLine().trim().split(",");
            states = new char[stStr.length];
            for (int i = 0; i < stStr.length; i++) states[i] = stStr[i].trim().charAt(0);
            
            String[] symStr = br.readLine().trim().split(",");
            symbols = new char[symStr.length];
            for (int i = 0; i < symStr.length; i++) symbols[i] = symStr[i].trim().charAt(0);
            
            startState = br.readLine().trim().charAt(0);
            
            String[] finStr = br.readLine().trim().split(",");
            finalStates = new char[finStr.length];
            for (int i = 0; i < finStr.length; i++) finalStates[i] = finStr[i].trim().charAt(0);
            
            transitions = new char[states.length][symbols.length][];

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
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
            return false;
        }
    }

    static int getStateIndex(char st) {
        for (int i = 0; i < states.length; i++) if (states[i] == st) return i;
        return -1;
    }

    static int getSymbolIndex(char sym) {
        for (int i = 0; i < symbols.length; i++) if (symbols[i] == sym) return i;
        return -1;
    }

    static boolean isFinalState(char st) {
        for (int i = 0; i < finalStates.length; i++) if (finalStates[i] == st) return true;
        return false;
    }
}