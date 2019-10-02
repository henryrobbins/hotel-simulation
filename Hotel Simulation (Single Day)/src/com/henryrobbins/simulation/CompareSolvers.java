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
 * and maintains the given set of statistics. Statistics are averaged over t trials on all given
 * hotel sizes. Creates a CSV file at the given directory in this form:
 *
 * <pre>
 *  Size 1  |  Statistic 1  |  Statistic 2  |  Statistic 3
 * --------------------------------------------------------
 * Solver1  |       -       |       -       |       -
 * Solver2  |       -       |       -       |       -
 *
 *  Size 2  |  Statistic 1  |  Statistic 2  |  Statistic 3
 * --------------------------------------------------------
 * Solver1  |       -       |       -       |       -
 * Solver2  |       -       |       -       |       -
 * </pre>
 */
public class CompareSolvers<T extends Decision> extends Simulation {

	/** The number of trials */
	private int trial;
	/** The set of hotel sizes to be simulated */
	private int[] sizes;
	/** The set of solvers */
	private ArrayList<Solver<T>> solvers;
	/** The set of statistics */
	private ArrayList<Statistic<T>> stats;

	public CompareSolvers(int t, int[] n, ArrayList<Solver<T>> solvers, ArrayList<Statistic<T>> stats, File dir,
		String name, JProgressBar progress) {
		super(dir, name, progress);
		trial= t;
		sizes= n;
		this.solvers= solvers;
		this.stats= stats;
		simTotal= this.solvers.size() * sizes.length * trial;
	}

	@Override
	public void run() {
		double[][][] result= new double[sizes.length][solvers.size()][stats.size()];

		for (int n= 0; n < sizes.length; n++ ) {
			for (int t= 0; t < trial; t++ ) {
				Instance instance= InstanceFactory.createRandom(name + "_n" + sizes[n] + "_t" + t, sizes[n]);
				for (Solver<T> solver : solvers) {
					T outcome= solver.solve(instance);
					for (Statistic<T> stat : stats) {
						result[n][solvers.indexOf(solver)][stats.indexOf(stat)]+= stat.getStat(outcome);
					}
					System.out.println(incrementSim());
				}
			}
		}

		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				for (int k= 0; k < result[i][j].length; k++ ) {
					result[i][j][k]/= trial;
				}
			}
		}

		File file= new File(dir.toString() + "/" + name + ".csv");
		try {

			FileWriter writer= new FileWriter(file);

			for (int n= 0; n < sizes.length; n++ ) {

				writer.write("Size: " + sizes[n]);
				for (Statistic<T> stat : stats) {
					writer.write("," + stat.toString());
				}
				writer.write("\n");

				for (Solver<T> solver : solvers) {
					writer.write(solver.toString());
					for (Statistic<T> stat : stats) {
						writer.write("," + result[n][solvers.indexOf(solver)][stats.indexOf(stat)]);
					}
					writer.write("\n");
				}
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		reset();
	}

}
