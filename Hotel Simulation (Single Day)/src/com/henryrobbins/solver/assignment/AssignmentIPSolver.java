package com.henryrobbins.solver.assignment;

import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

/** A Solver utilizing the assignment model with specified objective function */
public class AssignmentIPSolver implements Solver<Assignment> {

	/** The name of the objective function to be optimized */
	private String obj;

	/** Construct solver with specified objective function
	 *
	 * @param obj The objective function this solver will optimize */
	public AssignmentIPSolver(String obj) {
		if (obj == null) throw new IllegalArgumentException("Objective function was null");
		this.obj= obj;
	}

	@Override
	/** Return the room assignment optimizing the objective function */
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		return AMPLHelper.runAssignmentIP(instance, obj);
	}

	@Override
	public String toString() {
		return obj;
	}
}