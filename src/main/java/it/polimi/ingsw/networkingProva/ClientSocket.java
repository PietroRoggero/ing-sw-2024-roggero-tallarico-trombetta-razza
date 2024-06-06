
package it.polimi.ingsw.networkingProva;

import it.polimi.ingsw.networking.action.ActionType;
import it.polimi.ingsw.networking.action.toclient.AskingStartAction;
import it.polimi.ingsw.networking.action.toclient.JoiningPlayerAction;
import it.polimi.ingsw.networking.action.toserver.ReconnectedPlayerAction;
import it.polimi.ingsw.networking.action.toserver.SetNicknameAction;
import it.polimi.ingsw.networking.rmi.VirtualView;
import it.polimi.ingsw.networking.action.Action;


import java.io.IOException;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ClientSocket implements VirtualView, Runnable {
    private Socket serSocket = null;
    private final ObjectOutputStream outputStream;
    private BlockingQueue<Action> serverActions;
    private BlockingQueue<Action> clientActions;
    private final ObjectInputStream inputStream;
    private ArrayList<VirtualView> clients;
    private String nickname = null;
    private boolean connected = true;
    private boolean gui;
    private Map<VirtualView, Boolean> pingMap;
    private Map<VirtualView, Boolean> onlineMap;
    private Map<VirtualView, String> nicknamesMap;
    private boolean gamestarted;

    public ClientSocket(Socket serSocket, BlockingQueue<Action> serverActions, BlockingQueue<Action> clientActions, ArrayList<VirtualView> clients, Map<VirtualView, Boolean> pingMap, Map<VirtualView, Boolean> onlineMap, Map<VirtualView, String> nicknamesMap, boolean gameStarted) throws IOException {
        this.serSocket = serSocket;
        this.outputStream = new ObjectOutputStream(serSocket.getOutputStream());
        this.inputStream = new ObjectInputStream(serSocket.getInputStream());
        this.serverActions = serverActions;
        this.clientActions = clientActions;
        this.clients = clients;
        this.pingMap = pingMap;
        this.onlineMap = onlineMap;
        this.nicknamesMap = nicknamesMap;
        this.gamestarted = gameStarted;
    }

    @Override
    public void reportError(String details) throws RemoteException {

    }

    @Override
    public void showAction(Action act) throws IOException {
        //qui il server sta MANDANDO l'azione al client
        outputStream.writeObject(act);
        outputStream.flush();
        outputStream.reset();
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean getOnline() {
        return connected;
    }

    @Override
    public boolean getGui() {
        return gui;
    }


    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    @Override
    public void run() {
        while(true) {
            Action action = null;
            try {
                action = (Action) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                if(nickname != null ) {
                    if(action.getType().equals(ActionType.PONG)){
                        pingMap.replace(this, Boolean.TRUE);
                    }
                    serverActions.put(action);
                }
                else if(action.getType().equals(ActionType.SETNICKNAME)) {
                    if(!gamestarted) {
                        boolean checkAlreadyExists = false;
                        String tempNickname = ((SetNicknameAction) action).getNickname();
                        for (VirtualView c : clients) {
                            if (tempNickname.equalsIgnoreCase(c.getNickname()))
                                checkAlreadyExists = true;
                        }
                        if (checkAlreadyExists)// se ritorna il messaggio con null al client il nick non è valido, altrimenti se torna lo stesso nick al client è stato accettato
                            tempNickname = null;
                        Action response = new SetNicknameAction(tempNickname, false);
                        if (tempNickname != null) {
                            connected = true;
                            nickname = tempNickname;
                            nicknamesMap.put(this, nickname);
                            onlineMap.put(this, Boolean.TRUE);
                            gui = ((SetNicknameAction) action).getGui();
                            System.out.println(">Allowed Socket connection to a new client named \"" + nickname + "\".");
                        }
                        outputStream.writeObject(response);
                        outputStream.flush();
                        outputStream.reset();

                        String destNickname = null;
                        for (int i = 0; i < clients.size() && destNickname == null; i++) {
                            if (clients.get(i).getOnline()) {
                                destNickname = clients.get(i).getNickname();
                            }
                        }
                        int count = 0;
                        for (VirtualView v : this.clients) {
                            if (v.getOnline() && v.getNickname() != null)
                                count++;
                        }
                        Action act = new AskingStartAction(destNickname, count);
                        clientActions.put(act);

                        act = new JoiningPlayerAction(nickname, count, 4);
                        try {
                            clientActions.put(act);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            for (VirtualView v : clients) {
                                System.out.println("Nome: " + v.getNickname());
                            }
                        }
                    } else {
                        {  //Reconnect
                            //Ricerca VirtualView Vecchia
                            nickname = ((SetNicknameAction) action).getNickname();
                            VirtualView oldVirtualView = null;
                            for(VirtualView c : clients){
                                if(nicknamesMap.get(c).equalsIgnoreCase(nickname)){
                                    oldVirtualView = c;
                                    break;
                                }
                            }
                            if(oldVirtualView!= null && !(onlineMap.get(oldVirtualView).booleanValue())) {
                                // il client si è già connesso in precedenza e deve recuperare i dati

                                // mando maction "reconnect" che manda nickname e la nuova virtualview
                                try {
                                    serverActions.put(new ReconnectedPlayerAction(nickname, oldVirtualView, this));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                //aggiornamento mappe
                                int index = clients.indexOf(oldVirtualView);
                                clients.remove(index);
                                clients.add(index, this);

                                onlineMap.remove(oldVirtualView);
                                onlineMap.put(this, Boolean.TRUE);

                                nicknamesMap.remove(oldVirtualView);
                                nicknamesMap.put(this, nickname);
                                outputStream.writeObject(new SetNicknameAction(nickname, false));
                                outputStream.flush();
                                outputStream.reset();
                            }
                            else{
                                outputStream.writeObject(new SetNicknameAction(null, false));
                                outputStream.flush();
                                outputStream.reset();
                                System.out.println("> User " + nickname + " already online or doesn't exist");
                            }
                        }
                    }
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
