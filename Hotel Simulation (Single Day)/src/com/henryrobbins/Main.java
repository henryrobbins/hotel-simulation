package com.henryrobbins;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.decision.Statistic.MeanSatisfaction;
import com.henryrobbins.decision.Statistic.MinSatisfaction;
import com.henryrobbins.simulation.SimAddGuest;
import com.henryrobbins.simulation.SimRelaxations;
import com.henryrobbins.solver.Solver;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.BestFirst;
import com.henryrobbins.solver.assignment.Linear;
import com.henryrobbins.solver.assignment.WorstFirst;

public class Main {

	public static void main(String[] args) {

		// PAPER SIMULATION

		File file= new File("/Users/Henry/Downloads");

		ArrayList<Solver<Assignment>> solvers= new ArrayList<>(Arrays.asList(
			new Linear(),
			new BestFirst(),
			new WorstFirst(),
			new AssignmentIPSolver("Mean_Satisfaction"),
			new AssignmentIPSolver("Min_Satisfaction"),
			new AssignmentIPSolver("Satisfaction")));

		ArrayList<Statistic<Assignment>> stats= new ArrayList<>(Arrays.asList(
			new MinSatisfaction(),
			new MeanSatisfaction()));

//		new SimSolvers<>(250, new int[] { 75 }, solvers, stats, file, "solversAvg_n75_t250", null).start();
//		new SimShowTrials<>(1000, 75, solvers, new MeanSatisfaction(), file, "histoMean_n75_t1000", null).start();
//		new SimShowTrials<>(1000, 75, solvers, new MinSatisfaction(), file, "histoMin_n75_t1000", null).start();
//
		new SimRelaxations(250, 75, new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97 },
			new double[] { 1, 0.98, 0.96, 0.94, 0.92, 0.90 }, Statistic.ASSIGNMENT_STATS, file, "abRelax_n75_t250_02",
			null).start();

//		new SimRelaxations(250, 50, new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
//			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
//			"abRelax_n50_t250_005", null).start();

		new SimRelaxations(250, 75, new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
			"abRelax_n75_t250_005", null).start();

		new SimRelaxations(250, 100,
			new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
			"abRelax_n100_t250_005", null).start();

		new SimAddGuest(250, new int[] { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150 }, file,
			"addGuest_t250", null).start();

	}
}