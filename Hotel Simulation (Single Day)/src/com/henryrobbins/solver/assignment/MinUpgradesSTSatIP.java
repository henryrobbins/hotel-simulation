package com.henryrobbins.solver.assignment;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

/** Housekeeping solver using a three-round IP. Rounds 1 and 2 are done by maxMeanSatSTMin IP to
 * generate the optimal minimum satisfaction and optimal average satisfaction subject to this
 * minimum. The solver also takes alpha and beta values in range 0..1 as inputs which relax the
 * constraints. The objective function minimizes the sum of upgrades subject to the min and mean
 * satisfaction found in rounds 1 and 2 respectively. */
public class MinUpgradesSTSatIP implements Solver<Assignment> {

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
		if (beta < 0 || beta > 1) throw new IllegalArgumentException("Beta not in range 0..1" + " " + beta);
		this.alpha= alpha;
		this.beta= beta;
	}

	/** Return the assignment with minimum upgrades subject to min and mean satisfaction <br>
	 * constraints relaxed by the current alpha and beta relaxation constants */
	@Override
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		MaxMeanSatSTMinIP firstRound= new MaxMeanSatSTMinIP();
		Assignment presolve= firstRound.solve(instance);
		double minimum= presolve.satisfactionStats().getMin();
		double average= presolve.satisfactionStats().getMean();

		return solve(instance, minimum, average);
	}

	/** Return the assignment with minimum upgrades subject to min and mean satisfaction <br>
	 * constraints relaxed by the current alpha and beta relaxation constants. The minimum <br>
	 * and average satisfaction are given as inputs to the problem */
	public Assignment solve(Instance instance, double min, double mean) {

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "assignment");
		AMPLHelper.setObjectiveFunction(ampl, "Upgrades");
		AMPLHelper.setRoomAndGuestParams(ampl, instance);
		ampl.getParameter("minMeanMatchingWeight").set(mean * alpha);
		ampl.getVariable("minWeight").fix(min * beta);
		ampl.solve();

		Assignment assignment= AMPLHelper.getAssignment(ampl, instance);
		AMPLHelper.close(ampl);
		return assignment;
	}

	@Override
	public String toString() {
		return "minUpgradesSTSat IP";
	}

}
