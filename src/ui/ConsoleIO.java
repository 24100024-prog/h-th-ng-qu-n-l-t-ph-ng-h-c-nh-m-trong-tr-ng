package ui;

import java.util.Scanner;
import java.util.function.Function;

public class ConsoleIO {
    public static final String CANCEL_WORD = "huy";

    private final Scanner scanner;

    public ConsoleIO(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nKet thuc du lieu nhap, thoat chuong trinh.");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    private boolean isCancel(String s) {
        return s.equalsIgnoreCase(CANCEL_WORD);
    }

    public String ask(String prompt, Function<String, String> check) {
        while (true) {
            String in = readLine(prompt).trim();
            if (isCancel(in)) return null;
            String error = check.apply(in);
            if (error == null) return in;
            System.out.println("  -> " + error + " (nhap lai, hoac go '" + CANCEL_WORD + "' de quay lai)");
        }
    }

    public String askKeep(String label, String current, Function<String, String> check) {
        String prompt = label + " (hien tai: " + current + ", Enter de giu nguyen): ";
        while (true) {
            String in = readLine(prompt).trim();
            if (isCancel(in)) return null;
            if (in.isEmpty()) return current;
            String error = check.apply(in);
            if (error == null) return in;
            System.out.println("  -> " + error + " (nhap lai, hoac go '" + CANCEL_WORD + "' de quay lai)");
        }
    }

    public Integer askInt(String prompt, int min, String errorMessage) {
        String in = ask(prompt, s -> {
            try {
                return Integer.parseInt(s) >= min ? null : errorMessage;
            } catch (NumberFormatException e) {
                return errorMessage;
            }
        });
        return in == null ? null : Integer.valueOf(in);
    }

    public boolean askYesNo(String prompt) {
        while (true) {
            String in = readLine(prompt + " (y/n): ").trim().toLowerCase();
            if (in.equals("y")) return true;
            if (in.equals("n")) return false;
            System.out.println("  -> Chi nhap y hoac n.");
        }
    }
    public void guarded(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Loi: " + e.getMessage());
        }
    }
}