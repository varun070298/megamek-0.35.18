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
 * @author Ben Grills
 */
public class InfantryGrenadeMiniInfernoWeapon extends InfantryWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -3164871600230559641L;

    public InfantryGrenadeMiniInfernoWeapon() {
        super();
        techLevel = TechConstants.T_TW_ALL;
        name = "Mini Inferno Grenades";
        setInternalName(name);
        addLookupName("InfantryMiniInfernoGrenade");
        ammoType = AmmoType.T_NA;
        cost = 8;
        bv = 0.15;
        flags = flags.or(F_INFERNO).or(F_BALLISTIC).or(F_INF_SUPPORT);
        infantryDamage = 0.09;
        infantryRange = 0;
    }
}
