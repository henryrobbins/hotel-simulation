package com.henryrobbins.solver.assignment;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

public class PreserveEdgesMeanSat implements Solver<Assignment> {

	/** The assignment we wish to preserve */
	private Assignment prevAssignment;
	/** The instance the previous assignment is for */
	private Instance prevInstance;

	/** Construct a solver which preserves edges from the given assignment
	 *
	 * @param prevInstance   The instance the assignment is for
	 * @param prevAssignment An assignment for the instance */
	public PreserveEdgesMeanSat(Instance prevInstance, Assignment prevAssignment) {
		if (!prevAssignment.isAssignmentFor(prevInstance))
			throw new IllegalArgumentException("This assignment is not for this instance");
		this.prevAssignment= prevAssignment;
		this.prevInstance= prevInstance;
	}

	@Override
	public Assignment solve(Instance instance) {

		AMPL ampl= AMPLHelper.createAMPL();
		AMPLHelper.uploadModel(ampl, "assignment");
		AMPLHelper.setObjectiveFunction(ampl, "Mean_Satisfaction");
		AMPLHelper.setRoomAndGuestParams(ampl, instance);
		AMPLHelper.setPreviousAssignment(ampl, prevInstance, prevAssignment);
		ampl.getParameter("preservedEdges").set(preserveEdges(instance));
		ampl.solve();

		Assignment assignment= AMPLHelper.getAssignment(ampl, instance);
		AMPLHelper.close(ampl);
		return assignment;

	}

	/** Return the maximum number of preserved edges between this previous assignment <br>
	 * and an assignment for the given instance
	 *
	 * @param instance The instance for which a maximally preserved assignment is being made */
	public int preserveEdges(Instance instance) {

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "assignment");
		AMPLHelper.setObjectiveFunction(ampl, "Preserved_Edges");
		AMPLHelper.setRoomAndGuestParams(ampl, instance);
		AMPLHelper.setPreviousAssignment(ampl, prevInstance, prevAssignment);
		ampl.solve();

		int preserved= (int) ampl.getObjective("Preserved_Edges").value();
		AMPLHelper.close(ampl);
		return preserved;
	}

	@Override
	public String toString() {
		return "Mean Sat s.t. Preserved Edges";
	}
}