package utils;

import model.User;

public class UserSession {
    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "Guest";
    }

    public static String getCurrentUserName() {
        if (currentUser == null) return "User";
        String name = currentUser.getName();
        if (name == null || name.isEmpty() || name.equals("null")) {
            String email = currentUser.getEmail();
            return (email != null && email.contains("@")) ? email.split("@")[0] : "User";
        }
        return name;
    }
}
