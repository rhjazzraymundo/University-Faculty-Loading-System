package util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private Validator() { }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmailOrEmpty(String s) {
        return isBlank(s) || EMAIL.matcher(s.trim()).matches();
    }

    public static boolean isValidPhoneOrEmpty(String s) {
        return isBlank(s) || PHONE.matcher(s.trim()).matches();
    }

    /** null when not a whole number. */
    public static Integer parseInt(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}