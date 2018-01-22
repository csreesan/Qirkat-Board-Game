package qirkat;

import java.util.Observable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Formatter;
import java.util.Observer;

import static qirkat.PieceColor.*;
import static qirkat.Move.*;

/** A Qirkat board.   The squares are labeled by column (a char value between
 *  'a' and 'e') and row (a char value between '1' and '5'.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (with row 0 being the bottom row)
 *  counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Chris Sreesangkom
 */
class Board extends Observable {

    /** A new, cleared board at the start of the game. */
    Board() {
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        internalCopy(b);
    }

    /** Return a constant view of me (allows any access method, but no
     *  method that modifies it). */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions. */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;
        _illegalHorBlack = new ArrayList<>();
        _illegalHorWhite = new ArrayList<>();
        _boardsStack = new Stack<>();

        for (int k = 0; k < 10; k++) {
            set(k, WHITE);
        }
        set(10, BLACK);
        set(11, BLACK);
        set(12, EMPTY);
        set(13, WHITE);
        set(14, WHITE);
        for (int k = 15; k < _board.length; k++) {
            set(k, BLACK);
        }
        _legalMoves = getMoves();
        setChanged();
        notifyObservers();
    }

    /** Copy B into me. */
    void copy(Board b) {
        internalCopy(b);
    }

    /** Copy B into me. */
    private void internalCopy(Board b) {
        for (int i = 0; i < _board.length; i++) {
            set(i, b.get(i));
        }
        _illegalHorBlack = new ArrayList<>();
        _illegalHorWhite = new ArrayList<>();
        _boardsStack = new Stack<>();
        _legalMoves = new ArrayList<>();
        _whoseMove = b._whoseMove;
        _illegalHorBlack.addAll(b._illegalHorBlack);
        _illegalHorWhite.addAll(b._illegalHorWhite);
        _legalMoves.addAll(b._legalMoves);
        _gameOver = b._gameOver;
        _boardsStack.addAll(b._boardsStack);
    }

    /** Set my contents as defined by STR.  STR consists of 25 characters,
     *  each of which is b, w, or -, optionally interspersed with whitespace.
     *  These give the contents of the Board in row-major order, starting
     *  with the bottom row (row 1) and left column (column a). All squares
     *  are initialized to allow horizontal movement in either direction.
     *  NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }

        for (int k = 0; k < str.length(); k += 1) {
            switch (str.charAt(k)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b': case 'B':
                set(k, BLACK);
                break;
            case 'w': case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }
        _illegalHorBlack = new ArrayList<>();
        _illegalHorWhite = new ArrayList<>();
        _boardsStack = new Stack<>();
        _gameOver = false;
        _whoseMove = nextMove;
        _legalMoves = getMoves();
        _gameOver = !isMove();
        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if the current player has
     *  no moves. */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current contents of square C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.  */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return get(index(c, r));
    }

    /** Return the current contents of the square at linearized index K. */
    PieceColor get(int k) {
        assert validSquare(k);
        return _board[k];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'e', and
     *  '1' <= R <= '5'. */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Set get(K) to V, where K is the linearized index of a square. */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        _board[k] = v;
    }

    /** Return true iff MOV is legal on the current board. */
    boolean legalMove(Move mov) {
        return _legalMoves.contains(mov);
    }

    /** Return a list of all legal moves from the current position. */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /** Add all legal moves from the current position to MOVES. */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getJumps(moves, k);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getMoves(moves, k);
            }
        }
    }

    /** Add all legal non-capturing moves from the position
     *  with linearized index K to MOVES. */
    private void getMoves(ArrayList<Move> moves, int k) {
        PieceColor piece = get(k);
        if (piece != _whoseMove) {
            return;
        }
        if (piece == WHITE
                && row(k) == '5') {
            return;
        }
        if (piece == BLACK
                && row(k) == '1') {
            return;
        }
        for (int i: _allNeighbors) {
            if (!isPossibleMove(k, i)) {
                continue;
            }
            if (get(k + i) == EMPTY) {
                Move m = move(col(k), row(k), col(k + i), row(k + i));
                if (_whoseMove == WHITE
                        && _illegalHorWhite.contains(m)) {
                    continue;
                }
                if (_whoseMove == BLACK
                        && _illegalHorBlack.contains(m)) {
                    continue;
                }
                moves.add(m);
            }
        }
    }

    /** Add all legal captures from the position with linearized index K
     *  to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        PieceColor piece = get(k);
        if (piece != _whoseMove || !jumpPossible(k)) {
            return;
        }
        moves.addAll(getJumpsList(k));
    }

    /** Helper recursive function to help get all jump moves with next
     *  jumps as well for square K, return in the form of ArrayList. */
    private ArrayList<Move> getJumpsList(int k) {
        ArrayList<Move> result = new ArrayList<>();
        if (!this.jumpPossible(k)) {
            return result;
        }
        for (int i: _allNeighbors) {
            if (!isValidNeighbor(k, i)) {
                continue;
            }
            if (get(k + i) == _whoseMove.opposite()) {
                if (!isValidNeighbor(k + i, i)) {
                    continue;
                }
                if (get(k + i + i) == EMPTY) {
                    Move m = move(col(k), row(k),
                            col(k + i + i), row(k + i + i));
                    PieceColor initialK = get(k);
                    set(k, EMPTY);
                    set(k + i, EMPTY);
                    set(k + i + i, _whoseMove);
                    ArrayList<Move> nextJumps = getJumpsList(k + i + i);
                    set(k + i + i, EMPTY);
                    for (Move next: nextJumps) {
                        result.add(move(m, next));
                    }
                    if (nextJumps.size() == 0) {
                        result.add(m);
                    }
                    set(k, initialK);
                    set(k + i, _whoseMove.opposite());
                }
            }
        }
        return result;
    }

    /** Return true iff MOV is a valid jump sequence on the current board.
     *  MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     *  could be continued and are valid as far as they go.  */
    boolean checkJump(Move mov, boolean allowPartial) {
        if (mov == null) {
            return true;
        }
        if (_legalMoves.contains(mov)) {
            if (mov.jumpTail() == null) {
                return true;
            }
            return false;
        }
        return false;
    }

    /** Return true iff a jump is possible for a piece at position C R. */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /** Return true iff a jump is possible for a piece at position with
     *  linearized index K. */
    boolean jumpPossible(int k) {
        PieceColor piece = get(k);
        if (piece != _whoseMove) {
            return false;
        }
        for (int i: _allNeighbors) {
            if (!isValidNeighbor(k, i)) {
                continue;
            }
            if (get(k + i) == _whoseMove.opposite()) {
                if (!isValidNeighbor(k + i, i)) {
                    continue;
                }
                if (get(k + i + i) == EMPTY) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Return true iff a jump is possible from the current board. */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /** Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     *  Assumes the result is legal. */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }

    /** Make the Move MOV on this Board, assuming it is legal. */
    void makeMove(Move mov) {
        assert legalMove(mov);
        _boardsStack.push(new Board(this));
        if (mov.isJump()) {
            makeJump(mov);
        } else {
            set(mov.fromIndex(), EMPTY);
            set(mov.toIndex(), _whoseMove);
        }
        updateIllegalHorizontal(mov);
        _whoseMove = _whoseMove.opposite();
        _legalMoves = getMoves();
        _gameOver = !isMove();
        setChanged();
        notifyObservers();
    }

    /** Updates illegal horizontal move after making move MOV. */
    private void updateIllegalHorizontal(Move mov) {
        for (int i = 0; i < _illegalHorWhite.size(); i++) {
            if (get(_illegalHorWhite.get(i).fromIndex())
                    == EMPTY) {
                _illegalHorWhite.remove(i);
            }
        }
        for (int i = 0; i < _illegalHorBlack.size(); i++) {
            if (get(_illegalHorBlack.get(i).fromIndex())
                    == EMPTY) {
                _illegalHorBlack.remove(i);
            }
        }
        if (!(mov.isLeftMove() || mov.isRightMove())) {
            return;
        }
        Move newIllegalMove = move(mov.col1(), mov.row1(),
                mov.col0(), mov.row0());
        if (_whoseMove == WHITE) {
            _illegalHorWhite.add(newIllegalMove);
        } else {
            _illegalHorBlack.add(newIllegalMove);
        }
    }

    /** Make the jump helper for makeMove. Performing MOV
     *  in which MOV is a jump*/
    private void makeJump(Move mov) {
        set(mov.fromIndex(), EMPTY);
        while (true) {
            set(mov.jumpedIndex(), EMPTY);
            if (mov.jumpTail() == null) {
                set(mov.toIndex(), _whoseMove);
                break;
            }
            mov = mov.jumpTail();
        }
    }

    /** Undo the last move, if any. */
    void undo() {
        if (_boardsStack.empty()) {
            return;
        }
        internalCopy(_boardsStack.pop());
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        char row = '5';
        while (row > '0') {
            if (legend) {
                out.format(Character.toString(row));
            }
            out.format(" ");
            for (char col = 'a'; col < 'f'; col++) {
                out.format(" %s", get(col, row).shortName());
            }
            row--;
            if (row > '0') {
                out.format("\n");
            }
        }
        if (legend) {
            out.format("\n  a b c d e");
        }
        return out.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Board) {
            Board b = (Board) o;
            return (b.toString().equals(toString())
                    && _whoseMove == b.whoseMove()
                    && _gameOver == b.gameOver()
                    && _legalMoves.equals(b._legalMoves)
                    && _illegalHorBlack.equals(b._illegalHorBlack)
                    && _illegalHorWhite.equals(b._illegalHorWhite)
                    && _boardsStack.equals(b._boardsStack));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    /** Return true iff there is a move for the current player. */
    private boolean isMove() {
        return _legalMoves.size() > 0;
    }


    /** Array representing the board, pieces in linearized index. */
    private PieceColor[] _board = new PieceColor[MAX_INDEX + 1];

    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Set true when game ends. */
    private boolean _gameOver;

    /** ArrayList storing all legal moves. */
    private ArrayList<Move> _legalMoves;

    /** Return ArrayList that sores all legal moves for this board. */
    ArrayList<Move> getLegalMoves() {
        ArrayList<Move> result = new ArrayList<>();
        result.addAll(_legalMoves);
        return result;
    }

    /** Stack of all the boards. */
    private Stack<Board> _boardsStack;

    /** List storing illegal horizontal moves for white. */
    private ArrayList<Move> _illegalHorWhite;

    /** Function to return illegal horizontal white moves. */
    ArrayList<Move> getIllegalHorWhite() {
        ArrayList<Move> result = new ArrayList<>();
        result.addAll(_illegalHorWhite);
        return result;
    }

    /** List storing illegal horizontal moves for black. */
    private ArrayList<Move> _illegalHorBlack;

    /** Function to return illegal horizontal black moves. */
    ArrayList<Move> getIllegalHorBlack() {
        ArrayList<Move> result = new ArrayList<>();
        result.addAll(_illegalHorBlack);
        return result;
    }

    /** Convenience array for getting all neighbors of any square. */
    private int[] _allNeighbors = {-6, -5, -4, -1, 1, 4, 5, 6};

    /** Helper function for finding legal non-capturing moves
     *  at K, returning boolean of whether square I index
     *  difference is possible. */
    private boolean isPossibleMove(int k, int i) {
        if (!isValidNeighbor(k, i)) {
            return false;
        }
        if (_whoseMove == WHITE) {
            if (i == -6 || i == -5 || i == -4) {
                return false;
            }
        } else {
            if (i == 6 || i == 5 || i == 4) {
                return false;
            }
        }
        return true;
    }

    /** Helper function for finding valid neighbors
     *  at K, returning boolean of whether square I index
     *  difference is possible. */
    static boolean isValidNeighbor(int k, int i) {
        if (k + i > MAX_INDEX || k + i < 0) {
            return false;
        }
        if (k % 2 == 1) {
            if (diagonalFail(i)) {
                return false;
            }
        }
        if (k % 5 == 0) {
            if (leftEdgeFail(i)) {
                return false;
            }
        }
        if (k % 5 == 4) {
            if (rightEdgeFail(i)) {
                return false;
            }
        }
        return true;
    }

    /** Helper function which returns a boolean indicating whether
     *  index difference I is a possible neighbor for left edge
     *  squares. */
    static boolean leftEdgeFail(int i) {
        return (i == -6 || i == -1 || i == 4);
    }

    /** Helper function which returns a boolean indicating whether
     *  index difference I is a possible neighbor for right edge
     *  squares. */
    static boolean rightEdgeFail(int i) {
        return (i == 6 || i == 1 || i == -4);
    }

    /** Helper function which returns a boolean indicating whether
     *  index difference I is a possible neighbor for squares with
     *  no diagonal connections. */
    static boolean diagonalFail(int i) {
        return (i % 2 == 0);
    }

    /** A read-only view of a Board. */
    private class ConstantBoard extends Board implements Observer {
        /** A constant view of this Board. */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /** Undo the last move. */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}
