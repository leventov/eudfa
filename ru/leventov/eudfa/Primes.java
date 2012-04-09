package ru.leventov.eudfa;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Date: 01.03.12
 * Time: 23:30
 */
public class Primes {
	public static final int N_PRIMES = 1000;
	public static final boolean[] ERATO = new boolean[8000]; // 1000th prime is 7919
	public static int[] PRIMES = new int[N_PRIMES];
	static {
		ERATO[0] = ERATO[1] = true;
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

	/**
	 * @param n integer between 0 and 7920
	 */
	public static boolean isPrime(int n) {
		return !ERATO[n];
	}

	// least number with 10 prime divs is 6469693230
	private static int[] divs = new int[10];

	public static int[] primeDivisors(int n) {
		if (!ERATO[n]) // prime has no prime divisors
			return new int[0];
		int dc = 0;
		int i = 0;
		while (n > 1) {
			if (n % PRIMES[i] == 0) {
				divs[dc++] = PRIMES[i];
				while (n % PRIMES[i] == 0) {
					n /= PRIMES[i];
				}
			}
			i++;
		}
		return Arrays.copyOf(divs, dc);
	}

	public static int gcd(int a, int b) {
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	/**
	 * @param a element of the ring
	 * @param m modulo
	 * @return multiplicative inverse of a modulo m
	 */
	public static int inverse(int a, int m) {
		// wiki: Extended Euclidean algorithm
		int x = 0, y = 0, lastX = 1, lastY = 0;
		while (m != 0) {
			int k = a / m;

			int t = m;
			m = a % m;
			a = t;

			t = x;
			x = lastX - k*x;
			lastX = t;

			t = y;
			y = lastY - k*y;
			lastY = t;
		}
		return lastX;
	}

	public static int lcm(int a, int b) {
		return a * b / gcd(a, b);
	}

}
