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
public class ISMRM30IOS extends MRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 7118245780649534184L;

    /**
     *
     */
    public ISMRM30IOS() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "MRM 30 (IOS)";
        setInternalName(name);
        addLookupName("IOS MRM-30");
        addLookupName("ISMRM30 (IOS)");
        addLookupName("IS MRM 30 (IOS)");
        heat = 10;
        rackSize = 30;
        shortRange = 3;
        mediumRange = 8;
        longRange = 15;
        extremeRange = 22;
        tonnage = 10.0f;
        criticals = 5;
        bv = 34;
        flags = flags.or(F_ONESHOT);
        cost = 225000;
        shortAV = 18;
        medAV = 18;
        maxRange = RANGE_MED;
    }
}
