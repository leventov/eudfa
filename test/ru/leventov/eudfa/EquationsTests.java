package test.ru.leventov.eudfa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.leventov.eudfa.Ring;

import static org.junit.Assert.*;
import static ru.leventov.eudfa.Equations.*;
import static ru.leventov.eudfa.Ring.byAccepts;
import static ru.leventov.eudfa.Ring.byBits;

/**
 * Date: 09.04.12
 * Time: 21:06
 */
@RunWith(value = JUnit4.class)
public class EquationsTests {
	@Test public void less64() {
		Ring r1 = byAccepts(12, new int[] {0}), r2 = byAccepts(4, new int[] {3});
		Ring r3 = solveRight(r1, r2);
		assertTrue(r1.multiply(r3).equals(r3));
	}

	@Test public void less64noSolution() {
		Ring r1 = byAccepts(4, new int[] {0, 3}), r2 = byAccepts(2, new int[] {1});
		assertTrue(solveRight(r1, r2) == null);
	}

	@Test public void incompatibleLengths() {
		Ring r1 = byBits(7, 1L), r2 = byBits(3, 2L);
		assertTrue(solveRight(r1, r2) == null);

		r1 = byAccepts(128, new int[]{0, 1});
		assertTrue(solveRight(r1, r2) == null);
	}

	@Test public void more64NoSolution() {
		Ring r1 = byAccepts(100, new int[] {0, 1}), r2 = byBits(2, 1L);
		Ring r3 = solveRight(r1, r2);
		//System.out.println(r3);
		assertTrue(r3 == null);
	}

	@Test public void more64() {
		Ring r1 = byAccepts(100, new int[] {5}), r2 = byBits(2, 1L);
		Ring r3 = solveRight(r1, r2);
		//System.out.println(r3);
		assertTrue(r1.multiply(r3).equals(r2));
	}
}
