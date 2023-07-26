// Project: 3 Card Poker
//  By: Ali Saeed
//      Description: the ClientGUI class is the Graphical User Interface for the clients program.
//          The class allows user to interact and get a feel of how the game is being played.
//          There are four stages of the game and through each stage the program prompts the
//          user to enter information that will allow him to move to the next stage

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ClientGUI extends Application {
    private Stage primaryStage; // declaring object of type Stage to set the scenes of the game
    private Client client; // object of type client
    private PokerInfo pokerGame; // object of type pokerInfo to send info to server
    private ArrayList<ImageView> cardImages; // ArrayList to hold the images of all the cards
    private String currFont = "Georgia"; // stores the current Font of the program
    private String currColor = "-fx-background-color: seagreen"; // stores the current color of the program
    private String currImage = "Cards.png"; // stores the current Image of the program
    private int currLook = 0; // will indicate which Look the program is on
    private String backgroundCardColor = "BackOfCard.png"; // used to change the images of the cards facing backwards
    private int gamePoint = 0; // indicating what point we're at in the game
    public ListView<String> status; // prints the status of the game
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch(args);
    }

    // start Method:
    //  Description: allows user to start the game display the scene
    //  Parameters: takes in object of type Stage which allows the game to be played
    //  Returns:  void
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // assigns the primary stage of this clas with the one passed in
        this.primaryStage.setTitle("3 Card Poker"); //sets the title of the application
        this.primaryStage.setScene(clientWelcomeScene()); // creates the scene
        this.primaryStage.show(); // shows the scene
    }

    //  creatingMenu Method:
    //      Description: This method will display the options menu through the whole for the player accesses
    //      Parameters: None
    //      Return: MenuBar which is the menu.
    public MenuBar creatingMenu() {
        MenuBar menu = new MenuBar(); // creating an instance of menubar to display menu
        Menu menuOption = new Menu("Options"); // display the options text

        // creating each menu item of the game
        MenuItem freshStart = new MenuItem("Fresh Start");
        freshStart.setOnAction(e->{
            pokerGame.clear();
            status.getItems().clear();
            status.getItems().add("Game Messages:");
            pokerGame.gamePhase = 0;
            primaryStage.setScene(gameScene());
            client.send(pokerGame);
        }); // when fresh start is pressed the program will reset.

        MenuItem newLook = new MenuItem("New Look");
        newLook.setOnAction(e->{
            currLook++;
            if(currLook > 3){
                currLook = 0;
            }
            newLook();
            if(gamePoint == 0) {
                primaryStage.setScene(clientWelcomeScene());
            }
            else if(gamePoint == 1) {
                primaryStage.setScene(gameScene());
            }
            else {
                primaryStage.setScene(endScene());
            }
        }); // once player clicks on new look the look of the scene will change corresponding with what currLook value is.

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e->{
            primaryStage.close();
        });//exit when pressed will terminate connection of client to server

        // adding the menu-items into the menu drop for a dropdown effect
        menuOption.getItems().add(freshStart);
        menuOption.getItems().add(newLook);
        menuOption.getItems().add(exit);
        // inserting the menu options into the menu bar
        menu.getMenus().addAll(menuOption);

        return menu; // returning the menu bar
    }


    // ClientGuI method:
    //   Description: constructor that creates a new instances of the array list
    //      and stores the images of the cards into that array list
    //   Parameters: None
    //   Returns: None
    public ClientGUI() {
        //this.primaryStage = primaryStage;
        this.cardImages = new ArrayList<>();

        for (int i = 1; i <= 52; i++) {
            Image temp = new Image(i + ".png");
            cardImages.add(new ImageView(temp));
        }
        status = new ListView<>();
        status.setMinSize(370, 200);
        status.getItems().add("Game Messages:");
        //status.setStyle(currColor);
    }

    // clientWelcomeScene Method:
    //      Description: the Method will display the welcome that the client will see once player runs program.
    //          the client will be prompted to enter an ip address and a port number to connect a server.
    //          once client enters those items, then they can move on to play the game.
    //      Parameters: None
    //      Returns: Scene, for displaying the items of the scene
    //
    public Scene clientWelcomeScene() {
        gamePoint = 0;
        // creating the title
        Text title = new Text("Welcome to 3 Card Poker!");
        title.setFont(Font.font(currFont, FontWeight.BOLD,50));

        // creating the prompt
        Text connectPrompt = new Text("Connect to a Server");
        connectPrompt.setFont(new Font(currFont,18));

        // creating textField, for user to input ip address and portNumber
        TextField IPInputField = new TextField();
        TextField portInputField = new TextField();
        IPInputField.setPromptText("IP Address");
        portInputField.setPromptText("Port Number");

        // creating the button to connect to server once user enters ip address and port number
        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e-> {
            if (!IPInputField.getText().isEmpty() && IPInputField.getText().equals("127.0.0.1") && !portInputField.getText().isEmpty() && portInputField.getText().matches("^[0-9]*$")) {
                client = new Client(data -> {
                    Platform.runLater(() -> {
                        PokerInfo temp = (PokerInfo) data;
                        if (temp.sendMessage) {
                            status.getItems().add(temp.message);
                        }
                        else {
                            pokerGame = (PokerInfo) data;

                            if (pokerGame.gamePhase == -2) {
                                connectPrompt.setText("Not found. Try again..");
                            } else if (pokerGame.gamePhase == -3) {
                                connectPrompt.setText("Server is full. Try again later");
                            } else {
                                primaryStage.setScene(gameScene());
                            }
                        }
                    });
                }, IPInputField.getText(), portInputField.getText());
                client.start();
            }
            else {
                if (IPInputField.getText().equals("127.0.0.1")) {
                    connectPrompt.setText("Invalid port number...Enter valid port number.");
                }
                else {
                    connectPrompt.setText("Invalid IP address/port number...Try again.");
                }
            }
        });

        // creating the images of this scene
        Image pokerImage = new Image(currImage);
        ImageView pokerImageViewer = new ImageView(pokerImage);
        ImageView pokerImageViewer2 = new ImageView(pokerImage);
        pokerImageViewer.setFitWidth(250);
        pokerImageViewer.setFitHeight(350);
        pokerImageViewer2.setFitWidth(250);
        pokerImageViewer2.setFitHeight(350);

        // aligning the prompt text, two text-fields, and the button vertically
        VBox connectionPrompts = new VBox(20, connectPrompt, IPInputField, portInputField, connectButton);
        connectionPrompts.setAlignment(Pos.CENTER);
        // aligning all components horizontally and vertically
        HBox promptsAndImages = new HBox(35, pokerImageViewer, connectionPrompts, pokerImageViewer2);
        promptsAndImages.setAlignment(Pos.CENTER);
        VBox sceneOrganization = new VBox(50,title, promptsAndImages);
        sceneOrganization.setAlignment(Pos.CENTER);

        // creating the borderPane position the scene
        BorderPane sceneLayout = new BorderPane(sceneOrganization);
        sceneLayout.setCenter(sceneOrganization);
        sceneLayout.setStyle("-fx-background-color: slategray");
        MenuBar menu = creatingMenu();
        menu.getMenus().get(0).getItems().get(0).setDisable(true);
        menu.getMenus().get(0).getItems().get(2).setOnAction(e->primaryStage.close());
        sceneLayout.setTop(menu);

        return new Scene(sceneLayout, 1200, 800); // returning the new scene
    }

    // gameLost Method:
    //      Description: this method is called when the server disconnects while the client is in the middle of the game.
    //          the scene will display a connection lost text and has two buttons one for trying to reconnect and the other is for exiting the program.
    //      Parameters: None
    //      Return: Scene which is the scene that player sees when this method is called
    private Scene gameLost() {

        // creating the title of the scene
        Text connLost = new Text("Connection to the server has been lost..");
        connLost.setFont(new Font(currFont,35));
        connLost.setTextAlignment(TextAlignment.CENTER);

        // creating the prompt to have user move to a different scene
        Text playAgainPrompt = new Text("Would you like to play again?");
        playAgainPrompt.setTextAlignment(TextAlignment.CENTER);
        playAgainPrompt.setFont(new Font(currFont,35));

        // creating the play again button which allows user to try and reconnect with the server once more
        Button playAgain = new Button("Play Again");
        playAgain.setFont(new Font(currFont,30));
        playAgain.setAlignment(Pos.CENTER);
        playAgain.setOnAction(e->{
            primaryStage.setScene(clientWelcomeScene());
            status.getItems().clear();
            status.getItems().add("Game Messages:");
        });

        // creating the exit button to allow user to exit the program
        Button exit = new Button("Exit");
        exit.setFont(new Font(currFont,30));
        exit.setAlignment(Pos.CENTER);
        exit.setOnAction(e->primaryStage.close());

        // aligning the buttons horizontally
        HBox buttonLayout = new HBox(40, playAgain, exit);
        buttonLayout.setAlignment(Pos.CENTER);

        // aligning the components of the scene vertically
        VBox sceneOrganization = new VBox(30, connLost, playAgainPrompt, buttonLayout);
        sceneOrganization.setAlignment(Pos.CENTER);

        // positioning all the components in the center
        BorderPane sceneLayout = new BorderPane(sceneOrganization);
        sceneLayout.setStyle("-fx-background-color: slategray");

        return new Scene(sceneLayout, 1200, 800); // returning the scene

    }

    // gamePhase1 Method:
    //      Description: the game is split into different phase. this Method calls the first phase of the game.
    //          Phase one is where the player has to enter an ante bet and an optional pair plus bet between the ranges of 5 to 25 dollars
    //          Once player enters their bets they can then move on to the next phase.
    //      Parameters: None
    //      Returns: a scene which is the scene of game phase 1.
    private Scene gamePhase1(){

        VBox dealer; // used to organize the dealers cards
        VBox player; // used to organize players cards

        // displaying and sizing the cards backwards to not reveal the dealers or players cards.
        Image backofCard = new Image(backgroundCardColor);
        ImageView backOfCardView1 = new ImageView(backofCard);
        backOfCardView1.setFitWidth(150);
        backOfCardView1.setFitHeight(225);
        ImageView backOfCardView2 = new ImageView(backofCard);
        backOfCardView2.setFitWidth(150);
        backOfCardView2.setFitHeight(225);
        ImageView backOfCardView3 = new ImageView(backofCard);
        backOfCardView3.setFitWidth(150);
        backOfCardView3.setFitHeight(225);
        ImageView backOfCardView4 = new ImageView(backofCard);
        backOfCardView4.setFitWidth(150);
        backOfCardView4.setFitHeight(225);
        ImageView backOfCardView5 = new ImageView(backofCard);
        backOfCardView5.setFitWidth(150);
        backOfCardView5.setFitHeight(225);
        ImageView backOfCardView6 = new ImageView(backofCard);
        backOfCardView6.setFitWidth(150);
        backOfCardView6.setFitHeight(225);

        // displaying the total money of player
        Text totalMoney = new Text("Player's Ante: $"+ pokerGame.ante + "\n" + "Player's Play: $" + pokerGame.ante + "\n" + "Player's Pair Plus: $" + pokerGame.pairPlusAmount);
        totalMoney.setFont(new Font(currFont,17));

        Text totalWinnings = new Text("Total Won: $" + (pokerGame.moneyWon - pokerGame.moneyLost + pokerGame.pairPlusWin));
        totalWinnings.setFont(new Font(currFont,17));

        VBox moneyTextOrganization = new VBox(300, totalMoney, totalWinnings);

        // organize the dealer cards horizontally
        HBox dealerSide = new HBox(20, backOfCardView1, backOfCardView2, backOfCardView3);
        dealerSide.setAlignment(Pos.CENTER);
        Text dealerLabel = new Text("Dealer Hand");
        dealerLabel.setFont(new Font(currFont,20));
        dealer = new VBox(10, dealerSide, dealerLabel);
        dealer.setAlignment(Pos.CENTER);

        // organize the players cards horizontally
        HBox playerSide = new HBox(20, backOfCardView4, backOfCardView5, backOfCardView6);
        playerSide.setAlignment(Pos.CENTER);
        Text playerLabel = new Text("Player Hand");
        playerLabel.setFont(new Font(currFont,20));
        player = new VBox(10, playerLabel, playerSide);
        player.setAlignment(Pos.CENTER);

        // creating the ante bet section
        Text anteText = new Text("Place your ante wager $5-$25");
        anteText.setFont(new Font(currFont,20));
        TextField anteRequest = new TextField();
        anteRequest.setPromptText("Required");
        anteRequest.setMaxWidth(400);

        // creating the pair plus bet section
        Text pairText = new Text("Pair Plus Wager? $5-$25");
        pairText.setFont(new Font(currFont,20));
        TextField pairRequest = new TextField();
        pairRequest.setPromptText("Optional");
        pairRequest.setMaxWidth(400);

        // creating the button to move to the next phase once player enters an ante.
        Button start = new Button("Start");
        start.setOnAction(e->{
            if (!anteRequest.getText().isEmpty() && !anteRequest.getText().matches(".*\\D.*") && Integer.valueOf(anteRequest.getText()) >= 5 && Integer.valueOf(anteRequest.getText()) <= 25) {
                pokerGame.ante = Integer.valueOf(anteRequest.getText());
                if (!pairRequest.getText().isEmpty() && !pairRequest.getText().matches(".*\\D.*") && Integer.valueOf(pairRequest.getText()) >= 5 && Integer.valueOf(pairRequest.getText()) <= 25) {
                    pokerGame.isPairPlus = true;
                    pokerGame.pairPlusAmount = Integer.valueOf(pairRequest.getText());
                }
                if (pokerGame.isPairPlus || pairRequest.getText().isEmpty()) {
                    client.send(pokerGame);
                }
                else {
                    pairText.setText("Invalid wager amount.. Enter from $5-$25 only");
                }
            }
            else {
                anteText.setText("Invalid wager amount.. Enter from $5-$25 only");
                if (!pairRequest.getText().isEmpty()) {
                    if (pairRequest.getText().matches(".*\\D.*") || Integer.valueOf(pairRequest.getText()) < 5 || Integer.valueOf(pairRequest.getText()) > 25) {
                        pairText.setText("Invalid wager amount.. Enter from $5-$25 only");
                    }
                }
            }
        });
        start.setPrefSize(100,20);

        // aligning all the game components vertically
        VBox sceneOrganization = new VBox(10, dealer, anteText, anteRequest, pairText, pairRequest, start, player);
        sceneOrganization.setAlignment(Pos.CENTER);

        // creating the borderPane to display the components of the scene
        BorderPane sceneLayout = new BorderPane(sceneOrganization);
        sceneLayout.setTop(creatingMenu());
        sceneLayout.setStyle(currColor);
        sceneLayout.setCenter(sceneOrganization);
        sceneLayout.setRight(status);
        sceneLayout.setLeft(moneyTextOrganization);

        return new Scene(sceneLayout, 1200, 800); // returning the new scene
    }

    // gamePhase2 Method:
    //      Description: In this method the player is move to phase 2 of the game which where the player is shown their cards.
    //          Once the player sees their cards, they will have an option of playing with those cards or folding.
    //      Parameters: None
    //      Returns: Scene which is the phase 2 display screen
    private Scene gamePhase2() {

        VBox dealer; // used to organize the dealers cards
        VBox player; // used to organize players cards

        // displaying and sizing the images of the scene
        Image backofCard = new Image(backgroundCardColor);
        // players cards are taken from the arrayList that contained 3 random cards
        Image playerCard1 = new Image(pokerGame.userCards.get(0) + ".png");
        Image playerCard2 = new Image(pokerGame.userCards.get(1) + ".png");
        Image playerCard3 = new Image(pokerGame.userCards.get(2) + ".png");
        ImageView backOfCardView1 = new ImageView(backofCard);
        backOfCardView1.setFitWidth(150);
        backOfCardView1.setFitHeight(225);
        ImageView backOfCardView2 = new ImageView(backofCard);
        backOfCardView2.setFitWidth(150);
        backOfCardView2.setFitHeight(225);
        ImageView backOfCardView3 = new ImageView(backofCard);
        backOfCardView3.setFitWidth(150);
        backOfCardView3.setFitHeight(225);
        ImageView playerCardView1 = new ImageView(playerCard1);
        playerCardView1.setFitWidth(150);
        playerCardView1.setFitHeight(225);
        ImageView playerCardView2 = new ImageView(playerCard2);
        playerCardView2.setFitWidth(150);
        playerCardView2.setFitHeight(225);
        ImageView playerCardView3 = new ImageView(playerCard3);
        playerCardView3.setFitWidth(150);
        playerCardView3.setFitHeight(225);

        // displaying the total money of player
        Text totalMoney = new Text("Player's Ante: $"+ pokerGame.ante + "\n" + "Player's Play: $" + pokerGame.ante + "\n" + "Player's Pair Plus: $" + pokerGame.pairPlusAmount);
        totalMoney.setFont(new Font(currFont,17));

        Text totalWinnings = new Text("Total Won: $" + (pokerGame.moneyWon - pokerGame.moneyLost + pokerGame.pairPlusWin));
        totalWinnings.setFont(new Font(currFont,17));

        VBox moneyTextOrganization = new VBox(300, totalMoney, totalWinnings);

        // organize the dealers cards horizontally
        HBox dealerSide = new HBox(20, backOfCardView1, backOfCardView2, backOfCardView3);
        dealerSide.setAlignment(Pos.CENTER);
        Text dealerLabel = new Text("Dealer Hand");
        dealerLabel.setFont(new Font(currFont,20));
        dealer = new VBox(10, dealerSide, dealerLabel);
        dealer.setAlignment(Pos.CENTER);



        // organize the players cards horizontally
        HBox playerSide = new HBox(20, playerCardView1, playerCardView2, playerCardView3);
        playerSide.setAlignment(Pos.CENTER);
        Text playerLabel = new Text("Player Hand");
        playerLabel.setFont(new Font(currFont,20));
        player = new VBox(10, playerLabel, playerSide);
        player.setAlignment(Pos.CENTER);

        // displaying both buttons that allow player to move to the next scene
        Button play = new Button("Play");
        play.setOnAction(e->{
            client.send(pokerGame);
        });
        play.setPrefSize(150,25);
        Button fold = new Button("Fold");
        fold.setOnAction(e->{
          pokerGame.chooseFold = true;
          client.send(pokerGame);
        });
        fold.setPrefSize(150,25);

        // aligning both buttons horizontally
        HBox playFoldOrganization = new HBox(20, play, fold);
        playFoldOrganization.setAlignment(Pos.CENTER);

        //status.getItems().clear();
        // aligning game components vertically
        VBox sceneOrganization = new VBox(75, dealer, playFoldOrganization, player);
        sceneOrganization.setAlignment(Pos.CENTER);
        //creating the border pane
        BorderPane sceneLayout = new BorderPane(sceneOrganization);
        sceneLayout.setStyle(currColor);
        sceneLayout.setCenter(sceneOrganization);
        sceneLayout.setTop(creatingMenu());
        sceneLayout.setRight(status);
        sceneLayout.setLeft(moneyTextOrganization);

        return new Scene(sceneLayout, 1200, 800); // returning the new scene
    }

    // gamePhase3 Method:
    //      Description: this method will be called on the third phase of the game which is when the dealers cards are revealed.
    //          Then the player will be prompt to move to the end where it is determined whether the player wins or loses.
    //      Parameters: None
    //      Return: Scene,
    //
    private Scene gamePhase3() {

        // temp placement just to test server changes.
        VBox dealer; // used to organize the dealers cards
        VBox player; // used to organize players cards

        // displaying and sizing the images of the scene
        Image dealerCard1 = new Image(pokerGame.dealerCards.get(0) + ".png");
        Image dealerCard2 = new Image(pokerGame.dealerCards.get(1) + ".png");
        Image dealerCard3 = new Image(pokerGame.dealerCards.get(2) + ".png");
        // players cards are taken from the arrayList that contained 3 random cards
        Image playerCard1 = new Image(pokerGame.userCards.get(0) + ".png");
        Image playerCard2 = new Image(pokerGame.userCards.get(1) + ".png");
        Image playerCard3 = new Image(pokerGame.userCards.get(2) + ".png");
        ImageView backOfCardView1 = new ImageView(dealerCard1);
        backOfCardView1.setFitWidth(150);
        backOfCardView1.setFitHeight(225);
        ImageView backOfCardView2 = new ImageView(dealerCard2);
        backOfCardView2.setFitWidth(150);
        backOfCardView2.setFitHeight(225);
        ImageView backOfCardView3 = new ImageView(dealerCard3);
        backOfCardView3.setFitWidth(150);
        backOfCardView3.setFitHeight(225);
        ImageView playerCardView1 = new ImageView(playerCard1);
        playerCardView1.setFitWidth(150);
        playerCardView1.setFitHeight(225);
        ImageView playerCardView2 = new ImageView(playerCard2);
        playerCardView2.setFitWidth(150);
        playerCardView2.setFitHeight(225);
        ImageView playerCardView3 = new ImageView(playerCard3);
        playerCardView3.setFitWidth(150);
        playerCardView3.setFitHeight(225);


        // displaying the total money of player
        Text totalMoney = new Text("Player's Ante: $"+ pokerGame.ante + "\n" + "Player's Play: $" + pokerGame.ante + "\n" + "Player's Pair Plus: $" + pokerGame.pairPlusAmount);
        totalMoney.setFont(new Font(currFont,17));

        Text totalWinnings = new Text("Total Won: $" + (pokerGame.moneyWon - pokerGame.moneyLost + pokerGame.pairPlusWin));
        totalWinnings.setFont(new Font(currFont,17));

        VBox moneyTextOrganization = new VBox(300, totalMoney, totalWinnings);

        // organize the dealers cards horizontally
        HBox dealerSide = new HBox(20, backOfCardView1, backOfCardView2, backOfCardView3);
        dealerSide.setAlignment(Pos.CENTER);
        Text dealerLabel = new Text("Dealer Hand");
        dealerLabel.setFont(new Font(currFont,20));
        dealer = new VBox(10, dealerSide, dealerLabel);
        dealer.setAlignment(Pos.CENTER);

        // organize the players cards horizontally
        HBox playerSide = new HBox(20, playerCardView1, playerCardView2, playerCardView3);
        playerSide.setAlignment(Pos.CENTER);
        Text playerLabel = new Text("Player Hand");
        playerLabel.setFont(new Font(currFont,20));
        player = new VBox(10, playerLabel, playerSide);
        player.setAlignment(Pos.CENTER);


        // if the cards of the dealer and the cards of the player are the same
        if (pokerGame.winner == 3) {

            // displaying the text that indicates that the dealer won the round
            Text result = new Text("Game resulted in a draw...ante pushed.");
            result.setFont(new Font(currFont,20));

            // prompting the player to move to the next phase
            Text continuePrompt = new Text("Press continue to move onto next draw.");
            continuePrompt.setFont(new Font(currFont,20));

            // Move onto next round.
            Button continueButton = new Button("Continue");
            continueButton.setPrefSize(100,25);
            continueButton.setOnAction(e->{
                client.send(pokerGame);
            });

            VBox textOrganization = new VBox(15, result, continuePrompt);
            textOrganization.setAlignment(Pos.CENTER);

            // aligning game components vertically
            VBox sceneOrganization = new VBox(30, dealer, textOrganization, continueButton, player);
            sceneOrganization.setAlignment(Pos.CENTER);
            //creating the border pane
            BorderPane sceneLayout = new BorderPane(sceneOrganization);
            sceneLayout.setStyle(currColor);
            sceneLayout.setTop(creatingMenu());
            sceneLayout.setRight(status);
            sceneLayout.setLeft(moneyTextOrganization);

            return new Scene(sceneLayout, 1200, 800); // returning the new scene

        }
        // if the player won the round
        else if (pokerGame.winner == 2) {

            // displaying the text that indicates that the dealer won the round
            Text result = new Text("Player has the better cards.");
            result.setFont(new Font(currFont,20));

            // prompting the player to move to the next phase
            Text continuePrompt = new Text("Press continue to view results.");
            continuePrompt.setFont(new Font(currFont,20));

            // continue button to move to the next phase of the
            Button continueButton = new Button("Continue");
            continueButton.setPrefSize(100,25);
            continueButton.setOnAction(e->{
                primaryStage.setScene(endScene());
            });

            VBox textOrganization = new VBox(15, result, continuePrompt);
            textOrganization.setAlignment(Pos.CENTER);

            // aligning game components vertically
            VBox sceneOrganization = new VBox(30, dealer, textOrganization, continueButton, player);
            sceneOrganization.setAlignment(Pos.CENTER);
            //creating the border pane
            BorderPane sceneLayout = new BorderPane(sceneOrganization);
            sceneLayout.setStyle(currColor);
            sceneLayout.setCenter(sceneOrganization);
            sceneLayout.setTop(creatingMenu());
            sceneLayout.setRight(status);
            sceneLayout.setLeft(moneyTextOrganization);

            return new Scene(sceneLayout, 1200, 800); // returning the new scene
        }
        // if the player lost to the dealer
        else if (pokerGame.winner == 1) {

            // displaying the text that indicates that the dealer won the round
            Text result = new Text("Dealer has the better cards.");
            result.setFont(new Font(currFont,20));

            // prompting the player to move to the next phase
            Text continuePrompt = new Text("Press continue to view results.");
            continuePrompt.setFont(new Font(currFont,20));

            // continue button to move to the next phase of the
            Button continueButton = new Button("Continue");
            continueButton.setPrefSize(100,25);
            continueButton.setOnAction(e->{
                primaryStage.setScene(endScene());
            });

            VBox textOrganization = new VBox(15, result, continuePrompt);
            textOrganization.setAlignment(Pos.CENTER);

            // aligning game components vertically
            VBox sceneOrganization = new VBox(30, dealer, textOrganization, continueButton, player);
            sceneOrganization.setAlignment(Pos.CENTER);
            //creating the border pane
            BorderPane sceneLayout = new BorderPane(sceneOrganization);
            sceneLayout.setStyle(currColor);
            sceneLayout.setTop(creatingMenu());
            sceneLayout.setRight(status);
            sceneLayout.setLeft(moneyTextOrganization);

            return new Scene(sceneLayout, 1200, 800); // returning the new scene
        }
        // if the dealer didnt have a queen or higher
        else {
            if (!pokerGame.queenOrHigher) {
                //displaying text that indicate that the dealer doesn't have a queen or higher
                Text result = new Text("Dealer did not have a queen or higher...ante pushed.");
                result.setFont(new Font(currFont,20));

                // prompting user to contuine to the next phase
                Text continuePrompt = new Text("Press continue to move onto next draw.");
                continuePrompt.setFont(new Font(currFont,20));

                // Move onto next round.
                Button continueButton = new Button("Continue");
                continueButton.setPrefSize(100,25);
                continueButton.setOnAction(e->{
                    client.send(pokerGame);
                });

                VBox textOrganization = new VBox(15, result, continuePrompt);
                textOrganization.setAlignment(Pos.CENTER);

                // aligning game components vertically
                VBox sceneOrganization = new VBox(30, dealer, textOrganization, continueButton, player);
                sceneOrganization.setAlignment(Pos.CENTER);
                //creating the border pane
                BorderPane sceneLayout = new BorderPane(sceneOrganization);
                sceneLayout.setStyle(currColor);
                sceneLayout.setCenter(sceneOrganization);
                sceneLayout.setTop(creatingMenu());
                sceneLayout.setRight(status);
                sceneLayout.setLeft(moneyTextOrganization);

                return new Scene(sceneLayout, 1200, 800); // returning the new scene
            }
        }

        return new Scene(new TextField("Error"), 1200, 800); // returning the new scene
    }

    // gameScene Method:
    //      Description: The gameScene method controls the flow of the game through phases. Each phase is determine by the players action.
    //      Parameters: None
    //      Returns : Scene which displays which phase player is currently on.
    private Scene gameScene() {
        gamePoint = 1;
        if (pokerGame.chooseFold) {
            return endScene(); // returning the end scene
        }
        else if (pokerGame.gamePhase == -1) {
            return gameLost(); // returning scene for when player game disconnects
        }
        else if (pokerGame.gamePhase == 1) {
            return gamePhase1(); // returning the scene for phase 1
        }
        else if (pokerGame.gamePhase == 2) {
            return gamePhase2(); // returning the scene for phase 2
        }
        else if (pokerGame.gamePhase == 3) {
            return gamePhase3(); // returning the scene for phase 3
        }
        else {
            return new Scene(new TextField("Error"), 1200, 800);
        }
    }

    // endScene Method:
    //      Description: This method will display the end scene of the game which has the total amount of money the player won or lost.
    //          Will also display 2 buttons one for exiting the program and the other for playing another round
    //      Parameters: None
    //      Return: Scene which is the scene that the game will display once player reached the end of the game
    private Scene endScene() {

        gamePoint = 2; // keeps track of which scene player is currently at

        // adds up the total that was bet by the player
        //int amountBet = pokerGame.ante + pokerGame.ante +pokerGame.pairPlusAmount;

        // creating objects of text to store the text of the scene
        Text textTitle = new Text();
        Text payOut = new Text();

        // if the player folded then the text of the scene will change
        if(pokerGame.chooseFold){
            // creating the title
            textTitle.setText("Player Folds.");
            textTitle.setFont(new Font(currFont, 50));
            int totalMoney = pokerGame.moneyLost + pokerGame.pairPlusAmount; // adding both the money player lost and the pair plus bet that player made

            // creating the text to display the amount of money player lost
            payOut.setText("Amount Lost: $" +  totalMoney);
            payOut.setFont(new Font(currFont, 25));
        }
        // if the player won the round
        else if(pokerGame.winner == 2){
            // storing the totalMoney that player won from their bets
            int totalMoney = pokerGame.moneyWon + pokerGame.pairPlusWin;

            // if the money won and pair plus bet is greater then zero
            if (totalMoney > 0) {
                // creating the text for the end scene
                textTitle.setText("Congrats you Won!!");
                textTitle.setFont(new Font(currFont, 30));

                // displaying amount of money won or lost
                payOut.setText("Total Won: $" +  totalMoney);
                payOut.setFont(new Font(currFont, 25));
            }
            // if the pair plus is more than the total money won
            else if (totalMoney < 0) {

                // creating the title for the end scene
                textTitle.setText("You Won..But losing Pair Plus made you lose money.");
                textTitle.setFont(new Font(currFont, 30));

                // displaying amount of money won or lost
                payOut.setText("Total Lost: $" +  totalMoney);
                payOut.setFont(new Font(currFont, 25));
            }
            // else the player breaks even meaning that the bet is the same as the pair plus
            else {
                // creating the title for the end scene
                textTitle.setText("You Won, But losing Pair Plus made you break even.");
                textTitle.setFont(new Font(currFont, 30));

                // displaying amount of money won or lost
                payOut.setText("Total Won: $" +  totalMoney);
                payOut.setFont(new Font(currFont, 25));
            }
        }
        // else player lost the round to the dealer
        else {

            int totalMoney = pokerGame.moneyLost - pokerGame.pairPlusWin;

            // if the player didn't lose all their money
            if (totalMoney < 0) {
                // creating the title for the end scene
                textTitle.setText("You Lost but Pair Plus helped you win!!");
                textTitle.setFont(new Font(currFont, 30));

                // displaying amount of money won or lost
                payOut.setText("Amount Won: $" +  totalMoney);
                payOut.setFont(new Font(currFont, 25));
            }
            // if the player lost all their money
            else if (totalMoney > 0) {
                // creating the title for the end scene
                textTitle.setText("You Lose...");
                textTitle.setFont(new Font(currFont, 30));

                // displaying amount of money won or lost
                payOut.setText("Amount Lost: $" +  totalMoney);
                payOut.setFont(new Font(currFont, 25));
            }
            // if the player broke even
            else {
                // creating the title for the end scene
                textTitle.setText("You Lost but Pair Plus helped you break even.");
                textTitle.setFont(new Font(currFont, 30));

                // displaying amount of money won or lost
                payOut.setText("Amount Won: $" + totalMoney);
                payOut.setFont(new Font(currFont, 25));
            }
        }


        // creating the button for player to play again
        Button playAgain = new Button("Play Again");
        playAgain.setOnAction(e->{
           pokerGame.clear();
           status.getItems().clear();
           status.getItems().add("Game Messages:");
           pokerGame.gamePhase = 00;
           primaryStage.setScene(gameScene());
           client.send(pokerGame);
        });
        playAgain.setPrefSize(150,25);

        // creating the button for player to exit the program
        Button exit = new Button("Exit");
        exit.setOnAction(e->{
           primaryStage.close();
        });
        exit.setPrefSize(150,25);

        // creating the image of the game
        Image jokerCard = new Image(currImage);
        ImageView jokerPic = new ImageView(jokerCard);
        ImageView jokerPic2 = new ImageView(jokerCard);
        jokerPic.setFitWidth(100);
        jokerPic.setFitHeight(125);
        jokerPic2.setFitWidth(100);
        jokerPic2.setFitHeight(125);

        // aligning the all the text horizontally
        HBox textAlign = new HBox(20,jokerPic,textTitle,jokerPic2);
        textAlign.setAlignment(Pos.CENTER);

        // aligning the buttons horizontally.
        HBox buttonsAlignment = new HBox(50, playAgain, exit);
        buttonsAlignment.setAlignment(Pos.CENTER);

        //  aligning the components of the game vertically
        VBox component = new VBox(20, textAlign, payOut, buttonsAlignment);
        component.setAlignment(Pos.CENTER);

        // creating the Border Pane for positioning the elements
        BorderPane pane = new BorderPane();
        pane.setCenter(component);
        pane.setTop(creatingMenu());
        pane.setStyle(currColor);

        return new Scene(pane,1200,800); // returning the scene
    }


    // newLook Method:
    //      Description: this method will allow user to change the look of the scene.
    //          The user will be allowed to scene the scene 4 different times
    //      Parameters: None
    //      Return: void
    private void newLook() {

        // depending on what currLook value is, the look of the scene will change.
        if(currLook == 1) {
            currFont = "Verdana";
            currColor = "-fx-background-color: lightblue;";
            backgroundCardColor = "blueBackCard3.png";
            currImage = "ace.png";
        }
        else if(currLook == 2) {
            currFont = "Courier New";
            currColor = "-fx-background-color: cornflowerblue;";
            backgroundCardColor = "BackOfCard.png";
            currImage = "Cards.png";
        }
        else if (currLook == 3) {
            currFont = "Roboto Slab";
            currColor = "-fx-background-color: slategray";
            backgroundCardColor = "blueBackCard3.png";
            currImage = "joker.png";
        }
        else {
            currFont = "Georgia";
            currColor = "-fx-background-color: seagreen";
            backgroundCardColor = "BackOfCard.png";
            currImage = "Cards.png";
        }
    }

}