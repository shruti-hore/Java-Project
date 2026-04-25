package utils;

import auth.session.SessionState;

public class UserSession {
    private static SessionState session;

    public static void login(SessionState s) {
        session = s;
    }

    public static void logout() {
        if (session != null) {
            session.zero();
            session = null;
        }
    }

    public static SessionState getSession() {
        return session;
    }

    public static boolean isLoggedIn() {
        return session != null;
    }

    public static String getCurrentUserEmail() {
        return session != null ? session.getUserId() : "Guest";
    }
}
