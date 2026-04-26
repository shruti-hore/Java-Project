package service;

/** @deprecated Replaced by auth.service.AuthService + HttpAuthClient. Do not call. */
@Deprecated
public class AuthService {
    public boolean login(String u, String p) {
        throw new UnsupportedOperationException("Use DashboardUI secure login flow");
    }
    public boolean register(String u, String p) {
        throw new UnsupportedOperationException("Use DashboardUI secure login flow");
    }
}
