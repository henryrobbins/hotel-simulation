import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import com.ampl.AMPL;
import com.ampl.Environment;

/** An instance maintains a list of guests and rooms for a simulated hotel day */
public class Simulation {

	/** The name of the instance: the .dat file created upon instantiation will <br>
	 * be of the form name.dat and placed in the Simulations folder within AMPL folder */
	private String name;
	/** a list of rooms (must have unique room numbers and fully accommodate guests) */
	private ArrayList<Room> rooms= new ArrayList<>();
	/** a list of guests (must have unique guestIDs and arrival positions) */
	private ArrayList<Guest> guests= new ArrayList<>();
	/** a dictionary of rooms and their assigned guest (null if unassigned) */
	private LinkedHashMap<Room, Guest> assignments= new LinkedHashMap<>();
	/** a set of all used room numbers */
	private HashSet<Integer> roomNumbers= new HashSet<>();
	/** a set of all used guest IDs */
	private HashSet<Integer> guestIDs= new HashSet<>();
	/** a set of all used arrival positions */
	private HashSet<Integer> arrivalPositions= new HashSet<>();

	private AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));

	// STATISTICS FIELDS

	/** the total number of preferences met for the current assignment */
	private int metPreferences= 0;
	/** the minimum number of met preferences of a guest for the current room assignment */
	private int minimumPreferences= 0;
	/** the average satisfaction of guests for the current assignment */
	private double averageSatisfaction= 0.0;
	/** the minimum satisfaction of a guest for the current room assignment */
	private double minimumSatisfaction= 0.0;
	/** the total number of upgrades made in the current assignments */
	private int totalUpgrades= 0;

	/** Constructor: a new simulation with the list of rooms and guests associated <br>
	 * with the corresponding CSV files within the directory name <br>
	 * A new .dat file is also created for use by AMPL with given name <br>
	 * Precondition: the directory contains a rooms.csv file and a guests.csv file. <br>
	 * Rooms have unique room numbers and guests have unique guest IDs <br>
	 * and arrival positions. Enough rooms to satisfy guest requests (with upgrades) */
	public Simulation(String name) {

		this.name= name;

		readRoomCSV(Paths.get("AMPL", "Simulations", name, "rooms.csv"));
		readGuestCSV(Paths.get("AMPL", "Simulations", name, "guests.csv"));

		if (!canAccommodate()) { throw new IllegalArgumentException("CSV File Error: rooms can't accomodate guests"); }

		finishInstantiation();
	}

	/** Constructor: a new simulation with n rooms of randomly generated types and <br>
	 * attributes and a random number of guests near full occupancy with random <br>
	 * arrival position, requested room type, and preferences <br>
	 * A new .dat file is also created for use by AMPL with given name */
	public Simulation(int n, String name) {

		this.name= name;

		int[] roomTypes= new int[] { 1, 2, 3, 4, 5, 6, 7 };
		double[] roomProbabilities= new double[] { 0.25, 0.25, 0.15, 0.15, 0.10, 0.09, 0.01 };
		double[] requestProbabilities= new double[] { 0.28, 0.27, 0.18, 0.17, 0.05, 0.04, 0.01 };

		EnumeratedIntegerDistribution roomDistribution= new EnumeratedIntegerDistribution(roomTypes,
			roomProbabilities);

		EnumeratedIntegerDistribution requestDistribution= new EnumeratedIntegerDistribution(roomTypes,
			requestProbabilities);

		boolean feasible= false;

		while (!feasible) {

			rooms.clear();
			guests.clear();

			for (int i= 1; i <= n; i++ ) {
				rooms.add(new Room(i, roomDistribution.sample(1)[0], getRandAttributes()));
			}

			for (int i= 1; i <= getBinomial(n, 0.9); i++ ) {
				guests.add(new Guest(i, i, requestDistribution.sample(1)[0], getRandAttributes()));
			}

			feasible= canAccommodate();
		}

		try {
			Files.createDirectories(Paths.get("AMPL", "Simulations", name));
		} catch (IOException e) {
			e.printStackTrace();
		}

		finishInstantiation();
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

	/** Returns true iff the current rooms list can accommodate ALL guests in guests list */
	private boolean canAccommodate() {

		Collections.sort(rooms, new SortByRoomTypeDescending());
		Collections.sort(guests, new SortByGuestType());

		for (int i= 0; i < guests.size(); i++ ) {
			if (rooms.get(i).getType() < guests.get(i).getType()) { return false; }
		}
		return true;
	}

	/** This code should be run at the end of ALL constructors */
	private void finishInstantiation() {

		// create name.dat file for use by AMPL
		convert();
		ampl.setOption("solver", "gurobi");
		convertToCSV();
		// make invariant hold true for assignments field
		for (Room room : rooms) {
			assignments.put(room, null);
		}

	}

	// GETTERS FOR STATISTICS

	/** TODO: delete */
	public double getNumGuests() {
		return guests.size();
	}

	/** Returns the total met preferences for the current room assignment */
	public int getMetPreferences() {
		return metPreferences;
	}

	/** Returns the minimum number of preferences met for the current room assignment */
	public int getMinimumPreferences() {
		return minimumPreferences;
	}

	/** Returns the average satisfaction for the current room assignment */
	public double getAverageSatisfaction() {
		return averageSatisfaction;
	}

	/** Returns minimum satisfaction of a guest for the current room assignment */
	public double getMinimumSatisfaction() {
		return minimumSatisfaction;
	}

	/** Returns the total number of upgrades for the current room assignment */
	public int getTotalUpgrades() {
		return totalUpgrades;
	}

	/** Prints a report of statistics for current room assignments */
	public void printStats() {

		System.out.println("STATISTICS");
		System.out.println("Met Preferences: " + metPreferences);
		System.out.println("Minimum Met Preferences: " + minimumPreferences);
		System.out.println("Average Satisfaction: " + averageSatisfaction);
		System.out.println("Minimum Satisfaction: " + minimumSatisfaction);
		System.out.println("Total Upgrades: " + totalUpgrades);
		System.out.println();

	}

	// ROOM ASSIGNMENT ALGORITHMS

	/** Makes a valid room assignment by assigning guests to the lowest room type <br>
	 * possible upon their arrival and returns the elapsed time in milliseconds */
	public double assignLinearly() {
		StopWatch watch= new StopWatch();
		watch.start();
		Collections.sort(guests, new SortByArrivalPosition());
		Collections.sort(rooms, new SortByRoomTypeAscending());
		for (Guest guest : guests) {
			for (Room room : rooms) {
				if (assignments.get(room) == null && room.getType() >= guest.getType()) {
					assign(room, guest);
					break;
				}
			}
		}
		watch.stop();
		System.out.println("Linearly");
		printStats();
		return watch.getNanoTime() / 1000000.0;
	}

	/** Makes a valid room assignment by assigning guests to the room of the minimum <br>
	 * valid room type while fulfilling the most preferences upon guest arrival <br>
	 * Additionally, returns the elapsed time in milliseconds */
	public double assignLexicographically() {
		StopWatch watch= new StopWatch();
		watch.start();
		Collections.sort(guests, new SortByArrivalPosition());
		for (Guest guest : guests) {
			Collections.sort(rooms, new SortBySatisfactionFor(guest));
			int i= guest.getType();
			while (!assignments.containsValue(guest)) {
				for (Room room : rooms) {
					if (assignments.get(room) == null && room.getType() == i) {
						assign(room, guest);
						break;
					}
				}
				i++ ;
			}
		}
		watch.stop();
		System.out.println("Lexicographically");
		printStats();
		return watch.getNanoTime() / 1000000.0;
	}

	/** Updates the current assignment such that the total number of met preferences <br>
	 * met is maximized and returns elapsed run time in milliseconds */
	public double maximizeMetPreferences() {
		double runTime= runIP("maxMetPrefs");
		System.out.println("Max Met Preferences");
		printStats();
		return runTime;
	}

	/** Updates the current assignment such that the minimum number of met preferences <br>
	 * for a guest is maximized and returns elapsed run time in milliseconds */
	public double maximizeMinimumPreferences() {
		double runTime= runIP("maxMinimumPreferences");
		System.out.println("Max Minimum Preferences");
		printStats();
		return runTime;
	}

	/** Updates the current assignment such that the average satisfaction <br>
	 * is maximized and returns elapsed run time in milliseconds */
	public double maximizeAverageSatisfaction() {
		double runTime= runIP("maxAverageSatisfaction");
		System.out.println("Max Average Satisfaction");
		printStats();
		return runTime;
	}

	/** Updates the current assignment such that the minimum satisfaction <br>
	 * for a guest is maximized and returns elapsed run time in milliseconds */
	public double maximizeMinimumSatisfaction() {
		double runTime= runIP("maxMinimumSatisfaction");
		System.out.println("Max Minimum Satisfaction");
		printStats();
		return runTime;
	}

	/** Updates the current assignment such that the number of upgrades made <br>
	 * is minimized and returns elapsed run time in milliseconds */
	public double minimizeUpgrades() {
		double runTime= runIP("minUpgrades");
		System.out.println("Minimize Upgrades");
		printStats();
		return runTime;
	}

	// TODO: method specification
	// TODO: determine cause of time difference
	public double maximizeSatisfaction() {
		double runTime= runIP("maxSatisfaction");
		System.out.println("Max Satisfaction");
		printStats();
		return runTime;
	}

	public double maxAvgSatisfactionSTMin() {

		double runTime= maximizeMinimumSatisfaction();
		double minimum= minimumSatisfaction;
		reset();

		StopWatch watch= new StopWatch();
		watch.start();
		try {
			ampl.read(Paths.get("AMPL", "Models", "maxAvgSatisfactionSubjectToMinimum" + ".mod").toString());
			ampl.readData(Paths.get("AMPL", "Simulations", name, name + ".dat").toString());
		} catch (Exception e) {
			ampl.close();
		}
		ampl.getParameter("minimum").set(minimum);
		ampl.solve();
		assignmentsFromAMPL();
		ampl.reset();
		watch.stop();
		runTime+= watch.getNanoTime() / 1000000.0;

		System.out.println("Max Avg Satisfaction S.T. Min");
		printStats();
		return runTime;
	}

	// TODO: method specification
	public double minUpgradesSTAvgAndMin(double alpha, double beta) {

		double runTime= maxAvgSatisfactionSTMin();
		double minimum= minimumSatisfaction;
		double average= averageSatisfaction;
		reset();

		StopWatch watch= new StopWatch();
		watch.start();
		try {
			ampl.read(Paths.get("AMPL", "Models", "maxUpgradesSubjectToSatisfaction" + ".mod").toString());
			ampl.readData(Paths.get("AMPL", "Simulations", name, name + ".dat").toString());
		} catch (Exception e) {
			ampl.close();
		}
		ampl.getParameter("average").set(average * alpha);
		ampl.getParameter("minimum").set(minimum * beta);
		ampl.solve();
		assignmentsFromAMPL();
		ampl.reset();
		watch.stop();
		runTime+= watch.getNanoTime() / 1000000.0;

		System.out.println("Min Upgrades S.T. Average and Minimum");
		printStats();
		return runTime;
	}

	// TODO: correct method specification
	/** Creates an instance of AMPL and uses it to solve an IP yielding a valid room <br>
	 * assignment from the given model and the .dat file in the directory name <br>
	 * returns elapsed run time in milliseconds */
	private double runIP(String model) {

		StopWatch watch= new StopWatch();

		watch.start();
		try {
			ampl.read(Paths.get("AMPL", "Models", model + ".mod").toString());
			ampl.readData(Paths.get("AMPL", "Simulations", name, name + ".dat").toString());
		} catch (Exception e) {
			ampl.close();
		}
		ampl.solve();
		assignmentsFromAMPL();
		ampl.reset();
		watch.stop();

		return watch.getNanoTime() / 1000000.0;
	}

	// TODO: write method specification
	private void assignmentsFromAMPL() {
		for (Room room : rooms) {
			for (Guest guest : guests) {
				if (ampl.getVariable("assign").get(room.getNumber(), guest.getID()).value() == 1.0) {
					assign(room, guest);
					break;
				}
			}
		}
	}

	/** Assigns the Guest guest to Room room and maintains the class invariant <br>
	 * for all of the statistics fields <br>
	 * Precondition: room has no guest and vice versa */
	private void assign(Room room, Guest guest) {
		assert assignments.get(room) == null;
		assert !assignments.containsValue(guest);

		metPreferences+= guest.getMetPreferences(room);
		averageSatisfaction+= guest.getAverageSatisfaction(room) / guests.size();
		totalUpgrades+= room.getType() - guest.getType();

		if (assignmentEmpty()) {
			minimumSatisfaction= guest.getAverageSatisfaction(room);
			minimumPreferences= guest.getMetPreferences(room);
		} else {
			minimumSatisfaction= Math.min(minimumSatisfaction, guest.getAverageSatisfaction(room));
			minimumPreferences= Math.min(minimumPreferences, guest.getMetPreferences(room));
		}

		assignments.put(room, guest);
	}

	/** Returns true iff there are currently no guests assigned to a room */
	private boolean assignmentEmpty() {
		for (Room room : rooms) {
			if (assignments.get(room) != null) return false;
		}
		return true;
	}

	/** Resets the room assignments */
	public void reset() {
		assignments.clear();
		for (Room room : rooms) {
			assignments.put(room, null);
		}
		metPreferences= 0;
		minimumPreferences= 0;
		averageSatisfaction= 0.0;
		minimumSatisfaction= 0.0;
		totalUpgrades= 0;
	}

	// CSV FILE READER METHODS

	/** Updates the list of Rooms for the simulation to those in the associated <br>
	 * CSV file where the CSV file is of the following format: <br>
	 * unique room number, room type, attributes <br>
	 * EX: 1, 2, A:B:C <br>
	 * Precondition: Every room number in the CSV file is unique */
	private void readRoomCSV(Path path) {

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
				rooms.add(new Room(num, type, attributes));
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
	private void readGuestCSV(Path path) {

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
				guests.add(new Guest(id, position, type, preferences));
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/** Converts the rooms and guests lists to a .dat file called name */
	private void convert() {

		Collections.sort(rooms, new SortByRoomNumber());
		Collections.sort(guests, new SortByGuestID());

		File file= new File(Paths.get("AMPL", "Simulations", name, name + ".dat").toString());
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);
			writer.write("data; \n");
			writer.write("\n");

			writer.write("set GUESTS := ");
			for (Guest guest : guests) {
				writer.write(guest.getID() + " ");
			}
			writer.write(";\n");

			writer.write("set ROOMS := ");
			for (Room room : rooms) {
				writer.write(room.getNumber() + " ");
			}
			writer.write(";\n \n");

			writer.write("param: roomType :=");
			for (Room room : rooms) {
				writer.write("\n" + room.getNumber() + " " + room.getType());
			}
			writer.write(" ;\n \n");

			writer.write("param: guestType :=");
			for (Guest guest : guests) {
				writer.write("\n" + guest.getID() + " " + guest.getType());
			}
			writer.write(" ;\n \n");

			writer.write("param metPreferences: \n  ");
			for (Guest guest : guests) {
				writer.write(guest.getID() + " ");
			}
			writer.write(" :=");
			for (Room room : rooms) {
				writer.write("\n" + room.getNumber() + " ");
				for (Guest guest : guests) {
					writer.write(guest.getMetPreferences(room) + " ");
				}
			}
			writer.write(" ;\n \n");

			writer.write("param satisfaction: \n  ");
			for (Guest guest : guests) {
				writer.write(guest.getID() + " ");
			}
			writer.write(" :=");
			for (Room room : rooms) {
				writer.write("\n" + room.getNumber() + " ");
				for (Guest guest : guests) {
					writer.write(guest.getAverageSatisfaction(room) + " ");
				}
			}
			writer.write(" ;");
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO: Write method specification
	private void convertToCSV() {

		Collections.sort(rooms, new SortByRoomNumber());
		Collections.sort(guests, new SortByGuestID());

		File rooms= new File(Paths.get("AMPL", "Simulations", name, "rooms.csv").toString());
		File guests= new File(Paths.get("AMPL", "Simulations", name, "guests.csv").toString());
		FileWriter roomWriter= null;
		FileWriter guestWriter= null;
		try {

			roomWriter= new FileWriter(rooms);
			guestWriter= new FileWriter(guests);
			roomWriter.write("number,type,attributes\n");
			guestWriter.write("id,position,type,prefs\n");

			for (Room room : this.rooms) {

				roomWriter.write(room.getNumber() + "," + room.getType() + ",");
				Iterator<String> iterator= room.getAttributes().iterator();
				while (iterator.hasNext()) {
					String attribute= iterator.next();
					if (!iterator.hasNext()) {
						roomWriter.write(attribute);
					} else {
						roomWriter.write(attribute + ":");
					}
				}
				roomWriter.write("\n");
			}

			for (Guest guest : this.guests) {
				guestWriter.write(guest.getID() + "," + guest.getArrivalPosition() + "," + guest.getType() + ",");

				Iterator<String> iterator= guest.getPreferences().iterator();
				while (iterator.hasNext()) {
					String preferences= iterator.next();
					if (!iterator.hasNext()) {
						guestWriter.write(preferences);
					} else {
						guestWriter.write(preferences + ":");
					}
				}
				guestWriter.write("\n");
			}

			roomWriter.close();
			guestWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// METHODS FOR SORTING ROOMS AND GUESTS VIA VARIOUS MEASURES

	/** Used for sorting a list of rooms in ascending order by room number */
	public static class SortByRoomNumber implements Comparator<Room> {
		@Override
		public int compare(Room a, Room b) {
			return a.getNumber() - b.getNumber();
		}
	}

	/** Used for sorting a list of guests in ascending order by guestID */
	public static class SortByGuestID implements Comparator<Guest> {
		@Override
		public int compare(Guest a, Guest b) {
			return a.getID() - b.getID();
		}
	}

	/** Used for sorting a list of rooms in ascending order by room type */
	public static class SortByRoomTypeAscending implements Comparator<Room> {
		@Override
		public int compare(Room a, Room b) {
			return a.getType() - b.getType();
		}
	}

	/** Used for sorting a list of rooms in descending order by room type */
	public static class SortByRoomTypeDescending implements Comparator<Room> {
		@Override
		public int compare(Room a, Room b) {
			return b.getType() - a.getType();
		}
	}

	/** Used for sorting a list of guests in descending order by requested room type */
	public static class SortByGuestType implements Comparator<Guest> {
		@Override
		public int compare(Guest a, Guest b) {
			return b.getType() - a.getType();
		}
	}

	/** Used for sorting a list of rooms in descending order by the number of <br>
	 * preferences met for the given Guest guest's preferences */
	public static class SortBySatisfactionFor implements Comparator<Room> {
		private Guest guest;

		SortBySatisfactionFor(Guest g) {
			guest= g;
		}

		@Override
		public int compare(Room a, Room b) {
			return guest.getMetPreferences(b) - guest.getMetPreferences(a);
		}
	}

	/** Used for sorting a list of guests in ascending order by arrival position */
	public static class SortByArrivalPosition implements Comparator<Guest> {
		@Override
		public int compare(Guest a, Guest b) {
			return a.getArrivalPosition() - b.getArrivalPosition();
		}

	}

}