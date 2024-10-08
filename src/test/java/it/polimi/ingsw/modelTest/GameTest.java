package it.polimi.ingsw.modelTest;

import com.google.gson.Gson;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.*;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.networking.ClientRmi;
import it.polimi.ingsw.networking.VirtualServer;
import it.polimi.ingsw.networking.VirtualView;
import it.polimi.ingsw.util.Print;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

public class GameTest {

    /*
    NB to Dispose Singleton use these commands at the end of method:
        testGame.end();
        testGame.nextState();
     */



    public ArrayList<ResourceCard> getOrderedResourceDeck() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("src/main/resources/it/polimi/ingsw/model/ResourceCards.json")) {
            ResourceCard[] tempResource = gson.fromJson(reader, ResourceCard[].class);
            ArrayList<ResourceCard> tempDeck = new ArrayList<>();
            Collections.addAll(tempDeck, tempResource);
            return tempDeck;
        } catch (IOException e) {
            return null;
        }
    }

    public ArrayList<GoldCard> getOrderedGoldDeck() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("src/main/resources/it/polimi/ingsw/model/GoldCards.json")) {
            GoldCard[] tempResource = gson.fromJson(reader, GoldCard[].class);
            ArrayList<GoldCard> tempDeck = new ArrayList<>();
            Collections.addAll(tempDeck, tempResource);
            return tempDeck;
        } catch (IOException e) {
            return null;
        }
    }

    public ArrayList<AchievementCard> getOrderedAchievementDeck() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("src/main/resources/it/polimi/ingsw/model/AchievementCards.json")) {
            AchievementCard[] tempAchievement = gson.fromJson(reader, AchievementCard[].class);
            ArrayList<AchievementCard> tempDeck = new ArrayList<>();
            for (AchievementCard achievementCard : tempAchievement)
                tempDeck.add(new AchievementCard(achievementCard.getPoints(), achievementCard.getResource(), achievementCard.getStrategyType(), achievementCard.getItem(), achievementCard.getID()));
            return tempDeck;
        } catch (IOException e) {
            return null;
        }
    }





    @Test
    void IntegrityTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();

        Player fake1 = new Player("Marco");
        players.add(fake1);
        Player fake2 = new Player("Luca");
        players.add(fake2);
        Player fake3 = new Player("Andrea");
        players.add(fake3);
        Player fake4 = new Player("Paolo");
        players.add(fake4);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);
        // Verifica che ogni giocatore abbia un colore assegnato
        for (Player player : testGame.getPlayers()) {
            assertNotEquals(Color.NONE, player.getColor(), "Il colore del giocatore non è stato assegnato correttamente");
        }

        // Verifica che i colori dei giocatori siano diversi
        for (int i = 0; i < testGame.getPlayersNumber() - 1; i++) {
            for (int j = i + 1; j < testGame.getPlayersNumber(); j++) {
                System.out.println(testGame.getPlayers().get(i).getColor());
                System.out.println(testGame.getPlayers().get(j).getColor());
                assertNotEquals(testGame.getPlayers().get(i).getColor(), testGame.getPlayers().get(j).getColor(), "I colori dei giocatori sono uguali");
            }
        }
        assertEquals(testGame.getGameState(), GameState.READY);
        //To Reset instance
        while (!testGame.getGameState().equals(GameState.END)) {
            testGame.nextState();
        }
        testGame.nextState();
        assertEquals(testGame.getGameState(), GameState.LOBBY);
        assertTrue(testGame.getPlayers().isEmpty());
        assertEquals(testGame.getPlayersNumber(), 0);
        assertEquals(testGame.getCurrPlayer(), 0);
        testGame.end();
        testGame.nextState();
    }

    @Test
    void nextStateTest() {
        Game testGame = Game.getInstance();
        GameState[] vetStati = new GameState[8];
        vetStati[0] = GameState.LOBBY;
        vetStati[1] = GameState.INIT;
        vetStati[2] = GameState.READY;
        vetStati[3] = GameState.SELECTACHIEVEMENT;
        vetStati[4] = GameState.GAME;
        vetStati[5] = GameState.LASTROUND;
        vetStati[6] = GameState.FINALSCORE;
        vetStati[7] = GameState.END;

        assertEquals(testGame.getGameState(), vetStati[0]);
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[1]);
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[2]);
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[3]);
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[4]);
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[5]);
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[6]);
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[7]);
        //Dispose GameTest
        testGame.nextState();
        assertEquals(testGame.getGameState(), vetStati[0]);
        assertTrue(testGame.getPlayers().isEmpty());
        assertEquals(testGame.getPlayersNumber(), 0);
        assertEquals(testGame.getCurrPlayer(), 0);

    }

    @Test
    void nextPlayerTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();

        Player fake1 = new Player("Marco");
        fake1.setOnline(true);
        players.add(fake1);
        Player fake2 = new Player("Luca");
        fake2.setOnline(true);
        players.add(fake2);
        Player fake3 = new Player("Andrea");
        fake3.setOnline(true);
        players.add(fake3);
        Player fake4 = new Player("Paolo");
        fake4.setOnline(true);
        players.add(fake4);

        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        clients.add(cli);
        clients.add(cli);
        clients.add(cli);

        testGame.addPlayers(players, clients);
        testGame.setGameState(GameState.GAME);
        testGame.getPlayers().get(0).addPoints(20);
        for (int j = 0; j < testGame.getPlayersNumber(); j++) {
            testGame.nextPlayer();
            assertEquals(GameState.GAME, testGame.getGameState());
            int curr = testGame.getCurrPlayer();
            testGame.nextPlayer();
            assertEquals(GameState.GAME, testGame.getGameState());
            if (curr == testGame.getPlayersNumber() - 1)
                assertEquals(testGame.getCurrPlayer(), 0);
            else
                assertEquals(testGame.getCurrPlayer(), curr + 1);
        }
        testGame.end();
        testGame.nextState();
    }




    @Test
    void handsTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        Player fake2 = new Player("Luca");
        players.add(fake2);
        Player fake3 = new Player("Andrea");
        players.add(fake3);
        Player fake4 = new Player("Paolo");
        players.add(fake4);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);

        // in questo for controllo che ogni giocatore abbia effettivamente due carte achievement in secretAchievement, due carte risorsa e una oro in hand
        for (int i = 0; i < testGame.getPlayersNumber(); i++) {
            assertEquals(players.get(i).getHand().size(), 3);
            assertEquals(players.get(i).getSecretAchievement().size(), 2);
            assertTrue(players.get(i).getSecretAchievement().get(0) instanceof AchievementCard);
            assertTrue(players.get(i).getSecretAchievement().get(1) instanceof AchievementCard);
            assertTrue(players.get(i).getHand().get(0) instanceof ResourceCard);
            assertTrue(players.get(i).getHand().get(1) instanceof ResourceCard);
            assertTrue(players.get(i).getHand().get(2) instanceof GoldCard);
            //in questo for controllo che non ci siano giocatori che hanno ricevuto la stessa identica carta
            for (int j = 0; j < testGame.getPlayersNumber() && j != i; j++) {
                assertFalse(players.get(i).getSecretAchievement().get(0).equals(players.get(j).getSecretAchievement().get(0)));
                assertFalse(players.get(i).getSecretAchievement().get(0).equals(players.get(j).getSecretAchievement().get(1)));
                assertFalse(players.get(i).getSecretAchievement().get(1).equals(players.get(j).getSecretAchievement().get(0)));
                assertFalse(players.get(i).getSecretAchievement().get(1).equals(players.get(j).getSecretAchievement().get(1)));
                assertFalse(players.get(i).getHand().get(0).equals(players.get(j).getHand().get(0)));
                assertFalse(players.get(i).getHand().get(0).equals(players.get(j).getHand().get(1)));
                assertFalse(players.get(i).getHand().get(0).equals(players.get(j).getHand().get(2)));
                assertFalse(players.get(i).getHand().get(1).equals(players.get(j).getHand().get(0)));
                assertFalse(players.get(i).getHand().get(1).equals(players.get(j).getHand().get(1)));
                assertFalse(players.get(i).getHand().get(1).equals(players.get(j).getHand().get(2)));
                assertFalse(players.get(i).getHand().get(2).equals(players.get(j).getHand().get(0)));
                assertFalse(players.get(i).getHand().get(2).equals(players.get(j).getHand().get(1)));
                assertFalse(players.get(i).getHand().get(2).equals(players.get(j).getHand().get(2)));
            }
        }
        testGame.end();
        testGame.nextState();
    }


    @Test
    void diagonalTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);
        ArrayList<Card> hand = new ArrayList<Card>(getOrderedResourceDeck());
        ArrayList<AchievementCard> hand2 = new ArrayList<AchievementCard>(getOrderedAchievementDeck());
        fake1.setHand(hand);
        Card tempCard;
        AchievementCard tempAchievement,tempAchievement2,tempAchievement3;
        tempCard = hand.get(0);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 39, 41);
        tempCard = hand.get(1);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 38, 42);
        tempCard = hand.get(3);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 37, 43);
        tempAchievement = hand2.get(0);
        tempAchievement.setPlayer(fake1);
        fake1.addPoints(tempAchievement.calculatePoints());
        assertEquals(fake1.getPoints(), 2);
        tempCard = hand.get(20);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 36, 44);
        tempCard = hand.get(21);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 35, 45);
        tempCard = hand.get(22);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 34, 46);
        tempAchievement2 = hand2.get(2);
        tempAchievement2.setPlayer(fake1);
        fake1.addPoints(tempAchievement2.calculatePoints());
        assertEquals(fake1.getPoints(), 4);
        tempCard = hand.get(11);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 35, 47);
        tempCard = hand.get(12);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 36, 48);
        tempCard = hand.get(13);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 37, 49);
        tempAchievement3 = hand2.get(1);
        tempAchievement3.setPlayer(fake1);
        fake1.addPoints(tempAchievement3.calculatePoints());
        assertEquals(fake1.getPoints(), 6);
        Print.playgroundPrinter(fake1.getArea());
        testGame.end();
        testGame.nextState();
    }

    @Test
    void generalTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);
        ArrayList<Card> hand = new ArrayList<Card>(getOrderedResourceDeck());
        ArrayList<AchievementCard> hand2 = new ArrayList<AchievementCard>(getOrderedAchievementDeck());
        Card tempCard;
        AchievementCard tempAchievement;
        tempCard = hand.get(0);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 39, 41);
        tempCard = hand.get(1);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 38, 42);
        tempCard = hand.get(3);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 37, 43);
        tempCard = hand.get(4);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 36, 44);
        tempCard = hand.get(5);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 37, 45);
        tempCard = hand.get(6);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 38, 46);
        tempCard = hand.get(7);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 39, 47);
        tempCard = hand.get(20);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 40, 46);
        tempCard = hand.get(21);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 41, 45);
        tempCard = hand.get(22);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 42, 46);
        tempCard = hand.get(31);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 43, 47);
        tempCard = hand.get(32);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 44, 48);
        tempCard = hand.get(33);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 45, 47);
        tempCard = hand.get(34);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 46, 48);
        tempCard = hand.get(35);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 47, 49);
        tempCard = hand.get(36);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 46, 50);
        tempCard = hand.get(38);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 45, 49);
        tempCard = hand.get(39);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 44, 50);
        tempCard = hand.get(23);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard, 43, 51);
        int n=0;
        for(int i = 0; i < 16; i++){
            tempAchievement = hand2.get(i);
            tempAchievement.setPlayer(fake1);
            n += tempAchievement.calculatePoints();
        }
        Print.playgroundPrinter(fake1.getArea());

        System.out.println(fake1.getArea().countResources(Resource.MUSHROOM));

        assertEquals(n , 35);
        testGame.end();
        testGame.nextState();
    }

    @Test
    void lShapeTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);
        ArrayList<Card> hand = new ArrayList<Card>(getOrderedResourceDeck());
        ArrayList<AchievementCard> hand2 = new ArrayList<AchievementCard>(getOrderedAchievementDeck());
        fake1.setHand(hand);
        Card tempCard;
        AchievementCard tempAchievement,tempAchievement2,tempAchievement3;
        tempCard = hand.get(1);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,39,41);
        tempCard = hand.get(2);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,40,42);
        tempCard = hand.get(3);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,41,41);
        tempCard = hand.get(11);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,42,42);
        tempCard = hand.get(23);
        tempCard.setFront(false);
        fake1.getArea().setSpace(tempCard,43,43);
        tempCard = hand.get(22);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,44,44);
        tempCard = hand.get(21);
        tempCard.setFront(false);
        fake1.getArea().setSpace(tempCard,45,43);
        tempCard = hand.get(4);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,42,44);
        tempCard = hand.get(10);
        tempCard.setFront(false);
        fake1.getArea().setSpace(tempCard,44,42);
        tempCard = hand.get(11);
        tempCard.setFront(false);
        fake1.getArea().setSpace(tempCard,45,41);
        tempCard = hand.get(12);
        tempCard.setFront(false);
        fake1.getArea().setSpace(tempCard,46,42);
        tempCard = hand.get(30);
        tempCard.setFront(false);
        fake1.getArea().setSpace(tempCard,47,41);
        tempAchievement = hand2.get(4);
        tempAchievement2 = hand2.get(6);
        tempAchievement3 = hand2.get(5);
        tempAchievement.setPlayer(fake1);
        fake1.addPoints(tempAchievement.calculatePoints());
        assertEquals(fake1.getPoints(),3);
        tempAchievement2.setPlayer(fake1);
        fake1.addPoints(tempAchievement2.calculatePoints());
        assertEquals(fake1.getPoints(),6);
        tempAchievement3.setPlayer(fake1);
        fake1.addPoints(tempAchievement3.calculatePoints());
        assertEquals(fake1.getPoints(),9);
        Print.playgroundPrinter(fake1.getArea());
        testGame.end();
        testGame.nextState();
    }

    @Test
    void itemTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);
        ArrayList<Card> hand = new ArrayList<Card>(getOrderedResourceDeck());
        ArrayList<AchievementCard> hand2 = new ArrayList<AchievementCard>(getOrderedAchievementDeck());
        fake1.setHand(hand);
        Card tempCard;
        AchievementCard tempAchievement;
        tempCard = hand.get(4);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,41,41);
        tempCard = hand.get(14);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,42,42);
        tempCard = hand.get(26);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,43,43);
        tempCard = hand.get(34);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,44,44);
        tempAchievement = hand2.get(15);
        tempAchievement.setPlayer(fake1);
        fake1.addPoints(tempAchievement.calculatePoints());
        assertEquals(fake1.getPoints(),4);
        testGame.end();
        testGame.nextState();
    }
    @Test
    void resourceTest() throws RemoteException {
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);
        ArrayList<Card> hand = new ArrayList<Card>(getOrderedResourceDeck());
        ArrayList<AchievementCard> hand2 = new ArrayList<AchievementCard>(getOrderedAchievementDeck());
        fake1.setHand(hand);
        Card tempCard;
        AchievementCard tempAchievement;
        tempCard = hand.get(0);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,39,41);
        tempCard = hand.get(1);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,38,42);
        tempCard = hand.get(2);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,39,43);
        tempAchievement = hand2.get(8);
        tempAchievement.setPlayer(fake1);
        fake1.addPoints(tempAchievement.calculatePoints());
        assertEquals(fake1.getPoints(),4);
        testGame.end();
        testGame.nextState();
    }
    @Test
    void mixedTest() throws RemoteException {

        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);

        testGame.addPlayers(players, clients);
        ArrayList<Card> hand = new ArrayList<Card>(getOrderedResourceDeck());
        ArrayList<AchievementCard> hand2 = new ArrayList<AchievementCard>(getOrderedAchievementDeck());
        fake1.setHand(hand);
        Card tempCard;
        AchievementCard tempAchievement;
        tempCard = hand.get(4);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,41,41);
        tempCard = hand.get(5);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,42,42);
        tempCard = hand.get(6);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,43,43);
        tempCard = hand.get(14);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,42,44);
        tempCard = hand.get(15);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,41,45);
        tempCard = hand.get(16);
        tempCard.setFront(true);
        fake1.getArea().setSpace(tempCard,40,46);
        tempAchievement = hand2.get(12);
        tempAchievement.setPlayer(fake1);
        fake1.addPoints(tempAchievement.calculatePoints());
        assertEquals(fake1.getPoints(),6);
        testGame.end();
        testGame.nextState();
    }

    @Test
    void drawTest() throws RemoteException {

        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualServer server = null;
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);

        testGame.addPlayers(players, clients);
        Card card;
        assertNull(testGame.draw("Marco", -1));
        assertNotNull(testGame.draw("Marco", 6));
        card = testGame.getCommonGold().get(1);
        assertEquals(card, testGame.draw("Marco", 2));
        assertInstanceOf(GoldCard.class, card);
        assertNull(testGame.draw("Marco", 0));
        assertInstanceOf(GoldCard.class, testGame.draw("Marco", 1));
        card = testGame.getGoldDeck().get(0);
        assertEquals(card, testGame.draw("Marco", 5));
        assertInstanceOf(GoldCard.class, card);
        assertInstanceOf(ResourceCard.class, testGame.draw("Marco", 3));
        assertInstanceOf(ResourceCard.class, testGame.draw("Marco", 4));
        testGame.end();
        testGame.nextState();
    }

    @Test
    void coverageTest() throws RemoteException{
        Game testGame = Game.getInstance();
        ArrayList<Player> players = new ArrayList<Player>();
        Player fake1 = new Player("Marco");
        players.add(fake1);
        VirtualView cli = new ClientRmi(null);
        ArrayList<VirtualView> clients = new ArrayList<>();
        clients.add(cli);
        testGame.addPlayers(players, clients);
        ResourceCard tempRes;
        GoldCard tempGold;
        ArrayList<ResourceCard> deck1 = new ArrayList<ResourceCard>(getOrderedResourceDeck());
        ArrayList<GoldCard> deck2 = new ArrayList<GoldCard>(getOrderedGoldDeck());
        ArrayList<AchievementCard> deck3 = new ArrayList<AchievementCard>(getOrderedAchievementDeck());
        //test of override method equals
        //between resource cards:
        assertFalse(deck1.get(0).equals(deck1.get(1)));
        assertTrue(deck1.get(0).equals(deck1.get(0)));
        //between gold cards
        assertFalse(deck2.get(0).equals(deck2.get(1)));
        assertTrue(deck2.get(0).equals(deck2.get(0)));
        //between achievement cards
        assertFalse(deck3.get(0).equals(deck3.get(1)));
        assertTrue(deck3.get(0).equals(deck3.get(0)));
        // test of method countResource inside gold cards used to determine number of each resource required to place the gold card
        assertEquals(deck2.get(0).countResource(Resource.MUSHROOM),2);
        assertEquals(deck2.get(0).countResource(Resource.WOLF),1);
        assertEquals(deck2.get(0).countResource(Resource.LEAF),0);
        assertEquals(deck2.get(0).countResource(Resource.BUTTERFLY),0);
        assertEquals(deck2.get(1).countResource(Resource.MUSHROOM),2);
        assertEquals(deck2.get(1).countResource(Resource.LEAF),1);
        assertEquals(deck2.get(1).countResource(Resource.BUTTERFLY),0);
        assertEquals(deck2.get(1).countResource(Resource.WOLF),0);
        // test of checkGold method used to determine if there are enough resources to place a gold card
        tempRes = deck1.get(20);
        tempGold = deck2.get(0);
        tempRes.setFront(true);
        tempGold.setFront(true);
        // placement of the first card, not giving enough resources to place the card
        fake1.getArea().setSpace(tempRes,41,41);
        assertFalse(fake1.checkGold(tempGold));
        tempRes = deck1.get(0);
        tempRes.setFront(true);
        // placement of the second card, giving enough resources to place the card
        fake1.getArea().setSpace(tempRes,42,42);
        assertTrue(fake1.checkGold(tempGold));
        testGame.end();
        testGame.nextState();
    }


}