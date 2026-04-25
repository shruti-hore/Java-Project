package ui.controllers;

import ui.views.LoginView;
import ui.http.HttpAuthClient;
import auth.service.CryptoAdapter;
import auth.service.AuthService;

public class LoginController {
    public LoginController(LoginView view, HttpAuthClient client, CryptoAdapter adapter, AuthService auth, Runnable onSuccess) {}
    public LoginController(LoginView view, HttpAuthClient client, CryptoAdapter adapter) {}
    public void initialize() {}
}
