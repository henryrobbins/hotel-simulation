import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {

		Test2(300, 50, "/Users/Henry/Downloads");

//		int[] roomNumbers= { 10, 100, 200, 300 };
//		Test1(roomNumbers, 3, "/Users/Henry/Downloads");

//		Simulation sim= new Simulation(25, "exp");
//
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
//		sim.maximizeMinimumPreferences();
//		sim.reset();
//		sim.minimizeUpgrades();

//		for (int i= 0; i < 50; i++ ) {
//			Simulation sim= new Simulation(300, "exp");
//			System.out.println(sim.assignLinearly());
//		}

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

}
