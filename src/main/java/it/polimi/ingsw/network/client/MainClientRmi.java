package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.rmi.ClientRmi;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class MainClientRmi {
    static void main(String[] args) throws NotBoundException, RemoteException {
        System.setProperty("jansi.passthrough", "true");
        System.setProperty("org.jline.terminal.jansi", "true");
        ClientRmi client = null;
        try {
            client = new ClientRmi();
        } catch (RemoteException | NotBoundException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        boolean loggedIn = false;
        Scanner scanner = new Scanner(System.in);
        while (!loggedIn) {
            System.out.print("Inserire il nickname: ");
            String nickname = scanner.nextLine().trim();
            if(!nickname.isEmpty())
                loggedIn = client.login(nickname);
        }
        client.start();
    }
}
