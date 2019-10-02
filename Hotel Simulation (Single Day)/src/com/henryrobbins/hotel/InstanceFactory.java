package com.henryrobbins.hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

/** Contains methods for creating instances in various ways */
public class InstanceFactory {

	/** The number of minutes each time interval represents */
	private static final double TIME_INTERVAL= 5;

	/** The ratio of guests to rooms */
	private static double guestRatio= 0.90;

	/** The ratio of housekeepers to rooms */
	private static double housekeepingRatio= 0.10;

	/** The set of room types */
	private static int[] roomTypes= new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
	// Type 1 - Single ( View 1 )
	// Type 2 - Single ( View 2 )
	// Type 3 - Double ( View 1 )
	// Type 4 - Double ( View 2 )
	// Type 5 - Suite ( Type 1 )
	// Type 6 - Suite ( Type 2 )
	// Type 7 - Penthouse 1
	// Type 8 - Penthouse 2

	/** The distribution of room types */
	private static EnumeratedIntegerDistribution roomDistribution= new EnumeratedIntegerDistribution(roomTypes,
		new double[] { 0.43, 0.255, 0.14, 0.055, 0.05, 0.045, 0.02, 0.005 });
	/** The distribution of room type requests */
	private static EnumeratedIntegerDistribution requestDistribution= new EnumeratedIntegerDistribution(roomTypes,
		new double[] { 0.58, 0.185, 0.125, 0.04, 0.025, 0.025, 0.015, 0.005 });

	/** The distribution of guest pickiness thresholds */
	private static NormalDistribution pickinessDistribution= new NormalDistribution(0.5, 0.2);

	/** The distribution of cleaning times based on room type */
	private static NormalDistribution[] cleanTime= {
			new NormalDistribution(30 / TIME_INTERVAL, 5 / TIME_INTERVAL), // Type 1
			new NormalDistribution(30 / TIME_INTERVAL, 5 / TIME_INTERVAL), // Type 2
			new NormalDistribution(35 / TIME_INTERVAL, 5 / TIME_INTERVAL), // Type 3
			new NormalDistribution(35 / TIME_INTERVAL, 5 / TIME_INTERVAL), // Type 4
			new NormalDistribution(40 / TIME_INTERVAL, 10 / TIME_INTERVAL), // Type 5
			new NormalDistribution(40 / TIME_INTERVAL, 10 / TIME_INTERVAL), // Type 6
			new NormalDistribution(50 / TIME_INTERVAL, 10 / TIME_INTERVAL), // Type 7
			new NormalDistribution(50 / TIME_INTERVAL, 10 / TIME_INTERVAL) }; // Type 8

	/** The lower bound of cleaning time based on room type */
	private static int[] cleanTimeLB= {
			(int) (20 / TIME_INTERVAL),
			(int) (20 / TIME_INTERVAL),
			(int) (25 / TIME_INTERVAL),
			(int) (25 / TIME_INTERVAL),
			(int) (30 / TIME_INTERVAL),
			(int) (30 / TIME_INTERVAL),
			(int) (40 / TIME_INTERVAL),
			(int) (40 / TIME_INTERVAL) };

	/** The upper bound of cleaning time based on room type */
	private static int[] cleanTimeUB= {
			(int) (60 / TIME_INTERVAL),
			(int) (60 / TIME_INTERVAL),
			(int) (65 / TIME_INTERVAL),
			(int) (65 / TIME_INTERVAL),
			(int) (70 / TIME_INTERVAL),
			(int) (70 / TIME_INTERVAL),
			(int) (80 / TIME_INTERVAL),
			(int) (80 / TIME_INTERVAL) };

	/** The distribution of check-out times */
	private static NormalDistribution checkout= new NormalDistribution(660 / TIME_INTERVAL, 45 / TIME_INTERVAL);
	/** The lower bound check-out time */
	private static int checkoutLB= (int) (360.0 / TIME_INTERVAL); // 6:00 AM
	/** The upper bound check-out time */
	private static int checkoutUB= (int) (780.0 / TIME_INTERVAL); // 1:00 PM

	/** The distribution of check-in times */
	private static NormalDistribution checkin= new NormalDistribution(900 / TIME_INTERVAL, 45 / TIME_INTERVAL);
	/** The lower bound check-in time */
	private static int checkinLB= (int) (840.0 / TIME_INTERVAL); // 2:00 PM
	/** The upper bound check-in time */
	private static int checkinUB= (int) (1200.0 / TIME_INTERVAL); // 8:00 PM

	/** Create an Instance from the given directory which contains three CSV files: <br>
	 * rooms, guests, and weights. Furthermore, there are h housekeepers in the instance.
	 *
	 * @param dir The directory containing the three CSV files
	 * @param h   The number of housekeepers in the instance (at least 1) */
	public static Instance readCSV(Path dir, int h) {

		if (h < 1) throw new IllegalArgumentException("Less than 1 housekeeper");

		Instance.Builder builder= new Instance.Builder(dir.toFile().getName(), h);

		readRoomCSV(dir, builder);
		readGuestCSV(dir, builder);
		readWeightCSV(dir, builder);

		Instance instance= builder.build();
		if (!instance.feasible()) {
			throw new IllegalArgumentException("CSV File Error: rooms can't accomodate guests");
		}
		return instance;
	}

	/** Create an Instance from a directory with the given name containing three CSV files: <br>
	 * rooms, guests, and weights. The directory must be located in the "Simulations" directory. <br>
	 * Furthermore, there are h housekeepers in the instance.
	 *
	 * @param name Name of directory containing the three CSV files (located in "Simulations" directory)
	 * @param h    The number of housekeepers in the instance (at least 1) */
	public static Instance readCSV(String name, int h) {
		if (name == null) throw new IllegalArgumentException("Name was null");
		Path dir= Paths.get("AMPL", "Simulations", name);
		return readCSV(dir, h);
	}

	/** Create a feasible random Instance with n rooms. The number of guests and housekeepers,<br>
	 * room types and request type, check out and arrival times, and attributes and preferences <br>
	 * are chosen randomly based on implemented distributions. Information about these <br>
	 * distributions can be found in the appropriate file.
	 *
	 * @param name The name of the instance
	 * @param n    The number of rooms in the instance (at least 1) */
	public static Instance createRandom(String name, int n) {
		if (n < 1) throw new IllegalArgumentException("Less than 1 room");

		// The number of arriving guests binomially distributed around 90% capacity
		BinomialDistribution guestSize= new BinomialDistribution(n, guestRatio);

		int h= (int) (n * housekeepingRatio);
		h= h < 1 ? 1 : h;

		Instance instance= new Instance.Builder(name, h).build();

		boolean feasible= false;
		while (!feasible) {

			Instance.Builder builder= new Instance.Builder(name, h);

			for (int i= 1; i <= n; i++ ) {
				int num= i;
				int type= roomDistribution.sample(1)[0];
				int out= rejectionIntSample(checkout, checkoutLB, checkoutUB);
				int j= type - 1;
				int clean= rejectionIntSample(cleanTime[j], cleanTimeLB[j], cleanTimeUB[j]);
				builder.addRoom(new Room(num, type, out, clean));
			}

			int g= guestSize.sample();
			for (int i= 1; i <= g; i++ ) {
				int id= i;
				int type= requestDistribution.sample(1)[0];
				int arrival= rejectionIntSample(checkin, checkinLB, checkinUB);
				builder.addGuest(new Guest(id, type, arrival));
			}

			ArrayList<Guest> guests= builder.guests();
			ArrayList<Room> rooms= builder.rooms();

			// Keep track of highest assigned weight for each guest
			HashMap<Guest, Double> max= new HashMap<>();
			for (Guest guest : guests) {
				max.put(guest, 0.0);
				// Generate guest's pickiness threshold. A lower number indicates a pickier guest
				double pickiness= rejectionSample(pickinessDistribution, 0, 1);
				for (Room room : rooms) {
					// Generate a weight distribution slightly skewed for upgraded room types
					int upgrade= room.type() - guest.type();
					NormalDistribution weightDistribution= new NormalDistribution(0.6 + 0.05 * upgrade, 0.2);
					// Generate a weight from distribution between pickiness and 1
					DecimalFormat df= new DecimalFormat("#.######");
					double weight= Double.valueOf(df.format(rejectionSample(weightDistribution, pickiness, 1)));
					builder.addWeight(guest, room, weight);
					max.put(guest, Math.max(max.get(guest), weight));
				}
			}

			MultiKeyMap<Object, Double> weights= builder.weights();
			for (Guest guest : guests) {
				for (Room room : rooms) {
					Double prev= weights.get(guest, room);
					builder.addWeight(guest, room, prev + 1 - max.get(guest));
				}
			}

			instance= builder.build();
			feasible= instance.feasible();
		}
		return instance;
	}

	/** Creates a copy of the given instance with an additional randomly generated guest. <br>
	 * If adding a guest is infeasible, returns a null Instance.
	 *
	 * @param name     The name of the instance to be created
	 * @param instance The instance to which the new guest will be added */
	public static Instance addGuestTo(Instance instance, String name) {
		if (name == null) throw new IllegalArgumentException("Name is null");
		if (name.length() < 1) throw new IllegalArgumentException("Name is less than 1 character");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Instance.Builder builder= new Instance.Builder(name, instance);

		Guest add= null;
		int maxType= instance.maxFeasibleTypeRequest();
		if (maxType < 1) {
			System.out.println("A guest could not be added. Null returned.");
			return null;
		}
		while (add == null || add.type() > maxType) {
			int id= instance.guests().size() + 1;
			int type= requestDistribution.sample(1)[0];
			int arrival= rejectionIntSample(checkin, checkinLB, checkinUB);
			add= new Guest(id, type, arrival);
		}

		builder.addGuest(add);

		HashMap<Room, Double> addWeights= new HashMap<>();
		// Generate guest's pickiness threshold. A lower number indicates a pickier guest
		double pickiness= rejectionSample(pickinessDistribution, 0, 1);
		for (Room room : builder.rooms()) {
			// Generate a weight distribution slightly skewed higher for higher room types
			NormalDistribution weightDistribution= new NormalDistribution(0.6 + 0.05 * room.type(), 0.2);
			// Generate a weight from distribution between pickiness and 1
			DecimalFormat df= new DecimalFormat("#.######");
			double weight= Double.valueOf(df.format(rejectionSample(weightDistribution, pickiness, 1)));
			addWeights.put(room, weight);

		}
		// Shift all weights for guest such that top room has weight 1
		Double max= 0.0;
		for (Room room : addWeights.keySet()) {
			max= Math.max(max, addWeights.get(room));
		}
		Double shift= 1 - max;
		for (Room room : addWeights.keySet()) {
			builder.addWeight(add, room, addWeights.get(room) + shift);
		}

		return builder.build();

	}

	/** Given a path to a directory containing a file named rooms.csv, read the CSV file and <br>
	 * add every room entry to the builder. The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * ROOM NUMBER | ROOM TYPE | CHECK OUT TIME | CLEAN TIME
	 *     1             2             6              2
	 *     2             1             3              4
	 * </pre>
	 *
	 * Furthermore, every room number in the CSV should be unique.
	 *
	 * @param path    A path to a directory containing a file named rooms.csv
	 * @param builder The Builder in which the constructed rooms will be added */
	private static void readRoomCSV(Path dir, Instance.Builder builder) {

		try {
			Path path= Paths.get(dir.toString().concat("/rooms.csv"));
			BufferedReader br= Files.newBufferedReader(path);

			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				String[] values= contentLine.split(",");
				int num= Integer.parseInt(values[0].trim());
				int type= Integer.parseInt(values[1].trim());
				int checkOut= Integer.parseInt(values[2].trim());
				int cleanTime= Integer.parseInt(values[3].trim());
				builder.addRoom(new Room(num, type, checkOut, cleanTime));
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			throw new IllegalArgumentException("rooms.csv was not found for that instance name");
		}
	}

	/** Given a path to a directory containing a file named guests.csv, read the CSV file and <br>
	 * add every guest entry to the builder. The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * GUEST ID | REQUESTED ROOM TYPE | ARRIVAL TIME
	 *     1               1                 6
	 *     2               3                 2
	 * </pre>
	 *
	 * Furthermore, every guest ID in the CSV should be unique.
	 *
	 * @param path    A path to a directory containing a file named guests.csv
	 * @param builder The Builder in which the constructed guests will be added */
	private static void readGuestCSV(Path dir, Instance.Builder builder) {

		try {
			Path path= Paths.get(dir.toString().concat("/guests.csv"));
			BufferedReader br= Files.newBufferedReader(path);

			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				String[] values= contentLine.split(",");
				int id= Integer.parseInt(values[0].trim());
				int type= Integer.parseInt(values[1].trim());
				int arrival= Integer.parseInt(values[2].trim());
				builder.addGuest(new Guest(id, type, arrival));
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			throw new IllegalArgumentException("guests.csv was not found for that instance name");
		}
	}

	/** Given a path to a directory containing a file named weights.csv, read the CSV file and <br>
	 * add every guest-room weight to the builder. The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * 	                     ROOMS
	 *             |  1  |  2  |  3  |  4  |
	 *          1  | 0.1 | 0.7 | 0.2 | 0.4 |
	 * GUESTS   2  | 0.6 | 0.1 | 0.0 | 0.1 |
	 *          3  | 0.8 | 0.9 | 0.0 | 0.8 |
	 *
	 * </pre>
	 *
	 * @param path    A path to a directory containing a file named weights.csv
	 * @param builder The Builder in which the constructed guests will be added */
	private static void readWeightCSV(Path dir, Instance.Builder builder) {

		try {
			Path path= Paths.get(dir.toString().concat("/weights.csv"));
			BufferedReader br= Files.newBufferedReader(path);

			ArrayList<Room> rooms= new ArrayList<>();
			String contentLine= br.readLine();
			String[] ids= contentLine.split(",");
			for (int i= 1; i < ids.length; i++ ) {
				rooms.add(builder.room(Integer.parseInt(ids[i].trim())));
			}
			contentLine= br.readLine();
			while (contentLine != null) {
				String[] wgts= contentLine.split(",");
				Guest guest= builder.guest(Integer.parseInt(wgts[0].trim()));
				for (int i= 1; i < wgts.length; i++ ) {
					builder.addWeight(guest, rooms.get(i - 1), Double.parseDouble(wgts[i].trim()));
				}
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			throw new IllegalArgumentException("weights.csv was not found for that instance name");
		}
	}

	/** Return sample from given distribution within the specified range via rejection sampling
	 *
	 * @param dist The distribution from which the sample will be taken
	 * @param a    The lower bound of the accepted region - inclusive (less than b)
	 * @param b    The upper bound of the accepted region - inclusive (greater than a)
	 * @return sample within the specified range */
	private static double rejectionSample(AbstractRealDistribution dist, double a, double b) {
		if (a > b) throw new IllegalArgumentException("a was greater than b");
		double sample= dist.sample();
		while (sample < a || sample > b) {
			sample= dist.sample();
		}
		return sample;
	}

	/** Return sample from given distribution within the specified range via rejection sampling
	 *
	 * @param dist The distribution from which the sample will be taken
	 * @param a    The lower bound of the accepted region - inclusive (less than b)
	 * @param b    The upper bound of the accepted region - inclusive (greater than a)
	 * @return sample within the specified range */
	private static int rejectionIntSample(AbstractRealDistribution dist, int a, int b) {
		if (a > b) throw new IllegalArgumentException("a was greater than b");
		int sample= (int) dist.sample();
		while (sample < a || sample > b) {
			sample= (int) dist.sample();
		}
		return sample;
	}
}