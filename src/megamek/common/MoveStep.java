/*
 * MegaMek -
 * Copyright (C) 2000,2001,2002,2003,2004,2005 Ben Mazur (bmazur@sev.org)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * Created on Aug 28, 2003
 */
package megamek.common;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Vector;

import megamek.common.MovePath.MoveStepType;

/**
 * A single step in the entity's movment.
 */
public class MoveStep implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -6075640793056182285L;
    private MoveStepType type = MoveStepType.NONE;
    private int targetId = Entity.NONE;
    private int targetType = Targetable.TYPE_ENTITY;

    private Coords position;
    private int facing;

    private int mp; // this step
    private int mpUsed; // whole path

    private int heat; // this step
    private int totalHeat;

    private int distance;
    private int leapDistance;

    private int elevation = -999;
    private int altitude = -999;

    private int mineToLay = -1;

    /**
     * This step's static movement type. Additional steps in the path will not
     * change this value.
     */
    private EntityMovementType movementType;

    private boolean isProne;
    private boolean isFlying;
    private boolean isHullDown;
    private boolean climbMode;

    private boolean danger; // keep psr
    private boolean pastDanger;
    private boolean isUsingMASC;
    private int targetNumberMASC; // psr
    //
    private boolean firstStep; // check if no previous
    private boolean isTurning; // method
    private boolean isUnloaded;
    private boolean hasEverUnloaded;
    private boolean prevStepOnPavement; // prev
    private boolean hasJustStood;
    private boolean thisStepBackwards;
    private boolean onlyPavement; // additive
    private boolean isPavementStep;
    private boolean isRunProhibited = false;
    private boolean isStackingViolation = false;
    private boolean isDiggingIn = false;
    private MovePath parent = null;

    /*
     * Aero related stuf
     */
    private int velocity = -999;
    private int velocityN = -999;

    //also keep track of velocity left to spend
    private int velocityLeft = 0;
    //how many turns?
    private int nTurns = 0;
    private int nRolls = 0;
    //does the unit have a free turn available
    private boolean freeTurn = false;
    //how many hexes straight has the unit traveled
    private int nStraight = 0;
    //how many altitude down
    private int nDown = 0;
    //how many hexes moved in this velocity "chunk" (for aero on ground maps)
    private int nMoved = 0;
    //for Aeros, they may get pushed off board by OOC
    private boolean offBoard = false;
    //for optional vector movement
    private int[] mv;
    private int recoveryUnit = -1;
    TreeMap<Integer, Vector<Integer>> launched = new TreeMap<Integer, Vector<Integer>>();
    private boolean isEvading = false;
    private boolean isRolled = false;

    //for maneuvers
    private int maneuverType = ManeuverType.MAN_NONE;
    //steps associated with maneuvers have no cost
    private boolean noCost = false;
    //is this step part of a maneuver?
    private boolean maneuver = false;

    private Minefield mf;

    /**
     * Flag that indicates that this step is into prohibited terrain. <p/> If
     * the unit is jumping, this step is only invalid if it is the end of the
     * path.
     */
    private boolean terrainInvalid = false;

    /**
     * Flag that indicates that this step's position is the end of a path.
     */
    private boolean isEndPos = true;

    /**
     * Create a step of the given type.
     *
     * @param type - should match one of the MovePath constants, but this is not
     *            currently checked.
     */
    public MoveStep(MovePath path, MoveStepType type) {
        this.type = type;
        parent = path;
        if ((type == MoveStepType.UNLOAD) || (type==MoveStepType.LAUNCH) || (type==MoveStepType.DROP)) {
            hasEverUnloaded = true;
        } else {
            hasEverUnloaded = false;
        }
    }

    /**
     * Create a step with the given target.
     *
     * @param type - should match one of the MovePath constants, but this is not
     *            currently checked.
     * @param target - the <code>Targetable</code> that is the target of this
     *            step. For example, the enemy being charged.
     */
    public MoveStep(MovePath path, MoveStepType type, Targetable target) {
        this(path, type);
        targetId = target.getTargetId();
        targetType = target.getTargetType();
        if ((type == MoveStepType.UNLOAD) || (type==MoveStepType.LAUNCH) || (type==MoveStepType.DROP)) {
            hasEverUnloaded = true;
        } else {
            hasEverUnloaded = false;
        }
    }

    /**
     * Create a step with the given mine to lay.
     *
     * @param path
     * @param type - should match one of the MovePath constants, but this is not
     *            currently checked.
     * @param mineToLay - the <code>int</code> that is the id of the mine that
     *            should be laid in this step.
     */
    public MoveStep(MovePath path, MoveStepType type, int mineToLay) {
        this(path, type);
        this.mineToLay = mineToLay;
    }

    /**
     * Create a step with the units to launch or drop.
     *
     * @param path
     * @param type - should match one of the MovePath constants, but this is not
     *            currently checked.
     * @param targets - vector of integers identifying the entities to launch
     */
    public MoveStep(MovePath path, MoveStepType type, TreeMap<Integer, Vector<Integer>> targets) {
        this(path, type);
        launched = targets;
        if ((type==MoveStepType.UNLOAD) || (type==MoveStepType.LAUNCH) || (type==MoveStepType.DROP)) {
            hasEverUnloaded=true;
        } else {
            hasEverUnloaded=false;
        }
    }

    public MoveStep(MovePath path, MoveStepType type, int recovery, int mineToLay) {
        this(path, type);
        recoveryUnit = recovery;
        this.mineToLay = mineToLay;
    }

    public MoveStep(MovePath path, MoveStepType type, boolean noCost) {
        this(path, type);
        this.noCost = noCost;
    }

    public MoveStep(MovePath path, MoveStepType type, boolean noCost, boolean isManeuver) {
        this(path, type);
        this.noCost = noCost;
        maneuver = isManeuver;
    }

    public MoveStep(MovePath path, MoveStepType type, int recovery, int mineToLay, int manType) {
        this(path, type);
        recoveryUnit = recovery;
        this.mineToLay = mineToLay;
        maneuverType = manType;
    }

    public MoveStep(MovePath path, MoveStepType type, Minefield mf) {
        this(path, type);
        this.mf = mf;
    }

    void setParent(MovePath path) {
        parent = path;
    }

    @Override
    public String toString() {
        switch (type) {
        case BACKWARDS:
            return "B";
        case CHARGE:
            return "Ch";
        case DFA:
            return "DFA";
        case FORWARDS:
            return "F";
        case CAREFUL_STAND:
        case GET_UP:
            return "Up";
        case GO_PRONE:
            return "Prone";
        case START_JUMP:
            return "StrJump";
        case TURN_LEFT:
            return "L";
        case TURN_RIGHT:
            return "R";
        case LATERAL_LEFT:
            return "ShL";
        case LATERAL_RIGHT:
            return "ShR";
        case LATERAL_LEFT_BACKWARDS:
            return "ShLB";
        case LATERAL_RIGHT_BACKWARDS:
            return "ShRB";
        case UNJAM_RAC:
            return "Unjam";
        case SEARCHLIGHT:
            return "SLight";
        case LOAD:
            return "Load";
        case UNLOAD:
            return "Unload";
        case EJECT:
            return "Eject";
        case UP:
            return "U";
        case DOWN:
            return "D";
        case HULL_DOWN:
            return "HullDown";
        case CLIMB_MODE_ON:
            return "CM+";
        case CLIMB_MODE_OFF:
            return "CM-";
        case TAKEOFF:
            return "Takeoff";
        case VTAKEOFF:
            return "Vertical Takeoff";
        case LAND:
            return "Landing";
        default:
            return "???";
        }
    }

    public MoveStepType getType() {
        return type;
    }

    public MovePath getParent() {
        return parent;
    }

    /**
     * get a copy of this MoveStep's parent, but only upto this step
     * @return
     */
    public MovePath getParentUpToThisStep() {
        Vector<MoveStep> steps = new Vector<MoveStep>();
        MovePath toReturn = new MovePath(parent.game, parent.getEntity());
        for (Enumeration<MoveStep> e = parent.getSteps();; e.hasMoreElements()) {
            MoveStep step = e.nextElement();
            steps.add(step);
            if (step.equals(this)) {
                break;
            }
        }
        toReturn.steps = steps;
        return toReturn;
    }

    /**
     * Set the target of the current step.
     *
     * @param target - the <code>Targetable</code> that is the target of this
     *            step. For example, the enemy being charged. If there is no
     *            target, pass a <code>null</code>
     */
    public void setTarget(Targetable target) {
        if (target == null) {
            targetId = Entity.NONE;
            targetType = Targetable.TYPE_ENTITY;
        } else {
            targetId = target.getTargetId();
            targetType = target.getTargetType();
        }
    }

    /**
     * Get the target of the current step.
     *
     * @param game - The <code>Game</code> object.
     * @return The <code>Targetable</code> that is the target of this step.
     *         For example, the enemy being charged. This value may be
     *         <code>null</code>
     */
    public Targetable getTarget(IGame game) {
        if (targetId == Entity.NONE) {
            return null;
        }
        return game.getTarget(targetType, targetId);
    }

    public TreeMap<Integer, Vector<Integer>> getLaunched() {
        return launched;
    }

    /**
     * Helper for compile(), to deal with steps that move to a new hex.
     *
     * @param game
     * @param entity
     * @param prev
     */
    private void compileMove(final IGame game, final Entity entity,
            MoveStep prev) {

        IHex destHex = game.getBoard().getHex(getPosition());

        // Check for pavement movement.
        if (Compute.canMoveOnPavement(game, prev.getPosition(), getPosition(), getParentUpToThisStep())) {
            setPavementStep(true);
        } else {
            setPavementStep(false);
            setOnlyPavement(false);
        }

        setHasJustStood(false);
        if (prev.isThisStepBackwards() != isThisStepBackwards()) {
            setDistance(0); // start over after shifting gears
        }
        addDistance(1);

        //need to reduce velocity left for aerospace units (and also reset nTurns)
        //this is handled differently by aerospace units operating on the ground map and by spheroids in atmosphere
        if(entity.isAirborne() && game.getBoard().onGround()) {
            setNMoved(getNMoved() + 1);
            if((entity.getMovementMode() != EntityMovementMode.SPHEROID) && (getNMoved() >= 16)) {
                setVelocityLeft(getVelocityLeft() - 1);
                setNMoved(0);
            }
        } else if(entity.isAirborne() && !game.useVectorMove() && !useSpheroidAtmosphere(game, entity)) {
            setVelocityLeft(getVelocityLeft() - 1);
            setNTurns(0);
        }

        //if in atmosphere, then I need to know if this move qualifies the unit
        //for a free turn
        if(useAeroAtmosphere(game, entity)) {
            setNStraight(getNStraight() + 1);
            if(game.getBoard().onGround() && (getNStraight() > 7)) {
                //if flying on ground map, then you have to fly at least 8 straight hexes between turns (free or not)
                //http://www.classicbattletech.com/forums/index.php/topic,37171.new.html#new
                setNTurns(0);
            }
            if(!hasFreeTurn()) {
                //check conditions
                if(dueFreeTurn()) {
                    setFreeTurn(true);
                }
            }
        }

        if (getType() == MoveStepType.DFA) {
            IHex hex = game.getBoard().getHex(getPosition());
            setElevation(Math.max(0, hex.terrainLevel(Terrains.BLDG_ELEV)));
            // If we're DFA-ing, we want to be 1 above the level of the target.
            // However, if that puts us in the ground, we're instead 1 above the
            // level of the hex right before the target.
            int otherEl = 0;
            IHex hex2 = game.getBoard().getHex(prev.getPosition());
            otherEl = Math.max(0, hex2.terrainLevel(Terrains.BLDG_ELEV));
            if (otherEl > getElevation()) {
                setElevation(otherEl);
            }
            setElevation(getElevation() + 1);
        } else if (parent.isJumping()) {
            IHex hex = game.getBoard().getHex(getPosition());
            int maxElevation = entity.getJumpMP() + entity.getElevation()
            + game.getBoard().getHex(entity.getPosition()).surface()
            - hex.surface();
            int building = hex.terrainLevel(Terrains.BLDG_ELEV);
            int depth = -hex.depth();
            //need to adjust depth for potential ice over water
            if(hex.containsTerrain(Terrains.ICE) && hex.containsTerrain(Terrains.WATER)) {
                depth = 0;
            }
            if (entity instanceof Infantry) {
                // infantry can jump into a building
                setElevation(Math.max(depth, Math.min(building,
                        maxElevation)));
            } else {
                setElevation(Math.max(depth, building));
            }
            if (climbMode()
                    && (maxElevation >= hex.terrainLevel(Terrains.BRIDGE_ELEV))) {
                setElevation(Math.max(getElevation(), hex
                        .terrainLevel(Terrains.BRIDGE_ELEV)));
            }
        } else {
            Building bld = game.getBoard().getBuildingAt(getPosition());

            if (bld != null) {
                IHex hex = game.getBoard().getHex(getPosition());
                int maxElevation = entity.getElevation()
                + game.getBoard().getHex(entity.getPosition())
                .surface() - hex.surface();

                // Meks can climb up level 2 walls or less while everything
                // can only climb up one level
                if (entity instanceof Mech) {
                    maxElevation += 2;
                } else {
                    maxElevation++;
                }

                if (bld.getType() == Building.WALL) {
                    if (maxElevation >= hex.terrainLevel(Terrains.BLDG_ELEV)) {
                        setElevation(Math.max(getElevation(), hex
                                .terrainLevel(Terrains.BLDG_ELEV)));
                    } else {// if the wall is taller then the unit then they
                        // cannot climb it or enter it
                        return;
                    }
                } else {
                    setElevation(entity.calcElevation(game.getBoard().getHex(
                            prev.getPosition()), game.getBoard().getHex(
                                    getPosition()), elevation, climbMode(),
                                    (entity.getMovementMode() == EntityMovementMode.WIGE)
                                    && (prev.getType() == MoveStepType.CLIMB_MODE_OFF)));
                }
            } else {
                setElevation(entity.calcElevation(game.getBoard().getHex(
                        prev.getPosition()), game.getBoard().getHex(
                                getPosition()), elevation, climbMode(),
                                (entity.getMovementMode() == EntityMovementMode.WIGE)
                                && (prev.getType() == MoveStepType.CLIMB_MODE_OFF)));
            }
        }

        //if this is a flying aero, then there is no MP cost for moving
        if(entity.isAirborne()) {
            setMp(0);
            //if this a spheroid in atmosphere then the cost is always two
            if(useSpheroidAtmosphere(game, entity)) {
                if(game.getBoard().onGround()) {
                    //spheroids only pay for the first hex moved into every 8 hexes
                    if((distance % 8) == 1) {
                        setMp(1);
                    }
                } else {
                    setMp(2);
                }
            }
        } else {
            calcMovementCostFor(game, prev.getPosition(), prev.getElevation());
        }

        // check for water
        if (!isPavementStep()
                && (destHex.terrainLevel(Terrains.WATER) > 0)
                && !(destHex.containsTerrain(Terrains.ICE) && (elevation >= 0))
                && !(destHex.terrainLevel(Terrains.BRIDGE_ELEV) == elevation)
                && (entity.getMovementMode() != EntityMovementMode.HOVER)
                && (entity.getMovementMode() != EntityMovementMode.NAVAL)
                && (entity.getMovementMode() != EntityMovementMode.HYDROFOIL)
                && (entity.getMovementMode() != EntityMovementMode.INF_UMU)
                // sub can't flank underwater
                && !((elevation == 0) && (entity.getMovementMode() == EntityMovementMode.SUBMARINE))
                && (entity.getMovementMode() != EntityMovementMode.VTOL)
                && (entity.getMovementMode() != EntityMovementMode.WIGE)) {
            setRunProhibited(true);
        }

        int magmaLevel = destHex.terrainLevel(Terrains.MAGMA);
        if (elevation > 0) {
            magmaLevel = 0;
        }
        // Check for fire or magma crust in the new hex.
        if (destHex.containsTerrain(Terrains.FIRE) || (magmaLevel == 1)) {
            heat = 2;
            totalHeat += 2;
        }
        // Check for liquid magma
        else if (magmaLevel == 2) {
            heat = 5;
            totalHeat += 5;
        }

    }

    /**
     * Compile the static move data for this step.
     *
     * @param game the <code>Game</code> being played.
     * @param entity the <code>Entity</code> taking this step.
     * @param prev the previous step in the path.
     */
    protected void compile(final IGame game, final Entity entity, MoveStep prev) {
        final boolean isInfantry = entity instanceof Infantry;
        boolean isFieldArtillery = (entity instanceof Infantry) && ((Infantry)entity).hasActiveFieldArtillery();
        copy(game, prev);

        // Is this the first step?
        if (prev == null) {
            prev = new MoveStep(parent, MoveStepType.FORWARDS);
            prev.setFromEntity(entity, game);
            setFirstStep(prev.mpUsed == 0); // Bug 1519330 - its not a first
            // step when continuing after a fall
        }
        switch (getType()) {
        case UNLOAD:
            // Infantry in immobilized transporters get
            // a special "unload stranded" game turn.
            hasEverUnloaded = true;
            setMp(1);
            break;
        case LOAD:
            setMp(1);
            break;
        case TURN_LEFT:
        case TURN_RIGHT:
            // Check for pavement movement.
            if (Compute.canMoveOnPavement(game, prev.getPosition(),
                    getPosition(), getParentUpToThisStep())) {
                setPavementStep(true);
            } else {
                setPavementStep(false);
                setOnlyPavement(false);
            }

            // Infantry can turn for free, except for field artillery
            setMp((parent.isJumping() || isHasJustStood() || (isInfantry && !isFieldArtillery)) ? 0
                    : 1);
            if(entity.isAirborne() && (entity instanceof Aero)) {
                setMp(asfTurnCost(game, getType(), entity));
                setNTurns(getNTurns() + 1);

                if(useAeroAtmosphere(game, entity)) {
                    setNStraight(0);
                    setFreeTurn(false);
                }

            }
            if(entity.isDropping()) {
                setMp(0);
            }
            adjustFacing(getType());
            break;
        case BACKWARDS:
            moveInDir((getFacing() + 3) % 6);
            setThisStepBackwards(true);
            setRunProhibited(true);
            compileMove(game, entity, prev);
            break;
        case FORWARDS:
        case DFA:
        case SWIM:
            // step forwards or backwards
            moveInDir(getFacing());
            setThisStepBackwards(false);
            compileMove(game, entity, prev);
            break;
        case CHARGE :
            if(!(entity.isAirborne()) || !game.useVectorMove()) {
                moveInDir(getFacing());
                setThisStepBackwards(false);
                compileMove(game, entity, prev);
            }
            break;
        case LATERAL_LEFT_BACKWARDS:
        case LATERAL_RIGHT_BACKWARDS:
            moveInDir((MovePath.getAdjustedFacing(getFacing(), MovePath
                    .turnForLateralShift(getType())) + 3) % 6);
            setThisStepBackwards(true);
            setRunProhibited(true);
            compileMove(game, entity, prev);
            if(entity.isAirborne()) {
                setMp(0);
            } else if(entity.isUsingManAce() & (entity instanceof QuadMech)) {
                setMp(getMp());
            } else {
                setMp(getMp() + 1); //+1 for side step
            }
            break;
        case LATERAL_LEFT:
        case LATERAL_RIGHT:
            moveInDir(MovePath.getAdjustedFacing(getFacing(), MovePath
                    .turnForLateralShift(getType())));
            setThisStepBackwards(false);
            compileMove(game, entity, prev);
            if(entity.isAirborne()) {
                setMp(0);
            } else if(entity.isUsingManAce() & (entity instanceof QuadMech)) {
                setMp(getMp());
            } else {
                setMp(getMp() + 1); //+1 for side step
            }
            break;
        case GET_UP:
            // mechs with 1 MP are allowed to get up
            setMp(entity.getRunMP() == 1 ? 1 : 2);
            setHasJustStood(true);
            break;
        case CAREFUL_STAND:
            if ( entity.getWalkMP() <= 2) {
                entity.setCarefulStand(false);
                setMp(entity.getRunMP() == 1 ? 1 : 2);
            }else {
                setMp(entity.getWalkMP());
            }
            setHasJustStood(true);
            break;
        case GO_PRONE:
            if (!entity.isHullDown()) {
                setMp(1);
            }
            break;
        case START_JUMP:
            break;
        case UP:
            if (entity.isAirborne()) {
                setAltitude(altitude + 1);
                setMp(2);
            } else {
                setElevation(elevation + 1);
                if (entity.getMovementMode() == EntityMovementMode.WIGE) {
                    setMp(5);
                } else {
                    setMp(parent.isJumping()?0:1);
                }
            }
            break;
        case DOWN:
            if (entity.isAirborne()) {
                setAltitude(altitude - 1);
                // it costs nothing (and may increase velocity)
                setMp(0);
                setNDown(getNDown() + 1);
            } else {
                setElevation(elevation - 1);
                if (entity.getMovementMode() == EntityMovementMode.WIGE) {
                    setMp(0);
                } else {
                    setMp(parent.isJumping() ? 0 : 1);
                }
            }
            break;
        case HULL_DOWN:
            if ( isProne() && (entity instanceof Mech)){
                int mpUsed = 1;
                if ( entity instanceof BipedMech ){
                    for ( int location = Mech.LOC_RLEG; location <= Mech.LOC_LLEG; location++ ){
                        if ( entity.isLocationBad(location ) ){
                            mpUsed += 99;
                            break;
                        }
                        mpUsed += ((Mech)entity).countLegActuatorCrits(location);
                        if ( ((Mech)entity).legHasHipCrit(location) ){
                            mpUsed += 1;
                        }
                    }
                }else {
                    for ( int location = Mech.LOC_RARM; location <= Mech.LOC_LLEG; location++ ){
                        if ( entity.isLocationBad(location ) ){
                            mpUsed += 99;
                            break;
                        }
                        mpUsed += ((QuadMech)entity).countLegActuatorCrits(location);
                        if ( ((QuadMech)entity).legHasHipCrit(location) ){
                            mpUsed += 1;
                        }
                    }
                }
                setMp(mpUsed);
            }else{
                setMp(2);
            }
            break;
        case CLIMB_MODE_ON:
            setClimbMode(true);
            break;
        case CLIMB_MODE_OFF:
            setClimbMode(false);
            break;
        case SHAKE_OFF_SWARMERS:
            // Counts as flank move but you can only use cruise MP
            setMp(entity.getRunMP() - entity.getWalkMP());
            break;
        case TAKEOFF:
        case VTAKEOFF:
            setMp(0);
            break;
        case LAND:
            setMp(2);
            break;
        case ACCN:
            setVelocityN(getVelocityN()+1);
            setMp(1);
            break;
        case DECN:
            setVelocityN(getVelocityN()-1);
            setMp(1);
            break;
        case ACC:
            setVelocity(getVelocity()+1);
            setVelocityLeft(getVelocityLeft()+1);
            setMp(1);
            break;
        case DEC:
            setVelocity(getVelocity()-1);
            setVelocityLeft(getVelocityLeft()-1);
            setMp(1);
            break;
        case EVADE:
            setEvading(true);
            if(entity.isAirborne()) {
                setMp(2);
            }
            break;
        case ROLL:
            if(prev.isRolled) {
                isRolled = false;
            } else {
                isRolled = true;
            }
            //doesn't cost anything if previous was a yaw
            if(prev.getType() != MoveStepType.YAW) {
                setMp(1);
                setNRolls(getNRolls() + 1);
            } else {
                setMp(0);
            }
            break;
        case LAUNCH:
        case DROP:
            hasEverUnloaded=true;
            setMp(0);
            break;
        case RECOVER:
            setMp(0);
            break;
        case JOIN:
            setMp(0);
            break;
        case THRUST:
            setVectors(Compute.changeVectors(getVectors(), getFacing()));
            setMp(1);
            break;
        case YAW:
            setNRolls(getNRolls() + 1);
            reverseFacing();
            setMp(2);
            break;
        case HOVER:
            setMp(2);
            break;
        case MANEUVER:
            int cost = ManeuverType.getCost(getManeuverType(), getVelocity());
            if(entity.isUsingManAce()) {
                cost = Math.max(cost - 1, 0);
            }
            setMp(cost);
            break;
        case LOOP:
            setVelocityLeft(getVelocityLeft() - 4);
            setMp(0);
        default:
            setMp(0);
        }

        if(noCost) {
            setMp(0);
        }

        // Update the entity's total MP used.
        addMpUsed(getMp());

        // Check for a stacking violation.
        final Entity violation = Compute.stackingViolation(game,
                entity.getId(), getPosition());
        if ((violation != null) && (getType() != MoveStepType.CHARGE)
                && (getType() != MoveStepType.DFA)) {
            setStackingViolation(true);
        }

        // set moveType, illegal, trouble flags
        compileIllegal(game, entity, prev);
    }

    /**
     * Returns whether the two step types contain opposite turns
     */
    boolean oppositeTurn(MoveStep turn2) {
        switch (type) {
        case TURN_LEFT:
            return turn2.getType() == MoveStepType.TURN_RIGHT;
        case TURN_RIGHT:
            return turn2.getType() == MoveStepType.TURN_LEFT;
        default:
            return false;
        }
    }

    public void setElevation(int el) {
        elevation = el;
    }

    public void setAltitude(int alt) {
        altitude = alt;
    }

    /**
     * Takes the given state as the previous state and sets flags from it.
     *
     * @param game
     * @param prev
     */
    public void copy(final IGame game, MoveStep prev) {
        if (prev == null) {
            setFromEntity(parent.getEntity(), game);
            return;
        }
        hasJustStood = prev.hasJustStood;
        facing = prev.getFacing();
        position = prev.getPosition();

        distance = prev.getDistance();
        mpUsed = prev.mpUsed;
        totalHeat = prev.totalHeat;
        isPavementStep = prev.isPavementStep;
        onlyPavement = prev.onlyPavement;
        thisStepBackwards = prev.thisStepBackwards;
        isProne = prev.isProne;
        isFlying = prev.isFlying;
        isHullDown = prev.isHullDown;
        climbMode = prev.climbMode;
        isRunProhibited = prev.isRunProhibited;
        hasEverUnloaded = prev.hasEverUnloaded;
        elevation = prev.elevation;
        altitude = prev.altitude;
        velocity = prev.velocity;
        velocityN = prev.velocityN;
        velocityLeft = prev.velocityLeft;
        nTurns = prev.nTurns;
        isEvading = prev.isEvading;
        nRolls = prev.nRolls;
        isRolled = prev.isRolled;
        mv = prev.mv.clone();
        freeTurn = prev.freeTurn;
        nStraight = prev.nStraight;
        nDown = prev.nDown;
        nMoved = prev.nMoved;
    }

    /**
     * Sets this state as coming from the entity.
     *
     * @param entity
     */
    public void setFromEntity(Entity entity, IGame game) {
        position = entity.getPosition();
        facing = entity.getFacing();
        // elevation
        mpUsed = entity.mpUsed;
        distance = entity.delta_distance;
        isProne = entity.isProne();
        isFlying = entity.isAirborne() || entity.isAirborneVTOL();
        isHullDown = entity.isHullDown();
        climbMode = entity.climbMode();
        thisStepBackwards = entity.inReverse;

        elevation = entity.getElevation();
        altitude = entity.getAltitude();
        movementType = entity.moved;

        isRolled = false;
        freeTurn = false;
        nStraight = 0;
        nDown = 0;

        //for some reason, doing it directly is adjusting the entity's vector itself
        //which causes problems when canceling the action
        //what a hack. but I can't figure out what is going wrong
        //this works but god is it ugly
        //TODO: figure this out
        int[] tempMv = entity.getVectors();

        mv = new int[] {0,0,0,0,0,0};
        for(int i = 0; i < 6; i++) {
            mv[i] = tempMv[i];
        }

        //if ASF get velocity
        if(entity instanceof Aero) {
            Aero a = (Aero)entity;
            velocity = a.getCurrentVelocity();
            velocityN = a.getNextVelocity();
            velocityLeft = a.getCurrentVelocity() - a.delta_distance;
            if(game.getBoard().onGround()) {
                velocityLeft = a.getCurrentVelocity() - a.delta_distance / 16;
            }
            isRolled = false;//a.isRolled();
            nStraight = a.getStraightMoves();
        }

        EntityMovementMode nMove = entity.getMovementMode();

        // tanks with stunned crew can't flank
        if ((entity instanceof Tank) && (((Tank) entity).getStunnedTurns() > 0)) {
            isRunProhibited = true;
        }

        // check pavement & water
        if (position != null) {
            IHex curHex = game.getBoard().getHex(position);
            if (curHex.hasPavement()) {
                onlyPavement = true;
                isPavementStep = true;
                // if we previously moved, and didn't get a pavement bonus, we
                // shouldn't now get one, either (this can happen when skidding
                // onto a pavement hex
                if ((entity.gotPavementBonus == false)
                        && (entity.delta_distance > 0)) {
                    onlyPavement = false;
                }
            }
            // if entity already moved into water it can't run now
            if (curHex.containsTerrain(Terrains.WATER)
                    && (entity.getElevation() < 0) && (distance > 0)
                    && (nMove != EntityMovementMode.NAVAL)
                    && (nMove != EntityMovementMode.HYDROFOIL)
                    && (nMove != EntityMovementMode.SUBMARINE)
                    && (nMove != EntityMovementMode.INF_UMU)) {
                isRunProhibited = true;
            }
        }
    }

    /**
     * Adjusts facing to comply with the type of step indicated.
     *
     * @param stepType
     */
    public void adjustFacing(MoveStepType stepType) {
        facing = MovePath.getAdjustedFacing(facing, stepType);
    }

    /**
     * For yaws, reverse the current facing
     *
     */
    public void reverseFacing() {
        facing = MovePath.getAdjustedFacing(facing, MoveStepType.TURN_RIGHT);
        facing = MovePath.getAdjustedFacing(facing, MoveStepType.TURN_RIGHT);
        facing = MovePath.getAdjustedFacing(facing, MoveStepType.TURN_RIGHT);
    }

    /**
     * Moves the position one hex in the direction indicated. Does not change
     * facing.
     *
     * @param dir
     */
    public void moveInDir(int dir) {
        position = position.translated(dir);
        if (!parent.game.getBoard().contains(position)) {
            throw new RuntimeException("Coordinate off the board.");
        }
    }

    /**
     * Adds a certain amount to the distance parameter.
     *
     * @param increment
     */
    public void addDistance(int increment) {
        distance += increment;
    }

    /**
     * Adds a certain amount to the mpUsed parameter.
     *
     * @param increment
     */
    public void addMpUsed(int increment) {
        mpUsed += increment;
    }

    /**
     * @return
     */
    public boolean isDanger() {
        return danger;
    }

    /**
     * @return
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @return
     */
    public int getLeapDistance() {
        return leapDistance;
    }

    /**
     * @return
     */
    public int getFacing() {
        return facing;
    }

    /**
     * @return
     */
    public boolean isFirstStep() {
        return firstStep;
    }

    /**
     * @return
     */
    public boolean isHasJustStood() {
        return hasJustStood;
    }

    public boolean isPavementStep() {
        return isPavementStep;
    }

    /**
     * @return
     */
    public boolean isProne() {
        return isProne;
    }

    /**
     * Returns true if the entity is flying on this step.
     *
     * @return
     */
    public boolean isFlying() {
        return isFlying;
    }

    public boolean isHullDown() {
        return isHullDown;
    }

    public boolean climbMode() {
        return climbMode;
    }

    /**
     * @return
     */
    public boolean isTurning() {
        return isTurning;
    }

    /**
     * @return
     */
    public boolean isUnloaded() {
        return isUnloaded;
    }

    /**
     * @return
     */
    public boolean isUsingMASC() {
        return isUsingMASC;
    }

    public boolean isEvading() {
        return isEvading;
    }

    public boolean isRolled() {
        return isRolled;
    }

    /**
     * Determine if this is a legal step.
     *
     * @return <code>true</code> if the step is legal. <code>false</code>
     *         otherwise.
     */
    public boolean isLegal() {
        // A step is legal if it's static movement type is not illegal,
        // and it is either a valid end position, or not an end position.
        return ((movementType != EntityMovementType.MOVE_ILLEGAL) && (isLegalEndPos() || !isEndPos));
    }

    /**
     * Return this step's movement type.
     *
     * @return the <code>int</code> constant for this step's movement type.
     */
    public EntityMovementType getMovementType() {
        EntityMovementType moveType = movementType;
        // If this step's position is the end of the path, and it is not
        // a valid end postion, then the movement type is "illegal".
        if (!isLegalEndPos() && isEndPos) {
            moveType = EntityMovementType.MOVE_ILLEGAL;
        }
        return moveType;
    }

    /**
     * Check to see if this step's position is a valid end of a path.
     *
     * @return <code>true</code> if this step's position is a legal end of a
     *         path. If the step is not legal for an end of a path, then
     *         <code>false</code> is returned.
     */
    public boolean isLegalEndPos() {
        // Can't be a stacking violation.
        boolean legal = true;
        if (isStackingViolation) {
            legal = false;
        } else if (terrainInvalid) {
            // Can't be into invalid terrain.
            legal = false;
        } else if (parent.isJumping() && (distance == 0)) {
            // Can't jump zero hexes.
            legal = false;
        } else if (hasEverUnloaded && (type != MoveStepType.UNLOAD) && (type != MoveStepType.LAUNCH) && (type != MoveStepType.DROP)) {
            // Can't be after unloading BA/inf
            legal = false;
        }
        return legal;
    }

    /**
     * Update this step's status as the ending position of a path.
     *
     * @param isEnd the <code>boolean</code> flag that specifies that this
     *            step's position is the end of a path.
     * @return <code>true</code> if the path needs to keep updating the steps.
     *         <code>false</code> if the update of the path is complete.
     * @see <code>#isLegalEndPos()</code>
     * @see <code>#isEndPos</code>
     * @see <code>MovePath#addStep( MoveStep )</code>
     */
    public boolean setEndPos(boolean isEnd) {
        // A step that is always illegal is always the end of the path.
        if (EntityMovementType.MOVE_ILLEGAL == movementType) {
            isEnd = true;
        }

        // If this step didn't already know it's status as the ending
        // position of a path, then there are more updates to do.
        boolean moreUpdates = (isEndPos != isEnd);
        isEndPos = isEnd;

        // If this step isn't the end step anymore, we might not be in danger after all
        if (parent.game.getOptions().booleanOption("psr_jump_heavy_woods")) {
            if (!isEnd
                    && parent.isJumping()
                    && parent.game.getBoard().getHex(position).containsTerrain(
                            Terrains.WOODS, 2)) {
                danger = false;
                pastDanger = false;
            }
        }

        return moreUpdates;
    }

    /**
     * @return
     */
    public int getMpUsed() {
        return mpUsed;
    }

    /**
     * @return
     */
    public boolean isOnlyPavement() {
        return onlyPavement;
    }

    /**
     * @return
     */
    public boolean isPastDanger() {
        return pastDanger;
    }

    /**
     * @return
     */
    public Coords getPosition() {
        return position;
    }

    /**
     * @return
     */
    public boolean isPrevStepOnPavement() {
        return prevStepOnPavement;
    }

    /**
     * @return
     */
    public int getTargetNumberMASC() {
        return targetNumberMASC;
    }

    /**
     * @return
     */
    public boolean isThisStepBackwards() {
        return thisStepBackwards;
    }

    /**
     * @param b
     */
    public void setDanger(boolean b) {
        danger = b;
    }

    /**
     * @param i
     */
    public void setDistance(int i) {
        distance = i;
    }

    /**
     * @param i
     */
    public void setLeapDistance(int i) {
        leapDistance = i;
    }

    /**
     * @param i
     */
    public void setFacing(int i) {
        facing = i;
    }

    /**
     * @param b
     */
    public void setFirstStep(boolean b) {
        firstStep = b;
    }

    /**
     * @param b
     */
    public void setHasJustStood(boolean b) {
        hasJustStood = b;
    }

    /**
     * @param b
     */
    public void setPavementStep(boolean b) {
        isPavementStep = b;
    }

    /**
     * @param b
     */
    public void setProne(boolean b) {
        isProne = b;
    }

    /**
     * Sets whether the entity is flying or not.
     *
     * @param b is this entity flying?
     */
    public void setFlying(boolean b) {
        isFlying = b;
    }

    public void setHullDown(boolean b) {
        isHullDown = b;
    }

    public void setClimbMode(boolean b) {
        climbMode = b;
    }

    /**
     * @param b
     */
    public void setTurning(boolean b) {
        isTurning = b;
    }

    /**
     * @param b
     */
    public void setUnloaded(boolean b) {
        isUnloaded = b;
        if (b) {
            hasEverUnloaded = true;
        }
    }

    /**
     * @param b
     */
    public void setUsingMASC(boolean b) {
        isUsingMASC = b;
    }

    /**
     * @param i
     */
    public void setMovementType(EntityMovementType i) {
        movementType = i;
    }

    public void setEvading(boolean b) {
        isEvading = b;
    }

    /**
     * @param b
     */
    public void setOnlyPavement(boolean b) {
        onlyPavement = b;
    }

    public void setTargetNumberMASC(int i) {
        targetNumberMASC = i;
    }

    public void setThisStepBackwards(boolean b) {
        thisStepBackwards = b;
    }

    /**
     * Returns the mp used for just this step.
     */
    public int getMp() {
        return mp;
    }

    /**
     * sets the mp for this step.
     *
     * @param i the mp for this step.
     */
    public void setMp(int i) {
        mp = i;
    }

    void setRunProhibited(boolean isRunProhibited) {
        this.isRunProhibited = isRunProhibited;
    }

    boolean isRunProhibited() {
        return isRunProhibited;
    }

    void setStackingViolation(boolean isStackingViolation) {
        this.isStackingViolation = isStackingViolation;
    }

    boolean isStackingViolation() {
        return isStackingViolation;
    }

    /**
     * This function checks that a step is legal. And adjust the movement type.
     *
     * @param game
     * @param entity
     * @param prev
     */
    private void compileIllegal(final IGame game, final Entity entity,
            final MoveStep prev) {
        final MoveStepType stepType = getType();
        final boolean isInfantry = entity instanceof Infantry;

        Coords curPos = getPosition();
        Coords lastPos = prev.getPosition();
        boolean isUnjammingRAC = entity.isUnjammingRAC();
        prevStepOnPavement = prev.isPavementStep();
        isTurning = prev.isTurning();
        isUnloaded = prev.isUnloaded();

        // Infantry get a first step if all they've done is spin on the spot.
        if (isInfantry && ((getMpUsed() - getMp()) == 0)) {
            setFirstStep(true);

            // getMpUsed() is the MPs used in the whole MovePath
            // getMp() is the MPs used in the last (illegal) step (this step)
            // if the difference between the whole path and this step is 0
            // then this must be their first step
        }

        // guilty until proven innocent
        movementType = EntityMovementType.MOVE_ILLEGAL;



        //AERO STUFF
        //I am going to put in a whole seperate section for Aeros and just return from it
        //only if Aeros are airborne, otherwise they should move like other units
        if (entity.isAirborne()) {

          //If airborne and not an Aero then everything is illegal, except turns
            if(!(entity instanceof Aero)) {
                switch (type) {
                case TURN_LEFT:
                case TURN_RIGHT:
                    movementType = EntityMovementType.MOVE_WALK;
                }
                return;
            }

            int tmpSafeTh = entity.getWalkMP();
            Aero a = (Aero)entity;

            //if the vessel is "immobile" due to shutdown or pilot black out then all moves are illegal
            if (a.isImmobile()) {
                return;
            }

            //can't let players do an illegal move and use that to go less than
            //velocity
            if ( !isFirstStep() && (prev.getMovementType() == EntityMovementType.MOVE_ILLEGAL)) {
                return;
            }

            //check the fuel requirements
            if(game.getOptions().booleanOption("fuel_consumption")) {
                int fuelUsed = mpUsed + Math.max(mpUsed - a.getWalkMP(), 0);
                if(fuelUsed > a.getFuel()) {
                    return;
                }
            }

            //**Space turning limits**//
            if(game.getBoard().inSpace()) {
                //if jumpships turn, they can't do anything else
                if((entity instanceof Jumpship) && !(entity instanceof Warship) && !isFirstStep()
                        && (prev.getParent().contains(MoveStepType.TURN_LEFT) || prev.getParent().contains(MoveStepType.TURN_RIGHT))) {
                    return;
                }

                //space stations can only turn
                if((entity instanceof SpaceStation) && !((type == MoveStepType.TURN_LEFT) || (type == MoveStepType.TURN_RIGHT))) {
                    return;
                }

                //unless velocity is zero ASFs must move forward one hex before making turns in space
                if (!game.useVectorMove() &&
                        (distance == 0) && (velocity != 0) &&
                        ((type == MoveStepType.TURN_LEFT) || (type == MoveStepType.TURN_RIGHT))) {
                    return;
                }

                //no more than two turns in one hex unless velocity is zero for anything except ASF in space
                if (!game.useVectorMove()
                        && (a instanceof SmallCraft) && (velocity != 0)
                        && (getNTurns() > 2)) {
                    return;
                }

                //for warships the limit is one
                if(!game.useVectorMove() &&
                        (a instanceof Jumpship) && (velocity != 0) && (getNTurns() > 1) ) {
                    return;
                }
            }


            //atmosphere has its own rules about turning
            if(useAeroAtmosphere(game, entity)
                    && ((type == MoveStepType.TURN_LEFT) || (type == MoveStepType.TURN_RIGHT))
                    && !prev.canAeroTurn(game)) {
                return;
            }

            if ((type == MoveStepType.FORWARDS) && game.getBoard().inAtmosphere()
                    && !a.isOutControl()) {
                IHex desth = game.getBoard().getHex(getPosition());
                if (altitude<=desth.ceiling()) {
                    return; //can't fly into a cliff face or woods (unless out of control)
                }
            }

            /*
             * TODO: better to disable this in movement display
            //don't let them evade more than once
            if(type == MoveStepType.EVADE ) {
                if(isEvading) {
                    return;
                } else {
                    setEvading(true);
                }
            }
             */

            //check for thruster damage
            if ((type == MoveStepType.TURN_LEFT) && (a.getRightThrustHits() > 2)
                    && !useSpheroidAtmosphere(game, entity)) {
                return;
            }
            if ((type == MoveStepType.TURN_RIGHT) && (a.getLeftThrustHits() > 2)
                    && !useSpheroidAtmosphere(game, entity)) {
                return;
            }

            //no moves after launching fighters
            if (!isFirstStep() && (prev.getType() == MoveStepType.LAUNCH)) {
                return;
            }

            //no moves after being recovered
            if (!isFirstStep() && (prev.getType() == MoveStepType.RECOVER)) {
                return;
            }

            //no moves after joining
            if (!isFirstStep() && (prev.getType() == MoveStepType.JOIN)) {
                return;
            }

            //can only use safe thrust when ammo (or bomb) dumping
            //(unless out of control?)
            boolean bDumping = false;//a.isDumpingBombs();
            for (Mounted mo : entity.getAmmo()) {
                if (mo.isDumping()) {
                    bDumping = true;
                    break;
                }
            }

            if (bDumping && (getMpUsed() > tmpSafeTh) && !a.isRandomMove()) {
                return;
            }


            //check to make sure there is velocity left to spend
            if ((getVelocityLeft() >= 0) || useSpheroidAtmosphere(game, entity)) {
                //when aeros are flying on the ground mapsheet we need an additional check
                //because velocityLeft is only decremented at intervals of 16 hexes
                if(useAeroAtmosphere(game, entity) && game.getBoard().onGround() && (getVelocityLeft() == 0)
                        && (getNMoved() > 0)) {
                    return;
                }
                if (getMpUsed() <= tmpSafeTh) {
                    movementType = EntityMovementType.MOVE_SAFE_THRUST;
                } else if(getMpUsed() <= entity.getRunMPwithoutMASC()) {
                    movementType = EntityMovementType.MOVE_OVER_THRUST;
                } else if(a.isRandomMove()) {
                    //if random move then allow it to be over thrust allowance
                    movementType = EntityMovementType.MOVE_OVER_THRUST;
                }
            }

            return;
        } // end AERO stuff


        if (prev.isDiggingIn) {
            isDiggingIn = true;
            if ((type != MoveStepType.TURN_LEFT)
                    && (type != MoveStepType.TURN_RIGHT)) {
                return; // can't move when digging in
            }
            movementType = EntityMovementType.MOVE_NONE;
        } else if ((type == MoveStepType.DIG_IN)
                || (type == MoveStepType.FORTIFY)) {
            if (!isInfantry || !isFirstStep()) {
                return; // can't dig in
            }
            Infantry inf = (Infantry) entity;
            if ((inf.getDugIn() != Infantry.DUG_IN_NONE)
                    && (inf.getDugIn() != Infantry.DUG_IN_COMPLETE)) {
                return; // already dug in
            }
            if (game.getBoard().getHex(curPos).containsTerrain(
                    Terrains.PAVEMENT) ||
                    game.getBoard().getHex(curPos).containsTerrain(
                            Terrains.FORTIFIED) ||
                            game.getBoard().getHex(curPos).containsTerrain(
                                    Terrains.BUILDING) ||
                                    game.getBoard().getHex(curPos).containsTerrain(
                                            Terrains.ROAD)) {
                // already fortified - pointless, or terrain is illegal for
                // digging in
                return;
            }
            isDiggingIn = true;
            movementType = EntityMovementType.MOVE_NONE;
        }

        // WIGEs can take off on their first step
        if (isFirstStep() && (type == MoveStepType.UP)
                && (entity.getMovementMode() == EntityMovementMode.WIGE)) {
            movementType = EntityMovementType.MOVE_WALK;
        }

        // check to see if it's trying to flee and can legally do so.
        if ((type == MoveStepType.FLEE) && entity.canFlee()) {
            movementType = EntityMovementType.MOVE_LEGAL;
        }

        if ((type == MoveStepType.CLIMB_MODE_ON)
                    || (type == MoveStepType.CLIMB_MODE_OFF)) {
                movementType = prev.movementType;
        }
        // check for ejection (always legal?)
        if (type == MoveStepType.EJECT) {
            movementType = EntityMovementType.MOVE_NONE;
        }
        if (type == MoveStepType.SEARCHLIGHT) {
            movementType = prev.movementType;
        }
        if (type == MoveStepType.UNJAM_RAC) {
            movementType = EntityMovementType.MOVE_NONE;
        }
        // infantry are allowed to clear mines
        if ((type == MoveStepType.CLEAR_MINEFIELD) && (entity instanceof Infantry)) {
            movementType = EntityMovementType.MOVE_NONE;
        }
        //check for evasion
        if (type == MoveStepType.EVADE) {
            if(entity.hasHipCrit()) {
                movementType = EntityMovementType.MOVE_ILLEGAL;
                return;
            }
            movementType = prev.movementType;
        }

        // check for valid jump mp
        if (parent.isJumping()
                && (getMpUsed() <= entity.getJumpMPWithTerrain())
                && !isProne()
                && !isHullDown()
                && !((entity instanceof Protomech) && (entity
                        .getInternal(Protomech.LOC_LEG) == IArmorState.ARMOR_DESTROYED))
                        && (!entity.isStuck() || entity.canUnstickByJumping())) {
            movementType = EntityMovementType.MOVE_JUMP;
        }

        // legged Protos may make one facing change
        if (isFirstStep()
                && (entity instanceof Protomech)
                && (entity.getInternal(Protomech.LOC_LEG) == IArmorState.ARMOR_DESTROYED)
                && ((stepType == MoveStepType.TURN_LEFT) || (stepType == MoveStepType.TURN_RIGHT))
                && !entity.isStuck()) {
            movementType = EntityMovementType.MOVE_WALK;
        }
        // Infantry that is first stepping and turning is legal
        if (isInfantry
                && ((stepType == MoveStepType.TURN_LEFT) || (stepType == MoveStepType.TURN_RIGHT))
                && isFirstStep()) {
            if (parent.isJumping()) {
                movementType = EntityMovementType.MOVE_JUMP;
            } else {
                movementType = EntityMovementType.MOVE_WALK;
            }
        }

        int tmpWalkMP = entity.getWalkMP();

        if ((parent.getEntity().getMovementMode() == EntityMovementMode.BIPED_SWIM)
                || (parent.getEntity().getMovementMode() == EntityMovementMode.QUAD_SWIM)) {
            tmpWalkMP = entity.getActiveUMUCount();
        }

        if ((parent.getEntity().getMovementMode() == EntityMovementMode.VTOL)
                && (getElevation() != 0)
                && !(parent.getEntity() instanceof VTOL)) {
            tmpWalkMP = entity.getJumpMP();
        }
        // check for valid walk/run mp
        if (!parent.isJumping() && !entity.isStuck() && (tmpWalkMP > 0)
                && (getMp() > 0)) {
            // Prone mechs can only spend MP to turn or get up
            if ((stepType != MoveStepType.TURN_LEFT)
                    && (stepType != MoveStepType.TURN_RIGHT)
                    && (stepType != MoveStepType.GET_UP)
                    && (stepType != MoveStepType.UNLOAD)
                    && (stepType != MoveStepType.LOAD)
                    && (stepType != MoveStepType.CAREFUL_STAND)
                    && (stepType != MoveStepType.HULL_DOWN)
                    && (stepType != MoveStepType.GO_PRONE)
                    && (isProne() || isHullDown())) {
                movementType = EntityMovementType.MOVE_ILLEGAL;
                return;
            }
            // WiGEs on the ground can use only 1 MP / do just one step
            if (!isFirstStep()
                    && (entity.getMovementMode() == EntityMovementMode.WIGE)
                    && (getElevation() == 0)) {
                movementType = EntityMovementType.MOVE_ILLEGAL;
                return;
            }

            if (getMpUsed() <= tmpWalkMP) {
                if ((parent.getEntity().getMovementMode() == EntityMovementMode.VTOL)
                        && (getElevation() > 0)) {
                    movementType = EntityMovementType.MOVE_VTOL_WALK;
                } else {
                    movementType = EntityMovementType.MOVE_WALK;
                    // Vehicles moving along pavement get "road bonus" of 1 MP.
                    // N.B. The Ask Precentor Martial forum said that a 4/6
                    // tank on a road can move 5/7, **not** 5/8.
                }
            } else if ((entity instanceof Tank) && !(entity instanceof VTOL)
                    && isOnlyPavement() && (getMpUsed() == tmpWalkMP + 1)) {
                // store if we got the pavement Bonus for end of phase
                // gravity psr
                movementType = EntityMovementType.MOVE_WALK;
                entity.gotPavementBonus = true;
            } else if (((getMpUsed() <= entity.getRunMPwithoutMASC()) || ((getMpUsed() <= entity
                    .getRunMP())
                    && (entity instanceof Mech) && ((Mech) entity).isMASCUsed()))
                    && !isRunProhibited()) {
                if (parent.getEntity().getMovementMode() == EntityMovementMode.VTOL) {
                    movementType = EntityMovementType.MOVE_VTOL_RUN;
                } else {
                    movementType = EntityMovementType.MOVE_RUN;
                }
            } else if ((getMpUsed() <= entity.getRunMP()) && !isRunProhibited() && !isEvading()) {
                setUsingMASC(true);
                Mech m = (Mech) entity;
                setTargetNumberMASC(m.getMASCTarget());
                movementType = EntityMovementType.MOVE_RUN;
            } else if ((entity instanceof Tank) && !(entity instanceof VTOL)
                    && isOnlyPavement()
                    && (getMpUsed() <= (entity.getRunMP() + 1))
                    && !isRunProhibited()) {
                movementType = EntityMovementType.MOVE_RUN;
                // store if we got the pavement Bonus for end of phase
                // gravity psr
                entity.gotPavementBonus = true;
            } else if (game.getOptions().booleanOption("tacops_sprint")
                    && (entity instanceof Mech)
                    && ((getMpUsed() <= entity.getSprintMPwithoutMASC())
                            || ((getMpUsed() <= entity.getSprintMP()) && ((Mech) entity).isMASCUsed()))
                            && !isRunProhibited() && !isEvading()) {
                movementType = EntityMovementType.MOVE_SPRINT;
            } else if ((getMpUsed() <= entity.getSprintMP()) && !isRunProhibited() && !isEvading()
                    && game.getOptions().booleanOption("tacops_sprint")) {
                setUsingMASC(true);
                Mech m = (Mech) entity;
                setTargetNumberMASC(m.getMASCTarget());
                movementType = EntityMovementType.MOVE_SPRINT;
            }
        }
        // 0 MP infantry units can move 1 hex
        if (isInfantry && (parent.getEntity().getWalkMP() == 0)
                && parent.getEntity().getPosition().equals(prev.getPosition())
                && (parent.getEntity().getPosition().distance(getPosition()) == 1)
                && (movementType != EntityMovementType.MOVE_JUMP)) {
            movementType = EntityMovementType.MOVE_WALK;
        }
        // Free facing changes are legal
        if (((stepType == MoveStepType.TURN_LEFT) || (stepType == MoveStepType.TURN_RIGHT))
                && (getMp() == 0)) {
            movementType = prev.movementType;
        }

        //going prone from hull down is legal and costs 0
        if((getMp() == 0) && (stepType == MoveStepType.GO_PRONE) && isHullDown()) {
            movementType = prev.movementType;
        }

        if ((movementType == EntityMovementType.MOVE_WALK)
                && (prev.movementType == EntityMovementType.MOVE_RUN)) {
            movementType = EntityMovementType.MOVE_RUN;
        } else if ((movementType == EntityMovementType.MOVE_VTOL_WALK)
                && (prev.movementType == EntityMovementType.MOVE_VTOL_RUN)) {
            movementType = EntityMovementType.MOVE_VTOL_RUN;
        } else if (((movementType == EntityMovementType.MOVE_WALK) || (movementType == EntityMovementType.MOVE_RUN))
                && (prev.movementType == EntityMovementType.MOVE_SPRINT)) {
            movementType = EntityMovementType.MOVE_SPRINT;
        }


        // Mechs with busted Gyro may make only one facing change
        if ((entity.getBadCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_GYRO,
                Mech.LOC_CT) > 1)
                && !isFirstStep()) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // Mechs with 1 MP are allowed to get up, except
        // if they've used that 1MP up already
        if ((MoveStepType.GET_UP == stepType) && (1 == entity.getRunMP())
                && (entity.mpUsed < 1) && !entity.isStuck()) {
            movementType = EntityMovementType.MOVE_RUN;
        }

        if ( (MoveStepType.CAREFUL_STAND == stepType)
                && (entity.mpUsed > 1)) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }


        if (isFirstStep() && ((stepType == MoveStepType.TAKEOFF) || (stepType == MoveStepType.VTAKEOFF))) {
            movementType = EntityMovementType.MOVE_SAFE_THRUST;
        } else

        // VTOLs with a damaged flight stabiliser can't flank
        if ((entity instanceof VTOL)
                && (movementType == EntityMovementType.MOVE_VTOL_RUN)
                && ((VTOL) entity).isStabiliserHit(VTOL.LOC_ROTOR)) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // check for UMU infantry on land
        if ((entity.getMovementMode() == EntityMovementMode.INF_UMU)
                && !game.getBoard().getHex(curPos).containsTerrain(
                        Terrains.WATER)
                        && (movementType == EntityMovementType.MOVE_RUN)) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // amnesty for the first step
        if (isFirstStep() && (movementType == EntityMovementType.MOVE_ILLEGAL)
                && (entity.getWalkMP() > 0) && !entity.isProne()
                && !entity.isHullDown() && !entity.isStuck()
                && (stepType == MoveStepType.FORWARDS)) {
            movementType = EntityMovementType.MOVE_RUN;
        }

        // Is the entity unloading passengers?
        if (stepType == MoveStepType.UNLOAD) {
            if (isFirstStep()) {
                if (getMpUsed() <= entity.getRunMP()) {
                    movementType = EntityMovementType.MOVE_RUN;
                    if (getMpUsed() <= entity.getWalkMP()) {
                        movementType = EntityMovementType.MOVE_WALK;
                    }
                }
            }

            // Prone Meks are able to unload, if they have the MP.
            if ((getMpUsed() <= entity.getRunMP())
                    && (entity.isProne() || entity.isHullDown())
                    && (movementType == EntityMovementType.MOVE_ILLEGAL)) {
                movementType = EntityMovementType.MOVE_RUN;
                if (getMpUsed() <= entity.getWalkMP()) {
                    movementType = EntityMovementType.MOVE_WALK;
                }
            }

            // Can't unload units into prohibited terrain
            // or into stacking violation.
            Targetable target = getTarget(game);
            if (target instanceof Entity) {
                Entity other = (Entity) target;
                if ((null != Compute.stackingViolation(game, other, curPos,
                        entity))
                        || other
                        .isHexProhibited(game.getBoard().getHex(curPos))) {
                    movementType = EntityMovementType.MOVE_ILLEGAL;
                }
            } else {
                movementType = EntityMovementType.MOVE_ILLEGAL;
            }
        }

        if (stepType == MoveStepType.SHAKE_OFF_SWARMERS) {
            if ((getMp() == 0) || !(entity instanceof Tank)) {
                // Can't shake off swarmers if you can't flank
                movementType = EntityMovementType.MOVE_ILLEGAL;
            } else {
                // And its always considered to be flank movement
                if (entity.getMovementMode() == EntityMovementMode.VTOL) {
                    movementType = EntityMovementType.MOVE_VTOL_RUN;
                } else {
                    movementType = EntityMovementType.MOVE_RUN;
                }
            }
        }

        // Can't run or jump if unjamming a RAC.
        if (isUnjammingRAC
                && ((movementType == EntityMovementType.MOVE_RUN) || (movementType == EntityMovementType.MOVE_SPRINT)
                        || (movementType == EntityMovementType.MOVE_VTOL_RUN) || parent
                        .isJumping())) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // only standing mechs may go prone
        if ((stepType == MoveStepType.GO_PRONE)
                && (isProne() || !(entity instanceof Mech) || entity.isStuck())) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // only standing quads may go hull down
        if (stepType == MoveStepType.HULL_DOWN) {
            if ((isHullDown()
                    || !((entity instanceof Mech) || (entity instanceof Tank))
                    || entity.isStuck())) {
                movementType = EntityMovementType.MOVE_ILLEGAL;
            }
            if ((entity instanceof Tank)
                    && !(game.getBoard().getHex(curPos)
                            .containsTerrain(Terrains.FORTIFIED))) {
                movementType = EntityMovementType.MOVE_ILLEGAL;
            }
            if (entity instanceof Mech) {
                //Mechs need to check for valid Gyros
                int gyroHits = entity.getHitCriticals(CriticalSlot.TYPE_SYSTEM,
                        Mech.SYSTEM_GYRO, Mech.LOC_CT);
                if (entity.getGyroType() != Mech.GYRO_HEAVY_DUTY) {
                    gyroHits++;
                }
                //destrotyed Gyros means that the unit can not go HD
                if (gyroHits >2) {
                    movementType = EntityMovementType.MOVE_ILLEGAL;
                }
            }
        }

        // initially prone mechs can't charge
        if (((stepType == MoveStepType.CHARGE) || (stepType == MoveStepType.DFA))
                && entity.isProne()) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // do not allow to move onto a bridge if there's no exit in lastPos's
        // direction, unless jumping
        if (!isFirstStep() && !curPos.equals(lastPos) && climbMode &&
                (movementType != EntityMovementType.MOVE_JUMP) &&
                game.getBoard().getHex(curPos).containsTerrain(Terrains.BRIDGE) &&
                !game.getBoard().getHex(curPos).containsTerrainExit(
                        Terrains.BRIDGE, curPos.direction(lastPos))) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // anyone who can and does lay mines is legal
        if ((type == MoveStepType.LAY_MINE) && entity.canLayMine()) {
            if (entity instanceof BattleArmor) {
                if (isEndPos && ((prev.movementType == EntityMovementType.MOVE_JUMP) || (prev.movementType == EntityMovementType.MOVE_VTOL_RUN) || (prev.movementType == EntityMovementType.MOVE_VTOL_WALK))) {
                    movementType = prev.movementType;
                } else if (isFirstStep()){
                    movementType = EntityMovementType.MOVE_LEGAL;
                }
            } else {
                movementType = prev.movementType;
            }
        } else if (parent.contains(MoveStepType.LAY_MINE) && (entity instanceof BattleArmor) && (parent.getStep(0).getType() != MoveStepType.LAY_MINE)) {
            movementType = EntityMovementType.MOVE_ILLEGAL;

        }

        // check if this movement is illegal for reasons other than points
        if (!isMovementPossible(game, lastPos, prev.getElevation())
                || isUnloaded) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // If the previous step is always illegal, then so is this one
        if (EntityMovementType.MOVE_ILLEGAL == prev.movementType) {
            movementType = EntityMovementType.MOVE_ILLEGAL;
        }

        // Don't compute danger if the step is illegal.
        if (movementType == EntityMovementType.MOVE_ILLEGAL) {
            return;
        }

        int prevEl = prev.getElevation();
        danger |= Compute.isPilotingSkillNeeded(game, entity.getId(), lastPos,
                curPos, movementType, isTurning, prevStepOnPavement, prevEl,
                getElevation(), getParentUpToThisStep());

        //jumping into heavy woods is danger
        if (game.getOptions().booleanOption("psr_jump_heavy_woods")) {
            if (parent.isJumping() && isEndPos && game.getBoard().getHex(curPos).containsTerrain(Terrains.WOODS, 2)) {
                danger = true;
            }
        }

        // getting up is also danger
        if (stepType == MoveStepType.GET_UP) {
            danger = true;
        }

        // set past danger
        pastDanger |= danger;

        // Record if we're turning *after* check for danger,
        // because the danger lies in moving *after* turn.
        switch (stepType) {
        case TURN_LEFT:
        case TURN_RIGHT:
            setTurning(true);
            break;
        case UNLOAD:
            // Unloading must be the last step.
            setUnloaded(true);
            break;
        default:
            setTurning(false);
        break;
        }

        // update prone state
        if (stepType == MoveStepType.GO_PRONE) {
            setProne(true);
            setHullDown(false);
        } else if (stepType == MoveStepType.GET_UP) {
            setProne(false);
            setHullDown(false);
        } else if (stepType == MoveStepType.HULL_DOWN) {
            setProne(false);
            setHullDown(true);
        }

        if ( entity.isCarefulStand() ) {
            movementType = EntityMovementType.MOVE_CAREFUL_STAND;
        }

        //only walking speed in Tornados
        if(game.getPlanetaryConditions().getWindStrength() == PlanetaryConditions.WI_TORNADO_F4) {
            if(movementType != EntityMovementType.MOVE_WALK) {
                movementType = EntityMovementType.MOVE_ILLEGAL;
                return;
            }
        }
    }

    public int getTotalHeat() {
        return totalHeat;
    }

    public int getHeat() {
        return heat;
    }

    /**
     * Amount of movement points required to move from start to dest
     */
    protected void calcMovementCostFor(IGame game, Coords prev, int prevEl) {
        final EntityMovementMode moveMode = parent.getEntity().getMovementMode();
        final IHex srcHex = game.getBoard().getHex(prev);
        final IHex destHex = game.getBoard().getHex(getPosition());
        final boolean isInfantry = parent.getEntity() instanceof Infantry;
        final boolean isMechanizedInfantry = isInfantry && ((Infantry)parent.getEntity()).isMechanized();
        final boolean isProto = parent.getEntity() instanceof Protomech;
        final boolean isMech = parent.getEntity() instanceof Mech;
        int nSrcEl = srcHex.getElevation() + prevEl;
        int nDestEl = destHex.getElevation() + elevation;

        mp = 1;
        // jumping always costs 1
        if (parent.isJumping()) {
            return;
        }

        // 0 MP infantry units can move 1 hex
        if (isInfantry && (parent.getEntity().getWalkMP() == 0)
                && parent.getEntity().getPosition().equals(prev)
                && (parent.getEntity().getPosition().distance(getPosition()) == 1)) {
            mp = 0;
            return;
        }

        //check for fog
        if((game.getPlanetaryConditions().getFog() == PlanetaryConditions.FOG_LIGHT) &&
                !game.getBoard().inSpace() && parent.isCareful()) {
            mp += 1;
        } else if((game.getPlanetaryConditions().getFog() == PlanetaryConditions.FOG_HEAVY) &&
                !game.getBoard().inSpace() && parent.isCareful()) {
            mp += 2;
        }

        //According to emails with TPTB, poor light should also increase mp costs as per
        //the table on p. 36 of TacOps
        //TODO: waiting to hear whether searchlights affect this
        if((game.getPlanetaryConditions().getLight() == PlanetaryConditions.L_FULL_MOON) &&
                !game.getBoard().inSpace() && parent.isCareful()) {
            mp += 1;
        } else if((game.getPlanetaryConditions().getLight() == PlanetaryConditions.L_MOONLESS) &&
                !game.getBoard().inSpace() && parent.isCareful()) {
            mp += 2;
        } else if((game.getPlanetaryConditions().getLight() == PlanetaryConditions.L_PITCH_BLACK) &&
                !game.getBoard().inSpace() && parent.isCareful()) {
            mp += 3;
        }

        // VTOLs pay 1 for everything
        if (moveMode == EntityMovementMode.VTOL) {
            return;
        }

        // Account for terrain, unless we're moving along a road.
        if (!isPavementStep) {

            if ((moveMode != EntityMovementMode.BIPED_SWIM)
                    && (moveMode != EntityMovementMode.QUAD_SWIM)
                    && (!((moveMode == EntityMovementMode.WIGE) && (getElevation() > 0)))) {
                mp += destHex.movementCost(moveMode);
            }

            // non-hovers, non-navals and non-VTOLs check for water depth and
            // are affected by swamp
            if ((moveMode != EntityMovementMode.HOVER)
                    && (moveMode != EntityMovementMode.NAVAL)
                    && (moveMode != EntityMovementMode.HYDROFOIL)
                    && (moveMode != EntityMovementMode.SUBMARINE)
                    && (moveMode != EntityMovementMode.INF_UMU)
                    && (moveMode != EntityMovementMode.VTOL)
                    && (moveMode != EntityMovementMode.BIPED_SWIM)
                    && (moveMode != EntityMovementMode.QUAD_SWIM)
                    && (moveMode != EntityMovementMode.WIGE)) {
                // no additional cost when moving on surface of ice.
                if (!destHex.containsTerrain(Terrains.ICE)
                        || (nDestEl < destHex.surface())) {
                    if (destHex.terrainLevel(Terrains.WATER) == 1) {
                        mp++;
                    } else if (destHex.terrainLevel(Terrains.WATER) > 1) {
                        mp += 3;
                    }
                }
                //if using non-careful movement on ice then reduce cost
                if(destHex.containsTerrain(Terrains.ICE) && !parent.isCareful()
                        && (nDestEl == destHex.surface())) {
                    mp--;
                }

            }
        } // End not-along-road

        // non-WIGEs pay for elevation differences
        if ((nSrcEl != nDestEl) && (moveMode != EntityMovementMode.WIGE)) {
            int delta_e = Math.abs(nSrcEl - nDestEl);
            if(game.getOptions().booleanOption("tacops_leaping")
                    && isMech && (delta_e > 2) && (nDestEl < nSrcEl)) {
                //leaping (moving down more than 2 hexes) always costs 4 mp regardless of anything else
                mp = 4;
                return;
            }
            // non-flying Infantry and ground vehicles are charged double.
            if ((isInfantry && !((getMovementType() == EntityMovementType.MOVE_VTOL_WALK)
                    || (getMovementType() == EntityMovementType.MOVE_VTOL_RUN)))
                    || ((moveMode == EntityMovementMode.TRACKED)
                            || (moveMode == EntityMovementMode.WHEELED)
                            || (moveMode == EntityMovementMode.HOVER))) {
                delta_e *= 2;
            }
            mp += delta_e;
        }

        // WiGEs in climb mode pay 2 extra MP to stay at the same flight level
        if ((moveMode == EntityMovementMode.WIGE) && climbMode && (elevation > 0)) {
            mp += 2;
        }

        // If we entering a building, all non-infantry pay additional MP.
        if (nDestEl < destHex.terrainLevel(Terrains.BLDG_ELEV)) {
            Building bldg = game.getBoard().getBuildingAt(getPosition());
            //check for inside hangar movement
            if((null != prev) && (null != bldg) && bldg.isIn(prev)
                    && (bldg.getBldgClass() == Building.HANGAR)
                    && (destHex.terrainLevel(Terrains.BLDG_ELEV) > parent.getEntity().height())) {
                mp += 0;
            } else if (!isInfantry) {
                if (!isProto) {
                    // non-protos pay extra according to the building type
                    mp += bldg.getType();
                    if(bldg.getBldgClass() == Building.HANGAR) {
                        mp--;
                    }
                    if(bldg.getBldgClass() == Building.FORTRESS) {
                        mp++;
                    }
                } else {
                    // protos pay one extra
                    mp += 1;
                }
            } else if (isMechanizedInfantry) {
                // mechanized infantry pays 1 extra
                mp += 1;
            }
        }

        // Infantry (except mechanized) pay 1 less MP to enter woods
        // Assumption - this doesn't apply to jungle
        if (isInfantry
                && destHex.containsTerrain(Terrains.WOODS)
                && !isMechanizedInfantry) {
            mp--;
        }
    }

    /**
     * Is movement possible from a previous position to this one? <p/> This
     * function does not comment on whether an overall movement path is
     * possible, just whether the <em>current</em> step is possible.
     */
    public boolean isMovementPossible(IGame game, Coords src, int srcEl) {
        final IHex srcHex = game.getBoard().getHex(src);
        final Coords dest = getPosition();
        final IHex destHex = game.getBoard().getHex(dest);
        final Entity entity = parent.getEntity();

        if (null == dest) {
            throw new IllegalStateException("Step has no position.");
        }
        if (src.distance(dest) > 1) {
            StringBuffer buf = new StringBuffer();
            buf.append("Coordinates ").append(src.toString()).append(" and ")
            .append(dest.toString()).append(" are not adjacent.");
            throw new IllegalArgumentException(buf.toString());
        }

        // If we're a tank and immobile, check if we try to unjam
        // or eject and the crew is not unconscious
        if ((entity instanceof Tank)
                && !entity.getCrew().isUnconscious()
                && ((type == MoveStepType.UNJAM_RAC)
                        || (type == MoveStepType.EJECT) || (type == MoveStepType.SEARCHLIGHT))) {
            return true;
        }

        // super-easy
        if (entity.isImmobile()) {
            return false;
        }

        // another easy check
        if (!game.getBoard().contains(dest)) {
            return false;
        }

        // can't enter impassable hex
        if (destHex.containsTerrain(Terrains.IMPASSABLE)) {
            return false;
        }

        Building bld = game.getBoard().getBuildingAt(dest);

        if (bld != null) {
            // protomechs that are jumping can't change the level inside a building,
            // they can only jump onto a building or out of it
            if (src.equals(dest) && (entity instanceof Protomech) &&
                    (getMovementType() == EntityMovementType.MOVE_JUMP)) {
                return false;
            }
            IHex hex = game.getBoard().getHex(getPosition());
            int maxElevation = 2 + entity.getElevation()
            + game.getBoard().getHex(entity.getPosition()).surface()
            - hex.surface();

            if ((bld.getType() == Building.WALL)
                    && (maxElevation < hex.terrainLevel(Terrains.BLDG_ELEV))) {
                return false;
            }

            //only infantry can enter an armored building
            if((elevation < hex.terrainLevel(Terrains.BLDG_ELEV)) && (bld.getArmor(dest) > 0)
                    && !(entity instanceof Infantry)) {
                return false;
            }

            //only infantry can enter a gun emplacement
            if((elevation < hex.terrainLevel(Terrains.BLDG_ELEV)) && (bld.getBldgClass() == Building.GUN_EMPLACEMENT)
                    && !(entity instanceof Infantry)) {
                return false;
            }
        }

        final int srcAlt = srcEl + srcHex.getElevation();
        final int destAlt = elevation + destHex.getElevation();

        // Can't back up across an elevation change.
        if (!(entity instanceof VTOL)
                && isThisStepBackwards()
                && (((destAlt != srcAlt) && !game.getOptions().booleanOption(
                "tacops_walk_backwards")) || (game.getOptions()
                        .booleanOption("tacops_walk_backwards") && (Math
                                .abs(destAlt - srcAlt) > 1)))) {
            return false;
        }

        // Swarming entities can't move.
        if (Entity.NONE != entity.getSwarmTargetId()) {
            return false;
        }

        // The entity is trying to load. Check for a valid move.
        if (type == MoveStepType.LOAD) {

            // Transports can't load after the first step.
            if (!firstStep) {
                return false;
            }

            // Find the unit being loaded.
            Entity other = null;
            Enumeration<Entity> entities = game.getEntities(src);
            while (entities.hasMoreElements()) {

                // Is the other unit friendly and not the current entity?
                other = entities.nextElement();
                if (!entity.getOwner().isEnemyOf(other.getOwner())
                        && !entity.equals(other)) {

                    // The moving unit should be able to load the other unit.
                    if (!entity.canLoad(other)) {
                        return false;
                    }

                    // The other unit should be able to have a turn.
                    if (!other.isLoadableThisTurn()) {
                        return false;
                    }

                    // We can stop looking.
                    break;
                }
                // Nope. Discard it.
                other = null;

            } // Check the next entity in this position.

            // We were supposed to find someone to load.
            if (other == null) {
                return false;
            }

        } // End STEP_LOAD-checks

        // mechs dumping ammo can't run
        boolean bDumping = false;
        for (Mounted mo : entity.getAmmo()) {
            if (mo.isDumping()) {
                bDumping = true;
                break;
            }
        }
        if (bDumping
                && ((movementType == EntityMovementType.MOVE_RUN)
                        || (movementType == EntityMovementType.MOVE_SPRINT)
                        || (movementType == EntityMovementType.MOVE_VTOL_RUN)
                        || (movementType == EntityMovementType.MOVE_JUMP))) {
            return false;
        }

        // check elevation difference > max
        EntityMovementMode nMove = entity.getMovementMode();

        // Make sure that if it's a VTOL unit with the VTOL MP listed as jump
        // MP...
        // That it can't jump.
        if ((movementType == EntityMovementType.MOVE_JUMP)
                && (nMove == EntityMovementMode.VTOL)) {
            return false;
        }

        if ((movementType != EntityMovementType.MOVE_JUMP)
                && (nMove != EntityMovementMode.VTOL)) {
            if (((srcAlt - destAlt > 0) && (srcAlt - destAlt > entity.getMaxElevationDown())) ||
                    ((destAlt - srcAlt > 0) && (destAlt - srcAlt > entity.getMaxElevationChange()))) {
                return false;
            }
        }

        if((entity instanceof Mech) && ((srcAlt - destAlt) > 2)) {
            setLeapDistance(srcAlt - destAlt);
        }

        // Units moving backwards may not change elevation levels.
        // (Ben thinks this rule is dumb)
        if (((type == MoveStepType.BACKWARDS)
                || (type == MoveStepType.LATERAL_LEFT_BACKWARDS) || (type == MoveStepType.LATERAL_RIGHT_BACKWARDS))
                && (destAlt != srcAlt) && !(entity instanceof VTOL)) {
            if (game.getOptions().booleanOption("tacops_walk_backwards")
                    && (Math.abs(destAlt - srcAlt) > 1)) {
                return false;
            }
            if (!game.getOptions().booleanOption("tacops_walk_backwards")
                    && (destAlt != srcAlt)) {
                return false;
            }
        }

        // WiGEs can't move backwards
        if ((type == MoveStepType.BACKWARDS) && (nMove == EntityMovementMode.WIGE)) {
            return false;
        }

        // Can't run into water unless hovering, naval, first step, using a
        // bridge, or fly.
        if (((movementType == EntityMovementType.MOVE_RUN)
                || (movementType == EntityMovementType.MOVE_SPRINT)
                || (movementType == EntityMovementType.MOVE_VTOL_RUN))
                && (nMove != EntityMovementMode.HOVER)
                && (nMove != EntityMovementMode.NAVAL)
                && (nMove != EntityMovementMode.HYDROFOIL)
                && (nMove != EntityMovementMode.SUBMARINE)
                && (nMove != EntityMovementMode.INF_UMU)
                && (nMove != EntityMovementMode.VTOL)
                && (nMove != EntityMovementMode.WIGE)
                && (destHex.terrainLevel(Terrains.WATER) > 0)
                && !(destHex.containsTerrain(Terrains.ICE) && (elevation >= 0))
                && !dest.equals(entity.getPosition())
                && !firstStep
                && !isPavementStep) {
            return false;
        }

        // ugh, stacking checks. well, maybe we're immune!
        if (!parent.isJumping() && (type != MoveStepType.CHARGE)
                && (type != MoveStepType.DFA)) {
            // can't move a mech into a hex with an enemy mech
            if ((entity instanceof Mech)
                    && Compute.isEnemyIn(game, entity, dest, true, true,
                            getElevation())) {
                return false;
            }

            // Can't move out of a hex with an enemy unit unless we started
            // there, BUT we're allowed to turn, unload, or go prone.
            if (Compute.isEnemyIn(game, entity, src, false,
                    entity instanceof Mech, getElevation())
                    && !src.equals(entity.getPosition())
                    && (type != MoveStepType.TURN_LEFT)
                    && (type != MoveStepType.TURN_RIGHT)
                    && (type != MoveStepType.UNLOAD)
                    && (type != MoveStepType.GO_PRONE)) {
                return false;
            }
        }

        // can't jump over too-high terrain
        if ((movementType == EntityMovementType.MOVE_JUMP)
                && (destAlt > (entity.getElevation()
                        + entity.game.getBoard().getHex(entity.getPosition())
                        .getElevation() + entity.getJumpMPWithTerrain() + (type == MoveStepType.DFA ? 1
                                : 0)))) {
            return false;
        }

        // Certain movement types have terrain restrictions; terrain
        // restrictions are lifted when moving along a road or bridge,
        // or when flying. Naval movement does not have the pavement
        // exemption.
        if (entity.isHexProhibited(destHex)
                && (!isPavementStep() || (nMove == EntityMovementMode.NAVAL)
                        || (nMove == EntityMovementMode.HYDROFOIL) || (nMove == EntityMovementMode.SUBMARINE))
                        && (movementType != EntityMovementType.MOVE_VTOL_WALK)
                        && (movementType != EntityMovementType.MOVE_VTOL_RUN)) {

            // We're allowed to pass *over* invalid
            // terrain, but we can't end there.
            if (parent.isJumping()) {
                terrainInvalid = true;
            } else {
                // This is an illegal move.
                return false;
            }
        }

        // Jumping into a building hex below the roof ends the move
        // assume this applies also to sylph vtol movement
        if (!(src.equals(dest))
                && (src != entity.getPosition())
                && (parent.isJumping() || (entity.getMovementMode() == EntityMovementMode.VTOL))
                && (srcEl < srcHex.terrainLevel(Terrains.BLDG_ELEV))) {
            return false;
        }

        // If we are *in* restricted terrain, we can only leave via roads.
        if ((movementType != EntityMovementType.MOVE_JUMP)
                && (movementType != EntityMovementType.MOVE_VTOL_WALK)
                && (movementType != EntityMovementType.MOVE_VTOL_RUN)
                && entity.isHexProhibited(srcHex) && !isPavementStep) {
            return false;
        }
        if (type == MoveStepType.UP) {
            if (!(entity.canGoUp(elevation - 1, getPosition()))) {
                return false;
            }
        }
        if (type == MoveStepType.DOWN) {
            if (!(entity.canGoDown(elevation + 1, getPosition()))) {
                return false;// We can't intentionally crash.
            }
        }
        if (entity instanceof VTOL) {
            if ((type == MoveStepType.BACKWARDS)
                    || (type == MoveStepType.FORWARDS)
                    || (type == MoveStepType.LATERAL_LEFT)
                    || (type == MoveStepType.LATERAL_LEFT_BACKWARDS)
                    || (type == MoveStepType.LATERAL_RIGHT)
                    || (type == MoveStepType.LATERAL_RIGHT_BACKWARDS)
                    || (type == MoveStepType.TURN_LEFT)
                    || (type == MoveStepType.TURN_RIGHT)) {
                if (elevation == 0) {// can't move on the ground.
                    return false;
                }
            }
        }
        if ((entity instanceof VTOL)
                && ((type == MoveStepType.BACKWARDS) || (type == MoveStepType.FORWARDS))) {
            if (elevation <= (destHex.ceiling() - destHex.surface())) {
                return false; // can't fly into woods or a cliff face
            }
        }

        // check the elevation is valid for the type of entity and hex
        if ((type != MoveStepType.DFA)
                && !entity.isElevationValid(elevation, destHex)) {
            if (parent.isJumping()) {
                terrainInvalid = true;
            } else {
                return false;
            }
        }

        return true;
    }

    // Used by BoardView to see if we can re-use an old movement sprite.
    public boolean canReuseSprite(MoveStep other) {
        // Assume that we *can't* reuse the sprite, and prove ourself wrong.
        boolean reuse = false;
        if ((type == other.type)
                && (facing == other.facing)
                && (mpUsed == other.mpUsed)
                && (movementType == other.movementType)
                && (isProne == other.isProne)
                && (isFlying == other.isFlying)
                && (isHullDown == other.isHullDown)
                && (danger == other.danger)
                && (pastDanger == other.pastDanger)
                && (isUsingMASC == other.isUsingMASC)
                && (targetNumberMASC == other.targetNumberMASC)
                && (isPavementStep == other.isPavementStep)
                && (elevation == other.elevation) && isLegalEndPos()
                && other.isLegalEndPos()) {
            reuse = true;
        }
        return reuse;
    }

    public int getElevation() {
        return elevation;
    }

    public int getAltitude() {
        return altitude;
    }

    public int getMineToLay() {
        return mineToLay;
    }

    public void setMineToLay(int mineId) {
        mineToLay = mineId;
    }

    public void setVelocity(int vel) {
        velocity = vel;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocityN(int vel) {
        velocityN = vel;
    }

    public int getVelocityN() {
        return velocityN;
    }

    public void setVelocityLeft(int vel) {
        velocityLeft = vel;
    }

    public int getVelocityLeft() {
        return velocityLeft;
    }

    private int asfTurnCost(IGame game, MoveStepType direction, Entity entity) {

        //jumpships (but not space stations and warships) never pay
        if((entity instanceof Jumpship) && !(entity instanceof Warship) && !(entity instanceof SpaceStation)) {
            return 0;
        }


        //if in atmosphere, the rules are different
        if(useAeroAtmosphere(game, entity)) {
            //if they have a free turn, then this move is free
            if(hasFreeTurn()) {
                return 0;
            }
            //it costs half the current velocity (rounded up)
            return (int)Math.ceil(getVelocity() / 2.0);
        } else if(useSpheroidAtmosphere(game, entity)) {
            return 0;
        }


        //first check for thruster damage
        //put illegal for more than three thruster hits in CompileIllegal
        Aero a = (Aero)entity;
        int thrustCost = 0;
        if(direction == MoveStepType.TURN_LEFT) {
            thrustCost = a.getLeftThrustHits();
        }
        if(direction == MoveStepType.TURN_RIGHT) {
            thrustCost = a.getRightThrustHits();
        }

        if(game.useVectorMove()) {
            //velocity doesn't factor into advanced movement
            return (1 + thrustCost);
        }

        //based on velocity
        if(velocity < 3) {
            return 1 + thrustCost;
        } else if ((velocity > 2) && (velocity < 6)) {
            return 2 + thrustCost;
        } else if ((velocity > 5) && (velocity < 8)) {
            return 3 + thrustCost;
        } else if ((velocity > 7) && (velocity < 10)) {
            return 4 + thrustCost;
        } else if (velocity == 10) {
            return 5 + thrustCost;
        } else if (velocity == 11) {
            return 6 + thrustCost;
        }
        return (6 + velocity - 11 + thrustCost);
    }

    public void setNTurns(int turns) {
        nTurns = turns;
    }

    public int getNTurns() {
        return nTurns;
    }

    public void setNMoved(int moved) {
        nMoved = moved;
    }

    public int getNMoved() {
        return nMoved;
    }

    public void setNRolls(int rolls) {
        nRolls = rolls;
    }

    public int getNRolls() {
        return nRolls;
    }

    public void setOffBoard(boolean b) {
        offBoard = b;
    }

    public boolean isOffBoard() {
        return offBoard;
    }

    public int[] getVectors() {
        return mv;
    }

    public void setVectors(int[] v) {
        if(v.length != 6) {
            return;
        }

        mv = v;
    }

    public boolean hasFreeTurn() {
        return freeTurn;
    }

    public void setFreeTurn(boolean b) {
        freeTurn = b;
    }

    public int getNStraight() {
        return nStraight;
    }

    public void setNStraight(int i) {
        nStraight = i;
    }

    /**
     * can this aero turn for any reason in atmosphere?
     */
    public boolean canAeroTurn(IGame game) {
        Entity en = parent.getEntity();
        if(!(en instanceof Aero)) {
            return false;
        }

        if(dueFreeTurn()) {
            return true;
        }

        //if its parf of a maneuver then you can turn
        if(isManeuver()) {
            return true;
        }

        if(en instanceof ConvFighter) {
            //conventional fighters can only turn on free turns or maneuvers
            return false;
        }

        //cant use thrust turns in the first hex of movement (or first 8 if ground)
        if(game.getBoard().onGround()) {
            //if flying on the ground map then they need to move 8 hexes first
            if(distance < 8) {
                return false;
            }
        } else if(distance == 0) {
            return false;
        }

        //must have been no prior turns in this hex (or 8 hexes if on ground)
        return getNTurns() == 0;


    }

    public boolean dueFreeTurn() {

        Entity en = parent.getEntity();
        int straight = getNStraight();
        int vel = getVelocity();
        int thresh = 99;

        //I will assume that small craft should be treated as dropships?
        if(en instanceof SmallCraft) {
            if(vel > 15) {
                thresh = 6;
            } else if (vel > 12) {
                thresh = 5;
            } else if (vel > 9) {
                thresh = 4;
            } else if (vel > 6) {
                thresh = 3;
            } else if (vel > 3) {
                thresh = 2;
            } else {
                thresh = 1;
            }
        } else if (en instanceof ConvFighter) {
            if(vel > 15) {
                thresh = 4;
            } else if (vel > 12) {
                thresh = 3;
            } else if (vel > 9) {
                thresh = 2;
            }  else {
                thresh = 1;
            }
        } else {
            if(vel > 15) {
                thresh = 5;
            } else if (vel > 12) {
                thresh = 4;
            } else if (vel > 9) {
                thresh = 3;
            } else if (vel > 6) {
                thresh = 2;
            }  else {
                thresh = 1;
            }
        }

        //different rules if flying on the ground map
        if(en.game.getBoard().onGround() && (getElevation() > 0)) {
            if(en instanceof Dropship) {
                thresh = vel * 8;
            } else if (en instanceof SmallCraft) {
                thresh = 8 + (vel - 1) * 6;
            } else {
                thresh = 8 + (vel - 1) * 4;
            }
        }

        if(straight >= thresh) {
            return true;
        }

        return false;

    }

    public void setNDown(int i) {
        nDown = i;
    }

    public int getNDown() {
        return nDown;
    }

    public int getRecoveryUnit() {
        return recoveryUnit;
    }

    public void setRecoveryUnit(int i) {
        recoveryUnit = i;
    }

    public int getManeuverType() {
        return maneuverType;
    }

    public boolean hasNoCost() {
        return noCost;
    }

    public boolean isManeuver() {
        return maneuver;
    }

    public Minefield getMinefield() {
        return mf;
    }

    /**
     * Should we treat this movement as if it is occuring for an aerodyne unit flying in atmosphere?
     */
    private boolean useAeroAtmosphere(IGame game, Entity en) {
        if(!(en instanceof Aero)) {
            return false;
        }
        if(((Aero)en).isSpheroid()) {
            return false;
        }
        //are we in space?
        if(game.getBoard().inSpace()) {
            return false;
        }
        //are we airborne in non-vacuum?
        return en.isAirborne() && !game.getPlanetaryConditions().isVacuum();
    }

    /**
     * Should we treat this movement as if it is occurring for a spheroid unit flying in atmosphere?
     * @param game
     * @param en
     * @return
     */
    private boolean useSpheroidAtmosphere(IGame game, Entity en) {
        if(!(en instanceof Aero)) {
            return false;
        }
        //are we in space?
        if(game.getBoard().inSpace()) {
            return false;
        }
        //aerodyne's will operate like spheroids in vacuum
        if(!((Aero)en).isSpheroid() && !game.getPlanetaryConditions().isVacuum()) {
            return false;
        }
        //are we in atmosphere?
        return en.isAirborne();

    }

}
