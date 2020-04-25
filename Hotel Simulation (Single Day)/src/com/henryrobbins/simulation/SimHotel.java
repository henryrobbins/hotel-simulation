package com.henryrobbins.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import com.henryrobbins.decision.Decision;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Hotel;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.solver.Solver;

/** Generate random instances on a hotel for a given number of random days */
public class SimHotel<T extends Decision> extends Simulation {

	/** The number of trials */
	private int trials;
	/** The hotel */
	private Hotel hotel;
	/** The set of solvers */
	private ArrayList<Solver<T>> solvers;
	/** The set of statistics */
	private ArrayList<Statistic<T>> stats;

	public SimHotel(int t, Hotel hotel, ArrayList<Solver<T>> solvers, ArrayList<Statistic<T>> stats, File dir,
		String name, JProgressBar progress) {
		super(dir, name, progress);
		trials= t;
		this.hotel= hotel;
		this.solvers= solvers;
		this.stats= stats;
		simTotal= solvers.size() * t;
	}

	@Override
	public void run() {
		double[][] result= new double[solvers.size()][stats.size()];

		for (int t= 0; t < trials; t++ ) {
			Instance instance= InstanceFactory.randInstance(hotel);
			for (Solver<T> solver : solvers) {
				T outcome= solver.solve(instance);
				for (Statistic<T> stat : stats) {
					result[solvers.indexOf(solver)][stats.indexOf(stat)]+= stat.getStat(outcome);
				}
				System.out.println(incrementSim());
			}
		}
		for (Solver<T> solver : solvers) {
			for (Statistic<T> stat : stats) {
				result[solvers.indexOf(solver)][stats.indexOf(stat)]/= trials;
			}
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
