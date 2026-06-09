package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.server.rmi.ServerRmi;
import it.polimi.ingsw.network.server.socket.ServerSocket;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Application entry point for the game server.
 *
 * <p>Starts both transport layers in a single process:
 * <ul>
 *   <li><b>RMI</b> — creates an RMI registry on port 1099, instantiates
 *       {@link ServerRmi}, and binds it under the name {@code "GameServer"}.</li>
 *   <li><b>Socket (TCP)</b> — starts a {@link ServerSocket} on port 1234 on a
 *       dedicated thread.</li>
 * </ul>
 * Both layers share the same {@link MatchManager} instance so that RMI and
 * socket clients participate in the same matches.
 *
 * <p>The server's public IP is auto-detected via {@link #detectLocalIp()} and
 * set as the {@code java.rmi.server.hostname} system property so that RMI
 * callbacks reach the correct network interface.
 */
public class ServerMain {

    /**
     * Starts the server.
     *
     * <p>Configures RMI transport timeouts, detects the local IP, registers
     * the RMI stub, and launches the socket listener thread.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.setProperty("sun.rmi.transport.tcp.readTimeout", "5000");
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
        System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "3000");

        String serverIp = detectLocalIp();
        System.setProperty("java.rmi.server.hostname", serverIp);
        System.out.println("Binding RMI su: " + serverIp);

        try {
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
        } catch (RemoteException e) {
            System.out.println("Errore durante l'avvio del server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Detects the server's outbound IP address by briefly connecting a UDP
     * socket to a public DNS server (8.8.8.8). Falls back to {@code "localhost"}
     * if the detection fails.
     *
     * @return the detected local IP address string
     */
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