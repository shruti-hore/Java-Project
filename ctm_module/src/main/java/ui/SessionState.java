package ui;

/**
 * Global session state holder for the application.
 * Simplified for Phase 1.
 */
public class SessionState {

    private static String token;

    public static void setToken(String jwt) {
        token = jwt;
    }

    public static String getToken() {
        return token;
    }

    public static void clear() {
        token = null;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }
}
