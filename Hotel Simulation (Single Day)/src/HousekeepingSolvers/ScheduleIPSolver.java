package HousekeepingSolvers;

import AMPL.AMPLHelper;
import Hotel.HousekeepingSchedule;
import Hotel.Instance;

/** A Solver utilizing the housekeepingSchedule model with specified objective function */
public class ScheduleIPSolver implements ScheduleSolver {

	/** The name of the objective function to be optimized */
	private String obj;

	/** Construct solver with specified objective function
	 *
	 * @param obj The objective function this solver will optimize */
	public ScheduleIPSolver(String obj) {
		if (obj == null) throw new IllegalArgumentException("Objective function was null");
		this.obj= obj;
	}

	@Override
	/** Return the housekeeping schedule optimizing the objective function */
	public HousekeepingSchedule solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		return AMPLHelper.runScheduleIP(instance, obj);
	}

	@Override
	public String toString() {
		return "Schedule (" + obj + ")";
	}

}
