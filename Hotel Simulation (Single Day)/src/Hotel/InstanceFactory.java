package Hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

/** Contains methods for creating random instances and instances from CSV files */
public class InstanceFactory {

	/** The number of minutes each time interval represents */
	private static final double TIME_INTERVAL= 5;

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

	/** Create an Instance from a folder called "name" which contains two CSV files for <br>
	 * rooms and guests respectively. This folder should be in the Simulation folder within <br>
	 * the AMPL folder. Furthermore, there are h housekeepers in the instance.
	 *
	 * @param name The name of the instance (and folder)
	 * @param h    The number of housekeepers in the instance (at least 1) */
	public static Instance createInstance(String name, int h) {

		if (h < 1) throw new IllegalArgumentException("Less than 1 housekeeper");

		Instance.Builder builder= new Instance.Builder(name, h);

		readRoomCSV(Paths.get("AMPL", "Simulations", name, "rooms.csv"), builder);
		readGuestCSV(Paths.get("AMPL", "Simulations", name, "guests.csv"), builder);

		if (!builder.canAccommodate()) {
			throw new IllegalArgumentException("CSV File Error: rooms can't accomodate guests");
		}

		builder.convertToCSV();
		return builder.build();
	}

	/** Create a random Instance with n rooms and the given name. The number of guests and <br>
	 * housekeepers, room types and request type, check out and arrival times, and <br>
	 * attributes and preferences are chosen randomly based on implemented distributions. <br>
	 * Information about these distributions can be found in the appropriate file. <br>
	 *
	 * @param n    The number of rooms to be in this instance (at least 1)
	 * @param name The name of the instance to be created */
	public static Instance createRandomInstance(int n, String name) {
		if (n < 1) throw new IllegalArgumentException("Less than 1 room");

		// The number of arriving guests binomially distributed around 90% capacity
		BinomialDistribution guestSize= new BinomialDistribution(n, 0.9);

		int h= (int) (n * housekeepingRatio);
		h= h < 1 ? 1 : h;

		Instance.Builder builder= new Instance.Builder(name, h);
		boolean feasible= false;

		while (!feasible) {

			builder= new Instance.Builder(name, h);

			for (int i= 1; i <= n; i++ ) {
				int num= i;
				int type= roomDistribution.sample(1)[0];
				int out= rejectionSample(checkout, checkoutLB, checkoutUB);
				int j= type - 1;
				int clean= rejectionSample(cleanTime[j], cleanTimeLB[j], cleanTimeUB[j]);
				HashSet<String> attributes= getRandAttributes();
				builder.addRoom(new Room(num, type, out, clean, attributes));
			}

			int g= guestSize.sample();
			for (int i= 1; i <= g; i++ ) {
				int id= i;
				int type= requestDistribution.sample(1)[0];
				int arrival= rejectionSample(checkin, checkinLB, checkinUB);
				HashSet<String> preferences= getRandAttributes();
				builder.addGuest(new Guest(id, type, arrival, preferences));
			}

			feasible= builder.canAccommodate();
		}

		try {
			Files.createDirectories(Paths.get("AMPL", "Simulations", name));
		} catch (IOException e) {
			e.printStackTrace();
		}

		builder.convertToCSV();

		return builder.build();

	}

	/** Creates a copy of the given instance with an additional randomly generated guest. <br>
	 * If adding a guest is infeasible, returns a null Instance.
	 *
	 * @param name     The name of the instance to be created
	 * @param instance The instance to which the new guest will be added */
	public static Instance addGuestTo(Instance instance, String name) {
		if (name == null) throw new IllegalArgumentException("Name is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		int[] typeSize= new int[8];
		for (Room room : instance.getRooms()) {
			typeSize[room.getType() - 1]++ ;
		}
		int[] requestSize= new int[8];
		for (Guest guest : instance.getGuests()) {
			requestSize[guest.getType() - 1]++ ;
		}

		int t= 0;
		int requests= instance.getGuests().size();
		int available= instance.getRooms().size();
		while (requests < available) {
			requests-= requestSize[t];
			available-= typeSize[t];
			t++ ;
		}

		if (t == 0) return null;

		Instance.Builder builder= new Instance.Builder(name, instance.getNumOfHousekeepers());
		for (Room room : instance.getRooms()) {
			builder.addRoom(room);
		}
		for (Guest guest : instance.getGuests()) {
			builder.addGuest(guest);
		}

		Guest add= null;
		while (add == null || add.getType() > t) {
			int id= instance.getGuests().size() + 1;
			int type= requestDistribution.sample(1)[0];
			int arrival= rejectionSample(checkin, checkinLB, checkinUB);
			HashSet<String> preferences= getRandAttributes();
			add= new Guest(id, type, arrival, preferences);
		}

		builder.addGuest(add);

		return builder.build();

	}

	/** Return a randomly generated list of attributes / preferences */
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

	/** Return sample from given distribution within the specified range via rejection sampling
	 *
	 * @param dist The distribution from which the sample will be taken
	 * @param a    The lower bound of the accepted region - inclusive (less than b)
	 * @param b    The upper bound of the accepted region - inclusive (greater than a)
	 * @return sample within the specified range */
	private static int rejectionSample(AbstractRealDistribution dist, int a, int b) {
		if (a > b) throw new IllegalArgumentException("a was greater than b");
		int sample= (int) dist.sample();
		while (sample < a || sample > b) {
			sample= (int) dist.sample();
		}
		return sample;
	}

	/** Given a path to a directory containing a file named rooms.csv, read the CSV file and <br>
	 * add every room entry to the builder. The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * ROOM NUMBER | ROOM TYPE | CHECK OUT TIME | CLEAN TIME | ATTRIBUTES
	 *     1             2             6              2           A:B
	 *     2             1             3              4          A:C:D
	 * </pre>
	 *
	 * Furthermore, every room number in the CSV should be unique.
	 *
	 * @param path    A path to a directory containing a file named rooms.csv
	 * @param builder The Builder in which the constructed rooms will be added */
	private static void readRoomCSV(Path path, Instance.Builder builder) {

		try {
			BufferedReader br= Files.newBufferedReader(path);

			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				String[] values= contentLine.split(",");
				int num= Integer.parseInt(values[0]);
				int type= Integer.parseInt(values[1]);
				int checkOut= Integer.parseInt(values[2]);
				int cleanTime= Integer.parseInt(values[3]);
				HashSet<String> attributes= new HashSet<>();
				if (values.length > 4) {
					String[] attr= values[4].split(":");
					for (int i= 0; i < attr.length; i++ ) {
						attributes.add(attr[i]);
					}
				}
				builder.addRoom(new Room(num, type, checkOut, cleanTime, attributes));
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/** Given a path to a directory containing a file named guests.csv, read the CSV file and <br>
	 * add every guest entry to the builder. The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * GUEST ID | REQUESTED ROOM TYPE | ARRIVAL TIME | PREFERENCES
	 *     1               1                 6             A:B
	 *     2               3                 2            A:C:D
	 * </pre>
	 *
	 * Furthermore, every guest ID in the CSV should be unique.
	 *
	 * @param path    A path to a directory containing a file named guests.csv
	 * @param builder The Builder in which the constructed guests will be added */
	private static void readGuestCSV(Path path, Instance.Builder builder) {

		try {
			BufferedReader br= Files.newBufferedReader(path);

			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				String[] values= contentLine.split(",");
				int id= Integer.parseInt(values[0]);
				int type= Integer.parseInt(values[1]);
				int arrival= Integer.parseInt(values[2]);
				HashSet<String> preferences= new HashSet<>();
				if (values.length > 3) {
					String[] prefs= values[3].split(":");
					for (int i= 0; i < prefs.length; i++ ) {
						preferences.add(prefs[i]);
					}
				}
				builder.addGuest(new Guest(id, type, arrival, preferences));
				contentLine= br.readLine();
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}