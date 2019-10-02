package com.henryrobbins.solver.solution;

import java.util.ArrayList;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** A Solver utilizing the roomAssignment model with specified objective function. <br>
 * First, generates a housekeeping schedule according to the specified ScheduleSolver. This <br>
 * schedule is then used to give every room a completion time. */
public class AssignmentSTScheduleIP implements Solver<Solution> {

	/** The name of the objective function to be optimized */
	private String obj;
	/** The housekeeping schedule solver to be used */
	private Solver<Schedule> scheduleSolver;

	/** Construct solver with specified objective function and schedule solver
	 *
	 * @param obj            The name of the objective function (not null)
	 * @param scheduleSolver The solver used to generate a housekeeping schedule (not null) */
	public AssignmentSTScheduleIP(Solver<Schedule> scheduleSolver, String obj) {
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

		Schedule schedule= scheduleSolver.solve(instance);

		AMPL ampl= AMPLHelper.createAMPL();
		AMPLHelper.uploadModel(ampl, "assignment");
		AMPLHelper.setObjectiveFunction(ampl, obj);
		AMPLHelper.setRoomAndGuestParams(ampl, instance);

		ArrayList<Room> rooms= instance.rooms();
		double[] completion= new double[rooms.size()];
		int i= 0;
		for (Room room : rooms) {
			completion[i]= schedule.completion(room) - 1;
			i++ ;
		}
		ampl.getParameter("completion").setValues(completion);

		ampl.solve();
		Assignment assignment= AMPLHelper.getAssignment(ampl, instance);
		ampl.close();

		return new Solution(instance, schedule, assignment);

	}

	@Override
	public String toString() {
		return obj + " ST " + scheduleSolver;
	}

}