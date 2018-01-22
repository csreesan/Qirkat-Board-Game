/* Author: Paul N. Hilfinger.  (C) 2008. */

package qirkat;

import org.junit.Test;
import static org.junit.Assert.*;

import static qirkat.Move.*;

/** Test Move creation.
 *  @author Chris Sreesangkom
 */
public class MoveTest {

    @Test
    public void testMove1() {
        Move m = move('a', '3', 'b', '2');
        assertNotNull(m);
        assertFalse("move should not be jump", m.isJump());
    }

    @Test
    public void testJump1() {
        Move m = move('a', '3', 'a', '5');
        assertNotNull(m);
        assertTrue("move should be jump", m.isJump());
    }

    @Test
    public void testString() {
        assertEquals("a3-b2", move('a', '3', 'b', '2').toString());
        assertEquals("a3-a5", move('a', '3', 'a', '5').toString());
        assertEquals("a3-a5-c3", move('a', '3', 'a', '5',
                                      move('a', '5', 'c', '3')).toString());
    }

    @Test
    public void testParseString() {
        assertEquals("a3-b2", parseMove("a3-b2").toString());
        assertEquals("a3-a5", parseMove("a3-a5").toString());
        assertEquals("a3-a5-c3", parseMove("a3-a5-c3").toString());
        assertEquals("a3-a5-c3-e1", parseMove("a3-a5-c3-e1").toString());
    }

    @Test
    public void testIsLeftAndIsRightMove() {
        Move left = move('b', '1', 'a', '1');
        Move right = move('a', '1', 'b', '1');
        Move notLeft = move('b', '2', 'a', '1');
        Move notRight = move('a', '1', 'b', '2');

        assertTrue("failed to indicate that it is left move",
                left.isLeftMove());
        assertTrue("failed to indicate that it is right move",
                right.isRightMove());
        assertFalse("not horizontal", notLeft.isLeftMove());
        assertFalse("not horizontal", notRight.isRightMove());
    }

    @Test
    public void testJumpedIndex() {
        Move hJump = move('a', '1', 'c', '1');
        Move vJump = move('a', '1', 'a', '3');
        Move dJump = move('a', '1', 'c', '3');
        Move notJump = move('a', '1', 'b', '1');

        assertEquals("failed horizontal jumped index", 1, hJump.jumpedIndex());
        assertEquals("failed vertical jumped index", 5, vJump.jumpedIndex());
        assertEquals("failed diagonal jumped index", 6, dJump.jumpedIndex());
        assertEquals("failed jumped index fo non-jump",
                notJump.toIndex(), notJump.jumpedIndex());
    }

    @Test
    public void testConcatMove() {
        Move m1 = move('a', '1', 'a', '2');
    }
}
