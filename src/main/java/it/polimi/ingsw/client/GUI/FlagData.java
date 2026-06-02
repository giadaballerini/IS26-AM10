package it.polimi.ingsw.client.GUI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlagData {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;


    public String getDescription() { return description; }
    public String getName() { return name; }
}
