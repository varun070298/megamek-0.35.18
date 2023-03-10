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
public class ISLRT5IOS extends LRTWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -7475019239065402296L;

    /**
     *
     */
    public ISLRT5IOS() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "LRT 5 (IOS)";
        setInternalName(name);
        addLookupName("IS IOS LRT-5");
        addLookupName("ISLRTorpedo5 (IOS)");
        addLookupName("IS LRT 5 (IOS)");
        heat = 2;
        rackSize = 5;
        minimumRange = 6;
        waterShortRange = 7;
        waterMediumRange = 14;
        waterLongRange = 21;
        waterExtremeRange = 28;
        tonnage = 2.0f;
        criticals = 1;
        bv = 9;
        flags = flags.or(F_ONESHOT);
        cost = 30000;
    }
}
