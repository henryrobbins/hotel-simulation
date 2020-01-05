package com.henryrobbins.hotel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

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
	private static double avgCapacity;
	/** The ratio of housekeepers to rooms */
	private static double housekeepingRatio;

	/** The set of room types */
	private static int roomTypes;
	/** The distribution of room types */
	private static EnumeratedIntegerDistribution roomDistribution;
	/** The distribution of room type requests */
	private static EnumeratedIntegerDistribution requestDistribution;

	/** The distribution of guest pickiness thresholds */
	private static NormalDistribution pickinessDistribution;
	/** The mean for distribution of guest satisfaction (no upgrade) */
	private static double weightMean;
	/** The standard deviation for distribution of guest satisfaction (no upgrade) */
	private static double weightStd;
	/** The increase to mean for distribution of guest satisfaction for every upgrade */
	private static double upgradeBonus;

	/** The distribution of cleaning times based on room type */
	private static NormalDistribution[] cleanTime;
	/** The lower bound of cleaning time based on room type */
	private static int[] cleanTimeLB;
	/** The upper bound of cleaning time based on room type */
	private static int[] cleanTimeUB;

	/** The distribution of check-out times */
	private static NormalDistribution checkout;
	/** The lower bound check-out time */
	private static int checkoutLB;
	/** The upper bound check-out time */
	private static int checkoutUB;

	/** The distribution of check-in times */
	private static NormalDistribution checkin;
	/** The lower bound check-in time */
	private static int checkinLB;
	/** The upper bound check-in time */
	private static int checkinUB;

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

	/** Initialize random parameters from the properties file */
	private static void initParams() {
		Properties randParam= new Properties();
		try {
			InputStream is= new FileInputStream("randParam.properties");
			randParam.load(is);
			is.close();
		} catch (Exception e) {
			throw new IllegalArgumentException("Issue with properties file");
		}

		avgCapacity= Double.parseDouble((String) randParam.get("avgCapacity"));
		housekeepingRatio= Double.parseDouble((String) randParam.get("housekeepingRatio"));
		roomTypes= Integer.parseInt((String) randParam.get("roomTypes"));

		int[] rooms= new int[roomTypes];
		for (int i= 0; i < roomTypes; i++ ) {
			rooms[i]= i + 1;
		}
		double[] roomDist= parseDoubleArray((String) randParam.get("roomDist"));
		roomDistribution= new EnumeratedIntegerDistribution(rooms, roomDist);
		double[] requestDist= parseDoubleArray((String) randParam.get("requestDist"));
		requestDistribution= new EnumeratedIntegerDistribution(rooms, requestDist);

		double pickinessMean= Double.parseDouble((String) randParam.get("pickinessMean"));
		double pickinessStd= Double.parseDouble((String) randParam.get("pickinessStd"));
		pickinessDistribution= new NormalDistribution(pickinessMean, pickinessStd);
		weightMean= Double.parseDouble((String) randParam.get("weightMean"));
		weightStd= Double.parseDouble((String) randParam.get("weightStd"));
		upgradeBonus= Double.parseDouble((String) randParam.get("upgradeBonus"));

		int[] cleanTimeMean= parseIntArray((String) randParam.get("cleanTimeMean"));
		int[] cleanTimeStd= parseIntArray((String) randParam.get("cleanTimeStd"));
		cleanTime= new NormalDistribution[roomTypes];
		for (int i= 0; i < roomTypes; i++ ) {
			cleanTime[i]= new NormalDistribution(
				cleanTimeMean[i] / TIME_INTERVAL, cleanTimeStd[i] / TIME_INTERVAL);
		}

		int[] cleanTimeLb= parseIntArray((String) randParam.get("cleanTimeLb"));
		cleanTimeLB= interval(cleanTimeLb);

		int[] cleanTimeUb= parseIntArray((String) randParam.get("cleanTimeUb"));
		cleanTimeUB= interval(cleanTimeUb);

		int checkoutMean= Integer.parseInt((String) randParam.get("checkoutMean"));
		int checkoutStd= Integer.parseInt((String) randParam.get("checkoutStd"));
		checkout= new NormalDistribution(checkoutMean / TIME_INTERVAL, checkoutStd / TIME_INTERVAL);
		checkoutLB= (int) (Integer.parseInt((String) randParam.get("checkoutLb")) / TIME_INTERVAL);
		checkoutUB= (int) (Integer.parseInt((String) randParam.get("checkoutUb")) / TIME_INTERVAL);

		int checkinMean= Integer.parseInt((String) randParam.get("checkinMean"));
		int checkinStd= Integer.parseInt((String) randParam.get("checkinStd"));
		checkin= new NormalDistribution(checkinMean / TIME_INTERVAL, checkinStd / TIME_INTERVAL);
		checkinLB= (int) (Integer.parseInt((String) randParam.get("checkinLb")) / TIME_INTERVAL);
		checkinUB= (int) (Integer.parseInt((String) randParam.get("checkinUb")) / TIME_INTERVAL);

	}

	/** Convert the string representation of an int array back to int[] */
	private static int[] parseIntArray(String str) {

		str= str.replace("[", "");
		str= str.replace("]", "");
		String[] strArray= str.split(",");
		int[] array= new int[strArray.length];
		for (int i= 0; i < strArray.length; i++ ) {
			array[i]= Integer.valueOf(strArray[i].trim());
		}
		return array;
	}

	/** Convert the string representation of a double array back to double[] */
	private static double[] parseDoubleArray(String str) {

		str= str.replace("[", "");
		str= str.replace("]", "");
		String[] strArray= str.split(",");
		double[] array= new double[strArray.length];
		for (int i= 0; i < strArray.length; i++ ) {
			array[i]= Double.valueOf(strArray[i].trim());
		}
		return array;
	}

	/** Convert an array of minutes to time intervals */
	private static int[] interval(int[] array) {
		for (int i= 0; i < array.length; i++ ) {
			array[i]= (int) (array[i] / TIME_INTERVAL);
		}
		return array;
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

		initParams();

		// The number of arriving guests binomially distributed around average number of guests
		BinomialDistribution guestSize= new BinomialDistribution(n, avgCapacity);

		int h= Math.max(1, (int) (n * housekeepingRatio));

		Instance instance= null;

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

			int g= Math.max(1, guestSize.sample());
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
					NormalDistribution weightDistribution= new NormalDistribution(weightMean + upgradeBonus * upgrade,
						weightStd);
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