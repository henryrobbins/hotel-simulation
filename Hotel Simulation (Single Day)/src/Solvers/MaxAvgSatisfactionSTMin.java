package Solvers;

import java.nio.file.Paths;

import com.ampl.AMPL;
import com.ampl.Environment;

import AMPL.AMPLHelper;
import Hotel.Assignment;
import Hotel.Instance;

public class MaxAvgSatisfactionSTMin implements Solver {

	@Override
	public Assignment solve(Instance instance) {

		double minimum= AMPLHelper.runIP("maxMinimumSatisfaction", instance).getMinimumSatisfaction();

		AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));
		ampl.setOption("solver", "gurobi");

		AMPLHelper.uploadModel(ampl, "maxAvgSatisfactionSubjectToMinimum");
		AMPLHelper.uploadData(ampl, instance);
		ampl.getParameter("minimum").set(minimum);
		ampl.solve();

		return AMPLHelper.generateAssignment(ampl, instance);

	}

	@Override
	public String toString() {
		return "Max Avg ST Min";
	}
}
