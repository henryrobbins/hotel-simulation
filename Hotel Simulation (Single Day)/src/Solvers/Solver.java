package Solvers;

import Hotel.Assignment;
import Hotel.Instance;

/** A Solver that makes hotel room assignments for a given Instance */
public interface Solver {

	/** Returns an Assignment for an Instance */
	Assignment solve(Instance instance);

}