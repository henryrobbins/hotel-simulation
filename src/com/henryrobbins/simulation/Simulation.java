package com.henryrobbins.simulation;

import java.io.File;

import javax.swing.JProgressBar;

/** The Simulation class is a Thread which runs a simulation */
public abstract class Simulation extends Thread {

	/** The directory where the resulting file will be placed */
	protected File dir;
	/** The name of the file to be created */
	protected String name;
	/** A progress bar to track progress of the simulation (possibly null) */
	protected JProgressBar progress;
	/** The total number of individual simulations that have been run at a given time */
	protected double simRun= 0;
	/** The number of individual simulations to be run every time run() is called */
	protected double simTotal;

	/** Construct a simulation which will output a file of the given name in the given <br>
	 * directory and maintain simulation progress through the given progress bar */
	public Simulation(File dir, String name, JProgressBar progress) {
		if (dir == null) throw new IllegalArgumentException("The directory was null");
		if (name == null) throw new IllegalArgumentException("The name was null");
		if (name.length() < 1) throw new IllegalArgumentException("The name was less than one character");
		this.dir= dir;
		this.name= name;
		this.progress= progress;
	}

	/** Increment the number of individual simulations run by 1 */
	protected String incrementSim() {
		simRun++ ;
		double currentProg= simRun * 100 / simTotal;
		if (progress != null) { progress.setValue((int) currentProg); }
		return (int) currentProg + "% complete.";
	}

	/** Reset the simulation */
	protected void reset() {
		simRun= 0;
		if (progress != null) { progress.setValue(0); }
	}

}
