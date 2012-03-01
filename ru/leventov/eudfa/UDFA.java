package ru.leventov.eudfa;

/**
 * Date: 01.03.12
 * Time: 21:20
 */
public abstract class UDFA {
	public abstract boolean accepting(int length);
	public abstract UDFA multiply(UDFA other);
}
