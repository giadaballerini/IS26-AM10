package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.server.rmi.ServerRmi;
import it.polimi.ingsw.network.server.socket.ServerSocket;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        System.setProperty("sun.rmi.transport.tcp.readTimeout", "5000");
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
        System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "3000");

        String serverIp = detectLocalIp();
        System.setProperty("java.rmi.server.hostname", serverIp);
        System.out.println("Binding RMI su: " + serverIp);
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


    private static String detectLocalIp() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 80);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            System.err.println("Rilevazione IP fallita, uso localhost: " + e.getMessage());
            return "localhost";
        }
    }
}

