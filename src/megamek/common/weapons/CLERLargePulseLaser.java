/**
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
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
 * Created on Sep 12, 2004
 *
 */
package megamek.common.weapons;

import megamek.common.TechConstants;

/**
 * @author Sebastian Brocks
 */
public class CLERLargePulseLaser extends PulseLaserWeapon {
    /**
     *
     */
    private static final long serialVersionUID = -5795252987498124086L;

    /**
     *
     */
    public CLERLargePulseLaser() {
        super();
        techLevel = TechConstants.T_CLAN_EXPERIMENTAL;
        name = "ER Large Pulse Laser";
        setInternalName("CLERLargePulseLaser");
        addLookupName("Clan ER Pulse Large Laser");
        addLookupName("Clan ER Large Pulse Laser");
        heat = 13;
        damage = 10;
        toHitModifier = -1;
        shortRange = 7;
        mediumRange = 15;
        longRange = 23;
        extremeRange = 30;
        waterShortRange = 4;
        waterMediumRange = 10;
        waterLongRange = 14;
        waterExtremeRange = 20;
        tonnage = 6.0f;
        criticals = 3;
        bv = 272;
        cost = 400000;
    }
}
