package com.henryrobbins.solver.solution;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

public class CombinationSolver implements Solver<Solution> {

	private Solver<Schedule> housekeepingSolver;
	private Solver<Assignment> assignmentSolver;

	public CombinationSolver(Solver<Schedule> housekeepingSolver, Solver<Assignment> assignmentSolver) {
		this.housekeepingSolver= housekeepingSolver;
		this.assignmentSolver= assignmentSolver;
	}

	@Override
	public Solution solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Schedule schedule= housekeepingSolver.solve(instance);
		Assignment assignment= assignmentSolver.solve(instance);

		return new Solution(instance, schedule, assignment);
	}

	@Override
	public String toString() {
		return housekeepingSolver.toString() + " " + assignmentSolver.toString();
	}

}
