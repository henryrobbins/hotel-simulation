package com.henryrobbins;

import java.io.File;

import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;

public class Main {

	public static void main(String[] args) {

		// PAPER SIMULATION

		File file= new File("/Users/Henry/Downloads");

//		ArrayList<Solver<Assignment>> solvers= new ArrayList<>(Arrays.asList(
//			new Linear(),
//			new BestFirst(),
//			new WorstFirst()));
//			new AssignmentIPSolver("Mean_Satisfaction"),
//			new AssignmentIPSolver("Min_Satisfaction"),
//			new AssignmentIPSolver("Satisfaction")));

//		ArrayList<Statistic<Assignment>> stats= new ArrayList<>(Arrays.asList(
//			new MinSatisfaction(),
//			new MeanSatisfaction()));

		Instance instance= InstanceFactory.randInstance(10);
		System.out.println(instance);

//		new SimSolvers<>(10, new int[] { 100 }, solvers, stats, file, "solversAvg_n100_t500", null).start();
//
//		new SimSolvers<>(500, new int[] { 100 }, solvers, stats, file, "solversAvg_n100_t500", null).start();
//		new SimShowTrials<>(500, 100, solvers, new MeanSatisfaction(), file, "histoMean_n100_t1000", null).start();
//		new SimShowTrials<>(500, 100, solvers, new MinSatisfaction(), file, "histoMin_n100_t1000", null).start();
//
//		new SimRelaxations(500, 100, new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97 },
//			new double[] { 1, 0.98, 0.96, 0.94, 0.92, 0.90 }, Statistic.ASSIGNMENT_STATS, file, "abRelax_n100_t500_02",
//			null).start();
//
//		new SimRelaxations(500, 50, new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
//			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
//			"abRelax_n50_t500_005", null).start();
//
//		new SimRelaxations(500, 75, new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
//			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
//			"abRelax_n75_t500_005", null).start();
//
//		new SimRelaxations(500, 100,
//			new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
//			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
//			"abRelax_n100_t500_005", null).start();
//
//		new SimRelaxations(500, 125,
//			new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
//			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
//			"abRelax_n125_t500_005", null).start();
//
//		new SimRelaxations(500, 150,
//			new double[] { 1, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.965, 0.96, 0.955, 0.95 },
//			new double[] { 1 }, Statistic.ASSIGNMENT_STATS, file,
//			"abRelax_n150_t500_005", null).start();
//
//		new SimAddGuest(500, new int[] { 10, 150, 20, 140, 30, 130, 40, 120, 50, 110, 60, 100, 70, 80, 90 }, file,
//			"addGuest_t500", null).start();

	}
}