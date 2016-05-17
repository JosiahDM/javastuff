import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;
import java.util.Observable;
import java.util.Observer;
import java.io.*;
 
class ChatGUI implements ActionListener, Observer {
 
    ChatServerThread chatServer;
    JTextField serverIPInput, serverPortInput, userMessages;
    JTextArea textBox;
    JButton connectButton, disconnectButton, sendButton;
    JLabel friendIPLabel, friendPortLabel, myPortLabel;
    Socket connection;
    PrintWriter outMsg;
 
    private int port;
    private String username;
 
    ChatGUI(String username, int port) {
        this.port = port;
        this.username = username;
 
        // Create a new JFrame container
        JFrame jfrm = new JFrame("Kill The Messenger");
 
        // FlowLayout
        jfrm.setLayout(new FlowLayout());
 
        // Initial size.
        jfrm.setSize(650, 350);
 
        // Close behavior
        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        // Create the needed text fields for settings input
        serverIPInput = new JTextField(8);
        serverPortInput = new JTextField(3);
 
        // Text field for user messages to be sent
        userMessages = new JTextField(50);
 
        // text box for all messages in sesion
        textBox = new JTextArea("Waiting for friend to connect...", 10, 50);
        textBox.setEditable(false);
        textBox.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(textBox);
 
 
        // Create the buttons
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect/Exit");
        sendButton = new JButton("Send");
 
        // Set action commands
        userMessages.setActionCommand("msg");
        connectButton.setActionCommand("connect");
        disconnectButton.setActionCommand("disconnect");
        sendButton.setActionCommand("send");
 
        // Add action listeners for userMessages textarea, and buttons
        userMessages.addActionListener(this);
        connectButton.addActionListener(this);
        disconnectButton.addActionListener(this);
        sendButton.addActionListener(this);
 
 
        // create the labels
        friendIPLabel = new JLabel("Friend's IP: ");
        friendPortLabel = new JLabel("Friend's Port: ");
        myPortLabel = new JLabel("My Port: " + port);
 
        // Add compenents to the content pane
        jfrm.add(friendIPLabel);
        jfrm.add(serverIPInput);
        jfrm.add(friendPortLabel);
        jfrm.add(serverPortInput);
        // possibly need label placeholder here for where myIP will be auto-input
        // jfrm.add(myIp);   <--goal
        jfrm.add(myPortLabel);
        jfrm.add(connectButton);
        jfrm.add(disconnectButton);
        jfrm.add(scroll);
        jfrm.add(userMessages);
        jfrm.add(sendButton);
 
        // Display frame
        jfrm.setVisible(true);
 
        startServer();
 
    }
 
    // setup ChatServerThread, and make an observer for it. 
    public void startServer() { 
        chatServer = new ChatServerThread(port);
        new Thread(chatServer).start();
        chatServer.addObserver(this);   
    }
 
    // The updater for observables
    public void update(Observable o, Object arg) {
        // arg is the return code for the Observable 
        // when it calls notifyObservers(arg);
        if(arg == Integer.valueOf(1)) { 
            textBox.append(chatServer.getMsg() + "\n");
        }
    }
 
    public void sendMsg() {
        String message = userMessages.getText();
            userMessages.setText("");
            try {
                outMsg.println(username + ": " + message);
                textBox.append(username + ": " + message + "\n");
            } catch (NullPointerException e) {
                textBox.append("Error, probably not connected. -- " + e + "\n");
            }
    }
 
    // Handle action events
    public void actionPerformed(ActionEvent ae) {
        if(ae.getActionCommand().equals("connect")) {
            try { 
                InetAddress targetIP = InetAddress.getByName(serverIPInput.getText());
                int targetport = Integer.parseInt(serverPortInput.getText());
                connection = new Socket(targetIP, targetport);
                outMsg = new PrintWriter(connection.getOutputStream(), true);
                outMsg.println(username);
                textBox.append("Connected to " + targetIP + ":" + targetport + "\n");
            } catch (UnknownHostException e) {
                textBox.append("Unknown host: " + e + "\n");
            } catch (IOException ioe) {
                textBox.append("Error: Connection failed.\n");
            } catch(NumberFormatException nfe) {
                textBox.append("Error: Please enter a valid port number.\n");
            }
        }
        if(ae.getActionCommand().equals("send")) {
            sendMsg();
        }
        if(ae.getActionCommand().equals("msg")) {
            sendMsg();
        }
        if(ae.getActionCommand().equals("disconnect")) {
            System.exit(1);
        }
    }
 
    public static void main(String[] args) {
 
        if(args.length != 2) {
            System.out.println("Usage: java ChatGUI <username> <local port number>");
            return;
        }
 
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                new ChatGUI(args[0], Integer.parseInt(args[1]));
            }
        });
    }
}