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
public class CLStreakLRM15IOS extends StreakLRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 7336450815633311159L;

    /**
     *
     */
    public CLStreakLRM15IOS() {
        super();
        techLevel = TechConstants.T_CLAN_EXPERIMENTAL;
        name = "Streak LRM 15 (IOS)";
        setInternalName("CLIOSStreakLRM15");
        addLookupName("Clan Streak LRM-15 (IOS)");
        addLookupName("Clan Streak LRM 15 (IOS)");
        addLookupName("CLStreakLRM15 (IOS)");
        heat = 5;
        rackSize = 15;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        tonnage = 7.0f;
        criticals = 3;
        bv = 52;
        flags = flags.or(F_ONESHOT);
        cost = 400000;
    }
}
