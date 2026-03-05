package com.fifa.util;

public final class CleaningUtils {

    private CleaningUtils() {}

    public static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    public static boolean isMissing(String s) {
        if (s == null) return true;
        String t = s.trim().toLowerCase();
        return t.isEmpty() || t.equals("null") || t.equals("n/a") || t.equals("na")
            || t.equals("nan") || t.equals("?") || t.equals("??") || t.equals("???")
            || t.equals("none") || t.equals("missing");
    }

    public static int parseBoolean(String s) {
        if (isMissing(s)) return -1;
        String t = s.trim().toLowerCase();
        switch (t) {
            case "1": case "1.0": case "true": case "yes": case "y": case "t": return 1;
            case "0": case "0.0": case "false": case "no": case "n": case "f": return 0;
            default: return -1;
        }
    }

    public static String toTitleCase(String s) {
        if (isMissing(s)) return safe(s);
        String[] words = s.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0)));
            sb.append(word.substring(1));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static Integer parseScore(String s) {
        if (isMissing(s)) return null;
        try {
            int v = Integer.parseInt(s.trim());
            return (v >= 0) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseInteger(String s) {
        if (isMissing(s)) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
