package ru.leventov.eudfa;

import java.util.ArrayList;

/**
 * Date: 01.03.12
 * Time: 23:30
 */
public class Primes {
	public static final int N_PRIMES = 1000;
	public static final boolean[] ERATO = new boolean[8000]; // 1000th prime is 7919
	public static int[] PRIMES = new int[N_PRIMES];
	static {
		int c = 0;
		int i = 2;
		while (c < N_PRIMES) {
			if (!ERATO[i]) {
				PRIMES[c++] = i;
				int k = 2 * i;
				while (k < ERATO.length) {
					ERATO[k] = true;
					k += i;
				}
			}
			i++;
		}
	}
	
	public static int[] primeDivisors(int n) {
		ArrayList<Integer> results = new ArrayList<>();
		int i = 0;
		while (n > 1) {
			if (n % PRIMES[i] == 0) {
				results.add(PRIMES[i]);
				while (n % PRIMES[i] == 0) {
					n /= PRIMES[i];
				}
			}
			i++;
		}
		int[] r = new int[results.size()];
		for (i = 0; i < r.length; i++)
			r[i] = results.get(i).intValue();
		return r;
	}
}
