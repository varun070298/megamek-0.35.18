/**
 * MegaMek - Copyright (C) 2007 Ben Mazur (bmazur@sev.org)
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import megamek.common.BattleArmor;
import megamek.common.Compute;
import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.IGame;
import megamek.common.Infantry;
import megamek.common.Minefield;
import megamek.common.Report;
import megamek.common.Targetable;
import megamek.common.ToHitData;
import megamek.common.actions.WeaponAttackAction;
import megamek.server.Server;

public class StreakLRMHandler extends StreakHandler {

    /**
     *
     */
    private static final long serialVersionUID = -3848472655779311898L;

    /**
     * @param t
     * @param w
     * @param g
     * @param s
     */
    public StreakLRMHandler(ToHitData t, WeaponAttackAction w, IGame g, Server s) {
        super(t, w, g, s);
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.weapons.WeaponHandler#calcDamagePerHit()
     */
    @Override
    protected int calcDamagePerHit() {
        if (target instanceof Infantry && !(target instanceof BattleArmor)) {
            int toReturn = Compute.directBlowInfantryDamage(wtype.getRackSize()*2, bDirect ? toHit.getMoS()/3 : 0, Compute.WEAPON_CLUSTER_MISSILE, ((Infantry)target).isMechanized());
            return toReturn;
        }
        return 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.weapons.WeaponHandler#calcnCluster()
     */
    @Override
    protected int calcnCluster() {
        return 5;
    }


    /*
     * (non-Javadoc)
     *
     * @see megamek.common.weapons.WeaponHandler#specialResolution(java.util.Vector,
     *      megamek.common.Entity, boolean)
     */
    @Override
    protected boolean specialResolution(Vector<Report> vPhaseReport,
            Entity entityTarget) {
        if (!bMissed
                && target.getTargetType() == Targetable.TYPE_MINEFIELD_CLEAR) {
            Report r = new Report(3255);
            r.indent(1);
            r.subject = subjectId;
            vPhaseReport.addElement(r);
            Coords coords = target.getPosition();
            Enumeration<Minefield> minefields = game.getMinefields(coords).elements();
            ArrayList<Minefield> mfRemoved = new ArrayList<Minefield>();
            while (minefields.hasMoreElements()) {
                Minefield mf = minefields.nextElement();
                if(server.clearMinefield(mf, ae, Minefield.CLEAR_NUMBER_WEAPON, vPhaseReport)) {
                    mfRemoved.add(mf);
                }
            }
            //we have to do it this way to avoid a concurrent error problem
            for(Minefield mf : mfRemoved) {
                server.removeMinefield(mf);
            }
            return true;
        }
        return false;
    }
}