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
public class ISMRM20OS extends MRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -2738014475152659505L;

    /**
     *
     */
    public ISMRM20OS() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "MRM 20 (OS)";
        setInternalName(name);
        addLookupName("OS MRM-20");
        addLookupName("ISMRM20 (OS)");
        addLookupName("IS MRM 20 (OS)");
        heat = 6;
        rackSize = 20;
        shortRange = 3;
        mediumRange = 8;
        longRange = 15;
        extremeRange = 22;
        tonnage = 7.5f;
        criticals = 3;
        bv = 22;
        flags = flags.or(F_ONESHOT);
        cost = 125000;
        shortAV = 12;
        medAV = 12;
        maxRange = RANGE_MED;
    }
}
