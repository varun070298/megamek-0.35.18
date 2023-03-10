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
public class ISLRM10IOS extends LRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -2792101005477263443L;

    /**
     *
     */
    public ISLRM10IOS() {
        super();
        techLevel = TechConstants.T_INTRO_BOXSET;
        name = "LRM 10 (IOS)";
        setInternalName(name);
        addLookupName("IS IOS LRM-10");
        addLookupName("ISLRM10 (IOS)");
        addLookupName("IS LRM 10 (IOS)");
        heat = 4;
        rackSize = 10;
        minimumRange = 6;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        tonnage = 5.0f;
        criticals = 2;
        bv = 18;
        flags = flags.or(F_ONESHOT);
        cost = 100000;
        shortAV = 6;
        medAV = 6;
        longAV = 6;
        maxRange = RANGE_LONG;
    }
}
