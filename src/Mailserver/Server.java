
package Mailserver;

import java.net.*;


/**
 *
 * @author Roumpoutsos Nikolaos - Sapountzis Ioannis
 */
public class Server {

    
    ServerSocket myServerSocket;
    int port = 5555;
    
    public static void main(String[] args) {
        Server myserver = new Server();
        myserver.Action();
    }

    public void Action() {
        System.out.println("Starting server...");

        try {
            
            try {
                
                System.out.println("Trying to bind to localhost on port " + Integer.toString(port) + "...");
                //make a ServerSocket and bind it to given port,
                myServerSocket = new ServerSocket(port);
            } catch (Exception e) { //catch any errors and print errors
                System.out.println("\nFatal Error:" + e.getMessage());
                return;
            }
            System.out.println("Server is up and listening...");
             
             MailHandler mailhandler=new MailHandler();
            
            while (true) {
                Socket mySocket = myServerSocket.accept();
                
                Handler myHandler = new Handler(mySocket,mailhandler);
                myHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
