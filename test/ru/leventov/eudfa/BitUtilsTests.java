package test.ru.leventov.eudfa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
import static ru.leventov.eudfa.BitUtils.*;

/**
 * Date: 04.04.12
 * Time: 21:46
 */
@RunWith(value = JUnit4.class)
public class BitUtilsTests {
	@Test public void testRingSimplify() {
		assertTrue(ringSimplify(0b101_101_101L, 9) == 3); // = 101
		assertTrue(ringSimplify(0b11_10_10_10L, 6) == 2); // = 10
		assertTrue(ringSimplify(0b1111L, 3) == 1); // = 1
		assertTrue(ringSimplify(0L, 1) == 1);
		assertTrue(ringSimplify(1L, 1) == 1);
		assertTrue(ringSimplify(0b101101100L, 9) == 9);
		assertTrue(ringSimplify(0b101_001_101_101L, 12) == 12);
		assertTrue(ringSimplify(0b101_111_101_101L, 9) == 9);
	}

	@Test public void testRingMultiply() {
		assertTrue(ringMultiply(0b101L, 3, 0b100L, 3) == 6L); // 101 * 001 = 011
		assertTrue(ringMultiply(1L, 5, 2L, 4) == 1L); // взаимно простые
		assertTrue(ringMultiply(0b100001L, 6, 0b100L, 3) == 0b110L); // 100001 * 001 = 011
		assertTrue(ringMultiply(0b0100L, 4, 0b00010101L, 8) == 0b0101L); // 0010 * 10101000 = 1010
		assertTrue(ringMultiply(0b0100L, 4, 0b0000010101L, 10) == 1L); // 0010 * 1010100000 = 10
	}
}
