package it.polimi.ingsw.client.ui.gui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the display data associated with a player status flag, as loaded
 * from the client-side JSON resource file. Each flag corresponds to a special
 * effect active on the player (such as protection, double shaman income, or
 * category discounts) and is displayed as an icon with a tooltip in the GUI.
 *
 * @see FlagRegistry
 */
public class FlagData {

    /** The unique key identifying this flag, used to look it up in {@link FlagRegistry}. */
    @JsonProperty("name")
    private String name;

    /** The description displayed in the flag's tooltip in the GUI. */
    @JsonProperty("description")
    private String description;

    /**
     * The description displayed in the flag's tooltip in the GUI.
     * @return the description displayed in the flag's tooltip */
    public String getDescription() { return description; }

    /**
     * The name of the flag, used to look it up in {@link FlagRegistry}.
     *  @return the unique key identifying this flag */
    public String getName() { return name; }
    /** No-arg constructor required by Jackson for JSON deserialization. */
    private FlagData() {}
}