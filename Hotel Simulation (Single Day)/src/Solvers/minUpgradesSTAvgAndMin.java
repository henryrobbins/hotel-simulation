package Solvers;

import java.nio.file.Paths;

import com.ampl.AMPL;
import com.ampl.Environment;

import AMPL.AMPLHelper;
import Hotel.Assignment;
import Hotel.Instance;

public class minUpgradesSTAvgAndMin implements Solver {

	private double alpha= 0.0;
	private double beta= 0.0;

	public minUpgradesSTAvgAndMin(double alpha, double beta) {
		this.alpha= alpha;
		this.beta= beta;
	}

	@Override
	public Assignment solve(Instance instance) {

		MaxAvgSatisfactionSTMin firstRound= new MaxAvgSatisfactionSTMin();
		Assignment presolve= firstRound.solve(instance);
		double minimum= presolve.getMinimumSatisfaction();
		double average= presolve.getAverageSatisfaction();

		AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));
		ampl.setOption("solver", "gurobi");

		AMPLHelper.uploadModel(ampl, "maxUpgradesSubjectToSatisfaction");
		AMPLHelper.uploadData(ampl, instance);
		ampl.getParameter("average").set(average * alpha);
		ampl.getParameter("minimum").set(minimum * beta);
		ampl.solve();

		return AMPLHelper.generateAssignment(ampl, instance);
	}

	@Override
	public String toString() {
		return "Min Upgrades ST Avg and Min";
	}

}
