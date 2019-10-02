package com.henryrobbins.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import com.henryrobbins.decision.Decision;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.Solver;

/** Runs the given set of a certain type of solver on a set of randomly generated instances <br>
 * and maintains the given set of statistics. Statistics are averaged over t trials on all given
 * hotel sizes. Creates a CSV file at the given directory in this form:
 *
 * <pre>
 *          |  Statistic 1  |  Statistic 2  |  Statistic 3
 * --------------------------------------------------------
 * Solver1  |       -       |       -       |       -
 * Solver2  |       -       |       -       |       -
 *
 * </pre>
 */
public class CompareInstanceSolutions<T extends Decision> extends Simulation {

	/** The instance to be solved */
	private Instance instance;
	/** The set of solvers */
	private ArrayList<Solver<T>> solvers;
	/** The set of statistics */
	private ArrayList<Statistic<T>> stats;

	public CompareInstanceSolutions(ArrayList<Solver<T>> solvers, ArrayList<Statistic<T>> stats, Instance instance,
		File dir,
		JProgressBar progress) {
		super(dir, instance.name(), progress);
		this.instance= instance;
		this.solvers= solvers;
		this.stats= stats;
		simTotal= this.solvers.size();
	}

	@Override
	public void run() {
		double[][] result= new double[solvers.size()][stats.size()];

		for (Solver<T> solver : solvers) {
			T outcome= solver.solve(instance);
			for (Statistic<T> stat : stats) {
				result[solvers.indexOf(solver)][stats.indexOf(stat)]+= stat.getStat(outcome);
			}
			System.out.println(incrementSim());
		}

		File file= new File(dir.toString() + "/" + name + ".csv");
		try {

			FileWriter writer= new FileWriter(file);

			for (Statistic<T> stat : stats) {
				writer.write("," + stat.toString());
			}
			writer.write("\n");

			for (Solver<T> solver : solvers) {
				writer.write(solver.toString());
				for (Statistic<T> stat : stats) {
					writer.write("," + result[solvers.indexOf(solver)][stats.indexOf(stat)]);
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		reset();
	}

}
