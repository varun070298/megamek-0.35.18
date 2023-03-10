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
public class InfantryPortableMGWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 3434311797513896108L;

    public InfantryPortableMGWeapon() {
        super();
        techLevel = TechConstants.T_INTRO_BOXSET;
        name = "Portable MG";
        setInternalName(name);
        addLookupName("InfantryMGPortable");
        addLookupName("InfantryMG");
        ammoType = AmmoType.T_MG;
        cost = 1000;
        bv = 1.6;
        flags = flags.or(F_NO_FIRES).or(F_DIRECT_FIRE).or(F_BALLISTIC).or(F_INF_BURST).or(F_INF_SUPPORT).or(F_INF_ENCUMBER);
        infantryDamage = 0.65;
        infantryRange = 1;
    }
}
