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
 * @author Sebastian Brocks
 */
public class CLSRT4IOS extends SRTWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -1622623253640255529L;

    /**
     *
     */
    public CLSRT4IOS() {
        super();
        techLevel = TechConstants.T_CLAN_TW;
        name = "SRT 4 (IOS)";
        setInternalName("CLSRT4 (IOS)");
        addLookupName("Clan IOS SRT-4");
        addLookupName("Clan SRT 4 (IOS)");
        heat = 3;
        rackSize = 4;
        waterShortRange = 3;
        waterMediumRange = 6;
        waterLongRange = 9;
        waterExtremeRange = 12;
        tonnage = 1.0f;
        criticals = 1;
        bv = 8;
        flags = flags.or(F_NO_FIRES).or(F_ONESHOT);
        cost = 60000;
    }
}
