// Project: 3 Card Poker
//  By: Ali Saeed
//      Description: the client class is used to receive and send data from the client to server
//          uses input and out put streams to send those data. The client is able to connect to the server
//          by entering an ip address and a port number that is the same as the servers port number.

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Client extends Thread {
    private Socket socketClient; // the socket that client uses to communicate with the server
    private ObjectOutputStream out; // an object of output stream
    private ObjectInputStream in; // an object of input stream
    private String IPAddress; // stores the ipAddress of that client entered
    private String portNumber; // stores the port number that client entered
    private Consumer<Serializable> callback; // used to communicated with server

    // Client Method:
    //      Description: this is a constructor for the client class. The constructor will set the ipaddress
    //          and port number that user enters to connect to server
    //      Parameters: takes in the Consumer object, and two strings being the ipaddress string and the port number
    //      Returns: nothing
    Client(Consumer<Serializable> call, String IPAddress, String portNumber){
        callback = call;
        this.IPAddress = IPAddress;
        this.portNumber = portNumber;
    }

    // run Method:
    //  Description: run method will run the clients program until the thread connecting the server and client is terminated.
    //      in the run method the client will open the input and output streams to send and receive data to and from the server
    //  Parameters: None
    //  Returns: void
    public void run() {

        try {
            socketClient= new Socket(IPAddress, Integer.valueOf(portNumber)); // creating the socket for client

            // opening the streams for client to send and receive data
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        }
        catch(Exception e) {
            socketClient = null;
            PokerInfo message = new PokerInfo(false, 0, 0);
            message.gamePhase = -2;
            callback.accept(message);
        }

        // keeps running until the server is turned off or the thread is cut off
        while(true && socketClient != null) {

            try {
                // reads in pokerinfo object
                PokerInfo message = (PokerInfo) in.readObject();
                callback.accept(message);
            }
            catch(Exception e) {
                PokerInfo message = new PokerInfo(false, 0, 0);
                message.gamePhase = -1;
                callback.accept(message);
                break;
            }
        }

    }

    // send Method:
    //  Description: method is used to send the information of the client to the server. This is done by using the pokerInfo class object
    //  Parameters: takes in an object of type PokerInfo.
    //  Returns: Void
    public void send(PokerInfo data) {
        // encased in a try block incase an issue arises
        try {
            out.writeObject(data); // writes out the data
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}