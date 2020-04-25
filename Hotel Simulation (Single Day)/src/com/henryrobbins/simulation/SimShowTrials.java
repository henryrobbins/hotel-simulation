package com.henryrobbins.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import com.henryrobbins.decision.Decision;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.solver.Solver;

/** Runs the given set of a certain type of solver on a set of randomly generated instances <br>
 * and maintains the given statistic. The statistic for each solver is shown for all trials. <br>
 * Creates a CSV file at the given directory in this form:
 *
 * <pre>
 *          |  Solver 1  |  Solver 1  |  Solver 1  |
 * --------------------------------------------------------
 *  Trial 1 |     -      |     -      |     -      |
 *  Trial 2 |     -      |     -      |     -      |
 *
 * </pre>
 */
public class SimShowTrials<T extends Decision> extends Simulation {

	/** The number of trials */
	private int trial;
	/** The hotel size to be simulated */
	private int size;
	/** The set of solvers */
	private ArrayList<Solver<T>> solvers;
	/** The statistics to be compared */
	private ArrayList<Statistic<T>> stats;

	public SimShowTrials(int t, int n, ArrayList<Solver<T>> solvers, Statistic<T> stat, File dir,
		String name, JProgressBar progress) {
		super(dir, name, progress);
		trial= t;
		size= n;
		this.solvers= solvers;
		this.stats= new ArrayList<>();
		stats.add(stat);
		simTotal= this.solvers.size() * trial;
	}

	public SimShowTrials(int t, int n, ArrayList<Solver<T>> solvers, ArrayList<Statistic<T>> stat, File dir,
		String name, JProgressBar progress) {
		super(dir, name, progress);
		trial= t;
		size= n;
		this.solvers= solvers;
		this.stats= stat;
		simTotal= this.solvers.size() * trial;
	}

	@Override
	public void run() {
		double[][][] result= new double[stats.size()][trial][solvers.size()];

		for (int t= 0; t < trial; t++ ) {
			Instance instance= InstanceFactory.randInstance(size);
			for (Solver<T> solver : solvers) {
				T outcome= solver.solve(instance);
				for (Statistic<T> stat : stats) {
					result[stats.indexOf(stat)][t][solvers.indexOf(solver)]= stat.getStat(outcome);
				}
				System.out.println(incrementSim());
			}
		}

		for (Statistic<T> stat : stats) {

			File file= new File(dir.toString() + "/" + name + "_" + stat.toString() + ".csv");
			try {

				FileWriter writer= new FileWriter(file);

				for (Solver<T> solver : solvers) {
					writer.write("," + solver.toString());
				}
				writer.write("\n");

				for (int t= 0; t < trial; t++ ) {
					writer.write("" + (t + 1));

					for (Solver<T> solver : solvers) {
						writer.write("," + result[stats.indexOf(stat)][t][solvers.indexOf(solver)]);
					}
					writer.write("\n");
				}

				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		reset();
	}

}
