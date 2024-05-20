package it.polimi.ingsw;


import it.polimi.ingsw.clientProva.Client;
import it.polimi.ingsw.networking.action.*;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.networking.action.toclient.*;
import it.polimi.ingsw.networking.action.toserver.*;
import it.polimi.ingsw.networking.rmi.RmiClient;
import it.polimi.ingsw.networking.rmi.VirtualServer;
import it.polimi.ingsw.util.Print;
import it.polimi.ingsw.networking.rmi.VirtualView;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientApp {

    public static void main(String[] args) throws NotBoundException, IOException {
        String AsciiArt =
                "  ______                   __                            __    __              __                                   __  __            \n"+
                " /      \\                 /  |                          /  \\  /  |            /  |                                 /  |/  |           \n"+
                "/$$$$$$  |  ______    ____$$ |  ______   __    __       $$  \\ $$ |  ______   _$$ |_    __    __   ______   ______  $$ |$$/   _______  \n"+
                "$$ |  $$/  /      \\  /    $$ | /      \\ /  \\  /  |      $$$  \\$$ | /      \\ / $$   |  /  |  /  | /      \\ /      \\ $$ |/  | /       | \n"+
                "$$ |      /$$$$$$  |/$$$$$$$ |/$$$$$$  |$$  \\/$$/       $$$$  $$ | $$$$$$  |$$$$$$/   $$ |  $$ |/$$$$$$  |$$$$$$  |$$ |$$ |/$$$$$$$/  \n"+
                "$$ |   __ $$ |  $$ |$$ |  $$ |$$    $$ | $$  $$<        $$ $$ $$ | /    $$ |  $$ | __ $$ |  $$ |$$ |  $$/ /    $$ |$$ |$$ |$$      \\  \n"+
                "$$ \\__/  |$$ \\__$$ |$$ \\__$$ |$$$$$$$$/  /$$$$  \\       $$ |$$$$ |/$$$$$$$ |  $$ |/  |$$ \\__$$ |$$ |     /$$$$$$$ |$$ |$$ | $$$$$$  | \n"+
                "$$    $$/ $$    $$/ $$    $$ |$$       |/$$/ $$  |      $$ | $$$ |$$    $$ |  $$  $$/ $$    $$/ $$ |     $$    $$ |$$ |$$ |/     $$/  \n"+
                " $$$$$$/   $$$$$$/   $$$$$$$/  $$$$$$$/ $$/   $$/       $$/   $$/  $$$$$$$/    $$$$/   $$$$$$/  $$/       $$$$$$$/ $$/ $$/ $$$$$$$/   \n";
        boolean checkChoice= false;
        Scanner scan = new Scanner(System.in);
        String line;
        StringTokenizer st;
        int connectionChoice;
        int portChoice = 7171;
        String ip = "127.0.0.1";
        int guiChoice;
        do {
            System.out.println("> Select connection method:");
            System.out.println("   [1] TUI");
            System.out.println("   [2] GUI");
            scan = new Scanner(System.in);
            line = scan.nextLine();
            st = new StringTokenizer(line);
            guiChoice = Integer.parseInt(st.nextToken());
            if (guiChoice == 1 || guiChoice == 2) {
                checkChoice = true;
            }
        } while(!checkChoice);

        checkChoice= false;

        do{
            System.out.println("> Select connection method:");
            System.out.println("   [1] RMI Connection");
            System.out.println("   [2] Socket Connection");
            scan = new Scanner(System.in);
            line = scan.nextLine();
            st = new StringTokenizer(line);
            connectionChoice = Integer.parseInt(st.nextToken());
            if(connectionChoice == 1 || connectionChoice == 2) {
                checkChoice = true;
            }
        }while(!checkChoice);

        System.out.println("> Select port (0 for default): ");
        scan = new Scanner(System.in);
        line = scan.nextLine();
        st = new StringTokenizer(line);
        portChoice = Integer.parseInt(st.nextToken());
        if(portChoice == 0){
            if(connectionChoice == 1) //RMI
                portChoice = 6969;
            else
                portChoice =7171;
        }
        System.out.println("> Select ip (0 for localhost): ");
        scan = new Scanner(System.in);
        line = scan.nextLine();
        st = new StringTokenizer(line);
        String ipChoice = st.nextToken();
        if (ipChoice.equals("0")){
            ipChoice = "127.0.0.1";
        }


        if(guiChoice == 1){
            //TUI
            Client c = new Client(connectionChoice, portChoice, ipChoice, false);
        }
        else {
            //GUI
            Client c = new Client(connectionChoice, portChoice, ipChoice, true);
        }


    }

}

