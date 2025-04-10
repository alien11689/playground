package dpr.playground.taskprovider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private static final Map<String, String> tokens = new ConcurrentHashMap<>();

    public static void saveToken(String token, String username) {
        tokens.put(token, username);
    }

    public static Optional<String> getUserByToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }
}
