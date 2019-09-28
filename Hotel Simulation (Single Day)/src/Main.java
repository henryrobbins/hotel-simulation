import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {

//		int[] roomNumbers= { 10, 100, 200, 300 };
//		Test1(roomNumbers, 3, "/Users/Henry/Downloads");

//		Test2(300, 50, "/Users/Henry/Downloads");

//		Test3(300, 50, "/Users/Henry/Downloads");

//		Test5(75, 50, "/Users/Henry/Downloads");

		// 31 95 89 66

//		Simulation sim= new Simulation("timeTest95");
//		System.out.println(sim.maximizeSatisfaction());

//		double[][] t= new double[100][2];
//
//		for (int i= 0; i < 100; i++ ) {
//			Simulation sim= new Simulation("timeTest" + i);
//			t[i][0]= sim.maximizeSatisfaction();
//			t[i][1]= sim.getNumGuests();
//		}
//
//		for (int i= 0; i < 100; i++ ) {
//			System.out.println(t[i][1] + ": " + t[i][0] + " " + i);
//		}

//		int[] roomNumbers= { 10 };
//		Test7(roomNumbers, 100, "/Users/Henry/Downloads");

		double[][] time= new double[2][20];

		for (int i= 0; i < 20; i++ ) {
			Simulation sim= new Simulation(50, "qtest" + i);
			time[0][i]= sim.maxAvgSatisfactionSTMin();
			sim.reset();
			time[1][i]= sim.maximizeSatisfaction();
		}

		for (int i= 0; i < 20; i++ ) {
			System.out.print(time[0][i] + " " + time[1][i]);
			System.out.println();
		}

//		Simulation sim= new Simulation(100, "exp");

//		sim.assignLinearly();
//		sim.reset();
//		sim.assignLexicographically();
//		sim.reset();
//		sim.maximizeMetPreferences();
//		sim.reset();
//		sim.maximizeAverageSatisfaction();
//		sim.reset();
//		sim.maximizeMinimumSatisfaction();
//		sim.reset();
//		sim.maxAvgSatisfactionSTMin();
//		sim.reset();
//		sim.minUpgradesSTAvgAndMin(0, 1);

//		TODO: create a general Test generating csv class

	}

	private static void Test1(int[] roomNumbers, int trials, String directory) {

		File runTimes= new File(directory + "/runTimes.csv");
		File objective= new File(directory + "/objectives.csv");
		FileWriter timeWriter= null;
		FileWriter objetiveWriter= null;
		try {

			timeWriter= new FileWriter(runTimes);
			objetiveWriter= new FileWriter(objective);

			timeWriter.write(",Linear,Lexicographic,IP");
			timeWriter.write("\n");
			objetiveWriter.write(",Linear,Lexicographic,IP");
			objetiveWriter.write("\n");

			for (int roomNumber : roomNumbers) {

				double linearRunAvg= 0;
				double lexicographicRunAvg= 0;
				double IPRunAvg= 0;

				double linearObjetiveAvg= 0;
				double lexicographicObjetiveAvg= 0;
				double IPObjetiveAvg= 0;

				for (int i= 0; i < trials; i++ ) {
					Simulation sim= new Simulation(roomNumber, "size" + roomNumber + "test" + (i + 1));
					linearRunAvg+= sim.assignLinearly();
					linearObjetiveAvg+= sim.getMetPreferences();
					sim.reset();
					lexicographicRunAvg+= sim.assignLexicographically();
					lexicographicObjetiveAvg+= sim.getMetPreferences();
					sim.reset();
					IPRunAvg+= sim.maximizeMetPreferences();
					IPObjetiveAvg+= sim.getMetPreferences();
				}

				linearRunAvg/= trials;
				lexicographicRunAvg/= trials;
				IPRunAvg/= trials;

				linearObjetiveAvg/= trials;
				lexicographicObjetiveAvg/= trials;
				IPObjetiveAvg/= trials;

				timeWriter.write(roomNumber + "," + linearRunAvg + "," + lexicographicRunAvg + "," + IPRunAvg);
				timeWriter.write("\n");
				objetiveWriter
					.write(roomNumber + "," + linearObjetiveAvg + "," + lexicographicObjetiveAvg + "," + IPObjetiveAvg);
				objetiveWriter.write("\n");

			}

			timeWriter.close();
			objetiveWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void Test2(int n, int trials, String directory) {

		double[][] averages= new double[5][7];

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			writer.write(",Linear,Lexicographic,Max Met Preferences,Max Min Preferences,Max Average Satisfaction," +
				"Max Min Satisfaction,Min Upgrades");

			for (int i= 0; i < trials; i++ ) {
				Simulation sim= new Simulation(n, "exp");
				sim.assignLinearly();
				updateAverages(0, sim, averages);
				sim.reset();
				sim.assignLexicographically();
				updateAverages(1, sim, averages);
				sim.reset();
				sim.maximizeMetPreferences();
				updateAverages(2, sim, averages);
				sim.reset();
				sim.maximizeMinimumPreferences();
				updateAverages(3, sim, averages);
				sim.reset();
				sim.maximizeAverageSatisfaction();
				updateAverages(4, sim, averages);
				sim.reset();
				sim.maximizeMinimumSatisfaction();
				updateAverages(5, sim, averages);
				sim.reset();
				sim.minimizeUpgrades();
				updateAverages(6, sim, averages);
			}

			for (int i= 0; i < 5; i++ ) {
				for (int j= 0; j < 7; j++ ) {
					averages[i][j]/= trials;
				}
			}

			writer.write("\n Met Preferences,");
			for (int j= 0; j < 7; j++ ) {
				writer.write(averages[0][j] + ",");
			}
			writer.write("\n Minimum Preferences,");
			for (int j= 0; j < 7; j++ ) {
				writer.write(averages[1][j] + ",");
			}
			writer.write("\n Average Satisfaction,");
			for (int j= 0; j < 7; j++ ) {
				writer.write(averages[2][j] + ",");
			}
			writer.write("\n Minimum Satisfaction,");
			for (int j= 0; j < 7; j++ ) {
				writer.write(averages[3][j] + ",");
			}
			writer.write("\n Total Upgrades,");
			for (int j= 0; j < 7; j++ ) {
				writer.write(averages[4][j] + ",");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void updateAverages(int j, Simulation sim, double[][] averages) {

		averages[0][j]+= sim.getMetPreferences();
		averages[1][j]+= sim.getMinimumPreferences();
		averages[2][j]+= sim.getAverageSatisfaction();
		averages[3][j]+= sim.getMinimumSatisfaction();
		averages[4][j]+= sim.getTotalUpgrades();

	}

	private static void Test3(int n, int trials, String directory) {

		double[][] averages= new double[5][3];

		File file= new File(directory + "/Output.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			writer.write(",Max Average Satisfaction,Max Minimum Satisfaction,Two Round Solver");

			for (int i= 0; i < trials; i++ ) {
				Simulation sim= new Simulation(n, "exp");
				sim.maximizeAverageSatisfaction();
				averages[0][0]+= sim.getMetPreferences();
				averages[1][0]+= sim.getMinimumPreferences();
				averages[2][0]+= sim.getAverageSatisfaction();
				averages[3][0]+= sim.getMinimumSatisfaction();
				averages[4][0]+= sim.getTotalUpgrades();
				sim.reset();
				sim.maximizeMinimumSatisfaction();
				averages[0][1]+= sim.getMetPreferences();
				averages[1][1]+= sim.getMinimumPreferences();
				averages[2][1]+= sim.getAverageSatisfaction();
				averages[3][1]+= sim.getMinimumSatisfaction();
				averages[4][1]+= sim.getTotalUpgrades();
				sim.reset();
				sim.maximizeSatisfaction();
				averages[0][2]+= sim.getMetPreferences();
				averages[1][2]+= sim.getMinimumPreferences();
				averages[2][2]+= sim.getAverageSatisfaction();
				averages[3][2]+= sim.getMinimumSatisfaction();
				averages[4][2]+= sim.getTotalUpgrades();
				sim.reset();
			}

			for (int i= 0; i < 5; i++ ) {
				for (int j= 0; j < 3; j++ ) {
					averages[i][j]/= trials;
				}
			}

			writer.write("\n Met Preferences,");
			for (int j= 0; j < 3; j++ ) {
				writer.write(averages[0][j] + ",");
			}
			writer.write("\n Minimum Preferences,");
			for (int j= 0; j < 3; j++ ) {
				writer.write(averages[1][j] + ",");
			}
			writer.write("\n Average Satisfaction,");
			for (int j= 0; j < 3; j++ ) {
				writer.write(averages[2][j] + ",");
			}
			writer.write("\n Minimum Satisfaction,");
			for (int j= 0; j < 3; j++ ) {
				writer.write(averages[3][j] + ",");
			}
			writer.write("\n Total Upgrades,");
			for (int j= 0; j < 3; j++ ) {
				writer.write(averages[4][j] + ",");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void Test4(int n, int trials, String directory) {

		double start= 1.00;
		double end= 0.80;
		double jump= 0.02;
		int num= 11;

		double[][][] averages= new double[num][num][5];

		for (int t= 0; t < trials; t++ ) {
			Simulation sim= new Simulation(n, "exp");
			for (double alpha= start; alpha >= end - 0.009; alpha= alpha - jump) {
				for (double beta= start; beta >= end - 0.009; beta= beta - jump) {
					int i= (int) ((start - alpha) / jump);
					int j= (int) ((start - beta) / jump);
					sim.minUpgradesSTAvgAndMin(alpha, beta);
					averages[i][j][0]+= sim.getMetPreferences();
					averages[i][j][1]+= sim.getMinimumPreferences();
					averages[i][j][2]+= sim.getAverageSatisfaction();
					averages[i][j][3]+= sim.getMinimumSatisfaction();
					averages[i][j][4]+= sim.getTotalUpgrades();
					sim.reset();
				}
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

	private static void Test5(int n, int trials, String directory) {

		double start= 1.00;
		double end= 0.90;
		double jump= 0.01;
		int num= 26;

		double[][] averages= new double[num][5];

		for (int t= 0; t < trials; t++ ) {
			Simulation sim= new Simulation(n, "exp");
			double min= 0;
			while (min == 0) {
				sim= new Simulation(n, "exp");
				sim.minimizeUpgrades();
				min= sim.getTotalUpgrades();
				sim.reset();
			}
			for (double alpha= start; alpha >= end - 0.009; alpha= alpha - jump) {
				int i= (int) ((start - alpha) / jump);
				sim.minUpgradesSTAvgAndMin(alpha, 1);
				averages[i][0]+= sim.getMetPreferences();
				averages[i][1]+= sim.getMinimumPreferences();
				averages[i][2]+= sim.getAverageSatisfaction();
				averages[i][3]+= sim.getMinimumSatisfaction();
				averages[i][4]+= sim.getTotalUpgrades() / min;
				sim.reset();
			}
		}

		for (int a= 0; a < num; a++ ) {
			for (int s= 0; s < 5; s++ ) {
				averages[a][s]/= trials;
			}
		}

		File file= new File(directory + "/Test5.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			writer.write("Met Pref");

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				writer.write(averages[alpha][0] + ",");
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Min Pref");

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				writer.write(averages[alpha][1] + ",");
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Avg Sat");

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				writer.write(averages[alpha][2] + ",");
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Min Sat");

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				writer.write(averages[alpha][3] + ",");
				writer.write("\n");
			}

			writer.write("\n");

			writer.write("Min Upgrades");

			writer.write("\n");

			for (int alpha= 0; alpha < num; alpha++ ) {
				writer.write(start - jump * alpha + ",");
				writer.write(averages[alpha][4] + ",");
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void Test6(int n, int trials, String directory) {

		double start= 1.00;
		double end= 0.75;
		double jump= 0.01;
		int num= 26;

		double[][][] averages= new double[num][num][5];

		for (int t= 0; t < trials; t++ ) {
			Simulation sim= new Simulation(n, "exp");
			sim.minimizeUpgrades();
			double min= sim.getTotalUpgrades();
			sim.reset();
			for (double alpha= start; alpha >= end - 0.009; alpha= alpha - jump) {
				for (double beta= start; beta >= end - 0.009; beta= beta - jump) {
					int i= (int) ((start - alpha) / jump);
					int j= (int) ((start - beta) / jump);
					sim.minUpgradesSTAvgAndMin(alpha, beta);
					averages[i][j][0]+= sim.getMetPreferences();
					averages[i][j][1]+= sim.getMinimumPreferences();
					averages[i][j][2]+= sim.getAverageSatisfaction();
					averages[i][j][3]+= sim.getMinimumSatisfaction();
					averages[i][j][4]+= min / sim.getTotalUpgrades();
					sim.reset();
				}
			}
		}

		for (int a= 0; a < num; a++ ) {
			for (int b= 0; b < num; b++ ) {
				for (int s= 0; s < 5; s++ ) {
					averages[a][b][s]/= trials;
				}
			}
		}

		File file= new File(directory + "/Test6.csv");
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

	private static void Test7(int[] n, int trials, String directory) {

		double[][] runTimes= new double[n.length][trials];

		for (int s= 0; s < n.length; s++ ) {
			for (int i= 0; i < trials; i++ ) {
				Simulation sim= new Simulation(n[s], "timeTest" + i);
				runTimes[s][i]= sim.maximizeSatisfaction();
				sim.reset();
			}
		}

		File file= new File(directory + "/Test7.csv");
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);

			for (int num : n) {
				writer.write(num + ",");
			}

			writer.write("\n");

			for (int i= 0; i < trials; i++ ) {
				for (int s= 0; s < n.length; s++ ) {
					writer.write(runTimes[s][i] + ",");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
