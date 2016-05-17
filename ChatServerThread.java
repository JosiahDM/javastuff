import java.net.*;
import java.io.*;
import java.util.Observable;
import java.util.Observer;
 
public class ChatServerThread extends Observable implements Runnable {
 
    private String inText;
    private int port;
    private int code = 1;  // return code for notifyObservables method
 
    public ChatServerThread(int port) {
        this.port = port;
    }
 
    public String getMsg() { return inText; }
 
    public void update() {
        setChanged();
        notifyObservers(code);
    }
 
    public void run() {
        try(
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            // out sets up the stuff going out of the server, to client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            // in is what the client sends to server
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {
            // Check if client connects, then report it.
            /*if(clientSocket.isConnected()) { 
                inText = "Friend connected from " + clientSocket.getInetAddress() +
                                              ":" + clientSocket.getPort();
                update();
            }*/
            String friendsName = in.readLine();
            inText = friendsName + " connected from " + clientSocket.getInetAddress() +
                                                        ":" + clientSocket.getPort();
            update();
 
            // read what is sent to the bufferedReader in, until it is null
            // if null, client has disconnected.
            while(true) {
                inText = in.readLine();
                if(inText == null) {
                    inText = (friendsName + " Disconnected." + "\n");
                    update();
                    break;
                }
                update();
            }
        }
        catch(IOException e) {
            inText = "Error, probably not connected: " + e + "\n";
            update();
        } 
 
    }
}