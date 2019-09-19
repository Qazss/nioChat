package Application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Controller{

    @FXML
    VBox mainWindow;

    @FXML
    TextField messageWindow;

    @FXML
    Button butSend;

    @FXML
    HBox authrizationPannel;

    @FXML
    HBox messagePanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    Socket socket;
    DataInputStream inputStream;
    DataOutputStream outputStream;

    private final String IP = "localhost";
    private final int port = 7184;

    private SocketChannel socketChannel;
    private ByteBuffer buffer;

    @FXML
    public void initialize(){
        buffer = ByteBuffer.allocate(1024);

        mainWindow.setFillWidth(false);
        mainWindow.setSpacing(10);

        setAuthorized(true); // Пропускаем авторизацию
        try {
            createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createChannel() throws IOException {
        socketChannel = SocketChannel.open();
        SocketAddress socketAddr = new InetSocketAddress(IP, port);
        socketChannel.connect(socketAddr);
        System.out.println("Клиент подключен успешно");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (socketChannel.isOpen()) {
                        read();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void read() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        int bytesRead = socketChannel.read(buf);

        while (bytesRead != -1) {
            buf.flip();
            byte[] bytes = new byte[buf.limit()];
            buf.get(bytes);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    mainWindow.getChildren().add(new MessageTextArea(new String(bytes)));
                }
            });
            buf.clear();
            bytesRead = socketChannel.read(buf);
        }
    }

    public void send(){
        buffer.clear();
        buffer.put(messageWindow.getText().getBytes());

        buffer.flip();

        while(buffer.hasRemaining()) {
            try {
                socketChannel.write(buffer);
                System.out.println("сообщение отправлено");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        messageWindow.clear();
    }


    public void setAuthorized(boolean isAuthorised){

        if(isAuthorised){
            authrizationPannel.setVisible(false);
            authrizationPannel.setManaged(false);
            mainWindow.setVisible(true);
            mainWindow.setManaged(true);
            messagePanel.setVisible(true);
            messagePanel.setManaged(true);
        } else {
            authrizationPannel.setVisible(true);
            authrizationPannel.setManaged(true);
            mainWindow.setVisible(false);
            mainWindow.setManaged(false);
            messagePanel.setVisible(false);
            messagePanel.setManaged(false);
        }
    }

//    public void tryToAuthorise(){
//        if(socket == null || socket.isClosed())
//            connect();
//
//        try {
//            outputStream.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
//            loginField.clear();
//            passwordField.clear();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void connect() {
//        mainWindow.setFillWidth(false);
//        mainWindow.setSpacing(10);
//
//        try {
//            socket = new Socket(IP, port);
//            inputStream = new DataInputStream(socket.getInputStream());
//            outputStream = new DataOutputStream(socket.getOutputStream());
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        while (true){
//                            String authResult = inputStream.readUTF();
//                            if(authResult.startsWith("/authok")){
//                                setAuthorized(true);
//                                System.out.println("/authok Авторизовались");
//                                break;
//                            } else {
//                                JOptionPane.showMessageDialog(null, authResult);
//                            }
//                        }
//
//                        while(true) {
//                            String text = inputStream.readUTF();
//
//                            if(text.equals("/end")){
//                                outputStream.writeUTF("/serverClosed");
//                                break;
//                            }
//
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mainWindow.getChildren().add(new MessageTextArea(text));
//                                }
//                            });
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            inputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            outputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
//
//        } catch (EOFException e){
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(null, "Вы были отключены по таймауту (120 сек)");
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendMessage(){
//        if(messageWindow.getText().isEmpty()){
//            butSend.isDisabled();
//        } else {
//            try {
//                outputStream.writeUTF(messageWindow.getText());
//                messageWindow.clear();
//                messageWindow.requestFocus();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
