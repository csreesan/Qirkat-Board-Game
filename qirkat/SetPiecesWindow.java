package qirkat;

import ucb.gui2.LayoutSpec;
import ucb.gui2.TopLevel;

import java.io.PrintWriter;
import java.util.Formatter;


/** The is the window for setting pieces
 *  in the qirkat game GUI.
 *  @author Chris Sreesangom
 */
class SetPiecesWindow extends TopLevel {

    /** A new set pieces window using the same
     * *  print writer OUT as the GUI. */
    SetPiecesWindow(PrintWriter out) {
        super("Set Pieces", true);
        createSetColorButtons();
        _out = out;
        _squareColor = new String[BOARD_SIZE];
        _color = "white";
        setMinimumSize(MIN_BOARD_WIDTH, MIN_BOARD_HEIGHT);
        addButton("set", this::set,
                new LayoutSpec("height", 1, "width", 1, "x",
                        X_BOTTOM_BUTTONS, "y", Y_BOTTOM_BUTTONS));
        addRadioButton("White Start", "Start", true, this::setStart,
                new LayoutSpec("height", 1, "width", 1, "x",
                        2 * X_BOTTOM_BUTTONS, "y", Y_BOTTOM_BUTTONS));
        addRadioButton("Black Start", "Start", false, this::setStart,
                new LayoutSpec("height", 1, "width", 1, "x",
                        3 * X_BOTTOM_BUTTONS, "y", Y_BOTTOM_BUTTONS));
    }
    /** The size of the board. */
    private static final int BOARD_SIZE = 25;
    /** The minimum width of window. */
    private static final int MIN_BOARD_WIDTH = 700;
    /** The minimum height of window. */
    private static final int MIN_BOARD_HEIGHT = 300;
    /** X coordinate for buttons at the bottom. */
    private static final int X_BOTTOM_BUTTONS = 100;
    /** Y coordinate for buttons at the bottom. */
    private static final int Y_BOTTOM_BUTTONS = 100;
    /** Array of strings which stores the linearized index of
     *  each squares in order. */
    private static final String[] ALL_SQUARES = {"20", "21", "22", "23", "24",
        "15", "16", "17", "18", "19",
        "10", "11", "12", "13", "14",
        "05", "06", "07", "08", "09",
        "00", "01", "02", "03", "04"};
    /** Execute Set function to set the board and close
     *  this window. */
    private synchronized void set(String unused) {
        Formatter boardStringFormatter = new Formatter();
        for (String sq : _squareColor) {
            if (sq == null) {
                sq = "-";
            }
            boardStringFormatter.format(sq);
        }
        _out.printf("set %s %s%n", _color, boardStringFormatter.toString());
        this.display(false);
    }

    /** Execute Set Start function which sets the starting
     *  color to START. */
    private synchronized void setStart(String start) {
        if (start.equals("White Start")) {
            _color = "white";
        } else {
            _color = "black";
        }
    }
    /** Execute Set Empty function for sqaure SQ. */
    private synchronized void setEmpty(String sq) {
        sq = sq.substring(3);
        _squareColor[Integer.parseInt(sq)] = "-";
    }
    /** Execute Set White function for square SQ. */
    private synchronized void setWhite(String sq) {
        sq = sq.substring(1);
        _squareColor[Integer.parseInt(sq)] = "w";
    }
    /** Execute Set Black function for square SQ. */
    private synchronized void setBlack(String sq) {
        sq = sq.substring(1);
        _squareColor[Integer.parseInt(sq)] = "b";
    }

    /** Create radio buttons for setting the colors
     *  of the squares. */
    private void createSetColorButtons() {
        int x = 0;
        int y = 0;
        int squareNum = 0;
        for (String squareName : ALL_SQUARES) {
            addRadioButton("Emp" + squareName, squareName, true, this::setEmpty,
                    new LayoutSpec("height", 1,
                            "width", 1, "x", x, "y", y));
            addRadioButton("W" + squareName, squareName, false, this::setWhite,
                    new LayoutSpec("height", 1,
                            "width", 1, "x", x, "y", y + 2));
            addRadioButton("B" + squareName, squareName, false, this::setBlack,
                    new LayoutSpec("height", 1,
                            "width", 1, "x", x , "y", y + 4));
            x += 2;
            if (squareNum % 5 == 4) {
                y += 6;
                x = 0;
            }
            squareNum++;
        }
    }
    /** An array which stores the color of each square. */
    private String[] _squareColor;
    /** Variable which stores the print writer used to execute
     *  commands. */
    private PrintWriter _out;
    /** Variable storing the color of the starting move. */
    private String _color;
}
