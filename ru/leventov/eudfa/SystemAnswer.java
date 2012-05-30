package ru.leventov.eudfa;

import java.util.List;

/**
 * Date: 29.05.12
 * Time: 12:42
 */
public class SystemAnswer {
	final List<Solution> solution;
	final boolean nice;

	public SystemAnswer(List<Solution> solution, boolean nice) {
		this.solution = solution;
		this.nice = nice;
	}
}
