/**
 * MegaMek - Copyright (C) 2004,2005,2006,2007 Ben Mazur (bmazur@sev.org)
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
import megamek.common.IGame;
import megamek.common.TechConstants;
import megamek.common.ToHitData;
import megamek.common.actions.WeaponAttackAction;
import megamek.server.Server;

/**
 * @author Sebastian Brocks
 */
public class CLMGA extends AmmoWeapon {

    /**
     *
     */
    private static final long serialVersionUID = 3727925307251828896L;

    public CLMGA() {
        super();
        techLevel = TechConstants.T_CLAN_TW;
        name = "Machine Gun Array";
        addLookupName("Clan Machine Gun Array");
        setInternalName("CLMGA");
        heat = 0;
        damage = 2;
        rackSize = 2;
        ammoType = AmmoType.T_MG;
        minimumRange = WEAPON_NA;
        shortRange = 1;
        mediumRange = 2;
        longRange = 3;
        extremeRange = 4;
        tonnage = 0.25f;
        criticals = 1;
        bv = 0; // we'll have to calculate this in calculateBV(),
        // because it depends on the number of MGs linked to
        // the MGA
        flags = flags.or(F_MECH_WEAPON).or(F_TANK_WEAPON).or(F_AERO_WEAPON).or(F_BALLISTIC).or(F_BURST_FIRE).or(F_MGA);
        cost = 5000;
        String[] modeStrings = { "Linked", "Off" };
        setModes(modeStrings);
        instantModeSwitch = false;
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
        return new MGAWeaponHandler(toHit, waa, game, server);
    }

}
