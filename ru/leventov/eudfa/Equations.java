package ru.leventov.eudfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;
import static ru.leventov.eudfa.Primes.lcm;
import static ru.leventov.eudfa.Ring.byAccepts;

/**
 * Date: 06.03.12
 * Time: 15:15
 */
public class Equations {

	public static Collection<Ring> solveRight(Ring left, Ring product) {
		if (product == Ring.FULL) throw new IllegalArgumentException();

		int ll = left.length();
		int pl = product.length();
		if (ll % pl != 0)
			return Collections.emptyList();

		ArrayList<Ring> solutions = new ArrayList<Ring>();
		
		 // length of the product must divide right
		for(int i = 1; i < 7; i++) { // 20?
			solutions.addAll(iSolveRight(left, product, i * pl));
		}

		return solutions;
	}

	public static Collection<Ring> solveRight(Ring left, Ring product,
	                                          int rightLength) {
		if (rightLength <= 0 || product == Ring.FULL)
			throw new IllegalArgumentException();
		
		int pl = product.length();
		if (left.length() % pl != 0 || rightLength % pl != 0)
			return Collections.emptyList();

		return iSolveRight(left, product, rightLength);
	}
	
	private static Collection<Ring> iSolveRight(Ring left, Ring product,
	                                            int rightLength) {
		int rawLen = lcm(left.length(), rightLength);
		int[] rawProductAccepts = 
				product.wide(rawLen / product.length()).accepts();
		
		int start = left.getAccept(0), end = start + rightLength;
		int from = binarySearch(rawProductAccepts, start);
		if (from < 0) from = -from - 1;
		int to = binarySearch(rawProductAccepts, end);
		if (to < 0) to = -to - 1;
		int al = to - from;
		
		int[] allRightAccepts = new int[al];
		for(int i = from; i < to; i++) {
			allRightAccepts[i - from] =
					rawProductAccepts[i] - rawProductAccepts[from];
		}
		
		ArrayList<Ring> solutions = new ArrayList<Ring>();
		
		int[] t = new int[al];
		// from 1 - don't try EMPTY as second 
		for(int i = 1; i < (1 << al); i++) {
			int c = 0;
			for(int k = 0; k < al; k++)
				if ((i & (1 << k)) != 0) t[c++] = allRightAccepts[k];

			Ring right = byAccepts(rightLength, copyOf(t, c));
			if (left.multiply(right).equals(product)) {
				if (right.simple())
					solutions.add(right);
			}
		}
		return solutions;
	}

	public static void main(String[] args) {
		Collection<Ring> solutions = solveRight(
				byAccepts(8, new int[]{1, 2, 3, 4}),
				byAccepts(4, new int[]{0, 1, 2}));
		for (Ring r: solutions) {
			System.out.println(r);
		}
		System.out.println(solutions.size());
	}
}
