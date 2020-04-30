package com.henryrobbins.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;

/** Runs a specified assignment solver for all combinations of alpha, beta, and gamma. <br>
 * The given statistics are recorded and averaged over t trials. A separate CSV file <br>
 * for each statistic is created at the given directory in this form:
 *
 * <pre>
 *
 *    gamma 1    |  beta 1  |  beta 2  |  beta 3
 * -------------------------------------------------
 *    alpha 1    |     -    |     -    |     -
 *    alpha 2    |     -    |     -    |     -
 *
 *    gamma 2    |  beta 1  |  beta 2  |  beta 3
 * -------------------------------------------------
 *    alpha 1    |     -    |     -    |     -
 *    alpha 2    |     -    |     -    |     -
 *
 * </pre>
 */
public class SimParameters extends Simulation {

	/** The number of trials */
	private int trial;
	/** The size of the hotel */
	private int size;
	/** name of the objective function */
	private String obj;
	/** tau parameter values */
	private double tau= 0.8;
	/** The set of alpha parameter values to test */
	private double[] alpha;
	/** The set of beta parameter values to test */
	private double[] beta;
	/** The set of gamma parameter values to test */
	private double[] gamma;
	/** The set of statistics */
	private ArrayList<Statistic<Assignment>> stats;

	public SimParameters(int t, int n, double tau, double[] a, double[] b, double[] g, String obj,
		ArrayList<Statistic<Assignment>> stats, File dir, String name, JProgressBar progress) {
		super(dir, name, progress);
		trial= t;
		size= n;
		alpha= a;
		beta= b;
		gamma= g;
		this.obj= obj;
		this.stats= stats;
		simTotal= t * a.length * b.length + 2 * t;
	}

	public SimParameters(int t, int n, double[] a, double[] b, double[] g, String obj,
		ArrayList<Statistic<Assignment>> stats, File dir, String name, JProgressBar progress) {
		super(dir, name, progress);
		trial= t;
		size= n;
		alpha= a;
		beta= b;
		gamma= g;
		this.obj= obj;
		this.stats= stats;
		simTotal= t * a.length * b.length * g.length;
	}

	@Override
	public void run() {

		double[][][][] result= new double[stats.size()][alpha.length][beta.length][gamma.length];

		for (int t= 0; t < trial; t++ ) {
			Instance instance= InstanceFactory.randInstance(size);
			for (int a= 0; a < alpha.length; a++ ) {
				for (int b= 0; b < beta.length; b++ ) {
					for (int g= 0; g < gamma.length; g++ ) {
						AssignmentIPSolver solver= new AssignmentIPSolver(obj, tau, alpha[a], beta[b], gamma[g]);
						AssignmentIPSolver maxSolver= new AssignmentIPSolver("Mean_Satisfaction");
						Assignment assignment= solver.solve(instance);
						for (Statistic<Assignment> stat : stats) {
							result[stats.indexOf(stat)][a][b][g]+= stat.getStat(assignment);
						}
						System.out.println(incrementSim());
					}
				}
			}
		}

		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				for (int k= 0; k < result[i][j].length; k++ ) {
					for (int l= 0; l < result[i][j][k].length; l++ ) {
						result[i][j][k][l]/= trial;
					}
				}
			}
		}

		try {
			for (Statistic<Assignment> stat : stats) {
				File file= new File(dir.toString() + "/" + name + "_" + stat.toString() + ".csv");

				FileWriter writer= new FileWriter(file);

				for (int g= 0; g < gamma.length; g++ ) {

					writer.write("gamma: " + gamma[g]);
					for (double b : beta) {
						writer.write("," + b);
					}
					writer.write("\n");

					for (int a= 0; a < alpha.length; a++ ) {
						writer.write(alpha[a] + "");
						for (int b= 0; b < beta.length; b++ ) {
							writer.write("," + result[stats.lastIndexOf(stat)][a][b][g]);
						}
						writer.write("\n");
					}
				}

				writer.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		reset();

	}

}
