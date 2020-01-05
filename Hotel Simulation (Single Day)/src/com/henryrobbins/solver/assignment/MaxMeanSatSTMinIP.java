package com.henryrobbins.solver.assignment;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

/** Assignment solver using a two-round IP. First, the optimal minimum satisfaction is found. <br>
 * Then, the mean satisfaction is maximized subject to the optimal minimum satisfaction. */
public class MaxMeanSatSTMinIP implements Solver<Assignment> {

	/** Return the assignment maximizing mean satisfaction subject to optimal minimum */
	@Override
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		double minimum= AMPLHelper.runAssignmentIP(instance, "Min_Satisfaction").satisfactionStats().getMin();

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "assignment");
		AMPLHelper.setObjectiveFunction(ampl, "Mean_Satisfaction");
		AMPLHelper.setRoomAndGuestParams(ampl, instance);
		ampl.getVariable("minWeight").fix(minimum);
		ampl.solve();

		Assignment assignment= AMPLHelper.getAssignment(ampl, instance);
		AMPLHelper.close(ampl);
		return assignment;

	}

	@Override
	public String toString() {
		return "maxMeanSatSTMin IP";
	}

}
