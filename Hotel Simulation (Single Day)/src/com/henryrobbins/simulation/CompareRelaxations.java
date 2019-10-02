package com.henryrobbins.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.decision.Statistic.SumUpgrade;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.MinUpgradesSTSatIP;

/** Runs the three-round solver minimizing upgrades for all combinations of the provided alpha <br>
 * and beta relaxation values. The given statistics are recorded and averaged over t trials. <br>
 * Furthermore, the average optimal number of upgrades is recorded. <br>
 * Creates a CSV file at the given directory in this form:
 *
 * <pre>
 *
 *  Statistic 1  |  beta 1  |  beta 2  |  beta 3
 * -------------------------------------------------
 *    alpha 1    |     -    |     -    |     -
 *    alpha 2    |     -    |     -    |     -
 *
 *  Statistic 2  |  beta 1  |  beta 2  |  beta 3
 * -------------------------------------------------
 *    alpha 1    |     -    |     -    |     -
 *    alpha 2    |     -    |     -    |     -
 *
 * Average optimal upgrades:   ---
 *
 * </pre>
 */
public class CompareRelaxations extends Simulation {

	/** The number of trials */
	private int trial;
	/** The size of the hotel */
	private int size;
	/** The set of alpha relaxation values to test */
	private double[] alpha;
	/** The set of beta relaxation values to test */
	private double[] beta;
	/** The set of statistics */
	private ArrayList<Statistic<Assignment>> stats;

	public CompareRelaxations(int t, int n, double[] a, double[] b, ArrayList<Statistic<Assignment>> stats, File dir,
		String name, JProgressBar progress) {
		super(dir, name, progress);
		trial= t;
		size= n;
		alpha= a;
		beta= b;
		this.stats= stats;
		simTotal= t * a.length * b.length + t;
	}

	@Override
	public void run() {

		double[][][] result= new double[stats.size()][alpha.length][beta.length];
		double optUpgrades= 0;

		for (int t= 0; t < trial; t++ ) {
			Instance instance= InstanceFactory.createRandom(name + "_t" + t, size);
			AssignmentIPSolver minUpgrades= new AssignmentIPSolver("Upgrades");
			SumUpgrade tot= new SumUpgrade();
			optUpgrades+= tot.getStat(minUpgrades.solve(instance));
			System.out.println(incrementSim());
			for (int a= 0; a < alpha.length; a++ ) {
				for (int b= 0; b < beta.length; b++ ) {
					MinUpgradesSTSatIP solver= new MinUpgradesSTSatIP(alpha[a], beta[b]);
					Assignment assignment= solver.solve(instance);
					for (Statistic<Assignment> stat : stats) {
						result[stats.indexOf(stat)][a][b]+= stat.getStat(assignment);
					}
					System.out.println(incrementSim());
				}
			}
		}

		optUpgrades/= trial;
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

			for (Statistic<Assignment> stat : stats) {

				writer.write("Statistic: " + stat);
				for (double b : beta) {
					writer.write("," + b);
				}
				writer.write("\n");

				for (int a= 0; a < alpha.length; a++ ) {
					writer.write(alpha[a] + "");
					for (int b= 0; b < beta.length; b++ ) {
						writer.write("," + result[stats.lastIndexOf(stat)][a][b]);
					}
					writer.write("\n");
				}
			}

			writer.write("\n" + "Average Total Upgrades:," + optUpgrades);

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		reset();

	}

}
