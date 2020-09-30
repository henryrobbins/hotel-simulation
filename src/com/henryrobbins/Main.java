package com.henryrobbins;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.decision.Statistic.MeanSatisfaction;
import com.henryrobbins.decision.Statistic.MinSatisfaction;
import com.henryrobbins.decision.Statistic.PercentBelowTau;
import com.henryrobbins.decision.Statistic.SumUpgrade;
import com.henryrobbins.hotel.Hotel;
import com.henryrobbins.hotel.HotelFactory;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.simulation.SimInstanceSolvers;
import com.henryrobbins.solver.Solver;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.BestFirst;
import com.henryrobbins.solver.assignment.Linear;
import com.henryrobbins.solver.assignment.WorstFirst;

public class Main {

	public static void main(String[] args) throws Exception {

		Instance instance= InstanceFactory.randInstance(200);
		instance.writeCSV(new File("/Users/Henry/Downloads").toPath(), "hotel.csv");

		/** DEMO **/

		/** CONNECTING TO AMPL */

		Path ampl= new File("/Users/Henry/AMPL").toPath();
		AMPLHelper.setPath(ampl);

		/** READING CSV FILES */

		// Paths to CSV files
		Path hotelCSV= new File("/Users/Henry/Downloads/hotel.csv").toPath();
		Path arrivalsCSV= new File("/Users/Henry/Downloads/arrivals.csv").toPath();
		Path weightsCSV= new File("/Users/Henry/Downloads/weights.csv").toPath();

		// Create a hotel from a CSV file
		Hotel hotel= HotelFactory.readCSV(hotelCSV);

		// Create an instance from CSV files
		instance= InstanceFactory.readCSV(hotel, arrivalsCSV, weightsCSV);
		// Or, equivalently
		instance= InstanceFactory.readCSV(hotelCSV, arrivalsCSV, weightsCSV);
		System.out.println(instance);

		/** RUNNING SOLVERS */

		// Heuristics
		Linear lin= new Linear();
		BestFirst best= new BestFirst();
		WorstFirst worst= new WorstFirst();

		// Optimal (integer programming) solvers
		AssignmentIPSolver optMean= new AssignmentIPSolver("Mean_Satisfaction");
		AssignmentIPSolver optMin= new AssignmentIPSolver("Min_Satisfaction");

		// Generating a room assignment for an instance
		Assignment bestAssignment= best.solve(instance);
		Assignment optMeanAssignment= optMean.solve(instance);
		Assignment optMinAssignment= optMin.solve(instance);

		/** EVALUATING ASSIGNMENTS */

		// Viewing an assignment
		System.out.println(bestAssignment);
		System.out.println(optMeanAssignment);
		System.out.println(optMinAssignment);

		// Statistics
		MeanSatisfaction meanSat= new MeanSatisfaction();
		MinSatisfaction minSat= new MinSatisfaction();
		SumUpgrade upgradeSum= new SumUpgrade();

		// Getting value of statistics on an assignment
		Double upgradeSumLin= upgradeSum.getStat(bestAssignment);
		Double optMeanStat= meanSat.getStat(optMeanAssignment);
		Double optMinStat= minSat.getStat(optMinAssignment);

		/** Weighted Objective Functions */

		AssignmentIPSolver solver= new AssignmentIPSolver("Mean_Min_Sat_And_Upgrades", 1, 1, 2);

		/** Solvers with parameters */

		// Construct solvers
		AssignmentIPSolver minBelow90= new AssignmentIPSolver("Below_Tau", 0.90);
		AssignmentIPSolver minBelow80= new AssignmentIPSolver("Below_Tau", 0.80);
		AssignmentIPSolver minBelow85= new AssignmentIPSolver("Below_Tau", 0.85);

		// Create random instance and solve
		instance= InstanceFactory.randInstance(25);
		Assignment assignment80= minBelow80.solve(instance);
		Assignment assignment85= minBelow85.solve(instance);
		Assignment assignment90= minBelow90.solve(instance);

		// Output results
		PercentBelowTau pctBelow90= new PercentBelowTau(0.90);
		System.out.println("Min_Below(0.80): " + pctBelow90.getStat(assignment80));
		System.out.println("Min_Below(0.85): " + pctBelow90.getStat(assignment85));
		System.out.println("Min_Below(0.90): " + pctBelow90.getStat(assignment90));

		/** Running a Simulation */

		// Load CSV files and create Instance
		hotelCSV= new File("/Users/Henry/Downloads/hotel.csv").toPath();
		arrivalsCSV= new File("/Users/Henry/Downloads/arrivals.csv").toPath();
		weightsCSV= new File("/Users/Henry/Downloads/weights.csv").toPath();
		instance= InstanceFactory.readCSV(hotelCSV, arrivalsCSV, weightsCSV);

		// Solvers to run
		ArrayList<Solver<Assignment>> solvers= new ArrayList<>(Arrays.asList(
			new Linear(),
			new BestFirst(),
			new WorstFirst()));
		// Statistics to compute
		ArrayList<Statistic<Assignment>> stats= new ArrayList<>(Arrays.asList(
			new MeanSatisfaction(),
			new MinSatisfaction(),
			new PercentBelowTau(0.8),
			new SumUpgrade()));

		// Run simulation
		File resultDir= new File("/Users/Henry/Downloads");
		String name= "sim2";
		new SimInstanceSolvers<>(solvers, stats, instance, resultDir, name, null).start();

		Double blank= upgradeSumLin + optMeanStat + optMinStat;

		// TESTING

		int t= 5;
		int s[]= { 25, 50, 75, 100 };
		double a[]= { 0, 1 };
		double b[]= { 0, 1 };
		double g[]= { 0, 1 };
		String obj= "Mean_Min_Sat_And_Upgrades";
		solvers= new ArrayList<>(Arrays.asList(
			new Linear(),
			new BestFirst(),
			new WorstFirst(),
			new AssignmentIPSolver("Mean_Satisfaction"),
			new AssignmentIPSolver("Min_Satisfaction"),
			new AssignmentIPSolver("Below_Tau", 0.8),
			new AssignmentIPSolver("Upgrades"),
			new AssignmentIPSolver("Feasible"),
			new AssignmentIPSolver("Mean_And_Min_Satisfaction", 1, 1, 0),
			new AssignmentIPSolver("Mean_Min_Sat_And_Upgrades", 1, 1, 1),
			new AssignmentIPSolver("Mean_And_Below_Tau", 0.8, 1, 1, 0),
			new AssignmentIPSolver("Mean_Below_Tau_And_Upgrades", 0.8, 1, 1, 1)));
		stats= new ArrayList<>(Arrays.asList(
			new MeanSatisfaction(),
			new MinSatisfaction(),
//					new PercentBelowTau(0.8),
			new SumUpgrade()));
		resultDir= new File("/Users/Henry/Downloads");
		name= "res";

//		new SimRunTimes<>(t, s, solvers, resultDir, name, null).start();
//		new SimSolvers<>(t, s, solvers, stats, resultDir, name, null).start();
//		new SimParameters(t, s[0], a, b, g, obj, stats, resultDir, name, null).start();

	}
}