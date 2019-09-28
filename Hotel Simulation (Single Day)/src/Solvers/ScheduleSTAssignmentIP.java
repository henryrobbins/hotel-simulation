package Solvers;

import java.util.ArrayList;

import com.ampl.AMPL;

import AMPL.AMPLHelper;
import Hotel.Guest;
import Hotel.HousekeepingSchedule;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;
import Hotel.Solution;
import RoomAssignmentSolvers.AssignmentSolver;

/** A Solver utilizing the housekeepingSchedule model with specified objective function. <br>
 * First, generates a room assignment according to the specified AssignmentSolver. This room <br>
 * assignment is then used to give every room a deadline or due date. */
public class ScheduleSTAssignmentIP implements Solver {

	/** The name of the objective function to be optimized */
	private String obj;
	/** The room assignment solver to be used */
	private AssignmentSolver assignmentSolver;

	/** Construct solver with specified objective function and assignment solver
	 *
	 * @param obj              The name of the objective function (not null)
	 * @param assignmentSolver The solver used to generate a room assignment (not null) */
	public ScheduleSTAssignmentIP(AssignmentSolver assignmentSolver, String obj) {
		if (obj == null) throw new IllegalArgumentException("Objective function was null");
		if (assignmentSolver == null) throw new IllegalArgumentException("Assignment solver was null");
		this.obj= obj;
		this.assignmentSolver= assignmentSolver;
	}

	@Override
	/** Returns the solution comprising of the room assignment generated via this assignment <br>
	 * solver and a housekeeping schedule optimizing the objective function */
	public Solution solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		RoomAssignment assignment= assignmentSolver.solve(instance);

		AMPL ampl= AMPLHelper.createAMPL();
		AMPLHelper.uploadModel(ampl, "housekeepingSchedule");
		AMPLHelper.setObjectiveFunction(ampl, obj);
		AMPLHelper.uploadRoomAndGuestData(ampl, instance);
		AMPLHelper.uploadHousekeepingData(ampl, instance);

		ArrayList<Room> rooms= instance.getRooms();
		int maxT= ampl.getSet("TIME").size();
		double[] deadline= new double[rooms.size()];
		int i= 0;
		for (Room room : rooms) {
			Guest guest= assignment.getAssignment().get(room);
			deadline[i]= guest != null ? guest.getArrivalTime() : maxT;
			i++ ;
		}
		ampl.getParameter("deadline").setValues(deadline);

		ampl.solve();
		HousekeepingSchedule schedule= AMPLHelper.generateSchedule(ampl, instance);

		return new Solution(instance, schedule, assignment);

	}

	@Override
	public String toString() {
		return "Schedule (" + obj + ") ST " + assignmentSolver;
	}

}