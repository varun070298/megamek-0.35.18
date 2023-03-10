/*
 * MegaMek - Copyright (C) 2000,2001,2002,2003,2004 Ben Mazur (bmazur@sev.org)
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
 * MechView.java
 *
 * Created on January 20, 2003 by Ryan McConnell
 */

package megamek.client.ui.swing;

import java.text.DecimalFormat;
import java.util.Iterator;

import megamek.client.ui.Messages;
import megamek.common.Aero;
import megamek.common.BattleArmor;
import megamek.common.BombType;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.FighterSquadron;
import megamek.common.GunEmplacement;
import megamek.common.IArmorState;
import megamek.common.Infantry;
import megamek.common.Jumpship;
import megamek.common.LargeSupportTank;
import megamek.common.Mech;
import megamek.common.Mounted;
import megamek.common.Protomech;
import megamek.common.SmallCraft;
import megamek.common.SpaceStation;
import megamek.common.Tank;
import megamek.common.TechConstants;
import megamek.common.Warship;
import megamek.common.WeaponType;
import megamek.common.weapons.BayWeapon;

/**
 * A utility class for retrieving mech information in a formatted string.
 */
public class MechView {

    private Entity entity;
    private boolean isMech;
    private boolean isInf;
    private boolean isBA;
    private boolean isVehicle;
    private boolean isProto;
    private boolean isGunEmplacement;
    private boolean isLargeSupportVehicle;
    private boolean isAero;
    private boolean isSquadron;
    private boolean isSmallCraft;
    private boolean isJumpship;
    private boolean isSpaceStation;

    StringBuffer sBasic = new StringBuffer();
    StringBuffer sLoadout = new StringBuffer();
    StringBuffer sFluff = new StringBuffer("");

    public MechView(Entity entity, boolean showDetail) {
        this.entity = entity;
        isMech = entity instanceof Mech;
        isInf = entity instanceof Infantry;
        isBA = entity instanceof BattleArmor;
        isVehicle = entity instanceof Tank;
        isProto = entity instanceof Protomech;
        isGunEmplacement = entity instanceof GunEmplacement;
        isLargeSupportVehicle = entity instanceof LargeSupportTank;
        isAero = entity instanceof Aero;
        isSquadron = entity instanceof FighterSquadron;
        isSmallCraft = entity instanceof SmallCraft;
        isJumpship = entity instanceof Jumpship;
        isSpaceStation = entity instanceof SpaceStation;

        sLoadout.append( getWeapons(showDetail) )
        .append("<br>"); //$NON-NLS-1$
        if(!entity.usesWeaponBays() || !showDetail) {
            sLoadout.append(getAmmo())
            .append("<br>"); //$NON-NLS-1$
        }
        if(entity instanceof Aero) {
            sLoadout.append(getBombs())
            .append("<br>"); //$NON-NLS-1$
        }
        sLoadout.append(getMisc()) //has to occur before basic is processed
        .append("<br>") //$NON-NLS-1$
        .append(getFailed()).append("<br>");
        //sBasic.append(getFluffImage(entity)).append("<br>");
        sBasic.append("<font size=+1><b>" + entity.getShortNameRaw() + "</b></font>");
        sBasic.append("<br>"); //$NON-NLS-1$
        if (entity.isMixedTech()) {
            if (entity.isClan()) {
                sBasic.append(Messages.getString("MechView.MixedClan"));
            } else {
                sBasic.append(Messages.getString("MechView.MixedIS"));
            }
        } else {
            sBasic.append(TechConstants.getLevelDisplayableName(entity
                    .getTechLevel()));
        }
        sBasic.append("<br>"); //$NON-NLS-1$
        if (!isInf) {
            sBasic.append(Math.round(entity.getWeight())).append(
                    Messages.getString("MechView.tons")); //$NON-NLS-1$
        }
        sBasic.append("<br>"); //$NON-NLS-1$
        DecimalFormat dFormatter = new DecimalFormat("#,###.##");
        sBasic.append("BV: ");
        sBasic.append(dFormatter.format(entity.calculateBattleValue()));
        sBasic.append("<br>"); //$NON-NLS-1$
        sBasic.append("Cost: ");
        sBasic.append(dFormatter.format(entity.getCost(false)));
        sBasic.append(" C-bills");
        sBasic.append("<br>"); //$NON-NLS-1$

        if (!isGunEmplacement) {
            sBasic.append("<br>"); //$NON-NLS-1$
            sBasic.append(Messages.getString("MechView.Movement")) //$NON-NLS-1$
            .append(entity.getWalkMP()).append("/") //$NON-NLS-1$
            .append(entity.getRunMPasString());
            if (entity.getJumpMP() > 0) {
                sBasic.append("/") //$NON-NLS-1$
                .append(entity.getJumpMP());
            }
        }
        if (isBA && ((BattleArmor)entity).isBurdened()) {
            sBasic.append("<br><i>(").append(Messages.getString("MechView.Burdened")).append(")</i>"); //$NON-NLS-1$
        }
        if (isVehicle) {
            sBasic.append(" (") //$NON-NLS-1$
            .append(Messages.getString("MovementType."+entity.getMovementModeAsString())).append(")"); //$NON-NLS-1$
        }
        sBasic.append("<br>"); //$NON-NLS-1$
        if (isMech || isVehicle|| (isAero && !isSmallCraft && !isJumpship && !isSquadron)) {
            sBasic.append(Messages.getString("MechView.Engine")); //$NON-NLS-1$
            sBasic.append(entity.getEngine().getShortEngineName());
            sBasic.append("<br>"); //$NON-NLS-1$
        }
        if (entity.hasBARArmor()) {
            sBasic.append(Messages.getString("MechView.BARRating")); //$NON-NLS-1$
            sBasic.append(entity.getBARRating());
            sBasic.append("<br>"); //$NON-NLS-1$
        }

        if (isAero ) {
            Aero a = (Aero)entity;
            sBasic.append( Messages.getString("MechView.HeatSinks") ) //$NON-NLS-1$
            .append( a.getHeatSinks() );
            if (a.getHeatCapacity() > a.getHeatSinks()) {
                sBasic.append( " [" ) //$NON-NLS-1$
                .append( a.getHeatCapacity() )
                .append( "]" ); //$NON-NLS-1$
            }
            if (a.getCockpitType() != Mech.COCKPIT_STANDARD) {
                sBasic.append("<br>"); //$NON-NLS-1$
                sBasic.append(Messages.getString("MechView.Cockpit"));
                sBasic.append(a.getCockpitTypeString());
            }
        }

        if (isMech) {
            Mech aMech = (Mech) entity;
            sBasic.append(Messages.getString("MechView.HeatSinks")) //$NON-NLS-1$
            .append(aMech.heatSinks());
            if (aMech.getHeatCapacity() > aMech.heatSinks()) {
                sBasic.append(" [") //$NON-NLS-1$
                .append(aMech.getHeatCapacity()).append("]"); //$NON-NLS-1$
            }
            if (aMech.getCockpitType() != Mech.COCKPIT_STANDARD) {
                sBasic.append("<br>"); //$NON-NLS-1$
                sBasic.append(Messages.getString("MechView.Cockpit"));
                sBasic.append(aMech.getCockpitTypeString());
            }
            if (aMech.getGyroType() != Mech.GYRO_STANDARD) {
                sBasic.append("<br>");
                sBasic.append(Messages.getString("MechView.Gyro"));
                sBasic.append(aMech.getGyroTypeString());
            }
            sBasic.append("<br>");
        }
        sBasic.append("<br>"); //$NON-NLS-1$
        if (!isGunEmplacement) {
            if( isSquadron ) {
                sBasic.append(getArmor());
            } else if( isAero ) {
                sBasic.append( getSIandArmor() );
            } else {
                sBasic.append( getInternalAndArmor() );
            }
        }
        if (entity.getFluff().getHistory() != null) {
            sFluff.append(entity.getFluff().getHistory());
        }
        sFluff.append("<br>");
    }

    public String getMechReadoutBasic() {
        return sBasic.toString();
    }

    public String getMechReadoutLoadout() {
        return sLoadout.toString();
    }

    public String getMechReadoutFluff() {
        return sFluff.toString();
    }

    public String getMechReadout() {
        return "<div style='font: 12pt monospaced'>" + getMechReadoutBasic()
        + "<br>" + getMechReadoutLoadout() + "<br>" + getMechReadoutFluff()
        + "</div>";
    }

    private String getInternalAndArmor() {
        StringBuffer sIntArm = new StringBuffer();

        int maxArmor = entity.getTotalInternal() * 2 + 3;
        if(isInf && !isBA) {
            Infantry inf = (Infantry)entity;
            sIntArm.append(Messages.getString("MechView.Men")).append(entity.getTotalInternal()).append( " (" + inf.getSquadSize() + "/" + inf.getSquadN() + ")");
        } else {
            sIntArm.append(Messages.getString("MechView.Internal")) //$NON-NLS-1$
            .append(entity.getTotalInternal());
        }
        if (isMech) {
            sIntArm.append(Messages.getString("MechView."
                    + EquipmentType.getStructureTypeName(entity
                            .getStructureType())));
        }
        sIntArm.append("<br>"); //$NON-NLS-1$

        if(isInf && !isBA) {
            Infantry inf = (Infantry)entity;
            sIntArm.append(Messages.getString("MechView.Armor")).append(inf.getArmorDesc());
        }
        else {
            sIntArm.append(Messages.getString("MechView.Armor")) //$NON-NLS-1$
            .append(entity.getTotalArmor());

        }
        if (isMech) {
            sIntArm.append("/") //$NON-NLS-1$
            .append(maxArmor);
        }
        if (!isInf && !isProto) {
            sIntArm.append(Messages.getString("MechView."
                    + EquipmentType.getArmorTypeName(entity.getArmorType())));
        }
        sIntArm.append("<br>"); //$NON-NLS-1$
        // Walk through the entity's locations.

        if(!(isInf && !isBA)) {
            sIntArm.append("<table cellspacing=0 cellpadding=1 border=0>");
            sIntArm.append("<tr><th></th><th>&nbsp;&nbsp;Internal</th><th>&nbsp;&nbsp;Armor</th></tr>");
            for (int loc = 0; loc < entity.locations(); loc++) {

                // Skip empty sections.
                if ((IArmorState.ARMOR_NA == entity.getInternal(loc))
                        || (isVehicle && !isLargeSupportVehicle && ((((loc == Tank.LOC_TURRET) && ((Tank) entity).hasNoTurret()) || (loc == Tank.LOC_BODY))
                                || (isLargeSupportVehicle && (((loc == LargeSupportTank.LOC_TURRET) && ((LargeSupportTank) entity).hasNoTurret()) || (loc == LargeSupportTank.LOC_BODY)))))) {
                    continue;
                }

                sIntArm.append("<tr>");
                sIntArm.append("<td>").append(entity.getLocationName(loc)).append("</td>"); //$NON-NLS-1$
                sIntArm.append(renderArmor(entity.getInternal(loc), entity.getOInternal(loc))); //$NON-NLS-1$
                if (IArmorState.ARMOR_NA != entity.getArmor(loc)) {
                    sIntArm.append(renderArmor(entity.getArmor(loc), entity.getOArmor(loc)));
                }
                sIntArm.append("</tr>"); //$NON-NLS-1$
                if (entity.hasRearArmor(loc)) {
                    sIntArm.append("<tr>"); //$NON-NLS-1$
                    sIntArm.append("<td>").append(entity.getLocationName(loc)).append(" (rear)").append("</td>").append("<td></td>");
                    sIntArm.append(renderArmor(entity.getArmor(loc, true), entity.getOArmor(loc, true))); //$NON-NLS-1$
                    sIntArm.append("</tr>"); //$NON-NLS-1$
                }
            }
            sIntArm.append("</table>");
        }
        return sIntArm.toString();
    }

    private String getSIandArmor() {

        Aero a = (Aero)entity;

        StringBuffer sIntArm = new StringBuffer();

        sIntArm.append( "<br>" ); //$NON-NLS-1$

        //int maxArmor = (int) mech.getWeight() * 8;
        sIntArm.append( Messages.getString("MechView.SI") ) //$NON-NLS-1$
        .append( a.getSI() );

        sIntArm.append( "<br>" ); //$NON-NLS-1$

        //if it is a jumpship get sail and KF integrity
        if(isJumpship & !isSpaceStation) {
            Jumpship js = (Jumpship)entity;

            sIntArm.append( Messages.getString("MechView.SailIntegrity") ) //$NON-NLS-1$
            .append( js.getSailIntegrity() );

            sIntArm.append( "<br>" ); //$NON-NLS-1$

            sIntArm.append( Messages.getString("MechView.KFIntegrity") ) //$NON-NLS-1$
            .append( js.getKFIntegrity() );

            sIntArm.append( "<br>" ); //$NON-NLS-1$
        }

        if(entity.isCapitalFighter()) {
            sIntArm.append(Messages.getString("MechView.Armor")) //$NON-NLS-1$
            .append( a.getCapArmor());
        } else {
            sIntArm.append(Messages.getString("MechView.Armor")) //$NON-NLS-1$
            .append( entity.getTotalArmor() );
        }

        if(isJumpship) {
            sIntArm.append(Messages.getString("MechView.CapitalArmor"));
        }

        sIntArm.append(Messages.getString("MechView."
                + EquipmentType.getArmorTypeName(entity.getArmorType())));

        sIntArm.append( "<br>" ); //$NON-NLS-1$
        // Walk through the entity's locations.
        if(!entity.isCapitalFighter()) {
            sIntArm.append("<table cellspacing=0 cellpadding=1 border=0>");
            sIntArm.append("<tr><th></th><th>&nbsp;&nbsp;Armor</th></tr>");
            for ( int loc = 0; loc < entity.locations(); loc++ ) {

                // Skip empty sections.
                if ( IArmorState.ARMOR_NA == entity.getInternal(loc)) {
                    continue;
                }
                //skip broadsides on warships
                if((entity instanceof Warship) && ((loc == Warship.LOC_LBS) || (loc == Warship.LOC_RBS))) {
                    continue;
                }
                //skip the "Wings" location
                if(!a.isLargeCraft() && (loc == Aero.LOC_WINGS)) {
                    continue;
                }

                sIntArm.append("<tr><td>").append(entity.getLocationName(loc) )
                .append( "</td>" ); //$NON-NLS-1$
                if ( IArmorState.ARMOR_NA != entity.getArmor(loc) ) {
                    sIntArm.append( renderArmor(entity.getArmor(loc), entity.getOArmor(loc)) );
                }
                /*
                if ( entity.hasRearArmor(loc) ) {
                    sIntArm.append( " (" ) //$NON-NLS-1$
                        .append( renderArmor(entity.getArmor(loc, true), entity.getOArmor(loc, true)) )
                        .append( ")" ); //$NON-NLS-1$
                }
                 */
                sIntArm.append( "</tr>" ); //$NON-NLS-1$
            }
            sIntArm.append("</table>");
        }



        return sIntArm.toString();
    }

    private String getArmor() {

        FighterSquadron fs = (FighterSquadron)entity;

        StringBuffer sIntArm = new StringBuffer();

        sIntArm.append( "<br>" ); //$NON-NLS-1$

        sIntArm.append(Messages.getString("MechView.Armor")) //$NON-NLS-1$
        .append( fs.getTotalArmor() );

        sIntArm.append( "<br>" ); //$NON-NLS-1$

        sIntArm.append(Messages.getString("MechView.ActiveFighters")) //$NON-NLS-1$
        .append( fs.getNFighters() );

        sIntArm.append( "<br>" ); //$NON-NLS-1$

        return sIntArm.toString();
    }

    private String getWeapons(boolean showDetail) {

        StringBuffer sWeapons = new StringBuffer();

        if(isInf && !isBA) {
            Infantry inf = (Infantry)entity;
            sWeapons.append("<table cellspacing=0 cellpadding=1 border=0>");
            sWeapons.append("<tr><td>Primary Weapon:</td> ");
            if(null == inf.getPrimaryWeapon()) {
                sWeapons.append("<td>None</td></tr>");
            } else {
                sWeapons.append("<td>" + inf.getPrimaryWeapon().getDesc() + "</td></tr>");
            }
            sWeapons.append("<tr><td>Secondary Weapon:</td> ");
            if((null == inf.getSecondaryWeapon()) || (inf.getSecondaryN() == 0)) {
                sWeapons.append("<td>None</td></tr>");
            } else {
                sWeapons.append("<td>" + inf.getSecondaryWeapon().getDesc() + " ("+ inf.getSecondaryN() + ")</td></tr>");
            }
            sWeapons.append("<tr><td>Damage per trooper:</td><td>").append((double)Math.round(inf.getDamagePerTrooper()*1000)/1000).append("</td></tr>");
            sWeapons.append("</table><p>");
        }


        if(entity.getWeaponList().size() < 1) {
            return "";
        }
        sWeapons.append("<table cellspacing=0 cellpadding=1 border=0>");
        sWeapons.append("<tr><th align='left'>Weapons</th><th>&nbsp;&nbsp;Loc</th><th>&nbsp;&nbsp;Heat</th></tr>");
        for (Mounted mounted : entity.getWeaponList()) {
            WeaponType wtype = (WeaponType) mounted.getType();

            if(mounted.isDestroyed()) {
                sWeapons.append("<tr bgcolor='red'>");
            } else {
                sWeapons.append("<tr>");
            }
            sWeapons.append("<td>").append(mounted.getDesc()); //$NON-NLS-1$
            if (entity.isClan()
                    && mounted.getType().getInternalName().substring(0, 2)
                    .equals("IS")) { //$NON-NLS-1$
                sWeapons.append(Messages.getString("MechView.IS")); //$NON-NLS-1$
            }
            if (!entity.isClan()
                    && mounted.getType().getInternalName().substring(0, 2)
                    .equals("CL")) { //$NON-NLS-1$
                sWeapons.append(Messages.getString("MechView.Clan")); //$NON-NLS-1$
            }
            /*
             * TODO: this should probably go in the ammo table somewhere
            if (wtype.hasFlag(WeaponType.F_ONESHOT)) {
                sWeapons.append(" [") //$NON-NLS-1$
                        .append(mounted.getLinked().getDesc()).append("]"); //$NON-NLS-1$
            }
             */
            sWeapons.append("</td>");

            sWeapons.append("<td align='right'>").append(entity.getLocationAbbr(mounted.getLocation()));
            if (mounted.isSplit()) {
                sWeapons.append("/") // $NON-NLS-1$
                .append(
                        entity.getLocationAbbr(mounted
                                .getSecondLocation()));
            }
            sWeapons.append("</td>"); //$NON-NLS-1$

            int heat = wtype.getHeat();
            if(wtype instanceof BayWeapon) {
                //loop through weapons in bay and add up heat
                heat = 0;
                for(int wId : mounted.getBayWeapons()) {
                    Mounted m = entity.getEquipment(wId);
                    if(null == m) {
                        continue;
                    }
                    heat = heat + ((WeaponType)m.getType()).getHeat();
                }
            }
            sWeapons.append("<td align='right'>").append(heat).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$

            //          if this is a weapon bay, then cycle through weapons and ammo
            /*
            if((wtype instanceof BayWeapon) && showDetail) {
                for(int wId : mounted.getBayWeapons()) {
                    Mounted m = entity.getEquipment(wId);
                    if(null == m) {
                        continue;
                    }

                    WeaponType newwtype = (WeaponType)m.getType();

                    sWeapons.append("  ")
                        .append( m.getDesc() );

                    if (entity.isClan() &&
                        m.getType().getInternalName().substring(0,2).equals("IS")) { //$NON-NLS-1$
                        sWeapons.append(Messages.getString("MechView.IS")); //$NON-NLS-1$
                    }
                    if (!entity.isClan() &&
                        m.getType().getInternalName().substring(0,2).equals("CL")) { //$NON-NLS-1$
                        sWeapons.append(Messages.getString("MechView.Clan")); //$NON-NLS-1$
                    }
                    if (newwtype.hasFlag(WeaponType.F_ONESHOT)) {
                        sWeapons.append(" <") //$NON-NLS-1$
                            .append(mounted.getLinked().getDesc())
                            .append(">"); //$NON-NLS-1$
                    }

                    sWeapons.append("<br>"); //$NON-NLS-1$
                }
            }
             */
        }
        sWeapons.append("</table>"); //$NON-NLS-1$
        return sWeapons.toString();
    }

    private String getAmmo() {
        StringBuffer sAmmo = new StringBuffer();
        if(entity.getAmmo().size() < 1) {
            return "";
        }
        sAmmo.append("<table cellspacing=0 cellpadding=1 border=0>");
        sAmmo.append("<tr><th align='left'>Ammo</th><th>&nbsp;&nbsp;Loc</th><th>&nbsp;&nbsp;Shots</th></tr>");
        for (Mounted mounted : entity.getAmmo()) {
            if(mounted.isDestroyed()) {
                sAmmo.append("<tr bgcolor='red'>");
            } else if(mounted.getShotsLeft() < 1) {
                sAmmo.append("<tr bgcolor='yellow'>");
            } else {
                sAmmo.append("<tr>");
            }
            if (mounted.getLocation() != Entity.LOC_NONE) {
                sAmmo.append("<td>").append(mounted.getName()).append("</td>");
                sAmmo.append("<td align='right'>").append(entity.getLocationAbbr(mounted.getLocation())).append("</td>");
                sAmmo.append("<td align='right'>").append(mounted.getShotsLeft()).append("</td>");
            }
        }
        sAmmo.append("</table>");
        return sAmmo.toString();
    }

    private String getBombs() {
        StringBuffer sBombs = new StringBuffer();
        Aero a = (Aero)entity;
        int[] choices = a.getBombChoices();
        for(int type = 0; type < BombType.B_NUM; type++) {
            if(choices[type] > 0) {
                sBombs.append(BombType.getBombName(type)).append(" (").append(Integer.toString(choices[type])).append(")<br>");
            }
        }
        return sBombs.toString();
    }

    private String getMisc() {
        StringBuffer sMisc = new StringBuffer();
        sMisc.append("<table cellspacing=0 cellpadding=1 border=0>");
        sMisc.append("<tr><th align='left'>Equipment</th><th>&nbsp;&nbsp;Loc</th></tr>");
        int nEquip = 0;
        for (Mounted mounted : entity.getMisc()) {
            String name = mounted.getName();
            if ((name.indexOf("Jump Jet") != -1 //$NON-NLS-1$
            )
            || ((name.indexOf("CASE") != -1) && entity.isClan()) //$NON-NLS-1$
            || (name.indexOf("Heat Sink") != -1 //$NON-NLS-1$
            )
            || (name.indexOf("Endo Steel") != -1 //$NON-NLS-1$
            )
            || (name.indexOf("Ferro-Fibrous") != -1) //$NON-NLS-1$

            || (name.indexOf("Ferro-Lamellor") != -1)) { //$NON-NLS-1$


                // These items are displayed elsewhere, so skip them here.
                continue;
            }
            nEquip++;
            if(mounted.isDestroyed()) {
                sMisc.append("<tr bgcolor='red'>");
            } else {
                sMisc.append("<tr>");
            }
            sMisc.append("<td>").append(mounted.getDesc()).append("</td>"); //$NON-NLS-1$
            if (entity.isClan()
                    && mounted.getType().getInternalName().substring(0, 2)
                    .equals("IS")) { //$NON-NLS-1$
                sMisc.append(Messages.getString("MechView.IS")); //$NON-NLS-1$
            }
            if (!entity.isClan()
                    && mounted.getType().getInternalName().substring(0, 2)
                    .equals("CL")) { //$NON-NLS-1$
                sMisc.append(Messages.getString("MechView.Clan")); //$NON-NLS-1$
            }
            sMisc.append("</td><td align='right'>").append(entity.getLocationAbbr(mounted.getLocation()))
            .append("</td>"); //$NON-NLS-1$

            sMisc.append("</tr>"); //$NON-NLS-1$
        }
        sMisc.append("</table>");
        if(nEquip < 1) {
            sMisc = new StringBuffer();
        }

        String capacity = entity.getUnusedString();
        if ((capacity != null) && (capacity.length() > 0)) {
            sMisc.append("<br><b>").append(Messages.getString("MechView.CarringCapacity")).append("</b><br>") //$NON-NLS-1$
            .append(capacity).append("<br>"); //$NON-NLS-1$
        }
        return sMisc.toString();
    }

    private String getFailed() {
        StringBuffer sFailed = new StringBuffer();
        Iterator<String> eFailed = entity.getFailedEquipment();
        if (eFailed.hasNext()) {
            sFailed.append("<br><b>The following equipment slots failed to load:</b><br>"); //$NON-NLS-1$
            while (eFailed.hasNext()) {
                sFailed.append(eFailed.next()).append("<br>"); //$NON-NLS-1$
            }
        }
        return sFailed.toString();
    }

    private static String renderArmor(int nArmor, int origArmor) {
        double percentRemaining = ((double)nArmor)/((double)origArmor);
        String armor = Integer.toString(nArmor);
        if (percentRemaining <= 0) {
            return "<td align='center' bgcolor='black'><font color='white'>" + "X" + "</font>";
        } else if (percentRemaining <= .25) {
            return "<td align='right' bgcolor='red'><font color='white'>" + armor + "</font>";
        } else if (percentRemaining <= .75) {
            return "<td align='right' bgcolor='yellow'><font color='black'>" + armor + "</font>";
        } else if (percentRemaining < 1.00) {
            return "<td align='right' bgcolor='green'><font color='white'>" + armor + "</font>";
        } else {
            return "<td align='right' bgcolor='white'><font color='black'>" + armor + "</font>";
        }
    }
}
