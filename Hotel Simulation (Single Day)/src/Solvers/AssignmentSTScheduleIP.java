package Solvers;

import java.util.ArrayList;

import com.ampl.AMPL;

import AMPL.AMPLHelper;
import Hotel.HousekeepingSchedule;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;
import Hotel.Solution;
import HousekeepingSolvers.ScheduleSolver;

/** A Solver utilizing the roomAssignment model with specified objective function. <br>
 * First, generates a housekeeping schedule according to the specified ScheduleSolver. This <br>
 * schedule is then used to give every room a completion time. */
public class AssignmentSTScheduleIP implements Solver {

	/** The name of the objective function to be optimized */
	private String obj;
	/** The housekeeping schedule solver to be used */
	private ScheduleSolver scheduleSolver;

	/** Construct solver with specified objective function and schedule solver
	 *
	 * @param obj            The name of the objective function (not null)
	 * @param scheduleSolver The solver used to generate a housekeeping schedule (not null) */
	public AssignmentSTScheduleIP(ScheduleSolver scheduleSolver, String obj) {
		if (obj == null) throw new IllegalArgumentException("Objective function was null");
		if (scheduleSolver == null) throw new IllegalArgumentException("Schedule solver was null");
		this.obj= obj;
		this.scheduleSolver= scheduleSolver;
	}

	@Override
	/** Returns the solution comprising of the room assignment optimizing the objective function <br>
	 * and the housekeeping schedule generated via this schedule solver */
	public Solution solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		HousekeepingSchedule schedule= scheduleSolver.solve(instance);

		AMPL ampl= AMPLHelper.createAMPL();
		AMPLHelper.uploadModel(ampl, "roomAssignment");
		AMPLHelper.setObjectiveFunction(ampl, obj);
		AMPLHelper.uploadRoomAndGuestData(ampl, instance);

		ArrayList<Room> rooms= instance.getRooms();
		double[] completion= new double[rooms.size()];
		int i= 0;
		for (Room room : rooms) {
			completion[i]= room.getAvailability(schedule) - 1;
			i++ ;
		}
		ampl.getParameter("completion").setValues(completion);

		ampl.solve();
		RoomAssignment assignment= AMPLHelper.generateAssignment(ampl, instance);

		return new Solution(instance, schedule, assignment);

	}

	@Override
	public String toString() {
		return "Assignment (" + obj + ") ST " + scheduleSolver;
	}

}