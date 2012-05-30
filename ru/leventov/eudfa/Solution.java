package ru.leventov.eudfa;

/**
* Date: 29.05.12
* Time: 12:42
*/
public class Solution {
	final Ring left, right;

	public Solution(long left, int leftLen, long right, int rightLen) {
		this.left = Ring.byBits(leftLen, left);
		this.right = Ring.byBits(rightLen, right);
	}

	public String toString() {
		return "left: " + left +" right: " + right;
	}
}
