package com.henryrobbins.solver.schedule;

import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

/** A Solver utilizing the housekeepingSchedule model with specified objective function */
public class ScheduleIPSolver implements Solver<Schedule> {

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
	public Schedule solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		return AMPLHelper.runScheduleIP(instance, obj);
	}

	@Override
	public String toString() {
		return obj;
	}

}
