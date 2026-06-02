package it.polimi.ingsw.client;

import it.polimi.ingsw.client.GUI.LauncherApp;
import it.polimi.ingsw.client.TUI.ViewTUI;

import it.polimi.ingsw.network.client.rmi.ClientRmi;
import it.polimi.ingsw.network.client.socket.ClientSocket;
import javafx.application.Application;

import java.util.Scanner;



public class ClientMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Vuoi giocare in:\n1) TUI\n2) GUI");
        int uiType = scanner.nextInt();

        if (uiType == 2) {
            Application.launch(LauncherApp.class, args);
            return;
        }

        if (uiType != 1) {
            System.out.println("Scelta non valida");
            return;
        }
        System.out.println("Scegli la rete:\n1) Socket\n2) RMI");
        int networkType = scanner.nextInt();

        scanner.nextLine();

        System.out.println("Inserisci l'IP del server (invio = localhost):");
        String ip = scanner.nextLine();
        if (ip.isEmpty() || ip.isBlank()) ip = "127.0.0.1";

        try {
            VirtualModel model = new VirtualModel();
            Client client = networkType == 1
                    ? new ClientSocket(ip, 1234, model)
                    : new ClientRmi(ip, model);

            ViewTUI tui = new ViewTUI(model, client);
            client.setUi(tui);
            tui.setClient(client);

            client.start();

        } catch (Exception e) {
            System.err.println("Errore connessione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
