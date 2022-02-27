package server;

public interface AuthService {
    String getNameByLoginAndPassword(String login, String password);
}
