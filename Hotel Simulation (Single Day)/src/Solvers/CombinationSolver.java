package Solvers;

import Hotel.HousekeepingSchedule;
import Hotel.Instance;
import Hotel.RoomAssignment;
import Hotel.Solution;
import HousekeepingSolvers.ScheduleSolver;
import RoomAssignmentSolvers.AssignmentSolver;

public class CombinationSolver implements Solver {

	private ScheduleSolver housekeepingSolver;
	private AssignmentSolver assignmentSolver;

	public CombinationSolver(ScheduleSolver housekeepingSolver, AssignmentSolver assignmentSolver) {
		this.housekeepingSolver= housekeepingSolver;
		this.assignmentSolver= assignmentSolver;
	}

	@Override
	public Solution solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		HousekeepingSchedule schedule= housekeepingSolver.solve(instance);
		RoomAssignment assignment= assignmentSolver.solve(instance);

		return new Solution(instance, schedule, assignment);
	}

	@Override
	public String toString() {
		return housekeepingSolver.toString() + " " + assignmentSolver.toString();
	}

}
