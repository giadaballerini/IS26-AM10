package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.rmi.ClientRmi;
import it.polimi.ingsw.network.client.socket.ClientSocket;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class MainClientSocket {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1234;
    public static void main(String[] args) {
        System.setProperty("jansi.passthrough", "true");
        System.setProperty("org.jline.terminal.jansi", "true");
        ClientSocket clientSocket = new ClientSocket(SERVER_IP, SERVER_PORT);

        if(!clientSocket.isConnected()){
            System.err.println("Impossibile connettersi al server.");
            System.exit(1);
        }
        Thread reader = new Thread(clientSocket);
        reader.setDaemon(true);
        reader.start();

        boolean loggedIn = false;
        Scanner scanner = new Scanner(System.in);
        while (!loggedIn) {
            System.out.print("Inserire il nickname: ");
            String nickname = scanner.nextLine().trim();
            if(!nickname.isEmpty())
                loggedIn = clientSocket.login(nickname);
        }
        clientSocket.start();
    }
}