package RoomAssignmentSolvers;

import AMPL.AMPLHelper;
import Hotel.Instance;
import Hotel.RoomAssignment;

/** A Solver utilizing the housekeepingSchedule model with specified objective function */
public class AssignmentIPSolver implements AssignmentSolver {

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
	public RoomAssignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		return AMPLHelper.runAssignmentIP(instance, obj);
	}

	@Override
	public String toString() {
		return "Assignment (" + obj + ")";
	}

}
