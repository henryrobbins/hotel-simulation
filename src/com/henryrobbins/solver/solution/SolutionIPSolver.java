package com.henryrobbins.solver.solution;

import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

/** A Solver utilizing the scheduleAndAssignment model with specified objective function */
public class SolutionIPSolver implements Solver<Solution> {

	/** The name of the objective function to be optimized */
	private String obj;

	/** Construct solver with specified objective function
	 *
	 * @param obj The objective function this solver will optimize */
	public SolutionIPSolver(String obj) {
		if (obj == null) throw new IllegalArgumentException("Objective function was null");
		this.obj= obj;
	}

	@Override
	/** Return the solution optimizing the objective function */
	public Solution solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		return AMPLHelper.runSolutionIP(instance, obj);
	}

	@Override
	public String toString() {
		return "Solution (" + obj + ")";
	}

}
