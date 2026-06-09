package it.polimi.ingsw.enumerations;

/**
 * Represents the type of a card in the game.
 *
 * <p>Cards are divided into three categories: buildings, characters, and events.
 * The {@link #isCharacter()} and {@link #isEvent()} methods can be used to
 * determine which category a card belongs to.</p>
 */
public enum CardTypeEnum {

    /** A building card. */
    BUILDING,

    /** A character card representing a gatherer. */
    GATHERER {
        public boolean isCharacter() { return true; }
    },

    /** A character card representing a hunter. */
    HUNTER {
        public boolean isCharacter() { return true; }
    },

    /** A character card representing a painter. */
    PAINTER {
        public boolean isCharacter() { return true; }
    },

    /** A character card representing a builder. */
    BUILDER {
        public boolean isCharacter() { return true; }
    },

    /** A character card representing a shaman. */
    SHAMAN {
        public boolean isCharacter() { return true; }
    },

    /** A character card representing a crafter. */
    CRAFTER {
        public boolean isCharacter() { return true; }
    },

    /** An event card representing a feast. */
    FEAST {
        public boolean isEvent() { return true; }
    },

    /** An event card representing a hunt. */
    HUNT {
        public boolean isEvent() { return true; }
    },

    /** An event card representing a stone painting. */
    STONE_PAINTING {
        public boolean isEvent() { return true; }
    },

    /** An event card representing a ritual. */
    RITUAL {
        public boolean isEvent() { return true; }
    };

    /**
     * Returns whether this card type is a character.
     *
     * @return {@code true} if this card type is a character, {@code false} otherwise
     */
    public boolean isCharacter() { return false; }

    /**
     * Returns whether this card type is an event.
     *
     * @return {@code true} if this card type is an event, {@code false} otherwise
     */
    public boolean isEvent() { return false; }
}
