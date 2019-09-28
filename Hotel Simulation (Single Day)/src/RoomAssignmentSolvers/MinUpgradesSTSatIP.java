package RoomAssignmentSolvers;

import com.ampl.AMPL;

import AMPL.AMPLHelper;
import Hotel.Instance;
import Hotel.RoomAssignment;

/** Housekeeping solver using a three-round IP. Rounds 1 and 2 are done by maxMeanSatSTMin IP to
 * generate the optimal minimum satisfaction and optimal average satisfaction subject to this
 * minimum. The solver also takes alpha and beta values in range 0..1 as inputs which relax the
 * constraints. The objective function minimizes the sum of upgrades subject to the min and mean
 * satisfaction found in rounds 1 and 2 respectively. */
public class MinUpgradesSTSatIP implements AssignmentSolver {

	/** The relaxation constant for mean satisfaction constraint (in 0..1) */
	private double alpha= 0.0;
	/** The relaxation constant for minimum satisfaction constraint (in 0..1) */
	private double beta= 0.0;

	/** Construct an IP solver using minUpgradesSTSat.mod with given relaxations
	 *
	 * @param alpha The relaxation constant for mean satisfaction constraint (in 0..1)
	 * @param beta  The relaxation constant for minimum satisfaction constraint (in 0..1) */
	public MinUpgradesSTSatIP(double alpha, double beta) {
		if (alpha < 0 || alpha > 1) throw new IllegalArgumentException("Alpha not in range 0..1");
		if (beta < 0 || beta > 1) throw new IllegalArgumentException("Beta not in range 0..1");
		this.alpha= alpha;
		this.beta= beta;
	}

	/** Return the assignment with minimum upgrades subject to min and mean satisfaction <br>
	 * constraints relaxed by the current alpha and beta relaxation constants */
	@Override
	public RoomAssignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		MaxMeanSatSTMinIP firstRound= new MaxMeanSatSTMinIP();
		RoomAssignment presolve= firstRound.solve(instance);
		double minimum= presolve.getMinimumSatisfaction();
		double average= presolve.getAverageSatisfaction();

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "roomAssignment");
		AMPLHelper.setObjectiveFunction(ampl, "Upgrades");
		AMPLHelper.uploadRoomAndGuestData(ampl, instance);
		ampl.getParameter("meanSatisfaction").set(average * alpha);
		ampl.getVariable("minSatisfaction").fix(minimum * beta);
		ampl.solve();

		return AMPLHelper.generateAssignment(ampl, instance);
	}

	@Override
	public String toString() {
		return "minUpgradesSTSat IP";
	}

}
