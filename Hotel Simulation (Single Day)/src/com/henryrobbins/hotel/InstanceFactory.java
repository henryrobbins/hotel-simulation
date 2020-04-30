package com.henryrobbins.hotel;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import com.henryrobbins.RandProperties;
import com.henryrobbins.hotel.Instance.Builder;

/** Contains methods for creating instances in various ways */
public abstract class InstanceFactory {

	/** Read CSV files representing a hotel, arrivals, and weights respectively. Construct Instance.
	 *
	 * @param hotel    path to a CSV representing a hotel
	 * @param arrivals path to a CSV representing a arrivals
	 * @param weights  path to a CSV representing a weights
	 * @throws Exception */
	public static Instance readCSV(Path hotel, Path arrivals, Path weights) throws Exception {
		return readCSV(HotelFactory.readCSV(hotel), arrivals, weights);
	}

	/** Read CSV files representing arrivals and weights respectively. Construct Instance on hotel.
	 *
	 * @param hotel    A hotel to create an instance for
	 * @param arrivals path to a CSV representing a arrivals
	 * @param weights  path to a CSV representing a weights
	 * @throws Exception */
	public static Instance readCSV(Hotel hotel, Path arrivals, Path weights) throws Exception {
		Builder builder= new Instance.Builder(hotel);
		readArrivalsCSV(arrivals, builder);
		readWeightsCSV(weights, builder);
		return builder.build();
	}

	/** Read the CSV file representing arrivals located at the given path; add arrivals to builder <br>
	 * The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * GUEST ID | REQUESTED ROOM TYPE | ARRIVAL TIME
	 *     1               1                 6
	 *     2               3                 2
	 * </pre>
	 *
	 * @param path    path to a csv file in the proper format
	 * @param builder an instance builder to add the arrivals to
	 * @throws Exception */
	private static void readArrivalsCSV(Path path, Builder builder) throws Exception {
		if (path == null) throw new IllegalArgumentException("Path to Arrivals CSV file was null.");
		BufferedReader br= Files.newBufferedReader(path);
		String contentLine= br.readLine();
		contentLine= br.readLine();
		while (contentLine != null) {
			String[] values= contentLine.split(",");
			if (values.length == 3) {
				int id= Integer.parseInt(values[0].trim());
				int type= Integer.parseInt(values[1].trim());
				int arrival= Integer.parseInt(values[2].trim());
				builder.addGuest(new Guest(id, type, arrival));
			} else {
				br.close();
				throw new IllegalArgumentException("The Arrivals CSV file is not in the proper format.");
			}
			contentLine= br.readLine();
		}
		br.close();
	}

	/** Read the CSV file representing weights located at the given path; add weights to builder<br>
	 * The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * 	                     ROOMS
	 *             |  1  |  2  |  3  |  4  |
	 *          1  | 0.1 | 0.7 | 0.2 | 0.4 |
	 * GUESTS   2  | 0.6 | 0.1 | 0.0 | 0.1 |
	 *          3  | 0.8 | 0.9 | 0.0 | 0.8 |
	 * </pre>
	 *
	 * @param path    path to a csv file in the proper format
	 * @param builder an instance builder to add the arrivals to
	 * @throws Exception */
	private static void readWeightsCSV(Path path, Builder builder) throws Exception {
		if (path == null) throw new IllegalArgumentException("Path to Weights CSV file was null.");
		Hotel hotel= builder.hotel();
		BufferedReader br= Files.newBufferedReader(path);
		String contentLine= br.readLine();
		String[] rooms= contentLine.split(",");
		int[] roomIDs= new int[rooms.length];
		for (int i= 1; i < rooms.length; i++ ) {
			roomIDs[i]= Integer.parseInt(rooms[i].trim());
		}
		contentLine= br.readLine();
		while (contentLine != null) {
			String[] wgts= contentLine.split(",");
			int guest= Integer.parseInt(wgts[0].trim());
			for (int i= 1; i < wgts.length; i++ ) {
				Guest g= builder.guest(guest);
				Room r= hotel.room(roomIDs[i]);
				builder.addWeight(g, r, Double.parseDouble(wgts[i].trim()));
			}
			contentLine= br.readLine();
		}
		br.close();
	}

	/** Create a feasible random Instance with n rooms */
	public static Instance randInstance(int n) {
		return randInstance(HotelFactory.randHotel(n));
	}

	/** Create a feasible random Instance for a given hotel */
	public static Instance randInstance(Hotel hotel) {
		Instance instance= null;

		boolean feasible= false;
		while (!feasible) {

			ArrayList<Room> rooms= hotel.rooms();
			Instance.Builder builder= new Instance.Builder(hotel);

			// The number of arriving guests binomially distributed around average number of guests
			BinomialDistribution guestSize= new BinomialDistribution(rooms.size(), RandProperties.avgCapacity);

			int g= Math.max(1, guestSize.sample());
			for (int i= 1; i <= g; i++ ) {
				int id= i;
				int type= RandProperties.requestDistribution.sample(1)[0];
				int arrival= rejectionIntSample(RandProperties.checkin, RandProperties.checkinLB,
					RandProperties.checkinUB);
				builder.addGuest(new Guest(id, type, arrival));
			}

			ArrayList<Guest> guests= builder.guests();

			for (Guest guest : guests) {
				// Generate guest satisfaction lower and upper bounds
//				double ubSat= 1.0;
//				double lbSat= 0.4;
				double ubSat= rejectionSample(RandProperties.ubSatDistribution, 0, 1);
				double lbSat= ubSat * rejectionSample(RandProperties.lbSatDistribution, 0, 1);
				for (Room room : rooms) {
					// Initialize 1 if room upgrade; 0 otherwise
					int upgraded= Math.min(1, room.type() - guest.type());
					// Determine guest satisfaction;
					double sat= Math.min(1,
						lbSat + (ubSat - lbSat) * room.quality() + upgraded * RandProperties.upgradeBonus);
					// Introduce more randomness
					NormalDistribution satDist= new NormalDistribution(0, RandProperties.randSat);
					sat= sat + satDist.sample();
					sat= (double) Math.round(sat * 100000) / 100000;
					sat= Math.min(1, sat);
					builder.addWeight(guest, room, sat);
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
	 * @param instance The instance to which the new guest will be added */
	public static Instance addGuestTo(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Instance.Builder builder= new Instance.Builder(instance);

		Guest add= null;
		int maxType= instance.maxFeasibleTypeRequest();
		if (maxType < 1) { return null; }
		while (add == null || add.type() > maxType) {
			int id= instance.guests().size() + 1;
			int type= RandProperties.requestDistribution.sample(1)[0];
			int arrival= rejectionIntSample(RandProperties.checkin, RandProperties.checkinLB, RandProperties.checkinUB);
			add= new Guest(id, type, arrival);
		}

		builder.addGuest(add);

		// Generate guest satisfaction lower and upper bounds
		double ubSat= rejectionSample(RandProperties.ubSatDistribution, 0, 1);
		double lbSat= ubSat * rejectionSample(RandProperties.lbSatDistribution, 0, 1);
		for (Room room : instance.rooms()) {
			// Initialize 1 if room upgrade; 0 otherwise
			int upgraded= Math.min(1, room.type() - add.type());
			// Determine guest satisfaction;
			double sat= Math.min(1,
				lbSat + (ubSat - lbSat) * room.quality() + upgraded * RandProperties.upgradeBonus);
			// Introduce more randomness
			NormalDistribution satDist= new NormalDistribution(0, RandProperties.randSat);
			sat= sat + satDist.sample();
			sat= (double) Math.round(sat * 100000) / 100000;
			sat= Math.min(1, sat);
			builder.addWeight(add, room, sat);
		}

		return builder.build();

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