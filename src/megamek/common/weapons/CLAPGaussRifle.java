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
 * Created on Oct 19, 2004
 *
 */
package megamek.common.weapons;

import megamek.common.AmmoType;
import megamek.common.IGame;
import megamek.common.TechConstants;
import megamek.common.ToHitData;
import megamek.common.actions.WeaponAttackAction;
import megamek.server.Server;

/**
 * @author Sebastian Brocks
 */
public class CLAPGaussRifle extends GaussWeapon {
    /**
     * 
     */
    private static final long serialVersionUID = 3055904827702262063L;

    /**
     * 
     */
    public CLAPGaussRifle() {
        super();
        this.techLevel = TechConstants.T_CLAN_TW;
        this.name = "AP Gauss Rifle";
        this.setInternalName("CLAPGaussRifle");
        this.addLookupName("Clan AP Gauss Rifle");
        this.heat = 1;
        this.damage = 3;
        this.ammoType = AmmoType.T_APGAUSS;
        this.shortRange = 3;
        this.mediumRange = 6;
        this.longRange = 9;
        this.extremeRange = 12;
        this.tonnage = 0.5f;
        this.criticals = 1;
        this.bv = 21;
        this.cost = 8500;
        this.shortAV = 3;
        this.maxRange = RANGE_SHORT;
        this.explosionDamage = 3;
    }

    /*
     * (non-Javadoc)
     * 
     * @see megamek.common.weapons.Weapon#getCorrectHandler(megamek.common.ToHitData,
     *      megamek.common.actions.WeaponAttackAction, megamek.common.Game,
     *      megamek.server.Server)
     */
    @Override
    protected AttackHandler getCorrectHandler(ToHitData toHit,
            WeaponAttackAction waa, IGame game, Server server) {
        return new APGaussWeaponHandler(toHit, waa, game, server);
    }
}
