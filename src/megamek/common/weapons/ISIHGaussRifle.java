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
 * @author Jason Tighe
 */
public class ISIHGaussRifle extends GaussWeapon {
    /**
     *
     */
    private static final long serialVersionUID = -2379383217525139478L;

    /**
     *
     */
    public ISIHGaussRifle() {
        super();
        techLevel = TechConstants.T_IS_EXPERIMENTAL;
        name = "Improved Heavy Gauss Rifle";
        setInternalName("ISImprovedHeavyGaussRifle");
        addLookupName("IS Improved Heavy Gauss Rifle");
        heat = 2;
        damage = 22;
        ammoType = AmmoType.T_IGAUSS_HEAVY;
        minimumRange = 3;
        shortRange = 6;
        mediumRange = 12;
        longRange = 19;
        extremeRange = 24;
        flags = flags.or(F_SPLITABLE);
        tonnage = 20.0f;
        criticals = 11;
        bv = 385;
        cost = 700000;
        shortAV = 22;
        medAV = 22;
        longAV = 12;
        maxRange = RANGE_LONG;
        explosionDamage = 30;
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
        return new HGRHandler(toHit, waa, game, server);
    }

}
