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
public class CLAdvancedSRM5IOS extends AdvancedSRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 546071313282533016L;

    /**
     *
     */
    public CLAdvancedSRM5IOS() {
        super();
        techLevel = TechConstants.T_CLAN_TW;
        name = "Advanced SRM 5 (IOS)";
        setInternalName("CLAdvancedSRM5IOS");
        rackSize = 5;
        shortRange = 4;
        mediumRange = 8;
        longRange = 12;
        extremeRange = 16;
        bv = 15;
        tonnage -= .5f;
        flags = flags.or(F_NO_FIRES).or(F_ONESHOT);
    }
}
