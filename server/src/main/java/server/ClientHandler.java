package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (str.startsWith("/auth")) {
                                String[] token = str.split(" ", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                String newNick = server.getAuthService().getNameByLoginAndPassword(token[1], token[2]);
                                if (newNick != null) {
                                    nickname = newNick;
                                    sendMsg("/auth_ok "+ nickname);
                                    authenticated = true;
                                    server.subscribe(this);
                                    break;
                                } else {
                                    sendMsg("Login or password are incorrect");
                                    System.out.println("Login or password are incorrect");
                                }
                            }
                        }
                    }
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")){
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }

                            if (str.startsWith("/w ")) {
                                server.broadcastMsg(this, str.split(" ", 3)[2], str.split(" ", 3)[1]);
                            }
                        } else {
                            server.broadcastMsg(this, str, "EVERYONE");
                        }


                    }
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
}