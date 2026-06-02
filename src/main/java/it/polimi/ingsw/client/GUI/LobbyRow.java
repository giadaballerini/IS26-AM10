package it.polimi.ingsw.client.GUI;

public class LobbyRow {
    private final int id;
    private final int giocatoriAttuali;
    private final int maxGiocatori;
    private final String proprietario;

    public LobbyRow(int id, int giocatoriAttuali, int maxGiocatori, String proprietario){
        this.id = id;
        this.giocatoriAttuali = giocatoriAttuali;
        this.proprietario = proprietario;
        this.maxGiocatori = maxGiocatori;
    }

    public int getId() {
        return id;
    }
    public String getGiocatori() {return giocatoriAttuali + "/" + maxGiocatori;}
    public String getProprietario() {return proprietario;}
}
