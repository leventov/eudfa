package ru.leventov.eudfa;

import static ru.leventov.eudfa.Primes.gcd;

/**
 * Date: 05.04.12
 * Time: 16:23
 */
public class Systems {
	public static void solve(int[] leftPows, int[] rightPows, Ring[] products) {
		int l = leftPows.length;
		if (l != rightPows.length || l != products.length)
			throw new IllegalArgumentException();

		int[] baseLeftLens = new int[l], baseRightLens = new int[l];
		//int[][] leftWork = new int[l][], rightWork = new int[l][];
		for(int i = 0; i < l; i++) {
			int pl = products[i].length();

			int bll = pl / gcd(pl, leftPows[i]);
			baseLeftLens[i] = bll;
//			leftWork[i] = new int[bll];
//			for(int j = 0; j < bll; j++) {
//				leftWork[i][j] = (j * leftPows[i]) % pl;
//			}

			int brl = pl / gcd(pl, rightPows[i]);
			baseRightLens[i] = brl;
//			rightWork[i] = new int[brl];
//			for(int j = 0; j < brl; j++) {
//				rightWork[i][j] = (j * rightPows[i]) % pl;
//			}
		}
		//TODO

	}
}
