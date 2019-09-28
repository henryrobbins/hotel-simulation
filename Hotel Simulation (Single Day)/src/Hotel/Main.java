package Hotel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.time.StopWatch;

import Solvers.AssignLexicographically;
import Solvers.AssignLinearly;
import Solvers.IPSolver;
import Solvers.MaxAvgSatisfactionSTMin;
import Solvers.OnlineSTAverage;
import Solvers.Solver;
import Solvers.minUpgradesSTAvgAndMin;
import Solvers.suggestiveSTAverage;

public class Main {

	public static void main(String[] args) {

		AssignLinearly linear= new AssignLinearly();
		AssignLexicographically lexico= new AssignLexicographically();
		IPSolver maxPref= new IPSolver("maxMetPrefs");
		MaxAvgSatisfactionSTMin maxSatSTMin= new MaxAvgSatisfactionSTMin();
		minUpgradesSTAvgAndMin minUpgrades= new minUpgradesSTAvgAndMin(1, 1);
		OnlineSTAverage online= new OnlineSTAverage();
		suggestiveSTAverage suggestive= new suggestiveSTAverage(1);

		compareRunTimes(50, new int[] { 10, 20, 30, 40, 50 }, new Solver[] { linear, lexico, maxPref, maxSatSTMin },
			"/Users/Henry/Downloads");
//
//		Instance test= InstanceFactory.createInstance("saef");
//		Assignment assignment1= linear.solve(test);
//		Assignment assignment2= lexico.solve(test);
//		Assignment assignment3= maxPref.solve(test);
//		Assignment assignment4= maxSatSTMin.solve(test);
//		Assignment assignment5= minUpgrades.solve(test);
//		Assignment assignment6= online.solve(test);
//		Assignment assignment7= suggestive.solve(test);
//
//		assignment1.printStats();
//		assignment2.printStats();
//		assignment3.printStats();
//		assignment4.printStats();
//		assignment5.printStats();
//		assignment6.printStats();
//		assignment7.printStats();

	}

	// TODO: develop general testing procedures
	// (1) compare run times
	// -- (i) distribution of runtime
	// -- (ii) compare average runtime over n trials
	// (2) compare assignment statistics
	// -- (i) average statistics over n trials
	// -- (ii) adjust parameters (alpha and beta)

	/** Generates a CSV file containing average run times over t trials for every solver <br>
	 * for random instances at every provided size n */
	public static void compareRunTimes(int t, int[] n, Solver[] solver, String directory) {

		double[][] result= new double[n.length][solver.length];

		for (int i= 0; i < result.length; i++ ) {
			for (int trial= 0; trial < t; trial++ ) {
				Instance instance= InstanceFactory.createRandomInstance(n[i], "compareRunTimes");
				for (int j= 0; j < result[i].length; j++ ) {
					StopWatch watch= new StopWatch();
					watch.start();
					solver[j].solve(instance);
					watch.stop();
					result[i][j]+= watch.getNanoTime() / 1000000.0;
				}
			}
		}

		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				result[i][j]/= t;
			}
		}

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			for (int i= 0; i < solver.length; i++ ) {
				writer.write("," + solver[i].toString());
			}

			writer.write("\n");

			for (int i= 0; i < result.length; i++ ) {
				writer.write(n[i] + " ");
				for (int j= 0; j < result[i].length; j++ ) {
					writer.write("," + result[i][j]);
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
