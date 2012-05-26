package ru.leventov.eudfa;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.TreeSet;

import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;
import static ru.leventov.eudfa.BitUtils.ringMultiply;
import static ru.leventov.eudfa.BitUtils.ringSimplify;
import static ru.leventov.eudfa.Primes.gcd;
import static ru.leventov.eudfa.Primes.isPrime;

/**
 * Date: 01.03.12
 * Time: 21:30
 */
public final class Ring extends UDFA {
	
	public static final Ring EMPTY = 
			new Ring(1, new int[0], new long[] {0L});
	public static final Ring FULL = 
			new Ring(1, new int[] {0}, new long[] {1L});
	
	
	private final long[] states;
	private final int[] accepts;
	private final int mod;


	// Object methods
	public boolean equals(Object obj) {
		if (this == obj) 
			return true;
		if (obj.getClass() != Ring.class)
			return false;
		Ring oth = (Ring) obj;
		return oth.mod == mod
			&& Arrays.equals(oth.states, states);
	}

	public int hashCode() {
		return hashCode(mod, states);
	}
	
	private static int hashCode(int length, long[] states) {
		int result = 17 * 31 + length;
		result = result * 31 + Arrays.hashCode(states);
		return result;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < accepts.length; i++) {
			if (i != 0) sb.append(", ");
			sb.append(accepts[i]);
		}
		return String.format("{%s} %% %d ", sb.toString(), length());
	}

	
	// UDFA methods
	public boolean accepting(int length) {
		int resultLength = length % length();
		// see BitSet and JLS; left shift already uses 6 lower bits 
		return (states[resultLength / 64] & (1L << resultLength)) != 0;
	}

	public Ring pow(int e) {
		int[] a = new int[accepts.length];
		for(int i = 0; i < a.length; i++) {
			a[i] = accepts[i] * e;
		}
		return byAccepts(length() * e, a);
	}
	
	public Ring multiply(Ring other) {
		if (other == EMPTY || this == EMPTY) return EMPTY;

		int leftLen = length(), rightLen = other.length();

		int rawLen = gcd(leftLen, rightLen);
		if (rawLen == 1) return FULL;

		if (leftLen <= 64 && rightLen <= 64) {
			long leftSet = states[0], rightSet = other.states[0];
			long productSet = ringMultiply(leftSet, leftLen, rightSet, rightLen);
			int simpleLen = ringSimplify(productSet, rawLen);
			productSet &= ~(-1L << simpleLen);
			return byBits(simpleLen, productSet);
		}

		boolean[] states = new boolean[rawLen]; // raw len = mod
		for (int a : accepts) {
			for (int oa : other.accepts)
				states[(a + oa) % rawLen] = true;
		}
		
		int ac = 0;
		for (boolean s : states) if (s) ac++;
		int[] accepts = new int[ac];
		ac = 0;
		for (int i = 0; i < states.length; i++) {
			if (states[i]) accepts[ac++] = i;
		}
		
		return simplify(rawLen, accepts, null);
	}

	public UDFA multiply(UDFA other) {
		return null;  //TODO
	}

	public Ring simplify() {
		return simplify(length(), accepts, this);
	}
	
	private static Ring simplify(int length, int[] accepts, Ring existing) {
		if (accepts.length == 0) return EMPTY;
		if (accepts.length == length) return FULL;

		/*
		Method is private -> no need of defensive copying of the accepts array
		 */

		if (length <= 64) {
			long bitSet = 0;
			if (existing != null)
				bitSet = existing.states[0];
			else
				for (int a : accepts) bitSet |= 1L << a;

			int simpleLen = ringSimplify(bitSet, length);

			if (simpleLen == length) {
				if (existing != null) return existing;
				return new Ring(simpleLen, accepts, new long[] {bitSet});
			} else {
				bitSet &= ~(-1L << simpleLen);
				return new Ring(
						simpleLen,
						copyOf(accepts, simpleLen),
						new long[] {bitSet}
				);
			}
		}

		if (!isPrime(length))
			potentialLengths: for (int newLen = 2; newLen <= length / 2; newLen++) {
				if (length % newLen != 0) continue;

				int aLen = binarySearch(accepts, newLen);
				if (aLen < 0) aLen = -aLen - 1;
				if (	aLen == 0 ||
						accepts.length % aLen != 0 ||
						accepts.length / aLen != length / newLen )
					continue;

				int off = 0, j = 0;
				for (int a : accepts) {
					if (a != accepts[j] + off)
						continue potentialLengths;
					j++;
					if ((j %= aLen) == 0) off += newLen;
				}
				return byAccepts(newLen, copyOf(accepts, aLen));
			}
		if (existing != null) return existing;
		else return byAccepts(length, accepts);
	}
	
	public boolean simple() {
		return this == simplify();
	}
	
	
	// public api
	public int length() {
		return mod;
	}
	
	public int[] accepts() {
		return Arrays.copyOf(accepts, accepts.length);
	}
	
	public int getAccept(int no) {
		return accepts[no];
	}

	public boolean[] states() {
		boolean[] res = new boolean[length()];
		for (int a : accepts) {
			res[a] = true;
		}
		return res;
	}

	public long getStatesChuck(int no) {
		return states[no];
	}
	
	public BitSet statesAsBitSet() {
		return BitSet.valueOf(states);
	}

	
	// factories
	// shouldn't allow to create new Ring equals to FULL or EMPTY
	public static Ring byAccepts(int length, int[] accepts) {
		if (length < 1) throw new IllegalArgumentException();

		if (length == 1) {
			if (accepts.length == 1) return FULL;
			return EMPTY;
		}

		// accepts should be normalized and sorted
		accepts = normalizeArray(accepts, length);

		long[] states = new long[BitUtils.wordsLength(length)];
		for (int a : accepts) {
			states[a / 64] |= 1L << a;
		}
		
		return new Ring(length, accepts, states);
	}

	/**
	 * @param source cleaned(!) bit set of the accepts
	 */
	public static Ring byBits(int length, long source) {
		if (length > 64 || length < 1)
			throw new IllegalArgumentException();
		
		if (length == 1) {
			if ((source & 1) == 1) return FULL;
			return EMPTY;
		}
		
		int[] t = new int[length];
		int c = 0;
		for (int k = 0; k < length; k++)
			if ((source & (1L << k)) != 0L) t[c++] = k;

		long[] states = new long[] { source };

		return new Ring(length, copyOf(t, c), states);
	}
	
	private Ring(int length, int[] accepts, long[] states) {
		mod = length;
		this.accepts = accepts;
		this.states = states;
	}
	
	
	// utils
	public Ring wideTo(int l) {
		if (l % length() != 0) 
			throw new IllegalArgumentException();
		
		return wide(l/length());
	}
	/**
	 * @param c - coefficient - ex. wide([0] % 2, 2) = [0, 2] % 4 
	 */
	public Ring wide(int c) {
		if (c <= 0) throw new IllegalArgumentException();
		if (c == 1) return this;
		
		int[] as = new int[accepts.length * c];
		for(int i = 0; i < c; i++) {
			for(int j = 0; j < accepts.length; j++) {
				as[accepts.length * i + j] = accepts[j] + length() * i;
			}
		}
		return byAccepts(c * length(), as);
	}
	
	private static int[] normalizeArray(int[] ar, int length) {
		TreeSet<Integer> aSet = new TreeSet<Integer>();
		for (int a : ar) aSet.add(a % length);
		int[] res = new int[aSet.size()];
		Iterator<Integer> aIt = aSet.iterator();
		for (int i = 0; i < aSet.size(); i++) {
			res[i] = aIt.next();
		}
		return res;
	}
}
