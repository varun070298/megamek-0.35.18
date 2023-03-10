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
public class CLStreakLRM20OS extends StreakLRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -7687203185594888323L;

    /**
     *
     */
    public CLStreakLRM20OS() {
        super();
        techLevel = TechConstants.T_CLAN_EXPERIMENTAL;
        name = "Streak LRM 20 (OS)";
        setInternalName("CLOSStreakLRM20");
        addLookupName("Clan Streak LRM-20 (OS)");
        addLookupName("Clan Streak LRM 20 (OS)");
        addLookupName("CLStreakLRM20 (OS)");
        heat = 6;
        rackSize = 20;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        tonnage = 10.5f;
        criticals = 5;
        bv = 69;
        flags = flags.or(F_ONESHOT);
        cost = 600000;
    }
}
