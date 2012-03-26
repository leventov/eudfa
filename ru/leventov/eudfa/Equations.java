package ru.leventov.eudfa;

import java.util.*;

import static java.lang.Math.min;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;
import static ru.leventov.eudfa.Primes.gcd;
import static ru.leventov.eudfa.Primes.lcm;
import static ru.leventov.eudfa.Ring.byAccepts;

/**
 * Date: 06.03.12
 * Time: 15:15
 */
public class Equations {

	public static List<Ring> solveRight(Ring left, Ring product) {
		return solveRight(left, product, CLASSIC);
	}

	public static List<Ring> principleSolveRight(Ring left,
												 Ring product) {
		return solveRight(left, product, PRINCIPLE);
	}

	private static List<Ring> solveRight(Ring left, Ring product,
										 RightSolver solver) {
		if (product == Ring.FULL) throw new IllegalArgumentException();

		int ll = left.length();
		int pl = product.length();
		if (ll % pl != 0)
			return Collections.emptyList();

		ArrayList<Ring> solutions = new ArrayList<Ring>();
		
		 // length of the product must divide right
		for(int i = 1; i < 7; i++) { // 20?
			solutions.addAll(solver.iSolveRight(left, product, i * pl));
		}

		return solutions;
	}


	public static List<Ring> solveRight(Ring left, Ring product,
	                                          int rightLength) {
		return solveRight(left, product, rightLength, CLASSIC);
	}

	public static List<Ring> principleSolveRight(Ring left,
	                                                   Ring product,
	                                                   int rightLength) {
		return solveRight(left, product, rightLength, PRINCIPLE);
	}

	private static List<Ring> solveRight(Ring left, Ring product,
	                                          int rightLength,
	                                          RightSolver solver) {
		if (rightLength <= 0 || product == Ring.FULL)
			throw new IllegalArgumentException();
		
		int pl = product.length();
		if (left.length() % pl != 0 || rightLength % pl != 0)
			return Collections.emptyList();

		return solver.iSolveRight(left, product, rightLength);
	}
	
	

	private static interface RightSolver {
		public List<Ring> iSolveRight(Ring left, Ring product,
		                                    int rightLength);
	}
	
	private static class ClassicRightSolver implements RightSolver {
		
		public List<Ring> iSolveRight(Ring left, Ring product,
		                                    int rightLength) {
			int[] allRightAccepts =
					getAllRightAccepts(left, product, rightLength);
			int al = allRightAccepts.length;

			ArrayList<Ring> solutions = new ArrayList<Ring>();

			int[] t = new int[al];
			// from 1 - don't try EMPTY as second 
			for(int i = 1; i < (1 << al); i++) {
				int c = 0;
				for(int k = 0; k < al; k++)
					if ((i & (1 << k)) != 0) t[c++] = allRightAccepts[k];

				Ring right = byAccepts(rightLength, copyOf(t, c));
				if (left.multiply(right).equals(product)) {
					if (right.simple())
						solutions.add(right);
				}
			}
			return solutions;			
		}
	}
	private static ClassicRightSolver CLASSIC = new ClassicRightSolver();

	private static class PrincipleRightSolver implements RightSolver {

		public List<Ring> iSolveRight(Ring left, Ring product,
		                                    int rightLength) {
			int[] allRightAccepts =
					getAllRightAccepts(left, product, rightLength);
			int al = allRightAccepts.length;
			
			int minMultipleLength = gcd(left.length(), rightLength);

			BitSet[] sets = new BitSet[al];

			ArrayList<Ring> solutions = new ArrayList<Ring>();

			for(int i = 0; i < al; i++) {
				Ring right = byAccepts(rightLength,
						new int[]{allRightAccepts[i]});

				sets[i] = left.multiply(right)
						.wideTo(minMultipleLength).statesAsBitSet();
			}

//			for (int i = 0; i < sets.length; i++) {
//				BitSet set = sets[i];
//				System.out.println(allRightAccepts[i] + " " + set);
//			}


			BitSet productSet =
					product.wideTo(minMultipleLength)
							.statesAsBitSet();

			int[] t = new int[al];
			for(int i = 1; i < (1 << al); i++) {
				int c = 0;
				BitSet result = new BitSet();
				for(int k = 0; k < al; k++)
					if ((i & (1 << k)) != 0) {
						result.or(sets[k]);
						t[c++] = allRightAccepts[k];
					}
				if (productSet.equals(result))
					solutions.add(byAccepts(rightLength, copyOf(t, c)));
			}

			return solutions;
		}
	}
	private static PrincipleRightSolver PRINCIPLE =
			new PrincipleRightSolver();



	private static int[] getAllRightAccepts(Ring left, Ring product,
	                                        int rightLength) {
		int[] rawProductAccepts =
				rawProduct(left, product, rightLength).accepts();

		int start = left.getAccept(0), end = start + rightLength;
		int from = binarySearch(rawProductAccepts, start);
		if (from < 0) from = -from - 1;
		int to = binarySearch(rawProductAccepts, end);
		if (to < 0) to = -to - 1;
		int al = to - from;

		int[] allRightAccepts = new int[al];
		System.arraycopy(rawProductAccepts, from,
				allRightAccepts, 0, al);
		for(int i = 0; i < al; i++) {
			allRightAccepts[i] -= start;
		}
		return allRightAccepts;
	}

	public static void main(String[] args) {
		Collection<Ring> solutions = principleSolveRight(
				byAccepts(9, new int[]{0, 1}),
				byAccepts(3, new int[]{0, 1}));
		for (Ring r: solutions) {
			System.out.println(r);
		}
		System.out.println(solutions.size());
	}
	
	private static Ring rawProduct(Ring multiple, Ring product,
	                               int anotherLength) {
		int rawLen = lcm(multiple.length(), anotherLength);
		return product.wide(rawLen / product.length());
	}
}