package auth.repository;

import auth.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserRepository 
{
    private final Map<String, User> userStore = new HashMap<>();

    public void save(User user) 
    {
        userStore.put(user.getEmail(), user);
    }

    public User findByEmail(String email) 
    {
        return userStore.get(email);
    }

    public boolean exists(String email) 
    {
        return userStore.containsKey(email);
    }
}
