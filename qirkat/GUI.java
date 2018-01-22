package qirkat;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Observable;
import java.util.Observer;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import static qirkat.PieceColor.*;

/** The GUI for the Qirkat game.
 *  @author Chris Sreesangom
 */
class GUI extends TopLevel implements Observer, Reporter {

    /* The implementation strategy applied here is to make it as
     * unnecessary as possible for the rest of the program to know that it
     * is interacting with a GUI as opposed to a terminal.
     *
     * To this end, we first have made Board observable, so that the
     * GUI gets notified of changes to a Game's board and can interrogate
     * it as needed, while the Game and Board themselves need not be aware
     * that it is being watched.
     *
     * Second, instead of creating a new API by which the GUI communicates
     * with a Game, we instead simply arrange to make the GUI's input look
     * like that from a terminal, so that we can reuse all the machinery
     * in the rest of the program to interpret and execute commands.  The
     * GUI simply composes commands (such as "start" or "clear") and
     * writes them to a Writer that (using the Java library's PipedReader
     * and PipedWriter classes) provides input to the Game using exactly the
     * same API as would be used to read from a terminal. Thus, a simple
     * Manual player can handle all commands and moves from the GUI.
     *
     * See also Main.java for how this might get set up.
     */

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 300;

    /** A new display observing MODEL, with TITLE as its window title.
     *  It uses OUTCOMMANDS to send commands to a game instance, using the
     *  same commands as the text format for Qirkat. */
    GUI(String title, Board model, Writer outCommands) {
        super(title, true);
        addMenuButton("Game->New Game", this::clear);
        addMenuButton("Game->Start", this::startGame);
        addMenuButton("Game->Undo", this::undo);
        addMenuButton("Game->Set Pieces", this::setPieces);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Options->Seed...", this::setSeed);
        addMenuRadioButton("White->AI White", "White", false, this::setAuto);
        addMenuRadioButton("Black->AI Black", "Black", true, this::setAuto);
        addMenuRadioButton("White->Manual White", "White",
                true, this::setManual);
        addMenuRadioButton("Black->Manual Black", "Black",
                false, this::setManual);
        addMenuButton("Info->Help", this::help);
        _model = model;
        _widget = new BoardWidget(model);
        _out = new PrintWriter(outCommands, true);
        add(_widget,
            new LayoutSpec("height", "1",
                           "width", "REMAINDER",
                           "ileft", 5, "itop", 5, "iright", 5,
                           "ibottom", 5));
        setMinimumSize(MIN_SIZE, MIN_SIZE);
        _widget.addObserver(this);
        _model.addObserver(this);
        _setPiecesWindow = new SetPiecesWindow(_out);
        _whiteIsManual = true;
        _blackIsManual = false;

    }

    /** Execute start game button function. */
    private synchronized  void startGame(String unused) {
        _out.printf("start%n");
    }
    /** Execute the "Clear button function. */
    private synchronized  void clear(String unused) {
        _out.printf("clear%n");
    }
    /** Execute the "Quit" button function. */
    private synchronized void quit(String unused) {
        _out.printf("quit%n");
    }
    /** Execute the "Undo" button function. */
    private synchronized void undo(String unused) {
        _out.printf("undo%n");
    }
    /** Execute the "Help" button function. */
    private synchronized  void help(String unused) {
        _out.printf("help%n");
    }
    /** Set a color as AI, color given by LABEL. */
    private synchronized void setAuto(String label) {
        if (label.equals("White->AI White")) {
            _whiteIsManual = false;
            _out.printf("auto white%n");
        } else {
            _blackIsManual = false;
            _out.printf("auto black%n");
        }
    }
    /** Set a color as AI, color given by LABEL. */
    private synchronized void setManual(String label) {
        if (label.equals("Black->Manual Black")) {
            _blackIsManual = true;
            _out.printf("manual black%n");
        } else {
            _whiteIsManual = true;
            _out.printf("manual white%n");
        }
    }
    /** Execute Seed... command. */
    private synchronized void setSeed(String unused) {
        String resp =
            getTextInput("Random Seed", "Get Seed", "question", "");
        if (resp == null) {
            return;
        }
        try {
            long s = Long.parseLong(resp);
            _out.printf("seed %d%n", s);
        } catch (NumberFormatException excp) {
            return;
        }
    }

    /** Execute Set Pieces command. */
    private void setPieces(String unused) {
        _setPiecesWindow.display(true);
    }

    /** Display text in file NAME in a box titled TITLE. */
    private void displayText(String name, String title) {
        InputStream input =
            Game.class.getClassLoader().getResourceAsStream(name);
        if (input != null) {
            try {
                BufferedReader r
                    = new BufferedReader(new InputStreamReader(input));
                char[] buffer = new char[1 << 15];
                int len = r.read(buffer);
                showMessage(new String(buffer, 0, len), title, "plain");
                r.close();
            } catch (IOException e) {
                /* Ignore IOException */
            }
        }
    }

    @Override
    public void errMsg(String format, Object... args) {
        Formatter message = new Formatter();
        message.format(format, args);
        displayText(message.toString() , "Error");
    }

    @Override
    public void outcomeMsg(String format, Object... args) {
        if (format.equals("White wins.")) {
            displayText("qirkat/whitewin.txt", "Game Over!");
        } else if (format.equals("Black wins.")) {
            displayText("qirkat/blackwin.txt", "Game Over!");
        } else {
            displayText("qirkat/guihelp.txt", "Help");
        }
    }

    @Override
    public void moveMsg(String format, Object... args) {
    }

    @Override
    public void update(Observable obs, Object arg) {
        if (obs == _widget) {
            if (_model.whoseMove() == WHITE
                    && !_whiteIsManual) {
                return;
            }
            if (_model.whoseMove() == BLACK
                    && !_blackIsManual) {
                return;
            }
            String sq = (String) arg;
            ArrayList<Move> legalMoves = _model.getMoves();
            Move mov = Move.parseVestMove(sq);
            if (_selectedMove == null) {
                ArrayList<Move> possibleMoves = new ArrayList<Move>();
                for (Move legalMove : legalMoves) {
                    if (legalMove.fromIndex()
                            == mov.fromIndex()) {
                        possibleMoves.add(legalMove);
                    }
                }
                if (possibleMoves.size() > 0) {
                    selectMove(mov, possibleMoves);
                } else {
                    badSelectMove(mov);
                }
            } else {
                Move concat = Move.move(_selectedMove, mov);
                if (_model.getLegalMoves().contains(concat)) {
                    deselect();
                    _out.printf("%s%n", concat.toString());
                    return;
                }
                ArrayList<Move> newPossibleMoves = new ArrayList<>();
                Move compare = concat;
                while (compare.jumpTail() != null) {
                    compare = compare.jumpTail();
                }
                for (Move possMov: _possibleMoves) {
                    if (possMov.toIndex()
                            == compare.toIndex()) {
                        newPossibleMoves.add(possMov.jumpTail());
                    }
                }
                if (newPossibleMoves.size() == 0) {
                    deselect();
                    return;
                }
                selectMove(concat, newPossibleMoves);
            }
        }
    }

    /** Helper function for deselecting squares and getting rid
     *  of possible moves showing. */
    private void deselect() {
        _selectedMove = null;
        _possibleMoves = null;
        _widget.deselect();
    }

    /** Make MOV the user-selected move (no move if null)
     *  and also make POSSIBLEMOVES show up when MOV is selected. */
    private void selectMove(Move mov, ArrayList<Move> possibleMoves) {
        _selectedMove = mov;
        _possibleMoves = possibleMoves;
        _widget.indicateMove(mov, possibleMoves);
    }
    /** Make MOV the user-selected bad move. */
    private void badSelectMove(Move mov) {
        _widget.indicateBadMove(mov);
    }

    /** Contains the drawing logic for the Qirkat model. */
    private BoardWidget _widget;
    /** The model of the game. */
    private Board _model;
    /** Output sink for sending commands to a game. */
    private PrintWriter _out;
    /** Move selected by clicking. */
    private Move _selectedMove;
    /** ArrayList storing possible moves from _selectedMove. */
    private ArrayList<Move> _possibleMoves;
    /** A top level for the set pieces window. */
    private TopLevel _setPiecesWindow;
    /** Boolean indicating whether white is manual. */
    private boolean _whiteIsManual;
    /** Boolean indicating whether black is manual. */
    private boolean _blackIsManual;
}
