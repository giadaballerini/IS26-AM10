package it.polimi.ingsw.client;

import it.polimi.ingsw.client.ui.GUI.LauncherApp;
import it.polimi.ingsw.client.ui.TUI.ViewTUI;
import it.polimi.ingsw.network.client.rmi.ClientRmi;
import it.polimi.ingsw.network.client.socket.ClientSocket;
import javafx.application.Application;

import java.util.Scanner;

/**
 * Entry point for the client application.
 * Prompts the player to choose between the TUI and the GUI, and — for the TUI —
 * to choose the network protocol (Socket or RMI) and the server IP address.
 * Then creates the appropriate {@link Client} and {@link ViewTUI} instances,
 * wires them together, and starts the client.
 */
public class ClientMain {

    /**
     * Launches the client application.
     * <p>
     * If the player chooses the GUI, the JavaFX {@link LauncherApp} is launched
     * and handles network configuration internally. If the player chooses the TUI,
     * the network type and server IP are collected from standard input, and a
     * {@link ClientSocket} or {@link ClientRmi} is created accordingly.
     *
     * @param args command-line arguments passed to the JavaFX launcher when the GUI is selected
     */
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
        }
    }
}