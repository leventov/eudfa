package ru.leventov.eudfa;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static ru.leventov.eudfa.Primes.gcd;
import static ru.leventov.eudfa.Primes.isPrime;

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

	public static long zip(long source, int length, int to) {
		long copy = source;
		for(int i = to; i < length; i += to) {
			source |= copy >>> i;
		}
		return source & ~(-1L << to);
	}

	public static long wide(long source, int length, int to) {
		if (to > 64) throw new IllegalArgumentException();
		if (to % length != 0) throw new IllegalArgumentException();
		long res = source;
		for (int i = length; i < to; i += length) {
			res |= (source << i);
		}
		return res;
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

		// why rl + ll ? not rl only?
		return zip(result, rightLen + leftLen, resLen);
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
		return ringSolve(multiple, 1, product, length);
	}

	public static long ringSolve(long multiple, int step, long product, int length) {
		if (length < 1 || length > 64)
			throw new IllegalArgumentException();
		if (product == 0L) // иначе вернет все length нижних битов
			throw new IllegalArgumentException();
		if (length % step != 0) throw new IllegalArgumentException();

		long tMask = ~(-1L << length);
		product &= tMask;
		multiple &= tMask;

		long solution = 0L, notP = ~product;

		for(int i = 0; i < length; i += step) {
			long shM = ((multiple << i) | (multiple >>> (length - i)))
			              & tMask;
			if ((shM & notP) != 0) continue; // вылезло
			solution |= (1L << i);
			product &= ~shM;
		}
		if (product == 0) return solution; // полностью решает
		return 0L;
	}

	public static List<Long> minimumSolutions(
			long multiple, long product, int length) {
		return minimumSolutions(multiple, 1, product, length);
	}

	public static List<Long> minimumSolutions(
			long multiple, int step, long product, int length) {

		/* Повторение условий ringSolve */
		if (length < 1 || length > 64)
			throw new IllegalArgumentException();
		if (product == 0L) // иначе вернет все length нижних битов
			throw new IllegalArgumentException();
		if (length % step != 0) throw new IllegalArgumentException();

		long tMask = ~(-1L << length);
		product &= tMask;
		multiple &= tMask;
		/* Конец повторения */

		int multiplePointsC = 0;
		for (int i = 0; i < length; i++) {
			if ((multiple & (1L << i)) != 0)
				multiplePointsC++;
		}


		int[] solPoints = new int[length];
		int[][] pointSolutions = new int[length][multiplePointsC];
		int[] solCounts = new int[length];
		int c = 0;
		long notP = ~product;
		for(int i = 0; i < length; i += step) {
			long shM = ((multiple << i) | (multiple >>> (length - i)))
					& tMask;
			if ((shM & notP) != 0) continue; // вылезло

			// отличие тут только
			int lc = 0;
			for (int j = 0; j < length; j++) {
				if ((shM & (1L << j)) != 0) {
					pointSolutions[c][lc++] = j;
					solCounts[j]++;
				}
			}
			solPoints[c++] = i;
			product &= ~shM;
		}
		if (product != 0) return Collections.emptyList();
		/* Да и посюда почти то же самое. Надо следить*/

		product = ~notP;

		class Pair {
			final ArrayList<Integer> points;
			final int[] counts;

			Pair(ArrayList<Integer> points, int[] counts) {
				this.points = points;
				this.counts = counts;
			}
		}
		/*
		Это полный перебор - поиск минимальных подпокрытий.
		 */
		ArrayList<Long> results = new ArrayList<>();
		Queue<Pair> covers = new ArrayDeque<>();
		ArrayList<Integer> aCover = new ArrayList<>();
		for (int i = 0; i < c; i++) {
			aCover.add(solPoints[i]);
		}
		covers.add(new Pair(aCover, solCounts));
		while (!covers.isEmpty()) {
			Pair solution = covers.poll();
			boolean canBeLess = false;
			loopPoints: for (int i = 0; i < solution.points.size(); i++) {
				int point = solution.points.get(i);
				for (int pointSol: pointSolutions[point]) {
					if (solution.counts[pointSol] == 1)
						continue loopPoints;
				}
				canBeLess = true;

				int[] newCounts = Arrays.copyOf(solution.counts, solution.counts.length);
				for (int pointSol: pointSolutions[point])
					newCounts[pointSol]--;
				ArrayList<Integer> newPoints = new ArrayList<>();
				for (int p : solution.points)
					if (point != p) newPoints.add(p);
				covers.add(new Pair(newPoints, newCounts));
			}
			if (!canBeLess) {
				long sol = 0L;
				for (int point : solution.points)
					sol |= (1L << point);
				results.add(sol);
			}
		}
		return results;
	}

	public static List<Long> possibleLefts(
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
		for(int i = 1; i <= leftCount; i++) {
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
			if (pReminder == 0L)
				results.add(left);
		}

		return results;
	}

	public static List<Long> possibleRights(
			int leftStep, int rightStep,
			long product, int length)
	{
		// симметрия
		return possibleLefts(rightStep, leftStep, product, length);
	}

	public static long[] toArray(List<Long> list) {
		long[] res = new long[list.size()];
		for (int i = 0; i < res.length; i++)
			res[i] = list.get(i);
		return res;
	}

	public static String binaryString(List<Long> longs) {
		ArrayList<String> binaries = new ArrayList<>(longs.size());
		for (long l : longs) binaries.add(Long.toBinaryString(l));
		return binaries.toString();
	}

	public static void main(String[] args) {
		long product = 0b11111111L;
		int length = 6;
		List<Long> multiples = possibleLefts(2, 1, product, length);
		System.out.println(multiples.size() + ": " + binaryString(multiples));
		for (long m : multiples) {
			List<Long> others = minimumSolutions(m, 1, product, length);
			System.out.println(
					Long.toBinaryString(m) + ": " +
					Long.toBinaryString(ringSolve(m, 1, product, length)) + ", "
					+ others.size() + ": " + binaryString(others));
		}
	}
}
