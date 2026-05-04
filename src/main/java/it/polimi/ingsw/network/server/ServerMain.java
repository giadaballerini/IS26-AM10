package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.server.rmi.ServerRmi;
import it.polimi.ingsw.network.server.socket.ServerSocket;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    static void main(String[] args) {
        try{
            Registry registry = LocateRegistry.createRegistry(1099);
            MatchManager matchManager = new MatchManager();
            ServerRmi serverRmi = new ServerRmi(matchManager);
            registry.rebind("GameServer", serverRmi);
            System.out.println("> Server RMI pronto e registrato!");

            int socketPort = 1234;
            ServerSocket serverSocket = new ServerSocket(socketPort, matchManager);
            Thread socketThread = new Thread(() -> {
                System.out.println("> Server Socket pronto sulla porta " + socketPort);
                serverSocket.start();
            });
            socketThread.start();
            System.out.println("> Server pronto!");
        } catch (RemoteException e){
            System.out.println("Errore durante l'avvio del server:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
