package HousekeepingSolvers;

import Hotel.HousekeepingSchedule;
import Hotel.Instance;

/** A solver that makes a housekeeping schedule for a given instance */
public interface ScheduleSolver {

	/** Returns a housekeeping schedule for a given instance
	 *
	 * @param instance The instance to generate a housekeeping schedule for (not null)
	 * @return A housekeeping schedule for the given instance */
	HousekeepingSchedule solve(Instance instance);

}