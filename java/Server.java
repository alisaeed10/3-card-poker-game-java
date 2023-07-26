// Project: 3 Card Poker
//  By: Ali Saeed
//      Description: The server class is used create clients by entering a port number to listen to.
//          each time the client enters the port number the server creates a thread that will allow the communication between each-other.
//          Clients are then stored in an arrayList which makes for easy access to the client threads

import javafx.scene.control.ListView;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Server {
    private int count = 1; // indicate the number of servers on
    public ArrayList<ClientThreads> clients; // will store all the clients that are connected to the server
    Acceptor server; //object of type acceptor which turns on the server
    private Consumer<Serializable> callback; // object of type consumer which allows communication between client and server
    int portNumber; // portNumber that indicates what port the server is listening to
    ServerSocket serverSocket; // the socket that is used for the server
    boolean serverLaunched; // indicating if the server is on
    boolean serverOff; // indicating if the server is off

    // Server:
    //  Description: a Constructor that initializes all the components in the class to the default value or creates new instances of objects.
    //  Parameters: takes in Consumer class object and a string that is the port number that client entered.
    //  Returns: nothing
    Server(Consumer<Serializable> call, int portNumber){
        clients = new ArrayList<ClientThreads>();
        serverLaunched = false;
        serverOff = true;
        callback = call;
        server = new Acceptor();
        server.start();
        this.portNumber = portNumber;
    }

    // Acceptor Class:
    //  Description: a Class that extends Thread which creates the Server program. While the server program is running, it can keep listening to
    //      clients to join. Each client that joins the server will be added to the ArrayList of clients.
    public class Acceptor extends Thread {

        //
        // run Method:
        //  the method will try to create a new server socket then enters a while loop that will keep listening for clients to join the server
        //
        public void run() {
            // encased in a try block incase the server faces any issues
            try {

                serverSocket = new ServerSocket(portNumber);
                serverLaunched = true;
                serverOff = false;
                while(true) {
                    // this if statement limits the number of clients that can join
                    // a new client is created and then callback will print the message to the server, which then we add ot the arrayList of clients
                    PokerInfo newInstance = new PokerInfo(false, 0, 0);
                    ClientThreads client = new ClientThreads(serverSocket.accept(), newInstance, count);
                    // if the number of clenit is less then four then they can connect to the server
                    if (clients.size() < 4) {
                        callback.accept("Client: " + count + " has connected to server.");
                        clients.add(client);
                        client.start();
                        count++;
                    }
                    // else a message is printed to the server database
                    else {
                        callback.accept("A new client tried connecting but four players already exist...");
                        newInstance.gamePhase = -3;
                        client.start();
                    }

                }

            }//end of try
            catch (BindException e) {
                callback.accept("Server can't listen to listed port. Do you already have a server running?");
            }
            catch(Exception e) {
                if (!serverLaunched) {
                    callback.accept("Server socket did not launch");
                }
            }
        }//end of while
    }

    // ClientThreads Class:
    //      Description: The class is use to connect thread from the client to the server. Once the client thread is connected to the server socket the client.
    //         Client then opens its input and output streams to communicate with the server.
    //
    public class ClientThreads extends Thread {
        Socket connection; // socket that connects to the server socket
        int currCount; //
        ObjectInputStream in; // input stream that will receive output from the server
        ObjectOutputStream out; // output stream that will send input to the server
        PokerInfo pokerGame; // PokerInfo object to send the information to the server


        // ClientThreads Method:
        //  Description: a constructor that will assign connection to the servers' socket, pokerGame to pokerInfo object passed in,
        //      and currCount to the count passed in
        //  Parameters: takes in an object of Socket which is the server socket, an object of PokerInfo for sending information to the server,
        //      and an int data type count that updates currCount
        //  Returns: nothing
        ClientThreads(Socket s, PokerInfo pokerGame, int count){
            this.connection = s;
            this.pokerGame = pokerGame;
            this.currCount = count;
        }

        //
        // run Method:
        //  Description: run method will run the clients program until the thread connecting the server and client is terminated.
        //      in the run method the client will open the input and output streams to send and receive data to and from the server
        //  Parameters: None
        //  Returns: void
        public void run() {

            // tries to open the clients streams, if fails then the catch block will run
            try {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                connection.setTcpNoDelay(true);
                send(pokerGame);
            }
            catch(Exception e) {
                pokerGame.gamePhase = -1;
                send(pokerGame);
                callback.accept("I/O Stream objects for Client #" + currCount + " have failed to be created.");
            }
            // while loop will run
            while(true && pokerGame.gamePhase != -1) {
                try {
                    pokerGame = (PokerInfo) in.readObject();
                    //if the game phase in pokerInfo class is on 1 the, we move to the next phase and send the information to the server

                    if (pokerGame.gamePhase == -3) {
                        send(pokerGame);
                    }
                    else if (pokerGame.gamePhase == 0 || pokerGame.gamePhase == 00) {
                        if (pokerGame.gamePhase == 0) {
                            callback.accept("Client: " + currCount + " restarted the game.");
                        }
                        else {
                            callback.accept("Client: " + currCount + " started a new game.");
                        }
                        pokerGame.gamePhase = 1;
                        send(pokerGame);
                    }
                    else if (pokerGame.gamePhase == 1) {
                        if (pokerGame.isPairPlus) {
                            // sends message to server
                            callback.accept("Client: " + currCount + " placed an ante of $" + pokerGame.ante + " and a PairPlus amount of $" + pokerGame.pairPlusAmount + ".");
                        }
                        else {
                            // sends message to server
                            callback.accept("Client: " + currCount + " placed an ante of $" + pokerGame.ante + " and has opted out of PairPlus");
                        }

                        pokerGame.generateRandomCards(pokerGame.userCards, pokerGame.dealerCards);
                        pokerGame.gamePhase = 2;
                        send(pokerGame); // sending info of pokerGame
                    }
                    else if (pokerGame.gamePhase == 2) {
                        if (!pokerGame.chooseFold) {
                            if (pokerGame.isPairPlus && !pokerGame.pairChecked) {
                                int pairPlusWin = pokerGame.evaluatePairPlus(pokerGame.userCards);
                                if (pairPlusWin == 1) {
                                    pokerGame.pairPlusWin = pokerGame.pairPlusAmount * 40;
                                    callback.accept("Client: " + currCount + " wins the straight flush Pair Plus bet. Winning $" + pokerGame.pairPlusWin + "!!!");
                                    sendMessage("Player: " + currCount + " wins the straight flush Pair Plus bet!!!");
                                }
                                else if (pairPlusWin == 2) {
                                    pokerGame.pairPlusWin = pokerGame.pairPlusAmount * 30;
                                    callback.accept("Client: " + currCount + " wins the three of a kind Pair Plus bet. Winning $" + pokerGame.pairPlusWin + "!!!");
                                    sendMessage("Player: " + currCount + " wins the three of a kind Pair Plus bet!!!");
                                }
                                else if (pairPlusWin == 3) {
                                    pokerGame.pairPlusWin = pokerGame.pairPlusAmount * 6;
                                    callback.accept("Client: " + currCount + " wins the straight Pair Plus bet. Winning $" + pokerGame.pairPlusWin + "!!!");
                                    sendMessage("Player: " + currCount + " wins the straight Pair Plus bet!!!");
                                }
                                else if (pairPlusWin == 4) {
                                    pokerGame.pairPlusWin = pokerGame.pairPlusAmount * 3;
                                    callback.accept("Client: " + currCount + " wins the flush Pair Plus bet. Winning $" + pokerGame.pairPlusWin + "!!!");
                                    sendMessage("Player: " + currCount + " wins the flush Pair Plus bet!!!");
                                }
                                else if (pairPlusWin == 5) {
                                    pokerGame.pairPlusWin = pokerGame.pairPlusAmount;
                                    callback.accept("Client: " + currCount + " wins the pair Pair Plus bet. Winning $" + pokerGame.pairPlusWin + "!!!");
                                    sendMessage("Player: " + currCount + " wins the pair Pair Plus bet!!");
                                }
                                else {
                                    pokerGame.pairPlusWin = -1 * pokerGame.pairPlusAmount;
                                    callback.accept("Client: " + currCount + " did not have at least a pair...Pair Plus bet lost. Losing $" + pokerGame.pairPlusAmount + ".");
                                    sendMessage("Player: " + currCount + " did not have at least a pair...Pair Plus lost.");
                                }

                                pokerGame.pairChecked = true;

                            }

                            pokerGame.generateRandomCards(pokerGame.dealerCards, pokerGame.userCards);

                            for (int i = 0; i < pokerGame.dealerCards.size(); i++) {
                                if (pokerGame.queenHigh.contains(pokerGame.dealerCards.get(i))) {
                                    pokerGame.queenOrHigher = true;
                                    break;
                                }
                            }

                            if (pokerGame.queenOrHigher) {
                                pokerGame.winner = pokerGame.evaluateCards(pokerGame.dealerCards, pokerGame.userCards);

                                if (pokerGame.winner == 3) {
                                    callback.accept("Client: " + currCount + " game resulted in a draw...ante pushed.");
                                    sendMessage("Player: " + currCount + " game resulted in a draw...ante pushed.");
                                }
                                else if (pokerGame.winner == 2) {
                                    pokerGame.moneyWon = pokerGame.ante * 2;
                                    callback.accept("Client: " + currCount + " beats the dealer. Winning $" + pokerGame.moneyWon + "!!!");
                                    sendMessage("Player: " + currCount + " beats the dealer.");

                                    if (pokerGame.moneyWon + pokerGame.pairPlusWin > 0) {
                                        callback.accept("Client: " + currCount + " wins a total of $" + (pokerGame.moneyWon + pokerGame.pairPlusWin) + "!!!");
                                        sendMessage("Player: " + currCount + " nets positive winnings!!!");
                                    }
                                    else if (pokerGame.moneyWon + pokerGame.pairPlusWin < 0) {
                                        callback.accept("Client: " + currCount + " lost a total of $" + (pokerGame.moneyWon + pokerGame.pairPlusWin) + ".");
                                        sendMessage("Player: " + currCount + " nets negative winnings...");
                                    }
                                    else {
                                        callback.accept("Client: " + currCount + "breaks even.");
                                        sendMessage("Player: " + currCount + " breaks even.");
                                    }

                                }
                                else {
                                    pokerGame.moneyLost = pokerGame.ante * 2;
                                    callback.accept("Client: " + currCount + " loses to the dealer. Losing $" + pokerGame.moneyLost + ".");
                                    sendMessage("Player: " + currCount + " loses to the dealer.");

                                    if (pokerGame.moneyLost - pokerGame.pairPlusWin < 0) {
                                        callback.accept("Client: " + currCount + " wins a total of $" + (pokerGame.moneyLost - pokerGame.pairPlusWin) + "!!!");
                                        sendMessage("Player: " + currCount + " nets positive winnings!!!");
                                    }
                                    else if (pokerGame.moneyLost - pokerGame.pairPlusWin > 0) {
                                        callback.accept("Client: " + currCount + " loses a total of $" + (pokerGame.moneyLost - pokerGame.pairPlusWin) + ".");
                                        sendMessage("Player: " + currCount + " nets negative winnings...");
                                    }
                                    else {
                                        callback.accept("Client: " + currCount + "breaks even.");
                                        sendMessage("Player: " + currCount + " breaks even.");
                                    }
                                }
                            }
                            else {
                                callback.accept("Client: " + currCount + " ante got pushed...dealer did not have a queen or higher.");
                                sendMessage("Player: " + currCount + " ante got pushed...dealer did not have a queen or higher.");
                            }

                            pokerGame.gamePhase = 3;
                            send(pokerGame);
                        }
                        else {
                            pokerGame.moneyLost += pokerGame.ante * 2;
                            callback.accept("Client: " + currCount + " Folded.");
                            sendMessage("Client: " + currCount + " Folded.");
                            send(pokerGame);
                        }
                    }
                    else if (pokerGame.gamePhase == 3) {

                        pokerGame.pushedAnte();
                        pokerGame.generateRandomCards(pokerGame.userCards, pokerGame.dealerCards);
                        pokerGame.gamePhase = 2;
                        send(pokerGame);
                    }

                }
                // if the client disconnects
                catch(Exception e) {
                    if (pokerGame.gamePhase != -3 && !serverOff) {
                        callback.accept("Client: " + currCount + " no longer connected....ending game session.");
                        int index = clients.indexOf(this);
                        for (int i = 0; i < clients.size(); i++) {
                            if (i > index) {
                                clients.get(i).currCount--;
                            }
                        }
                        clients.remove(this);
                        count--;
                    }
                    break;
                }
            }
        }//end of run


        public void sendMessage(String message) {
            PokerInfo messenger = new PokerInfo(false, 0, 0);
            messenger.sendMessage = true;
            messenger.message = message;

            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).send(messenger);
            }

        }

        // send Method:
        //  Description: method is use to send the information of the client to the server. This is done by using the pokerInfo class object
        //  Parameters: takes in an object of type PokerInfo.
        //  Returns: Void
        public void send(PokerInfo data) {

            // encased in a try block incase an issue arises
            try {
                out.writeObject(data); // writes out the data
            }
            catch (Exception e) {
                callback.accept("Error sending message to client: " + currCount + ".");
                e.printStackTrace();
            }
        }
    }
}