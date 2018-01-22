package qirkat;

import java.util.ArrayList;

import static qirkat.PieceColor.*;
import static qirkat.Move.SIDE;
import static qirkat.Move.MAX_INDEX;

/** A Player that computes its own moves.
 *  @author Chris Sreesangkom
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 8;
    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove("%s moves %s.", myColor(), move);
        return move;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == WHITE) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Move best;
        best = null;
        int bestScore;

        if (sense == 1)  {
            bestScore = -WINNING_VALUE;
        } else {
            bestScore = WINNING_VALUE;
        }
        if (board.gameOver()) {
            board.undo();
            if (sense == -1) {
                return WINNING_VALUE;
            } else {
                return -WINNING_VALUE;
            }
        }
        if (depth == 0) {
            board.undo();
            return staticScore(board);
        }
        int score;
        if (sense == 1) {
            ArrayList<Move> legalMovesList = board.getLegalMoves();
            for (Move legalMove : legalMovesList) {
                board.makeMove(legalMove);
                score = findMove(board, depth - 1, false,
                        sense * -1, bestScore, beta);
                if (score >= bestScore) {
                    bestScore = score;
                    best = legalMove;
                    if (bestScore >= beta) {
                        break;
                    }
                }
            }
        } else {
            ArrayList<Move> legalMovesList = board.getLegalMoves();
            for (Move legalMove : legalMovesList) {
                board.makeMove(legalMove);
                score = findMove(board, depth - 1, false,
                        sense * -1, alpha, bestScore);
                if (score <= bestScore) {
                    bestScore = score;
                    best = legalMove;
                    if (bestScore <= alpha) {
                        break;
                    }
                }
            }
        }

        if (saveMove) {
            _lastFoundMove = best;
        }
        board.undo();
        return bestScore;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int total = 0;
        for (int k = 0; k <= MAX_INDEX; k++) {
            total += squareValue(board, k);
        }
        return total;
    }

    /** Return value of the piece at the position K index of BOARD. */
    private int squareValue(Board board, int k) {
        if (board.get(k) == EMPTY) {
            return 0;
        }
        if (board.get(k) == WHITE) {
            if (getRow(k) == 5) {
                return 0;
            }
            int rawScore = (SIDE - getRow(k) + 1) * 4;
            ArrayList<Move> illegalHorList = board.getIllegalHorWhite();
            for (Move illegalMove: illegalHorList) {
                if (illegalMove.fromIndex() == k) {
                    if (illegalMove.isRightMove()) {
                        rawScore -= (5 - getCol(k));
                    } else {
                        rawScore -= (getCol(k) - 1);
                    }
                }
            }
            return rawScore;
        } else {
            int rawScore = (getRow(k) * 4);
            if (getRow(k) == 1) {
                return 0;
            }
            ArrayList<Move> illegalHorList = board.getIllegalHorBlack();
            for (Move illegalMove: illegalHorList) {
                if (illegalMove.fromIndex() == k) {
                    if (illegalMove.isRightMove()) {
                        rawScore -= (5 - getCol(k));
                    } else {
                        rawScore -= (getCol(k) - 1);
                    }
                }
            }
            return -rawScore;

        }
    }

    /** Return row value of square K as an integer. */
    private int getRow(int k) {
        return (k / 5) + 1;
    }

    /** Return column value of square K as an integer. */
    private int getCol(int k) {
        return (k % 5) + 1;
    }
}
