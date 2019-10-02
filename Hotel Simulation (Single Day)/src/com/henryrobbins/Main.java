package com.henryrobbins;

import java.io.File;

import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.simulation.CompareAddGuest;
import com.henryrobbins.simulation.CompareInstanceSolutions;
import com.henryrobbins.simulation.CompareRelaxations;
import com.henryrobbins.simulation.CompareRunTimes;
import com.henryrobbins.simulation.CompareSolvers;
import com.henryrobbins.solver.Solver;

public class Main {

	public static void main(String[] args) {

		int[] sizes= { 10, 15, 20 };
		double[] a= { 1, 0.95, 0.9 };
		double[] b= { 1, 0.95, 0.9 };

		File file= new File("/Users/Henry/Downloads");
		Instance instance= InstanceFactory.readCSV("test3", 2);

		new CompareSolvers<>(10, sizes, Solver.ASSIGNMENT_SOLVERS, Statistic.ASSIGNMENT_STATS, file, "compareSolvers",
			null).start();
		new CompareRunTimes<>(10, sizes, Solver.ASSIGNMENT_SOLVERS, file, "compareRunTimes", null).start();
		new CompareRelaxations(10, 15, a, b, Statistic.ASSIGNMENT_STATS, file, "compareRelaxations", null).start();
		new CompareInstanceSolutions<>(Solver.ASSIGNMENT_SOLVERS, Statistic.ASSIGNMENT_STATS, instance, file, null)
			.start();
		new CompareAddGuest(10, sizes, file, "compareAddGuest", null).start();

	}
}
