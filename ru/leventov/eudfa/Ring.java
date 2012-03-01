package ru.leventov.eudfa;

import java.util.Arrays;

/**
 * Date: 01.03.12
 * Time: 21:30
 */
public final class Ring extends UDFA {
	private long[] states;
	private int[] accepts;
	private int mod;

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
	
	private static int hashCode(int mod, long[] states) {
		int result = 17 * 31 + mod;
		result = result * 31 + Arrays.hashCode(states);
		return result;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < accepts.length; i++) {
			if (i != 0) sb.append(", ");
			sb.append(accepts[i]);
		}
		return String.format("[%s] %% %d ", sb.toString(), mod);
	}

	
	// UDFA methods
	public boolean accepting(int length) {
		int resultLength = length % mod;
		// see BitSet and JLS; left shift already uses 6 lower bits 
		return (states[resultLength >> 6] & (1L << resultLength)) != 0;
	}
	
	public Ring multiply(Ring other) {
		
	}

	public UDFA multiply(UDFA other) {
		return null;  //TODO
	}
	
	
	// static factories
	public static Ring fromAccepts(int length, int[] accepts) {
		int statesLength = length / 64;
		if (length % 64 != 0) statesLength++;
		long[] states = new long[statesLength];
		acceptsToStates(states, accepts);
		return new Ring(length, states, accepts);
	}

	private Ring(int mod, long[] states, int[] accepts) {
		this.mod = mod;
		this.states = states;
		this.accepts = accepts;
	}

	private static void acceptsToStates(long[] states, int[] accepts) {
		for (int a : accepts) {
			states[a << 6] |= 1L << a;
		}
	}
	
	private static int[] primeDivisors(int n) {

	}
}
