/*
 * MegaMek -
 * Copyright (C) 2000,2001,2002,2003,2004,2005,2006 Ben Mazur (bmazur@sev.org)
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

package megamek.client.ui.AWT;

import java.awt.Adjustable;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.SwingUtilities;

import megamek.client.TimerSingleton;
import megamek.client.event.BoardViewEvent;
import megamek.client.event.BoardViewListener;
import megamek.client.event.MechDisplayEvent;
import megamek.client.event.MechDisplayListener;
import megamek.client.ui.IBoardView;
import megamek.client.ui.IDisplayable;
import megamek.client.ui.Messages;
import megamek.client.ui.AWT.util.ImageCache;
import megamek.client.ui.AWT.util.ImprovedAveragingScaleFilter;
import megamek.client.ui.AWT.util.KeyAlphaFilter;
import megamek.client.ui.AWT.util.PlayerColors;
import megamek.client.ui.AWT.util.StraightArrowPolygon;
import megamek.common.Aero;
import megamek.common.Building;
import megamek.common.Compute;
import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.EntityMovementMode;
import megamek.common.EntityMovementType;
import megamek.common.GunEmplacement;
import megamek.common.IBoard;
import megamek.common.IGame;
import megamek.common.IHex;
import megamek.common.Infantry;
import megamek.common.LosEffects;
import megamek.common.Mech;
import megamek.common.Minefield;
import megamek.common.Mounted;
import megamek.common.MovePath;
import megamek.common.MoveStep;
import megamek.common.PlanetaryConditions;
import megamek.common.Player;
import megamek.common.Protomech;
import megamek.common.SpecialHexDisplay;
import megamek.common.Tank;
import megamek.common.TargetRoll;
import megamek.common.Targetable;
import megamek.common.Terrains;
import megamek.common.UnitLocation;
import megamek.common.WeaponType;
import megamek.common.MovePath.MoveStepType;
import megamek.common.actions.ArtilleryAttackAction;
import megamek.common.actions.AttackAction;
import megamek.common.actions.ChargeAttackAction;
import megamek.common.actions.ClubAttackAction;
import megamek.common.actions.DfaAttackAction;
import megamek.common.actions.EntityAction;
import megamek.common.actions.KickAttackAction;
import megamek.common.actions.PhysicalAttackAction;
import megamek.common.actions.ProtomechPhysicalAttackAction;
import megamek.common.actions.PunchAttackAction;
import megamek.common.actions.PushAttackAction;
import megamek.common.actions.SearchlightAttackAction;
import megamek.common.actions.WeaponAttackAction;
import megamek.common.event.BoardEvent;
import megamek.common.event.BoardListener;
import megamek.common.event.GameBoardChangeEvent;
import megamek.common.event.GameBoardNewEvent;
import megamek.common.event.GameEntityChangeEvent;
import megamek.common.event.GameEntityNewEvent;
import megamek.common.event.GameEntityRemoveEvent;
import megamek.common.event.GameListener;
import megamek.common.event.GameListenerAdapter;
import megamek.common.event.GameNewActionEvent;
import megamek.common.event.GamePhaseChangeEvent;
import megamek.common.options.PilotOptions;
import megamek.common.preference.IClientPreferences;
import megamek.common.preference.IPreferenceChangeListener;
import megamek.common.preference.PreferenceChangeEvent;
import megamek.common.preference.PreferenceManager;

/**
 * Displays the board; lets the user scroll around and select points on it.
 */
public class BoardView1 extends Canvas implements IBoardView, BoardListener,
        MouseListener, MouseMotionListener, KeyListener, AdjustmentListener,
        MechDisplayListener, IPreferenceChangeListener {
    private static final long serialVersionUID = -7518808415074725538L;

    private static final int TRANSPARENT = 0xFFFF00FF;

    private static final int BOARD_HEX_CLICK = 1;
    private static final int BOARD_HEX_DOUBLECLICK = 2;
    private static final int BOARD_HEX_DRAG = 3;
    private static final int BOARD_HEX_POPUP = 4;

    // the dimensions of megamek's hex images
    private static final int HEX_W = 84;
    private static final int HEX_H = 72;
    private static final int HEX_WC = HEX_W - HEX_W / 4;

    // The list of valid zoom factors. Other values cause map aliasing,
    // I can't be bothered figuring out why. - Ben
    private static final float[] ZOOM_FACTORS = { 0.30f, 0.41f, 0.50f, 0.60f,
        0.68f, 0.79f, 0.90f, 1.00f
        // 1.09f, 1.17f
    };

    // the index of zoom factor 1.00f
    private static final int BASE_ZOOM_INDEX = 7;

    // line width of the c3 network lines
    private static final int C3_LINE_WIDTH = 1;

    private static Font FONT_7 = new Font("SansSerif", Font.PLAIN, 7); //$NON-NLS-1$
    private static Font FONT_8 = new Font("SansSerif", Font.PLAIN, 8); //$NON-NLS-1$
    private static Font FONT_9 = new Font("SansSerif", Font.PLAIN, 9); //$NON-NLS-1$
    private static Font FONT_10 = new Font("SansSerif", Font.PLAIN, 10); //$NON-NLS-1$
    private static Font FONT_12 = new Font("SansSerif", Font.PLAIN, 12); //$NON-NLS-1$

    Dimension hex_size = null;
    private Font       font_note   = FONT_10;

    private Font font_hexnum = FONT_10;
    private Font font_elev = FONT_9;
    private Font font_minefield = FONT_12;

    IGame game;

    private Point mousePos = new Point();
    Rectangle view = new Rectangle();
    Point offset = new Point();
    private Dimension boardSize;

    // scrolly stuff:
    private Scrollbar vScrollbar = null;
    private Scrollbar hScrollbar = null;
    private Panel scroller;
    private boolean isScrolling = false;
    private Point scroll = new Point();
    private boolean initCtlScroll;
    private boolean ctlKeyHeld = false;
    private int previousMouseX;
    private int previousMouseY;

    // back buffer to draw to
    private Image backImage;
    Dimension backSize;
    private Graphics backGraph;

    // buffer for all the hexes you can possibly see
    private Image boardImage;
    private Rectangle boardRect;
    private Graphics boardGraph;

    // entity sprites
    private ArrayList<EntitySprite> entitySprites = new ArrayList<EntitySprite>();
    private HashMap<Integer, EntitySprite> entitySpriteIds = new HashMap<Integer, EntitySprite>();

    // sprites for the three selection cursors
    private CursorSprite cursorSprite;
    private CursorSprite highlightSprite;
    private CursorSprite selectedSprite;
    private CursorSprite firstLOSSprite;
    private CursorSprite secondLOSSprite;

    // sprite for current movement
    private ArrayList<StepSprite> pathSprites = new ArrayList<StepSprite>();

    // vector of sprites for all firing lines
    ArrayList<AttackSprite> attackSprites = new ArrayList<AttackSprite>();

    //vector of sprites for all movement paths (using vectored movement)
    private ArrayList<MovementSprite> movementSprites = new ArrayList<MovementSprite>();

    // vector of sprites for C3 network lines
    private ArrayList<C3Sprite> C3Sprites = new ArrayList<C3Sprite>();

    // tooltip stuff
    private Window tipWindow;
    private boolean isTipPossible = false;
    private long lastIdle;

    TilesetManager tileManager = null;

    // polygons for a few things
    Polygon hexPoly;
    Polygon[] facingPolys;
    Polygon[] movementPolys;

    // the player who owns this BoardView's client
    private Player localPlayer = null;

    // should we mark deployment hexes for an entity?
    private Entity en_Deployer = null;

    // should be able to turn it off(board editor)
    private boolean useLOSTool = true;

    // Initial scale factor for sprites and map
    public int zoomIndex;
    float scale;
    private ImageCache<Image, Image> scaledImageCache = new ImageCache<Image, Image>();

    // Displayables (Chat box, etc.)
    ArrayList<IDisplayable> displayables = new ArrayList<IDisplayable>();

    // Move units step by step
    private ArrayList<MovingUnit> movingUnits = new ArrayList<MovingUnit>();
    private long moveWait = 0;

    // moving entity sprites
    private ArrayList<MovingEntitySprite> movingEntitySprites = new ArrayList<MovingEntitySprite>();
    private HashMap<Integer, MovingEntitySprite> movingEntitySpriteIds = new HashMap<Integer, MovingEntitySprite>();
    private ArrayList<GhostEntitySprite> ghostEntitySprites = new ArrayList<GhostEntitySprite>();
    protected transient ArrayList<BoardViewListener> boardListeners = new ArrayList<BoardViewListener>();

    // wreck sprites
    private ArrayList<WreckSprite> wreckSprites = new ArrayList<WreckSprite>();

    private Coords rulerStart; // added by kenn
    private Coords rulerEnd; // added by kenn
    private Color rulerStartColor; // added by kenn
    private Color rulerEndColor; // added by kenn

    // Position of the mouse before right mouse button was pressed. Used to have
    // an anchor for scrolling
    private Point oldMousePosition = null;

    // Indicate that a scrolling took place, so no popup should be drawn on
    // right mouse button release
    private boolean scrolled = false;

    private Coords lastCursor;
    private Coords highlighted;
    private Coords selected;
    private Coords firstLOS;

    // selected entity and weapon for artillery display
    private Entity selectedEntity = null;
    private Mounted selectedWeapon = null;

    // hexes with ECM effect
    private HashMap<Coords, Integer> ecmHexes = null;

    private boolean dirtyBoard = true;

    /**
     * Construct a new board view for the specified game
     */
    public BoardView1(IGame game) throws IOException {
        this.game = game;
        tileManager = new TilesetManager(this);

        game.addGameListener(gameListener);
        game.getBoard().addBoardListener(this);
        scheduleRedrawTimer();

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent we) {
                if (GUIPreferences.getInstance().getMouseWheelZoom()) {
                    if (we.getWheelRotation() > 0) {
                        zoomIn();
                    } else {
                        zoomOut();
                    }
                }
            }
        });

        zoomIndex = GUIPreferences.getInstance().getMapZoomIndex();
        checkZoomIndex();
        scale = ZOOM_FACTORS[zoomIndex];

        updateFontSizes();
        updateBoardSize();

        // tooltip
        tipWindow = new Window(new Frame());

        hex_size = new Dimension((int) (HEX_W * scale), (int) (HEX_H * scale));

        initPolys();

        cursorSprite = new CursorSprite(Color.cyan);
        highlightSprite = new CursorSprite(Color.white);
        selectedSprite = new CursorSprite(Color.blue);
        firstLOSSprite = new CursorSprite(Color.red);
        secondLOSSprite = new CursorSprite(Color.red);

        PreferenceManager.getClientPreferences().addPreferenceChangeListener(
                this);

        SpecialHexDisplay.Type.ARTILLERY_HIT.init(getToolkit());
        SpecialHexDisplay.Type.ARTILLERY_INCOMING.init(getToolkit());
        SpecialHexDisplay.Type.ARTILLERY_TARGET.init(getToolkit());
        SpecialHexDisplay.Type.ARTILLERY_ADJUSTED.init(getToolkit());
        SpecialHexDisplay.Type.ARTILLERY_AUTOHIT.init(getToolkit());
    }

    protected final RedrawWorker redrawWorker = new RedrawWorker();

    protected void scheduleRedrawTimer() {
        final TimerTask redraw = new TimerTask() {
            @Override
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(redrawWorker);
                } catch (Exception ie) {
                }
            }
        };
        TimerSingleton.getInstance().schedule(redraw, 20, 20);
    }

    protected void scheduleRedraw() {
        try {
            SwingUtilities.invokeLater(redrawWorker);
        } catch (Exception ie) {
        }
    }

    public void preferenceChange(PreferenceChangeEvent e) {
        if (e.getName().equals(IClientPreferences.MAP_TILESET)) {
            updateBoard();
        }
    }

    /**
     * Adds the specified board listener to receive board events from this
     * board.
     *
     * @param listener the board listener.
     */
    public void addBoardViewListener(BoardViewListener listener) {
        if (!boardListeners.contains(listener)) {
            boardListeners.add(listener);
        }
    }

    /**
     * Removes the specified board listener.
     *
     * @param listener the board listener.
     */
    public void removeBoardViewListener(BoardViewListener listener) {
        boardListeners.remove(listener);
    }

    /**
     * Notifies attached board listeners of the event.
     *
     * @param event the board event.
     */
    public void processBoardViewEvent(BoardViewEvent event) {
        if (boardListeners == null) {
            return;
        }
        for (BoardViewListener l : boardListeners) {
            switch (event.getType()) {
            case BoardViewEvent.BOARD_HEX_CLICKED:
            case BoardViewEvent.BOARD_HEX_DOUBLECLICKED:
            case BoardViewEvent.BOARD_HEX_DRAGGED:
            case BoardViewEvent.BOARD_HEX_POPUP:
                l.hexMoused(event);
                break;
            case BoardViewEvent.BOARD_HEX_CURSOR:
                l.hexCursor(event);
                break;
            case BoardViewEvent.BOARD_HEX_HIGHLIGHTED:
                l.boardHexHighlighted(event);
                break;
            case BoardViewEvent.BOARD_HEX_SELECTED:
                l.hexSelected(event);
                break;
            case BoardViewEvent.BOARD_FIRST_LOS_HEX:
                l.firstLOSHex(event);
                break;
            case BoardViewEvent.BOARD_SECOND_LOS_HEX:
                l.secondLOSHex(event, getFirstLOS());
                break;
            case BoardViewEvent.FINISHED_MOVING_UNITS:
                l.finishedMovingUnits(event);
                break;
            case BoardViewEvent.SELECT_UNIT:
                l.unitSelected(event);
                break;
            }
        }
    }

    void addMovingUnit(Entity entity, Vector<UnitLocation> movePath) {
        if (!movePath.isEmpty()) {
            MovingUnit m = new MovingUnit(entity, movePath);
            movingUnits.add(m);

            GhostEntitySprite ghostSprite = new GhostEntitySprite(entity);
            ghostEntitySprites.add(ghostSprite);

            // Center on the starting hex of the moving unit.
            UnitLocation loc = (movePath.elementAt(0));
            centerOnHex(loc.getCoords());
        }
    }

    public void addDisplayable(IDisplayable disp) {
        displayables.add(disp);
    }

    public void removeDisplayable(IDisplayable disp) {
        displayables.remove(disp);
    }

    /**
     * Update ourself when a scroll bar is adjusted.
     *
     * @param event - the <code>AdjustmentEvent</code> that caused this call.
     */
    public void adjustmentValueChanged(AdjustmentEvent event) {
        Point oldPt = scroll;
        Point newPt = new Point(oldPt.x, oldPt.y);
        if (event.getAdjustable().getOrientation() == Adjustable.VERTICAL) {
            newPt.y = event.getValue();
        } else {
            newPt.x = event.getValue();
        }
        scroll.setLocation(newPt);
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        update(g);
    }

    /**
     * Draw the screen!
     */
    @Override
    public synchronized void update(Graphics g) {
        // Limit our size to the viewport of the scroll pane.
        final Dimension size = getSize();
        // final long startTime = System.currentTimeMillis(); // commentme

        // Make sure our scrollbars have the right sizes.
        // N.B. A buggy Sun implementation makes me to do this here instead
        // of updateBoardSize() (which is where *I* think it belongs).
        if (null != vScrollbar) {
            vScrollbar.setVisibleAmount(size.height);
            vScrollbar.setBlockIncrement(size.height);
            vScrollbar.setUnitIncrement((int) (scale * HEX_H / 2.0));
            vScrollbar.setMaximum(boardSize.height);
        }
        if (null != hScrollbar) {
            hScrollbar.setVisibleAmount(size.width);
            hScrollbar.setBlockIncrement(size.width);
            hScrollbar.setUnitIncrement((int) (scale * HEX_W / 2.0));
            hScrollbar.setMaximum(boardSize.width);
        }

        // update view, offset
        view.setLocation(scroll);
        view.setSize(getOptimalView(size));
        offset.setLocation(getOptimalOffset(size));

        if (!isTileImagesLoaded()) {
            g.drawString(
                    Messages.getString("BoardView1.loadingImages"), 20, 50); //$NON-NLS-1$
            if (!tileManager.isStarted()) {
                System.out.println("boardview1: loading images for board"); //$NON-NLS-1$
                tileManager.loadNeededImages(game);
            }
            return;
        }

        // make sure back buffer is valid
        if ((backGraph == null) || !view.getSize().equals(backSize)) {
            // make new back buffer
            backSize = view.getSize();
            backImage = createImage(backSize.width, backSize.height);
            backGraph = backImage.getGraphics();
        }

        // make sure board rectangle contains our current view rectangle
        if ((boardImage == null) || !boardRect.union(view).equals(boardRect)
                || dirtyBoard) {
            updateBoardImage();
        }

        // draw onto the back buffer:

        // redraw all the specials. This is an inneficient hack :/
        for(Coords c : game.getBoard().getSpecialHexDisplayTable().keySet()) {
            drawHex(c);
        }

        // draw the board
        backGraph.drawImage(boardImage, 0, 0, this);

        // draw wrecks
        if (GUIPreferences.getInstance().getShowWrecks()) {
            drawSprites(wreckSprites);
        }

        // Minefield signs all over the place!
        drawMinefields();

        // draw highlight border
        drawSprite(highlightSprite);

        // draw cursors
        drawSprite(cursorSprite);
        drawSprite(selectedSprite);
        drawSprite(firstLOSSprite);
        drawSprite(secondLOSSprite);

        // draw deployment indicators
        if (en_Deployer != null) {
            drawDeployment();
        }

        // draw C3 links
        drawSprites(C3Sprites);

        // draw onscreen entities
        drawSprites(entitySprites);

        // draw moving onscreen entities
        drawSprites(movingEntitySprites);

        // draw ghost onscreen entities
        drawSprites(ghostEntitySprites);

        // draw onscreen attacks
        drawSprites(attackSprites);

        //draw movement vectors.
        if(game.useVectorMove() && (game.getPhase() == IGame.Phase.PHASE_MOVEMENT)) {
            drawSprites(movementSprites);
        }

        // draw movement, if valid
        drawSprites(pathSprites);

        // draw the ruler line
        if (rulerStart != null) {
            Point start = getCentreHexLocation(rulerStart);
            if (rulerEnd != null) {
                Point end = getCentreHexLocation(rulerEnd);
                backGraph.setColor(Color.yellow);
                backGraph.drawLine(start.x - boardRect.x,
                        start.y - boardRect.y, end.x - boardRect.x, end.y
                        - boardRect.y);

                backGraph.setColor(rulerEndColor);
                backGraph.fillRect(end.x - boardRect.x - 1, end.y - boardRect.y
                        - 1, 2, 2);
            }

            backGraph.setColor(rulerStartColor);
            backGraph.fillRect(start.x - boardRect.x - 1, start.y - boardRect.y
                    - 1, 2, 2);
        }

        // draw all the "displayables"
        for (int i = 0; i < displayables.size(); i++) {
            IDisplayable disp = displayables.get(i);
            disp.draw(backGraph, new Rectangle(backSize));
        }

        // draw the back buffer onto the screen
        // first clear the entire view if the map has been zoomed
        if (scale < 1.00f) {
            Image tmpImage = createImage(size.width, size.height);
            Graphics tmpGraphics = tmpImage.getGraphics();
            tmpGraphics.drawImage(backImage, offset.x, offset.y, this);
            g.drawImage(tmpImage, 0, 0, this);
            tmpGraphics.dispose();
        } else {
            g.drawImage(backImage, offset.x, offset.y, this);
        }
    }

    /**
     * Updates the boardSize variable with the proper values for this board.
     */
    private void updateBoardSize() {
        int width = game.getBoard().getWidth() * (int) (HEX_WC * scale)
                + (int) (HEX_W / 4 * scale);
        int height = game.getBoard().getHeight() * (int) (HEX_H * scale)
                + (int) (HEX_H / 2 * scale);
        boardSize = new Dimension(width, height);
    }

    /**
     * Think up the size of the view rectangle based on the size of the
     * component and the size of board
     */
    private Dimension getOptimalView(Dimension size) {
        return new Dimension(Math.min(size.width, boardSize.width), Math.min(
                size.height, boardSize.height));
    }

    /**
     * Where should the offset be for this screen size?
     */
    private Point getOptimalOffset(Dimension size) {
        int ox = 0;
        int oy = 0;
        if (size.width > boardSize.width) {
            ox = (size.width - boardSize.width) / 2;
        }
        if (size.height > boardSize.height) {
            oy = (size.height - boardSize.height) / 2;
        }
        return new Point(ox, oy);
    }

    /**
     * Repaint the bounds of a sprite, offset by view
     */
    private void repaintBounds(Rectangle bounds) {
        if (view != null) {
            repaint(bounds.x - view.x + offset.x, bounds.y - view.y + offset.y,
                    bounds.width, bounds.height);
        }
    }

    /**
     * Looks through a vector of buffered images and draws them if they're
     * onscreen.
     */
    private synchronized void drawSprites(
            ArrayList<? extends Sprite> spriteArrayList) {
        for (Sprite sprite : spriteArrayList) {
            drawSprite(sprite);
        }
    }

    /**
     * Draws a sprite, if it is in the current view
     */
    private final void drawSprite(Sprite sprite) {
        if (view.intersects(sprite.getBounds()) && !sprite.hidden) {
            final int drawX = sprite.getBounds().x - view.x;
            final int drawY = sprite.getBounds().y - view.y;
            if (!sprite.isReady()) {
                sprite.prepare();
            }
            sprite.drawOnto(backGraph, drawX, drawY, this);
        }
    }

    /**
     * Manages a cache of scaled images.
     */
    Image getScaledImage(Image base) {
        if (base == null) {
            return null;
        }
        if (zoomIndex == BASE_ZOOM_INDEX) {
            return base;
        }

        Image scaled = scaledImageCache.get(base);
        if (scaled == null) {
            MediaTracker tracker = new MediaTracker(this);
            if ((base.getWidth(null) == -1) || (base.getHeight(null) == -1)) {
                tracker.addImage(base, 0);
                try {
                    tracker.waitForID(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tracker.removeImage(base);
            }
            int width = (int) (base.getWidth(null) * scale);
            int height = (int) (base.getHeight(null) * scale);

            //TODO: insert a check that width and height are > 0.

            scaled = scale(base, width, height);
            tracker.addImage(scaled, 1);
            // Wait for image to load
            try {
                tracker.waitForID(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tracker.removeImage(scaled);

            scaledImageCache.put(base, scaled);
        }
        return scaled;
    }

    /**
     * The actual scaling code.
     */
    private Image scale(Image img, int width, int height) {
        ImageFilter filter;
        filter = new ImprovedAveragingScaleFilter(img.getWidth(null), img
                .getHeight(null), width, height);

        ImageProducer prod;
        prod = new FilteredImageSource(img.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(prod);
    }

    /**
     * Draw an outline around legal deployment hexes
     */
    private void drawDeployment() {
        // only update visible hexes
        int drawX = view.x / (int) (HEX_WC * scale) - 1;
        int drawY = view.y / (int) (HEX_H * scale) - 1;

        int drawWidth = view.width / (int) (HEX_WC * scale) + 3;
        int drawHeight = view.height / (int) (HEX_H * scale) + 3;
        IBoard board = game.getBoard();
        // loop through the hexes
        for (int i = 0; i < drawHeight; i++) {
            for (int j = 0; j < drawWidth; j++) {
                Coords c = new Coords(j + drawX, i + drawY);
                Point p = getHexLocation(c);
                p.translate(-(view.x), -(view.y));
                if (board.isLegalDeployment(c, en_Deployer.getStartingPos())) {
                    backGraph.setColor(Color.yellow);
                    int[] xcoords = { p.x + (int) (21 * scale),
                            p.x + (int) (62 * scale), p.x + (int) (83 * scale),
                            p.x + (int) (83 * scale), p.x + (int) (62 * scale),
                            p.x + (int) (21 * scale), p.x, p.x };
                    int[] ycoords = { p.y, p.y, p.y + (int) (35 * scale),
                            p.y + (int) (36 * scale), p.y + (int) (71 * scale),
                            p.y + (int) (71 * scale), p.y + (int) (36 * scale),
                            p.y + (int) (35 * scale) };
                    backGraph.drawPolygon(xcoords, ycoords, 8);
                }
            }
        }
    }

    /**
     * returns the weapon selected in the mech display, or null if none selected
     * or it is not artillery or null if the selected entity is not owned
     */
    private Mounted getSelectedArtilleryWeapon() {
        if ((selectedEntity == null) || (selectedWeapon == null)) {
            return null;
        }

        if (!selectedEntity.getOwner().equals(localPlayer)) {
            return null; // Not my business to see this
        }

        if (selectedEntity.getEquipmentNum(selectedWeapon) == -1) {
            return null; // inconsistent state - weapon not on entity
        }

        if (!((selectedWeapon.getType() instanceof WeaponType) && selectedWeapon
                .getType().hasFlag(WeaponType.F_ARTILLERY))) {
            return null; // not artillery
        }

        // otherwise, a weapon is selected, and it is artillery
        return selectedWeapon;
    }

    private ArrayList<ArtilleryAttackAction> getArtilleryAttacksAtLocation(
            Coords c) {
        ArrayList<ArtilleryAttackAction> v = new ArrayList<ArtilleryAttackAction>();
        for (Enumeration<ArtilleryAttackAction> attacks = game
                .getArtilleryAttacks(); attacks.hasMoreElements();) {
            ArtilleryAttackAction a = attacks.nextElement();
            if (a.getTarget(game).getPosition().equals(c)) {
                v.add(a);
            }
        }
        return v;
    }

    /**
     * Writes "MINEFIELD" in minefield hexes...
     */
    private void drawMinefields() {
        // only update visible hexes
        int drawX = view.x / (int) (HEX_WC * scale) - 1;
        int drawY = view.y / (int) (HEX_H * scale) - 1;

        int drawWidth = view.width / (int) (HEX_WC * scale) + 3;
        int drawHeight = view.height / (int) (HEX_H * scale) + 3;

        IBoard board = game.getBoard();
        // loop through the hexes
        for (int i = 0; i < drawHeight; i++) {
            for (int j = 0; j < drawWidth; j++) {
                Coords c = new Coords(j + drawX, i + drawY);
                Point p = getHexLocation(c);
                p.translate(-(view.x), -(view.y));

                if (!board.contains(c)) {
                    continue;
                }
                if (!game.containsMinefield(c)) {
                    continue;
                }

                Minefield mf = game.getMinefields(c).elementAt(0);

                Image tmpImage = getScaledImage(tileManager.getMinefieldSign());
                backGraph.drawImage(tmpImage, p.x + (int) (13 * scale), p.y
                        + (int) (13 * scale), this);

                backGraph.setColor(Color.black);
                int nbrMfs = game.getNbrMinefields(c);
                if (nbrMfs > 1) {
                    drawCenteredString(
                            Messages.getString("BoardView1.Multiple"), //$NON-NLS-1$
                            p.x, p.y + (int) (51 * scale), font_minefield,
                            backGraph);
                } else if (nbrMfs == 1) {
                    switch (mf.getType()) {
                    case (Minefield.TYPE_CONVENTIONAL):
                        drawCenteredString(
                                Messages
                                .getString("BoardView1.Conventional") + mf.getDensity() + ")", //$NON-NLS-1$
                                p.x, p.y + (int) (51 * scale),
                                font_minefield, backGraph);
                    if(mf.isSeaBased()) {
                        drawCenteredString(
                                "Depth: " + mf.getDepth() + "", //$NON-NLS-1$ //$NON-NLS-2$
                                p.x, p.y + (int) (60 * scale),
                                font_minefield, backGraph);
                    }
                    break;
                    case (Minefield.TYPE_INFERNO):
                        drawCenteredString(
                                Messages
                                .getString("BoardView1.Inferno") + mf.getDensity() + ")", //$NON-NLS-1$ //$NON-NLS-2$
                                p.x, p.y + (int) (51 * scale),
                                font_minefield, backGraph);
                    break;
                    case (Minefield.TYPE_ACTIVE):
                        drawCenteredString(
                                Messages
                                .getString("BoardView1.Active") + mf.getDensity() + ")", //$NON-NLS-1$ //$NON-NLS-2$
                                p.x, p.y + (int) (51 * scale),
                                font_minefield, backGraph);
                    break;
                    case (Minefield.TYPE_COMMAND_DETONATED):
                        drawCenteredString(
                                Messages.getString("BoardView1.Command") + mf.getDensity() + ")", //$NON-NLS-1$
                                p.x, p.y + (int) (51 * scale),
                                font_minefield, backGraph);
                    break;
                    case (Minefield.TYPE_VIBRABOMB):
                        drawCenteredString(
                                Messages.getString("BoardView1.Vibrabomb") + mf.getDensity() + ")", //$NON-NLS-1$
                                p.x, p.y + (int) (51 * scale),
                                font_minefield, backGraph);
                    if (mf.getPlayerId() == localPlayer.getId()) {
                        drawCenteredString(
                                "(" + mf.getSetting() + ")", //$NON-NLS-1$ //$NON-NLS-2$
                                p.x, p.y + (int) (60 * scale),
                                font_minefield, backGraph);
                    }
                    break;
                    }
                }
            }
        }
    }

    private void drawCenteredString(String string, int x, int y, Font font,
            Graphics graph) {
        FontMetrics currentMetrics = getFontMetrics(font);
        int stringWidth = currentMetrics.stringWidth(string);

        x += ((hex_size.width - stringWidth) / 2);

        graph.setFont(font);
        graph.drawString(string, x, y);
    }

    /**
     * Updates the board buffer to contain all the hexes needed by the view.
     */
    private void updateBoardImage() {
        // check to make sure image is big enough
        if ((boardGraph == null) || (view.width > boardRect.width)
                || (view.height > boardRect.height)) {
            /*
             * Ok, some history here. Before the zoom patch, the boardImage was
             * created with the same size as the view. After the zoom patch, the
             * boardImage was created with the same size as the entire board
             * (all maps). This change ate up a hideous amount of memory (eg: in
             * a 3x3 map set test with one mech, memory usage went from about
             * 15MB to 60MB). I have now changed it back to the old way, and the
             * zoom feature *seems* to still work. Why the zoom author made the
             * change, I cannot say.
             */
            boardImage = createImage(view.width, view.height);
            // boardImage = createImage(boardSize.width, boardSize.height);
            /* ----- */

            if (boardGraph != null) {
                boardGraph.dispose();
            }
            boardGraph = boardImage.getGraphics();

            // Handle resizes correctly.
            checkScrollBounds();
            boardRect = new Rectangle(view);
            System.out
                    .println("boardview1: made a new board buffer " + boardRect); //$NON-NLS-1$
            drawHexes(view);
            dirtyBoard = false;
        }
        if (!boardRect.union(view).equals(boardRect) || dirtyBoard) {
            moveBoardImage();
        }
    }

    /**
     * This method creates an image the size of the entire board (all
     * mapsheets), draws the hexes onto it, and returns that image.
     */
    public Image getEntireBoardImage() {
        Image entireBoard = createImage(boardSize.width, boardSize.height);
        Graphics temp = boardImage.getGraphics();
        boardGraph = entireBoard.getGraphics();
        drawHexes(new Rectangle(boardSize));
        boardGraph.dispose();
        boardGraph = temp;
        return entireBoard;
    }

    /**
     * Moves the board view to another area.
     */
    private void moveBoardImage() {
        // salvage the old

        boardGraph.setClip(0, 0, boardRect.width, boardRect.height);

        boardGraph.copyArea(0, 0, boardRect.width, boardRect.height,
                boardRect.x - view.x, boardRect.y - view.y);

        // what's left to paint?
        int midX = Math.max(view.x, boardRect.x);
        int midWidth = view.width - Math.abs(view.x - boardRect.x);
        Rectangle unLeft = new Rectangle(view.x, view.y, boardRect.x - view.x,
                view.height);
        Rectangle unRight = new Rectangle(boardRect.x + boardRect.width,
                view.y, view.x - boardRect.x, view.height);
        Rectangle unTop = new Rectangle(midX, view.y, midWidth, boardRect.y
                - view.y);
        Rectangle unBottom = new Rectangle(midX,
                boardRect.y + boardRect.height, midWidth, view.y - boardRect.y);

        // update boardRect
        boardRect = new Rectangle(view);

        if (dirtyBoard) {
            drawHexes(view);
            dirtyBoard = false;
        } else {
            // paint needed areas
            if (unLeft.width > 0) {
                drawHexes(unLeft);
            } else if (unRight.width > 0) {
                drawHexes(unRight);
            }
            if (unTop.height > 0) {
                drawHexes(unTop);
            } else if (unBottom.height > 0) {
                drawHexes(unBottom);
            }
        }
    }

    /**
     * Redraws all hexes in the specified rectangle
     */
    private void drawHexes(Rectangle rect) {

        // rect is the view
        int drawX = (int) (rect.x / (HEX_WC * scale)) - 1;
        int drawY = (int) (rect.y / (HEX_H * scale)) - 1;

        int drawWidth = (int) (rect.width / (HEX_WC * scale)) + 3;
        int drawHeight = (int) (rect.height / (HEX_H * scale)) + 3;

        // only draw what we came to draw
        boardGraph.setClip(rect.x - boardRect.x, rect.y - boardRect.y,
                rect.width, rect.height);

        // clear, if we need to
        if (rect.x < (21 * scale)) {
            boardGraph.clearRect(rect.x - boardRect.x, rect.y - boardRect.y,
                    (int) (21 * scale) - rect.x, rect.height);
        }
        if (rect.y < (36 * scale)) {
            boardGraph.clearRect(rect.x - boardRect.x, rect.y - boardRect.y,
                    rect.width, (int) (36 * scale) - rect.y);
        }
        if (rect.x > boardSize.width - view.width - (21 * scale)) {
            boardGraph.clearRect(boardRect.width - (int) (21 * scale), rect.y
                    - boardRect.y, (int) (21 * scale), rect.height);
        }
        if (rect.y > boardSize.height - view.height - (int) (36 * scale)) {
            boardGraph.clearRect(rect.x - boardRect.x, boardRect.height
                    - (int) (36 * scale), rect.width, (int) (36 * scale));
        }

        // draw some hexes
        for (int i = 0; i < drawHeight; i++) {
            for (int j = 0; j < drawWidth; j++) {
                drawHex(new Coords(j + drawX, i + drawY));
            }
        }
    }

    /**
     * Redraws a hex and all the hexes immediately around it. Used when the hex
     * is on the screen, as opposed to when it is scrolling onto the screen, so
     * it resets the clipping rectangle before drawing.
     */
    private void redrawAround(Coords c) {
        if(c != null) {
            boardGraph.setClip(0, 0, boardRect.width, boardRect.height);
            drawHex(c);
            drawHex(c.translated(0));
            drawHex(c.translated(1));
            drawHex(c.translated(2));
            drawHex(c.translated(3));
            drawHex(c.translated(4));
            drawHex(c.translated(5));
        }
    }

    /**
     * Draws a hex onto the board buffer. This assumes that boardRect is
     * current, and does not check if the hex is visible.
     */
    private void drawHex(Coords c) {
        if (!game.getBoard().contains(c)) {
            return;
        }

        final IHex hex = game.getBoard().getHex(c);
        final Point hexLoc = getHexLocation(c);

        int level = hex.getElevation();
        int depth = hex.depth();
        int height = Math.max(hex.terrainLevel(Terrains.BLDG_ELEV), hex
                .terrainLevel(Terrains.BRIDGE_ELEV));
        height = Math.max(height, hex.terrainLevel(Terrains.INDUSTRIAL));

        // offset drawing point

        int drawX = hexLoc.x - boardRect.x;
        int drawY = hexLoc.y - boardRect.y;

        // draw picture
        Image baseImage = tileManager.baseFor(hex);
        Image scaledImage = getScaledImage(baseImage);

        boardGraph.drawImage(scaledImage, drawX, drawY, this);

        if (tileManager.supersFor(hex) != null) {
            for (Image image : tileManager.supersFor(hex)) {
                scaledImage = getScaledImage(image);
                boardGraph.drawImage(scaledImage, drawX, drawY, this);
            }
        }

        if (ecmHexes != null) {
            Integer tint = ecmHexes.get(c);
            if (tint != null) {
                scaledImage = getScaledImage(tileManager.getEcmShade(tint
                        .intValue()));
                boardGraph.drawImage(scaledImage, drawX, drawY, this);
            }
        }

        if (GUIPreferences.getInstance().getBoolean(
                GUIPreferences.ADVANCED_DARKEN_MAP_AT_NIGHT)
                && (game.getPlanetaryConditions().getLight() > PlanetaryConditions.L_DAY)
                && !game.isPositionIlluminated(c)) {
            scaledImage = getScaledImage(tileManager.getNightFog());
            boardGraph.drawImage(scaledImage, drawX, drawY, this);
        }
        boardGraph.setColor(GUIPreferences.getInstance().getMapTextColor());
        if(game.getBoard().inSpace()) {
            boardGraph.setColor(Color.LIGHT_GRAY);
        }

        //draw special stuff for the hex
        final Collection<SpecialHexDisplay> shdList = game.getBoard().getSpecialHexDisplay(c);
        try {
            if(shdList != null) {
                for(SpecialHexDisplay shd : shdList)
                {
                    if (shd.drawNow(game.getPhase(), game.getRoundCount())) {
                        scaledImage = getScaledImage(shd.getType().getDefaultImage());
                        if(scaledImage != null) {
                            boardGraph.drawImage(scaledImage, drawX, drawY, this);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Illegal argument exception, probably can't load file.");
            e.printStackTrace();
            drawCenteredString(
                    "Loading Error",
                    drawX,
                    drawY + (int)(50*scale),
                    font_note,
                    boardGraph);
            return;
        }

        // draw hex number
        if (scale >= 0.5) {
            drawCenteredString(c.getBoardNum(), drawX, drawY
                    + (int) (12 * scale), font_hexnum, boardGraph);
        }
        // draw terrain level / water depth / building height
        if (zoomIndex > 3) {
            int ypos = 70;
            if(null != shdList) {
                Color oldColor = boardGraph.getColor();
                boardGraph.setColor(Color.RED);
                for(SpecialHexDisplay shd : shdList)
                {
                    if(SpecialHexDisplay.Type.PLAYER_NOTE == shd.getType()) {
                        drawCenteredString(
                                shd.getInfo(),
                                drawX,
                                drawY + (int)(ypos*scale),
                                font_note,
                                boardGraph);
                        ypos -= 10;
                    }
                }

                boardGraph.setColor(oldColor);
            }
            if (level != 0) {
                drawCenteredString(
                        Messages.getString("BoardView1.LEVEL") + level, //$NON-NLS-1$
                        drawX, drawY + (int) (ypos * scale), font_elev,
                        boardGraph);
                ypos -= 10;
            }
            if (depth != 0) {
                drawCenteredString(
                        Messages.getString("BoardView1.DEPTH") + depth, //$NON-NLS-1$
                        drawX, drawY + (int) (ypos * scale), font_elev,
                        boardGraph);
                ypos -= 10;
            }
            if (height > 0) {
                boardGraph.setColor(GUIPreferences.getInstance().getColor(
                "AdvancedBuildingTextColor"));
                drawCenteredString(
                        Messages.getString("BoardView1.HEIGHT") + height, //$NON-NLS-1$
                        drawX, drawY + (int) (ypos * scale), font_elev,
                        boardGraph);
                ypos -= 10;
            }
        }

        // draw elevation borders
        boardGraph.setColor(Color.black);
        if (drawElevationLine(c, 0)) {
            boardGraph.drawLine(drawX + (int) (21 * scale), drawY, drawX
                    + (int) (62 * scale), drawY);
        }
        if (drawElevationLine(c, 1)) {
            boardGraph.drawLine(drawX + (int) (62 * scale), drawY, drawX
                    + (int) (83 * scale), drawY + (int) (35 * scale));
        }
        if (drawElevationLine(c, 2)) {
            boardGraph.drawLine(drawX + (int) (83 * scale), drawY
                    + (int) (36 * scale), drawX + (int) (62 * scale), drawY
                    + (int) (71 * scale));
        }
        if (drawElevationLine(c, 3)) {
            boardGraph.drawLine(drawX + (int) (62 * scale), drawY
                    + (int) (71 * scale), drawX + (int) (21 * scale), drawY
                    + (int) (71 * scale));
        }
        if (drawElevationLine(c, 4)) {
            boardGraph.drawLine(drawX + (int) (21 * scale), drawY
                    + (int) (71 * scale), drawX, drawY + (int) (36 * scale));
        }
        if (drawElevationLine(c, 5)) {
            boardGraph.drawLine(drawX, drawY + (int) (35 * scale), drawX
                    + (int) (21 * scale), drawY);
        }

        // draw mapsheet borders
        if (GUIPreferences.getInstance().getShowMapsheets()) {
            boardGraph.setColor(GUIPreferences.getInstance().getColor(
                    GUIPreferences.ADVANCED_MAPSHEET_COLOR));
            if (c.x % 16 == 0) {
                // left edge of sheet (edge 4 & 5)
                boardGraph
                .drawLine(drawX + (int) (21 * scale), drawY
                        + (int) (71 * scale), drawX, drawY
                        + (int) (36 * scale));
                boardGraph.drawLine(drawX, drawY + (int) (35 * scale), drawX
                        + (int) (21 * scale), drawY);
            } else if (c.x % 16 == 15) {
                // right edge of sheet (edge 1 & 2)
                boardGraph.drawLine(drawX + (int) (62 * scale), drawY, drawX
                        + (int) (83 * scale), drawY + (int) (35 * scale));
                boardGraph.drawLine(drawX + (int) (83 * scale), drawY
                        + (int) (36 * scale), drawX + (int) (62 * scale), drawY
                        + (int) (71 * scale));
            }
            if (c.y % 17 == 0) {
                // top edge of sheet (edge 0 and possible 1 & 5)
                boardGraph.drawLine(drawX + (int) (21 * scale), drawY, drawX
                        + (int) (62 * scale), drawY);
                if (c.x % 2 == 0) {
                    boardGraph.drawLine(drawX + (int) (62 * scale), drawY,
                            drawX + (int) (83 * scale), drawY
                            + (int) (35 * scale));
                    boardGraph.drawLine(drawX, drawY + (int) (35 * scale),
                            drawX + (int) (21 * scale), drawY);
                }
            } else if (c.y % 17 == 16) {
                // bottom edge of sheet (edge 3 and possible 2 & 4)
                boardGraph.drawLine(drawX + (int) (62 * scale), drawY
                        + (int) (71 * scale), drawX + (int) (21 * scale), drawY
                        + (int) (71 * scale));
                if (c.x % 2 == 1) {
                    boardGraph.drawLine(drawX + (int) (83 * scale), drawY
                            + (int) (36 * scale), drawX + (int) (62 * scale),
                            drawY + (int) (71 * scale));
                    boardGraph.drawLine(drawX + (int) (21 * scale), drawY
                            + (int) (71 * scale), drawX, drawY
                            + (int) (36 * scale));
                }
            }
            boardGraph.setColor(Color.black);
        }
    }

    /**
     * Returns true if an elevation line should be drawn between the starting
     * hex and the hex in the direction specified. Results should be transitive,
     * that is, if a line is drawn in one direction, it should be drawn in the
     * opposite direction as well.
     */
    private final boolean drawElevationLine(Coords src, int direction) {
        final IHex srcHex = game.getBoard().getHex(src);
        final IHex destHex = game.getBoard().getHexInDir(src, direction);
        return (destHex != null) && (srcHex.floor() != destHex.floor());
    }

    /**
     * Returns the absolute position of the upper-left hand corner of the hex
     * graphic
     */
    private Point getHexLocation(int x, int y) {
        return new Point(x * (int) (HEX_WC * scale), y * (int) (HEX_H * scale)
                + ((x & 1) == 1 ? (int) (HEX_H / 2 * scale) : 0));
    }

    Point getHexLocation(Coords c) {
        return getHexLocation(c.x, c.y);
    }

    /**
     * Returns the absolute position of the centre of the hex graphic
     */
    private Point getCentreHexLocation(int x, int y) {
        Point p = getHexLocation(x, y);
        p.x += (HEX_W / 2 * scale);
        p.y += (HEX_H / 2 * scale);
        return p;
    }

    private Point getCentreHexLocation(Coords c) {
        return getCentreHexLocation(c.x, c.y);
    }

    /**
     * Returns the coords at the specified point
     */
    Coords getCoordsAt(Point p) {
        final int x = (p.x + scroll.x - offset.x) / (int) (HEX_WC * scale);
        final int y = ((p.y + scroll.y - offset.y) - ((x & 1) == 1 ? (int) (HEX_H / 2 * scale)
                : 0))
                / (int) (HEX_H * scale);
        return new Coords(x, y);
    }

    /**
     * Shows the tooltip thinger
     */
    private void showTooltip() {
        try {
            final Point tipLoc = new Point(getLocationOnScreen());
            // retrieve tip text
            String[] tipText = getTipText(mousePos);
            if (tipText == null) {
                return;
            }

            // update tip text
            tipWindow.removeAll();
            tipWindow.add(new TooltipCanvas(tipText));
            tipWindow.pack();

            tipLoc.translate(mousePos.x, mousePos.y + 20);

            // adjust horizontal location for the tipWindow if it goes off the
            // frame
            if (getLocationOnScreen().x + this.getSize().width < tipLoc.x
                    + tipWindow.getSize().width + 10) {
                if (this.getSize().width > tipWindow.getSize().width) {
                    // bound it by the right edge of the frame
                    tipLoc.x -= tipLoc.x + tipWindow.getSize().width + 10
                    - this.getSize().width - this.getLocation().x;
                } else {
                    // too big to fit, left justify to the frame (roughly).
                    // how do I extract the first term of HEX_SIZE to use
                    // here?--LDE
                    tipLoc.x = getLocationOnScreen().x + hex_size.width;
                }
            }

            // set tip location
            tipWindow.setLocation(tipLoc);

            tipWindow.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            tipWindow = new Window(null);
        }
    }

    /**
     * The text to be displayed when the mouse is at a certain point
     */
    private String[] getTipText(Point point) {

        int stringsSize = 0;
        IHex mhex = null;

        // first, we have to determine how much text we are going to have
        // are we on a hex?
        final Coords mcoords = getCoordsAt(point);
        if (GUIPreferences.getInstance().getShowMapHexPopup()
                && game.getBoard().contains(mcoords)) {
            mhex = game.getBoard().getHex(mcoords);
            stringsSize += 1;
        }

        // check if it's on any entities
        for (EntitySprite eSprite : entitySprites) {
            if (eSprite.isInside(point)) {
                stringsSize += 3;
            }
        }

        // check if it's on any attacks
        for (AttackSprite aSprite : attackSprites) {
            if (aSprite.isInside(point)) {
                stringsSize += 1 + aSprite.weaponDescs.size();
            }
        }

        // If the hex contains a building, make more space.
        // Also if it contains other displayable terrain.
        if (mhex != null) {
            stringsSize += mhex.displayableTerrainsPresent();
            if (mhex.containsTerrain(Terrains.BUILDING)) {
                stringsSize++;
            }
            if (mhex.containsTerrain(Terrains.FUEL_TANK)) {
                stringsSize++;
            }
            if (mhex.containsTerrain(Terrains.BRIDGE)) {
                stringsSize++;
            }
        }

        stringsSize += game.getNbrMinefields(mcoords);

        // Artillery
        final ArrayList<ArtilleryAttackAction> artilleryAttacks = getArtilleryAttacksAtLocation(mcoords);
        stringsSize += artilleryAttacks.size();

        // Artillery fire adjustment
        final Mounted curWeapon = getSelectedArtilleryWeapon();
        if (curWeapon != null) {
            stringsSize++;
        }

        /*
         * Eventaul replacemtn for the artilery popup.
         *
        final Collection<SpecialHexDisplay> specials = game.getBoard().getSpecialHexDisplay(mcoords);
        if(specials != null)
                stringsSize += specials.size();

        // if the size is zip, you must a'quit
        if (stringsSize == 0) {
            return null;
        }*/

        // now we can allocate an array of strings
        String[] strings = new String[stringsSize];
        int stringsIndex = 0;

        // are we on a hex?
        if (mhex != null) {
            strings[stringsIndex] = Messages.getString("BoardView1.Hex") + mcoords.getBoardNum() //$NON-NLS-1$
            + Messages.getString("BoardView1.level") + mhex.getElevation(); //$NON-NLS-1$
            stringsIndex += 1;

            //cycle through the terrains and report types found
            //this will skip buildings and other constructed units
            for(int i=0;i < Terrains.SIZE; i++) {
                if(mhex.containsTerrain(i)) {
                    int tf = mhex.getTerrain(i).getTerrainFactor();
                    int ttl = mhex.getTerrain(i).getLevel();
                    String name = Terrains.getDisplayName(i, ttl);
                    if(tf > 0) {
                        name = name + " (" + tf + ")";
                    }
                    if(null != name) {
                        strings[stringsIndex] = name;
                        stringsIndex += 1;
                    }
                }
            }

            // Do we have a building?
            if (mhex.containsTerrain(Terrains.FUEL_TANK)) {
                // Get the building.
                Building bldg = game.getBoard().getBuildingAt(mcoords);
                StringBuffer buf = new StringBuffer(Messages
                        .getString("BoardView1.Height")); //$NON-NLS-1$
                // Each hex of a building has its own elevation.
                buf.append(mhex.terrainLevel(Terrains.FUEL_TANK_ELEV));
                buf.append(" "); //$NON-NLS-1$
                buf.append(bldg.toString());
                buf.append(Messages.getString("BoardView1.CF")); //$NON-NLS-1$
                buf.append(bldg.getCurrentCF(mcoords));
                strings[stringsIndex] = buf.toString();
                stringsIndex += 1;
            }
            if (mhex.containsTerrain(Terrains.BUILDING)) {
                // Get the building.
                Building bldg = game.getBoard().getBuildingAt(mcoords);
                StringBuffer buf = new StringBuffer(Messages
                        .getString("BoardView1.Height")); //$NON-NLS-1$
                // Each hex of a building has its own elevation.
                buf.append(mhex.terrainLevel(Terrains.BLDG_ELEV));
                buf.append(" "); //$NON-NLS-1$
                buf.append(bldg.toString());
                buf.append(Messages.getString("BoardView1.CF")); //$NON-NLS-1$
                buf.append(bldg.getCurrentCF(mcoords));
                buf.append(Messages.getString("BoardView1.BldgArmor")); //$NON-NLS-1$
                buf.append(bldg.getArmor(mcoords));
                strings[stringsIndex] = buf.toString();
                stringsIndex += 1;
            }

            // Do we have a bridge?
            if (mhex.containsTerrain(Terrains.BRIDGE)) {
                // Get the building.
                Building bldg = game.getBoard().getBuildingAt(mcoords);
                StringBuffer buf = new StringBuffer(Messages
                        .getString("BoardView1.Height")); //$NON-NLS-1$
                // Each hex of a building has its own elevation.
                buf.append(mhex.terrainLevel(Terrains.BRIDGE_ELEV));
                buf.append(" "); //$NON-NLS-1$
                buf.append(bldg.toString());
                buf.append(Messages.getString("BoardView1.CF")); //$NON-NLS-1$
                buf.append(bldg.getCurrentCF(mcoords));
                strings[stringsIndex] = buf.toString();
                stringsIndex += 1;
            }

            if (game.containsMinefield(mcoords)) {
                Vector<Minefield> minefields = game.getMinefields(mcoords);
                for (int i = 0; i < minefields.size(); i++) {
                    Minefield mf = minefields.elementAt(i);
                    String owner = " (" + game.getPlayer(mf.getPlayerId()).getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

                    switch (mf.getType()) {
                    case (Minefield.TYPE_CONVENTIONAL):
                        strings[stringsIndex] = mf.getName()
                        + Messages
                        .getString("BoardView1.minefield") + "(" + mf.getDensity() + ")" + " " + owner; //$NON-NLS-1$ //$NON-NLS-2$
                    break;
                    case (Minefield.TYPE_COMMAND_DETONATED):
                        strings[stringsIndex] = mf.getName()
                        + Messages
                        .getString("BoardView1.minefield") + "(" + mf.getDensity() + ")"+ " " + owner; //$NON-NLS-1$ //$NON-NLS-2$
                    break;
                    case (Minefield.TYPE_VIBRABOMB):
                        if (mf.getPlayerId() == localPlayer.getId()) {
                            strings[stringsIndex] = mf.getName()
                            + Messages
                            .getString("BoardView1.minefield") + "(" + mf.getDensity() + ")" + "(" + mf.getSetting() + ") " + owner; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        } else {
                            strings[stringsIndex] = mf.getName()
                            + Messages
                            .getString("BoardView1.minefield") + "(" + mf.getDensity() + ")" + " " + owner; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    break;
                    case (Minefield.TYPE_ACTIVE):
                        strings[stringsIndex] = mf.getName()
                        + Messages
                        .getString("BoardView1.minefield") + "(" + mf.getDensity() + ")" + owner; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    break;
                    case (Minefield.TYPE_INFERNO):
                        strings[stringsIndex] = mf.getName()
                        + Messages
                        .getString("BoardView1.minefield") + "(" + mf.getDensity() + ")" + owner; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    break;
                    }
                    stringsIndex++;
                }
            }
        }
        // check if it's on any entities
        for (EntitySprite eSprite : entitySprites) {
            if (eSprite.isInside(point)) {
                final String[] entityStrings = eSprite.getTooltip();
                System.arraycopy(entityStrings, 0, strings, stringsIndex,
                        entityStrings.length);
                stringsIndex += entityStrings.length;
            }
        }

        // check if it's on any attacks
        for (AttackSprite aSprite : attackSprites) {
            if (aSprite.isInside(point)) {
                final String[] attackStrings = aSprite.getTooltip();
                System.arraycopy(attackStrings, 0, strings, stringsIndex,
                        attackStrings.length);
                stringsIndex += 1 + aSprite.weaponDescs.size();
            }
        }

        // check artillery attacks
        for (ArtilleryAttackAction aaa : artilleryAttacks) {
            final Entity ae = game.getEntity(aaa.getEntityId());
            String s = null;
            if (ae != null) {
                if (aaa.getWeaponId() > -1) {
                    Mounted weap = ae.getEquipment(aaa.getWeaponId());
                    s = weap.getName();
                    if (aaa.getAmmoId() > -1) {
                        Mounted ammo = ae.getEquipment(aaa.getAmmoId());
                        s += "(" + ammo.getName() + ")";
                    }
                }
            }
            if (s == null) {
                s = Messages.getString("BoardView1.Artillery");
            }
            strings[stringsIndex++] = Messages.getString(
                    "BoardView1.ArtilleryAttack", new Object[] { s,
                            new Integer(aaa.turnsTilHit),
                            aaa.toHit(game).getValueAsString() });
        }

        // check artillery fire adjustment
        if ((curWeapon != null) && (selectedEntity != null)) {
            // process targetted hexes
            int amod = 0;
            // Check the predesignated hexes
            if (selectedEntity.getOwner().getArtyAutoHitHexes().contains(
                    mcoords)) {
                amod = TargetRoll.AUTOMATIC_SUCCESS;
            } else {
                amod = selectedEntity.aTracker.getModifier(curWeapon, mcoords);
            }

            if (amod == TargetRoll.AUTOMATIC_SUCCESS) {
                strings[stringsIndex++] = Messages
                .getString("BoardView1.ArtilleryAutohit");
            } else {
                strings[stringsIndex++] = Messages.getString(
                        "BoardView1.ArtilleryAdjustment",
                        new Object[] { new Integer(amod) });
            }
        }
        return strings;
    }

    /**
     * Hides the tooltip thinger
     */
    public void hideTooltip() {
        tipWindow.setVisible(false);
    }

    /**
     * Returns true if the tooltip is showing
     */
    private boolean isTipShowing() {
        return tipWindow.isShowing();
    }

    /**
     * Checks if the mouse has been idling for a while and if so, shows the
     * tooltip window
     */
    void checkTooltip() {
        if (isTipShowing()) {
            if (!isTipPossible) {
                hideTooltip();
            }
        } else if (isTipPossible
                && (System.currentTimeMillis() - lastIdle > GUIPreferences
                .getInstance().getTooltipDelay())) {
            showTooltip();
        }
    }

    public void redrawMovingEntity(Entity entity, Coords position, int facing) {
        Integer entityId = new Integer(entity.getId());
        EntitySprite sprite = entitySpriteIds.get(entityId);
        ArrayList<EntitySprite> newSprites;
        HashMap<Integer, EntitySprite> newSpriteIds;

        if (sprite != null) {
            newSprites = new ArrayList<EntitySprite>(entitySprites);
            newSpriteIds = new HashMap<Integer, EntitySprite>(entitySpriteIds);

            newSprites.remove(sprite);

            entitySprites = newSprites;
            entitySpriteIds = newSpriteIds;
        }

        MovingEntitySprite mSprite = movingEntitySpriteIds.get(entityId);
        ArrayList<MovingEntitySprite> newMovingSprites = new ArrayList<MovingEntitySprite>(
                movingEntitySprites);
        HashMap<Integer, MovingEntitySprite> newMovingSpriteIds = new HashMap<Integer, MovingEntitySprite>(
                movingEntitySpriteIds);

        if (mSprite != null) {
            newMovingSprites.remove(mSprite);
        }

        if (entity.getPosition() != null) {
            mSprite = new MovingEntitySprite(entity, position, facing);
            newMovingSprites.add(mSprite);
            newMovingSpriteIds.put(entityId, mSprite);
        }

        movingEntitySprites = newMovingSprites;
        movingEntitySpriteIds = newMovingSpriteIds;
    }

    public boolean isMovingUnits() {
        return movingUnits.size() > 0;
    }

    /**
     * Clears the sprite for an entity and prepares it to be re-drawn. Replaces
     * the old sprite with the new! Try to prevent annoying
     * ConcurrentModificationExceptions
     */
    public synchronized void redrawEntity(Entity entity) {
        Integer entityId = new Integer(entity.getId());
        EntitySprite sprite = entitySpriteIds.get(entityId);
        ArrayList<EntitySprite> newSprites = new ArrayList<EntitySprite>(
                entitySprites);
        HashMap<Integer, EntitySprite> newSpriteIds = new HashMap<Integer, EntitySprite>(
                entitySpriteIds);

        if (sprite != null) {
            newSprites.remove(sprite);
        }
        Coords position = entity.getPosition();
        if (position != null) {
            sprite = new EntitySprite(entity);
            newSprites.add(sprite);
            newSpriteIds.put(entityId, sprite);
        }

        entitySprites = newSprites;
        entitySpriteIds = newSpriteIds;
        for (Iterator<C3Sprite> i = C3Sprites.iterator(); i.hasNext();) {
            final C3Sprite c3sprite = i.next();
            if ((c3sprite.entityId == entity.getId())
                    || (c3sprite.masterId == entity.getId())) {
                i.remove();
            }
        }

        if (entity.hasC3() || entity.hasC3i()) {
            addC3Link(entity);
        }

        scheduleRedraw();
    }

    /**
     * Clears all old entity sprites out of memory and sets up new ones.
     */
    void redrawAllEntities() {
        ArrayList<EntitySprite> newSprites = new ArrayList<EntitySprite>(game
                .getNoOfEntities());
        HashMap<Integer, EntitySprite> newSpriteIds = new HashMap<Integer, EntitySprite>(
                game.getNoOfEntities());
        ArrayList<WreckSprite> newWrecks = new ArrayList<WreckSprite>();

        Enumeration<Entity> e = game.getWreckedEntities();
        while (e.hasMoreElements()) {
            Entity entity = e.nextElement();
            if (!(entity instanceof Infantry) && (entity.getPosition() != null)) {
                WreckSprite ws = new WreckSprite(entity);
                newWrecks.add(ws);
            }
        }

        clearC3Networks();
        for (Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            final Entity entity = i.nextElement();
            if (entity.getPosition() == null) {
                continue;
            }

            EntitySprite sprite = new EntitySprite(entity);
            newSprites.add(sprite);
            newSpriteIds.put(new Integer(entity.getId()), sprite);

            if (entity.hasC3() || entity.hasC3i()) {
                addC3Link(entity);
            }
        }

        entitySprites = newSprites;
        entitySpriteIds = newSpriteIds;
        wreckSprites = newWrecks;

        scheduleRedraw();
    }

    /**
     * Moves the cursor to the new position, or hides it, if newPos is null
     */
    private void moveCursor(CursorSprite cursor, Coords newPos) {
        final Rectangle oldBounds = new Rectangle(cursor.getBounds());
        if (newPos != null) {
            // cursor.setLocation(getHexLocation(newPos));
            cursor.setHexLocation(newPos);
        } else {
            cursor.setOffScreen();
        }
        // repaint affected area
        repaintBounds(oldBounds);
        repaintBounds(cursor.getBounds());
    }

    public void centerOnHex(Coords c) {
        if (null == c) {
            return;
        }
        scroll.setLocation(getHexLocation(c));
        scroll.translate((int) (42 * scale) - (view.width / 2),
                (int) (36 * scale) - (view.height / 2));

        isScrolling = false;
        checkScrollBounds();
        repaint();
    }

    /**
     * Clears the old movement data and draws the new. Since it's less expensive
     * to check for and reuse old step sprites than to make a whole new one, we
     * do that.
     */
    public void drawMovementData(Entity entity, MovePath md) {
        ArrayList<StepSprite> temp = pathSprites;
        MoveStep previousStep = null;

        clearMovementData();

        //need to update the movement sprites based on the move path for this entity
        //only way to do this is to clear and refresh (seems wasteful)

        //first get the color for the vector
        Color col = Color.blue;
        if(md.getLastStep() != null) {
            switch (md.getLastStep().getMovementType()) {
            case MOVE_RUN:
            case MOVE_VTOL_RUN:
            case MOVE_OVER_THRUST:
                col = GUIPreferences.getInstance().getColor("AdvancedMoveRunColor");
                break;
            case MOVE_JUMP :
                col = GUIPreferences.getInstance().getColor("AdvancedMoveJumpColor");
                break;
            case MOVE_SPRINT :
                col = GUIPreferences.getInstance().getColor("AdvancedMoveSprintColor");
                break;
            case MOVE_ILLEGAL :
                col = GUIPreferences.getInstance().getColor("AdvancedMoveIllegalColor");
                break;
            default :
                col = GUIPreferences.getInstance().getColor("AdvancedMoveDefaultColor");
            break;
            }
        }

        refreshMoveVectors(entity, md, col);

        for (Enumeration<MoveStep> i = md.getSteps(); i.hasMoreElements();) {
            final MoveStep step = i.nextElement();
            // check old movement path for reusable step sprites
            boolean found = false;
            for (StepSprite sprite : temp) {
                if (sprite.getStep().canReuseSprite(step) && !(entity instanceof Aero)) {
                    pathSprites.add(sprite);
                    found = true;
                }
            }
            if (!found) {
                if ((null != previousStep) &&
                        ((step.getType() == MoveStepType.UP) ||
                                (step.getType() == MoveStepType.DOWN) ||
                                (step.getType() == MoveStepType.ACC) ||
                                (step.getType() == MoveStepType.DEC) ||
                                (step.getType() == MoveStepType.ACCN) ||
                                (step.getType() == MoveStepType.DECN))) {
                    //Mark the previous elevation change sprite hidden
                    // so that we can draw a new one in it's place without
                    // having overlap.
                    pathSprites.get(pathSprites.size() -1 ).hidden = true;
                }

                //for advanced movement, we always need to hide prior
                //because costs will overlap and we only want the current facing
                if((previousStep != null) && game.useVectorMove()) {
                    pathSprites.get(pathSprites.size() -1 ).hidden = true;
                }

                pathSprites.add(new StepSprite(step));
            }
            previousStep = step;
        }
        repaint(100);
    }

    /**
     * Clears current movement data from the screen
     */
    public void clearMovementData() {
        ArrayList<StepSprite> temp = pathSprites;
        pathSprites = new ArrayList<StepSprite>();
        for (Sprite sprite : temp) {
            repaintBounds(sprite.getBounds());
        }
        refreshMoveVectors();
    }

    public void setLocalPlayer(Player p) {
        localPlayer = p;
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    /**
     * Specifies that this should mark the deployment hexes for a player. If the
     * player is set to null, no hexes will be marked.
     */
    public void markDeploymentHexesFor(Entity ce) {
        en_Deployer = ce;
    }

    /**
     * Adds a c3 line to the sprite list.
     */
    public void addC3Link(Entity e) {
        if (e.getPosition() == null) {
            return;
        }

        if (e.hasC3i()) {
            for (java.util.Enumeration<Entity> i = game.getEntities(); i
            .hasMoreElements();) {
                final Entity fe = i.nextElement();
                if (fe.getPosition() == null) {
                    return;
                }
                if (e.onSameC3NetworkAs(fe) && !fe.equals(e) &&
                        !Compute.isAffectedByECM(e, e.getPosition(), fe.getPosition())) {
                    C3Sprites.add(new C3Sprite(e, fe));
                }
            }
        } else if (e.getC3Master() != null) {
            Entity eMaster = e.getC3Master();
            if (eMaster.getPosition() == null) {
                return;
            }

            // ECM cuts off the network
            if (!Compute.isAffectedByECM(e, e.getPosition(), eMaster
                    .getPosition())
                    && !Compute.isAffectedByECM(eMaster, eMaster.getPosition(),
                            eMaster.getPosition())) {
                C3Sprites.add(new C3Sprite(e, e.getC3Master()));
            }
        }
    }

    /**
     * Adds an attack to the sprite list.
     */
    public synchronized void addAttack(AttackAction aa) {
        // do not make a sprite unless we're aware of both entities
        // this is not a great solution but better than a crash
        Entity ae = game.getEntity(aa.getEntityId());
        Targetable t = game.getTarget(aa.getTargetType(), aa.getTargetId());
        if ((ae == null) || (t == null)
                || (t.getTargetType() == Targetable.TYPE_INARC_POD)
                || (t.getPosition() == null) || (ae.getPosition() == null)) {
            return;
        }

        for (AttackSprite sprite : attackSprites) {
            // can we just add this attack to an existing one?
            if ((sprite.getEntityId() == aa.getEntityId())
                    && (sprite.getTargetId() == aa.getTargetId())) {
                // use existing attack, but add this weapon
                if (aa instanceof WeaponAttackAction) {
                    WeaponAttackAction waa = (WeaponAttackAction) aa;
                    if (aa.getTargetType() != Targetable.TYPE_HEX_ARTILLERY) {
                        sprite.addWeapon(waa);
                    } else if (waa.getEntity(game).getOwner().getId() == localPlayer
                            .getId()) {
                        sprite.addWeapon(waa);
                    }
                }
                if (aa instanceof KickAttackAction) {
                    sprite.addWeapon((KickAttackAction) aa);
                }
                if (aa instanceof PunchAttackAction) {
                    sprite.addWeapon((PunchAttackAction) aa);
                }
                if (aa instanceof PushAttackAction) {
                    sprite.addWeapon((PushAttackAction) aa);
                }
                if (aa instanceof ClubAttackAction) {
                    sprite.addWeapon((ClubAttackAction) aa);
                }
                if (aa instanceof ChargeAttackAction) {
                    sprite.addWeapon((ChargeAttackAction) aa);
                }
                if (aa instanceof DfaAttackAction) {
                    sprite.addWeapon((DfaAttackAction) aa);
                }
                if (aa instanceof ProtomechPhysicalAttackAction) {
                    sprite.addWeapon((ProtomechPhysicalAttackAction) aa);
                }
                if (aa instanceof SearchlightAttackAction) {
                    sprite.addWeapon((SearchlightAttackAction) aa);
                }
                return;
            }
        }
        // no re-use possible, add a new one
        // don't add a sprite for an artillery attack made by the other player
        if (aa instanceof WeaponAttackAction) {
            WeaponAttackAction waa = (WeaponAttackAction) aa;
            if (aa.getTargetType() != Targetable.TYPE_HEX_ARTILLERY) {
                attackSprites.add(new AttackSprite(aa));
            } else if (waa.getEntity(game).getOwner().getId() == localPlayer
                    .getId()) {
                attackSprites.add(new AttackSprite(aa));
            }
        } else {
            attackSprites.add(new AttackSprite(aa));
        }
    }

    /** Removes all attack sprites from a certain entity */
    public synchronized void removeAttacksFor(Entity e) {
        if (e == null) {
            return;
        }
        int entityId = e.getId();
        for (Iterator<AttackSprite> i = attackSprites.iterator(); i.hasNext();) {
            AttackSprite sprite = i.next();
            if (sprite.getEntityId() == entityId) {
                i.remove();
            }
        }
        repaint(100);
    }

    /**
     * Clears out all attacks and re-adds the ones in the current game.
     */
    public void refreshAttacks() {
        clearAllAttacks();
        for (Enumeration<EntityAction> i = game.getActions(); i
        .hasMoreElements();) {
            EntityAction ea = i.nextElement();
            if (ea instanceof AttackAction) {
                addAttack((AttackAction) ea);
            }
        }
        for (Enumeration<AttackAction> i = game.getCharges(); i
        .hasMoreElements();) {
            AttackAction ea = i.nextElement();
            if (ea instanceof PhysicalAttackAction) {
                addAttack(ea);
            }
        }
    }

    public void refreshMoveVectors() {
        clearAllMoveVectors();
        for(Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            Entity e = i.nextElement();
            if(e.getPosition() != null) {
                movementSprites.add(new MovementSprite(e, e.getVectors(), Color.gray, false));
            }
        }
    }

    public void refreshMoveVectors(Entity en, MovePath md, Color col) {
        clearAllMoveVectors();
        //same as normal but when I find the active entity I used the MovePath
        //to get vector
        for(Enumeration<Entity> i = game.getEntities(); i.hasMoreElements();) {
            Entity e = i.nextElement();
            if(e.getPosition() != null) {
                if(e.getId() == en.getId()) {
                    movementSprites.add(new MovementSprite(e, md.getFinalVectors(), col, true));
                } else {
                    movementSprites.add(new MovementSprite(e, e.getVectors(), col, false));
                }
            }
        }
    }

    public void clearC3Networks() {
        C3Sprites.clear();
    }

    /**
     * Clears out all attacks that were being drawn
     */
    public void clearAllAttacks() {
        attackSprites.clear();
    }

    /**
     * Clears out all movement vectors that were being drawn
     */
    public void clearAllMoveVectors() {
        movementSprites.clear();
    }

    protected void firstLOSHex(Coords c) {
        if (useLOSTool) {
            moveCursor(secondLOSSprite, null);
            moveCursor(firstLOSSprite, c);
        }
    }

    protected void secondLOSHex(Coords c2, Coords c1) {
        if (useLOSTool) {
            moveCursor(firstLOSSprite, c1);
            moveCursor(secondLOSSprite, c2);
            Entity ae = chooseEntity(c1);
            Entity te = chooseEntity(c2);

            StringBuffer message = new StringBuffer();
            LosEffects le;
            if ((ae == null) || (te == null)) {
                boolean mechInFirst = GUIPreferences.getInstance()
                .getMechInFirst();
                boolean mechInSecond = GUIPreferences.getInstance()
                .getMechInSecond();
                LosEffects.AttackInfo ai = new LosEffects.AttackInfo();
                ai.attackPos = c1;
                ai.targetPos = c2;
                ai.attackHeight = mechInFirst ? 1 : 0;
                ai.targetHeight = mechInSecond ? 1 : 0;
                ai.attackAbsHeight = game.getBoard().getHex(c1).floor()
                + ai.attackHeight;
                ai.targetAbsHeight = game.getBoard().getHex(c2).floor()
                + ai.targetHeight;
                le = LosEffects.calculateLos(game, ai);
                message
                .append(Messages
                        .getString(
                                "BoardView1.Attacker", new Object[] { //$NON-NLS-1$
                                        mechInFirst ? Messages
                                                .getString("BoardView1.Mech") : Messages.getString("BoardView1.NonMech"), //$NON-NLS-1$ //$NON-NLS-2$
                                                c1.getBoardNum() }));
                message
                .append(Messages
                        .getString(
                                "BoardView1.Target", new Object[] { //$NON-NLS-1$
                                        mechInSecond ? Messages
                                                .getString("BoardView1.Mech") : Messages.getString("BoardView1.NonMech"), //$NON-NLS-1$ //$NON-NLS-2$
                                                c2.getBoardNum() }));
            } else {
                le = LosEffects.calculateLos(game, ae.getId(), te);
                message.append(Messages.getString(
                        "BoardView1.Attacker", new Object[] { //$NON-NLS-1$
                                ae.getDisplayName(), c1.getBoardNum() }));
                message.append(Messages.getString(
                        "BoardView1.Target", new Object[] { //$NON-NLS-1$
                                te.getDisplayName(), c2.getBoardNum() }));
            }
            if (le.isBlocked()) {
                message.append(Messages.getString(
                        "BoardView1.LOSBlocked", new Object[] { //$NON-NLS-1$
                                new Integer(c1.distance(c2)) }));
            } else {
                message.append(Messages.getString(
                        "BoardView1.LOSNotBlocked", new Object[] { //$NON-NLS-1$
                                new Integer(c1.distance(c2)) }));
                if (le.getHeavyWoods() > 0) {
                    message.append(Messages.getString(
                            "BoardView1.HeavyWoods", new Object[] { //$NON-NLS-1$
                                    new Integer(le.getHeavyWoods()) }));
                }
                if (le.getLightWoods() > 0) {
                    message.append(Messages.getString(
                            "BoardView1.LightWoods", new Object[] { //$NON-NLS-1$
                                    new Integer(le.getLightWoods()) }));
                }
                if (le.getLightSmoke() > 0) {
                    message.append(Messages.getString(
                            "BoardView1.LightSmoke", new Object[] { //$NON-NLS-1$
                                    new Integer(le.getLightSmoke()) }));
                }
                if (le.getHeavySmoke() > 0) {
                    message.append(Messages.getString(
                            "BoardView1.HeavySmoke", new Object[] { //$NON-NLS-1$
                                    new Integer(le.getHeavySmoke()) }));
                }
                if (le.isTargetCover()) {
                    message.append(Messages
                            .getString("BoardView1.TargetPartialCover")); //$NON-NLS-1$
                }
                if (le.isAttackerCover()) {
                    message.append(Messages
                            .getString("BoardView1.AttackerPartialCover")); //$NON-NLS-1$
                }
            }
            AlertDialog alert = new AlertDialog((Frame)SwingUtilities.getAncestorOfClass(Frame.class, this), Messages
                    .getString("BoardView1.LOSTitle"), //$NON-NLS-1$
                    message.toString(), false);
            alert.setVisible(true);
        }
    }

    /**
     * If the mouse is at the edges of the screen, this scrolls the board image
     * on the canvas. NOTE: CTL scroll is handled in mouseMoved()
     */
    public boolean doScroll() {
        final Point oldScroll = new Point(scroll);
        boolean s = false;

        if (isScrolling && GUIPreferences.getInstance().getRightDragScroll()) {
            if (!((oldMousePosition == null) || mousePos.equals(oldMousePosition))) {
                scroll.x -= GUIPreferences.getInstance().getScrollSensitivity()
                * (mousePos.x - oldMousePosition.x);
                scroll.y -= GUIPreferences.getInstance().getScrollSensitivity()
                * (mousePos.y - oldMousePosition.y);
                checkScrollBounds();
                oldMousePosition.setLocation(mousePos);
                s = !oldScroll.equals(scroll);
                scrolled = scrolled || s;
            }
        }

        if (isScrolling
                && (GUIPreferences.getInstance().getClickEdgeScroll() || GUIPreferences
                        .getInstance().getAutoEdgeScroll())) {
            final int sf = GUIPreferences.getInstance().getScrollSensitivity(); // scroll
            // factor
            // adjust x scroll
            // scroll when the mouse is at the edges
            if (mousePos.x < 100) {
                scroll.x -= (100 - mousePos.x) / sf;
            } else if (mousePos.x > (backSize.width - 100)) {
                scroll.x -= ((backSize.width - 100) - mousePos.x) / sf;
            }
            // scroll when the mouse is at the edges
            if (mousePos.y < 100) {
                scroll.y -= (100 - mousePos.y) / sf;
            } else if (mousePos.y > (backSize.height - 100)) {
                scroll.y -= ((backSize.height - 100) - mousePos.y) / sf;
            }
            checkScrollBounds();
            if (!oldScroll.equals(scroll)) {
                // repaint();
                s = true;
                scrolled = s;
            }
        }

        return s;
    }

    /**
     * Makes sure that the scroll dimensions stay in bounds
     */
    public void checkScrollBounds() {
        if (scroll.x < 0) {
            scroll.x = 0;
        } else if (scroll.x > (boardSize.width - view.width)) {
            scroll.x = (boardSize.width - view.width);
        }

        if (scroll.y < 0) {
            scroll.y = 0;
        } else if (scroll.y > (boardSize.height - view.height)) {
            scroll.y = (boardSize.height - view.height);
        }

        // Update our scroll bars.
        if (null != vScrollbar) {
            vScrollbar.setValue(scroll.y);
        }
        if (null != hScrollbar) {
            hScrollbar.setValue(scroll.x);
        }
    }

    protected void stopScrolling() {
        isScrolling = false;
    }

    /**
     * Initializes the various overlay polygons with their vertices.
     */
    public void initPolys() {
        // hex polygon
        hexPoly = new Polygon();
        hexPoly.addPoint(21, 0);
        hexPoly.addPoint(62, 0);
        hexPoly.addPoint(83, 35);
        hexPoly.addPoint(83, 36);
        hexPoly.addPoint(62, 71);
        hexPoly.addPoint(21, 71);
        hexPoly.addPoint(0, 36);
        hexPoly.addPoint(0, 35);

        // facing polygons
        facingPolys = new Polygon[6];
        facingPolys[0] = new Polygon();
        facingPolys[0].addPoint(41, 3);
        facingPolys[0].addPoint(38, 6);
        facingPolys[0].addPoint(45, 6);
        facingPolys[0].addPoint(42, 3);
        facingPolys[1] = new Polygon();
        facingPolys[1].addPoint(69, 17);
        facingPolys[1].addPoint(64, 17);
        facingPolys[1].addPoint(68, 23);
        facingPolys[1].addPoint(70, 19);
        facingPolys[2] = new Polygon();
        facingPolys[2].addPoint(69, 53);
        facingPolys[2].addPoint(68, 49);
        facingPolys[2].addPoint(64, 55);
        facingPolys[2].addPoint(68, 54);
        facingPolys[3] = new Polygon();
        facingPolys[3].addPoint(41, 68);
        facingPolys[3].addPoint(38, 65);
        facingPolys[3].addPoint(45, 65);
        facingPolys[3].addPoint(42, 68);
        facingPolys[4] = new Polygon();
        facingPolys[4].addPoint(15, 53);
        facingPolys[4].addPoint(18, 54);
        facingPolys[4].addPoint(15, 48);
        facingPolys[4].addPoint(14, 52);
        facingPolys[5] = new Polygon();
        facingPolys[5].addPoint(13, 19);
        facingPolys[5].addPoint(15, 23);
        facingPolys[5].addPoint(19, 17);
        facingPolys[5].addPoint(17, 17);

        // movement polygons
        movementPolys = new Polygon[8];
        movementPolys[0] = new Polygon();
        movementPolys[0].addPoint(41, 65);
        movementPolys[0].addPoint(38, 68);
        movementPolys[0].addPoint(45, 68);
        movementPolys[0].addPoint(42, 65);
        movementPolys[1] = new Polygon();
        movementPolys[1].addPoint(17, 48);
        movementPolys[1].addPoint(12, 48);
        movementPolys[1].addPoint(16, 54);
        movementPolys[1].addPoint(17, 49);
        movementPolys[2] = new Polygon();
        movementPolys[2].addPoint(18, 19);
        movementPolys[2].addPoint(17, 15);
        movementPolys[2].addPoint(13, 21);
        movementPolys[2].addPoint(17, 20);
        movementPolys[3] = new Polygon();
        movementPolys[3].addPoint(41, 6);
        movementPolys[3].addPoint(38, 3);
        movementPolys[3].addPoint(45, 3);
        movementPolys[3].addPoint(42, 6);
        movementPolys[4] = new Polygon();
        movementPolys[4].addPoint(67, 15);
        movementPolys[4].addPoint(66, 19);
        movementPolys[4].addPoint(67, 20);
        movementPolys[4].addPoint(71, 20);
        movementPolys[5] = new Polygon();
        movementPolys[5].addPoint(69, 55);
        movementPolys[5].addPoint(66, 50);
        movementPolys[5].addPoint(67, 49);
        movementPolys[5].addPoint(72, 48);

        movementPolys[6] = new Polygon(); // up arrow with tail
        movementPolys[6].addPoint(35, 44);
        movementPolys[6].addPoint(30, 49);
        movementPolys[6].addPoint(33, 49);
        movementPolys[6].addPoint(33, 53);
        movementPolys[6].addPoint(38, 53);
        movementPolys[6].addPoint(38, 49);
        movementPolys[6].addPoint(41, 49);
        movementPolys[6].addPoint(36, 44);
        movementPolys[7] = new Polygon(); // down arrow with tail
        movementPolys[7].addPoint(34, 53);
        movementPolys[7].addPoint(29, 48);
        movementPolys[7].addPoint(32, 48);
        movementPolys[7].addPoint(32, 44);
        movementPolys[7].addPoint(37, 44);
        movementPolys[7].addPoint(37, 48);
        movementPolys[7].addPoint(40, 48);
        movementPolys[7].addPoint(35, 53);
    }

    synchronized boolean doMoveUnits(long idleTime) {
        boolean movingSomething = false;

        if (movingUnits.size() > 0) {

            moveWait += idleTime;

            if (moveWait > GUIPreferences.getInstance().getInt(
            "AdvancedMoveStepDelay")) {

                ArrayList<MovingUnit> spent = new ArrayList<MovingUnit>();

                for (MovingUnit move : movingUnits) {
                    movingSomething = true;
                    Entity ge = game.getEntity(move.entity.getId());
                    if (move.path.size() > 0) {
                        UnitLocation loc = move.path.get(0);
                        if (ge != null) {
                            redrawMovingEntity(move.entity, loc.getCoords(),
                                    loc.getFacing());
                        }
                        move.path.remove(0);
                    } else {
                        if (ge != null) {
                            redrawEntity(ge);
                        }
                        spent.add(move);
                    }

                }

                for (MovingUnit move : spent) {
                    movingUnits.remove(move);
                }
                moveWait = 0;

                if (movingUnits.size() == 0) {
                    movingEntitySpriteIds.clear();
                    movingEntitySprites.clear();
                    ghostEntitySprites.clear();
                    processBoardViewEvent(new BoardViewEvent(this,
                            BoardViewEvent.FINISHED_MOVING_UNITS));
                }
            }
        }
        return movingSomething;
    }

    //
    // KeyListener
    //
    public void keyPressed(KeyEvent ke) {
        switch (ke.getKeyCode()) {
        case KeyEvent.VK_NUMPAD7:
            scroll.y -= HEX_H * scale;
            scroll.x -= HEX_W * scale;
            break;
        case KeyEvent.VK_NUMPAD8:
        case KeyEvent.VK_UP:
            scroll.y -= HEX_H * scale;
            break;
        case KeyEvent.VK_NUMPAD9:
            scroll.y -= HEX_H * scale;
            scroll.x += HEX_W * scale;
            break;
        case KeyEvent.VK_NUMPAD1:
            scroll.y += HEX_H * scale;
            scroll.x -= HEX_W * scale;
            break;
        case KeyEvent.VK_NUMPAD2:
        case KeyEvent.VK_DOWN:
            scroll.y += HEX_H * scale;
            break;
        case KeyEvent.VK_NUMPAD3:
            scroll.y += HEX_H * scale;
            scroll.x += HEX_W * scale;
            break;
        case KeyEvent.VK_NUMPAD4:
        case KeyEvent.VK_LEFT:
            scroll.x -= HEX_W * scale;
            break;
        case KeyEvent.VK_NUMPAD6:
        case KeyEvent.VK_RIGHT:
            scroll.x += HEX_W * scale;
            break;
        case KeyEvent.VK_NUMPAD5:
            // center on the selected entity
            if (selectedEntity != null) {
                centerOnHex(selectedEntity.getPosition());
            }
            break;
        case KeyEvent.VK_CONTROL:
            ctlKeyHeld = true;
            initCtlScroll = true;
            break;
        case KeyEvent.VK_PAGE_DOWN:
            zoomIn();
            break;
        case KeyEvent.VK_PAGE_UP:
            zoomOut();
            break;
        }

        if (isTipShowing()) {
            hideTooltip();
        }
        lastIdle = System.currentTimeMillis();
        checkScrollBounds();
        repaint();
    }

    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctlKeyHeld = false;
        }
    }

    public void keyTyped(KeyEvent ke) {
    }

    //
    // MouseListener
    //
    public void mousePressed(MouseEvent me) {
        scrolled = false; // not scrolled yet

        Point point = me.getPoint();
        if (null == point) {
            return;
        }
        oldMousePosition = point;

        isTipPossible = false;
        for (int i = 0; i < displayables.size(); i++) {
            IDisplayable disp = displayables.get(i);
            Point dispPoint = new Point();
            dispPoint.x = point.x - offset.x;
            dispPoint.y = point.y - offset.y;
            if ((backSize != null) && (disp.isHit(dispPoint, backSize))) {
                return;
            }
        }

        if (me.isPopupTrigger()) {
            mouseAction(getCoordsAt(point), BOARD_HEX_POPUP, me.getModifiers());
            return;
        }

        // Disable scrolling when ctrl or alt is held down, since this
        // means the user wants to use the LOS/ruler tools.
        int mask = InputEvent.CTRL_MASK | InputEvent.ALT_MASK;
        if (!GUIPreferences.getInstance().getRightDragScroll()
                && !GUIPreferences.getInstance().getAlwaysRightClickScroll()
                && ((game.getPhase() == IGame.Phase.PHASE_FIRING) || (game.getPhase() == IGame.Phase.PHASE_OFFBOARD))) {
            // In the firing phase, also disable scrolling if
            // the right or middle buttons are clicked, since
            // this means the user wants to activate the
            // popup menu or ruler tool.
            mask |= InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK;
        }

        // disable auto--edge-scrolling if no option set
        if (!GUIPreferences.getInstance().getAutoEdgeScroll()) {
            mask |= InputEvent.BUTTON1_MASK;
        }
        // disable edge-scrolling if no option set
        if (!GUIPreferences.getInstance().getClickEdgeScroll()) {
            mask |= InputEvent.BUTTON3_MASK;
        }

        if (GUIPreferences.getInstance().getRightDragScroll()) {
            mask |= InputEvent.BUTTON2_MASK;
        }

        if ((me.getModifiers() & mask) == 0) {
            isScrolling = true; // activate scrolling
        } else {
            isScrolling = false; // activate scrolling
        }

        if (isTipShowing()) {
            hideTooltip();
        }

        mouseAction(getCoordsAt(point), BOARD_HEX_DRAG, me.getModifiers());
    }

    public void mouseReleased(MouseEvent me) {
        isTipPossible = true;
        oldMousePosition = mousePos;

        for (int i = 0; i < displayables.size(); i++) {
            IDisplayable disp = displayables.get(i);
            if (disp.isReleased()) {
                return;
            }
        }
        isScrolling = false;

        // Unless the user has auto-scroll on and is using the left
        // mouse button, no click action should be triggered if the map
        // is being scrolled.
        if (scrolled
                && (((me.getModifiers() & InputEvent.BUTTON1_MASK) == 0) || !GUIPreferences
                        .getInstance().getAutoEdgeScroll())) {
            return;
        }

        if (me.isPopupTrigger()) {
            mouseAction(getCoordsAt(me.getPoint()), BOARD_HEX_POPUP, me.getModifiers());
            return;
        }

        if (me.getClickCount() == 1) {
            mouseAction(getCoordsAt(me.getPoint()), BOARD_HEX_CLICK, me
                    .getModifiers());
        } else {
            mouseAction(getCoordsAt(me.getPoint()), BOARD_HEX_DOUBLECLICK, me
                    .getModifiers());
        }
    }

    public void mouseEntered(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
        isTipPossible = false;
    }

    public void mouseClicked(MouseEvent me) {
        if (me.isPopupTrigger()) {
            mouseAction(getCoordsAt(me.getPoint()), BOARD_HEX_POPUP, me.getModifiers());
            return;
        }
    }

    //
    // MouseMotionListener
    //
    public void mouseDragged(MouseEvent me) {
        isTipPossible = false;

        Point point = me.getPoint();
        if (null == point) {
            return;
        }

        for (int i = 0; i < displayables.size(); i++) {
            IDisplayable disp = displayables.get(i);
            Point dispPoint = new Point();
            dispPoint.x = point.x - offset.x;
            dispPoint.y = point.y - offset.y;
            if (disp.isDragged(dispPoint, backSize)) {
                repaint();
                return;
            }
        }
        mousePos = point;

        // Disable scrolling when ctrl or alt is held down, since this
        // means the user wants to use the LOS/ruler tools.
        int mask = InputEvent.CTRL_MASK | InputEvent.ALT_MASK;

        if (!GUIPreferences.getInstance().getRightDragScroll()
                && !GUIPreferences.getInstance().getAlwaysRightClickScroll()
                && ((game.getPhase() == IGame.Phase.PHASE_FIRING) || (game.getPhase() == IGame.Phase.PHASE_OFFBOARD))) {
            // In the firing phase, also disable scrolling if
            // the right or middle buttons are clicked, since
            // this means the user wants to activate the
            // popup menu or ruler tool.
            mask |= InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK;
        }

        if (GUIPreferences.getInstance().getRightDragScroll()) {
            mask |= InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK;
        }

        // disable auto--edge-scrolling if no option set
        if (!GUIPreferences.getInstance().getAutoEdgeScroll()) {
            mask |= InputEvent.BUTTON1_MASK;
        }

        // disable edge-scrolling if no option set
        if (!GUIPreferences.getInstance().getClickEdgeScroll()
                && !GUIPreferences.getInstance().getRightDragScroll()) {
            mask |= InputEvent.BUTTON3_MASK;
        }

        if ((me.getModifiers() & mask) == 0) {
            isScrolling = true; // activate scrolling
        } else {
            isScrolling = false; // activate scrolling
        }

        mouseAction(getCoordsAt(point), BOARD_HEX_DRAG, me.getModifiers());
    }

    public void mouseMoved(MouseEvent me) {
        Point point = me.getPoint();
        if (null == point) {
            return;
        }

        for (int i = 0; i < displayables.size(); i++) {
            IDisplayable disp = displayables.get(i);
            if (disp.isBeingDragged()) {
                isTipPossible = false;
                return;
            }
            if (backSize != null) {
                disp.isMouseOver(point, backSize);
            }
        }
        mousePos = point;
        if (isTipShowing()) {
            hideTooltip();
        }
        if (ctlKeyHeld && GUIPreferences.getInstance().getCtlScroll()) {
            if (initCtlScroll) {
                previousMouseX = me.getX();
                previousMouseY = me.getY();
                initCtlScroll = false;
            }
            scroll.x += GUIPreferences.getInstance().getScrollSensitivity()
            * (me.getX() - previousMouseX);
            scroll.y += GUIPreferences.getInstance().getScrollSensitivity()
            * (me.getY() - previousMouseY);
            previousMouseX = me.getX();
            previousMouseY = me.getY();
            checkScrollBounds();
            repaint();
        }
        lastIdle = System.currentTimeMillis();
        isTipPossible = true;
    }

    /**
     * Increases zoomIndex and refreshes the map.
     */
    public void zoomIn() {
        if (zoomIndex == ZOOM_FACTORS.length - 1) {
            return;
        }
        zoomIndex++;
        zoom();
    }

    /**
     * Decreases zoomIndex and refreshes the map.
     */
    public void zoomOut() {
        if (zoomIndex == 0) {
            return;
        }
        zoomIndex--;
        zoom();
    }

    /**
     * zoomIndex is a reference to a static array of scale factors. The index
     * ranges from 0 to 9 and by default is set to 7 which corresponds to a
     * scale of 1.0 (draws megamek images at normal size). To zoom out the index
     * needs to be set to a lower value. To zoom in make it larger. If only
     * zooming a step at a time use the zoomIn and zoomOut methods instead.
     *
     * @param zoomIndex
     */
    public void setZoomIndex(int zoomIndex) {
        this.zoomIndex = zoomIndex;
        zoom();
    }

    public int getZoomIndex() {
        return zoomIndex;
    }

    private void checkZoomIndex() {
        if (zoomIndex > ZOOM_FACTORS.length - 1) {
            zoomIndex = ZOOM_FACTORS.length - 1;
        }
        if (zoomIndex < 0) {
            zoomIndex = 0;
        }
    }

    /**
     * Changes hex dimensions and refreshes the map with the new scale
     */
    private void zoom() {
        checkZoomIndex();
        scale = ZOOM_FACTORS[zoomIndex];
        GUIPreferences.getInstance().setMapZoomIndex(zoomIndex);

        hex_size = new Dimension((int) (HEX_W * scale), (int) (HEX_H * scale));

        final Dimension size = getSize();

        scaledImageCache = new ImageCache<Image, Image>();

        cursorSprite.prepare();
        highlightSprite.prepare();
        selectedSprite.prepare();
        firstLOSSprite.prepare();
        secondLOSSprite.prepare();

        updateBoard();

        view.setLocation(scroll);
        view.setSize(getOptimalView(size));
        offset.setLocation(getOptimalOffset(size));

        updateFontSizes();
        updateBoardImage();

        repaint();
    }

    private void updateFontSizes() {
        if (zoomIndex <= 4) {
            font_elev = FONT_7;
            font_hexnum = FONT_7;
            font_minefield = FONT_7;
        }
        if ((zoomIndex <= 5) & (zoomIndex > 4)) {
            font_elev = FONT_8;
            font_hexnum = FONT_8;
            font_minefield = FONT_8;
        }
        if (zoomIndex > 5) {
            font_elev = FONT_9;
            font_hexnum = FONT_9;
            font_minefield = FONT_9;
        }
    }

    private class MovingUnit {
        public Entity entity;
        public ArrayList<UnitLocation> path;

        MovingUnit(Entity entity, Vector<UnitLocation> path) {
            this.entity = entity;
            this.path = new ArrayList<UnitLocation>(path);
        }
    }

    /**
     * Displays a bit of text in a box. TODO: make multi-line
     */
    private class TooltipCanvas extends Canvas {
        private static final long serialVersionUID = 7085249378990745380L;
        private String[] tipStrings;
        private Dimension size;

        public TooltipCanvas(String[] tipStrings) {
            this.tipStrings = tipStrings;

            // setup
            setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
            setBackground(SystemColor.info);
            setForeground(SystemColor.infoText);

            // determine size
            final FontMetrics fm = getFontMetrics(getFont());
            int width = 0;
            for (String tipString : tipStrings) {
                if (fm.stringWidth(tipString) > width) {
                    width = fm.stringWidth(tipString);
                }
            }
            size = new Dimension(width + 5, fm.getAscent() * tipStrings.length
                    + 4);
            setSize(size);
        }

        @Override
        public void paint(Graphics g) {
            final FontMetrics fm = getFontMetrics(getFont());
            g.setColor(getBackground());
            g.fillRect(0, 0, size.width, size.height);
            g.setColor(getForeground());
            g.drawRect(0, 0, size.width - 1, size.height - 1);
            for (int i = 0; i < tipStrings.length; i++) {
                g.drawString(tipStrings[i], 2, (i + 1) * fm.getAscent());
            }
        }
    }

    /**
     * Everything in the main map view is either the board or it's a sprite
     * displayed on top of the board. Most sprites store a transparent image
     * which they draw onto the screen when told to. Sprites keep a bounds
     * rectangle, so it's easy to tell when they'return onscreen.
     */
    abstract class Sprite implements ImageObserver {
        protected Rectangle bounds;
        protected Image image;

        // Set this to true if you don't want the sprite to be drawn.
        protected boolean hidden = false;

        /**
         * Do any necessary preparation. This is called after creation, but
         * before drawing, when a device context is ready to draw with.
         */
        public abstract void prepare();

        /**
         * When we draw our buffered images, it's necessary to implement the
         * ImageObserver interface. This provides the necesasry functionality.
         */
        public boolean imageUpdate(Image image, int infoflags, int x, int y,
                int width, int height) {
            if (infoflags == ImageObserver.ALLBITS) {
                prepare();
                repaint();
                return false;
            }
            return true;
        }

        /**
         * Returns our bounding rectangle. The coordinates here are stored with
         * the top left corner of the _board_ being 0, 0, so these do not always
         * correspond to screen coordinates.
         */
        public Rectangle getBounds() {
            return bounds;
        }

        /**
         * Are we ready to draw? By default, checks to see that our buffered
         * image has been created.
         */
        public boolean isReady() {
            return image != null;
        }

        /**
         * Draws this sprite onto the specified graphics context.
         */
        public void drawOnto(Graphics g, int x, int y, ImageObserver observer) {
            drawOnto(g, x, y, observer, false);
        }

        public void drawOnto(Graphics g, int x, int y, ImageObserver observer,
                boolean makeTranslucent) {
            if (isReady()) {
                if (makeTranslucent) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, 0.5f));
                    g2.drawImage(image, x, y, observer);
                    g2.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, 1.0f));
                } else {
                    g.drawImage(image, x, y, observer);
                }
            } else {
                // grrr... we'll be ready next time!
                prepare();
            }
        }

        /**
         * Returns true if the point is inside this sprite. Uses board
         * coordinates, not screen coordinates. By default, just checks our
         * bounding rectangle, though some sprites override this for a smaller
         * sensitive area.
         */
        public boolean isInside(Point point) {
            return bounds.contains(point);
        }

        /**
         * Since most sprites being drawn correspond to something in the game,
         * this returns a little info for a tooltip.
         */
        public String[] getTooltip() {
            return null;
        }
    }

    /**
     * Sprite for a cursor. Just a hexagon outline in a specified color.
     */
    private class CursorSprite extends Sprite {
        private Color color;
        private Coords hexLoc;

        public CursorSprite(Color color) {
            this.color = color;
            bounds = new Rectangle(hexPoly.getBounds().width + 1, hexPoly
                    .getBounds().height + 1);
            image = null;

            // start offscreen
            setOffScreen();
        }

        @Override
        public void prepare() {
            // create image for buffer
            Image tempImage = createImage(bounds.width, bounds.height);
            Graphics graph = tempImage.getGraphics();

            // fill with key color
            graph.setColor(new Color(TRANSPARENT));
            graph.fillRect(0, 0, bounds.width, bounds.height);
            // draw attack poly
            graph.setColor(color);
            graph.drawPolygon(hexPoly);

            // create final image
            if (zoomIndex == BASE_ZOOM_INDEX) {
                image = createImage(new FilteredImageSource(tempImage
                        .getSource(), new KeyAlphaFilter(TRANSPARENT)));
            } else {
                image = getScaledImage(createImage(new FilteredImageSource(
                        tempImage.getSource(), new KeyAlphaFilter(TRANSPARENT))));
            }
            graph.dispose();
            tempImage.flush();
        }

        public void setOffScreen() {
            bounds.setLocation(-100, -100);
            hexLoc = new Coords(-2, -2);
        }

        public void setHexLocation(Coords hexLoc) {
            this.hexLoc = hexLoc;
            bounds.setLocation(getHexLocation(hexLoc));
        }

        @Override
        public Rectangle getBounds() {
            bounds = new Rectangle(hexPoly.getBounds().width + 1, hexPoly
                    .getBounds().height + 1);
            bounds.setLocation(getHexLocation(hexLoc));

            return bounds;
        }
    }

    private class GhostEntitySprite extends Sprite {
        private Entity entity;
        private Rectangle modelRect;

        public GhostEntitySprite(Entity entity) {
            this.entity = entity;

            String shortName = entity.getShortName();
            Font font = new Font("SansSerif", Font.PLAIN, 10); //$NON-NLS-1$
            modelRect = new Rectangle(47, 55, getFontMetrics(font).stringWidth(
                    shortName) + 1, getFontMetrics(font).getAscent());
            Rectangle tempBounds = new Rectangle(hex_size).union(modelRect);
            tempBounds.setLocation(getHexLocation(entity.getPosition()));

            bounds = tempBounds;
            image = null;
        }

        /**
         * Creates the sprite for this entity. It is an extra pain to create
         * transparent images in AWT.
         */
        @Override
        public void prepare() {
            // create image for buffer
            Image tempImage;
            Graphics graph;
            try {
                tempImage = createImage(bounds.width, bounds.height);
                graph = tempImage.getGraphics();
            } catch (NullPointerException ex) {
                // argh! but I want it!
                return;
            }

            // fill with key color
            graph.setColor(new Color(TRANSPARENT));
            graph.fillRect(0, 0, bounds.width, bounds.height);

            // draw entity image
            graph.drawImage(tileManager.imageFor(entity), 0, 0, this);

            // create final image
            if (zoomIndex == BASE_ZOOM_INDEX) {
                image = createImage(new FilteredImageSource(tempImage
                        .getSource(), new KeyAlphaFilter(TRANSPARENT)));
            } else {
                image = getScaledImage(createImage(new FilteredImageSource(
                        tempImage.getSource(), new KeyAlphaFilter(TRANSPARENT))));
            }
            graph.dispose();
            tempImage.flush();
        }

        @Override
        public Rectangle getBounds() {
            Rectangle tempBounds = new Rectangle(hex_size).union(modelRect);
            tempBounds.setLocation(getHexLocation(entity.getPosition()));
            bounds = tempBounds;

            return bounds;
        }

        @Override
        public void drawOnto(Graphics g, int x, int y, ImageObserver observer) {
            drawOnto(g, x, y, observer, true);
        }

    }

    private class MovingEntitySprite extends Sprite {
        private int facing;
        private Entity entity;
        private Rectangle modelRect;

        public MovingEntitySprite(Entity entity, Coords position, int facing) {
            this.entity = entity;
            this.facing = facing;

            String shortName = entity.getShortName();
            Font font = new Font("SansSerif", Font.PLAIN, 10); //$NON-NLS-1$
            modelRect = new Rectangle(47, 55, getFontMetrics(font).stringWidth(
                    shortName) + 1, getFontMetrics(font).getAscent());
            Rectangle tempBounds = new Rectangle(hex_size).union(modelRect);
            tempBounds.setLocation(getHexLocation(position));

            bounds = tempBounds;
            image = null;
        }

        /**
         * Creates the sprite for this entity. It is an extra pain to create
         * transparent images in AWT.
         */
        @Override
        public void prepare() {
            // create image for buffer
            Image tempImage;
            Graphics graph;
            try {
                tempImage = createImage(bounds.width, bounds.height);
                graph = tempImage.getGraphics();
            } catch (NullPointerException ex) {
                // argh! but I want it!
                return;
            }

            // fill with key color
            graph.setColor(new Color(TRANSPARENT));
            graph.fillRect(0, 0, bounds.width, bounds.height);

            // draw entity image
            graph.drawImage(tileManager.imageFor(entity, facing), 0, 0, this);

            // create final image
            if (zoomIndex == BASE_ZOOM_INDEX) {
                image = createImage(new FilteredImageSource(tempImage
                        .getSource(), new KeyAlphaFilter(TRANSPARENT)));
            } else {
                image = getScaledImage(createImage(new FilteredImageSource(
                        tempImage.getSource(), new KeyAlphaFilter(TRANSPARENT))));
            }
            graph.dispose();
            tempImage.flush();
        }
    }

    /**
     * Sprite for an wreck. Consists of an image, drawn from the Tile Manager
     * and an identification label.
     */
    private class WreckSprite extends Sprite {
        private Entity entity;
        private Rectangle modelRect;

        public WreckSprite(Entity entity) {
            this.entity = entity;

            String shortName = entity.getShortName();

            Font font = new Font("SansSerif", Font.PLAIN, 10); //$NON-NLS-1$
            modelRect = new Rectangle(47, 55, getFontMetrics(font).stringWidth(
                    shortName) + 1, getFontMetrics(font).getAscent());
            Rectangle tempBounds = new Rectangle(hex_size).union(modelRect);
            tempBounds.setLocation(getHexLocation(entity.getPosition()));

            bounds = tempBounds;
            image = null;
        }

        @Override
        public Rectangle getBounds() {
            Rectangle tempBounds = new Rectangle(hex_size).union(modelRect);
            tempBounds.setLocation(getHexLocation(entity.getPosition()));
            bounds = tempBounds;

            return bounds;
        }

        /**
         * Creates the sprite for this entity. It is an extra pain to create
         * transparent images in AWT.
         */
        @Override
        public void prepare() {
            // figure out size
            String shortName = entity.getShortName();
            Font font = new Font("SansSerif", Font.PLAIN, 10); //$NON-NLS-1$
            Rectangle tempRect = new Rectangle(47, 55, getFontMetrics(font)
                    .stringWidth(shortName) + 1, getFontMetrics(font)
                    .getAscent());

            // create image for buffer
            Image tempImage;
            Graphics graph;
            try {
                tempImage = createImage(bounds.width, bounds.height);
                graph = tempImage.getGraphics();
            } catch (NullPointerException ex) {
                // argh! but I want it!
                return;
            }

            // fill with key color
            graph.setColor(new Color(TRANSPARENT));
            graph.fillRect(0, 0, bounds.width, bounds.height);

            // Draw wreck image,if we've got one.
            Image wreck = tileManager.wreckMarkerFor(entity);
            if (null != wreck) {
                graph.drawImage(wreck, 0, 0, this);
            }

            // draw box with shortName
            Color text = Color.lightGray;
            Color bkgd = Color.darkGray;
            Color bord = Color.black;

            graph.setFont(font);
            graph.setColor(bord);
            graph.fillRect(tempRect.x, tempRect.y, tempRect.width,
                    tempRect.height);
            tempRect.translate(-1, -1);
            graph.setColor(bkgd);
            graph.fillRect(tempRect.x, tempRect.y, tempRect.width,
                    tempRect.height);
            graph.setColor(text);
            graph.drawString(shortName, tempRect.x + 1, tempRect.y
                    + tempRect.height - 1);

            // create final image
            if (zoomIndex == BASE_ZOOM_INDEX) {
                image = createImage(new FilteredImageSource(tempImage
                        .getSource(), new KeyAlphaFilter(TRANSPARENT)));
            } else {
                image = getScaledImage(createImage(new FilteredImageSource(
                        tempImage.getSource(), new KeyAlphaFilter(TRANSPARENT))));
            }
            graph.dispose();
            tempImage.flush();
        }

        /**
         * Overrides to provide for a smaller sensitive area.
         */
        @Override
        public boolean isInside(Point point) {
            return false;
        }

    }

    /**
     * Sprite for an entity. Changes whenever the entity changes. Consists of an
     * image, drawn from the Tile Manager; facing and possibly secondary facing
     * arrows; armor and internal bars; and an identification label.
     */
    private class EntitySprite extends Sprite {
        private Entity entity;
        private Rectangle entityRect;
        private Rectangle modelRect;

        public EntitySprite(Entity entity) {
            this.entity = entity;

            String shortName = entity.getShortName();

            if (entity.getMovementMode() == EntityMovementMode.VTOL) {
                shortName = shortName.concat(" (FL: ").concat(
                        Integer.toString(entity.getElevation())).concat(")");
            }
            if (entity.getMovementMode() == EntityMovementMode.SUBMARINE) {
                shortName = shortName.concat(" (Depth: ").concat(
                        Integer.toString(entity.getElevation())).concat(")");
            }
            int face = entity.isCommander() ? Font.ITALIC : Font.PLAIN;
            Font font = new Font("SansSerif", face, 10); //$NON-NLS-1$
            modelRect = new Rectangle(47, 55, getFontMetrics(font).stringWidth(
                    shortName) + 1, getFontMetrics(font).getAscent());
            Rectangle tempBounds = new Rectangle(hex_size).union(modelRect);
            tempBounds.setLocation(getHexLocation(entity.getPosition()));

            bounds = tempBounds;
            entityRect = new Rectangle(bounds.x + (int) (20 * scale),
                    bounds.y + (int) (14 * scale), (int) (44 * scale),
                    (int) (44 * scale));
            image = null;
        }

        @Override
        public Rectangle getBounds() {
            Rectangle tempBounds = new Rectangle(hex_size).union(modelRect);
            tempBounds.setLocation(getHexLocation(entity.getPosition()));
            bounds = tempBounds;

            entityRect = new Rectangle(bounds.x + (int) (20 * scale),
                    bounds.y + (int) (14 * scale), (int) (44 * scale),
                    (int) (44 * scale));

            return bounds;
        }

        @Override
        public void drawOnto(Graphics g, int x, int y, ImageObserver observer) {
            if (trackThisEntitiesVisibilityInfo(entity)
                    && !entity.isVisibleToEnemy()
                    && GUIPreferences.getInstance().getBoolean(
                            GUIPreferences.ADVANCED_TRANSLUCENT_HIDDEN_UNITS)) {
                // create final image with translucency
                drawOnto(g, x, y, observer, true);
            } else {
                drawOnto(g, x, y, observer, false);
            }
        }

        /**
         * Creates the sprite for this entity. It is an extra pain to create
         * transparent images in AWT.
         */
        @Override
        public void prepare() {
            // figure out size
            String shortName = entity.getShortName();
            if (entity.getMovementMode() == EntityMovementMode.VTOL) {
                shortName = shortName.concat(" (FL: ").concat(
                        Integer.toString(entity.getElevation())).concat(")");
            }
            if (PreferenceManager.getClientPreferences().getShowUnitId()) {
                shortName += (Messages.getString("BoardView1.ID") + entity.getId()); //$NON-NLS-1$
            }
            int face = entity.isCommander() ? Font.ITALIC : Font.PLAIN;
            Font font = new Font("SansSerif", face, 10); //$NON-NLS-1$
            Rectangle tempRect = new Rectangle(47, 55, getFontMetrics(font)
                    .stringWidth(shortName) + 1, getFontMetrics(font)
                    .getAscent());

            // create image for buffer
            Image tempImage;
            Graphics graph;
            try {
                tempImage = createImage(bounds.width, bounds.height);
                graph = tempImage.getGraphics();
            } catch (NullPointerException ex) {
                // argh! but I want it!
                return;
            }

            // fill with key color
            graph.setColor(new Color(TRANSPARENT));
            graph.fillRect(0, 0, bounds.width, bounds.height);

            // draw entity image
            graph.drawImage(tileManager.imageFor(entity), 0, 0, this);

            // draw box with shortName
            Color text, bkgd, bord;
            if (entity.isDone()) {
                text = Color.lightGray;
                bkgd = Color.darkGray;
                bord = Color.black;
            } else if (entity.isImmobile()) {
                text = Color.darkGray;
                bkgd = Color.black;
                bord = Color.lightGray;
            } else {
                text = Color.black;
                bkgd = Color.lightGray;
                bord = Color.darkGray;
            }
            graph.setFont(font);
            graph.setColor(bord);
            graph.fillRect(tempRect.x, tempRect.y, tempRect.width,
                    tempRect.height);
            tempRect.translate(-1, -1);
            graph.setColor(bkgd);
            graph.fillRect(tempRect.x, tempRect.y, tempRect.width,
                    tempRect.height);
            graph.setColor(text);
            graph.drawString(shortName, tempRect.x + 1, tempRect.y
                    + tempRect.height - 1);

            // draw facing
            graph.setColor(Color.white);
            if ((entity.getFacing() != -1)
                    && !((entity instanceof Infantry) && (((Infantry) entity)
                            .getDugIn() == Infantry.DUG_IN_NONE))
                            && !((entity instanceof Aero) && ((Aero)entity).isSpheroid()
                                    && (game.getBoard().inAtmosphere() || game.getBoard().onGround()))) {
                graph.drawPolygon(facingPolys[entity.getFacing()]);
            }

            // determine secondary facing for non-mechs & flipped arms
            int secFacing = entity.getFacing();
            if (!((entity instanceof Mech) || (entity instanceof Protomech))) {
                secFacing = entity.getSecondaryFacing();
            } else if (entity.getArmsFlipped()) {
                secFacing = (entity.getFacing() + 3) % 6;
            }
            // draw red secondary facing arrow if necessary
            if ((secFacing != -1) && (secFacing != entity.getFacing())) {
                graph.setColor(Color.red);
                graph.drawPolygon(facingPolys[secFacing]);
            }
            if((entity instanceof Aero) && game.useVectorMove()) {
                for(int head : entity.getHeading()) {
                    graph.setColor(Color.red);
                    graph.drawPolygon(facingPolys[head]);
                }
            }

            // Determine if the entity has a locked turret,
            // and if it is a gun emplacement
            boolean turretLocked = false;
            int crewStunned = 0;
            boolean ge = false;
            if (entity instanceof Tank) {
                turretLocked = !((Tank) entity).hasNoTurret()
                && !entity.canChangeSecondaryFacing();
                crewStunned = ((Tank) entity).getStunnedTurns();
            } else if (entity instanceof GunEmplacement) {
                turretLocked = ((GunEmplacement) entity).isTurret()
                && !entity.canChangeSecondaryFacing();
                ge = true;
            }
            if(entity.isAirborne()) {
                if(!game.getBoard().inSpace()) {
                    graph.setColor(Color.darkGray);
                    graph.drawString(Integer.toString(entity.getAltitude()) + "A", 26, 15);
                    graph.setColor(Color.PINK);
                    graph.drawString(Integer.toString(entity.getAltitude()) + "A", 25, 14);
                }
            } else if(entity.getElevation() > 0) {
                graph.setColor(Color.darkGray);
                graph.drawString(Integer.toString(entity.getElevation()), 26, 15);
                graph.setColor(Color.PINK);
                graph.drawString(Integer.toString(entity.getElevation()), 25, 14);
            }

            // draw condition strings
            if(entity instanceof Aero) {
                Aero a = (Aero)entity;

                if(a.isRolled()) {
                    // draw "rolled"
                    graph.setColor(Color.darkGray);
                    graph.drawString(Messages.getString("BoardView1.ROLLED"), 18, 15); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(Messages.getString("BoardView1.ROLLED"), 17, 14); //$NON-NLS-1$
                }

                if(a.isOutControlTotal() & a.isRandomMove()) {
                    // draw "RANDOM"
                    graph.setColor(Color.darkGray);
                    graph.drawString(Messages.getString("BoardView1.RANDOM"), 18, 35); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(Messages.getString("BoardView1.RANDOM"), 17, 34); //$NON-NLS-1$
                } else if (a.isOutControlTotal()) {
                    //                     draw "CONTROL"
                    graph.setColor(Color.darkGray);
                    graph.drawString(Messages.getString("BoardView1.CONTROL"), 18, 39); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(Messages.getString("BoardView1.CONTROL"), 17, 38); //$NON-NLS-1$
                }

                if(a.isEvading()) {
                    //draw "EVADE" - can't overlap with out of control
                    graph.setColor(Color.darkGray);
                    graph.drawString(Messages.getString("BoardView1.EVADE"), 18, 39); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(Messages.getString("BoardView1.EVADE"), 17, 38); //$NON-NLS-1$
                }
            }

            if (entity.crew.isDead()) {
                // draw "CREW DEAD"
                graph.setColor(Color.darkGray);
                graph.drawString(
                        Messages.getString("BoardView1.CrewDead"), 18, 39); //$NON-NLS-1$
                graph.setColor(Color.red);
                graph.drawString(
                        Messages.getString("BoardView1.CrewDead"), 17, 38); //$NON-NLS-1$
            } else if (!ge && entity.isImmobile()) {
                if (entity.isProne()) {
                    // draw "IMMOBILE" and "PRONE"
                    graph.setColor(Color.darkGray);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 18, 35); //$NON-NLS-1$
                    graph.drawString(
                            Messages.getString("BoardView1.PRONE"), 26, 48); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 17, 34); //$NON-NLS-1$
                    graph.setColor(Color.yellow);
                    graph.drawString(
                            Messages.getString("BoardView1.PRONE"), 25, 47); //$NON-NLS-1$
                } else if (crewStunned > 0) {
                    // draw IMMOBILE and STUNNED
                    graph.setColor(Color.darkGray);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 18, 35); //$NON-NLS-1$
                    graph
                    .drawString(
                            Messages
                            .getString(
                                    "BoardView1.STUNNED", new Object[] { crewStunned }), 22, 48); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 17, 34); //$NON-NLS-1$
                    graph.setColor(Color.yellow);
                    graph
                    .drawString(
                            Messages
                            .getString(
                                    "BoardView1.STUNNED", new Object[] { crewStunned }), 21, 47); //$NON-NLS-1$
                } else if (turretLocked) {
                    // draw "IMMOBILE" and "LOCKED"
                    graph.setColor(Color.darkGray);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 18, 35); //$NON-NLS-1$
                    graph.drawString(
                            Messages.getString("BoardView1.LOCKED"), 22, 48); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 17, 34); //$NON-NLS-1$
                    graph.setColor(Color.yellow);
                    graph.drawString(
                            Messages.getString("BoardView1.LOCKED"), 21, 47); //$NON-NLS-1$
                } else {
                    // draw "IMMOBILE"
                    graph.setColor(Color.darkGray);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 18, 39); //$NON-NLS-1$
                    graph.setColor(Color.red);
                    graph.drawString(
                            Messages.getString("BoardView1.IMMOBILE"), 17, 38); //$NON-NLS-1$
                }
            } else if (entity.isProne()) {
                // draw "PRONE"
                graph.setColor(Color.darkGray);
                graph.drawString(
                        Messages.getString("BoardView1.PRONE"), 26, 39); //$NON-NLS-1$
                graph.setColor(Color.yellow);
                graph.drawString(
                        Messages.getString("BoardView1.PRONE"), 25, 38); //$NON-NLS-1$
            } else if (crewStunned > 0) {
                // draw STUNNED
                graph.setColor(Color.darkGray);
                graph.drawString(
                        Messages
                        .getString(
                                "BoardView1.STUNNED", new Object[] { crewStunned }), 22, 48); //$NON-NLS-1$
                graph.setColor(Color.yellow);
                graph.drawString(
                        Messages
                        .getString(
                                "BoardView1.STUNNED", new Object[] { crewStunned }), 21, 47); //$NON-NLS-1$
            } else if (turretLocked) {
                // draw "LOCKED"
                graph.setColor(Color.darkGray);
                graph.drawString(
                        Messages.getString("BoardView1.LOCKED"), 22, 39); //$NON-NLS-1$
                graph.setColor(Color.yellow);
                graph.drawString(
                        Messages.getString("BoardView1.LOCKED"), 21, 38); //$NON-NLS-1$
            }

            // If this unit is being swarmed or is swarming another, say so.
            if (Entity.NONE != entity.getSwarmAttackerId()) {
                // draw "SWARMED"
                graph.setColor(Color.darkGray);
                graph.drawString(
                        Messages.getString("BoardView1.SWARMED"), 17, 22); //$NON-NLS-1$
                graph.setColor(Color.red);
                graph.drawString(
                        Messages.getString("BoardView1.SWARMED"), 16, 21); //$NON-NLS-1$
            }

            // If this unit is transporting another, say so.
            if ((entity.getLoadedUnits()).size() > 0) {
                // draw "T"
                graph.setColor(Color.darkGray);
                graph.drawString("T", 20, 71); //$NON-NLS-1$
                graph.setColor(Color.black);
                graph.drawString("T", 19, 70); //$NON-NLS-1$
            }

            // If this unit is stuck, say so.
            if ((entity.isStuck())) {
                graph.setColor(Color.darkGray);
                graph
                .drawString(
                        Messages.getString("BoardView1.STUCK"), 26, 61); //$NON-NLS-1$
                graph.setColor(Color.orange);
                graph
                .drawString(
                        Messages.getString("BoardView1.STUCK"), 25, 60); //$NON-NLS-1$

            }

            // If this unit is currently unknown to the enemy, say so.
            if (trackThisEntitiesVisibilityInfo(entity)) {
                if (!entity.isSeenByEnemy()) {
                    // draw "U"
                    graph.setColor(Color.darkGray);
                    graph.drawString("U", 30, 71); //$NON-NLS-1$
                    graph.setColor(Color.black);
                    graph.drawString("U", 29, 70); //$NON-NLS-1$
                } else if (!entity.isVisibleToEnemy()
                        && !GUIPreferences
                        .getInstance()
                        .getBoolean(
                                GUIPreferences.ADVANCED_TRANSLUCENT_HIDDEN_UNITS)) {
                    // If this unit is currently hidden from the enemy, say so.
                    // draw "H"
                    graph.setColor(Color.darkGray);
                    graph.drawString("H", 30, 71); //$NON-NLS-1$
                    graph.setColor(Color.black);
                    graph.drawString("H", 29, 70); //$NON-NLS-1$
                }
            }

            // If hull down, show
            if (entity.isHullDown()) {
                // draw "D"
                graph.setColor(Color.darkGray);
                graph.drawString(Messages.getString("UnitOverview.HULLDOWN"), 15, 39); //$NON-NLS-1$
                graph.setColor(Color.yellow);
                graph.drawString(Messages.getString("UnitOverview.HULLDOWN"), 14, 38); //$NON-NLS-1$
            } else if (entity instanceof Infantry) {
                int dig = ((Infantry) entity).getDugIn();
                if (dig == Infantry.DUG_IN_COMPLETE) {
                    // draw "D"
                    graph.setColor(Color.darkGray);
                    graph.drawString("D", 40, 71); //$NON-NLS-1$
                    graph.setColor(Color.black);
                    graph.drawString("D", 39, 70); //$NON-NLS-1$
                } else if (dig != Infantry.DUG_IN_NONE) {
                    // draw "W"
                    graph.setColor(Color.darkGray);
                    graph.drawString("W", 40, 71); //$NON-NLS-1$
                    graph.setColor(Color.black);
                    graph.drawString("W", 39, 70); //$NON-NLS-1$
                }
            }

            // Lets draw our armor and internal status bars
            int baseBarLength = 23;
            int barLength = 0;
            double percentRemaining = 0.00;

            percentRemaining = entity.getArmorRemainingPercent();
            barLength = (int) (baseBarLength * percentRemaining);

            graph.setColor(Color.darkGray);
            graph.fillRect(56, 7, 23, 3);
            graph.setColor(Color.lightGray);
            graph.fillRect(55, 6, 23, 3);
            graph.setColor(getStatusBarColor(percentRemaining));
            graph.fillRect(55, 6, barLength, 3);

            if (!ge) {
                // Gun emplacements don't have internal structure
                percentRemaining = entity.getInternalRemainingPercent();
                barLength = (int) (baseBarLength * percentRemaining);

                graph.setColor(Color.darkGray);
                graph.fillRect(56, 11, 23, 3);
                graph.setColor(Color.lightGray);
                graph.fillRect(55, 10, 23, 3);
                graph.setColor(getStatusBarColor(percentRemaining));
                graph.fillRect(55, 10, barLength, 3);
            }

            // create final image
            if (zoomIndex == BASE_ZOOM_INDEX) {
                image = createImage(new FilteredImageSource(tempImage
                        .getSource(), new KeyAlphaFilter(TRANSPARENT)));
            } else {
                image = getScaledImage(createImage(new FilteredImageSource(
                        tempImage.getSource(), new KeyAlphaFilter(TRANSPARENT))));
            }
            graph.dispose();
            tempImage.flush();
        }

        /**
         * We only want to show double-blind visibility indicators on our own
         * mechs and teammates mechs (assuming team vision option).
         */
        private boolean trackThisEntitiesVisibilityInfo(Entity e) {
            if (getLocalPlayer() == null) {
                return false;
            }

            if (game.getOptions().booleanOption("double_blind") //$NON-NLS-1$
                    && ((e.getOwner().getId() == getLocalPlayer().getId()) || (game
                            .getOptions().booleanOption("team_vision") //$NON-NLS-1$
                            && (e.getOwner().getTeam() == getLocalPlayer().getTeam())))) {
                return true;
            }
            return false;
        }

        private Color getStatusBarColor(double percentRemaining) {
            if (percentRemaining <= .25) {
                return Color.red;
            } else if (percentRemaining <= .75) {
                return Color.yellow;
            } else {
                return new Color(16, 196, 16);
            }
        }

        /**
         * Overrides to provide for a smaller sensitive area.
         */
        @Override
        public boolean isInside(Point point) {
            return entityRect.contains(point.x + view.x - offset.x, point.y
                    + view.y - offset.y);
        }

        @Override
        public String[] getTooltip() {
            String[] tipStrings = new String[3];
            StringBuffer buffer;

            buffer = new StringBuffer();
            buffer.append(entity.getChassis()).append(" (") //$NON-NLS-1$
            .append(entity.getOwner().getName()).append("); ") //$NON-NLS-1$
            .append(entity.getCrew().getGunnery()).append("/") //$NON-NLS-1$
            .append(entity.getCrew().getPiloting()).append(
                    Messages.getString("BoardView1.pilot")); //$NON-NLS-1$
            int numAdv = entity.getCrew().countOptions(PilotOptions.LVL3_ADVANTAGES);
            boolean isMD = entity.getCrew().countOptions(PilotOptions.MD_ADVANTAGES) > 0;
            if (numAdv > 0) {
                buffer.append(" <") //$NON-NLS-1$
                .append(numAdv).append(
                        Messages.getString("BoardView1.advs")); //$NON-NLS-1$
            }
            if (isMD) {
                buffer.append(Messages.getString("BoardView1.md")); //$NON-NLS-1$
            }
            tipStrings[0] = buffer.toString();

            GunEmplacement ge = null;
            if (entity instanceof GunEmplacement) {
                ge = (GunEmplacement) entity;
            }

            Aero a = null;
            if(entity instanceof Aero) {
                a = (Aero) entity;
            }

            buffer = new StringBuffer();
            if ((ge == null) && (a == null)) {
                buffer.append(Messages.getString("BoardView1.move")) //$NON-NLS-1$
                .append(entity.getMovementAbbr(entity.moved)).append(
                ":") //$NON-NLS-1$
                .append(entity.delta_distance).append(" (+") //$NON-NLS-1$
                .append(
                        Compute.getTargetMovementModifier(game,
                                entity.getId()).getValue())
                                .append(");") //$NON-NLS-1$
                                .append(Messages.getString("BoardView1.Heat")) //$NON-NLS-1$
                                .append(entity.heat);
                if (entity.isCharging()) {
                    buffer.append(" ") //$NON-NLS-1$
                    .append(Messages.getString("BoardView1.charge1")); //$NON-NLS-1$
                }
                if (entity.isMakingDfa()) {
                    buffer.append(" ") //$NON-NLS-1$
                    .append(Messages.getString("BoardView1.DFA1")); //$NON-NLS-1$
                }
            } else if (ge == null) {
                buffer.append( Messages.getString("BoardView1.move") ) //$NON-NLS-1$
                .append( entity.getMovementAbbr(entity.moved) )
                .append( ":" ) //$NON-NLS-1$
                .append( entity.delta_distance )
                .append( Messages.getString("BoardView1.Heat") ) //$NON-NLS-1$
                .append( entity.heat );
                if (entity.isCharging()) {
                    buffer.append(" ") //$NON-NLS-1$
                    .append( Messages.getString("BoardView1.charge1")); //$NON-NLS-1$
                }
            } else {
                if (ge.isTurret() && ge.isTurretLocked()) {
                    buffer
                    .append(Messages
                            .getString("BoardView1.TurretLocked"));
                    if (ge.getFirstWeapon() == -1) {
                        buffer.append(",");
                        buffer.append(Messages
                                .getString("BoardView1.WeaponsDestroyed"));
                    }
                } else if (ge.getFirstWeapon() == -1) {
                    buffer.append(Messages
                            .getString("BoardView1.WeaponsDestroyed"));
                } else {
                    buffer.append(Messages.getString("BoardView1.Operational"));
                }
            }
            if (entity.isDone()) {
                buffer.append(" (").append(
                        Messages.getString("BoardView1.done")).append(")");
            }
            tipStrings[1] = buffer.toString();

            buffer = new StringBuffer();
            if (ge == null) {
                buffer.append(Messages.getString("BoardView1.Armor")) //$NON-NLS-1$
                .append(entity.getTotalArmor()).append(
                        Messages.getString("BoardView1.internal")) //$NON-NLS-1$
                        .append(entity.getTotalInternal());
            }
            tipStrings[2] = buffer.toString();

            return tipStrings;
        }
    }

    /**
     * Sprite for a step in a movement path. Only one sprite should exist for
     * any hex in a path. Contains a colored number, and arrows indicating
     * entering, exiting or turning.
     */
    private class StepSprite extends Sprite {
        private MoveStep step;

        public StepSprite(MoveStep step) {
            this.step = step;

            // step is the size of the hex that this step is in
            bounds = new Rectangle(getHexLocation(step.getPosition()), hex_size);
            image = null;
        }

        @Override
        public void prepare() {
            // create image for buffer
            Image tempImage = createImage(bounds.width, bounds.height);
            Graphics graph = tempImage.getGraphics();

            // fill with key color
            graph.setColor(new Color(TRANSPARENT));
            graph.fillRect(0, 0, bounds.width, bounds.height);

            // setup some variables
            final Point stepPos = getHexLocation(step.getPosition());
            stepPos.translate(-bounds.x, -bounds.y);
            final Polygon facingPoly = facingPolys[step.getFacing()];
            final Polygon movePoly = movementPolys[step.getFacing()];
            Point offsetCostPos;
            Polygon myPoly;
            Color col;
            // set color
            switch (step.getMovementType()) {
            case MOVE_RUN:
            case MOVE_VTOL_RUN:
            case MOVE_OVER_THRUST:
                if (step.isUsingMASC()) {
                    col = GUIPreferences.getInstance().getColor(
                            "AdvancedMoveMASCColor");
                } else {
                    col = GUIPreferences.getInstance().getColor(
                            "AdvancedMoveRunColor");
                }
                break;
            case MOVE_JUMP:
                col = GUIPreferences.getInstance().getColor(
                        "AdvancedMoveJumpColor");
                break;
            case MOVE_SPRINT:
                col = GUIPreferences.getInstance().getColor(
                        "AdvancedMoveSprintColor");
                break;
            case MOVE_ILLEGAL:
                col = GUIPreferences.getInstance().getColor(
                        "AdvancedMoveIllegalColor");
                break;
            default:
                if (step.getType() == MoveStepType.BACKWARDS) {
                    col = GUIPreferences.getInstance().getColor(
                            "AdvancedMoveBackColor");
                } else {
                    col = GUIPreferences.getInstance().getColor(
                            "AdvancedMoveDefaultColor");
                }
            break;
            }

            if(game.useVectorMove()) {
                drawActiveVectors(step, stepPos, graph);
            }

            drawConditions(step, stepPos, graph, col);

            // draw arrows and cost for the step
            switch (step.getType()) {
            case FORWARDS:
            case SWIM:
            case BACKWARDS:
            case CHARGE:
            case DFA:
            case LATERAL_LEFT:
            case LATERAL_RIGHT:
            case LATERAL_LEFT_BACKWARDS:
            case LATERAL_RIGHT_BACKWARDS:
            case DEC:
            case DECN:
            case ACC:
            case ACCN:
            case LOOP:
                // draw arrows showing them entering the next
                myPoly = new Polygon(movePoly.xpoints, movePoly.ypoints,
                        movePoly.npoints);
                graph.setColor(Color.darkGray);
                myPoly.translate(stepPos.x + 1, stepPos.y + 1);
                graph.drawPolygon(myPoly);
                graph.setColor(col);
                myPoly.translate(-1, -1);
                graph.drawPolygon(myPoly);
                // draw movement cost
                drawMovementCost(step, stepPos, graph, col, true);
                drawRemainingVelocity(step, stepPos, graph, true);
                break;
            case GO_PRONE:
            case HULL_DOWN:
            case DOWN:
            case DIG_IN:
            case FORTIFY:
                //draw arrow indicating dropping prone
                // also doubles as the descent indication
                Polygon downPoly = movementPolys[7];
                myPoly = new Polygon(downPoly.xpoints, downPoly.ypoints,
                        downPoly.npoints);
                graph.setColor(Color.darkGray);
                myPoly.translate(stepPos.x, stepPos.y);
                graph.drawPolygon(myPoly);
                graph.setColor(col);
                myPoly.translate(-1, -1);
                graph.drawPolygon(myPoly);
                offsetCostPos = new Point(stepPos.x + 1, stepPos.y + 15);
                drawMovementCost(step, offsetCostPos, graph, col, false);
                drawRemainingVelocity(step, stepPos, graph, true);
                break;
            case GET_UP:
            case UP:
            case CAREFUL_STAND:
                // draw arrow indicating standing up
                // also doubles as the climb indication
                // and triples as deceleration
                Polygon upPoly = movementPolys[6];
                myPoly = new Polygon(upPoly.xpoints, upPoly.ypoints,
                        upPoly.npoints);
                graph.setColor(Color.darkGray);
                myPoly.translate(stepPos.x, stepPos.y);
                graph.drawPolygon(myPoly);
                graph.setColor(col);
                myPoly.translate(-1, -1);
                graph.drawPolygon(myPoly);
                offsetCostPos = new Point(stepPos.x, stepPos.y + 15);
                drawMovementCost(step, offsetCostPos, graph, col, false);
                drawRemainingVelocity(step, stepPos, graph, true);
                break;
            case CLIMB_MODE_ON:
                // draw climb mode indicator
                String climb;
                if (step.getParent().getEntity().getMovementMode() == EntityMovementMode.WIGE) {
                    climb = Messages.getString("BoardView1.WIGEClimb"); //$NON-NLS-1$
                } else {
                    climb = Messages.getString("BoardView1.Climb"); //$NON-NLS-1$
                }
                if (step.isPastDanger()) {
                    climb = "(" + climb + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int climbX = stepPos.x
                + 42
                - (graph.getFontMetrics(graph.getFont())
                        .stringWidth(climb) / 2);
                graph.setColor(Color.darkGray);
                graph.drawString(climb, climbX, stepPos.y + 39);
                graph.setColor(col);
                graph.drawString(climb, climbX - 1, stepPos.y + 38);
                break;
            case CLIMB_MODE_OFF:
                // cancel climb mode indicator
                String climboff;
                if (step.getParent().getEntity().getMovementMode() == EntityMovementMode.WIGE) {
                    climboff = Messages.getString("BoardView1.WIGEClimbOff"); //$NON-NLS-1$
                } else {
                    climboff = Messages.getString("BoardView1.ClimbOff"); //$NON-NLS-1$
                }
                if (step.isPastDanger()) {
                    climboff = "(" + climboff + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int climboffX = stepPos.x
                + 42
                - (graph.getFontMetrics(graph.getFont())
                        .stringWidth(climboff) / 2);
                graph.setColor(Color.darkGray);
                graph.drawString(climboff, climboffX, stepPos.y + 39);
                graph.setColor(col);
                graph.drawString(climboff, climboffX - 1, stepPos.y + 38);
                break;
            case TURN_LEFT:
            case TURN_RIGHT:
            case THRUST:
            case YAW:
            case EVADE:
            case ROLL:
                // draw arrows showing the facing
                myPoly = new Polygon(facingPoly.xpoints,
                        facingPoly.ypoints, facingPoly.npoints);
                graph.setColor(Color.darkGray);
                myPoly.translate(stepPos.x + 1, stepPos.y + 1);
                graph.drawPolygon(myPoly);
                graph.setColor(col);
                myPoly.translate(-1, -1);
                graph.drawPolygon(myPoly);
                if(game.useVectorMove()) {
                    drawMovementCost(step, stepPos, graph, col, false);
                }
                break;
            case LOAD:
                // Announce load.
                String load = Messages.getString("BoardView1.Load"); //$NON-NLS-1$
                if (step.isPastDanger()) {
                    load = "(" + load + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int loadX = stepPos.x
                + 42
                - (graph.getFontMetrics(graph.getFont())
                        .stringWidth(load) / 2);
                graph.setColor(Color.darkGray);
                graph.drawString(load, loadX, stepPos.y + 39);
                graph.setColor(col);
                graph.drawString(load, loadX - 1, stepPos.y + 38);
                break;
            case LAUNCH:
                //announce launch
                String launch = Messages.getString("BoardView1.Launch"); //$NON-NLS-1$
                if (step.isPastDanger()) {
                    launch = "(" + launch + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int launchX = stepPos.x + 42 - (graph.getFontMetrics(graph.getFont()).stringWidth(launch) / 2);
                int launchY = stepPos.y + 38 + graph.getFontMetrics(graph.getFont()).getHeight();
                graph.setColor(Color.darkGray);
                graph.drawString(launch, launchX, launchY + 1);
                graph.setColor(col);
                graph.drawString(launch, launchX - 1, launchY);
                break;
            case RECOVER:
                //announce launch
                String recover = Messages.getString("BoardView1.Recover"); //$NON-NLS-1$
                if (step.isPastDanger()) {
                    launch = "(" + recover + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int recoverX = stepPos.x + 42 - (graph.getFontMetrics(graph.getFont()).stringWidth(recover) / 2);
                int recoverY = stepPos.y + 38 + graph.getFontMetrics(graph.getFont()).getHeight();
                graph.setColor(Color.darkGray);
                graph.drawString(recover, recoverX, recoverY + 1);
                graph.setColor(col);
                graph.drawString(recover, recoverX - 1, recoverY);
                break;
            case JOIN:
                //announce launch
                String join = Messages.getString("BoardView1.Join"); //$NON-NLS-1$
                if (step.isPastDanger()) {
                    launch = "(" + join + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int joinX = stepPos.x + 42 - (graph.getFontMetrics(graph.getFont()).stringWidth(join) / 2);
                int joinY = stepPos.y + 38 + graph.getFontMetrics(graph.getFont()).getHeight();
                graph.setColor(Color.darkGray);
                graph.drawString(join, joinX, joinY + 1);
                graph.setColor(col);
                graph.drawString(join, joinX - 1, joinY);
                break;
            case UNLOAD:
                // Announce unload.
                String unload = Messages.getString("BoardView1.Unload"); //$NON-NLS-1$
                if (step.isPastDanger()) {
                    unload = "(" + unload + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int unloadX = stepPos.x
                + 42
                - (graph.getFontMetrics(graph.getFont())
                        .stringWidth(unload) / 2);
                int unloadY = stepPos.y + 38
                + graph.getFontMetrics(graph.getFont()).getHeight();
                graph.setColor(Color.darkGray);
                graph.drawString(unload, unloadX, unloadY + 1);
                graph.setColor(col);
                graph.drawString(unload, unloadX - 1, unloadY);
                break;
            case HOVER:
                //announce launch
                String hover = Messages.getString("BoardView1.Hover"); //$NON-NLS-1$
                if (step.isPastDanger()) {
                    hover = "(" + hover + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int hoverX = stepPos.x + 42 - (graph.getFontMetrics(graph.getFont()).stringWidth(hover) / 2);
                int hoverY = stepPos.y + 38 + graph.getFontMetrics(graph.getFont()).getHeight();
                graph.setColor(Color.darkGray);
                graph.drawString(hover, hoverX, hoverY + 1);
                graph.setColor(col);
                graph.drawString(hover, hoverX - 1, hoverY);
                drawMovementCost(step, stepPos, graph, col, false);
                break;
            default:
                break;
            }

            // create final image
            if (zoomIndex == BASE_ZOOM_INDEX) {
                image = createImage(new FilteredImageSource(tempImage
                        .getSource(), new KeyAlphaFilter(TRANSPARENT)));
            } else {
                image = getScaledImage(createImage(new FilteredImageSource(
                        tempImage.getSource(), new KeyAlphaFilter(TRANSPARENT))));
            }
            graph.dispose();
            tempImage.flush();
        }

        /**
         * draw conditions separate from the step, This allows me to keep
         * conditions on the Aero even when that step is erased (as per
         * advanced movement). For now, just evading and rolling. eventually
         * loading and unloading as well
         */
        private void drawConditions(MoveStep step, Point stepPos, Graphics graph, Color col) {
            if(step.isEvading()) {
                String evade = Messages.getString("BoardView1.Evade"); //$NON-NLS-1$
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int evadeX = stepPos.x + 42 - (graph.getFontMetrics(graph.getFont()).stringWidth(evade) / 2);
                graph.setColor(Color.darkGray);
                graph.drawString(evade, evadeX, stepPos.y + 64);
                graph.setColor(col);
                graph.drawString(evade, evadeX - 1, stepPos.y + 63);
            }

            if(step.isRolled()) {
                //Announce roll
                String roll = Messages.getString("BoardView1.Roll"); //$NON-NLS-1$
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                int rollX = stepPos.x + 42 - (graph.getFontMetrics(graph.getFont()).stringWidth(roll) / 2);
                graph.setColor(Color.darkGray);
                graph.drawString(roll, rollX, stepPos.y + 18);
                graph.setColor(col);
                graph.drawString(roll, rollX - 1, stepPos.y + 17);
            }


        }

        private void drawActiveVectors(MoveStep step, Point stepPos, Graphics graph) {

            /*TODO: it might be better to move this to the MovementSprite
             * so that it is visible before first step and you can't see it
             * for all entities
             */

            int[] activeXpos = {39, 59, 59, 40, 19, 19};
            int[] activeYpos = {20, 28, 52, 59, 52, 28};

            int[] v = step.getVectors();
            for(int i = 0; i < 6; i++) {

                String active = Integer.toString(v[i]);
                graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
                graph.setColor(Color.darkGray);
                graph.drawString(active, activeXpos[i] + stepPos.x, activeYpos[i] + stepPos.y);
                graph.setColor(Color.red);
                graph.drawString(active, activeXpos[i] + stepPos.x - 1, activeYpos[i] + stepPos.y - 1);

            }

        }

        @Override
        public Rectangle getBounds() {
            bounds = new Rectangle(getHexLocation(step.getPosition()), hex_size);
            return bounds;
        }

        public MoveStep getStep() {
            return step;
        }

        public Font getMovementFont(){

            String fontName= GUIPreferences.getInstance().getString(GUIPreferences.ADVANCED_MOVE_FONT_TYPE);
            int fontStyle =  GUIPreferences.getInstance().getInt(GUIPreferences.ADVANCED_MOVE_FONT_STYLE);
            int fontSize =  GUIPreferences.getInstance().getInt(GUIPreferences.ADVANCED_MOVE_FONT_SIZE);

            return new Font(fontName,fontStyle,fontSize);
        }

        private void drawRemainingVelocity(MoveStep step, Point stepPos,
                Graphics graph, boolean shiftFlag) {
            String velString = null;
            StringBuffer velStringBuf = new StringBuffer();

            if(game.useVectorMove()) {
                return;
            }

            if(!step.getParent().getEntity().isAirborne()) {
                return;
            }

            if(((Aero)step.getParent().getEntity()).isSpheroid()) {
                return;
            }

            velStringBuf.append("(")
            .append(step.getVelocityLeft())
            .append("/")
            .append(step.getVelocity())
            .append(")");

            Color col = Color.GREEN;
            if(step.getVelocityLeft() > 0) {
                col = Color.RED;
            }

            //          Convert the buffer to a String and draw it.
            velString = velStringBuf.toString();
            graph.setFont(new Font("SansSerif", Font.PLAIN, 12)); //$NON-NLS-1$
            int costX = stepPos.x + 42;
            if (shiftFlag) {
                costX -= (graph.getFontMetrics(graph.getFont()).stringWidth(
                        velString) / 2);
            }
            graph.setColor(Color.darkGray);
            graph.drawString(velString, costX, stepPos.y + 28);
            graph.setColor(col);
            graph.drawString(velString, costX - 1, stepPos.y + 27);

        }

        private void drawMovementCost(MoveStep step, Point stepPos,
                Graphics graph, Color col, boolean shiftFlag) {
            String costString = null;
            StringBuffer costStringBuf = new StringBuffer();
            costStringBuf.append(step.getMpUsed());

            // If the step is using a road bonus, mark it.
            if (step.isOnlyPavement()
                    && (step.getParent().getEntity() instanceof Tank)) {
                costStringBuf.append("+"); //$NON-NLS-1$
            }

            // If the step is dangerous, mark it.
            if (step.isDanger()) {
                costStringBuf.append("*"); //$NON-NLS-1$
            }

            // If the step is past danger, mark that.
            if (step.isPastDanger()) {
                costStringBuf.insert(0, "("); //$NON-NLS-1$
                costStringBuf.append(")"); //$NON-NLS-1$
            }

            if (step.isUsingMASC()) {
                costStringBuf.append("["); //$NON-NLS-1$
                costStringBuf.append(step.getTargetNumberMASC());
                costStringBuf.append("+]"); //$NON-NLS-1$
            }

            if ((step.getMovementType() == EntityMovementType.MOVE_VTOL_WALK)
                    || (step.getMovementType() == EntityMovementType.MOVE_VTOL_RUN)
                    || (step.getMovementType() == EntityMovementType.MOVE_SUBMARINE_WALK)
                    || (step.getMovementType() == EntityMovementType.MOVE_SUBMARINE_RUN)) {
                costStringBuf.append("{").append(step.getElevation()).append(
                "}");
            }

            if(step.getParent().getEntity().isAirborne()) {
                costStringBuf.append("{").append(step.getAltitude()).append("}");
            }

            // Convert the buffer to a String and draw it.
            costString = costStringBuf.toString();
            graph.setFont(getMovementFont()); //$NON-NLS-1$
            int costX = stepPos.x + 42;
            if (shiftFlag) {
                costX -= (graph.getFontMetrics(graph.getFont()).stringWidth(
                        costString) / 2);
            }
            graph.setColor(Color.darkGray);
            graph.drawString(costString, costX, stepPos.y + 39);
            graph.setColor(col);
            graph.drawString(costString, costX - 1, stepPos.y + 38);
        }

    }

    /**
     * Sprite and info for a C3 network. Does not actually use the image buffer
     * as this can be horribly inefficient for long diagonal lines.
     */
    private class C3Sprite extends Sprite {
        private Polygon C3Poly;

        protected int entityId;
        protected int masterId;
        protected Entity entityE;
        protected Entity entityM;

        Color spriteColor;

        public C3Sprite(Entity e, Entity m) {
            entityE = e;
            entityM = m;
            entityId = e.getId();
            masterId = m.getId();
            spriteColor = PlayerColors.getColor(e.getOwner()
                    .getColorIndex());

            if ((e.getPosition() == null) || (m.getPosition() == null)) {
                C3Poly = new Polygon();
                C3Poly.addPoint(0, 0);
                C3Poly.addPoint(1, 0);
                C3Poly.addPoint(0, 1);
                bounds = new Rectangle(C3Poly.getBounds());
                bounds.setSize(bounds.getSize().width + 1,
                        bounds.getSize().height + 1);
                image = null;
                return;
            }

            makePoly();

            // set bounds
            bounds = new Rectangle(C3Poly.getBounds());
            bounds.setSize(bounds.getSize().width + 1,
                    bounds.getSize().height + 1);

            // move poly to upper right of image
            C3Poly.translate(-bounds.getLocation().x, -bounds.getLocation().y);

            // set names & stuff

            // nullify image
            image = null;
        }

        @Override
        public void prepare() {
        }

        private void makePoly() {
            // make a polygon
            final Point a = getHexLocation(entityE.getPosition());
            final Point t = getHexLocation(entityM.getPosition());

            final double an = (entityE.getPosition().radian(
                    entityM.getPosition()) + (Math.PI * 1.5))
                    % (Math.PI * 2); // angle
            final double lw = scale * C3_LINE_WIDTH; // line width

            C3Poly = new Polygon();
            C3Poly.addPoint(a.x
                    + (int) (scale * (HEX_W / 2) - (int) Math.round(Math
                            .sin(an)
                            * lw)), a.y
                            + (int) (scale * (HEX_H / 2) + (int) Math.round(Math
                                    .cos(an)
                                    * lw)));
            C3Poly.addPoint(a.x
                    + (int) (scale * (HEX_W / 2) + (int) Math.round(Math
                            .sin(an)
                            * lw)), a.y
                            + (int) (scale * (HEX_H / 2) - (int) Math.round(Math
                                    .cos(an)
                                    * lw)));
            C3Poly.addPoint(t.x
                    + (int) (scale * (HEX_W / 2) + (int) Math.round(Math
                            .sin(an)
                            * lw)), t.y
                            + (int) (scale * (HEX_H / 2) - (int) Math.round(Math
                                    .cos(an)
                                    * lw)));
            C3Poly.addPoint(t.x
                    + (int) (scale * (HEX_W / 2) - (int) Math.round(Math
                            .sin(an)
                            * lw)), t.y
                            + (int) (scale * (HEX_H / 2) + (int) Math.round(Math
                                    .cos(an)
                                    * lw)));
        }

        @Override
        public Rectangle getBounds() {
            makePoly();
            // set bounds
            bounds = new Rectangle(C3Poly.getBounds());
            bounds.setSize(bounds.getSize().width + 1,
                    bounds.getSize().height + 1);

            // move poly to upper right of image
            C3Poly.translate(-bounds.getLocation().x, -bounds.getLocation().y);
            image = null;

            return bounds;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void drawOnto(Graphics g, int x, int y, ImageObserver observer) {
            // makePoly();

            Polygon drawPoly = new Polygon(C3Poly.xpoints, C3Poly.ypoints,
                    C3Poly.npoints);
            drawPoly.translate(x, y);

            g.setColor(spriteColor);
            g.fillPolygon(drawPoly);
            g.setColor(Color.black);
            g.drawPolygon(drawPoly);
        }

        /**
         * Return true if the point is inside our polygon
         */
        @Override
        public boolean isInside(Point point) {
            return C3Poly.contains(point.x + view.x - bounds.x - offset.x,
                    point.y + view.y - bounds.y - offset.y);
        }

    }

    /**
     * Sprite and info for an attack. Does not actually use the image buffer as
     * this can be horribly inefficient for long diagonal lines. Appears as an
     * arrow. Arrow becoming cut in half when two Meks attacking each other.
     */
    private class AttackSprite extends Sprite {
        private ArrayList<AttackAction> attacks = new ArrayList<AttackAction>();
        private Point a;
        private Point t;
        private double an;
        private StraightArrowPolygon attackPoly;
        private Color attackColor;
        private int entityId;
        private int targetType;
        private int targetId;
        private String attackerDesc;
        private String targetDesc;
        ArrayList<String> weaponDescs = new ArrayList<String>();
        private final Entity ae;
        private final Targetable target;

        public AttackSprite(AttackAction attack) {
            attacks.add(attack);
            entityId = attack.getEntityId();
            targetType = attack.getTargetType();
            targetId = attack.getTargetId();
            ae = game.getEntity(attack.getEntityId());
            target = game.getTarget(targetType, targetId);

            // color?
            attackColor = PlayerColors.getColor(ae.getOwner().getColorIndex());
            // angle of line connecting two hexes
            an = (ae.getPosition().radian(target.getPosition()) + (Math.PI * 1.5))
            % (Math.PI * 2); // angle
            makePoly();

            // set bounds
            bounds = new Rectangle(attackPoly.getBounds());
            bounds.setSize(bounds.getSize().width + 1,
                    bounds.getSize().height + 1);
            // move poly to upper right of image
            attackPoly.translate(-bounds.getLocation().x,
                    -bounds.getLocation().y);

            // set names & stuff
            attackerDesc = ae.getDisplayName();
            targetDesc = target.getDisplayName();
            if (attack instanceof WeaponAttackAction) {
                addWeapon((WeaponAttackAction) attack);
            }
            if (attack instanceof KickAttackAction) {
                addWeapon((KickAttackAction) attack);
            }
            if (attack instanceof PunchAttackAction) {
                addWeapon((PunchAttackAction) attack);
            }
            if (attack instanceof PushAttackAction) {
                addWeapon((PushAttackAction) attack);
            }
            if (attack instanceof ClubAttackAction) {
                addWeapon((ClubAttackAction) attack);
            }
            if (attack instanceof ChargeAttackAction) {
                addWeapon((ChargeAttackAction) attack);
            }
            if (attack instanceof DfaAttackAction) {
                addWeapon((DfaAttackAction) attack);
            }
            if (attack instanceof ProtomechPhysicalAttackAction) {
                addWeapon((ProtomechPhysicalAttackAction) attack);
            }
            if (attack instanceof SearchlightAttackAction) {
                addWeapon((SearchlightAttackAction) attack);
            }

            // nullify image
            image = null;
        }

        private void makePoly() {
            // make a polygon
            a = getHexLocation(ae.getPosition());
            t = getHexLocation(target.getPosition());
            // OK, that is actually not good. I do not like hard coded figures.
            // HEX_W/2 - x distance in pixels from origin of hex bounding box to
            // the center of hex.
            // HEX_H/2 - y distance in pixels from origin of hex bounding box to
            // the center of hex.
            // 18 - is actually 36/2 - we do not want arrows to start and end
            // directly
            // in the centes of hex and hiding mek under.

            a.x = a.x + (int) (HEX_W / 2 * scale)
            + (int) Math.round(Math.cos(an) * (int) (18 * scale));
            t.x = t.x + (int) (HEX_W / 2 * scale)
            - (int) Math.round(Math.cos(an) * (int) (18 * scale));
            a.y = a.y + (int) (HEX_H / 2 * scale)
            + (int) Math.round(Math.sin(an) * (int) (18 * scale));
            t.y = t.y + (int) (HEX_H / 2 * scale)
            - (int) Math.round(Math.sin(an) * (int) (18 * scale));

            // Checking if given attack is mutual. In this case we building
            // halved arrow
            if (isMutualAttack()) {
                attackPoly = new StraightArrowPolygon(a, t, (int) (8 * scale),
                        (int) (12 * scale), true);
            } else {
                attackPoly = new StraightArrowPolygon(a, t, (int) (4 * scale),
                        (int) (8 * scale), false);
            }
        }

        @Override
        public Rectangle getBounds() {
            makePoly();
            // set bounds
            bounds = new Rectangle(attackPoly.getBounds());
            bounds.setSize(bounds.getSize().width + 1,
                    bounds.getSize().height + 1);
            // move poly to upper right of image
            attackPoly.translate(-bounds.getLocation().x,
                    -bounds.getLocation().y);

            return bounds;
        }

        /**
         * If we have build full arrow already with single attack and have got
         * counter attack from our target lately - lets change arrow to halved.
         */
        public void rebuildToHalvedPolygon() {
            attackPoly = new StraightArrowPolygon(a, t, (int) (8 * scale),
                    (int) (12 * scale), true);
            // set bounds
            bounds = new Rectangle(attackPoly.getBounds());
            bounds.setSize(bounds.getSize().width + 1,
                    bounds.getSize().height + 1);
            // move poly to upper right of image
            attackPoly.translate(-bounds.getLocation().x,
                    -bounds.getLocation().y);
        }

        /**
         * Cheking if attack is mutual and changing target arrow to half-arrow
         */
        private boolean isMutualAttack() {
            for (AttackSprite sprite : attackSprites) {
                if ((sprite.getEntityId() == targetId)
                        && (sprite.getTargetId() == entityId)) {
                    sprite.rebuildToHalvedPolygon();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void prepare() {
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void drawOnto(Graphics g, int x, int y, ImageObserver observer) {
            Polygon drawPoly = new Polygon(attackPoly.xpoints,
                    attackPoly.ypoints, attackPoly.npoints);
            drawPoly.translate(x, y);

            g.setColor(attackColor);
            g.fillPolygon(drawPoly);
            g.setColor(Color.gray.darker());
            g.drawPolygon(drawPoly);
        }

        /**
         * Return true if the point is inside our polygon
         */
        @Override
        public boolean isInside(Point point) {
            return attackPoly.contains(point.x + view.x - bounds.x - offset.x,
                    point.y + view.y - bounds.y - offset.y);
        }

        public int getEntityId() {
            return entityId;
        }

        public int getTargetId() {
            return targetId;
        }

        /**
         * Adds a weapon to this attack
         */
        public void addWeapon(WeaponAttackAction attack) {
            final Entity entity = game.getEntity(attack.getEntityId());
            final WeaponType wtype = (WeaponType) entity.getEquipment(
                    attack.getWeaponId()).getType();
            final String roll = attack.toHit(game).getValueAsString();
            final String table = attack.toHit(game).getTableDesc();
            weaponDescs
            .add(wtype.getName()
                    + Messages.getString("BoardView1.needs") + roll + " " + table); //$NON-NLS-1$
        }

        public void addWeapon(KickAttackAction attack) {
            String bufer = ""; //$NON-NLS-1$
            String rollLeft = ""; //$NON-NLS-1$
            String rollRight = ""; //$NON-NLS-1$
            final int leg = attack.getLeg();
            switch (leg) {
            case KickAttackAction.BOTH:
                rollLeft = KickAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), KickAttackAction.LEFT)
                                .getValueAsString();
                rollRight = KickAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), KickAttackAction.RIGHT)
                                .getValueAsString();
                bufer = Messages
                .getString(
                        "BoardView1.kickBoth", new Object[] { rollLeft, rollRight }); //$NON-NLS-1$
                break;
            case KickAttackAction.LEFT:
                rollLeft = KickAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), KickAttackAction.LEFT)
                                .getValueAsString();
                bufer = Messages.getString(
                        "BoardView1.kickLeft", new Object[] { rollLeft }); //$NON-NLS-1$
                break;
            case KickAttackAction.RIGHT:
                rollRight = KickAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), KickAttackAction.RIGHT)
                                .getValueAsString();
                bufer = Messages.getString(
                        "BoardView1.kickRight", new Object[] { rollRight }); //$NON-NLS-1$
                break;
            }
            weaponDescs.add(bufer);
        }

        public void addWeapon(PunchAttackAction attack) {
            String bufer = ""; //$NON-NLS-1$
            String rollLeft = ""; //$NON-NLS-1$
            String rollRight = ""; //$NON-NLS-1$
            final int arm = attack.getArm();
            switch (arm) {
            case PunchAttackAction.BOTH:
                rollLeft = PunchAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), PunchAttackAction.LEFT)
                                .getValueAsString();
                rollRight = PunchAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), PunchAttackAction.RIGHT)
                                .getValueAsString();
                bufer = Messages
                .getString(
                        "BoardView1.punchBoth", new Object[] { rollLeft, rollRight }); //$NON-NLS-1$
                break;
            case PunchAttackAction.LEFT:
                rollLeft = PunchAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), PunchAttackAction.LEFT)
                                .getValueAsString();
                bufer = Messages.getString(
                        "BoardView1.punchLeft", new Object[] { rollLeft }); //$NON-NLS-1$
                break;
            case PunchAttackAction.RIGHT:
                rollRight = PunchAttackAction.toHit(
                        game,
                        attack.getEntityId(),
                        game.getTarget(attack.getTargetType(), attack
                                .getTargetId()), PunchAttackAction.RIGHT)
                                .getValueAsString();
                bufer = Messages
                .getString(
                        "BoardView1.punchRight", new Object[] { rollRight }); //$NON-NLS-1$
                break;
            }
            weaponDescs.add(bufer);
        }

        public void addWeapon(PushAttackAction attack) {
            final String roll = attack.toHit(game).getValueAsString();
            weaponDescs.add(Messages.getString(
                    "BoardView1.push", new Object[] { roll })); //$NON-NLS-1$
        }

        public void addWeapon(ClubAttackAction attack) {
            final String roll = attack.toHit(game).getValueAsString();
            final String club = attack.getClub().getName();
            weaponDescs.add(Messages.getString(
                    "BoardView1.hit", new Object[] { club, roll })); //$NON-NLS-1$
        }

        public void addWeapon(ChargeAttackAction attack) {
            final String roll = attack.toHit(game).getValueAsString();
            weaponDescs.add(Messages.getString(
                    "BoardView1.charge", new Object[] { roll })); //$NON-NLS-1$
        }

        public void addWeapon(DfaAttackAction attack) {
            final String roll = attack.toHit(game).getValueAsString();
            weaponDescs.add(Messages.getString(
                    "BoardView1.DFA", new Object[] { roll })); //$NON-NLS-1$
        }

        public void addWeapon(ProtomechPhysicalAttackAction attack) {
            final String roll = attack.toHit(game).getValueAsString();
            weaponDescs.add(Messages.getString(
                    "BoardView1.proto", new Object[] { roll })); //$NON-NLS-1$
        }

        public void addWeapon(SearchlightAttackAction attack) {
            weaponDescs.add(Messages.getString("BoardView1.Searchlight"));
        }

        @Override
        public String[] getTooltip() {
            String[] tipStrings = new String[1 + weaponDescs.size()];
            int tip = 1;
            tipStrings[0] = attackerDesc
            + " " + Messages.getString("BoardView1.on") + " " + targetDesc; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            for (Iterator<String> i = weaponDescs.iterator(); i.hasNext();) {
                tipStrings[tip++] = i.next();
            }
            return tipStrings;
        }
    }

    /**
     * Sprite and info for movement vector (AT2 advanced movement).
     *  Does not actually use the image buffer
     * as this can be horribly inefficient for long diagonal lines.
     *
     * Appears as an arrow pointing to the hex this entity will move to
     * based on current movement vectors.
     * TODO: Different color depending upon whether entity has already moved
     * this turn
     *
     */
    private class MovementSprite extends Sprite
    {
        private Point a;
        private Point t;
        private double an;
        private StraightArrowPolygon movePoly;
        private Color moveColor;
        //private MovementVector mv;
        private int[] vectors;
        private Coords start;
        private Coords end;
        private Entity en;
        private int vel;

        public MovementSprite(Entity e, int[] v, Color col, boolean isCurrent) {
            //this.mv = en.getMV();

            en = e;
            vectors = v;//en.getVectors();
            //get the starting and ending position
            start = en.getPosition();
            end = Compute.getFinalPosition(start, vectors);

            //what is the velocity
            vel = 0;
            for (int element : v) {
                vel += element;
            }

            // color?
            //player colors
            moveColor = PlayerColors.getColor(en.getOwner().getColorIndex());
            //TODO: Its not going transparent. Oh well, it is a minor issue at the moment
            /*
            if(isCurrent) {
                int colour = col.getRGB();
                int transparency = GUIPreferences.getInstance().getInt(GUIPreferences.ADVANCED_ATTACK_ARROW_TRANSPARENCY);
                moveColor = new Color(colour | (transparency << 24), true);
            }
             */
            //red if offboard
            if(!game.getBoard().contains(end)) {
                int colour = 0xff0000; //red
                int transparency = GUIPreferences.getInstance().getInt(GUIPreferences.ADVANCED_ATTACK_ARROW_TRANSPARENCY);
                moveColor = new Color(colour | (transparency << 24), true);
            }
            //dark gray if done
            if(en.isDone()) {
                int colour = 0x696969; //gray
                int transparency = GUIPreferences.getInstance().getInt(GUIPreferences.ADVANCED_ATTACK_ARROW_TRANSPARENCY);
                moveColor = new Color(colour | (transparency << 24), true);
            }

            //moveColor = PlayerColors.getColor(en.getOwner().getColorIndex());
            //angle of line connecting two hexes
            an = (start.radian(end) + (Math.PI * 1.5)) % (Math.PI * 2); // angle
            makePoly();

            // set bounds
            bounds = new Rectangle(movePoly.getBounds());
            bounds.setSize(bounds.getSize().width + 1, bounds.getSize().height + 1);
            // move poly to upper right of image
            movePoly.translate(-bounds.getLocation().x, -bounds.getLocation().y);

            // nullify image
            image = null;
        }

        private void makePoly(){
            // make a polygon
            a = getHexLocation(start);
            t = getHexLocation(end);
            // OK, that is actually not good. I do not like hard coded figures.
            // HEX_W/2 - x distance in pixels from origin of hex bounding box to the center of hex.
            // HEX_H/2 - y distance in pixels from origin of hex bounding box to the center of hex.
            // 18 - is actually 36/2 - we do not want arrows to start and end directly
            // in the centes of hex and hiding mek under.

            a.x = a.x + (int)(HEX_W/2*scale) + (int)Math.round(Math.cos(an) * (int)(18*scale));
            t.x = t.x + (int)(HEX_W/2*scale) - (int)Math.round(Math.cos(an) * (int)(18*scale));
            a.y = a.y + (int)(HEX_H/2*scale) + (int)Math.round(Math.sin(an) * (int)(18*scale));
            t.y = t.y + (int)(HEX_H/2*scale) - (int)Math.round(Math.sin(an) * (int)(18*scale));
            movePoly = new StraightArrowPolygon(a, t, (int)(4*scale), (int)(8*scale), false);
        }

        @Override
        public Rectangle getBounds(){
            makePoly();
            // set bounds
            bounds = new Rectangle(movePoly.getBounds());
            bounds.setSize(bounds.getSize().width + 1, bounds.getSize().height + 1);
            // move poly to upper right of image
            movePoly.translate(-bounds.getLocation().x, -bounds.getLocation().y);

            return bounds;
        }

        @Override
        public void prepare() {

        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void drawOnto(Graphics g, int x, int y, ImageObserver observer) {
            //don't draw anything if the unit has no velocity

            if(vel == 0) {
                return;
            }

            Polygon drawPoly = new Polygon(movePoly.xpoints, movePoly.ypoints, movePoly.npoints);
            drawPoly.translate(x, y);

            g.setColor(moveColor);
            g.fillPolygon(drawPoly);
            g.setColor(Color.gray.darker());
            g.drawPolygon(drawPoly);


        }

        /**
         * Return true if the point is inside our polygon
         */
        @Override
        public boolean isInside(Point point) {
            return movePoly.contains(point.x + view.x - bounds.x - offset.x,
                    point.y + view.y - bounds.y - offset.y);
        }

        /*
        public String[] getTooltip() {
            String[] tipStrings = new String[1 + weaponDescs.size()];
            int tip = 1;
            tipStrings[0] = attackerDesc + " "+Messages.getString("BoardView1.on")+" " + targetDesc; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            for (Iterator<String> i = weaponDescs.iterator(); i.hasNext();) {
                tipStrings[tip++] = i.next();
            }
            return tipStrings;
        }
         */
    }

    /**
     * Determine if the tile manager's images have been loaded.
     *
     * @return <code>true</code> if all images have been loaded.
     *         <code>false</code> if more need to be loaded.
     */
    public boolean isTileImagesLoaded() {
        return tileManager.isLoaded();
    }

    public void setUseLOSTool(boolean use) {
        useLOSTool = use;
    }

    public TilesetManager getTilesetManager() {
        return tileManager;
    }

    public void drawRuler(Coords s, Coords e, Color sc, Color ec) {
        rulerStart = s;
        rulerEnd = e;
        rulerStartColor = sc;
        rulerEndColor = ec;

        repaint();
    }

    /**
     * @param lastCursor The lastCursor to set.
     */
    public void setLastCursor(Coords lastCursor) {
        this.lastCursor = lastCursor;
    }

    /**
     * @return Returns the lastCursor.
     */
    public Coords getLastCursor() {
        return lastCursor;
    }

    /**
     * @param highlighted The highlighted to set.
     */
    public void setHighlighted(Coords highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * @return Returns the highlighted.
     */
    public Coords getHighlighted() {
        return highlighted;
    }

    /**
     * @param selected The selected to set.
     */
    public void setSelected(Coords selected) {
        this.selected = selected;
    }

    /**
     * @return Returns the selected.
     */
    public Coords getSelected() {
        return selected;
    }

    /**
     * @param firstLOS The firstLOS to set.
     */
    public void setFirstLOS(Coords firstLOS) {
        this.firstLOS = firstLOS;
    }

    /**
     * @return Returns the firstLOS.
     */
    public Coords getFirstLOS() {
        return firstLOS;
    }

    /**
     * Determines if this Board contains the Coords, and if so, "selects" that
     * Coords.
     *
     * @param coords the Coords.
     */
    public void select(Coords coords) {
        if ((coords == null) || game.getBoard().contains(coords)) {
            setSelected(coords);
            moveCursor(selectedSprite, coords);
            moveCursor(firstLOSSprite, null);
            moveCursor(secondLOSSprite, null);
            processBoardViewEvent(new BoardViewEvent(this, coords, null,
                    BoardViewEvent.BOARD_HEX_SELECTED, 0));
        }
    }

    /**
     * "Selects" the specified Coords.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     */
    public void select(int x, int y) {
        select(new Coords(x, y));
    }

    /**
     * Determines if this Board contains the Coords, and if so, highlights that
     * Coords.
     *
     * @param coords the Coords.
     */
    public void highlight(Coords coords) {
        if ((coords == null) || game.getBoard().contains(coords)) {
            setHighlighted(coords);
            moveCursor(highlightSprite, coords);
            moveCursor(firstLOSSprite, null);
            moveCursor(secondLOSSprite, null);
            processBoardViewEvent(new BoardViewEvent(this, coords, null,
                    BoardViewEvent.BOARD_HEX_HIGHLIGHTED, 0));
        }
    }

    /**
     * Highlights the specified Coords.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     */
    public void highlight(int x, int y) {
        highlight(new Coords(x, y));
    }

    /**
     * Determines if this Board contains the Coords, and if so, "cursors" that
     * Coords.
     *
     * @param coords the Coords.
     */
    public void cursor(Coords coords) {
        if ((coords == null) || game.getBoard().contains(coords)) {
            if ((getLastCursor() == null) || (coords == null)
                    || !coords.equals(getLastCursor())) {
                setLastCursor(coords);
                moveCursor(cursorSprite, coords);
                moveCursor(firstLOSSprite, null);
                moveCursor(secondLOSSprite, null);
                processBoardViewEvent(new BoardViewEvent(this, coords, null,
                        BoardViewEvent.BOARD_HEX_CURSOR, 0));
            } else {
                setLastCursor(coords);
            }
        }
    }

    /**
     * "Cursors" the specified Coords.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     */
    public void cursor(int x, int y) {
        cursor(new Coords(x, y));
    }

    public void checkLOS(Coords c) {
        if ((c == null) || game.getBoard().contains(c)) {
            if (getFirstLOS() == null) {
                setFirstLOS(c);
                firstLOSHex(c);
                processBoardViewEvent(new BoardViewEvent(this, c, null,
                        BoardViewEvent.BOARD_FIRST_LOS_HEX, 0));
            } else {
                secondLOSHex(c, getFirstLOS());
                processBoardViewEvent(new BoardViewEvent(this, c, null,
                        BoardViewEvent.BOARD_SECOND_LOS_HEX, 0));
                setFirstLOS(null);
            }
        }
    }

    /**
     * Determines if this Board contains the (x, y) Coords, and if so, notifies
     * listeners about the specified mouse action.
     */
    public void mouseAction(int x, int y, int mtype, int modifiers) {
        if (game.getBoard().contains(x, y)) {
            Coords c = new Coords(x, y);
            switch (mtype) {
            case BOARD_HEX_CLICK:
                if ((modifiers & java.awt.event.InputEvent.CTRL_MASK) != 0) {
                    checkLOS(c);
                } else {
                    processBoardViewEvent(new BoardViewEvent(this, c, null,
                            BoardViewEvent.BOARD_HEX_CLICKED, modifiers));
                }
                break;
            case BOARD_HEX_DOUBLECLICK:
                processBoardViewEvent(new BoardViewEvent(this, c, null,
                        BoardViewEvent.BOARD_HEX_DOUBLECLICKED, modifiers));
                break;
            case BOARD_HEX_DRAG:
                processBoardViewEvent(new BoardViewEvent(this, c, null,
                        BoardViewEvent.BOARD_HEX_DRAGGED, modifiers));
                break;
            case BOARD_HEX_POPUP :
                processBoardViewEvent(new BoardViewEvent(this, c, null, BoardViewEvent.BOARD_HEX_POPUP, modifiers));
                break;
            }
        }
    }

    /**
     * Notifies listeners about the specified mouse action.
     *
     * @param coords the Coords.
     */
    public void mouseAction(Coords coords, int mtype, int modifiers) {
        mouseAction(coords.x, coords.y, mtype, modifiers);
    }

    /**
     * Return, whether a popup may be drawn, this currently means, whether no
     * scrolling took place.
     */
    public boolean mayDrawPopup() {
        return !scrolled;
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.BoardListener#boardNewBoard(megamek.common.BoardEvent)
     */
    public void boardNewBoard(BoardEvent b) {
        updateBoard();
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.BoardListener#boardChangedHex(megamek.common.BoardEvent)
     */
    public synchronized void boardChangedHex(BoardEvent b) {
        IHex hex = game.getBoard().getHex(b.getCoords());
        tileManager.clearHex(hex);
        tileManager.waitForHex(hex);
        if (boardGraph != null) {
            redrawAround(b.getCoords());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.BoardListener#boardChangedHex(megamek.common.BoardEvent)
     */
    public synchronized void boardChangedAllHexes(BoardEvent b) {
        updateBoardImage();
    }

    private GameListener gameListener = new GameListenerAdapter() {

        @Override
        public void gameEntityNew(GameEntityNewEvent e) {
            updateEcmList();
            redrawAllEntities();
            if(game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                refreshMoveVectors();
            }
        }

        @Override
        public void gameEntityRemove(GameEntityRemoveEvent e) {
            updateEcmList();
            redrawAllEntities();
            if(game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                refreshMoveVectors();
            }
        }

        @Override
        public void gameEntityChange(GameEntityChangeEvent e) {
            Vector<UnitLocation> mp = e.getMovePath();
            updateEcmList();
            if (e.getEntity().hasActiveECM()) {
                redrawAllEntities();
            }
            if(game.getPhase() == IGame.Phase.PHASE_MOVEMENT) {
                refreshMoveVectors();
            }
            if ((mp != null) && (mp.size() > 0)
                    && GUIPreferences.getInstance().getShowMoveStep()) {
                addMovingUnit(e.getEntity(), mp);
            } else {
                redrawEntity(e.getEntity());
            }
        }

        @Override
        public void gameNewAction(GameNewActionEvent e) {
            EntityAction ea = e.getAction();
            if (ea instanceof AttackAction) {
                addAttack((AttackAction) ea);
            }
        }

        @Override
        public void gameBoardNew(GameBoardNewEvent e) {
            IBoard b = e.getOldBoard();
            if (b != null) {
                b.removeBoardListener(BoardView1.this);
            }
            b = e.getNewBoard();
            if (b != null) {
                b.addBoardListener(BoardView1.this);
            }
            updateBoard();
        }

        @Override
        public void gameBoardChanged(GameBoardChangeEvent e) {
            boardChanged();
        }

        @Override
        public void gamePhaseChange(GamePhaseChangeEvent e) {
            refreshAttacks();
            switch (e.getNewPhase()) {
            case PHASE_MOVEMENT:
                refreshMoveVectors();
            case PHASE_FIRING:
                clearAllMoveVectors();
            case PHASE_PHYSICAL:
                refreshAttacks();
                break;
            case PHASE_INITIATIVE:
                clearAllAttacks();
                break;
            case PHASE_END:
            case PHASE_VICTORY:
                clearSprites();
            }
        }
    };

    synchronized void boardChanged() {
        redrawAllEntities();
    }

    void clearSprites() {
        pathSprites.clear();
        attackSprites.clear();
        C3Sprites.clear();
        movementSprites.clear();

    }

    protected synchronized void updateBoard() {
        updateBoardSize();
        if (backGraph != null) {
            backGraph.dispose();
        }
        backGraph = null;
        backImage = null;
        backSize = null;
        boardImage = null;
        if (boardGraph != null) {
            boardGraph.dispose();
        }
        boardGraph = null;
        redrawAllEntities();
    }

    /**
     * the old redrawworker converted to a runnable which is called now and then
     * from the event thread
     */
    protected class RedrawWorker implements Runnable {

        protected long lastTime = System.currentTimeMillis();
        protected long currentTime = System.currentTimeMillis();

        /**
         * somebody please document wth this is doing..- itmo
         */
        public void run() {
            currentTime = System.currentTimeMillis();
            if (isShowing()) {
                boolean redraw = false;
                for (int i = 0; i < displayables.size(); i++) {
                    IDisplayable disp = displayables.get(i);
                    if (!disp.isSliding()) {
                        disp.setIdleTime(currentTime - lastTime, true);
                    } else {
                        redraw = redraw || disp.slide();
                    }
                }
                if (backSize != null) {
                    redraw = redraw || doMoveUnits(currentTime - lastTime);
                    redraw = redraw || doScroll();
                    checkTooltip();
                } else {
                    repaint(100);
                }
                if (redraw) {
                    repaint();
                }
            }
            lastTime = currentTime;
        }
    }

    public synchronized void WeaponSelected(MechDisplayEvent b) {
        selectedEntity = b.getEntity();
        selectedWeapon = b.getEquip();
        repaint(100);
    }

    private class EcmBubble extends Coords {
        private static final long serialVersionUID = -1605350790342525964L;
        int range;
        int tint;
        int direction;

        public EcmBubble(Coords c, int range, int tint) {
            super(c);
            this.range = range;
            this.tint = tint;
            direction = -1;
        }

        public EcmBubble(Coords c, int range, int tint, int direction) {
            super(c);
            this.range = range;
            this.tint = tint;
            this.direction = direction;
        }
    }

    // This is expensive, so precalculate when entity changes
    public void updateEcmList() {
        ArrayList<EcmBubble> list = new ArrayList<EcmBubble>();
        for (Enumeration<Entity> e = game.getEntities(); e.hasMoreElements();) {
            Entity ent = e.nextElement();
            Coords entPos = ent.getPosition();
            int range = ent.getECMRange();
            boolean deployed = ent.isDeployed();
            boolean offboard = ent.isOffBoard();
            if((entPos == null) && (ent.getTransportId() != Entity.NONE)) {
                Entity carrier = game.getEntity(ent.getTransportId());
                if((null != carrier) && carrier.loadedUnitsHaveActiveECM()) {
                    entPos = carrier.getPosition();
                    deployed = carrier.isDeployed();
                    offboard = carrier.isOffBoard();
                }
            }
            if ((entPos == null) || !deployed
                    || offboard) {
                continue;
            }
            if (range != Entity.NONE) {
                int tint = PlayerColors.getColorRGB(ent.getOwner()
                        .getColorIndex());
                list.add(new EcmBubble(entPos, range, tint));
            }
            if(game.getBoard().inSpace()) {
                //then BAP is also ECCM so it needs a bubble
                range = ent.getBAPRange();
                int direction = -1;
                if (range != Entity.NONE) {
                    if(range > 6) {
                        direction = ent.getFacing();
                    }
                    int tint = PlayerColors.getColorRGB(ent.getOwner()
                            .getColorIndex());
                    list.add(new EcmBubble(entPos, range, tint, direction));
                }
            }
        }
        HashMap<Coords, Integer> table = new HashMap<Coords, Integer>();
        for (EcmBubble b : list) {
            Integer col = new Integer(b.tint);
            for (int x = -b.range; x <= b.range; x++) {
                for (int y = -b.range; y <= b.range; y++) {
                    Coords c = new Coords(x + b.x, y + b.y);
                    // clip rectangle to hexagon
                    if ((b.distance(c) <= b.range) && ((b.direction == -1) || Compute.isInArc(b, b.direction, c, Compute.ARC_NOSE))) {
                        Integer tint = table.get(c);
                        if (tint == null) {
                            table.put(c, col);
                        } else if (tint.intValue() != b.tint) {
                            int red1 = (tint.intValue() >> 16) & 0xff;
                            int green1 = (tint.intValue() >> 8) & 0xff;
                            int blue1 = tint.intValue() & 0xff;
                            int red2 = (b.tint >> 16) & 0xff;
                            int green2 = (b.tint >> 8) & 0xff;
                            int blue2 = b.tint & 0xff;
                            red1 = (red1 + red2) / 2;
                            green1 = (green1 + green2) / 2;
                            blue1 = (blue1 + blue2) / 2;
                            table.put(c, new Integer((red1 << 16)
                                    | (green1 << 8) | blue1));
                        }
                    }
                }
            }
        }
        synchronized (this) {
            ecmHexes = table;
            dirtyBoard = true;
        }
        repaint(100);
    }

    /**
     * Have the player select an Entity from the entities at the given coords.
     *
     * @param pos - the <code>Coords</code> containing targets.
     */
    private Entity chooseEntity(Coords pos) {

        // Assume that we have *no* choice.
        Entity choice = null;

        // Get the available choices.
        Enumeration<Entity> choices = game.getEntities(pos);

        // Convert the choices into a List of targets.
        Vector<Entity> entities = new Vector<Entity>();
        while (choices.hasMoreElements()) {
            entities.addElement(choices.nextElement());
        }

        // Do we have a single choice?
        if (entities.size() == 1) {
            // Return that choice.
            choice = entities.elementAt(0);
        }

        // If we have multiple choices, display a selection dialog.
        else if (entities.size() > 1) {
            String[] names = new String[entities.size()];
            for (int loop = 0; loop < names.length; loop++) {
                names[loop] = entities.elementAt(loop).getDisplayName();
            }
            SingleChoiceDialog choiceDialog = new SingleChoiceDialog(
                    (Frame)SwingUtilities.getAncestorOfClass(Frame.class, this),
                    Messages.getString("BoardView1.ChooseEntityDialog.title"), //$NON-NLS-1$
                    Messages
                    .getString(
                            "BoardView1.ChooseEntityDialog.message", new Object[] { pos.getBoardNum() }), //$NON-NLS-1$
                            names);
            choiceDialog.setVisible(true);
            if (choiceDialog.getAnswer() == true) {
                choice = entities.elementAt(choiceDialog.getChoice());
            }
        } // End have-choices

        // Return the chosen unit.
        return choice;
    }

    public Component getComponent() {
        // Scrollbars are broken for "Brandon Drew" <brandx0@hotmail.com>
        if (System.getProperty
                ("megamek.client.clientgui.hidescrollbars", "false").equals //$NON-NLS-1$ //$NON-NLS-2$
                ("true")) {
            return this;
        }
        if (scroller != null) {
            return scroller;
        }

        // Place the board viewer in a set of scrollbars.
        scroller = new Panel();
        scroller.setLayout(new BorderLayout());
        vScrollbar = new Scrollbar(Scrollbar.VERTICAL);
        hScrollbar = new Scrollbar(Scrollbar.HORIZONTAL);
        scroller.add(this, BorderLayout.CENTER);

        // Assign the scrollbars to the board viewer.
        scroller.add(vScrollbar, BorderLayout.EAST);
        scroller.add(hScrollbar, BorderLayout.SOUTH);

        // When the scroll bars are adjusted, update our offset.
        vScrollbar.addAdjustmentListener (this);
        hScrollbar.addAdjustmentListener (this);

        return scroller;
    }

    public void refreshDisplayables() {
        repaint();
    }

    public void showPopup(Object popup, Coords c) {
        Point p = getHexLocation(c);
        p.x += (int)(HEX_WC*scale)-view.x;
        p.y += (int)(HEX_H*scale/2)-view.y;
        if (((PopupMenu)popup).getParent() == null) {
            add((PopupMenu)popup);
        }
        ((PopupMenu)popup).show(this, p.x, p.y);
    }

    public void refreshMinefields() {
        repaint();
    }

    public boolean toggleIsometric() {
        // Only Implemented on SWING gui
        return false;
    }
}
