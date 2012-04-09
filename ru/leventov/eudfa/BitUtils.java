package ru.leventov.eudfa;

import java.util.ArrayList;

import static ru.leventov.eudfa.Primes.gcd;
import static ru.leventov.eudfa.Primes.isPrime;
import static ru.leventov.eudfa.Primes.lcm;

/**
 * Date: 04.04.12
 * Time: 21:45
 */
public class BitUtils {

	public static int wordsLength(int setLength) {
		if (setLength < 0) throw new IllegalArgumentException();

		int resLen = setLength / 64;
		if (setLength % 64 != 0) resLen++;
		return resLen;
	}

	/**
	 * @param left bit set of accepts of the left multiplier.
	 *                1 for bit 0 means ring accepts value 0 and so on
	 * @param right bit set of accepts of the left ring
	 * @return bit set of accepts of the product
	 */
	public static long ringMultiply(long left, int leftLen, long right, int rightLen) {
		if (leftLen < 1 || leftLen > 64 || rightLen < 1 || rightLen > 64)
			throw new IllegalArgumentException();

		// mask "undefined" bits
		left &= ~(-1L << leftLen);
		right &= ~(-1L << rightLen);

		int resLen = gcd(leftLen, rightLen);

		long result = (right & 1L) == 1L ? left : 0L;
		for(int a = 1; a < rightLen; a++)
			if ((right & (1L << a)) != 0L)
				result |= (left << a) | (left >>> (rightLen - a));

		long compRes = result;
		// why rl + ll ? not rl only?
		for(int i = resLen; i < rightLen + leftLen; i += resLen) {
			compRes |= result >>> i;
		}

		// cleanup result
		compRes &= ~(-1L << resLen);
		return compRes;
	}

	/**
	 * @param source bit set of accepts.
	 *                  1 for bit 0 means ring accepts value 0 and so on
	 * @param length length of the ring
	 * @return length of the simplification of the ring;
	 * ex. length (param) itself if the ring is simple
	 */
	public static int ringSimplify(long source, int length) {
		if (length < 1 || length > 64)
			throw new IllegalArgumentException();

		long tMask = ~(-1L << length);
		source &= tMask;

		if (source == 0L || source == tMask)
			return 1; // empty of full ring

		if (!isPrime(length))
			potentialLengths: for (int newLen = 2; newLen <= length / 2; newLen++) {
				if (length % newLen != 0) continue;
				int count = length / newLen;

				long mask = ~(-1L << newLen);
				long pattern = source & mask;

				for(int i = 1; i < count; i++) {
					mask <<= newLen;
					pattern <<= newLen;
					if (((source & mask) ^ pattern) != 0L)
						continue potentialLengths;
				}
				return newLen;
			}
		return length;
	}

	public static long ringSolve(long multiple, long product, int length) {
		if (length < 1 || length > 64)
			throw new IllegalArgumentException();

		long tMask = ~(-1L << length);
		product &= tMask;
		multiple &= tMask;

		long solution = 0L, notP = ~product;

		for(int i = 0; i < length; i++) {
			long shLeft = ((multiple << i) | (multiple >>> (length - i)))
			              & tMask;
			if ((shLeft & notP) != 0) continue; // вылезло
			solution |= (1L << i);
		}
		return solution;
	}

	public static ArrayList<Long> possibleLeftsFromProduct(
			int leftStep, int rightStep,
			long product, int length)
	{
		if (length < 1 || length > 64) throw new IllegalArgumentException();
		if (length % leftStep != 0 || length % rightStep != 0)
			throw new IllegalArgumentException();

		long tMask = ~(-1L << length);
		product &= tMask;

		ArrayList<Long> results = new ArrayList<Long>();

		long baseLeft = 0L;
		for(int i = 0; i < length; i += leftStep)
			baseLeft |= 1L << i;

		int leftCount = 1 << (length / leftStep);
		long leftMaskUnit = ~(-1L << leftStep), notP = ~product;
		long leftMask = 0L;
		for(int i = 1; i < leftCount; i++) {
			long pReminder = product;
			leftMask += leftMaskUnit;
			long left = baseLeft & leftMask;
			for(int rs = 0; rs < length; rs += rightStep) {

				long shLeft = ((left << rs) | (left >>> (length - rs)))
				              & tMask;

				// не подходит
				if ((shLeft & notP) != 0) continue;

				// подходит
				pReminder &= ~shLeft; // вычесть из остатка
			}
			if (pReminder == 0)
				results.add(left);
		}

		return results;
	}

	public static void main(String[] args) {
		ArrayList<Long> a = possibleLeftsFromProduct(1,1,45,6);
		for (long l : a) {
			System.out.println(Long.toBinaryString(l));
		}
		System.out.println(a.size());
	}

}
