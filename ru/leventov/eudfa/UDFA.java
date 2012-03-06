package ru.leventov.eudfa;

/**
 * Immutable class representing
 * Unary Deterministic Finite Automata
 * Date: 01.03.12
 * Time: 21:20
 */
public abstract class UDFA {
	public abstract boolean accepting(int length);
	public abstract UDFA simplify();
	public abstract UDFA multiply(UDFA other);
}
