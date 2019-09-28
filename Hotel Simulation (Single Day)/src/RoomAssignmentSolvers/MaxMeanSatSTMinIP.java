package RoomAssignmentSolvers;

import com.ampl.AMPL;

import AMPL.AMPLHelper;
import Hotel.Instance;
import Hotel.RoomAssignment;

/** Housekeeping solver using a two-round IP. First, the optimal minimum satisfaction is determined
 * by maxMinSat IP. Then, a new IP (maxMeanSatSTMin.mod) is solved with an objective function that
 * maximizes mean satisfaction subject to every guest's satisfaction being above the minimum. */
public class MaxMeanSatSTMinIP implements AssignmentSolver {

	/** Return the assignment maximizing mean satisfaction subject to optimal minimum */
	@Override
	public RoomAssignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		double minimum= AMPLHelper.runAssignmentIP(instance, "Min_Satisfaction")
			.getMinimumSatisfaction();

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "roomAssignment");
		AMPLHelper.setObjectiveFunction(ampl, "Mean_Satisfaction");
		AMPLHelper.uploadRoomAndGuestData(ampl, instance);
		ampl.getVariable("minSatisfaction").fix(minimum);
		ampl.solve();

		return AMPLHelper.generateAssignment(ampl, instance);

	}

	@Override
	public String toString() {
		return "maxMeanSatSTMin IP";
	}

}
