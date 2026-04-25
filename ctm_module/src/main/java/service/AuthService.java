package service;

import org.bson.Document;

public class AuthService {
    private final MongoService mongo = new MongoService();

    public boolean login(String username, String password) {
        Document user = mongo.getUserByUsername(username);
        if (user == null) return false;
        return password.equals(user.getString("password"));
    }

    public boolean register(String username, String password) {
        if (mongo.getUserByUsername(username) != null) return false;
        mongo.createUser(username, password);
        return true;
    }
}
