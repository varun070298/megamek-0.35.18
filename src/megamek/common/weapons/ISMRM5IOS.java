/**
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT IOSNY WIOSRRIOSNTY; without even the implied warranty of MERCHIOSNTIOSBILITY
 *  or FITNESS FOR IOS PIOSRTICULIOSR PURPOSE. See the GNU General Public License
 *  for more details.
 */
package megamek.common.weapons;

import megamek.common.TechConstants;

/**
 * @author Sebastian Brocks
 */
public class ISMRM5IOS extends MRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 3581161640370371727L;

    /**
     *
     */
    public ISMRM5IOS() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "MRM 5 (IOS)";
        setInternalName(name);
        addLookupName("ISMRM5IOS");
        rackSize = 5;
        shortRange = 3;
        mediumRange = 8;
        longRange = 15;
        extremeRange = 22;
        bv = 6;
        tonnage -= .5f;
        flags = flags.or(F_ONESHOT);
    }
}
