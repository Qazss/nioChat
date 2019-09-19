package Server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class MyServer {
    private Vector<ClientHandler> clients;
    //Logger logger = Logger.getLogger("file");

    public MyServer(){
        ServerSocket server = null;
        Socket socket = null;
        clients = new Vector<>();

        try {
            server = new ServerSocket(7184);
            System.out.println("Сервер запущен");

            AuthService.connect();

            while(true){
                socket = server.accept();
                System.out.println("Клиент подключен");
                new ClientHandler(socket, this);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void broadcastMessage(ClientHandler sender, String message){
        for(ClientHandler o: clients){
            if(!AuthService.checkBlackListRecord(o.getNickname(), sender.getNickname()))
            o.sendMessage(message);
        }
    }

    public void sendPersonalMsg(ClientHandler sender, String nickname, String message){
        for(ClientHandler o: clients){
            if(!AuthService.checkBlackListRecord(o.getNickname(), sender.getNickname())) {
                if (o.getNickname().equals(nickname) || o.getNickname().equals(sender.getNickname())) {
                    o.sendMessage(message);
                }
            } else {
                sender.sendMessage("system: Вы не можете отправлять сообщения пользователю " + nickname);
                break;
            }
        }
    }

    public boolean isNotAuthorised(String nickname){
        boolean isNotAuthorised = true;

        for(ClientHandler o: clients){
            if(o.getNickname().equals(nickname)){
                isNotAuthorised = false;
                break;
            }
        }
        return isNotAuthorised;
    }

    public void subscribe(ClientHandler client){
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client){
        clients.remove(client);
    }
}
