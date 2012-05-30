package ru.leventov.eudfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static ru.leventov.eudfa.BitUtils.*;
import static ru.leventov.eudfa.Primes.gcd;
import static ru.leventov.eudfa.Primes.lcm;

/**
 * Date: 05.04.12
 * Time: 16:23
 */
public class Systems {

	public static void main(String[] args) {
		SystemAnswer sa = solve(new int[]{1, 2}, new int[]{1, 1},
				new Ring[]{
						Ring.byAccepts(3, new int[]{1}),
						Ring.byAccepts(6, new int[] {5})});
		System.out.println(sa.nice);
		for (Solution sol : sa.solution) {
			System.out.println(sol);
		}
	}

	public static SystemAnswer solve(int[] leftPows, int[] rightPows, Ring[] products) {
		int l = leftPows.length;

		/* Пока так! */
		if (l != 2) throw new IllegalArgumentException();

		if (l != rightPows.length || l != products.length)
			throw new IllegalArgumentException();

		int[] baseLeftLens = new int[l], baseRightLens = new int[l];
		int[] leftSteps = new int[l], rightSteps = new int[l];
		int[][] leftInverses = new int[l][], rightInverses = new int[l][];
		long[][] leftPossibles = new long[l][], rightPossibles = new long[l][];
		long[][] leftPsCommon = new long[l][], rightPsCommon = new long[l][];

		boolean nice = false;
		for(int i = 0; i < l; i++) {
			int pl = products[i].length();
			if (pl > 64) throw new IllegalArgumentException();

			leftSteps[i] = gcd(pl, leftPows[i]);
			baseLeftLens[i] = pl / leftSteps[i];

			leftInverses[i] = new int[pl];
			for (int j = 0; j < baseLeftLens[i]; j++)
				leftInverses[i][(j * leftPows[i]) % pl] = j;


			rightSteps[i] = gcd(pl, rightPows[i]);
			baseRightLens[i] = pl / rightSteps[i];

			rightInverses[i] = new int[pl];
			for (int j = 0; j < baseRightLens[i]; j++)
				rightInverses[i][(j * rightPows[i]) % pl] = j;


			calcPossibles(leftPossibles, leftPsCommon, leftSteps[i],
					rightSteps[i], leftInverses[i], products[i], i);

			calcPossibles(rightPossibles, rightPsCommon, rightSteps[i],
					leftSteps[i], rightInverses[i], products[i], i);

			if (rightPossibles[i].length > 1) nice = true;
			if (leftPossibles[i].length > 1) nice = true;
			Ring[] tpLeftPossibles = new Ring[leftPossibles[i].length];
			Ring[] tpLeftPsCommon = new Ring[leftPsCommon[i].length];
			Ring[] tpRightPsCommon = new Ring[rightPsCommon[i].length];
			Ring[] tpRightPossibles = new Ring[rightPossibles[i].length];
			for (int j = 0; j < leftPossibles[i].length; j++) {
				tpLeftPossibles[j] = Ring.byBits(pl, leftPossibles[i][j]);
				tpLeftPsCommon[j] = Ring.byBits(baseLeftLens[i], leftPsCommon[i][j]);
			}
			for (int j = 0; j < rightPossibles[i].length; j++) {
				tpRightPossibles[j] = Ring.byBits(pl, rightPossibles[i][j]);
				tpRightPsCommon[j] = Ring.byBits(baseRightLens[i], rightPsCommon[i][j]);
			}
			System.out.println("lp: " + Arrays.toString(tpLeftPossibles));
			System.out.println("lc: " + Arrays.toString(tpLeftPsCommon));
			System.out.println("rp: " + Arrays.toString(tpRightPossibles));
			System.out.println("rc: " + Arrays.toString(tpRightPsCommon));
		}
		//if (!nice) {
		//	return new SystemAnswer(null, false);
		//}
		nice = false;
		boolean incomp = false;
		long product0 = products[0].getStatesChuck(0);
		long product1 = products[1].getStatesChuck(0);
		ArrayList<Solution> res = new ArrayList<>();
		if (l == 2) {
			for (int i = 0; i < leftPsCommon[0].length; i++) {
				iterateLefts: for (int j = 0; j < leftPsCommon[1].length; j++) {
					long left0Common = leftPsCommon[0][i];
					long left1Common = leftPsCommon[1][j];
					long left0Eq = leftPossibles[0][i];
					long left1Eq = leftPossibles[1][j];

					long leftSol = compatible(left0Common, baseLeftLens[0], left1Common, baseLeftLens[1]);
					if (leftSol == 0L) {
						System.out.printf("left commons %d, %d - incomp.\n", i, j);
						incomp = true;
						continue;
					}
					List<Long> rightMinimums0 = minimumSolutions(left0Eq, rightSteps[0], product0, products[0].length());
					for (int k = 0; k < rightMinimums0.size(); k++)
						rightMinimums0.set(k, toCommonForm(rightMinimums0.get(k), products[0].length(), rightSteps[0], rightInverses[0]));
					List<Long> rightMinimums1 = minimumSolutions(left1Eq, rightSteps[1], product1, products[1].length());
					for (int k = 0; k < rightMinimums1.size(); k++)
						rightMinimums1.set(k, toCommonForm(rightMinimums1.get(k), products[1].length(), rightSteps[1], rightInverses[1]));

					long rightMax0 = toCommonForm(ringSolve(left0Eq, rightSteps[0], product0, products[0].length()),
							products[0].length(), rightSteps[0], rightInverses[0]);

					long rightMax1 = toCommonForm(ringSolve(left1Eq, rightSteps[1], product1, products[1].length()),
							products[1].length(), rightSteps[1], rightInverses[1]);

					int rightLen = lcm(baseRightLens[0], baseRightLens[1]);
					for (int aa = 0;; aa++) {

						long comp = wide(rightMax0, baseRightLens[0], rightLen) & wide(rightMax1, baseRightLens[1], rightLen);

						long zip0 = zip(comp, rightLen, baseRightLens[0]);

						long zip1 = zip(comp, rightLen, baseRightLens[1]);
						System.out.printf("%d %d: %d :\n", i, j, aa);
						System.out.println("right sol 0:" + Ring.byBits(products[0].length(), toEquationForm(rightMax0, baseRightLens[0], rightPows[0], products[0].length())));
						System.out.println("in common:" + Ring.byBits(baseRightLens[0], rightMax0));
						System.out.println("right sol 1:" + Ring.byBits(products[1].length(), toEquationForm(rightMax1, baseRightLens[1], rightPows[1], products[1].length())));
						System.out.println("in common:" + Ring.byBits(baseRightLens[1], rightMax1));

						System.out.println("comp: " + Ring.byBits(rightLen, comp));
						System.out.println("zip0: " + Ring.byBits(baseRightLens[0], zip0));
						System.out.println("zip1: " + Ring.byBits(baseRightLens[1], zip1));

						if (zip0 == rightMax0 && zip1 == rightMax1) {
							res.add(new Solution(leftSol, lcm(baseLeftLens[0], baseLeftLens[1]), comp, rightLen));
							//if (aa != 0) nice = true;
							continue iterateLefts;
						}

						if (zip0 < rightMax0) {
							for (Iterator<Long> itMin = rightMinimums0.iterator(); itMin.hasNext();) {
								long rMin = itMin.next();
								if (0 != (rMin & ~zip0)) // rMin is subset of zip0
									itMin.remove();
							}
							if (rightMinimums0.isEmpty()) {
								nice = true;
								continue iterateLefts;
							}

							rightMax0 = zip0;
						}

						if (zip1 < rightMax1) {
							for (Iterator<Long> itMin = rightMinimums1.iterator(); itMin.hasNext();) {
								long rMin = itMin.next();
								if (0 != (rMin & ~zip1)) // rMin is subset of zip1
									itMin.remove();
							}
							if (rightMinimums1.isEmpty()) {
								nice = true;
								continue iterateLefts;
							}

							rightMax1 = zip1;
						}
					}


				}
			}
		}
		return new SystemAnswer(res, nice && incomp && res.size() > 0);
	}

	// чтобы не копировать код
	private static void calcPossibles(
			long[][] possibles, long[][] psCommon,
			int step, int otherStep,
			int[] inverses, Ring product,
			int i // index
	) {
		int pl = product.length();
		possibles[i] = toArray(
				possibleLefts(step, otherStep,
						product.getStatesChuck(0), pl));
		int lpl = possibles[i].length;
		psCommon[i] = new long[lpl];
		for (int j = 0; j < lpl; j++)
			psCommon[i][j] = toCommonForm(possibles[i][j], pl, step, inverses);
	}

	// to "long" form
	private static long toEquationForm(long source, int len, int pow, int productLength) {
		long res = 0L;
		for (int i = 0; i < len; i++) {
			if ((source & (1L << i)) != 0L) {
				res |= 1L << (i * pow % productLength);
			}
		}
		return res;
	}

	private static long toCommonForm(long source, int len, int step, int[] inverses) {
		long res = 0L;
		for (int i = 0; i < len; i+= step) {
			if ((source & (1L << i)) != 0L) {
				res |= 1L << inverses[i];
			}
		}
		return res;
	}

	private static long compatible(long filterA, int lengthA, long filterB, int lengthB) {
		int sec = gcd(lengthA, lengthB);
		int resLen = lcm(lengthA, lengthB); // <= pl
		if (sec == 1)
			return (wide(filterA, lengthA, resLen) & wide(filterB, lengthB, resLen));

		long inSecA = zip(filterA, lengthA, sec);
		long inSecB = zip(filterB, lengthB, sec);
		if (inSecA != inSecB)
			return 0L;

		long possibleRes = wide(filterA, lengthA, resLen) & wide(filterB, lengthB, resLen);
		if ((zip(possibleRes, resLen, lengthA) == filterA)
			&& (zip(possibleRes, resLen, lengthB) == filterB))
			return possibleRes;
		
		return 0L;
	}



}
