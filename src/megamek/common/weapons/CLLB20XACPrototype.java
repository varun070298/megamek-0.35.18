/**
 * MegaMek - Copyright (C) 2004,2005 Ben Mazur (bmazur@sev.org)
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
/*
 * Created on Oct 15, 2004
 *
 */
package megamek.common.weapons;

/**
 * @author Andrew Hunter
 */
public class CLLB20XACPrototype extends CLLBXACPrototypeWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -4257248228202258750L;

    /**
     *
     */
    public CLLB20XACPrototype() {
        super();
        name = "LB 20-X AC (CP)";
        setInternalName("CLLBXAC20Prototype");
        heat = 6;
        damage = 20;
        rackSize = 20;
        shortRange = 4;
        mediumRange = 8;
        longRange = 12;
        extremeRange = 16;
        tonnage = 14.0f;
        criticals = 12;
        bv = 237;
        flags = flags.or(F_SPLITABLE);
        cost = 600000;
        shortAV = 20;
        medAV = 20;
        maxRange = RANGE_MED;
    }
}
