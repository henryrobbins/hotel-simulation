package com.henryrobbins.solver.assignment;

import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

/** A Solver utilizing assignment.mod with specified objective function and tunable parameters */
public class AssignmentIPSolver implements Solver<Assignment> {

	/** name of the objective function to be optimized */
	private String obj;
	/** setting for tau--the threshold for guest satisfaction */
	private double tau= 0;
	/** setting for the tunable parameter alpha */
	private double alpha= 1;
	/** setting for the tunable parameter beta */
	private double beta= 1;
	/** setting for the tunable parameter gamma */
	private double gamma= 1;

	/** Construct assignment solver with specified objective function. Use default parameters.
	 *
	 * @param obj objective function this solver will optimize */
	public AssignmentIPSolver(String obj) {
		this(obj, 0.0, 1.0, 1.0, 1.0);
	}

	/** Construct solver with specified objective function, tunable parameters alpha, beta, and gamma
	 *
	 * @param obj   The objective function this solver will optimize
	 * @param alpha setting for the tunable parameter alpha
	 * @param beta  setting for the tunable parameter beta
	 * @param gamma setting for the tunable parameter gamma */
	public AssignmentIPSolver(String obj, double alpha, double beta, double gamma) {
		this(obj, 0.0, alpha, beta, gamma);
	}

	/** Construct assignment solver with specified objective function. Use the specified tau <br>
	 * and default settings for alpha, beta, and gamma
	 *
	 * @param obj The objective function this solver will optimize
	 * @param tau setting for the tunable parameter tau */
	public AssignmentIPSolver(String obj, double tau) {
		this(obj, tau, 1.0, 1.0, 1.0);
	}

	/** Construct solver with specified objective function, tunable parameters alpha, beta, and gamma
	 *
	 * @param obj   The objective function this solver will optimize
	 * @param alpha setting for the tunable parameter alpha
	 * @param beta  setting for the tunable parameter beta
	 * @param gamma setting for the tunable parameter gamma
	 * @param tau   setting for the tunable parameter tau */
	public AssignmentIPSolver(String obj, double tau, double alpha, double beta, double gamma) {
		if (alpha < 0) throw new IllegalArgumentException("Alpha < 0");
		if (beta < 0) throw new IllegalArgumentException("Beta < 0");
		if (gamma < 0) throw new IllegalArgumentException("Gamma < 0");
		if (tau < 0 || tau > 1) throw new IllegalArgumentException("tau not in [0,1]");
		if (obj == null) throw new IllegalArgumentException("Objective function was null");
		this.obj= obj;
		this.tau= tau;
		this.alpha= alpha;
		this.beta= beta;
		this.gamma= gamma;
	}

	@Override
	/** Return the room assignment optimizing the objective function */
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		return AMPLHelper.runAssignmentIP(instance, obj, tau, alpha, beta, gamma);
	}

	@Override
	public String toString() {
		return obj;
	}
}