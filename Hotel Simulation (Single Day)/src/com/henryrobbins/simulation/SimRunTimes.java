package com.henryrobbins.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import org.apache.commons.lang3.time.StopWatch;

import com.henryrobbins.decision.Decision;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.solver.Solver;

/** Runs the given set of a certain type of solver on a set of randomly generated instances <br>
 * and maintains the solve time. Solve times are averaged over t trials on all given hotel <br>
 * sizes. Creates a CSV file at the given directory in this form:
 *
 * <pre>
 * 		    |  Size 1  |  Size 2  |  Size 3
 * -----------------------------------------
 * Solver1  |    -     |    -     |    -
 * Solver2  |    -     |    -     |    -
 *
 * </pre>
 */
public class SimRunTimes<T extends Decision> extends Simulation {

	/** The number of trials */
	private int trial;
	/** The set of hotel sizes to be simulated */
	private int[] sizes;
	/** The set of solvers */
	private ArrayList<Solver<T>> solvers;

	public SimRunTimes(int t, int[] n, ArrayList<Solver<T>> solvers, File dir, String name,
		JProgressBar progress) {
		super(dir, name, progress);
		trial= t;
		sizes= n;
		this.solvers= solvers;
		simTotal= this.solvers.size() * sizes.length * trial;
	}

	@Override
	public void run() {

		double[][] result= new double[solvers.size()][sizes.length];

		for (Solver<T> solver : solvers) {
			for (int t= 0; t < trial; t++ ) {
				for (int n= 0; n < sizes.length; n++ ) {
					Instance instance= InstanceFactory.createRandom(name + "_n" + sizes[n] + "_t" + t, sizes[n]);
					StopWatch watch= new StopWatch();
					watch.start();
					solver.solve(instance);
					watch.stop();
					result[solvers.indexOf(solver)][n]+= watch.getNanoTime() / 1000000.0;
					System.out.println(incrementSim());
				}
			}
		}

		for (int s= 0; s < solvers.size(); s++ ) {
			for (int n= 0; n < sizes.length; n++ ) {
				result[s][n]/= trial;
			}
		}

		File file= new File(dir.toString() + "/" + name + ".csv");
		try {

			FileWriter writer= new FileWriter(file);

			for (int n= 0; n < sizes.length; n++ ) {
				writer.write("," + sizes[n]);
			}

			writer.write("\n");

			for (Solver<T> solver : solvers) {
				writer.write(solver.toString());
				for (int n= 0; n < sizes.length; n++ ) {
					writer.write("," + result[solvers.indexOf(solver)][n]);
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