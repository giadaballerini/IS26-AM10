package it.polimi.ingsw.client;

import it.polimi.ingsw.client.rmi.ClientRmi;
import it.polimi.ingsw.client.socket.ClientSocket;
import it.polimi.ingsw.client.ui.gui.LauncherApp;
import it.polimi.ingsw.client.ui.tui.ViewTUI;
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
     * @param args command-line arguments passed to the client
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int uiType = -1;
        while (uiType != 1 && uiType != 2) {
            System.out.println("Vuoi giocare in:\n1) TUI\n2) GUI");
            uiType = getUiType(scanner, uiType);
        }

        if (uiType == 2) {
            Application.launch(LauncherApp.class, args);
            return;
        }

        int networkType = -1;
        while (networkType != 1 && networkType != 2) {
            System.out.println("Scegli la rete:\n1) Socket\n2) RMI");
            networkType = getUiType(scanner, networkType);
        }

        boolean connected = false;
        while (!connected) {
            System.out.println("Inserisci l'IP del server (invio = localhost):");
            String ip = scanner.nextLine().trim();
            if (ip.isBlank()) {
                ip = "127.0.0.1";
            }

            try {
                VirtualModel model = new VirtualModel();
                Client client = networkType == 1
                        ? new ClientSocket(ip, 1234, model)
                        : new ClientRmi(ip, model);

                ViewTUI tui = new ViewTUI(model, client);
                client.setUi(tui);
                tui.setClient(client);
                client.start();
                connected = true;

            } catch (Exception e) {
                System.out.println("\n=================================");
                System.out.println(" ERRORE: Connessione al server fallita!");
                System.out.println("=================================\n");
            }
        }
    }

    /**
     * Gets the user input for the UI type.
     * @param scanner the scanner to use for user input
     * @param uiType the current UI type, or -1 if the user hasn't entered anything yet
     * @return the user's choice, or -1 if the input was invalid
     */
    private static int getUiType(Scanner scanner, int uiType) {
        String input = scanner.nextLine().trim();
        try {
            uiType = Integer.parseInt(input);
            if (uiType != 1 && uiType != 2) {
                System.out.println("Scelta non valida. Inserisci 1 o 2.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido. Inserisci un numero (1 o 2).\n");
        }
        return uiType;
    }

    /** Prevents instantiation of this entry-point class; all logic is driven by the {@code main} method. */
    private ClientMain() {}
}