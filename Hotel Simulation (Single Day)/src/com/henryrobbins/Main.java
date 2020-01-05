package com.henryrobbins;

import java.io.File;

import com.henryrobbins.decision.Statistic;
import com.henryrobbins.simulation.SimSolvers;
import com.henryrobbins.solver.Solver;

public class Main {

	public static void main(String[] args) {

		int[] sizes= { 2 };
		double[] a= { 1, 0.9, 0.8, 0.7, 0.6 };
		double[] b= { 1, 0.9, 0.8, 0.7, 0.6 };

		File file= new File("/Users/Henry/Downloads");
//		Instance instance= InstanceFactory.readCSV("test3", 2);

		new SimSolvers<>(100000, sizes, Solver.ASSIGNMENT_SOLVERS, Statistic.ASSIGNMENT_STATS, file, "compareSolvers",
			null).start();
//		new SimRunTimes<>(500, sizes, Solver.ASSIGNMENT_SOLVERS, file, "compareRunTimes", null).start();
//		new SimRelaxations(100, 2, a, b, Statistic.ASSIGNMENT_STATS, file, "compareRelaxations", null).start();
//		new SimInstanceSolvers<>(Solver.ASSIGNMENT_SOLVERS, Statistic.ASSIGNMENT_STATS, instance, file, null)
//			.start();
//		new SimAddGuest(500, sizes, file, "compareAddGuest", null).start();

	}
}