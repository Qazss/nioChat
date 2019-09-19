package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ClientHandler {
    private Socket socket;
    private MyServer server;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String nickname;

    public ClientHandler(Socket socket, MyServer server){
        this.socket = socket;
        this.server = server;

        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            socket.setSoTimeout(120000);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String authorizationData = inputStream.readUTF();
                            String[] tokens = authorizationData.split(" ");
                            String nickSQL = AuthService.getNickByAuthorization(tokens[1], tokens[2]);

                            if (authorizationData.startsWith("/auth") && server.isNotAuthorised(nickSQL)) {
                                if (nickSQL != null) {
                                    nickname = nickSQL;
                                    sendMessage("/authok");
                                    server.subscribe(ClientHandler.this);
                                    printMessageHistory();
                                    break;
                                } else {
                                    sendMessage("Неверный логин или пароль");
                                }
                            } else {
                                sendMessage("Данный пользователь уже авторизирован");
                            }
                        }

                        while (true) {
                            String text = inputStream.readUTF();
                            Date date = new Date();
                            SimpleDateFormat currentTime = new SimpleDateFormat("E hh:mm:ss");

                            if (text.startsWith("/")) {
                                if (text.equals("/end")) {
                                    server.broadcastMessage(ClientHandler.this, "(" + currentTime.format(date) + ")" + nickname + " покинул чат");
                                    break;
                                } else if (text.startsWith("/w")) {
                                    String[] personalMsg = text.split(" ", 3);
                                    server.sendPersonalMsg(ClientHandler.this, personalMsg[1],
                                            "(" + currentTime.format(date) + ")" + nickname + " to " + personalMsg[1] + ": " + personalMsg[2]);
                                } else if (text.startsWith("/blacklist")) {
                                    String[] blacklist = text.split(" ", 3);
                                    sendMessage("system: " + AuthService.addToBlacklist(nickname, blacklist[1]));
                                }
                            } else {
                                String message = "(" + currentTime.format(date) + ")" + nickname + ": " + text;
                                server.broadcastMessage(ClientHandler.this, message);
                                AuthService.addToMessageHistory(nickname, message);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String text){
        try {
            outputStream.writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printMessageHistory() throws SQLException {
        ResultSet resultSet = AuthService.getMessageHistory();

        while (resultSet.next()) {
            if(!AuthService.checkBlackListRecord(nickname, resultSet.getString(1))) {
                sendMessage(resultSet.getString(2));
            }
        }
    }

    public String getNickname() {
        return nickname;
    }
}
