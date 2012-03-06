package ru.leventov.eudfa;

import java.util.HashMap;

import static ru.leventov.eudfa.Primes.lcm;

/**
 * Date: 06.03.12
 * Time: 5:02
 */
public class Tests {
	public static HashMap<Integer, Integer> productLengths(int f, int s) {
		HashMap<Integer, Integer> lengthMap = new HashMap<>();
		for (int i = 0; i <= lcm(f, s); i++) lengthMap.put(i, 0);

		for (int i = 0; i < 1 << f; i++) {
			Ring fr = Ring.byBits(f, i);
			if (!fr.simple()) continue;
			for (int j = 0; j < 1 << s; j++) {
				Ring sr = Ring.byBits(s, j);
				if (!sr.simple()) continue;
				Ring res = fr.multiply(sr);
				lengthMap.put(res.length(),
						lengthMap.get(res.length()) + 1);
			}
		}

		for (int k : lengthMap.keySet().toArray(new Integer[]{}))
			if (lengthMap.get(k) == 0)
				lengthMap.remove(k);

		return lengthMap;
	}
}
