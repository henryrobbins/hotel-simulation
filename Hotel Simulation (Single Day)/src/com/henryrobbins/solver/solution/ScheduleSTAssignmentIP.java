package com.henryrobbins.solver.solution;

import java.util.ArrayList;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** A Solver utilizing the housekeepingSchedule model with specified objective function. <br>
 * First, generates a room assignment according to the specified AssignmentSolver. This room <br>
 * assignment is then used to give every room a deadline or due date. */
public class ScheduleSTAssignmentIP implements Solver<Solution> {

	/** The name of the objective function to be optimized */
	private String obj;
	/** The room assignment solver to be used */
	private Solver<Assignment> assignmentSolver;

	/** Construct solver with specified objective function and assignment solver
	 *
	 * @param obj              The name of the objective function (not null)
	 * @param assignmentSolver The solver used to generate a room assignment (not null) */
	public ScheduleSTAssignmentIP(Solver<Assignment> assignmentSolver, String obj) {
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

		Assignment assignment= assignmentSolver.solve(instance);

		AMPL ampl= AMPLHelper.createAMPL();
		AMPLHelper.uploadModel(ampl, "schedule");
		AMPLHelper.setObjectiveFunction(ampl, obj);
		AMPLHelper.setRoomAndGuestParams(ampl, instance);
		AMPLHelper.setHousekeepingParams(ampl, instance);

		ArrayList<Room> rooms= instance.rooms();
		int maxT= ampl.getSet("TIME").size();
		double[] deadline= new double[rooms.size()];
		int i= 0;
		for (Room room : rooms) {
			Guest guest= assignment.assignment().getKey(room);
			deadline[i]= guest != null ? guest.arrival() : maxT;
			i++ ;
		}
		ampl.getParameter("deadline").setValues(deadline);

		ampl.solve();
		Schedule schedule= AMPLHelper.getSchedule(ampl, instance);
		ampl.close();
		return new Solution(instance, schedule, assignment);

	}

	@Override
	public String toString() {
		return obj + " ST " + assignmentSolver;
	}

}