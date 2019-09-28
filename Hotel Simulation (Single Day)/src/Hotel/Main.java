package Hotel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;

import HousekeepingSolvers.ScheduleSolver;
import RoomAssignmentSolvers.AssignmentIPSolver;
import RoomAssignmentSolvers.AssignmentSolver;
import RoomAssignmentSolvers.Lexicographic;
import RoomAssignmentSolvers.Linear;
import RoomAssignmentSolvers.MaxMeanSatSTMinIP;
import RoomAssignmentSolvers.MinUpgradesSTSatIP;
import RoomAssignmentSolvers.PreserveEdgesMeanSat;
import Solvers.Solver;

public class Main {

	public static void main(String[] args) {

//		ROOM ASSIGNMENT SOLVERS
//
//		AssignLinearly linear= new AssignLinearly();
//		AssignLexicographically lexico= new AssignLexicographically();
//		AssignFirstAvailableRoom first= new AssignFirstAvailableRoom();
//		IPSolver maxPref= new IPSolver("maxMetPrefs");
//		MaxAvgSatisfactionSTMin maxSatSTMin= new MaxAvgSatisfactionSTMin();
//		minUpgradesSTAvgAndMin minUpgrades= new minUpgradesSTAvgAndMin(1, 1);
//		OnlineSTAverage online= new OnlineSTAverage();
//		suggestiveSTAverage suggestive= new suggestiveSTAverage(1);
//
//		compareRunTimes(50, new int[] { 10, 20, 30, 40, 50 }, new Solver[] { linear, lexico, maxPref, maxSatSTMin },
//			"/Users/Henry/Downloads");
//
//		Instance test= InstanceFactory.createInstance("test3");
//		RoomAssignment assignment1= linear.solve(test);
//		RoomAssignment assignment2= lexico.solve(test);
//		RoomAssignment assignment3= first.solve(test);
//		RoomAssignment assignment4= maxPref.solve(test);
//		RoomAssignment assignment5= maxSatSTMin.solve(test);
//		RoomAssignment assignment6= minUpgrades.solve(test);
//		RoomAssignment assignment7= online.solve(test);
//		RoomAssignment assignment8= suggestive.solve(test);
//
//		assignment1.printStats();
//		assignment2.printStats();
//		assignment3.printStats();
//		assignment4.printStats();
//		assignment5.printStats();
//		assignment6.printStats();
//		assignment7.printStats();
//		assignment8.printStats();

//		HOUSEKEEPING SCHEDULE SOLVERS
//
//		AssignFirstAvailable firstAvailable= new AssignFirstAvailable();
//		Instance instance= InstanceFactory.createRandomInstance(10, "rand");
//		Instance instance= InstanceFactory.createInstance("test3");
//		HousekeepingSchedule schedule= firstAvailable.solve(instance, 3);
//		System.out.println(schedule);
//		System.out.println(schedule.getVisual());
//
//		AssignFirstAvailableRoom first= new AssignFirstAvailableRoom();
//		System.out.println(first.solve(instance, schedule));
//		System.out.println(first.solve(instance, schedule).getTotalOverlap());

//		TESTING

//		Instance instance= InstanceFactory.createInstance("test6", 2);
//		Instance instance= InstanceFactory.createRandomInstance(100, "rand");
//		AssignFirstAvailable firstAvailable= new AssignFirstAvailable();
//		CleanNeededRooms needed= new CleanNeededRooms();
//		HousekeepingIPSolver minSum= new HousekeepingIPSolver("minAvailabilitySum");
//		CleanNeededRoomsNoStop neededNoStop= new CleanNeededRoomsNoStop();
//		RoomsAvailableFeasibility roomsAvailable= new RoomsAvailableFeasibility();
//		HousekeepingSchedule schedule1= roomsAvailable.solve(instance);
//		HousekeepingSchedule schedule2= neededNoStop.solve(instance);
//		System.out.println(instance);
//		System.out.println(schedule1);
//		schedule1.printStats();
//		System.out.println(schedule1.getVisual());
//		System.out.println(schedule2);
//		schedule2.printStats();
//		System.out.println(schedule2.getVisual());

//		AssignFirstAvailableRoom firstRoom= new AssignFirstAvailableRoom();
//		Solution solution= firstRoom.solve(instance, firstAvailable);
//		RoomAssignment assignment2= firstRoom.solve(instance, firstAvailable);
//		System.out.println(assignment1);
//		assignment1.printStats();
//		System.out.println(assignment2);
//		assignment2.printStats();

//		FirstAvailable firstAvailable= new FirstAvailable();
//		FirstRoom firstRoom= new FirstRoom();
//		Solution solution= firstRoom.solve(instance, firstAvailable);
//		System.out.println(instance);
//		System.out.println(solution.getSchedule());
//		System.out.println(solution.getAssignment());

//		AssignFirstAvailableRoom firstAvailable= new AssignFirstAvailableRoom();
//		AssignFirstAvailable first= new AssignFirstAvailable();
//		CleanNeededRooms needed= new CleanNeededRooms();
//		CleanNeededRoomsNoStop neededNoStop= new CleanNeededRoomsNoStop();
//		HousekeepingIPSolver minSum= new HousekeepingIPSolver("minAvailabilitySum");
//		HousekeepingIPSolver minMakespan= new HousekeepingIPSolver("minMakespan");
//		HousekeepingSolver[] housekeepingSolvers= { first, needed, neededNoStop, minSum, minMakespan };

//		compareHousekeeping(1, new int[] { 150 }, firstAvailable, housekeepingSolvers,
//			"/Users/Henry/Downloads");

//		Instance before= InstanceFactory.createRandomInstance(5, "compareAddGuest");
//		AssignmentIPSolver solver= new AssignmentIPSolver("maxMeanSat");
//		RoomAssignment assignment= solver.solve(before);
//		Instance after= InstanceFactory.addGuestTo(before, "compareAddGuest2");
//		int e= preserveEdges(assignment, after);
//		System.out.println(before);
//		System.out.println(assignment);
//		System.out.println(after);
//		System.out.println(e);

//		compareAddGuest(25, new int[] { 25, 50, 75, 100, 125, 150, 200, 250, 300 },
//			"/Users/Henry/Downloads");

//		HousekeepingIPSolver minSumAvail= new HousekeepingIPSolver("minAvailabilityTI");
//		HousekeepingIPSolver minMakespan= new HousekeepingIPSolver("minMakespanTI");
//		FirstAvailable firstAvailable= new FirstAvailable();
//		NeededFirst neededFirst= new NeededFirst();
//		NeededFirstNoWait neededFirstNoWait= new NeededFirstNoWait();
//		HousekeepingSolver[] housekeepingSolvers= { firstAvailable, neededFirst, neededFirstNoWait };
//

//		Solver[] solvers= new Solver[15];
//		int s= 0;
//		for (HousekeepingSolver hSolver : housekeepingSolvers) {
//			for (AssignmentSolver aSolver : assignmentSolvers) {
//				solvers[s]= new CombinationSolver(hSolver, aSolver);
//				s++ ;
//			}
//			solvers[s]= new minOverlapIP(hSolver);
//			s++ ;
//		}
//
//		SolutionIPSolver minOverlapIP= new SolutionIPSolver("minOverlap");
//		SolutionIPSolver minOverlapMaxSatIP= new SolutionIPSolver("minOverlapMaxSat");
//
//		FirstAvailable firstAvailable= new FirstAvailable();
//		NeededFirst neededFirst= new NeededFirst();
//		NeededFirstNoWait neededFirstNoWait= new NeededFirstNoWait();
//		ScheduleIPSolver minMakespan= new ScheduleIPSolver("Makespan");
//		ScheduleSolver[] housekeepingSolvers= { firstAvailable };

		Linear linear= new Linear();
		Lexicographic lexico= new Lexicographic();
		AssignmentIPSolver maxMeanSat= new AssignmentIPSolver("Mean_Satisfaction");
		AssignmentIPSolver maxMinSat= new AssignmentIPSolver("Min_Satisfaction");
		MaxMeanSatSTMinIP maxSat= new MaxMeanSatSTMinIP();
		ArrayList<AssignmentSolver> assignmentSolvers= new ArrayList<>();
		assignmentSolvers.add(linear);
		assignmentSolvers.add(lexico);
		assignmentSolvers.add(maxMeanSat);
		assignmentSolvers.add(maxMinSat);
		assignmentSolvers.add(maxSat);
//
//
//		ArrayList<Solver> solvers= new ArrayList<>();
//		for (HousekeepingSolver hSolver : housekeepingSolvers) {
//			solvers.add(new minOverlapMeanSatSTScheduleIP(hSolver));
//			solvers.add(new minOverlapScheduleIP(hSolver));
//		}
//		solvers.add(minOverlapIP);
//		solvers.add(minOverlapMaxSatIP);
//		solvers.add(new CombinationSolver(firstAvailable, new AssignmentIPSolver("maxMeanSat")));

//		sensitivityMinUpgrades(10, 150, "/Users/Henry/Downloads");
//		compareRoomAssignment(100, 150, assignmentSolvers, "/Users/Henry/Downloads");
//		compareSolvers(50, new int[] { 25, 50, 75, 100 }, solvers, "/Users/Henry/Downloads");
//		compareRunTimes(1, new int[] { 20 }, solvers, "/Users/Henry/Downloads");
//		compareToOptimal(25, 50, housekeepingSolvers, minMakespan, "/Users/Henry/Downloads");
		compareAddGuest(100, new int[] { 120, 130, 140 }, "/Users/Henry/Downloads");

	}

	// TODO: develop general testing procedures
	// (1) compare run times
	// -- (i) distribution of runtime
	// -- (ii) compare average runtime over n trials
	// (2) compare assignment statistics
	// -- (i) average statistics over n trials
	// -- (ii) adjust parameters (alpha and beta)

	/** Generates a CSV file containing average run times over t trials for every solver <br>
	 * for random instances at every provided size n <br>
	 *
	 * @param t the number of trials */
	public static void compareRunTimes(int t, int[] n, ArrayList<AssignmentSolver> solverList, String directory) {

		Object[] solver= solverList.toArray();
		double[][] result= new double[solver.length][n.length];

		for (int i= 0; i < result.length; i++ ) {
			for (int trial= 0; trial < t; trial++ ) {
				for (int j= 0; j < result[i].length; j++ ) {
					Instance instance= InstanceFactory.createRandomInstance(n[j], "compareRunTimes");
					StopWatch watch= new StopWatch();
					watch.start();
					((AssignmentSolver) solver[i]).solve(instance);
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

			for (int i= 0; i < result[0].length; i++ ) {
				writer.write("," + n[i]);
			}

			writer.write("\n");

			for (int i= 0; i < result.length; i++ ) {
				writer.write(solver[i] + " ");
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

	/** Generates a CSV file with a list of hard-coded statistics for every trial of a solver */
	public static void compareToOptimal(int t, int n, ScheduleSolver[] solver, ScheduleSolver optimal,
		String directory) {

		double[][] result= new double[t][solver.length];

		for (int i= 0; i < t; i++ ) {
			Instance instance= InstanceFactory.createRandomInstance(n, "compare");
			double optMakespan= optimal.solve(instance).getMakespan();
			for (int j= 0; j < result[i].length; j++ ) {
				result[i][j]+= solver[j].solve(instance).getMakespan() - optMakespan;
			}
		}

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			for (int i= 0; i < result[0].length; i++ ) {
				writer.write("," + solver[i]);
			}

			writer.write("\n");

			for (int i= 0; i < result.length; i++ ) {
				writer.write(i + 1 + ",");
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

	/** Generates a CSV file with statistics for different housekeeping assignments and a given <br>
	 * room assignment algorithm used after all scheduling algorithms. <br>
	 * t trials for every solver for random instances size n with n*h housekeepers. (0 < h < 1)
	 *
	 * <pre>
	 * 			Makespan	Sum		Overlap		Avg. Satisfaction
	 * Solver1     -         -         -                -
	 * Solver2     -         -         -                -
	 *
	 * </pre>
	 */
	public static void compareHousekeeping(int t, int[] sizes, AssignmentSolver assignmentSolver,
		ScheduleSolver[] housekeepingSolver, String directory) {

		int numOfSolvers= housekeepingSolver.length;
		double[][][] result= new double[numOfSolvers][4][sizes.length];

		for (int k= 0; k < sizes.length; k++ ) {
			int n= sizes[k];
			for (int trial= 0; trial < t; trial++ ) {
				Instance instance= InstanceFactory.createRandomInstance(n, "compareHousekeeping");
				for (int j= 0; j < numOfSolvers; j++ ) {
					HousekeepingSchedule schedule= housekeepingSolver[j].solve(instance);
					result[j][0][k]+= schedule.getMakespan();
					result[j][1][k]+= schedule.getSumOfAvailabilities();
					RoomAssignment assignment= assignmentSolver.solve(instance);
					result[j][3][k]+= assignment.getAverageSatisfaction();
					Solution solution= new Solution(instance, schedule, assignment);
					result[j][2][k]+= solution.getTotalOverlap();
				}
			}
		}

		// Average
		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				for (int k= 0; k < result[i][j].length; k++ ) {
					result[i][j][k]/= t;
				}
			}
		}

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			for (int k= 0; k < sizes.length; k++ ) {

				writer.write(sizes[k] + ",Makespan,Sum,Overlap,Avg. Satisfaction \n");

				for (int i= 0; i < result.length; i++ ) {
					writer.write(housekeepingSolver[i].toString());
					for (int j= 0; j < result[i].length; j++ ) {
						writer.write("," + result[i][j][k]);
					}
					writer.write("\n");
				}

				writer.write("\n\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Generates a CSV file with statistics for different full solutions using each Solver. <br>
	 * t trials for every solver for random instances size n with n*h housekeepers. (0 < h < 1)
	 *
	 * <pre>
	 * 			Availability Sum   Makespan  Overlap   Mean Sat  Min Sat  Upgrades
	 * Solver1        -               -         -         -         -        -
	 * Solver2        -               -         -         -         -        -
	 *
	 * </pre>
	 */
	public static void compareSolvers(int t, int[] sizes, ArrayList<Solver> solversList, String directory) {

		Object[] solvers= solversList.toArray();
		int numOfSolvers= solvers.length;
		double[][][] result= new double[numOfSolvers][6][sizes.length];

		for (int k= 0; k < sizes.length; k++ ) {
			int n= sizes[k];
			for (int trial= 0; trial < t; trial++ ) {
				Instance instance= InstanceFactory.createRandomInstance(n, "compareHousekeeping");
				for (int j= 0; j < numOfSolvers; j++ ) {
					Solution solution= ((Solver) solvers[j]).solve(instance);
					HousekeepingSchedule schedule= solution.getSchedule();
					RoomAssignment assignment= solution.getAssignment();
					result[j][0][k]+= schedule.getSumOfAvailabilities();
					result[j][1][k]+= schedule.getMakespan();
					result[j][2][k]+= solution.getTotalOverlap();
					result[j][3][k]+= assignment.getAverageSatisfaction();
					result[j][4][k]+= assignment.getMinimumSatisfaction();
					result[j][5][k]+= assignment.getTotalUpgrades();
				}
			}
		}

		// Average
		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				for (int k= 0; k < result[i][j].length; k++ ) {
					result[i][j][k]/= t;
				}
			}
		}

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			for (int k= 0; k < sizes.length; k++ ) {

				writer.write(sizes[k] + ",Availability Sum,Makespan,Overlap,Mean Sat,Min Sat,Upgrades\n");

				for (int i= 0; i < result.length; i++ ) {
					writer.write(solvers[i].toString());
					for (int j= 0; j < result[i].length; j++ ) {
						writer.write("," + result[i][j][k]);
					}
					writer.write("\n");
				}

				writer.write("\n\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Generates a CSV file with statistics for different full solutions using each Solver. <br>
	 * t trials for every solver for random instances size n with n*h housekeepers. (0 < h < 1)
	 *
	 * <pre>
	 * 		     Mean Sat  Min Sat  Upgrades
	 * Solver1      -         -        -
	 * Solver2      -         -        -
	 *
	 * </pre>
	 */
	public static void compareRoomAssignment(int t, int size, ArrayList<AssignmentSolver> solversList,
		String directory) {

		Object[] solvers= solversList.toArray();
		int numOfSolvers= solvers.length;
		double[][] result= new double[numOfSolvers][3];

		for (int trial= 0; trial < t; trial++ ) {
			Instance instance= InstanceFactory.createRandomInstance(size, "compareHousekeeping");
			for (int j= 0; j < numOfSolvers; j++ ) {
				RoomAssignment assignment= ((AssignmentSolver) solvers[j]).solve(instance);
				result[j][0]+= assignment.getAverageSatisfaction();
				result[j][1]+= assignment.getMinimumSatisfaction();
				result[j][2]+= assignment.getTotalUpgrades();
			}
		}

		// Average
		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				result[i][j]/= t;
			}
		}

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			writer.write(size + ",Mean Sat,Min Sat,Upgrades\n");

			for (int i= 0; i < result.length; i++ ) {
				writer.write(solvers[i].toString());
				for (int j= 0; j < result[i].length; j++ ) {
					writer.write("," + result[i][j]);
				}
				writer.write("\n");
			}

			writer.write("\n\n");

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Generates a CSV file with the average satisfaction before and after adding an <br>
	 * additional guest to the instance. The maxMeanSat IP is used to find optimal. <br>
	 * t trials for every solver for random instances size n with n*h housekeepers. (0 < h < 1)
	 *
	 * <pre>
	 * 		            Avg. Satisfaction
	 *                Before   		  After
	 * Size 1           -               -
	 * Size 2           -               -
	 *
	 * </pre>
	 */
	public static void compareAddGuest(int t, int[] sizes, String directory) {

		double[][] result= new double[sizes.length][4];

		for (int k= 0; k < sizes.length; k++ ) {
			int n= sizes[k];
			for (int trial= 0; trial < t; trial++ ) {
				Instance before= null;
				Instance after= null;
				while (after == null) {
					before= InstanceFactory.createRandomInstance(n, "compareAddGuest");
					after= InstanceFactory.addGuestTo(before, "compareAddGuest2");
				}
				AssignmentIPSolver solver= new AssignmentIPSolver("Mean_Satisfaction");
				PreserveEdgesMeanSat preserveSolver= new PreserveEdgesMeanSat();

				RoomAssignment assignment= solver.solve(before);
				RoomAssignment newAssignment= solver.solve(after);
				Object[] values= preserveSolver.solve(assignment, after);
				RoomAssignment newPreservedAssignment= (RoomAssignment) values[0];
				int e= (int) values[1];

				double kBound= (double) before.getTypeSize() / (double) (n + 1);
				double eBound= (double) (e + 1) / (n + 1);
				double oldMean= assignment.getAverageSatisfaction();
				double changeInPreserved= oldMean - newPreservedAssignment.getAverageSatisfaction();
				double change= oldMean - newAssignment.getAverageSatisfaction();

				result[k][0]+= kBound;
				result[k][1]+= eBound;
				result[k][2]+= changeInPreserved;
				result[k][3]+= change;
			}
		}

		// Average
		for (int i= 0; i < result.length; i++ ) {
			for (int j= 0; j < result[i].length; j++ ) {
				result[i][j]/= t;
			}
		}

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			writer.write(",k bound,e bound,minEChange,change\n");

			for (int i= 0; i < result.length; i++ ) {
				writer.write(sizes[i] + "");
				writer.write("," + result[i][0]);
				writer.write("," + result[i][1]);
				writer.write("," + result[i][2]);
				writer.write("," + result[i][3]);
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sensitivityMinUpgrades(int trials, int n, String directory) {

		double start= 1.00;
		double end= 0.8;
		double jump= 0.01;
		int num= 21;

		double[][][] averages= new double[num][num][5];

		for (int t= 0; t < trials; t++ ) {
			Instance instance= InstanceFactory.createRandomInstance(n, "sen");
			AssignmentIPSolver opt= new AssignmentIPSolver("Upgrades");
			RoomAssignment optAssignment= opt.solve(instance);
			averages[0][1][4]+= optAssignment.getTotalUpgrades();
			for (double alpha= start; alpha >= end - 0.009; alpha= alpha - jump) {
				int i= (int) ((start - alpha) / jump);
				System.out.println(alpha);
				MinUpgradesSTSatIP solver= new MinUpgradesSTSatIP(alpha, 1);
				RoomAssignment assignment= solver.solve(instance);
				averages[i][0][0]+= assignment.getMetPreferences();
				averages[i][0][1]+= assignment.getMinimumPreferences();
				averages[i][0][2]+= assignment.getAverageSatisfaction();
				averages[i][0][3]+= assignment.getMinimumSatisfaction();
				averages[i][0][4]+= assignment.getTotalUpgrades();
			}
		}

		for (int a= 0; a < num; a++ ) {
			for (int b= 0; b < num; b++ ) {
				for (int s= 0; s < 5; s++ ) {
					averages[a][b][s]/= trials;
				}
			}
		}

		File file= new File(directory + "/Test4.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			writer.write("Met Pref");
			for (int beta= 0; beta < num; beta++ ) {
				writer.write("," + (start - jump * beta));
			}

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				for (int beta= 0; beta < num; beta++ ) {
					writer.write(averages[alpha][beta][0] + ",");
				}
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Min Pref");
			for (int beta= 0; beta < num; beta++ ) {
				writer.write("," + (start - jump * beta));
			}

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				for (int beta= 0; beta < num; beta++ ) {
					writer.write(averages[alpha][beta][1] + ",");
				}
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Avg Sat");
			for (int beta= 0; beta < num; beta++ ) {
				writer.write("," + (start - jump * beta));
			}

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				for (int beta= 0; beta < num; beta++ ) {
					writer.write(averages[alpha][beta][2] + ",");
				}
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Min Sat");
			for (int beta= 0; beta < num; beta++ ) {
				writer.write("," + (start - jump * beta));
			}

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				for (int beta= 0; beta < num; beta++ ) {
					writer.write(averages[alpha][beta][3] + ",");
				}
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Min Upgrades");
			for (int beta= 0; beta < num; beta++ ) {
				writer.write("," + (start - jump * beta));
			}

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				for (int beta= 0; beta < num; beta++ ) {
					writer.write(averages[alpha][beta][4] + ",");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
