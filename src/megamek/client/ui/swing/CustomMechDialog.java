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
 * CustomMechDialog.java
 *
 * Created on March 18, 2002, 2:56 PM
 */

package megamek.client.ui.swing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import megamek.client.Client;
import megamek.client.ui.GBC;
import megamek.client.ui.Messages;
import megamek.common.Aero;
import megamek.common.AmmoType;
import megamek.common.BattleArmor;
import megamek.common.BombType;
import megamek.common.Dropship;
import megamek.common.Entity;
import megamek.common.EntitySelector;
import megamek.common.EquipmentType;
import megamek.common.FighterSquadron;
import megamek.common.GunEmplacement;
import megamek.common.IGame;
import megamek.common.Infantry;
import megamek.common.Jumpship;
import megamek.common.Mech;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.OffBoardDirection;
import megamek.common.Pilot;
import megamek.common.PlanetaryConditions;
import megamek.common.Player;
import megamek.common.Protomech;
import megamek.common.SmallCraft;
import megamek.common.Tank;
import megamek.common.TechConstants;
import megamek.common.WeaponType;
import megamek.common.options.IOption;
import megamek.common.options.IOptionGroup;
import megamek.common.options.PilotOptions;
import megamek.common.options.Quirks;
import megamek.common.options.WeaponQuirks;
import megamek.common.preference.PreferenceManager;

/**
 * A dialog that a player can use to customize his mech before battle.
 * Currently, changing pilots, setting up C3 networks, changing ammunition,
 * deploying artillery offboard, setting MGs to rapidfire, setting auto-eject is
 * supported.
 *
 * @author Ben
 */
public class CustomMechDialog extends ClientDialog implements ActionListener,
        DialogOptionListener {

    /**
     *
     */
    private static final long serialVersionUID = -6809436986445582731L;

    private JPanel panPilot;
    private JPanel panEquip;
    private JPanel panDeploy;
    private JPanel panQuirks;

    private JScrollPane scrPilot;
    private JScrollPane scrEquip;
    private JScrollPane scrDeploy;
    private JScrollPane scrQuirks;

    private JPanel panOptions;
    //private JScrollPane scrOptions;

    private JTabbedPane tabAll;

    private JLabel labName = new JLabel(Messages
            .getString("CustomMechDialog.labName"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldName = new JTextField(20);

    private JLabel labNick = new JLabel(Messages
            .getString("CustomMechDialog.labNick"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldNick = new JTextField(20);

    private JLabel labGunnery = new JLabel(Messages
            .getString("CustomMechDialog.labGunnery"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldGunnery = new JTextField(3);

    private JLabel labGunneryL = new JLabel(Messages
            .getString("CustomMechDialog.labGunneryL"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldGunneryL = new JTextField(3);

    private JLabel labGunneryM = new JLabel(Messages
            .getString("CustomMechDialog.labGunneryM"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldGunneryM = new JTextField(3);

    private JLabel labGunneryB = new JLabel(Messages
            .getString("CustomMechDialog.labGunneryB"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldGunneryB = new JTextField(3);

    private JLabel labPiloting = new JLabel(Messages
            .getString("CustomMechDialog.labPiloting"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldPiloting = new JTextField(3);

    private JLabel labArtillery = new JLabel(Messages
            .getString("CustomMechDialog.labArtillery"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldArtillery = new JTextField(3);

    private JLabel labFatigue = new JLabel(Messages
            .getString("CustomMechDialog.labFatigue"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldFatigue = new JTextField(3);

    private JLabel labTough = new JLabel(Messages
            .getString("CustomMechDialog.labTough"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldTough = new JTextField(3);


    private JLabel labInit = new JLabel(Messages
            .getString("CustomMechDialog.labInit"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldInit = new JTextField(3);

    private JLabel labCommandInit = new JLabel(Messages
            .getString("CustomMechDialog.labCommandInit"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldCommandInit = new JTextField(3);

    private JLabel labC3 = new JLabel(Messages
            .getString("CustomMechDialog.labC3"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JComboBox choC3 = new JComboBox();

    private int[] entityCorrespondance;

    private JLabel labCallsign = new JLabel(Messages
            .getString("CustomMechDialog.labCallsign"), SwingConstants.CENTER); //$NON-NLS-1$

    private JLabel labUnitNum = new JLabel(Messages
            .getString("CustomMechDialog.labUnitNum"), SwingConstants.CENTER); //$NON-NLS-1$

    private JComboBox choUnitNum = new JComboBox();

    private ArrayList<Entity> entityUnitNum = new ArrayList<Entity>();

    private JLabel labDeployment = new JLabel(Messages
            .getString("CustomMechDialog.labDeployment"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JComboBox choDeployment = new JComboBox();

    private JLabel labAutoEject = new JLabel(Messages
            .getString("CustomMechDialog.labAutoEject"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JCheckBox chAutoEject = new JCheckBox();

    private JLabel labSearchlight = new JLabel(Messages
            .getString("CustomMechDialog.labSearchlight"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JCheckBox chSearchlight = new JCheckBox();

    private JLabel labCommander = new JLabel(Messages
            .getString("CustomMechDialog.labCommander"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JCheckBox chCommander = new JCheckBox();

    private JLabel labOffBoard = new JLabel(Messages
            .getString("CustomMechDialog.labOffBoard"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JCheckBox chOffBoard = new JCheckBox();

    private JLabel labOffBoardDirection = new JLabel(
            Messages.getString("CustomMechDialog.labOffBoardDirection"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JComboBox choOffBoardDirection = new JComboBox();

    private JLabel labOffBoardDistance = new JLabel(
            Messages.getString("CustomMechDialog.labOffBoardDistance"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldOffBoardDistance = new JTextField(4);

    private JButton butOffBoardDistance = new JButton("0");

    private JLabel labStartVelocity = new JLabel(Messages
            .getString("CustomMechDialog.labStartVelocity"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldStartVelocity = new JTextField(3);

    private JLabel labStartAltitude = new JLabel(Messages
            .getString("CustomMechDialog.labStartAltitude"), SwingConstants.RIGHT); //$NON-NLS-1$

    private JTextField fldStartAltitude = new JTextField(3);

    private JPanel panButtons = new JPanel();

    private JButton butOkay = new JButton(Messages.getString("Okay")); //$NON-NLS-1$

    private JButton butCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$

    private JButton butNext = new JButton(Messages.getString("Next"));

    private JButton butPrev = new JButton(Messages.getString("Previous"));

    private ArrayList<MunitionChoicePanel> m_vMunitions = new ArrayList<MunitionChoicePanel>();

    private JPanel panMunitions = new JPanel();

    private ArrayList<RapidfireMGPanel> m_vMGs = new ArrayList<RapidfireMGPanel>();

    private JPanel panRapidfireMGs = new JPanel();

    private InfantryArmorPanel panInfArmor = new InfantryArmorPanel();

    private ArrayList<MineChoicePanel> m_vMines = new ArrayList<MineChoicePanel>();

    private JPanel panMines = new JPanel();

    private ArrayList<SantaAnnaChoicePanel> m_vSantaAnna = new ArrayList<SantaAnnaChoicePanel>();

    private JPanel panSantaAnna = new JPanel();

    private BombChoicePanel m_bombs;

    private JPanel panBombs = new JPanel();

    Entity entity;

    private boolean okay;

    ClientGUI clientgui;

    Client client;

    private PilotOptions options;
    private Quirks quirks;
    private HashMap<Integer, WeaponQuirks> h_wpnQuirks = new HashMap<Integer, WeaponQuirks>();

    private ArrayList<DialogOptionComponent> optionComps = new ArrayList<DialogOptionComponent>();
    private ArrayList<DialogOptionComponent> quirkComps = new ArrayList<DialogOptionComponent>();
    private HashMap<Integer, ArrayList<DialogOptionComponent>> h_wpnQuirkComps = new HashMap<Integer,ArrayList<DialogOptionComponent>>();

    private boolean editable;

    private OffBoardDirection direction = OffBoardDirection.NONE;

    private int distance = 17;

    private JButton butPortrait;
    PortraitChoiceDialog portraitDialog;

    /**
     * Creates new CustomMechDialog
     */
    public CustomMechDialog(ClientGUI clientgui, Client client, Entity entity,
            boolean editable) {
        super(clientgui.frame,
                Messages.getString("CustomMechDialog.title"), true); //$NON-NLS-1$

        //set up the panels
        JPanel mainPanel = new JPanel(new GridBagLayout());
        tabAll = new JTabbedPane();

        panPilot = new JPanel(new GridBagLayout());
        panEquip = new JPanel(new GridBagLayout());
        panDeploy = new JPanel(new GridBagLayout());
        panQuirks = new JPanel(new GridBagLayout());

        scrPilot = new JScrollPane(panPilot);
        scrEquip = new JScrollPane(panEquip);
        scrDeploy = new JScrollPane(panDeploy);
        scrQuirks = new JScrollPane(panQuirks);

        panOptions = new JPanel(new GridBagLayout());

        mainPanel.add(tabAll, GBC.eol().fill(GridBagConstraints.BOTH).insets(5, 5, 5, 5));
        mainPanel.add(panButtons, GBC.eol().anchor(GridBagConstraints.CENTER));

        tabAll.addTab(Messages.getString("CustomMechDialog.tabPilot"), scrPilot);
        tabAll.addTab(Messages.getString("CustomMechDialog.tabEquipment"), scrEquip);
        tabAll.addTab(Messages.getString("CustomMechDialog.tabDeployment"), scrDeploy);
        if(clientgui.getClient().game.getOptions().booleanOption("stratops_quirks")) {
            tabAll.addTab("Quirks", scrQuirks);
        }

        getContentPane().add(mainPanel);

        this.entity = entity;
        this.clientgui = clientgui;
        this.client = client;
        options = entity.getCrew().getOptions();
        quirks = entity.getQuirks();
        for (Mounted m : entity.getWeaponList()) {
            h_wpnQuirks.put(entity.getEquipmentNum(m), m.getQuirks());
        }
        this.editable = editable;

        //**PILOT TAB**/
        if (entity instanceof Tank) {
            labPiloting.setText(Messages
                    .getString("CustomMechDialog.labDriving"));
        } else if (entity instanceof Infantry) {
            labPiloting.setText(Messages
                    .getString("CustomMechDialog.labAntiMech"));
        } else {
            labPiloting.setText(Messages
                    .getString("CustomMechDialog.labPiloting"));
        }

        butPortrait = new JButton();
        butPortrait.setPreferredSize(new Dimension(72, 72));
        butPortrait.setText(Messages.getString("CustomMechDialog.labPortrait"));
        butPortrait.setActionCommand("portrait"); //$NON-NLS-1$
        butPortrait.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                portraitDialog.setVisible(true);
            }
        });
        portraitDialog = new PortraitChoiceDialog(clientgui.getFrame(), butPortrait);
        portraitDialog.setPilot(entity.crew);

        panPilot.add(butPortrait, GBC.eol());
        panPilot.add(labName, GBC.std());
        panPilot.add(fldName, GBC.eol());

        panPilot.add(labNick, GBC.std());
        panPilot.add(fldNick, GBC.eop());

        if (client.game.getOptions().booleanOption("rpg_gunnery")) {

            panPilot.add(labGunneryL, GBC.std());
            panPilot.add(fldGunneryL, GBC.eol());

            panPilot.add(labGunneryM, GBC.std());
            panPilot.add(fldGunneryM, GBC.eol());

            panPilot.add(labGunneryB, GBC.std());
            panPilot.add(fldGunneryB, GBC.eol());

        } else {
            panPilot.add(labGunnery, GBC.std());
            panPilot.add(fldGunnery, GBC.eol());
        }

        panPilot.add(labPiloting, GBC.std());
        panPilot.add(fldPiloting, GBC.eop());

        if(client.game.getOptions().booleanOption("artillery_skill")) {
            panPilot.add(labArtillery, GBC.std());
            panPilot.add(fldArtillery, GBC.eop());
        }

        if (client.game.getOptions().booleanOption("tacops_fatigue")) {
            panPilot.add(labFatigue, GBC.std());
            panPilot.add(fldFatigue, GBC.eop());
        }

        if(client.game.getOptions().booleanOption("toughness")) {
            panPilot.add(labTough, GBC.std());
            panPilot.add(fldTough, GBC.eop());
        }

        if (client.game.getOptions().booleanOption("individual_initiative")) {
            panPilot.add(labInit, GBC.std());
            panPilot.add(fldInit, GBC.eop());
        }

        if (client.game.getOptions().booleanOption("command_init")) {
            panPilot.add(labCommandInit, GBC.std());
            panPilot.add(fldCommandInit, GBC.eop());
        }

        // Set up commanders for commander killed victory condition
        if (clientgui.getClient().game.getOptions().booleanOption(
                "commander_killed")) { //$NON-NLS-1$
            panPilot.add(labCommander, GBC.std());
            panPilot.add(chCommander, GBC.eol());
            chCommander.setSelected(entity.isCommander());
        }

        if (entity instanceof Protomech) {
            // All Protomechs have a callsign.
            StringBuffer callsign = new StringBuffer(Messages
                    .getString("CustomMechDialog.Callsign")); //$NON-NLS-1$
            callsign.append(": "); //$NON-NLS-1$
            callsign.append(
                    (char) (this.entity.getUnitNumber() + PreferenceManager
                            .getClientPreferences().getUnitStartChar()))
                    .append('-').append(this.entity.getId());
            labCallsign.setText(callsign.toString());
            panPilot.add(labCallsign, GBC.eol().anchor(GridBagConstraints.CENTER));

            // Get the Protomechs of this entity's player
            // that *aren't* in the entity's unit.
            Enumeration<Entity> otherUnitEntities = client.game
                    .getSelectedEntities(new EntitySelector() {
                        private final int ownerId = CustomMechDialog.this.entity
                                .getOwnerId();

                        private final char unitNumber = CustomMechDialog.this.entity
                                .getUnitNumber();

                        public boolean accept(Entity unitEntity) {
                            if ((unitEntity instanceof Protomech)
                                    && (ownerId == unitEntity.getOwnerId())
                                    && (unitNumber != unitEntity.getUnitNumber())) {
                                return true;
                            }
                            return false;
                        }
                    });

            // If we got any other entites, show the unit number controls.
            if (otherUnitEntities.hasMoreElements()) {
                panPilot.add(labUnitNum, GBC.std());
                panPilot.add(choUnitNum, GBC.eop());
                refreshUnitNum(otherUnitEntities);
            }
        }

        if (clientgui.getClient().game.getOptions().booleanOption(
                "pilot_advantages") //$NON-NLS-1$
                || clientgui.getClient().game.getOptions().booleanOption(
                        "manei_domini")) { //$NON-NLS-1$

            panPilot.add(panOptions, GBC.eop());
        }

        //**EQUIPMENT TAB**//
        //Auto-eject checkbox.
        if (entity instanceof Mech) {
            Mech mech = (Mech) entity;
            boolean hasEjectSeat = true;
            // torso mounted cockpits don't have an ejection seat
            if (mech.getCockpitType() == Mech.COCKPIT_TORSO_MOUNTED) {
                hasEjectSeat = false;
            }
            if (mech.isIndustrial()) {
                hasEjectSeat = false;
                // industrials can only eject when they have an ejection seat
                for (Mounted misc : mech.getMisc()) {
                    if (misc.getType().hasFlag(MiscType.F_EJECTION_SEAT)) {
                        hasEjectSeat = true;
                    }
                }
            }
            if (hasEjectSeat) {
                panEquip.add(labAutoEject, GBC.std());
                panEquip.add(chAutoEject, GBC.eol());
                chAutoEject.setSelected(!mech.isAutoEject());
            }
        }

        if (entity.hasC3() || entity.hasC3i()) {
            panEquip.add(labC3, GBC.std());
            panEquip.add(choC3, GBC.eol());
            refreshC3();
        }

        //Can't set up munitions on infantry.
        if (!(entity instanceof Infantry) || (entity instanceof BattleArmor)) {
            setupMunitions();
            panEquip.add(panMunitions, GBC.eop().anchor(GridBagConstraints.CENTER));
        }

        // set up Santa Annas if using nukes
        if (((entity instanceof Dropship) || (entity instanceof Jumpship))
                && clientgui.getClient().game.getOptions().booleanOption(
                        "at2_nukes")) {
            setupSantaAnna();
            panEquip.add(panSantaAnna, GBC.eop().anchor(GridBagConstraints.CENTER));
        }

         if ((entity instanceof Aero)
                && !((entity instanceof FighterSquadron)
                        || (entity instanceof SmallCraft)
                        || (entity instanceof Jumpship))) {
            setupBombs();
            panEquip.add(panBombs, GBC.eop().anchor(GridBagConstraints.CENTER));
        }


        // Set up rapidfire mg
        if (clientgui.getClient().game.getOptions().booleanOption(
                "tacops_burst")) { //$NON-NLS-1$
            setupRapidfireMGs();
            panEquip.add(panRapidfireMGs, GBC.eop().anchor(GridBagConstraints.CENTER));
        }

        //set up infantry armor
        if((entity instanceof Infantry) && !(entity instanceof BattleArmor)) {
            panInfArmor.initialize();
            panEquip.add(panInfArmor, GBC.eop().anchor(GridBagConstraints.CENTER));
        }

        // Set up searchlight
        if (clientgui.getClient().game.getPlanetaryConditions().getLight() > PlanetaryConditions.L_DUSK) {
            panEquip.add(labSearchlight, GBC.std());
            panEquip.add(chSearchlight, GBC.eol());
            chSearchlight.setSelected(entity.hasSpotlight());
        }

        // Set up mines
        setupMines();
        panEquip.add(panMines, GBC.eop().anchor(GridBagConstraints.CENTER));

        //**DEPLOYMENT TAB**//
        boolean eligibleForOffBoard = false;
        for (Mounted mounted : entity.getWeaponList()) {
            WeaponType wtype = (WeaponType) mounted.getType();
            if (wtype.hasFlag(WeaponType.F_ARTILLERY)) {
                eligibleForOffBoard = true;
            }
        }

        if (entity instanceof Aero) {
            panDeploy.add(labStartVelocity, GBC.std());
            panDeploy.add(fldStartVelocity, GBC.eol());

            panDeploy.add(labStartAltitude, GBC.std());
            panDeploy.add(fldStartAltitude, GBC.eol());
        }

        panDeploy.add(labDeployment, GBC.std());
        panDeploy.add(choDeployment, GBC.eol());
        refreshDeployment();

        if (eligibleForOffBoard) {
            panDeploy.add(labOffBoard, GBC.std());
            panDeploy.add(chOffBoard, GBC.eol());
            chOffBoard.setSelected(entity.isOffBoard());

            panDeploy.add(labOffBoardDirection, GBC.std());

            choOffBoardDirection.addItem(Messages
                    .getString("CustomMechDialog.North")); //$NON-NLS-1$
            choOffBoardDirection.addItem(Messages
                    .getString("CustomMechDialog.South")); //$NON-NLS-1$
            choOffBoardDirection.addItem(Messages
                    .getString("CustomMechDialog.East")); //$NON-NLS-1$
            choOffBoardDirection.addItem(Messages
                    .getString("CustomMechDialog.West")); //$NON-NLS-1$
            direction = entity.getOffBoardDirection();
            if (OffBoardDirection.NONE == direction) {
                direction = OffBoardDirection.NORTH;
            }
            choOffBoardDirection.setSelectedIndex(direction.getValue());
            panDeploy.add(choOffBoardDirection, GBC.eol());

            panDeploy.add(labOffBoardDistance, GBC.std());

            butOffBoardDistance.addActionListener(this);
            butOffBoardDistance.setText(Integer.toString(distance));
            panDeploy.add(butOffBoardDistance, GBC.eol());
        }

        setupButtons();

        fldName.setText(entity.getCrew().getName());
        fldName.addActionListener(this);
        fldNick.setText(entity.getCrew().getNickname());
        fldNick.addActionListener(this);
        fldGunnery.setText(Integer.toString(entity.getCrew().getGunnery()));
        fldGunnery.addActionListener(this);
        fldGunneryL.setText(Integer.toString(entity.getCrew().getGunneryL()));
        fldGunneryL.addActionListener(this);
        fldGunneryM.setText(Integer.toString(entity.getCrew().getGunneryM()));
        fldGunneryM.addActionListener(this);
        fldGunneryB.setText(Integer.toString(entity.getCrew().getGunneryB()));
        fldGunneryB.addActionListener(this);
        fldPiloting.setText(Integer.toString(entity.getCrew().getPiloting()));
        fldPiloting.addActionListener(this);
        fldArtillery.setText(Integer.toString(entity.getCrew().getArtillery()));
        fldArtillery.addActionListener(this);
        fldFatigue.setText(Integer.toString(entity.getCrew().getFatigue()));
        fldFatigue.addActionListener(this);
        fldTough.setText(Integer.toString(entity.getCrew().getToughness()));
        fldTough.addActionListener(this);
        fldInit.setText(Integer.toString(entity.getCrew().getInitBonus()));
        fldInit.addActionListener(this);
        fldCommandInit.setText(Integer.toString(entity.getCrew()
                .getCommandBonus()));
        fldCommandInit.addActionListener(this);
        if (entity instanceof Aero) {
            Aero a = (Aero) entity;
            fldStartVelocity.setText(new Integer(a.getCurrentVelocity())
                    .toString());
            fldStartVelocity.addActionListener(this);

            fldStartAltitude.setText(new Integer(a.getAltitude()).toString());
            fldStartAltitude.addActionListener(this);
        }

        if (!editable) {
            fldName.setEnabled(false);
            fldNick.setEnabled(false);
            fldGunnery.setEnabled(false);
            fldGunneryL.setEnabled(false);
            fldGunneryM.setEnabled(false);
            fldGunneryB.setEnabled(false);
            fldPiloting.setEnabled(false);
            fldArtillery.setEnabled(false);
            fldFatigue.setEnabled(false);
            fldTough.setEnabled(false);
            fldInit.setEnabled(false);
            fldCommandInit.setEnabled(false);
            choC3.setEnabled(false);
            choDeployment.setEnabled(false);
            chAutoEject.setEnabled(false);
            chSearchlight.setEnabled(false);
            chCommander.setEnabled(false);
            m_bombs.setEnabled(false);
            disableMunitionEditing();
            disableMGSetting();
            disableMineSetting();
            panInfArmor.setEnabled(false);
            chOffBoard.setEnabled(false);
            choOffBoardDirection.setEnabled(false);
            fldOffBoardDistance.setEnabled(false);
            fldStartVelocity.setEnabled(false);
            fldStartAltitude.setEnabled(false);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        pack();
        mainPanel.setSize(mainPanel.getSize().width, Math.min(mainPanel.getSize().height, 400));
        setResizable(true);
        setLocationRelativeTo(clientgui);
    }

    private void setupButtons() {
        butOkay.addActionListener(this);
        butCancel.addActionListener(this);
        butNext.addActionListener(this);
        butPrev.addActionListener(this);

        // layout
        panButtons.setLayout(new GridLayout(1, 4, 10, 0));
        panButtons.add(butPrev);
        panButtons.add(butOkay);
        panButtons.add(butCancel);
        panButtons.add(butNext);

        butNext.setEnabled(getNextEntity(true) != null);
        butPrev.setEnabled(getNextEntity(false) != null);
    }

    private void setupRapidfireMGs() {
        GridBagLayout gbl = new GridBagLayout();
        panRapidfireMGs.setLayout(gbl);
        for (Mounted m : entity.getWeaponList()) {
            WeaponType wtype = (WeaponType) m.getType();
            if (!wtype.hasFlag(WeaponType.F_MG)) {
                continue;
            }
            RapidfireMGPanel rmp = new RapidfireMGPanel(m);
            panRapidfireMGs.add(rmp, GBC.eol());
            m_vMGs.add(rmp);
        }
    }

    private void setupMines() {
        GridBagLayout gbl = new GridBagLayout();
        panMines.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();

        int row = 0;
        for (Mounted m : entity.getMisc()) {
            if (!m.getType().hasFlag((MiscType.F_MINE))) {
                continue;
            }

            gbc.gridy = row++;
            MineChoicePanel mcp = new MineChoicePanel(m);
            gbl.setConstraints(mcp, gbc);
            panMines.add(mcp);
            m_vMines.add(mcp);
        }
    }

    private void setupSantaAnna() {
        GridBagLayout gbl = new GridBagLayout();
        panSantaAnna.setLayout(gbl);
        for (Mounted m : entity.getAmmo()) {
            AmmoType at = (AmmoType) m.getType();
            // Santa Annas?
            if (clientgui.getClient().game.getOptions().booleanOption(
                    "at2_nukes")
                    && ((at.getAmmoType() == AmmoType.T_KILLER_WHALE) || ((at
                            .getAmmoType() == AmmoType.T_AR10) && at
                            .hasFlag(AmmoType.F_AR10_KILLER_WHALE)))) {
                SantaAnnaChoicePanel sacp = new SantaAnnaChoicePanel(m);
                panSantaAnna.add(sacp, GBC.std());
                m_vSantaAnna.add(sacp);
            }
        }
    }

    private void setupBombs() {
        GridBagLayout gbl = new GridBagLayout();
        panBombs.setLayout(gbl);

        Aero a = (Aero) entity;
        m_bombs = new BombChoicePanel(a.getBombChoices(), a.getMaxBombPoints());
        panBombs.add(m_bombs, GBC.std());
    }

    private void setupMunitions() {

        GridBagLayout gbl = new GridBagLayout();
        panMunitions.setLayout(gbl);
        for (Mounted m : entity.getAmmo()) {
            AmmoType at = (AmmoType) m.getType();
            ArrayList<AmmoType> vTypes = new ArrayList<AmmoType>();
            Vector<AmmoType> vAllTypes = AmmoType.getMunitionsFor(at
                    .getAmmoType());
            if (vAllTypes == null) {
                continue;
            }

            // don't allow ammo switching of most things for Aeros
            // allow only MML, ATM, NARC, and LBX switching
            // TODO: need a better way to customize munitions on Aeros
            // currently this doesn't allow AR10 and tele-missile launchers
            // to switch back and forth between tele and regular missiles
            // also would be better to not have to add Santa Anna's in such
            // an idiosyncratic fashion
            if ((entity instanceof Aero)
                    && !((at.getAmmoType() == AmmoType.T_MML)
                            || (at.getAmmoType() == AmmoType.T_ATM)
                            || (at.getAmmoType() == AmmoType.T_NARC) || (at
                            .getAmmoType() == AmmoType.T_AC_LBX))) {
                continue;
            }

            for (int x = 0, n = vAllTypes.size(); x < n; x++) {
                AmmoType atCheck = vAllTypes.elementAt(x);
                boolean bTechMatch = TechConstants.isLegal(entity
                        .getTechLevel(), atCheck.getTechLevel());

                // allow all lvl2 IS units to use level 1 ammo
                // lvl1 IS units don't need to be allowed to use lvl1 ammo,
                // because there is no special lvl1 ammo, therefore it doesn't
                // need to show up in this display.
                if (!bTechMatch
                        && (entity.getTechLevel() == TechConstants.T_IS_TW_NON_BOX)
                        && (atCheck.getTechLevel() == TechConstants.T_INTRO_BOXSET)) {
                    bTechMatch = true;
                }

                // if is_eq_limits is unchecked allow l1 guys to use l2 stuff
                if (!clientgui.getClient().game.getOptions().booleanOption(
                        "is_eq_limits") //$NON-NLS-1$
                        && (entity.getTechLevel() == TechConstants.T_INTRO_BOXSET)
                        && (atCheck.getTechLevel() == TechConstants.T_IS_TW_NON_BOX)) {
                    bTechMatch = true;
                }

                // Possibly allow advanced/experimental ammos, possibly not.
                if (clientgui.getClient().game.getOptions().booleanOption(
                        "allow_advanced_ammo")) {
                    if (!clientgui.getClient().game.getOptions().booleanOption(
                            "is_eq_limits")) {
                        if (((entity.getTechLevel() == TechConstants.T_CLAN_TW) || (entity.getTechLevel() == TechConstants.T_CLAN_ADVANCED))
                                && ((atCheck.getTechLevel() == TechConstants.T_CLAN_ADVANCED)
                                        || (atCheck.getTechLevel() == TechConstants.T_CLAN_EXPERIMENTAL) || (atCheck
                                        .getTechLevel() == TechConstants.T_CLAN_UNOFFICIAL))) {
                            bTechMatch = true;
                        }
                        if (((entity.getTechLevel() == TechConstants.T_INTRO_BOXSET) || (entity
                                .getTechLevel() == TechConstants.T_IS_TW_NON_BOX) || (entity.getTechLevel() == TechConstants.T_IS_ADVANCED))
                                && ((atCheck.getTechLevel() == TechConstants.T_IS_ADVANCED)
                                        || (atCheck.getTechLevel() == TechConstants.T_IS_EXPERIMENTAL) || (atCheck
                                        .getTechLevel() == TechConstants.T_IS_UNOFFICIAL))) {
                            bTechMatch = true;
                        }
                    }
                } else if ((atCheck.getTechLevel() == TechConstants.T_IS_ADVANCED)
                        || (atCheck.getTechLevel() == TechConstants.T_CLAN_ADVANCED)) {
                    bTechMatch = false;
                }

                // allow mixed Tech Mechs to use both IS and Clan ammo of any
                // level (since mixed tech is always level 3)
                if (entity.isMixedTech()) {
                    bTechMatch = true;
                }

                // If clan_ignore_eq_limits is unchecked,
                // do NOT allow Clans to use IS-only ammo.
                // N.B. play bit-shifting games to allow "incendiary"
                // to be combined to other munition types.
                long muniType = atCheck.getMunitionType();
                muniType &= ~AmmoType.M_INCENDIARY_LRM;
                if (!clientgui.getClient().game.getOptions().booleanOption(
                        "clan_ignore_eq_limits") //$NON-NLS-1$
                        && entity.isClan()
                        && ((muniType == AmmoType.M_SEMIGUIDED)
                                || (muniType == AmmoType.M_SWARM_I)
                                || (muniType == AmmoType.M_FLARE)
                                || (muniType == AmmoType.M_FRAGMENTATION)
                                || (muniType == AmmoType.M_THUNDER_AUGMENTED)
                                || (muniType == AmmoType.M_THUNDER_INFERNO)
                                || (muniType == AmmoType.M_THUNDER_VIBRABOMB)
                                || (muniType == AmmoType.M_THUNDER_ACTIVE)
                                || (muniType == AmmoType.M_INFERNO_IV)
                                || (muniType == AmmoType.M_VIBRABOMB_IV)
                                || (muniType == AmmoType.M_LISTEN_KILL) || (muniType == AmmoType.M_ANTI_TSM))) {
                    bTechMatch = false;
                }

                if (!clientgui.getClient().game.getOptions().booleanOption(
                        "minefields") && //$NON-NLS-1$
                        AmmoType.canDeliverMinefield(atCheck)) {
                    continue;
                }

                // Only Protos can use Proto-specific ammo
                if (atCheck.hasFlag(AmmoType.F_PROTOMECH)
                        && !(entity instanceof Protomech)) {
                    continue;
                }

                // When dealing with machine guns, Protos can only
                // use proto-specific machine gun ammo
                if ((entity instanceof Protomech)
                        && atCheck.hasFlag(AmmoType.F_MG)
                        && !atCheck.hasFlag(AmmoType.F_PROTOMECH)) {
                    continue;
                }

                // Battle Armor ammo can't be selected at all.
                // All other ammo types need to match on rack size and tech.
                if (bTechMatch
                        && (atCheck.getRackSize() == at.getRackSize())
                        && (atCheck.hasFlag(AmmoType.F_BATTLEARMOR) == at
                                .hasFlag(AmmoType.F_BATTLEARMOR))
                        && (atCheck.hasFlag(AmmoType.F_ENCUMBERING) == at
                                .hasFlag(AmmoType.F_ENCUMBERING))
                        && (atCheck.getTonnage(entity) == at.getTonnage(entity))) {
                    vTypes.add(atCheck);
                }
            }
            if ((vTypes.size() < 2)
                    && !client.game.getOptions().booleanOption(
                            "lobby_ammo_dump")
                    && !client.game.getOptions()
                            .booleanOption("tacops_hotload")) { //$NON-NLS-1$
                continue;
            }
            // Protomechs need special choice panels.
            MunitionChoicePanel mcp;
            if (entity instanceof Protomech) {
                mcp = new ProtomechMunitionChoicePanel(m, vTypes);
            } else {
                mcp = new MunitionChoicePanel(m, vTypes);
            }
            panMunitions.add(mcp, GBC.eol());
            m_vMunitions.add(mcp);
        }
    }

    class MineChoicePanel extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = -1868675102440527538L;

        private JComboBox m_choice;

        private Mounted m_mounted;

        MineChoicePanel(Mounted m) {
            m_mounted = m;
            m_choice = new JComboBox();
            m_choice.addItem(Messages
                    .getString("CustomMechDialog.Conventional")); //$NON-NLS-1$
            m_choice.addItem(Messages.getString("CustomMechDialog.Vibrabomb")); //$NON-NLS-1$
            // m_choice.add("Messages.getString("CustomMechDialog.Command-detonated"));
            // //$NON-NLS-1$
            int loc;
            loc = m.getLocation();
            String sDesc = '(' + entity.getLocationAbbr(loc) + ')';
            JLabel lLoc = new JLabel(sDesc);
            GridBagLayout g = new GridBagLayout();
            setLayout(g);
            add(lLoc, GBC.std());
            m_choice.setSelectedIndex(m.getMineType());
            add(m_choice, GBC.eol());
        }

        public void applyChoice() {
            m_mounted.setMineType(m_choice.getSelectedIndex());
        }

        @Override
        public void setEnabled(boolean enabled) {
            m_choice.setEnabled(enabled);
        }
    }

    class MunitionChoicePanel extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = 3401106035583965326L;

        private ArrayList<AmmoType> m_vTypes;

        private JComboBox m_choice;

        private Mounted m_mounted;

        JLabel labDump = new JLabel(Messages
                .getString("CustomMechDialog.labDump")); //$NON-NLS-1$

        JCheckBox chDump = new JCheckBox();

        JLabel labHotLoad = new JLabel(Messages
                .getString("CustomMechDialog.switchToHotLoading")); //$NON-NLS-1$

        JCheckBox chHotLoad = new JCheckBox();

        MunitionChoicePanel(Mounted m, ArrayList<AmmoType> vTypes) {
            m_vTypes = vTypes;
            m_mounted = m;
            AmmoType curType = (AmmoType) m.getType();
            m_choice = new JComboBox();
            Iterator<AmmoType> e = m_vTypes.iterator();
            for (int x = 0; e.hasNext(); x++) {
                AmmoType at = e.next();
                m_choice.addItem(at.getName());
                if (at.getInternalName() == curType.getInternalName()) {
                    m_choice.setSelectedIndex(x);
                }
            }
            int loc;
            if (m.getLocation() == Entity.LOC_NONE) {
                // oneshot weapons don't have a location of their own
                Mounted linkedBy = m.getLinkedBy();
                loc = linkedBy.getLocation();
            } else {
                loc = m.getLocation();
            }
            String sDesc = '(' + entity.getLocationAbbr(loc) + ')';
            JLabel lLoc = new JLabel(sDesc);
            GridBagLayout g = new GridBagLayout();
            setLayout(g);
            add(lLoc, GBC.std());
            add(m_choice, GBC.eol());
            if (clientgui.getClient().game.getOptions().booleanOption(
                    "lobby_ammo_dump")) { //$NON-NLS-1$
                add(labDump, GBC.std());
                add(chDump, GBC.eol());
                if (clientgui.getClient().game.getOptions().booleanOption(
                        "tacops_hotload")
                        && curType.hasFlag(AmmoType.F_HOTLOAD)) {
                    add(labHotLoad, GBC.std());
                    add(chHotLoad, GBC.eol());
                }
            } else if (clientgui.getClient().game.getOptions().booleanOption(
                    "tacops_hotload")
                    && curType.hasFlag(AmmoType.F_HOTLOAD)) {
                add(labHotLoad, GBC.std());
                add(chHotLoad, GBC.eol());
            }
        }

        public void applyChoice() {
            int n = m_choice.getSelectedIndex();
            AmmoType at = m_vTypes.get(n);
            m_mounted.changeAmmoType(at);
            if (chDump.isSelected()) {
                m_mounted.setShotsLeft(0);
            }
            if (clientgui.getClient().game.getOptions().booleanOption(
                    "tacops_hotload")) {
                if (chHotLoad.isSelected() != m_mounted.isHotLoaded()) {
                    m_mounted.setHotLoad(chHotLoad.isSelected());
                }
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            m_choice.setEnabled(enabled);
        }

        /**
         * Get the number of shots in the mount.
         *
         * @return the <code>int</code> number of shots in the mount.
         */
        int getShotsLeft() {
            return m_mounted.getShotsLeft();
        }

        /**
         * Set the number of shots in the mount.
         *
         * @param shots
         *            the <code>int</code> number of shots for the mount.
         */
        void setShotsLeft(int shots) {
            m_mounted.setShotsLeft(shots);
        }
    }

    // a choice panel for determining number of santa anna warheads
    class SantaAnnaChoicePanel extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = -1645895479085898410L;

        private JComboBox m_choice;

        private Mounted m_mounted;

        public SantaAnnaChoicePanel(Mounted m) {
            m_mounted = m;
            m_choice = new JComboBox();
            for (int i = 0; i <= m_mounted.getShotsLeft(); i++) {
                m_choice.addItem(Integer.toString(i));
            }
            int loc;
            loc = m.getLocation();
            String sDesc = "Nuclear warheads for " + m_mounted.getName() + " (" + entity.getLocationAbbr(loc) + "):"; //$NON-NLS-1$ //$NON-NLS-2$
            JLabel lLoc = new JLabel(sDesc);
            GridBagLayout g = new GridBagLayout();
            setLayout(g);
            add(lLoc, GBC.std());
            m_choice.setSelectedIndex(m.getNSantaAnna());
            add(m_choice, GBC.eol());
        }

        public void applyChoice() {
            //          this is a hack. I can't immediately apply the choice, because
            //that would split this ammo bin in two and then the player could never
            //get back to it. So I keep track of the Santa Anna allocation
            //on the mounted and then apply it before deployment
            m_mounted.setNSantaAnna(m_choice.getSelectedIndex());
        }

        @Override
        public void setEnabled(boolean enabled) {
            m_choice.setEnabled(enabled);
        }
    }

    class BombChoicePanel extends JPanel implements ItemListener {
        /**
         *
         */
        private static final long serialVersionUID = 483782753790544050L;

        private JComboBox[] b_choices = new JComboBox[BombType.B_NUM];
        private JLabel[] b_labels = new JLabel[BombType.B_NUM];
        private int maxPoints = 0;
        private int maxRows = (int)Math.ceil(BombType.B_NUM / 2.0);

        public BombChoicePanel(int[] bombChoices, int maxBombPoints) {
            maxPoints = maxBombPoints;

            // how many bomb points am I currently using?
            int curBombPoints = 0;
            for (int i = 0; i < bombChoices.length; i++) {
                curBombPoints += bombChoices[i] * BombType.getBombCost(i);
            }
            int availBombPoints = maxBombPoints - curBombPoints;

            GridBagLayout g = new GridBagLayout();
            setLayout(g);
            GridBagConstraints c = new GridBagConstraints();

            int column = 0;
            int row = 0;
            for(int type = 0; type < BombType.B_NUM; type++) {

                b_labels[type] = new JLabel();
                b_choices[type] = new JComboBox();

                for (int x = 0; x <= Math.max(Math.round(availBombPoints / BombType.getBombCost(type)),
                        bombChoices[type]); x++) {
                    b_choices[type].addItem(Integer.toString(x));
                }

                b_choices[type].setSelectedIndex(bombChoices[type]);
                b_labels[type].setText(BombType.getBombName(type));
                b_choices[type].addItemListener(this);

                if((type == BombType.B_ALAMO) && !client.game.getOptions().booleanOption("at2_nukes")) {
                    b_choices[type].setEnabled(false);
                }
                if((type > BombType.B_TAG) && !client.game.getOptions().booleanOption("allow_advanced_ammo")) {
                    b_choices[type].setEnabled(false);
                }
                if((type == BombType.B_ASEW) || (type == BombType.B_ALAMO) || (type == BombType.B_TAG)) {
                    b_choices[type].setEnabled(false);
                }

                if(row >= maxRows) {
                    row = 0;
                    column += 2;
                }

                c.gridx = column;
                c.gridy = row;
                c.anchor = GridBagConstraints.EAST;
                g.setConstraints(b_labels[type], c);
                add(b_labels[type]);

                c.gridx = column + 1;
                c.gridy = row;
                c.anchor = GridBagConstraints.WEST;
                g.setConstraints(b_choices[type], c);
                add(b_choices[type]);
                row++;
            }
        }

        public void itemStateChanged(ItemEvent ie) {

            int[] current = new int[BombType.B_NUM];
            int curPoints= 0;
            for(int type = 0; type < BombType.B_NUM; type++) {
                current[type] = b_choices[type].getSelectedIndex();
                curPoints += current[type] * BombType.getBombCost(type);
            }

            int availBombPoints = maxPoints - curPoints;

            for(int type = 0; type < BombType.B_NUM; type++) {
                b_choices[type].removeItemListener(this);
                b_choices[type].removeAllItems();
                for (int x = 0; x <= Math.max(Math.round(availBombPoints / BombType.getBombCost(type)),
                        current[type]); x++) {
                    b_choices[type].addItem(Integer.toString(x));
                }
                b_choices[type].setSelectedIndex(current[type]);
                b_choices[type].addItemListener(this);
            }
        }

        public void applyChoice() {
            int[] choices = new int[BombType.B_NUM];
            for(int type = 0; type < BombType.B_NUM; type++) {
                choices[type] = b_choices[type].getSelectedIndex();
            }

            ((Aero) entity).setBombChoices(choices);

        }

        @Override
        public void setEnabled(boolean enabled) {
            for(int type = 0; type < BombType.B_NUM; type++) {
                if((type == BombType.B_ALAMO) && !client.game.getOptions().booleanOption("at2_nukes")) {
                    b_choices[type].setEnabled(false);
                }
                else if((type > BombType.B_TAG) && !client.game.getOptions().booleanOption("allow_advanced_ammo")) {
                    b_choices[type].setEnabled(false);
                }
                else if((type == BombType.B_ASEW) || (type == BombType.B_ALAMO) || (type == BombType.B_TAG)) {
                    b_choices[type].setEnabled(false);
                }
                else {
                    b_choices[type].setEnabled(enabled);
                }
            }
        }

    }

    /**
     * When a Protomech selects ammo, you need to adjust the shots on the unit
     * for the weight of the selected munition.
     */
    class ProtomechMunitionChoicePanel extends MunitionChoicePanel {
        /**
         *
         */
        private static final long serialVersionUID = -8170286698673268120L;

        private final float m_origShotsLeft;

        private final AmmoType m_origAmmo;

        ProtomechMunitionChoicePanel(Mounted m, ArrayList<AmmoType> vTypes) {
            super(m, vTypes);
            m_origAmmo = (AmmoType) m.getType();
            m_origShotsLeft = m.getShotsLeft();
        }

        /**
         * All ammo must be applied in ratios to the starting load.
         */
        @Override
        public void applyChoice() {
            super.applyChoice();

            // Calculate the number of shots for the new ammo.
            // N.B. Some special ammos are twice as heavy as normal
            // so they have half the number of shots (rounded down).
            setShotsLeft(Math.round(getShotsLeft() * m_origShotsLeft
                    / m_origAmmo.getShots()));
            if (chDump.isSelected()) {
                setShotsLeft(0);
            }
        }
    }

    class RapidfireMGPanel extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = 5261919826318225201L;

        private Mounted m_mounted;

        JCheckBox chRapid = new JCheckBox();

        RapidfireMGPanel(Mounted m) {
            m_mounted = m;
            int loc = m.getLocation();
            String sDesc = Messages
                    .getString(
                            "CustomMechDialog.switchToRapidFire", new Object[] { entity.getLocationAbbr(loc) }); //$NON-NLS-1$
            JLabel labRapid = new JLabel(sDesc);
            GridBagLayout g = new GridBagLayout();
            setLayout(g);
            add(labRapid, GBC.std().anchor(GridBagConstraints.EAST));
            chRapid.setSelected(m.isRapidfire());
            add(chRapid, GBC.eol());
        }

        public void applyChoice() {
            boolean b = chRapid.isSelected();
            m_mounted.setRapidfire(b);
        }

        @Override
        public void setEnabled(boolean enabled) {
            chRapid.setEnabled(enabled);
        }
    }

    class InfantryArmorPanel extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = -909995917737642853L;

        private Infantry inf;
        JLabel labArmor = new JLabel(Messages.getString("CustomMechDialog.labInfantryArmor"));
        JLabel labDivisor = new JLabel(Messages.getString("CustomMechDialog.labDamageDivisor"));
        JLabel labEncumber = new JLabel(Messages.getString("CustomMechDialog.labEncumber"));
        JLabel labSpaceSuit = new JLabel(Messages.getString("CustomMechDialog.labSpaceSuit"));
        JLabel labDEST = new JLabel(Messages.getString("CustomMechDialog.labDEST"));
        JLabel labSneakCamo = new JLabel(Messages.getString("CustomMechDialog.labSneakCamo"));
        JLabel labSneakIR = new JLabel(Messages.getString("CustomMechDialog.labSneakIR"));
        JLabel labSneakECM = new JLabel(Messages.getString("CustomMechDialog.labSneakECM"));
        private JTextField fldDivisor = new JTextField(3);
        JCheckBox chEncumber = new JCheckBox();
        JCheckBox chSpaceSuit = new JCheckBox();
        JCheckBox chDEST = new JCheckBox();
        JCheckBox chSneakCamo = new JCheckBox();
        JCheckBox chSneakIR = new JCheckBox();
        JCheckBox chSneakECM = new JCheckBox();

        InfantryArmorPanel() {
            GridBagLayout g = new GridBagLayout();
            setLayout(g);
            add(labArmor, GBC.eol());
            add(labDivisor, GBC.std());
            add(fldDivisor, GBC.eol());
            add(labEncumber, GBC.std());
            add(chEncumber, GBC.eol());
            add(labSpaceSuit, GBC.std());
            add(chSpaceSuit, GBC.eol());
            add(labDEST, GBC.std());
            add(chDEST, GBC.eol());
            add(labSneakCamo, GBC.std());
            add(chSneakCamo, GBC.eol());
            add(labSneakIR, GBC.std());
            add(chSneakIR, GBC.eol());
            add(labSneakECM, GBC.std());
            add(chSneakECM, GBC.eol());
        }

        public void initialize() {
            inf = (Infantry)entity;
            fldDivisor.setText(Double.toString(inf.getDamageDivisor()));
            chEncumber.setSelected(inf.isArmorEncumbering());
            chSpaceSuit.setSelected(inf.hasSpaceSuit());
            chDEST.setSelected(inf.hasDEST());
            chSneakCamo.setSelected(inf.hasSneakCamo());
            chSneakIR.setSelected(inf.hasSneakIR());
            chSneakECM.setSelected(inf.hasSneakECM());
            if(chDEST.isSelected()) {
                chSneakCamo.setEnabled(false);
                chSneakIR.setEnabled(false);
                chSneakECM.setEnabled(false);
            }
            chDEST.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent event) {
                    if(event.getStateChange() == ItemEvent.SELECTED) {
                        chSneakCamo.setSelected(false);
                        chSneakCamo.setEnabled(false);
                        chSneakIR.setSelected(false);
                        chSneakIR.setEnabled(false);
                        chSneakECM.setSelected(false);
                        chSneakECM.setEnabled(false);
                    } else if(event.getStateChange() == ItemEvent.DESELECTED) {
                        chSneakCamo.setEnabled(true);
                        chSneakIR.setEnabled(true);
                        chSneakECM.setEnabled(true);
                    }
                }
            });
        }

        public void applyChoice() {
            inf.setDamageDivisor(Double.valueOf(fldDivisor.getText()));
            inf.setArmorEncumbering(chEncumber.isSelected());
            inf.setSpaceSuit(chSpaceSuit.isSelected());
            inf.setDEST(chDEST.isSelected());
            inf.setSneakCamo(chSneakCamo.isSelected());
            inf.setSneakIR(chSneakIR.isSelected());
            inf.setSneakECM(chSneakECM.isSelected());

        }

        @Override
        public void setEnabled(boolean enabled) {
            fldDivisor.setEnabled(enabled);
            chEncumber.setEnabled(enabled);
            chSpaceSuit.setEnabled(enabled);
            chDEST.setEnabled(enabled);
            chSneakCamo.setEnabled(enabled);
            chSneakIR.setEnabled(enabled);
            chSneakECM.setEnabled(enabled);
        }
    }

    private void disableMunitionEditing() {
        for (int i = 0; i < m_vMunitions.size(); i++) {
            m_vMunitions.get(i).setEnabled(false);
        }
    }

    private void disableMGSetting() {
        for (int i = 0; i < m_vMGs.size(); i++) {
            m_vMGs.get(i).setEnabled(false);
        }
    }

    private void disableMineSetting() {
        for (int i = 0; i < m_vMines.size(); i++) {
            m_vMines.get(i).setEnabled(false);
        }
    }

    private void setOptions() {
        IOption option;
        for (final Object newVar : optionComps) {
            DialogOptionComponent comp = (DialogOptionComponent) newVar;
            option = comp.getOption();
            if ((comp.getValue() == Messages.getString("CustomMechDialog.None"))) { // NON-NLS-$1
                entity.getCrew().getOptions().getOption(option.getName())
                        .setValue("None"); // NON-NLS-$1
            } else {
                entity.getCrew().getOptions().getOption(option.getName())
                        .setValue(comp.getValue());
            }
        }
    }

    public void refreshOptions() {
        panOptions.removeAll();
        optionComps = new ArrayList<DialogOptionComponent>();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panOptions.setLayout(gridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        c.ipadx = 0;
        c.ipady = 0;

        for (Enumeration<IOptionGroup> i = options.getGroups(); i
                .hasMoreElements();) {
            IOptionGroup group = i.nextElement();

            if (group.getKey().equalsIgnoreCase(PilotOptions.LVL3_ADVANTAGES)
                    && !clientgui.getClient().game.getOptions().booleanOption(
                            "pilot_advantages")) {
                continue;
            }

            if (group.getKey().equalsIgnoreCase(PilotOptions.MD_ADVANTAGES)
                    && !clientgui.getClient().game.getOptions().booleanOption(
                            "manei_domini")) {
                continue;
            }

            addGroup(group, gridbag, c);

            for (Enumeration<IOption> j = group.getOptions(); j
                    .hasMoreElements();) {
                IOption option = j.nextElement();

                if(entity instanceof GunEmplacement) {
                    continue;
                }

                // a bunch of stuf should get disabled for conv infantry
                if ((((entity instanceof Infantry) && !(entity instanceof BattleArmor)))
                        && (option.getName().equals("vdni")
                                || option.getName().equals("bvdni"))) {
                    continue;
                }

                //a bunch of stuff should get disabled for all but conventional infantry
                if(!((entity instanceof Infantry) && !(entity instanceof BattleArmor))
                        && (option.getName().equals("grappler")
                                || option.getName().equals("pl_masc")
                                || option.getName().equals("cyber_eye_im")
                                || option.getName().equals("cyber_eye_tele"))) {
                    continue;
                }

                addOption(option, gridbag, c, editable);
            }
        }

        validate();
    }

    private void setQuirks() {
        IOption option;
        for (final Object newVar : quirkComps) {
            DialogOptionComponent comp = (DialogOptionComponent) newVar;
            option = comp.getOption();
            if ((comp.getValue() == Messages.getString("CustomMechDialog.None"))) { // NON-NLS-$1
                entity.getQuirks().getOption(option.getName())
                        .setValue("None"); // NON-NLS-$1
            } else {
                entity.getQuirks().getOption(option.getName())
                        .setValue(comp.getValue());
            }
        }
        //now for weapon quirks
        Set<Integer> set= h_wpnQuirkComps.keySet();
        Iterator<Integer> iter = set.iterator();
        while(iter.hasNext()) {
            int key = iter.next();
            Mounted m = entity.getEquipment(key);
            ArrayList<DialogOptionComponent> wpnQuirkComps = h_wpnQuirkComps.get(key);
            for (final Object newVar : wpnQuirkComps) {
                DialogOptionComponent comp = (DialogOptionComponent) newVar;
                option = comp.getOption();
                if ((comp.getValue() == Messages.getString("CustomMechDialog.None"))) { // NON-NLS-$1
                    m.getQuirks().getOption(option.getName())
                            .setValue("None"); // NON-NLS-$1
                } else {
                    m.getQuirks().getOption(option.getName())
                            .setValue(comp.getValue());
                }
            }
        }
    }

    public void refreshQuirks() {
        panQuirks.removeAll();
        quirkComps = new ArrayList<DialogOptionComponent>();
        for(Mounted m : entity.getWeaponList()) {
            h_wpnQuirkComps.put(entity.getEquipmentNum(m), new ArrayList<DialogOptionComponent>());
        }

        for (Enumeration<IOptionGroup> i = quirks.getGroups(); i
                .hasMoreElements();) {
            IOptionGroup group = i.nextElement();

            panQuirks.add(new JLabel(group.getDisplayableName()), GBC.eol());

            for (Enumeration<IOption> j = group.getOptions(); j
                    .hasMoreElements();) {
                IOption option = j.nextElement();

                if(!Quirks.isQuirkLegalFor(option, entity)) {
                    continue;
                }

                addQuirk(option, editable);
            }
        }

        //now for weapon quirks
        Set<Integer> set= h_wpnQuirks.keySet();
        Iterator<Integer> iter = set.iterator();
        while(iter.hasNext()) {
            int key = iter.next();
            Mounted m = entity.getEquipment(key);
            WeaponQuirks wpnQuirks = h_wpnQuirks.get(key);
            JLabel labWpn = new JLabel(m.getName() + " (" + entity.getLocationName(m.getLocation()) + ")");
            panQuirks.add(labWpn, GBC.eol());
            for (Enumeration<IOptionGroup> i = wpnQuirks.getGroups(); i.hasMoreElements();) {
                IOptionGroup group = i.nextElement();
                for (Enumeration<IOption> j = group.getOptions(); j.hasMoreElements();) {
                    IOption option = j.nextElement();
                    if(!WeaponQuirks.isQuirkLegalFor(option, entity, (WeaponType)m.getType())) {
                        continue;
                    }
                    addWeaponQuirk(key, option, editable);
                }
            }
        }

        validate();
    }

    private void addGroup(IOptionGroup group, GridBagLayout gridbag,
            GridBagConstraints c) {
        JLabel groupLabel = new JLabel(group.getDisplayableName());

        gridbag.setConstraints(groupLabel, c);
        panOptions.add(groupLabel);
    }

    private void addOption(IOption option, GridBagLayout gridbag,
            GridBagConstraints c, boolean editable) {
        DialogOptionComponent optionComp = new DialogOptionComponent(this,
                option, editable);

        if ("weapon_specialist".equals(option.getName())) { //$NON-NLS-1$
            optionComp.addValue(Messages.getString("CustomMechDialog.None")); //$NON-NLS-1$
            TreeSet<String> uniqueWeapons = new TreeSet<String>();
            for (int i = 0; i < entity.getWeaponList().size(); i++) {
                Mounted m = entity.getWeaponList().get(i);
                uniqueWeapons.add(m.getName());
            }
            for (String name : uniqueWeapons) {
                optionComp.addValue(name);
            }
            optionComp.setSelected(option.stringValue());
        }

        if ("specialist".equals(option.getName())) { //$NON-NLS-1$
            optionComp.addValue(Pilot.SPECIAL_NONE);
            optionComp.addValue(Pilot.SPECIAL_LASER);
            optionComp.addValue(Pilot.SPECIAL_BALLISTIC);
            optionComp.addValue(Pilot.SPECIAL_MISSILE);
            optionComp.setSelected(option.stringValue());
        }

        gridbag.setConstraints(optionComp, c);
        panOptions.add(optionComp);

        optionComps.add(optionComp);
    }

    private void addQuirk(IOption option, boolean editable) {
        DialogOptionComponent optionComp = new DialogOptionComponent(this,
                option, editable);
        panQuirks.add(optionComp, GBC.eol());

        quirkComps.add(optionComp);
    }

    private void addWeaponQuirk(int key, IOption option, boolean editable) {
        DialogOptionComponent optionComp = new DialogOptionComponent(this,
                option, editable);

        panQuirks.add(optionComp, GBC.eol());
        h_wpnQuirkComps.get(key).add(optionComp);
    }

    public void optionClicked(DialogOptionComponent comp, IOption option,
            boolean state) {
        // TODO : implement me!!!
    }

    public boolean isOkay() {
        return okay;
    }

    private void refreshDeployment() {
        choDeployment.removeAllItems();
        choDeployment.addItem(Messages
                .getString("CustomMechDialog.StartOfGame")); //$NON-NLS-1$

        if (entity.getDeployRound() < 1) {
            choDeployment.setSelectedIndex(0);
        }

        for (int i = 1; i <= 40; i++) {
            choDeployment.addItem(Messages
                    .getString("CustomMechDialog.AfterRound") + i); //$NON-NLS-1$

            if (entity.getDeployRound() == i) {
                choDeployment.setSelectedIndex(i);
            }
        }
    }

    private void refreshC3() {
        choC3.removeAllItems();
        int listIndex = 0;
        entityCorrespondance = new int[client.game.getNoOfEntities() + 2];

        if (entity.hasC3i()) {
            choC3.addItem(Messages
                    .getString("CustomMechDialog.CreateNewNetwork")); //$NON-NLS-1$
            if (entity.getC3Master() == null) {
                choC3.setSelectedIndex(listIndex);
            }
            entityCorrespondance[listIndex++] = entity.getId();
        } else if (entity.hasC3MM()) {
            int mNodes = entity.calculateFreeC3MNodes();
            int sNodes = entity.calculateFreeC3Nodes();

            choC3
                    .addItem(Messages
                            .getString(
                                    "CustomMechDialog.setCompanyMaster", new Object[] { new Integer(mNodes), new Integer(sNodes) })); //$NON-NLS-1$

            if (entity.C3MasterIs(entity)) {
                choC3.setSelectedIndex(listIndex);
            }
            entityCorrespondance[listIndex++] = entity.getId();

            choC3
                    .addItem(Messages
                            .getString(
                                    "CustomMechDialog.setIndependentMaster", new Object[] { new Integer(sNodes) })); //$NON-NLS-1$
            if (entity.getC3Master() == null) {
                choC3.setSelectedIndex(listIndex);
            }
            entityCorrespondance[listIndex++] = -1;

        } else if (entity.hasC3M()) {
            int nodes = entity.calculateFreeC3Nodes();

            choC3
                    .addItem(Messages
                            .getString(
                                    "CustomMechDialog.setCompanyMaster1", new Object[] { new Integer(nodes) })); //$NON-NLS-1$
            if (entity.C3MasterIs(entity)) {
                choC3.setSelectedIndex(listIndex);
            }
            entityCorrespondance[listIndex++] = entity.getId();

            choC3
                    .addItem(Messages
                            .getString(
                                    "CustomMechDialog.setIndependentMaster", new Object[] { new Integer(nodes) })); //$NON-NLS-1$
            if (entity.getC3Master() == null) {
                choC3.setSelectedIndex(listIndex);
            }
            entityCorrespondance[listIndex++] = -1;

        }
        for (Enumeration<Entity> i = client.getEntities(); i.hasMoreElements();) {
            final Entity e = i.nextElement();
            // ignore enemies or self
            if (entity.isEnemyOf(e) || entity.equals(e)) {
                continue;
            }
            // c3i only links with c3i
            if (entity.hasC3i() != e.hasC3i()) {
                continue;
            }
            // maximum depth of a c3 network is 2 levels.
            Entity eCompanyMaster = e.getC3Master();
            if ((eCompanyMaster != null)
                    && (eCompanyMaster.getC3Master() != eCompanyMaster)) {
                continue;
            }
            int nodes = e.calculateFreeC3Nodes();
            if (e.hasC3MM() && entity.hasC3M() && e.C3MasterIs(e)) {
                nodes = e.calculateFreeC3MNodes();
            }
            if (entity.C3MasterIs(e) && !entity.equals(e)) {
                nodes++;
            }
            if (entity.hasC3i()
                    && (entity.onSameC3NetworkAs(e) || entity.equals(e))) {
                nodes++;
            }
            if (nodes == 0) {
                continue;
            }
            if (e.hasC3i()) {
                if (entity.onSameC3NetworkAs(e)) {
                    choC3
                            .addItem(Messages
                                    .getString(
                                            "CustomMechDialog.join1", new Object[] { e.getDisplayName(), e.getC3NetId(), new Integer(nodes - 1) })); //$NON-NLS-1$
                    choC3.setSelectedIndex(listIndex);
                } else {
                    choC3
                            .addItem(Messages
                                    .getString(
                                            "CustomMechDialog.join2", new Object[] { e.getDisplayName(), e.getC3NetId(), new Integer(nodes) })); //$NON-NLS-1$
                }
                entityCorrespondance[listIndex++] = e.getId();
            } else if (e.C3MasterIs(e) && e.hasC3MM()) {
                // Company masters with 2 computers can have
                // *both* sub-masters AND slave units.
                choC3
                        .addItem(Messages
                                .getString(
                                        "CustomMechDialog.connect2", new Object[] { e.getDisplayName(), e.getC3NetId(), new Integer(nodes) })); //$NON-NLS-1$
                entityCorrespondance[listIndex] = e.getId();
                if (entity.C3MasterIs(e)) {
                    choC3.setSelectedIndex(listIndex);
                }
                listIndex++;
            } else if (e.C3MasterIs(e) != entity.hasC3M()) {
                // If we're a slave-unit, we can only connect to sub-masters,
                // not main masters likewise, if we're a master unit, we can
                // only connect to main master units, not sub-masters.
            } else if (entity.C3MasterIs(e)) {
                choC3
                        .addItem(Messages
                                .getString(
                                        "CustomMechDialog.connect1", new Object[] { e.getDisplayName(), e.getC3NetId(), new Integer(nodes - 1) })); //$NON-NLS-1$
                choC3.setSelectedIndex(listIndex);
                entityCorrespondance[listIndex++] = e.getId();
            } else {
                choC3
                        .addItem(Messages
                                .getString(
                                        "CustomMechDialog.connect2", new Object[] { e.getDisplayName(), e.getC3NetId(), new Integer(nodes) })); //$NON-NLS-1$
                entityCorrespondance[listIndex++] = e.getId();
            }
        }
    }

    /**
     * Populate the list of entities in other units from the given enumeration.
     *
     * @param others
     *            the <code>Enumeration</code> containing entities in other
     *            units.
     */
    private void refreshUnitNum(Enumeration<Entity> others) {
        // Clear the list of old values
        choUnitNum.removeAllItems();
        entityUnitNum.clear();

        // Make an entry for "no change".
        choUnitNum.addItem(Messages
                .getString("CustomMechDialog.doNotSwapUnits")); //$NON-NLS-1$
        entityUnitNum.add(entity);

        // Walk through the other entities.
        while (others.hasMoreElements()) {
            // Track the position of the next other entity.
            final Entity other = others.nextElement();
            entityUnitNum.add(other);

            // Show the other entity's name and callsign.
            StringBuffer callsign = new StringBuffer(other.getDisplayName());
            callsign
                    .append(" (")//$NON-NLS-1$
                    .append(
                            (char) (other.getUnitNumber() + PreferenceManager
                                    .getClientPreferences().getUnitStartChar()))
                    .append('-').append(other.getId()).append(')');
            choUnitNum.addItem(callsign.toString());
        }
        choUnitNum.setSelectedIndex(0);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource().equals(butOffBoardDistance)) {
            int maxDistance = 19 * 17; // Long Tom
            for (Mounted wep : entity.getWeaponList()) {
                EquipmentType e = wep.getType();
                WeaponType w = (WeaponType) e;
                if (w.hasFlag(WeaponType.F_ARTILLERY)) {
                    int nDistance = (w.getLongRange() - 1) * 17;
                    if (nDistance < maxDistance) {
                        maxDistance = nDistance;
                    }
                }
            }
            Slider sl = new Slider(
                    clientgui.frame,
                    Messages
                            .getString("CustomMechDialog.offboardDistanceTitle"),
                    Messages
                            .getString("CustomMechDialog.offboardDistanceQuestion"),
                    Math.min(Math.max(entity.getOffBoardDistance(), 17), maxDistance), 17, maxDistance);
            if (!sl.showDialog()) {
                return;
            }
            distance = sl.getValue();
            butOffBoardDistance.setText(Integer.toString(distance));
            return;
        }
        if (!actionEvent.getSource().equals(butCancel)) {
            // get values
            String name = fldName.getText();
            String nick = fldNick.getText();
            int gunnery;
            int gunneryL;
            int gunneryM;
            int gunneryB;
            int artillery;
            int fatigue = 0;
            int piloting;
            int tough = 0;
            int init = 0;
            int command = 0;
            int velocity = 0;
            int altitude = 0;
            int offBoardDistance;
            boolean autoEject = chAutoEject.isSelected();
            try {
                gunnery = Integer.parseInt(fldGunnery.getText());
                gunneryL = Integer.parseInt(fldGunneryL.getText());
                gunneryM = Integer.parseInt(fldGunneryM.getText());
                gunneryB = Integer.parseInt(fldGunneryB.getText());
                piloting = Integer.parseInt(fldPiloting.getText());
                artillery = Integer.parseInt(fldArtillery.getText());
                tough = Integer.parseInt(fldTough.getText());
                init = Integer.parseInt(fldInit.getText());
                fatigue = Integer.parseInt(fldFatigue.getText());
                command = Integer.parseInt(fldCommandInit.getText());
                if (entity instanceof Aero) {
                    velocity = Integer.parseInt(fldStartVelocity.getText());
                    altitude = Integer.parseInt(fldStartAltitude.getText());
                }
            } catch (NumberFormatException e) {
                JOptionPane
                        .showMessageDialog(
                                clientgui.frame,
                                Messages
                                        .getString("CustomMechDialog.EnterValidSkills"), Messages.getString("CustomMechDialog.NumberFormatError"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }

            // keep these reasonable, please
            if ((gunnery < 0) || (gunnery > 8) || (piloting < 0) || (piloting > 8)
                    || (gunneryL < 0) || (gunneryL > 8) || (gunneryM < 0)
                    || (gunneryM > 8) || (gunneryB < 0) || (gunneryB > 8)
                    || (artillery < 0) || (artillery > 8)) {
                JOptionPane
                        .showMessageDialog(
                                clientgui.frame,
                                Messages
                                        .getString("CustomMechDialog.EnterSkillsBetween0_8"), Messages.getString("CustomMechDialog.NumberFormatError"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }

            if (entity instanceof Aero) {
                if ((velocity > (2 * entity.getWalkMP())) || (velocity < 0)) {
                    JOptionPane
                            .showMessageDialog(
                                    clientgui.frame,
                                    Messages
                                            .getString("CustomMechDialog.EnterCorrectVelocity"), Messages.getString("CustomMechDialog.NumberFormatError"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                if ((altitude < 0) || (altitude > 10)) {
                    JOptionPane
                            .showMessageDialog(
                                    clientgui.frame,
                                    Messages
                                            .getString("CustomMechDialog.EnterCorrectAltitude"), Messages.getString("CustomMechDialog.NumberFormatError"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
            }

            if (chOffBoard.isSelected()) {
                try {
                    offBoardDistance = distance;
                } catch (NumberFormatException e) {
                    JOptionPane
                            .showMessageDialog(
                                    clientgui.frame,
                                    Messages
                                            .getString("CustomMechDialog.EnterValidSkills"), Messages.getString("CustomMechDialog.NumberFormatError"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                if (offBoardDistance < 17) {
                    JOptionPane
                            .showMessageDialog(
                                    clientgui.frame,
                                    Messages
                                            .getString("CustomMechDialog.OffboardDistance"), Messages.getString("CustomMechDialog.NumberFormatError"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                entity.setOffBoard(offBoardDistance, OffBoardDirection.getDirection(choOffBoardDirection
                        .getSelectedIndex()));
            } else {
                entity.setOffBoard(0,OffBoardDirection.NONE);
            }

            // change entity
            if (client.game.getOptions().booleanOption("rpg_gunnery")) {
                entity.setCrew(new Pilot(name, gunneryL, gunneryM, gunneryB,
                        piloting));
            } else {
                entity.setCrew(new Pilot(name, gunnery, piloting));
            }
            entity.getCrew().setArtillery(artillery);
            entity.getCrew().setFatigue(fatigue);
            entity.getCrew().setToughness(tough);
            entity.getCrew().setInitBonus(init);
            entity.getCrew().setCommandBonus(command);
            entity.getCrew().setNickname(nick);
            entity.getCrew().setPortraitCategory(portraitDialog.getCategory());
            entity.getCrew().setPortraitFileName(portraitDialog.getFileName());
            if (entity instanceof Mech) {
                Mech mech = (Mech) entity;
                mech.setAutoEject(!autoEject);
            }
            if (entity instanceof Aero) {
                Aero a = (Aero) entity;
                a.setCurrentVelocity(velocity);
                a.setNextVelocity(velocity);
                //we need to determine whether this aero is airborne or not in order for
                //prohibited terrain and stacking to work right in the deployment phase
                //this is very tricky because in atmosphere, zero altitude does not
                //necessarily mean grounded
                if(altitude <= 0) {
                    a.land();
                } else {
                    a.liftOff(altitude);
                }
            }

            // If the player wants to swap unit numbers, update both
            // entities and send an update packet for the other entity.
            if (!entityUnitNum.isEmpty() && (choUnitNum.getSelectedIndex() > 0)) {
                Entity other = entityUnitNum.get(choUnitNum.getSelectedIndex());
                char temp = entity.getUnitNumber();
                entity.setUnitNumber(other.getUnitNumber());
                other.setUnitNumber(temp);
                client.sendUpdateEntity(other);
            }

            // Set the entity's deployment round.
            // entity.setDeployRound((choDeployment.getSelectedIndex() ==
            // 0?0:choDeployment.getSelectedIndex()+1));
            entity.setDeployRound(choDeployment.getSelectedIndex());

            if((entity instanceof Infantry) && !(entity instanceof BattleArmor)) {
                panInfArmor.applyChoice();
            }

            // update munitions selections
            for (final Object newVar2 : m_vMunitions) {
                ((MunitionChoicePanel) newVar2).applyChoice();
            }
            // update MG rapid fire settings
            for (final Object newVar1 : m_vMGs) {
                ((RapidfireMGPanel) newVar1).applyChoice();
            }
            // update mines setting
            for (final Object newVar : m_vMines) {
                ((MineChoicePanel) newVar).applyChoice();
            }
            // update Santa Anna setting
            for (final Object newVar : m_vSantaAnna) {
                ((SantaAnnaChoicePanel) newVar).applyChoice();
            }
            // update bomb setting
            if (null != m_bombs) {
                m_bombs.applyChoice();
            }
            // update searchlight setting
            entity.setSpotlight(chSearchlight.isSelected());
            entity.setSpotlightState(chSearchlight.isSelected());

            // update commander status
            entity.setCommander(chCommander.isSelected());

            setOptions();
            setQuirks();
            if (entity.hasC3() && (choC3.getSelectedIndex() > -1)) {
                Entity chosen = client.getEntity(entityCorrespondance[choC3
                        .getSelectedIndex()]);
                int entC3nodeCount = client.game.getC3SubNetworkMembers(entity)
                        .size();
                int choC3nodeCount = client.game.getC3NetworkMembers(chosen)
                        .size();
                if (entC3nodeCount + choC3nodeCount <= Entity.MAX_C3_NODES) {
                    entity.setC3Master(chosen);
                } else {
                    String message = Messages
                            .getString(
                                    "CustomMechDialog.NetworkTooBig.message", new Object[] {//$NON-NLS-1$
                                    entity.getShortName(),
                                            chosen.getShortName(),
                                            new Integer(entC3nodeCount),
                                            new Integer(choC3nodeCount),
                                            new Integer(Entity.MAX_C3_NODES) });
                    clientgui.doAlertDialog(Messages
                            .getString("CustomMechDialog.NetworkTooBig.title"), //$NON-NLS-1$
                            message);
                    refreshC3();
                }
            } else if (entity.hasC3i() && (choC3.getSelectedIndex() > -1)) {
                entity.setC3NetId(client.getEntity(entityCorrespondance[choC3
                        .getSelectedIndex()]));
            }

            if(entity instanceof BattleArmor) {
                //have to reset internals because of dermal armor option
                if(entity.crew.getOptions().booleanOption("dermal_armor")) {
                    ((BattleArmor)entity).setInternal(2);
                } else {
                    ((BattleArmor)entity).setInternal(1);
                }
            } else if(entity instanceof Infantry) {
                //need to reset armor on conventional infantry
                if(entity.crew.getOptions().booleanOption("dermal_armor")) {
                    entity.initializeArmor(entity.getOInternal(Infantry.LOC_INFANTRY), Infantry.LOC_INFANTRY);
                } else {
                    entity.initializeArmor(0, Infantry.LOC_INFANTRY);
                }
            }


            okay = true;
            clientgui.chatlounge.refreshEntities();
        }
        setVisible(false);
        Entity nextOne = null;
        if (actionEvent.getSource().equals(butPrev)) {
            nextOne = getNextEntity(false);
        } else if (actionEvent.getSource().equals(butNext)) {
            nextOne = getNextEntity(true);
        }
        if (nextOne != null) {
            clientgui.chatlounge.customizeMech(nextOne);
        }
    }

    private Entity getNextEntity(boolean forward) {
        IGame game = client.game;
        boolean bd = game.getOptions().booleanOption("blind_drop"); //$NON-NLS-1$
        boolean rbd = game.getOptions().booleanOption("real_blind_drop"); //$NON-NLS-1$
        Player p = client.getLocalPlayer();

        Entity nextOne;
        if (forward) {
            nextOne = game.getNextEntityFromList(entity);
        } else {
            nextOne = game.getPreviousEntityFromList(entity);
        }
        while ((nextOne != null) && !nextOne.equals(entity)) {
            if (nextOne.getOwner().equals(p) || !(bd || rbd)) {
                return nextOne;
            }
            if (forward) {
                nextOne = game.getNextEntityFromList(nextOne);
            } else {
                nextOne = game.getPreviousEntityFromList(nextOne);
            }
        }
        return null;
    }
}
