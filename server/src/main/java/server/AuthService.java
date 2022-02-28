package server;

public interface AuthService {
    String getNameByLoginAndPassword(String login, String password);
    boolean registration(String login, String password, String nickname);
}
