package com.henryrobbins.hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import com.henryrobbins.RandProperties;
import com.henryrobbins.hotel.Instance.Builder;

/** Contains methods for creating instances in various ways */
public class InstanceFactory {

	/** Read CSV files arrivals.csv and weights.csv located in the given directory. <br>
	 * The CSV files should be in the following forms respectively <br>
	 *
	 * <pre>
	 *  arrivals.csv
	 * GUEST ID | REQUESTED ROOM TYPE | ARRIVAL TIME
	 *     1               1                 6
	 *     2               3                 2
	 * </pre>
	 *
	 * <pre>
	 * weights.csv
	 * 	                     ROOMS
	 *             |  1  |  2  |  3  |  4  |
	 *          1  | 0.1 | 0.7 | 0.2 | 0.4 |
	 * GUESTS   2  | 0.6 | 0.1 | 0.0 | 0.1 |
	 *          3  | 0.8 | 0.9 | 0.0 | 0.8 |
	 *
	 * </pre>
	 *
	 * Then create an instance from on the given hotel
	 *
	 * @param path  A path to a directory containing necessary files
	 * @param hotel A hotel for which the corresponding arrivals are feasible */
	public static Instance readCSV(Path dir, Hotel hotel) {
		// Create builder on the given hotel
		Builder builder= new Builder(hotel);
		// Read the arrivals.csv
		try {
			Path path= Paths.get(dir.toString().concat("/arrivals.csv"));
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
			throw new IllegalArgumentException("Error reading the arrivals.csv");
		}
		// Read the weights.csv
		try {
			Path path= Paths.get(dir.toString().concat("/weights.csv"));
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
		} catch (IOException ioe) {
			throw new IllegalArgumentException("Error reading the weights.csv");
		}
		return builder.build();
	}

	/** Read CSV file hotel.csv, arrivals.csv and weights.csv for the given hotel name <br>
	 * and instance name
	 *
	 * @param hotelID The name of the hotel
	 * @param instID  The name of the instance */
	public static Instance readCSV(String hotelID, String instID) {
		Hotel hotel= HotelFactory.readCSV(hotelID);
		return readCSV(Paths.get("Simulations", hotelID, instID), hotel);
	}

	/** Read CSV file hotel.csv, arrivals.csv and weights.csv for the given hotel id <br>
	 * and instance id
	 *
	 * @param hotelID The id of the hotel
	 * @param instID  The id of the instance */
	public static Instance readCSV(int hotelID, int instID) {
		Hotel hotel= HotelFactory.readCSV(hotelID);
		return readCSV(Paths.get("Simulations", String.valueOf(hotelID), String.valueOf(instID)), hotel);
	}

	/** Create a feasible random Instance with n room */
	public static Instance randInstance(int n) {
		Instance instance= null;

		boolean feasible= false;
		while (!feasible) {

			Hotel hotel= HotelFactory.randHotel(n);
			ArrayList<Room> rooms= hotel.rooms();
			Instance.Builder builder= new Instance.Builder(hotel);

			// The number of arriving guests binomially distributed around average number of guests
			BinomialDistribution guestSize= new BinomialDistribution(n, RandProperties.avgCapacity);

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
					sat= Math.min(1, sat + satDist.sample());
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
		if (maxType < 1) {
			System.out.println("A guest could not be added. Null returned.");
			return null;
		}
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
			sat= Math.min(1, sat + satDist.sample());
			builder.addWeight(add, room, sat);
		}

		return builder.build();

	}

	/** Round a double to n decimal places */
	public static double round(double value, int places) {
		double scale= Math.pow(10, places);
		return Math.round(value * scale) / scale;
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