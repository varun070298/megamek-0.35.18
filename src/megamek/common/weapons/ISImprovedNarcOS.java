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

import megamek.common.AmmoType;
import megamek.common.TechConstants;

/**
 * @author Sebastian Brocks
 */
public class ISImprovedNarcOS extends NarcWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -3509295242151016719L;

    /**
     *
     */
    public ISImprovedNarcOS() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "iNarc (OS)";
        setInternalName("ISImprovedNarc (OS)");
        addLookupName("IS OS iNarc Beacon");
        addLookupName("IS iNarc Missile Beacon (OS)");
        ammoType = AmmoType.T_INARC;
        heat = 0;
        rackSize = 1;
        shortRange = 4;
        mediumRange = 9;
        longRange = 15;
        extremeRange = 18;
        tonnage = 5.5f;
        criticals = 2;
        bv = 15;
        flags = flags.or(F_ONESHOT);
        cost = 250000;
    }
}
