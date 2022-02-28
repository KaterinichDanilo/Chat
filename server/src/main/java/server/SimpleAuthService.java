package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleAuthService implements AuthService {
    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            users.add(new UserData("a" + i, "a" + i, "a" + i));
        }

    }

    @Override
    public String getNameByLoginAndPassword(String login, String password) {
        for (UserData u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData u : users) {
            if (u.login.equals(login) && u.nickname.equals(nickname)) {
                return false;
            }
        }
        return true;
    }
}

