/**
 * MegaMek - Copyright (C) 2004,2005 Ben Mazur (bmazur@sev.org)
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
/*
 * Created on Sep 7, 2005
 *
 */
package megamek.common.weapons.infantry;

import megamek.common.AmmoType;
import megamek.common.TechConstants;

/**
 * @author Sebastian Brocks
 */
public class InfantryLRMWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -966926675003846938L;

    public InfantryLRMWeapon() {
        super();
        techLevel = TechConstants.T_IS_TW_NON_BOX;
        name = "LRM Launcher";
        setInternalName(name);
        addLookupName("InfantryLRM");
        ammoType = AmmoType.T_LRM;
        cost = 2000;
        bv = 1.15;
        flags = flags.or(F_NO_FIRES).or(F_DIRECT_FIRE).or(F_MISSILE).or(F_INF_ENCUMBER).or(F_INF_SUPPORT);
        setModes(new String[] { "", "Indirect" });
        infantryDamage = 0.19;
        infantryRange = 3;
    }
}
