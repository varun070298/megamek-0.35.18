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
package megamek.common.weapons;

import megamek.common.TechConstants;

/**
 * @author Jason Tighe
 */
public class CLMekMortar4 extends MekMortarWeapon{

    /**
     *
     */
    private static final long serialVersionUID = -7326848486069567891L;

    /**
     *
     */
    public CLMekMortar4() {
        super();
        techLevel = TechConstants.T_CLAN_ADVANCED;
        name = "Mortar 4";
        setInternalName("Clan Mech Mortar-4");
        addLookupName("CLMekMortar4");
        addLookupName("Clan Mek Mortar 4");
        rackSize = 1;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        bv = 26;
        heat = 5;
        criticals = 2;
        tonnage = 3.5f;
        cost = 32000;
    }
}
