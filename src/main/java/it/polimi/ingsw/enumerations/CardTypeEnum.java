package it.polimi.ingsw.enumerations;

public enum CardTypeEnum {
    BUILDING,
    GATHERER{
        public boolean isCharacter(){return true;}
    },
    HUNTER{
        public boolean isCharacter(){return true;}
    },
    PAINTER{
        public boolean isCharacter(){return true;}
    },
    BUILDER{
        public boolean isCharacter(){return true;}
    },
    SHAMAN{
        public boolean isCharacter(){return true;}
    },
    CRAFTER{
        public boolean isCharacter(){return true;}
    },
    FEAST{
        public boolean isEvent(){return true;}
    },
    HUNT{
        public boolean isEvent(){return true;}
    },
    STONE_PAINTING{
        public boolean isEvent(){return true;}
    },
    RITUAL{
        public boolean isEvent(){return true;}
    };

    public boolean isCharacter(){return false;}
    public boolean isEvent(){return false;}
}
