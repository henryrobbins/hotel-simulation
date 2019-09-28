package RoomAssignmentSolvers;

import Hotel.Instance;
import Hotel.RoomAssignment;

/** A solver that makes hotel room assignments for a given instance */
public interface AssignmentSolver {

	/** Returns a room assignment for a given instance
	 *
	 * @param instance The instance to generate a room assignment for (not null)
	 * @return A room assignment for the given instance */
	RoomAssignment solve(Instance instance);

}