package it.polimi.ingsw.networking.rmi;

import it.polimi.ingsw.action.Action;
import it.polimi.ingsw.action.ActionType;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.Card;
import it.polimi.ingsw.model.Message;
import it.polimi.ingsw.model.Player;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//NB: qua NON è sufficiente gestire le eccezioni con throws ma si dovrà usare try-catch correttamente
public class RmiServer implements VirtualServer {

    private static int PORT = 1234;
    private final GameController controller;
    private final ArrayList<VirtualView> clients = new ArrayList<>();
    private final BlockingQueue<Object> updates = new LinkedBlockingQueue<>();
    private final BlockingQueue<Action> serverActions = new LinkedBlockingQueue<>();
    private final BlockingQueue<Action> clientActions = new LinkedBlockingQueue<>();

    public RmiServer(GameController controller){
        this.controller = controller;
    }

    public static void main(String[] args) throws RemoteException, InterruptedException {

        final String serverName = "GameServer";
        /// qui non deve andare nulla di println perché non si andrà mai a leggere dal server
        System.out.print("> Enter desired players number: ");
        Scanner scan = new Scanner(System.in);
        int playersNumber = scan.nextInt();
        VirtualServer server = new RmiServer(new GameController(playersNumber));
        VirtualServer stub = (VirtualServer) UnicastRemoteObject.exportObject(server,0 );
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind(serverName, stub);
        System.out.println("> Server bound.");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Codice da eseguire nel thread
                System.out.println("Questo è un thread separato.");
                try {
                    ((RmiServer)server).clientsUpdateThread();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();

        try {
            System.out.println("RAGGIUNGE SERVER UPDATETHREAD");
            ((RmiServer)server).serverUpdateThread();
        }
        catch (InterruptedException e){
            System.err.println("> ERROR: Updates queue for server interrupted:\n" + e.getMessage());
        }

    }

    public void broadcastUpdateThread() throws InterruptedException, RemoteException {
        while(true) {
            Object o = updates.take();
            synchronized (this.clients){
                for (VirtualView c : clients) {
                    c.showUpdate(o);
                }
            }
        }
    }

    public void clientsUpdateThread() throws InterruptedException, RemoteException  {
        while (true) {
            Action a = clientActions.take();
            if (a.getRecipient().isEmpty()) { //to all clients
                for(VirtualView c : clients) {
                    c.showAction(a);
                }
            } else { //to a single client
                for(VirtualView c : clients) {
                    if (c.getNickname().equalsIgnoreCase(a.getRecipient())) {
                        c.showAction(a);
                    }
                }
            }
        }

    }


    public void serverUpdateThread() throws InterruptedException, RemoteException {
        while(true) {
            Action a = serverActions.take();
            System.out.println("> Handling action, action type " + a.getType().toString());
            Action newAct = null;
            switch (a.getType()) {
                case WHOLECHAT:
                    break;
                case ASKINGCHAT:
                    newAct = new Action(ActionType.WHOLECHAT, this.controller.getWholeChat(), null, a.getAuthor());
                    clientActions.put(newAct);
                    break;
                case CHATMESSAGE:
                    this.controller.sendChatMessage((Message)a.getObject());
                    clientActions.put(a);
                    break;
                case CHOSENACHIEVEMENT:
                    break;
                case CHOOSEABLEACHIEVEMENTS:
                    break;
                case HAND:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean connect(VirtualView client) throws RemoteException {
        synchronized (this.clients) {
            System.err.println("> Join request received");
            String nick = client.getNickname();
            //System.out.println("> Adding player " + nick + ".");
            if(!clients.isEmpty())
                for(VirtualView v : this.clients) {
                    if(v.getNickname().equalsIgnoreCase(nick)) {
                        System.out.println("> Denied connection to a new client, user \"" + nick + "\" already existing.");
                        return false;
                    }
                }
            if(this.controller.getCurrPlayersNumber() == this.controller.getMaxPlayersNumber()) {
                System.out.println("> Denied connection to a new client, max number of players already reached.");
                return false;
            } else {
                this.clients.add(client);
                System.out.println("> Allowed connection to a new client named \"" + nick + "\".");
                addPlayer(new Player(nick, false));
                return true;
            }
        }
    }

    @Override
    public void sendAction(Action action) throws RemoteException {
        try {
            System.out.println("> Received action, type " + action.getType().toString());
            serverActions.put(action);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPlayer(Player p) throws RemoteException {
        synchronized (this.clients){
            this.controller.addPlayer(p);
            String textUpdate = "> Player " + p.getName() + " joined the game. " + this.controller.getCurrPlayersNumber() + "/" + this.controller.getMaxPlayersNumber();
            System.out.println(textUpdate);
            try {
                clientActions.put(new Action(ActionType.JOININGPLAYER, p.getName(), null, null));
                //updates.put(textUpdate);
            } catch(InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public void sendChatMessage(String msg, String author) throws RemoteException {
        Message mex = new Message(msg, author);
        mex = this.controller.sendChatMessage(mex);
        try {
            updates.put(mex);
        } catch(InterruptedException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void sendChatWhisper(String msg, String author, String recipient) throws RemoteException {
        Message mex = new Message(msg, author, recipient);
        mex = this.controller.sendChatMessage(mex);
        try {
            updates.put(mex);
        } catch(InterruptedException e) {
            throw new RuntimeException();
        }
    }


    @Override
    public void getWholeChat() throws RemoteException {
        try {
            updates.put(this.controller.getWholeChat());
        } catch (InterruptedException e){
            throw new RuntimeException();
        }
    }

    @Override
    public ArrayList<Player> getPlayers() throws RemoteException{
        return this.controller.getPlayers();
    }


    //da completare v
    @Override
    public void placeCard(Card card, int row, int column) throws RemoteException {
        this.controller.placeCard(card,row,column);
        // currentState = this.controller.getState();
        synchronized (this.clients){
            for(VirtualView c : this.clients){
                //c.showUpdate(currentState);
            }
        }
    }


    @Override
    public void drawCard(int index) throws RemoteException {
        this.controller.drawCard(index);
        // currentState = this.controller.getState();
        synchronized (this.clients) {
            for (VirtualView c : this.clients) {
                // c.showUpdate(currentState);
            }
        }
    }

    @Override
    public void selectAchievementCard (int position) throws RemoteException {
        this.controller.selectAchievementCard(position);
        // currentState = this.controller.getState();
        synchronized (this.clients) {
            for (VirtualView c : this.clients) {
                //c.showUpdate(currentState);
            }
        }

    }

}
