package Solvers;

import AMPL.AMPLHelper;
import Hotel.Assignment;
import Hotel.Instance;

/** Solver for one round Integer Programs named model */
public class IPSolver implements Solver {

	/** The name of the IP model */
	private String model;

	/** Constructor: sets the name of the IP model */
	public IPSolver(String model) {
		this.model= model;
	}

	@Override
	/** returns the Assignment for the Instance using the IP (model) */
	public Assignment solve(Instance instance) {
		return AMPLHelper.runIP(model, instance);
	}

	@Override
	public String toString() {
		return model;
	}

}
