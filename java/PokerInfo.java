// Project: 3 Card Poker
//  By: Ali Saeed
//      Description: PokerInfo class is used to communicate information about the game between the client and the server.
//          So if user able to move to the next stage in the clients program then the client will send that information
//          through the pokerInfo object to the server which displays what had happened. The same goes for if the user does something to the server program.

import java.io.Serializable;
import java.util.*;

public class PokerInfo implements Serializable {

    public ArrayList<Integer> cards; // stores the 52 cards in the deck
    public HashSet<Integer> queenHigh;  // storing the queen or higher cards
    public int moneyLost; // indicate the amount of money player has
    public int moneyWon; // store the amount of money player won
    public int ante; // players ante bet
    public int pairPlusAmount; // players pair plus bet
    public boolean isPairPlus; // indicate if the players cards form a pair plus
    public boolean chooseFold; // indicates if the player folded
    public ArrayList<Integer> userCards; // store the players cards
    public ArrayList<Integer> dealerCards; //stores the dealers cards
    public HashMap<Integer, ArrayList<Integer>> cardOrganization; // organizes the card information
    int gamePhase; // inducting where player is in the game
    int winner; // indicating the winner of the round
    boolean queenOrHigher; // indicating whether dealer has a queen or higher
    boolean sendMessage; // indicating if the message was sent
    public String message; // storing the message to send
    boolean pairChecked; // checking if the cards form a pair
    int pairPlusWin; // storing the amount of money the user won from the pair plus bet


    // PokerInfo:
    //      Constructor initializes all the game components to their starting phase. Takes in a boolean and two int values and then initializes those parameters to this classes fields
    //      Adds all 52 cards into the ArrayList called cards and creates new instances of userCards and dealerCards.
    //
    public PokerInfo(boolean isPairPlus, int ante, int pairPlusAmount) {
        this.cards = new ArrayList<>();
        for (int i = 1; i <= 52; i++) {
            cards.add(i);
        }
        this.queenHigh = new HashSet<>();
        int i = 0;
        while (i <= 52) {
            queenHigh.add(i + 11);
            queenHigh.add(i + 12);
            queenHigh.add(i + 13);
            i += 13;
        }
        this.cardOrganization = new HashMap<>();
        int cardNumber = 2;
        int shape = 1;
        for (i = 1; i <= cards.size(); i++) {
            if (cardNumber == 15) {
                cardNumber = 2;
                shape++;
            }

            ArrayList<Integer> cardDetails = new ArrayList<>(2);
            cardDetails.add(cardNumber++);
            cardDetails.add(shape);

            cardOrganization.put(i, cardDetails);
        }
        this.isPairPlus = isPairPlus;
        if(ante > 25 || ante < 5){
            ante = 0;
        }
        this.ante = ante;
        this.pairPlusAmount = pairPlusAmount;
        if(this.pairPlusAmount > 25 || this.pairPlusAmount < 5){
            this.pairPlusAmount = 0;
        }
        this.gamePhase = 1;
        this.userCards = new ArrayList<>();
        this.dealerCards = new ArrayList<>();
        this.chooseFold = false;
        this.queenOrHigher = false;
        this.winner = 0;
        this.moneyLost = 0;
        this.moneyWon = 0;
        this.pairChecked = false;
        this.pairPlusWin = 0;

    }


    // generateRandomCards:
    //      Description: Method will generate 3 random cards from the arrayList containing the 52 cards and then stores them into the arrayList that was passed in
    //      Parameter: takes in an arrayList which is the arrayList of either dealer or players
    //      Returns: void because we only want to modify the arraylist that was passed in
    //
    public void generateRandomCards(ArrayList<Integer> arrayList, ArrayList<Integer> arrayListOther) {
        arrayList.clear(); // clears the cards that were store from before
        Random nums = new Random(); // uses the random class to generate random numbers
        int cards = 0;
        while (cards < 3) {
            int rand = nums.nextInt(52) + 1; // gets a random number from 1 to 52. the +1 is for the offset.
            // to prevent the arrayList from duplicating values
            if (!arrayList.contains(rand) && !arrayListOther.contains(rand)) {
                cards++; // increment cards until arrayList contains three cards
                arrayList.add(rand);
            }
        }
    }


    // clear Method:
    //      Description: This will clear the components of pokerInfo. Only called when the game of player restarts
    //      Parameters: None
    //      Return: void
    public void clear() {
        moneyLost = 0;
        moneyWon = 0;
        ante = 0;
        pairPlusAmount = 0;
        isPairPlus = false;
        chooseFold = false;
        queenOrHigher = false;
        userCards.clear();
        dealerCards.clear();
        winner = 0;
        pairChecked = false;
        pairPlusWin = 0;
    }

    // pushedAnte Method:
    //      Description: this method will help not clear the ante or play plus when the next draw of cards happen
    //      Parameters: None
    //      Return: void
    public void pushedAnte() {
        queenOrHigher = false;
        userCards.clear();
        dealerCards.clear();
        winner = 0;
    }


    // evaluatePairPlus Method:
    //      Description: this method will take the three cards of player and will indicate if the players cards formed a pattern.
    //      Parameters: arrayList of the players cards
    //      Return: int
    public int evaluatePairPlus (ArrayList<Integer> player) {

        // reseting the variables for players cards
        boolean straight = false;
        boolean sameCards = false;
        boolean sameShape = false;
        boolean pair = false;

        // stores the number of each player's cards
        int CP1 = cardOrganization.get(player.get(0)).get(0);
        int CP2 = cardOrganization.get(player.get(1)).get(0);
        int CP3 = cardOrganization.get(player.get(2)).get(0);

        // storing the players cards in another array
        ArrayList<Integer> realRNumbers = new ArrayList<>(3);
        realRNumbers.add(CP1);
        realRNumbers.add(CP2);
        realRNumbers.add(CP3);

        Collections.sort(realRNumbers); // sorting the new array

        CP1 = realRNumbers.get(0);
        CP2 = realRNumbers.get(1);
        CP3 = realRNumbers.get(2);

        // stores the type of shape of players cards
        int SP1 = cardOrganization.get(player.get(0)).get(1);
        int SP2 = cardOrganization.get(player.get(1)).get(1);
        int SP3 = cardOrganization.get(player.get(2)).get(1);

        // comparing the players cards to see if they form a straight, pair, etc.
        if (CP2 == CP3 - 1 && CP1 == CP2 - 1) {
            straight = true; // if the cards are all off by one then the dealer has a straight
        }

        if (CP1 == CP2 || CP2 == CP3 || CP1 == CP3) {
            pair = true; // if the cards has two of the same cards then a pair if formed
        }

        if (CP1 == CP2 && CP2 == CP3) {
            sameCards = true; // if the cards are the same then a three of a kind is formed
        }

        if (SP1 == SP2 && SP2 == SP3) {
            sameShape = true; // if the cards contained the same shapes
        }

        // inducting what type of winning cards they form and returning the number that corresponds with that winning
        if (straight && sameShape) {
            return 1;
        }
        else if (sameCards) {
            return 2;
        }
        else if (straight) {
            return 3;
        }
        else if (sameShape) {
            return 4;
        }
        else if (pair) {
            return 5;
        }
        else {
            return 6;
        }


    }


    // evaluateCards Method:
    //      Description: Method will calculate both the players and dealers hand to see if they acquired one of the forms to a winning hand.
    //          This will allow us to determine who from player and dealer has the winning hand
    //      Parameters: two arrayList, one for the players hands and one for the dealers hand
    //      Return: int which indicates who won the game
    public int evaluateCards (ArrayList<Integer> dealer, ArrayList<Integer> player) {

        int dealerOrder = 0; // stores the highest formation of cards for dealers hand
        int playerOrder = 0; // stores the highest formation of cards for players hand

        // variables that indicate the type of formation that the cards formed
        boolean straight = false;
        boolean sameCards = false;
        boolean sameShape = false;
        boolean pair = false;

        // stores the number of each dealers cards
        int CD1 = cardOrganization.get(dealer.get(0)).get(0);
        int CD2 = cardOrganization.get(dealer.get(1)).get(0);
        int CD3 = cardOrganization.get(dealer.get(2)).get(0);

        ArrayList<Integer> realDNumbers = new ArrayList<>(3);
        realDNumbers.add(CD1);
        realDNumbers.add(CD2);
        realDNumbers.add(CD3);

        Collections.sort(realDNumbers);

        CD1 = realDNumbers.get(0);
        CD2 = realDNumbers.get(1);
        CD3 = realDNumbers.get(2);

        // stores the type of shape of dealers cards
        int SD1 = cardOrganization.get(dealer.get(0)).get(1);
        int SD2 = cardOrganization.get(dealer.get(1)).get(1);
        int SD3 = cardOrganization.get(dealer.get(2)).get(1);

        // comparing the dealers cards to see if they form a straight, pair, etc.
        if (CD2 == CD3 - 1 && CD1 == CD2 - 1) {
            straight = true; // if the cards are all off by one then the dealer has a straight
        }

        if (CD1 == CD2 || CD2 == CD3 || CD1 == CD3) {
            pair = true; // if the cards has two of the same cards then a pair if formed
        }

        if (CD1 == CD2 && CD2 == CD3) {
            sameCards = true; // if the cards are the same then a three of a kind is formed
        }

        if (SD1 == SD2 && SD2 == SD3) {
            sameShape = true; // if the cards contained the same shapes
        }

        // inducting what type of winning cards they form
        if (straight && sameShape) {
            dealerOrder = 1;
        }
        else if (sameCards) {
            dealerOrder = 2;
        }
        else if (straight) {
            dealerOrder = 3;
        }
        else if (sameShape) {
            dealerOrder = 4;
        }
        else if (pair) {
            dealerOrder = 5;
        }
        else {
            dealerOrder = 6;
        }


        // reseting the variables for players cards
        straight = false;
        sameCards = false;
        sameShape = false;
        pair = false;

        // stores the number of each player's cards
        int CP1 = cardOrganization.get(player.get(0)).get(0);
        int CP2 = cardOrganization.get(player.get(1)).get(0);
        int CP3 = cardOrganization.get(player.get(2)).get(0);

        ArrayList<Integer> realRNumbers = new ArrayList<>(3);
        realRNumbers.add(CP1);
        realRNumbers.add(CP2);
        realRNumbers.add(CP3);

        Collections.sort(realRNumbers);

        CP1 = realRNumbers.get(0);
        CP2 = realRNumbers.get(1);
        CP3 = realRNumbers.get(2);

        // stores the type of shape of players cards
        int SP1 = cardOrganization.get(player.get(0)).get(1);
        int SP2 = cardOrganization.get(player.get(1)).get(1);
        int SP3 = cardOrganization.get(player.get(2)).get(1);

        // comparing the players cards to see if they form a straight, pair, etc.
        if (CP2 == CP3 - 1 && CP1 == CP2 - 1) {
            straight = true; // if the cards are all off by one then the dealer has a straight
        }

        if (CP1 == CP2 || CP2 == CP3 || CP1 == CP3) {
            pair = true; // if the cards has two of the same cards then a pair if formed
        }

        if (CP1 == CP2 && CP2 == CP3) {
            sameCards = true; // if the cards are the same then a three of a kind is formed
        }

        if (SP1 == SP2 && SP2 == SP3) {
            sameShape = true; // if the cards contained the same shapes
        }

        // inducting what type of winning cards they form
        if (straight && sameShape) {
            playerOrder = 1;
        }
        else if (sameCards) {
            playerOrder = 2;
        }
        else if (straight) {
            playerOrder = 3;
        }
        else if (sameShape) {
            playerOrder = 4;
        }
        else if (pair) {
            playerOrder = 5;
        }
        else {
            playerOrder = 6;
        }

        // these conditions indicate who would the round based on the player and dealer cards.
        // returns a number depending on who wins, if player wins then a 2 is returned,
        // if the dealer wins then a 1 is returned, else the game is tied and a 3 is returned
        if (playerOrder < dealerOrder) {
            return 2;
        }
        else if (playerOrder == dealerOrder) {
            if (CP3 > CD3) {
                return 2;
            }
            else if (CP3 < CD3) {
                return 1;
            }
            else {
                return 3;
            }
        }
        else {
            return 1;
        }
    }
}