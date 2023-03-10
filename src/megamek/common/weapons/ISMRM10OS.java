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
public class ISMRM10OS extends MRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 1980690855716987710L;

    /**
     *
     */
    public ISMRM10OS() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "MRM 10 (OS)";
        setInternalName(name);
        addLookupName("OS MRM-10");
        addLookupName("ISMRM10 (OS)");
        addLookupName("IS MRM 10 (OS)");
        heat = 4;
        rackSize = 10;
        shortRange = 3;
        mediumRange = 8;
        longRange = 15;
        extremeRange = 22;
        tonnage = 3.5f;
        criticals = 2;
        bv = 11;
        flags = flags.or(F_ONESHOT);
        cost = 50000;
        shortAV = 6;
        medAV = 6;
        maxRange = RANGE_MED;
    }
}
