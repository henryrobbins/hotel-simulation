package com.henryrobbins;

public class Main {

	public static void main(String[] args) {

		// PAPER SIMULATION

//		File file= new File("/Users/Henry/Downloads");
//
//		ArrayList<Solver<Assignment>> solvers= new ArrayList<>(Arrays.asList(
//			new Linear(),
//			new BestFirst(),
//			new WorstFirst(),
//			new AssignmentIPSolver("Mean_Satisfaction"),
//			new AssignmentIPSolver("Min_Satisfaction"),
//			new MinBelowTau(0.8)));
//
////			new AssignmentIPSolver("Satisfaction")));
//
//		ArrayList<Statistic<Assignment>> stats= new ArrayList<>(Arrays.asList(
//			new MinSatisfaction(),
//			new MeanSatisfaction(),
//			new PercentBelowTau(0.8)));

//		PercentBelowTau stat= new PercentBelowTau(0.75);
//		MinBelowTau solver= new MinBelowTau(0.75);
//		Hotel hotel= HotelFactory.readCSV("HotelEx");
//		Hotel hotel= HotelFactory.randHotel(25);
//		hotel.writeCSV();
//		Instance instance= InstanceFactory.readCSV("HotelEx", "InstEx");
//		Instance instance= InstanceFactory.randInstance(20);
//		Solver<Schedule> solver= new FirstAvailable();
//		System.out.println(instance);
//		System.out.println(solver.solve(instance).getVisual());
//		Assignment assignment= solver.solve(instance);
//		System.out.println(instance);
//		System.out.println(assignment);
//		System.out.println(stat.getStat(assignment));

//		new SimHotel<>(25, hotel, Solver.ASSIGNMENT_SOLVERS, Statistic.ASSIGNMENT_STATS, file, "res", null).start();

//		new SimSolvers<>(10, new int[] { 100 }, solvers, stats, file, "solversAvg_n100_t500", null).start();
//
//		new SimSolvers<>(100, new int[] { 25, 50, 75, 100 }, solvers, stats, file, "solversAvg_n100_t250", null)
//			.start();
//		new SimShowTrials<>(100, 50, solvers, stats, file, "histoMean_n100_t1000", null).start();

//
//		new SimRelaxations(50, 50, new double[] { 1, 0.98, 0.96, 0.94, 0.92, 0.90 },
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
//		new SimAddGuest(50, new int[] { 10, 150, 20, 140, 30, 130, 40, 120, 50, 110, 60, 100, 70, 80, 90 }, file,
//			"addGuest_t500", null).start();

	}
}