package ru.leventov.eudfa;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;
import static ru.leventov.eudfa.Primes.lcm;

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
		return String.format("[%s] %% %d ", sb.toString(), length());
	}

	
	// UDFA methods
	public boolean accepting(int length) {
		int resultLength = length % length();
		// see BitSet and JLS; left shift already uses 6 lower bits 
		return (states[resultLength / 64] & (1L << resultLength)) != 0;
	}
	
	public Ring multiply(Ring other) {
		if (other == EMPTY || other == FULL) return other;

		int rawLen = lcm(length(), other.length());

		boolean[] states = new boolean[length()]; // raw len = mod
		for (int a : accepts) {

			// automata MUST move to the second ring
			//states[a] = true;

			for (int sOff = 0; sOff < rawLen; sOff += other.length())
				for (int oa : other.accepts)
					states[(a + sOff + oa) % length()] = true;
		}
		
		int ac = 0;
		for (boolean s : states) if (s) ac++;
		int[] accepts = new int[ac];
		ac = 0;
		for (int i = 0; i < states.length; i++) {
			if (states[i]) accepts[ac++] = i;
		}
		
		return simplify(length(), accepts, null);
	}

	public UDFA multiply(UDFA other) {
		return null;  //TODO
	}

	public UDFA simplify() {
		return simplify(length(), accepts, this);
	}
	
	private static Ring simplify(int length, int[] accepts, Ring existing) {
		if (accepts.length == 0) return EMPTY;
		if (accepts.length == length) return FULL;

		primes: for (int newLen = 2; newLen <= length / 2; newLen++) {
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
					continue primes;
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

	
	// factories
	// shouldn't allow to create new Ring equals to FULL or EMPTY
	public static Ring byAccepts(int length, int[] accepts) {
		if (length < 1) throw new IllegalArgumentException();

		if (length == 1) {
			if (accepts.length == 1) return FULL;
			else return EMPTY;
		}

		// accepts should be normalized and sorted
		normalizeArray(accepts, length);

		long[] states = new long[statesLength(length)];
		acceptsToStates(states, accepts);
		
		return new Ring(length, accepts, states);
	}
	
	public static Ring byBits(int length, int source) {
		if (length > 32 || length < 0) 
			throw new IllegalArgumentException();
		
		if (length == 1) {
			if ((source & 1) == 1) {
				return FULL;
			} else return EMPTY;
		}
		
		int[] t = new int[length];
		int c = 0;
		for (int k = 0; k < length; k++)
			if ((source & (1 << k)) != 0) t[c++] = k;

		long[] states = new long[] { ((1L << length) - 1) & source };

		return new Ring(length, copyOf(t, c), states);
	}
	
	private Ring(int length, int[] accepts, long[] states) {
		mod = length;
		this.accepts = accepts;
		this.states = states;
	}
	
	
	// utils
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
	
	private static void normalizeArray(int[] ar, int length) {
		TreeSet<Integer> aSet = new TreeSet<>();
		for (int a : ar) aSet.add(a % length);
		ar = new int[aSet.size()];
		Iterator<Integer> aIt = aSet.iterator();
		for (int i = 0; i < ar.length; i++) {
			ar[i] = aIt.next();
		}
	}

	private static int statesLength(int ringLength) {
		int resLen = ringLength / 64;
		if (ringLength % 64 != 0) resLen++;
		return resLen;
	}

	private static void acceptsToStates(long[] states, int[] accepts) {
		for (int a : accepts) {
			states[a / 64] |= 1L << a;
		}
	}
}
