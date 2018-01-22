package qirkat;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/** Tests of the Board class.
 *  @author Chris Sreesangkom
 */
public class BoardTest {

    private static final String INIT_BOARD =
        "  b b b b b\n  b b b b b\n  b b - w w\n  w w w w w\n  w w w w w";

    private static final String[] GAME1 =
    { "c2-c3", "c4-c2",
      "c1-c3", "a3-c1",
      "c3-a3", "c5-c4",
      "a3-c5-c3",
    };

    private static final String GAME1_BOARD =
        "  b b - b b\n  b - - b b\n  - - w w w\n  w - - w w\n  w w b w w";

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(Move.parseMove(s));
        }
    }

    @Test
    public void testInit1() {
        Board b0 = new Board();
        assertEquals(INIT_BOARD, b0.toString());
    }

    @Test
    public void testMoves1() {
        Board b0 = new Board();
        makeMoves(b0, GAME1);
        assertEquals(GAME1_BOARD, b0.toString());
    }

    @Test
    public void testUndo() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);
        for (int i = 0; i < GAME1.length; i += 1) {
            b0.undo();
        }
        assertEquals("failed to return to start", b1, b0);
        assertNotEquals("should not share stacks", b2, b0);
        makeMoves(b0, GAME1);
        assertEquals("second pass failed to reach same position", b2, b0);
        assertNotEquals("copy should not be affected", b1, b2);
        for (int i = 0; i < GAME1.length; i += 1) {
            b2.undo();
        }
        assertEquals("copy failed to return to start", b1, b2);
    }

    @Test
    public void testClear() {
        Board b0 = new Board();
        Board b1 = new Board();
        makeMoves(b0, GAME1);
        assertNotEquals("moves weren't performed", b0, b1);
        b0.clear();
        assertEquals("failed to clear the board to initial position", b0, b1);
    }

    @Test
    public void testInternalCopy() {
        Board b0 = new Board();
        makeMoves(b0, GAME1);
        Board b1 = new Board(b0);
        assertEquals("failed to copy board", b0, b1);
    }

    @Test
    public void testJumpPossible() {
        Board b0 = new Board();
        assertFalse(b0.jumpPossible());
    }

    @Test
    public void testGetMoves() {
        Board b0 = new Board();
        ArrayList<Move> legalMoves = b0.getMoves();
        assertEquals("not the right number of legal moves",
                4, legalMoves.size());
        assertTrue(legalMoves.contains(Move.move('b', '2', 'c', '3')));
        assertTrue(legalMoves.contains(Move.move('c', '2', 'c', '3')));
        assertTrue(legalMoves.contains(Move.move('d', '2', 'c', '3')));
        assertTrue(legalMoves.contains(Move.move('d', '3', 'c', '3')));
    }

    @Test
    public void testGet() {
        Board b0 = new Board();
        assertEquals(PieceColor.BLACK, b0.get('b', '3'));
    }
    @Test
    public void testSetPieces() {
        Board b0 = new Board();
        Board b1 = new Board();
        makeMoves(b0, GAME1);
        b1.setPieces("w wbw ww - - w w  - - w w wb - - b bb b - b b",
                PieceColor.BLACK);
        assertEquals(b0.toString(), b1.toString());
    }

    @Test
    public void testMakeMove() {
        Board b0 = new Board();
        Board b1 = new Board();
        b0.setPieces("--wb---b-b---b-----------", PieceColor.WHITE);
        b1.setPieces("--w----------------------", PieceColor.WHITE);
        b0.makeMove(Move.parseMove("c1-c3-e3-e1-c1"));
        assertEquals(b1.toString(), b0.toString());

        Board b2 = new Board();
        b2.setPieces("----- -w--- ----- ----- ----b", PieceColor.WHITE);
        b2.makeMove(Move.parseMove("b2-a2"));
        b2.makeMove(Move.parseMove("e5-e4"));
        b2.makeMove(Move.parseMove("a2-a3"));
        b2.makeMove(Move.parseMove("e4-e3"));
        b2.makeMove(Move.parseMove("a3-b3"));
        b2.makeMove(Move.parseMove("e3-e2"));
        Board b3 = new Board();
        b3.setPieces("----- ----b -w--- ----- -----", PieceColor.WHITE);
        assertEquals(b3.toString(), b2.toString());
    }

    @Test
    public void isValidNeighborTest() {
        assertFalse("Right edge neighbor fail", Board.isValidNeighbor(19, 1));
    }

    @Test
    public void checkIllegalMoves() {
        Board b0 = new Board();
        b0.setPieces("----- ----- ----- ----b --w--", PieceColor.WHITE);
        try {
            b0.makeMove(Move.parseMove("c5-c4"));
            throw new Error("backward move not allowed");
        } catch (AssertionError e) {
            System.out.println("pass");
        }
        Board b1 = new Board();
        b1.setPieces("w---- ----b ----- ----b --w--", PieceColor.WHITE);
        try {
            b1.makeMove(Move.parseMove("c5-b5"));
            b1.makeMove(Move.parseMove("e4-e3"));

            b1.makeMove(Move.parseMove("b5-c5"));
            throw new Error("backward move not allowed");
        } catch (AssertionError e) {
            System.out.println("pass");
        }
    }
}
