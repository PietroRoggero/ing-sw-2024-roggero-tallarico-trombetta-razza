package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.networking.VirtualView;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * The GameController class is responsible for managing the game logic and interactions between players and the game model.
 * It maintains the list of players, clients, and handles the game chat functionality.
 */
public class GameController {
    /**
     * The number of players in the game.
     */
    private int playersNumber;

    /**
     * The game model instance.
     */
    private final Game model;

    /**
     * The list of players in the game.
     */
    private final ArrayList<Player> players;

    /**
     * The list of clients connected to the game.
     */
    private final ArrayList<VirtualView> clients;

    /**
     * The chat instance for the game.
     */
    private final Chat chat;

    /**
     * The countdown before the game starts, decremented every time a player sets his starter card and achievements
     */
    private int startCountdown;

    /**
     * Constructs a new GameController and initializes the game model, player list, client list, and chat instance.
     */
    public GameController() {
        model = Game.getInstance();
        this.players = new ArrayList<>();
        this.clients = new ArrayList<>();
        chat = Chat.getInstance();
    }


    /**
     * Handles the reconnection of a player to the game.
     *
     * @param nickname The nickname of the player who is reconnecting.
     * @param oldVirtualView The old VirtualView associated with the player.
     * @param newVirtualView The new VirtualView associated with the player.
     * @throws RemoteException If a remote communication error occurs during the reconnection process.
     */
    public void reconnection(String nickname, VirtualView oldVirtualView, VirtualView newVirtualView) throws RemoteException {
        model.reconnection(nickname, oldVirtualView, newVirtualView);
    }

    /**
     * Sets the starter card side for a player.
     *
     * @param playerName The name of the player setting their starter card.
     * @param front True if the front side of the card should be used, false if the back side should be used.
     * @throws RemoteException If a remote communication error occurs while setting the starter card.
     */
    public void setStarterCardSide(String playerName, boolean front) throws RemoteException {
        model.setStarterCard(playerName, front);
    }


    /**
     * Adds a player to the game.
     *
     * @param p The player to be added.
     * @param v The VirtualView associated with the player.
     * @throws RemoteException If a remote communication error occurs while adding the player.
     */
    public void addPlayer(Player p, VirtualView v) throws RemoteException {
        synchronized (this.players) {
            if (players.size() <= playersNumber && model.getGameState().equals(GameState.LOBBY)) {
                players.add(p);
                clients.add(v);
                if (players.size() == playersNumber) {
                    model.addPlayers(players, clients);
                }
            }
        }
    }

    /**
     * Places a card on the game playground for a player.
     *
     * @param playerName The name of the player placing the card.
     * @param cardIndex The index of the card to be placed.
     * @param side True if the front side of the card should be used, false if the back side should be used.
     * @param row The row on the board where the card should be placed.
     * @param column The column on the board where the card should be placed.
     * @return True if the card was successfully placed, false otherwise.
     * @throws RemoteException If a remote communication error occurs during the card placement.
     */
    public boolean placeCard(String playerName, int cardIndex, boolean side, int row, int column) throws RemoteException {
        synchronized (this.model) {
            if(model.getGameState().equals(GameState.GAME) || model.getGameState().equals(GameState.LASTROUND)) {
                if(playerName.equalsIgnoreCase(model.getPlayers().get(model.getCurrPlayer()).getName()))
                    return model.getPlayers().get(model.getCurrPlayer()).place(cardIndex, side, row, column);
                else
                    return false;
            } else
                return false;
        }
    }

    /**
     * Draws a card for a player.
     *
     * @param playerName The name of the player drawing the card.
     * @param index The index of the card to be drawn.
     * @return True if the card was successfully drawn, false otherwise.
     * @throws RemoteException If a remote communication error occurs during the card drawing.
     */
    public boolean drawCard(String playerName, int index) throws RemoteException {
        if (model.getGameState().equals(GameState.GAME) || model.getGameState().equals(GameState.LASTROUND)) {
            if (getPlayers().get(model.getCurrPlayer()).getHand().size() == 2) {
                Card card = model.draw(playerName, index);
                if (card != null) {
                    model.nextPlayer();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles the disconnection of a player from the game.
     *
     * @param playerName The name of the player who is disconnecting.
     * @throws RemoteException If a remote communication error occurs during the disconnection process.
     */
    public void disconnection(String playerName) throws RemoteException {
        model.disconnection(playerName);
    }

    /**
     * Sets the secret achievement card for a player.
     *
     * @param playerName The name of the player setting their secret achievement.
     * @param achievement The secret achievement card to be set.
     * @throws RemoteException If a remote communication error occurs while setting the secret achievement.
     */
    public void setSecretAchievement(String playerName, AchievementCard achievement) throws RemoteException {
        ArrayList<AchievementCard> secretAch = new ArrayList<>();
        secretAch.add(achievement);
        for(Player p : model.getPlayers()) {
            if(p.getName().equalsIgnoreCase(playerName)) {
                p.setSecretAchievement(secretAch);
                synchronized (clients) {
                    for (VirtualView c : clients) {
                        if (playerName.equalsIgnoreCase(c.getNickname())) {
                            c.setStarter(true);
                        }
                    }
                }
                countdown();
                return;
            }
        }
    }

    /**
     * Calculates the end points for the game and advances the game state.
     *
     * @return True if the end points were successfully calculated and the game state advanced, false otherwise.
     * @throws RemoteException If a remote communication error occurs during the calculation.
     */
    public boolean calculateEndPoints() throws RemoteException {
        synchronized (this.model) {
            if(model.getGameState().equals(GameState.FINALSCORE)) {
                model.calculateEndPoints();
                model.nextState();
                return true;
            }
        }
        return false;
    }


    /**
     * Decreases the countdown each time a player successfully sets their game state (chooses starter card and achievement).
     * When the countdown reaches zero, the game state is set to GAME.
     *
     * @throws RemoteException If a remote communication error occurs during the countdown.
     */
    private void countdown() throws RemoteException {
        startCountdown--;
        if(startCountdown == 0) {
            model.setGameState(GameState.GAME);
        }
    }

    /**
     * Gets the list of players in the game.
     *
     * @return The list of players.
     */
    public ArrayList<Player> getPlayers() {
        synchronized(this.players){
            return model.getPlayers();
        }
    }

    /**
     * Sends a chat message and returns the last message sent in the chat.
     *
     * @param msg The message to be sent.
     * @return The last message sent in the chat.
     */
    public Message sendChatMessage(Message msg) {
        synchronized (this.chat) {
            chat.sendMessage(msg);
            return chat.getLastMessage();
        }
    }

    /**
     * Gets the entire chat history.
     *
     * @return The list of all messages in the chat.
     */
    public ArrayList<Message> getWholeChat() {
        synchronized (this.chat) {
            return chat.getWholeChat();
        }
    }

    /**
     * Sets the number of players for the game and initializes the start countdown.
     *
     * @param playersNumber The number of players to be set.
     */
    public void setPlayersNumber(int playersNumber) {
        this.playersNumber = playersNumber;
        startCountdown = playersNumber;
    }


}
