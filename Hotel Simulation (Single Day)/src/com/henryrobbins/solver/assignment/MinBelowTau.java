package com.henryrobbins.solver.assignment;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

public class MinBelowTau implements Solver<Assignment> {

	private double tau;

	public MinBelowTau(double tau) {
		if (tau < 0 || tau > 1) throw new IllegalArgumentException("Tau not in range 0..1");
		this.tau= tau;
	}

	@Override
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "assignment");
		AMPLHelper.setObjectiveFunction(ampl, "Below_Tau");
		AMPLHelper.setRoomAndGuestParams(ampl, instance);
		ampl.getParameter("tau").set(tau);
		ampl.solve();

		Assignment assignment= AMPLHelper.getAssignment(ampl, instance);
		AMPLHelper.close(ampl);
		return assignment;
	}

	@Override
	public String toString() {
		return "Min Below " + tau + " IP";
	}

}
