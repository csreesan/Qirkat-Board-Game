package qirkat;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Observable;

import java.awt.event.MouseEvent;
import static qirkat.PieceColor.*;

/** Widget for displaying a Qirkat board.
 *  @author Chris Sreesangkom
 */
class BoardWidget extends Pad implements Observer {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Move.SIDE;
    /** Mid point of a square. */
    static final int MIDSQ = 25;
    /** Array of coordinate positions for each mid point of a piece
     *  for both x and y axes. */
    private static final int[] MID = {MIDSQ, MIDSQ + SQDIM,
        MIDSQ + 2 * SQDIM, MIDSQ + 3 * SQDIM, MIDSQ + 4 * SQDIM};
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;

    /** Color of white pieces. */
    private static final Color WHITE_COLOR = Color.WHITE;
    /** Color of "phantom" white pieces. */
    /** Color of black pieces. */
    private static final Color BLACK_COLOR = Color.BLACK;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = new Color(216, 123, 30);
    /** Color of selected squares which have moves. */
    private static final Color SELECTED_COLOR = new Color(182, 228, 156);
    /** Color of selected squares which don't have moves. */
    private static final Color BAD_SELECTED_COLOR = new Color(233, 2, 5);
    /** Color of possible move squares. */
    private static final Color POSS_COLOR = new Color(168, 228, 214);

    /** Stroke for lines.. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(3.0f);

    /** Stroke for outlining pieces. */
    private static final BasicStroke OUTLINE_STROKE = LINE_STROKE;

    /** Model being displayed. */
    private static Board _model;

    /** A new widget displaying MODEL. */
    BoardWidget(Board model) {
        _model = model;
        setMouseHandler("click", this::readMove);
        _model.addObserver(this);
        _dim = SQDIM * SIDE;
        setPreferredSize(_dim, _dim);
    }

    /** Indicate that the squares indicated by MOV are the currently selected
     *  squares for a pending move, and show POSSIBLEMOVES. */
    void indicateMove(Move mov, ArrayList<Move> possibleMoves) {
        _selectedMove = mov;
        _possibleMoves = possibleMoves;
        repaint();
    }
    /** Indicate the selection of a square that cannot be moved, by looking
     *  at MOV. */
    void indicateBadMove(Move mov) {
        if (_badSelectedMove != null) {
            _badSelectedMove = null;
            repaint();
            return;
        }
        _badSelectedMove = mov;
        repaint();
    }

    /** Opposite of indicate move, deselect the area. */
    void deselect() {
        _selectedMove = null;
        _possibleMoves = null;
        _badSelectedMove = null;
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawBackGround(g);
        if (_selectedMove != null) {
            _badSelectedMove = null;
            int k = _selectedMove.fromIndex();
            g.setColor(SELECTED_COLOR);
            g.fillRect(getX(k), getY(k), PIECE_RADIUS * 2, PIECE_RADIUS * 2);
            for (Move move: _possibleMoves) {
                g.setColor(POSS_COLOR);
                int i = move.toIndex();
                g.fillOval(getX(i), getY(i), PIECE_RADIUS * 2,
                        PIECE_RADIUS * 2);
            }
        }
        if (_badSelectedMove != null) {
            int k = _badSelectedMove.fromIndex();
            g.setColor(BAD_SELECTED_COLOR);
            g.fillRect(getX(k), getY(k), PIECE_RADIUS * 2, PIECE_RADIUS * 2);
        }
        drawPieces(g);

    }

    /** Helper function to draw back ground which includes filling
     *  in the rectangle, and drawing the lines on G. */
    private void drawBackGround(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);
        g.setColor(LINE_COLOR);
        g.setStroke(OUTLINE_STROKE);
        for (int mid: MID) {
            g.drawLine(mid, MIDSQ, mid, SIDE * SQDIM - MIDSQ);
            g.drawLine(MIDSQ, mid, SIDE * SQDIM - MIDSQ, mid);
        }
        g.drawLine(MIDSQ, MIDSQ, SIDE * SQDIM - MIDSQ, SIDE * SQDIM - MIDSQ);
        g.drawLine(MIDSQ, SIDE * SQDIM - MIDSQ, SIDE * SQDIM - MIDSQ, MIDSQ);
        g.drawLine(MIDSQ, MIDSQ + 2 * SQDIM, MIDSQ + 2 * SQDIM,
                SIDE * SQDIM - MIDSQ);
        g.drawLine(MIDSQ + 2 * SQDIM, SIDE * SQDIM - MIDSQ,
                SIDE * SQDIM - MIDSQ, MIDSQ + 2 * SQDIM);
        g.drawLine(SIDE * SQDIM - MIDSQ, MIDSQ + 2 * SQDIM,
                MIDSQ + 2 * SQDIM, MIDSQ);
        g.drawLine(MIDSQ + 2 * SQDIM, MIDSQ, MIDSQ, MIDSQ + 2 * SQDIM);
    }

    /** Helper function to draw the pieces on G. */
    private void drawPieces(Graphics2D g) {
        for (int k = 0; k < SIDE * SIDE; k++) {
            if (_model.get(k) == EMPTY) {
                continue;
            }
            if (_model.get(k) == WHITE) {
                g.setColor(WHITE_COLOR);
            } else {
                g.setColor(BLACK_COLOR);
            }
            g.fillOval(getX(k), getY(k), PIECE_RADIUS * 2, PIECE_RADIUS * 2);
        }
    }

    /** Notify observers of mouse's current position from click event WHERE. */
    private void readMove(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                setChanged();
                notifyObservers("" + mouseCol + mouseRow);
            }
        }
    }

    @Override
    public synchronized void update(Observable model, Object arg) {
        repaint();
    }

    /** Return the x coordinate of the piece at square K as integer. */
    private int getX(int k) {
        return (k % SIDE) * SQDIM + 3 * MIDSQ / 8;
    }

    /** Return the y coordinate of the piece at square K as integer. */
    private int getY(int k) {
        return (SIDE - (k / SIDE)) * SQDIM - 3 * SQDIM / 4;
    }

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** A partial Move indicating selected squares. */
    private Move _selectedMove;

    /** A partial Move for bad selected sqaures. */
    private Move _badSelectedMove;

    /** An ArrayList storing possible moves from _selectedMove. */
    private ArrayList<Move> _possibleMoves;
}
