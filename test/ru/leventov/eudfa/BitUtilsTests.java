package test.ru.leventov.eudfa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.everyItem;
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

	@Test public void testRingSolve() {
		// see testRingMultiply
		assertTrue(ringSolve(0b101L, 0b110L, 3) == 0b100L);
		assertTrue(ringSolve(1L, 1L, 1) == 1L);
		assertTrue(ringSolve(1L, 0b11L, 2) == 0b11L);
		assertTrue(ringSolve(0b11L, 0b10L, 2) == 0L); // ничего не подходит
		assertTrue(ringSolve(0b00010101L, 0b01010101L, 8) ==  0b01010101L); // 0010 * 10101000 = 1010
	}

	@Test public void testPossibleMultiplesAreLegal() {
		int[] lengths = new int[]     {8,           8,           6};
		long[] products = new long [] {0b10101010L, 0b10011001L, 0b101111L};
		for (int i = 0; i < lengths.length; i++) {
			int length = lengths[i];
			long product = products[i];
			List<Long> pLefts = possibleLefts(1, 1, product, length);

			ArrayList<Long> rights = new ArrayList<>(pLefts.size());
			for (long l : pLefts) rights.add(ringSolve(l, product, length));
			//System.out.println(rights);

			assertThat(rights, everyItem(not(equalTo(0L))));
		}
	}

	@Test public void testPossibleMultiplesMissed() {
		int[] lengths = new int[]     {8,           8,           6,         4,     };
		long[] products = new long [] {0b10101010L, 0b10011001L, 0b101111L, 0b0011L};
		for (int i = 0; i < lengths.length; i++) {
			int length = lengths[i];
			long product = products[i];
			List<Long> pLefts = possibleLefts(1, 1, product, length);

			// множество невозможных
			ArrayList<Long> leftComp = new ArrayList<Long>((1 << length) - pLefts.size());
			for (long j = 0, c = 0; j < 1L << length; j++)
				if (!pLefts.contains(j) ) leftComp.add(j);
			//System.out.println(leftComp);

			ArrayList<Long> rights = new ArrayList<>(leftComp.size());
			for (long l : leftComp) rights.add(ringSolve(l, product, length));
			//System.out.println(rights);

			assertThat(rights, everyItem(equalTo(0L))); // все фейлятся
		}
	}

	@Test public void testMinimumSolutions() {
		long left = 0b101010L;
		long product = 0b10101010L;
		int length = 8;
		List<Long> rights = minimumSolutions(left, product, length);
//		for (long r : rights) {
//			System.out.println(Long.toBinaryString(r));
//		}

		ArrayList<Long> products = new ArrayList<>(rights.size());
		for (long r : rights)
			products.add(ringMultiply(left, length, r, length));
		//System.out.println(rights);

		assertThat(products, everyItem(equalTo(product)));
	}
}
