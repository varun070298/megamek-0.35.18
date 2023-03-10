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
public class ISLRT20OS extends LRTWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -8753051336468930345L;

    /**
     *
     */
    public ISLRT20OS() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "LRT 20 (OS)";
        setInternalName(name);
        addLookupName("IS OS LRT-20");
        addLookupName("ISLRTorpedo20 (OS)");
        addLookupName("IS LRT 20 (OS)");
        heat = 6;
        rackSize = 20;
        minimumRange = 6;
        waterShortRange = 7;
        waterMediumRange = 14;
        waterLongRange = 21;
        waterExtremeRange = 28;
        tonnage = 10.5f;
        criticals = 5;
        bv = 36;
        flags = flags.or(F_ONESHOT);
        cost = 250000;
    }
}
