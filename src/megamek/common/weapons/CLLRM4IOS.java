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
public class CLLRM4IOS extends LRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -7115498642122846062L;

    /**
     *
     */
    public CLLRM4IOS() {
        super();
        techLevel = TechConstants.T_CLAN_TW;
        name = "LRM 4 (IOS)";
        setInternalName("CLLRM4IOS");
        heat = 0;
        rackSize = 4;
        minimumRange = WEAPON_NA;
        shortRange = 7;
        mediumRange = 14;
        longRange = 21;
        extremeRange = 28;
        tonnage = 0.3f;
        criticals = 0;
        bv = 9;
        flags = flags.or(F_ONESHOT);
    }
}
