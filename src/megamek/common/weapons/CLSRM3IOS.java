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
public class CLSRM3IOS extends SRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 1661723137877595056L;

    /**
     *
     */
    public CLSRM3IOS() {
        super();
        techLevel = TechConstants.T_CLAN_TW;
        name = "SRM 3 (IOS)";
        setInternalName("CLSRM3IOS");
        rackSize = 3;
        shortRange = 3;
        mediumRange = 6;
        longRange = 9;
        extremeRange = 12;
        bv = 6;
        tonnage -= .5f;
        flags = flags.or(F_NO_FIRES).or(F_ONESHOT);
    }
}
