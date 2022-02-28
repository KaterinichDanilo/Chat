package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Commands.END)) {
                                sendMsg(Commands.END);
                                break;
                            }
                            if (str.startsWith(Commands.AUTH)) {
                                String[] token = str.split(" ", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                String newNick = server.getAuthService().getNameByLoginAndPassword(token[1], token[2]);
                                login = token[1];
                                if (newNick != null) {
                                    if (!server.isLogin(login)){
                                        nickname = newNick;
                                        sendMsg(Commands.AUTH_OK + " " + nickname);
                                        authenticated = true;
                                        server.subscribe(this);
                                        break;
                                    } else {
                                        sendMsg("Already logged into this account");
                                    }

                                } else {
                                    sendMsg("Login or password are incorrect");
                                    System.out.println("Login or password are incorrect");
                                }
                            }
                            if (str.startsWith(Commands.REG)) {
                                String[] token = str.split(" ");
                                if (token.length < 4) {
                                    continue;
                                }
                                if (server.getAuthService().registration(token[1], token[2], token[3])){
                                    sendMsg(Commands.REG_OK);
                                } else {
                                    sendMsg("Registration failed");
                                }
                            }
                        }
                    }
                    socket.setSoTimeout(0);
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")){
                            if (str.equals(Commands.END)) {
                                sendMsg(Commands.END);
                                break;
                            }

                            if (str.startsWith("/w ")) {
                                server.broadcastMsg(this, str.split(" ", 3)[2], str.split(" ", 3)[1]);
                            }
                        } else {
                            server.broadcastMsg(this, str, "EVERYONE");
                        }


                    }
                } catch (SocketTimeoutException e) {
                    sendMsg(Commands.END);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}