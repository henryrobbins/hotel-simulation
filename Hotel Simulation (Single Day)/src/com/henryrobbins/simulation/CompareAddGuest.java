package com.henryrobbins.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JProgressBar;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.PreserveEdgesMeanSat;

/** Generates a CSV file with the optimal average satisfaction before and after adding an <br>
 * additional guest to the instance. Two proven upper bounds for this gap are maintained: <br>
 * (k / n + 1) where k is the number of room types and (e - 1 / n + 1) where e is the <br>
 * number of assignments that must be changed (due to room type constraints). Lastly, in <br>
 * addition to the optimal satisfaction after a guest is added, the optimal satisfaction <br>
 * subject to the minimum number of assignment changes is also recorded. <br>
 *
 * <pre>
 *           | Opt. Sat. Before | (k/n+1) | (e-1/n+1) | Opt. Sat. After | Opt. Sat. s.t. e
 * ----------------------------------------------------------------------------------------
 *   Size 1  |        -         |    -    |     -     |        -        |        -
 *   Size 2  |        -         |    -    |     -     |        -        |        -
 *
 * </pre>
 */
public class CompareAddGuest extends Simulation {

	/** The number of trials */
	private int trials;
	/** The set of room sizes */
	private int[] sizes;

	public CompareAddGuest(int t, int[] s, File dir, String name, JProgressBar progress) {
		super(dir, name, progress);
		trials= t;
		sizes= s;
		simTotal= 3 * t * s.length;
	}

	@Override
	public void run() {

		double[][] result= new double[sizes.length][5];

		for (int k= 0; k < sizes.length; k++ ) {
			int n= sizes[k];
			for (int t= 0; t < trials; t++ ) {
				Instance before= null;
				Instance after= null;
				while (after == null) {
					before= InstanceFactory.createRandom("compareAddGuestBefore", n);
					after= InstanceFactory.addGuestTo(before, "compareAddGuestAfter");
				}

				AssignmentIPSolver solver= new AssignmentIPSolver("Mean_Satisfaction");

				Assignment assignment= solver.solve(before);
				System.out.println(incrementSim());
				Assignment newAssignment= solver.solve(after);
				System.out.println(incrementSim());

				PreserveEdgesMeanSat preserveSolver= new PreserveEdgesMeanSat(before, assignment);

				Assignment newPreservedAssignment= preserveSolver.solve(after);
				int e= preserveSolver.preserveEdges(before);

				double kBound= (double) before.typeSize() / (double) (n + 1);
				double eBound= (double) (e + 1) / (n + 1);
				double oldMean= assignment.satisfactionStats().getMean();
				double preserved= newPreservedAssignment.satisfactionStats().getMean();
				double newMean= newAssignment.satisfactionStats().getMean();

				result[k][0]+= oldMean;
				result[k][1]+= kBound;
				result[k][2]+= eBound;
				result[k][3]+= newMean;
				result[k][4]+= preserved;
			}
		}

		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				result[i][j]/= trials;
			}
		}

		File file= new File(dir.toString() + "/" + name + ".csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			writer.write(",Opt. Sat. Before,(k/n+1),(e-1/n+1),Opt. Sat. After,Opt. Sat. s.t. e\n");

			for (int i= 0; i < result.length; i++ ) {
				writer.write(sizes[i] + "");
				writer.write("," + result[i][0]);
				writer.write("," + result[i][1]);
				writer.write("," + result[i][2]);
				writer.write("," + result[i][3]);
				writer.write("," + result[i][4]);
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
