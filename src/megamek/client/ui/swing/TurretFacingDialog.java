/**
 * MegaMek - Copyright (C) 2010 Ben Mazur (bmazur@sev.org)
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
package megamek.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import megamek.client.ui.Messages;
import megamek.common.Hex;
import megamek.common.Mech;
import megamek.common.MiscType;
import megamek.common.Mounted;

/**
 * @author beerockxs
 * 
 */
public class TurretFacingDialog extends JDialog implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = -4509638026655222982L;
    private JButton butOkay = new JButton(Messages.getString("Okay")); //$NON-NLS-1$
    private JButton butCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
    Mech mech;
    Mounted turret;
    ButtonGroup buttonGroup = new ButtonGroup();
    ClientGUI clientgui;

    ArrayList<JRadioButton> facings = new ArrayList<JRadioButton>();

    public TurretFacingDialog(JFrame parent, Mech mech, Mounted turret, ClientGUI clientgui) {
        super(parent, "Turret facing", false);
        super.setResizable(false);
        this.mech = mech;
        this.turret = turret;
        this.clientgui = clientgui;
        butOkay.addActionListener(this);
        butCancel.addActionListener(this);

        for (int i = 0; i <= 5; i++) {
            JRadioButton button = new JRadioButton();
            button.setActionCommand(i + "");
            facings.add(button);
            buttonGroup.add(button);
        }
        int turretFacing = 0;
        if (turret.getType().hasFlag(MiscType.F_SHOULDER_TURRET) || turret.getType().hasFlag(MiscType.F_QUAD_TURRET)) {
            if (turret.getLocation() == Mech.LOC_LT) {
                for (Mounted mount : mech.getEquipment()) {
                    if ((mount.getLocation() == Mech.LOC_LT) && mount.isTurretMounted()) {
                        turretFacing = mount.getFacing();
                        break;
                    }
                }
            } else if (turret.getLocation() == Mech.LOC_RT) {
                for (Mounted mount : mech.getEquipment()) {
                    if ((mount.getLocation() == Mech.LOC_RT) && mount.isTurretMounted()) {
                        turretFacing = mount.getFacing();
                        break;
                    }
                }
            }
        } else if (turret.getType().hasFlag(MiscType.F_HEAD_TURRET)) {
            for (Mounted mount : mech.getEquipment()) {
                if ((mount.getLocation() == Mech.LOC_HEAD) && mount.isTurretMounted()) {
                    turretFacing = mount.getFacing();
                    break;
                }
            }
        }
        int frontFacing = mech.getFacing();
        // select appropriate button if we already have a facing
        for (JRadioButton button : facings) {
            if (button.getActionCommand().equals((frontFacing + turretFacing) % 6 + "")) {
                button.setSelected(true);
            }
        }
        setLayout(new BorderLayout());
        JPanel tempPanel = new JPanel(new BorderLayout());
        JPanel panNorth = new JPanel(new GridBagLayout());
        JPanel panWest = new JPanel(new BorderLayout());
        JPanel panEast = new JPanel(new BorderLayout());
        JPanel panSouth = new JPanel(new GridBagLayout());
        panNorth.add(facings.get(0));
        panSouth.add(facings.get(3));
        panWest.add(facings.get(5), BorderLayout.NORTH);
        panWest.add(facings.get(4), BorderLayout.SOUTH);
        panEast.add(facings.get(1), BorderLayout.NORTH);
        panEast.add(facings.get(2), BorderLayout.SOUTH);
        // for shoulder turrets, we need to disable the appropriate facings
        // opposite of the shoulder the turret is mounted on
        if (turret.getType().hasFlag(MiscType.F_SHOULDER_TURRET)) {
            if (turret.getLocation() == Mech.LOC_LT) {
                facings.get((frontFacing + 1) % 6).setEnabled(false);
                facings.get((frontFacing + 2) % 6).setEnabled(false);
            } else if (turret.getLocation() == Mech.LOC_LT) {
                facings.get((frontFacing + 4) % 6).setEnabled(false);
                facings.get((frontFacing + 5) % 6).setEnabled(false);
            }
        }
        if (turret.isHit()) {
            for (JRadioButton button : facings) {
                button.setEnabled(false);
            }
        }
        tempPanel.add(panNorth, BorderLayout.NORTH);
        tempPanel.add(panWest, BorderLayout.WEST);

        JLabel labImage = new JLabel();
        clientgui.loadPreviewImage(labImage, mech);
        Image mechImage = ((ImageIcon) labImage.getIcon()).getImage();
        Image hexImage = ((TilesetManager) clientgui.bv.getTilesetManager()).baseFor(new Hex());
        BufferedImage toDraw = new BufferedImage(84, 72, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = toDraw.createGraphics();
        g2.drawImage(hexImage, 0, 0, null);
        g2.drawImage(mechImage, 0, 0, null);
        labImage.setIcon(new ImageIcon(toDraw));
        labImage.setHorizontalAlignment(SwingConstants.CENTER);
        tempPanel.add(labImage, BorderLayout.CENTER);
        labImage.setOpaque(false);
        tempPanel.add(labImage, BorderLayout.CENTER);
        tempPanel.add(panEast, BorderLayout.EAST);
        tempPanel.add(panSouth, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(butOkay);
        buttonPanel.add(butCancel);
        add(tempPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocation(parent.getLocation().x + parent.getSize().width / 2 - getSize().width / 2, parent.getLocation().y + parent.getSize().height / 2 - getSize().height / 2);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(butCancel)) {
            dispose();
        } else if (ae.getSource().equals(butOkay)) {
            int facing = Integer.parseInt(buttonGroup.getSelection().getActionCommand());
            int locToChange;
            turret.setFacing(facing);
            clientgui.getClient().sendMountFacingChange(mech.getId(), mech.getEquipmentNum(turret), facing);
            if (turret.getLocation() == Mech.LOC_CT) {
                locToChange = Mech.LOC_HEAD;
            } else {
                locToChange = turret.getLocation();
            }
            for (Mounted weapon : mech.getWeaponList()) {
                if ((weapon.getLocation() == locToChange) && weapon.isTurretMounted()) {
                    weapon.setFacing(facing);
                    clientgui.getClient().sendMountFacingChange(mech.getId(), mech.getEquipmentNum(weapon), facing);
                }
            }
            dispose();
        }

    }

}
