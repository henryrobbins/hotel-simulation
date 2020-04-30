package com.henryrobbins;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.decision.Statistic.MeanSatisfaction;
import com.henryrobbins.decision.Statistic.MinSatisfaction;
import com.henryrobbins.decision.Statistic.SumUpgrade;
import com.henryrobbins.simulation.SimParameters;
import com.henryrobbins.solver.Solver;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.BestFirst;
import com.henryrobbins.solver.assignment.Linear;
import com.henryrobbins.solver.assignment.WorstFirst;

public class Main {

	public static void main(String[] args) {

		// TESTING

		int t= 5;
		int s[]= { 25, 50, 75, 100 };
		double a[]= { 0, 1 };
		double b[]= { 0, 1 };
		double g[]= { 0, 1 };
		String obj= "Mean_Min_Sat_And_Upgrades";
		ArrayList<Solver<Assignment>> solvers= new ArrayList<>(Arrays.asList(
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
		ArrayList<Statistic<Assignment>> stats= new ArrayList<>(Arrays.asList(
			new MeanSatisfaction(),
			new MinSatisfaction(),
//			new PercentBelowTau(0.8),
			new SumUpgrade()));
		File resultDir= new File("/Users/Henry/Downloads");
		String name= "res";

//		new SimRunTimes<>(t, s, solvers, resultDir, name, null).start();
//		new SimSolvers<>(t, s, solvers, stats, resultDir, name, null).start();
		new SimParameters(t, s[0], a, b, g, obj, stats, resultDir, name, null).start();

	}
}