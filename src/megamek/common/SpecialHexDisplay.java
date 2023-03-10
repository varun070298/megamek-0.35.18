/*
 * MegaMek - Copyright (C) 2007 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
package megamek.common;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.Serializable;

/**
 * @author dirk
 */
public class SpecialHexDisplay implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 27470795993329492L;

    public enum Type {
        ARTILLERY_AUTOHIT   ("data/images/hexes/artyauto.gif") {
            @Override
            public boolean drawBefore() {
                return false;
            }

            @Override
            public boolean drawAfter() {
                return true;
            }
        },
        ARTILLERY_ADJUSTED  ("data/images/hexes/artyadj.gif") {
            @Override
            public boolean drawBefore() {
                return false;
            }

            @Override
            public boolean drawAfter() {
                return true;
            }
        },
        ARTILLERY_INCOMING   ("data/images/hexes/artyinc.gif"),
        ARTILLERY_TARGET      ("data/images/hexes/artytarget.gif"){
            @Override
            public boolean drawBefore() {
                return false;
            }
        },
        ARTILLERY_HIT        ("data/images/hexes/artyhit.gif") {
            @Override
            public boolean drawBefore() {
                return false;
            }
        },
        PLAYER_NOTE         (null);

        private transient Image defaultImage;
        private final String defaultImagePath;

        Type(String iconPath) {
            defaultImagePath = iconPath;
        }

        public void init(Toolkit toolkit) {
            if(defaultImagePath != null) {
                defaultImage = toolkit.getImage(defaultImagePath);
            }

        }

        public Image getDefaultImage() {
            return defaultImage;
        }

        public void setDefaultImage(Image defaultImage) {
            this.defaultImage = defaultImage;
        }

        public boolean drawBefore() {
            return true;
        }

        public boolean drawAfter() {
            return false;
        }
    }

    private String info;
    private Type type;
    private int round;

    private String owner = null;

    private boolean obscured = true;

    public static int NO_ROUND = -99;

    @SuppressWarnings("unused")
    private SpecialHexDisplay() {
        //deserialization use only
    }

    public SpecialHexDisplay(Type type) {
        this.type = type;
        round = NO_ROUND;
    }

    public SpecialHexDisplay(Type type, String info) {
        this.type = type;
        this.info = info;
        round = NO_ROUND;
    }

    public SpecialHexDisplay(Type type, int round, String info) {
        this.type = type;
        this.info = info;
        this.round = round;
    }

    public SpecialHexDisplay(Type type, int round, String owner, String info) {
        this.type = type;
        this.info = info;
        this.round = round;
        this.owner = owner;
    }

    public SpecialHexDisplay(Type type, int round, String owner, String info, boolean obscured) {
        this.type = type;
        this.info = info;
        this.round = round;
        this.owner = owner;
        this.obscured = obscured;
    }

    public boolean thisRound(int testRound) {
        if (NO_ROUND == round) {
            return true;
        }
        return testRound == round;
    }

    /** Does this SpecialHexDisplayObjet concern a round in the future? */
    public boolean futureRound(int testRound) {
        if(NO_ROUND == round) {
            return true;
        }
        return testRound > round;
    }

    /** Does this SpecialHexDisplayObjet concern a round in the past? */
    public boolean pastRound(int testRound) {
        if(NO_ROUND == round) {
            return true;
        }
        return testRound < round;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isObscured() {
        return obscured;
    }

    public void setObscured(boolean obscured) {
        this.obscured = obscured;
    }

    /**
     * @param phase
     * @param curRound
     * @return
     */
    public boolean drawNow(IGame.Phase phase, int curRound) {
        boolean shouldDisplay = thisRound(curRound) ||
            (pastRound(curRound) && type.drawBefore()) ||
            (futureRound(curRound) && type.drawAfter());
        if(phase.isBefore(IGame.Phase.PHASE_OFFBOARD) &&
                ((type == Type.ARTILLERY_TARGET) || (type == Type.ARTILLERY_HIT))) {
            //hack to display atry targets the round after the hit.
            shouldDisplay = shouldDisplay || thisRound(curRound-1);
        }

        //System.err.println("turn: " + round + " Special type: " + type + " drawing: " + shouldDisplay + " details: " + info);

        return shouldDisplay;
    }

    /**
     * @param toPlayer
     * @return
     */
    public boolean isOwner(String toPlayer) {
        if((owner == null) || owner.equals(toPlayer)) {
            return true;
        }

        return false;
    }
}
