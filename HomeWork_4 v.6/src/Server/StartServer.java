package Server;


import org.apache.log4j.Logger;

import java.io.IOException;

public class StartServer {
    public static void main(String[] args) {
       // MyServer myServer = new MyServer();
        try {
            new Thread(new NioChatServer()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
