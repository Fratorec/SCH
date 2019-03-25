import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends JFrame {
    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String message = "";
    private String serverIp;
    private Socket socket;


    public Client(String host) throws HeadlessException {
        super("Client");
        serverIp = host;
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
        chatWindow.setBackground(Color.lightGray);
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(600, 400);
        setVisible(true);
        setResizable(false);
    }

    public void startClient() {
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (Exception e) {
            showMessage("\nClient connection is lost...");
        } finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException {
        showMessage("Trying to connect...");
        socket = new Socket(InetAddress.getByName(serverIp), 7777);
        showMessage("You are connected to " + socket.getInetAddress().getHostName());
    }

    private void setupStreams() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("Stream ready...\n");

    }

    private void whileChatting() throws IOException {
        readyToType(true);
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage(message);
            } catch (ClassNotFoundException e) {
                showMessage("\nSomething go wrong...");
            }
        }
        while (!message.equals("STOP ALL!!!"));
    }

    private void closeConnection() {
        showMessage("\nTry to close connection...");
        readyToType(false);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();

        } catch (IOException e) {
            showMessage("\nConnection cant be closed...");
        } finally {
            showMessage("\nConnection is closed...");
        }
    }

    private void sendMessage(String message) {
        try {
            outputStream.writeObject("Client: " + message);
            outputStream.flush();
            showMessage("Client: " + message);
        } catch (IOException e) {
            chatWindow.append("\nCould not sent message...");
        }
    }

    private void showMessage(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatWindow.append("\n" + text);
            }
        });
    }

    private void readyToType(final boolean typeReady) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                userInputText.setEditable(typeReady);
            }
        });
    }


}
