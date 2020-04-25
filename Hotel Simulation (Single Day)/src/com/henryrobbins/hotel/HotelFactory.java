package com.henryrobbins.hotel;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import com.henryrobbins.RandProperties;
import com.henryrobbins.hotel.Hotel.Builder;

/** Contains static methods to construct a hotel from a parameter file or <br>
 * from a CSV file located at some given directory */
public abstract class HotelFactory {

	/** Read the CSV file located at the given path <br>
	 * The CSV file should be in the following format: <br>
	 *
	 * <pre>
	 * ROOM NUMBER | ROOM TYPE | ROOM QUALITY | CHECK OUT TIME | CLEAN TIME
	 *      1            2            0.5             6              2
	 *      2            1             1              3              4
	 *      h
	 * </pre>
	 *
	 * where h represents the size of the housekeeping team
	 *
	 * @param path path to a csv file in the proper format */
	public static Hotel readCSV(Path path) throws Exception {
		if (path == null) throw new IllegalArgumentException("Path to Hotel CSV file was null.");
		Builder builder= new Builder();
		BufferedReader br= Files.newBufferedReader(path);
		String contentLine= br.readLine();
		contentLine= br.readLine();
		while (contentLine != null) {
			String[] values= contentLine.split(",");
			if (values.length == 5) {
				int num= Integer.parseInt(values[0].trim());
				int type= Integer.parseInt(values[1].trim());
				double quality= Double.parseDouble(values[2].trim());
				int checkOut= Integer.parseInt(values[3].trim());
				int cleanTime= Integer.parseInt(values[4].trim());
				builder.addRoom(new Room(num, type, quality, checkOut, cleanTime));
			} else if (values.length == 1) {
				builder.setH(Integer.parseInt(values[0]));
			} else {
				br.close();
				throw new IllegalArgumentException("The Hotel CSV file is not in the proper format.");
			}
			contentLine= br.readLine();
		}
		br.close();
		return builder.build();
	}

	/** Create a randomly generated hotel with n rooms */
	public static Hotel randHotel(int n) {
		if (n < 1) throw new IllegalArgumentException("Less than 1 room");
		Hotel.Builder builder= new Hotel.Builder();

		builder.setH(Math.max(1, (int) (n * RandProperties.housekeepingRatio)));

		for (int i= 1; i <= n; i++ ) {
			int num= i;
			int type= RandProperties.roomDistribution.sample(1)[0];
			double quality= rejectionSample(RandProperties.qualitiesDistribution, 0, 1);
			int out= rejectionIntSample(RandProperties.checkout, RandProperties.checkoutLB, RandProperties.checkoutUB);
			int j= type - 1;
			int clean= rejectionIntSample(RandProperties.cleanTime[j], RandProperties.cleanTimeLB[j],
				RandProperties.cleanTimeUB[j]);
			builder.addRoom(new Room(num, type, quality, out, clean));
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
