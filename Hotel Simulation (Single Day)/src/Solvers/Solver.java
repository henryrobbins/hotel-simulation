package Solvers;

import Hotel.Instance;
import Hotel.Solution;

/** A solver that returns both a housekeeping schedule and room assignment */
public interface Solver {

	/** Return a solution containing a housekeeping schedule and room assignment for an instance
	 *
	 * @param instance The instance to generate a housekeeping schedule <br>
	 *                 and room assignment for (not null) */
	Solution solve(Instance instance);

}
