package ru.leventov.eudfa;

import java.util.*;

import static java.lang.Math.min;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;
import static ru.leventov.eudfa.Primes.gcd;
import static ru.leventov.eudfa.Primes.lcm;
import static ru.leventov.eudfa.Ring.FULL;
import static ru.leventov.eudfa.Ring.byAccepts;
import static ru.leventov.eudfa.Ring.byBits;

/**
 * Date: 06.03.12
 * Time: 15:15
 */
public class Equations {

	public static Ring solve(Ring multiple, Ring product) {
		if (product == Ring.FULL)
			return FULL;
		
		int pl = product.length(), ll = multiple.length();
		if (gcd(ll, pl) == 1)
			return null;

		int rawPL = lcm(ll, pl);
		if (rawPL <= 64) {
			long leftSource = multiple.wideTo(rawPL).getStatesChuck(0);
			for(int i = 0; i < rawPL; i += pl) {
				leftSource |= leftSource >>> i;
			}
			long right = BitUtils.ringSolve(
							leftSource,
							product.getStatesChuck(0),
							pl);
			if (right == 0L) return null;
			return byBits(pl, right);
		}

		int[] allRightAccepts = getAllAccepts(multiple, product);
		int al = allRightAccepts.length;

		BitSet productSet = product.statesAsBitSet();
		BitSet cover = new BitSet();

		int[] t = new int[al];
		int c = 0;
		for(int i = 0; i < al; i++) {
			Ring right = byAccepts(pl, new int[]{allRightAccepts[i]});

			BitSet acceptSet = multiple.multiply(right)
					.wideTo(pl).statesAsBitSet();

			BitSet as = (BitSet) acceptSet.clone();
			as.andNot(productSet);
			if (as.isEmpty()) {
				cover.or(acceptSet);
				t[c++] = allRightAccepts[i];
			}
		}
		if (c == 0 || !productSet.equals(cover)) return null;
		return byAccepts(pl, copyOf(t, c));
	}



	private static int[] getAllAccepts(Ring multiple, Ring product) {
		int pl = product.length(); // = length of the right ring. see facts.txt
		int[] rawProductAccepts =
				rawProduct(multiple, product, pl).accepts();

		int start = multiple.getAccept(0), end = start + pl;
		int from = binarySearch(rawProductAccepts, start);
		if (from < 0) from = -from - 1;
		int to = binarySearch(rawProductAccepts, end);
		if (to < 0) to = -to - 1;
		int al = to - from;

		int[] allRightAccepts = new int[al];
		System.arraycopy(rawProductAccepts, from,
				allRightAccepts, 0, al);
		for(int i = 0; i < al; i++) {
			allRightAccepts[i] -= start;
		}
		return allRightAccepts;
	}
	
	private static Ring rawProduct(Ring multiple, Ring product,
	                               int anotherLength) {
		int rawLen = lcm(multiple.length(), anotherLength);
		return product.wideTo(rawLen);
	}
}