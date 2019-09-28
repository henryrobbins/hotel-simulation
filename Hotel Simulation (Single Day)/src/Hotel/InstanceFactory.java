package Hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

/** A Factory containing static methods to create Instances */
public class InstanceFactory {

	/** Creates an Instance from a folder called (instanceName) in Simulation folder which <br>
	 * maintains two CSV files for rooms and guests respectivly */
	public static Instance createInstance(String instanceName) {

		Instance.Builder builder= new Instance.Builder(instanceName);

		readRoomCSV(Paths.get("AMPL", "Simulations", instanceName, "rooms.csv"), builder);
		readGuestCSV(Paths.get("AMPL", "Simulations", instanceName, "guests.csv"), builder);

		if (!builder.canAccommodate()) {
			throw new IllegalArgumentException("CSV File Error: rooms can't accomodate guests");
		}

		builder.convertToCSV();

		return builder.build();
	}

	/** Creates a random Instance with n rooms. The number of guests, room and room request type, <br>
	 * and attributes and preferences are chosen randomly based on implemented distributions */
	public static Instance createRandomInstance(int n, String instanceName) {

		Instance.Builder builder= new Instance.Builder(instanceName);

		int[] roomTypes= new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		// VERSION 1 DISTRIBTUIONS
		// double[] roomProbabilities= new double[] { 0.25, 0.25, 0.15, 0.15, 0.10, 0.09, 0.01 };
		// double[] requestProbabilities= new double[] { 0.28, 0.27, 0.18, 0.17, 0.05, 0.04, 0.01 };

		double[] roomProbabilities= new double[] { 0.43, 0.255, 0.14, 0.055, 0.05, 0.045, 0.02, 0.005 };
		double[] requestProbabilities= new double[] { 0.58, 0.185, 0.125, 0.04, 0.025, 0.025, 0.015, 0.005 };

		EnumeratedIntegerDistribution roomDistribution= new EnumeratedIntegerDistribution(roomTypes,
			roomProbabilities);

		EnumeratedIntegerDistribution requestDistribution= new EnumeratedIntegerDistribution(roomTypes,
			requestProbabilities);

		boolean feasible= false;

		while (!feasible) {

			builder= new Instance.Builder(instanceName);

			for (int i= 1; i <= n; i++ ) {
				builder.addRoom(new Room(i, roomDistribution.sample(1)[0], getRandAttributes()));
			}

			for (int i= 1; i <= getBinomial(n, 0.9); i++ ) {
				builder.addGuest(new Guest(i, i, requestDistribution.sample(1)[0], getRandAttributes()));
			}

			feasible= builder.canAccommodate();
		}

		try {
			Files.createDirectories(Paths.get("AMPL", "Simulations", instanceName));
		} catch (IOException e) {
			e.printStackTrace();
		}

		builder.convertToCSV();

		return builder.build();

	}

	/** Generate a random number from a binomial distribution ~B(n,p) */
	private static int getBinomial(int n, double p) {
		int x= 0;
		for (int i= 0; i < n; i++ ) {
			if (Math.random() < p)
				x++ ;
		}
		return x;
	}

	/** Generate random list of attributes / preferences */
	private static HashSet<String> getRandAttributes() {

		HashSet<String> set= new HashSet<>();

		String[] attributes= { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };
		double[] probabilities= { 0.4, 0.4, 0.4, 0.4, 0.3, 0.3, 0.3, 0.2, 0.2, 0.1 };

		for (int i= 0; i < 10; i++ ) {
			if (Math.random() < probabilities[i]) {
				set.add(attributes[i]);
			}
		}

		return set;
	}

	// CSV FILE READER METHODS

	/** Updates the list of Rooms for the simulation to those in the associated <br>
	 * CSV file where the CSV file is of the following format: <br>
	 * unique room number, room type, attributes <br>
	 * EX: 1, 2, A:B:C <br>
	 * Precondition: Every room number in the CSV file is unique */
	private static void readRoomCSV(Path path, Instance.Builder builder) {

		HashSet<Integer> roomNumbers= new HashSet<>();

		try {
			BufferedReader br= Files.newBufferedReader(path);

			// Essentially skips first line (HEADER)
			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				// Convert each row to an array of Strings
				String[] values= contentLine.split(",");

				// Get room number
				int num= Integer.parseInt(values[0]);

				// Get room type
				int type= Integer.parseInt(values[1]);

				// Get room attributes
				HashSet<String> attributes= new HashSet<>();
				if (values.length > 2) {
					String[] attr= values[2].split(":");
					for (int i= 0; i < attr.length; i++ ) {
						attributes.add(attr[i]);
					}
				}

				// Checks precondition and updates set of room numbers
				if (!roomNumbers.contains(num)) {
					roomNumbers.add(num);
				} else {
					throw new IllegalArgumentException("CSV File Error: repeat room number");
				}

				// Add guest to the list of guests
				builder.addRoom(new Room(num, type, attributes));
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/** Updates the list of Guests for the simulation to those in the associated <br>
	 * CSV file where the CSV file is of the following format: <br>
	 * unique ID, unique arrival position, requested type, preferences <br>
	 * EX: 1,1,1,A:B:C <br>
	 * Precondition: Every guestId and arrival position in the CSV file is unique */
	private static void readGuestCSV(Path path, Instance.Builder builder) {

		HashSet<Integer> guestIDs= new HashSet<>();
		HashSet<Integer> arrivalPositions= new HashSet<>();

		try {
			BufferedReader br= Files.newBufferedReader(path);

			// Essentially skips first line (HEADER)
			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				// Convert each row to an array of Strings
				String[] values= contentLine.split(",");

				// Get guest ID
				int id= Integer.parseInt(values[0]);

				// Get guest arrival position
				int position= Integer.parseInt(values[1]);

				// Get requested room type
				int type= Integer.parseInt(values[2]);

				// Get guest preferences
				HashSet<String> preferences= new HashSet<>();
				if (values.length > 3) {
					String[] prefs= values[3].split(":");
					for (int i= 0; i < prefs.length; i++ ) {
						preferences.add(prefs[i]);
					}
				}

				// Checks precondition and updates sets of Id's
				if (!guestIDs.contains(id)) {
					guestIDs.add(id);
				} else {
					throw new IllegalArgumentException("CSV File Error: repeat guest ID");
				}

				// Checks precondition and updates sets of arrival positions
				if (!arrivalPositions.contains(position)) {
					arrivalPositions.add(position);
				} else {
					throw new IllegalArgumentException("CSV File Error: repeat guest arrival position");
				}

				// Add guest to the list of guests
				builder.addGuest(new Guest(id, position, type, preferences));
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}