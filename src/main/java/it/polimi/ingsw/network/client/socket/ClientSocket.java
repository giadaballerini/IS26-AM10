package it.polimi.ingsw.network.client.socket;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.TUI.ViewTUI;
import it.polimi.ingsw.client.UserInterface;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.server.ServerMessage;
import it.polimi.ingsw.visitors.ServerMessageVisitor;
import it.polimi.ingsw.visitors.ServerMessageVisitorImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ClientSocket extends Client implements Runnable{
    private boolean hasAvaiableLobbies = false;
    private int matchId;
    private VirtualModel model;
    private UserInterface ui;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerMessageVisitor visitor;
    private volatile boolean connected = true;

    public ClientSocket(String ip, int port){
        this.matchId = 0;
        try{
            this.socket = new Socket(ip, port);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.model = new VirtualModel();
            this.ui = new ViewTUI(model, this);
            this.visitor = new ServerMessageVisitorImpl(model, ui, this);
        } catch (IOException e) {
            this.connected = false;
        }
    }

    public synchronized void sendMessage(ClientMessage message){
        try{
            if(connected && out != null) {
                out.writeObject(message);
                out.flush();
                out.reset();
            }
        }catch (IOException e){
            disconnect();
        }
    }

    @Override
    public void run() {
        try{
            while(connected){
                ServerMessage message = (ServerMessage) in.readObject();
                if(message != null)
                    message.accept(visitor);
            }
        }catch (IOException | ClassNotFoundException e){
            disconnect();
        }
    }

    private void disconnect(){
        connected = false;
        try {
            if(in != null)
                in.close();
            if(out != null)
                out.close();
            if(socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {

            ui.printError(e);
            e.printStackTrace();
        }
    }


    @Override
    public String getNickname() {
        return model.getNickname();
    }

    @Override
    public boolean hasAvailableLobbies() {
        return hasAvaiableLobbies;
    }

    public void setHasAvaiableLobbies(boolean hasAvaiableLobbies){
        this.hasAvaiableLobbies = hasAvaiableLobbies;
    }

    @Override
    public void start() {
        ui.start();
    }

    @Override
    public boolean isInGame() {
        return matchId != 0;
    }

    @Override
    public void help() {
        ui.displayHelpMessage();
    }

    @Override
    public void info(int cardId) {
        ui.info(cardId);
    }

    @Override
    public boolean login(String nickname) {
        model.setNickname(nickname);
        sendMessage(new LoginMessage(nickname));
        return true;
    }

    @Override
    public void createGame(String nickname, int numPlayers) {
        sendMessage(new CreateGameMessage(numPlayers, nickname));
    }

    @Override
    public void joinGame(String nickname, int id) {
        sendMessage(new JoinGameMessage(id, nickname));
    }

    @Override
    public void move(int tileId) {
        sendMessage(new MoveMessage(model.getNickname(), tileId));
    }

    @Override
    public void skip() {
        sendMessage(new SkipMessage(model.getNickname()));
    }

    @Override
    public void draw(int card) {
        sendMessage(new DrawMessage(card, model.getNickname()));
    }

    @Override
    public Map<String, Integer> requestRanking() {
        //TODO
        return Map.of();
    }

    @Override
    public void requestJoin() {
        sendMessage(new AskLobbiesMessage());
    }

    @Override
    public void quit() {
        sendMessage(new QuitMessage());
    }

    @Override
    public void exit() {
        sendMessage(new ExitMessage());
    }

    public void setVisitor(ServerMessageVisitor visitor){
        this.visitor = visitor;
    }

    public void setMatchId(int matchId){this.matchId = matchId;}

    public boolean isConnected(){return connected;}
}
