import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {
    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ServerSocket serverSocket;
    private Socket connection;

    public Server() throws HeadlessException {
        super("Server");
        userInputText = new JTextField();
        userInputText.setEditable(false);
        userInputText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());
                userInputText.setText("");
            }
        });
        add(userInputText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(600, 400);
        setVisible(true);
        setResizable(false);
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(7777, 100);
            while (true) {
                try {
                    waitForConnection();
                    setupStreams();
                    whileChatting();

                } catch (Exception e) {
                    showMessage("Connection is lost...");
                } finally {
                    closeConnection();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForConnection() throws IOException {
        showMessage("Waiting for client connection.");
        connection = serverSocket.accept();
        showMessage("Connecting with " + connection.getInetAddress().getHostName() + "\nConnection ready...");
    }

    private void setupStreams() throws IOException {
        outputStream = new ObjectOutputStream(connection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(connection.getInputStream());
        showMessage("Stream ready...\n");
    }

    private void whileChatting() throws IOException {
        String message = "Ready for chat.\n";
        sendMessage(message);
        readyToType(true);
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage(message);
            } catch (ClassNotFoundException e) {
                System.out.println("Send message unknown format.\n");
            }
        }
        while (!message.equals("STOP ALL!"));
    }

    private void closeConnection() {
        showMessage("\nConnection closed.");
        try {
            outputStream.close();
            inputStream.close();
            serverSocket.close();
            connection.close();
            readyToType(false);
        } catch (IOException e) {
            System.out.println("\nConnection cant be closed...");
        }
    }

    private void sendMessage(String message) {
        try {
            outputStream.writeObject("Server: " + message);
            outputStream.flush();
            showMessage("Server: " + message);
        } catch (IOException e) {
            chatWindow.append("\nCould not send message.");
        }
    }

    private void showMessage(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatWindow.append("\n"+ text);
            }
        });
    }

    private void readyToType(final Boolean typeReady) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                userInputText.setEditable(typeReady);
            }
        });
    }
}
