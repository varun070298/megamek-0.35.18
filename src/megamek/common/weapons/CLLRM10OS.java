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
public class CLLRM10OS extends LRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -1260890574819347313L;

    /**
     *
     */
    public CLLRM10OS() {
        super();
        techLevel = TechConstants.T_CLAN_TW;
        name = "LRM 10 (OS)";
        setInternalName("CLLRM10 (OS)");
        addLookupName("Clan OS LRM-10");
        addLookupName("Clan LRM 10 (OS)");
        heat = 4;
        rackSize = 10;
        minimumRange = WEAPON_NA;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        tonnage = 2.5f;
        criticals = 1;
        bv = 22;
        flags = flags.or(F_ONESHOT);
        cost = 100000;
        shortAV = 6;
        medAV = 6;
        longAV = 6;
        maxRange = RANGE_LONG;
    }
}
