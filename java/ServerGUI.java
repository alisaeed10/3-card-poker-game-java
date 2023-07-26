// Project: 3 Card Poker
//  By: Ali Saeed
//      Description: Server GUI class is used to create the Graphical User Interface of the server.
//          This will allow the user to interact and see what the server is doing with each client that connects
//          The GUI contains different scenes which indicates the where the user is currently in the process of the game

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.ServerSocket;


public class ServerGUI {
    private Stage primaryStage; // used to indicate the next scene
    private ListView<String> messages; // the server uses listView to print all the info of the game
    private Server server; // the server object for creating a new server
    private int portNumber; // the por number that is used to listen to clients

    // ServerGUI Method:
    //      Description: constructor that sets the primary stage that was passed in with the one of this class.
    //             also creates a new reference of listView.
    //      Parameters: takes in Stage object and sets the stage of this class with the one passed in
    //      Returns: nothing
    public ServerGUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
        messages = new ListView<>();
    }

    // serverWelcomeScene Method:
    //      Description: This method creates the welcome screen of the server.
    //      Parameters: None
    //      Returns: Scene which is the scene created
    public Scene serverWelcomeScene() {
        // setting the title screen
        Text serverWelcome = new Text("Welcome to the Server!");
        serverWelcome.setFont(Font.font("Georgia", FontWeight.BOLD,50));

        // prompting user to enter port number
        Text portInputText = new Text("Enter Port Number");
        portInputText.setFont(new Font("Georgia",20));

        // creating the TextField to enter port number
        TextField portInput = new TextField();
        portInput.setPromptText("Port Number");

        // creating the button that lets user continue to the next scene
        Button continueButton = new Button("Continue");
        continueButton.setOnAction(e->{
            if (!portInput.getText().isEmpty() && !portInput.getText().matches(".*\\D.*")) {
                portNumber = Integer.valueOf(portInput.getText());
                primaryStage.setScene(serverDataScene());
                server = new Server(data -> {
                    Platform.runLater(() -> {
                        messages.getItems().add(data.toString());
                    });
                }, portNumber);
                messages.getItems().add("Server is now online...Waiting for clients.");
            }
            else {
                portInputText.setText("Invalid port number...Enter valid port number.");
            }
        });


        // creating the images of this scene
        Image pokerImage = new Image("Cards.png");
        ImageView pokerImageViewer = new ImageView(pokerImage);
        ImageView pokerImageViewer2 = new ImageView(pokerImage);
        pokerImageViewer.setFitWidth(250);
        pokerImageViewer.setFitHeight(350);
        pokerImageViewer2.setFitWidth(250);
        pokerImageViewer2.setFitHeight(350);

        // aligning the welcome text, button, and prompt text vertically
        VBox portTextAndField = new VBox(20, portInputText, portInput, continueButton);
        portTextAndField.setAlignment(Pos.CENTER);

        // aligning the images and the text with the button horizontally
        HBox portAndImages = new HBox(20, pokerImageViewer, portTextAndField, pokerImageViewer2);

        // aligning all the elements of the scene
        VBox sceneOrganization = new VBox(20, serverWelcome, portAndImages);
        sceneOrganization.setAlignment(Pos.CENTER);

        //setting up the borderPane
        BorderPane sceneLayout = new BorderPane();
        sceneLayout.setCenter(sceneOrganization);
        sceneLayout.setStyle("-fx-background-color: slategray;");
        sceneLayout.setPadding(new Insets(60));

        return new Scene(sceneLayout, 800, 800); // returning the scene
    }

    // serverDataScene Method:
    //      Description: This method creates the servers database scene which is the scene prints all the game information of the game
    //      Parameters: None
    //      Returns: Scene which is the scene created
    private Scene serverDataScene() {

        // creating the Title of the scene
        Text title = new Text("Server Database");
        title.setFont(Font.font("Georgia", FontWeight.BOLD,50));

        //creating the buttons to turn off and on the server
        Image redX = new Image("redX.png");
        ImageView x = new ImageView(redX);
        x.setFitHeight(20);
        x.setFitWidth(20);
        Image greenCheck = new Image("greenCheck.png");
        ImageView green = new ImageView(greenCheck);
        green.setFitHeight(25);
        green.setFitWidth(25);
        Button onButton = new Button("ON", green);
        Button offButton = new Button("OFF", x);

        // adding functionality to the off button
        offButton.setOnAction(e->{
            // turns off the server and then disconnects all the clients that were connected to the server
            try {
                server.serverSocket.close();
                server.serverOff = true;
                if (server.clients.size() > 1) {
                    messages.getItems().add("Server closed...All clients connections have been lost.");
                }
                else if (server.clients.size() == 1) {
                    messages.getItems().add("Server closed...Client connection lost.");
                }
                else {
                    messages.getItems().add("Server closed.");
                }
            }
            catch (Exception error) {
                messages.getItems().add("Error closing server socket.");
            }

            try {
                for (int i = 0; i < server.clients.size(); i++) {
                    server.clients.get(i).connection.close();
                }
                server.clients.clear();
                offButton.setDisable(true);
                onButton.setDisable(false);
            }
            catch (Exception error) {
                messages.getItems().add("Error closing client sockets.");
            }
        });
        offButton.setPrefSize(100,50);
        offButton.setStyle("-fx-font-size: 30px; ");
        offButton.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2px;");

        // adding functionality to the on button
        onButton.setOnAction(e->{
            // turns server back on and waits for clients to join
            server = new Server(data -> {
                Platform.runLater(() -> {
                    messages.getItems().add(data.toString());
                });
            }, portNumber);

            onButton.setDisable(true);
            offButton.setDisable(false);
            messages.getItems().add("Server is back online...Clients can now connect.");

        });
        onButton.setDisable(true);
        onButton.setPrefSize(100,50);
        onButton.setStyle("-fx-font-size: 30px; ");
        onButton.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2px;");


        //TextField temp = new TextField();
        //offButton.setOnAction(e->server.clients.get(0).send(temp.getText()));

        messages.setStyle("-fx-font-size: 15px; ");

        VBox buttonOrganization = new VBox (10, onButton, offButton);
        // setting the title and off button horizontally
        HBox titleAndButton = new HBox(130, title, buttonOrganization);

        //aligning the listView vertical to the title text
        VBox sceneOrganization = new VBox(20, titleAndButton, messages);

        // setting the border pane
        BorderPane sceneLayout = new BorderPane(sceneOrganization);
        sceneLayout.setPadding(new Insets(70));
        sceneLayout.setStyle("-fx-background-color: slategray;");

        return new Scene(sceneLayout, 800, 800); // return scene
    }
}