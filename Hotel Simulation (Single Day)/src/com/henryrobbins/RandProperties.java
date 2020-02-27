package com.henryrobbins;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

public class RandProperties {

	/** The number of minutes each time interval represents */
	public final static double TIME_INTERVAL;

	/** The ratio of guests to rooms */
	public final static double avgCapacity;
	/** The ratio of housekeepers to rooms */
	public final static double housekeepingRatio;

	/** The set of room types */
	public final static int roomTypes;
	/** The distribution of room types */
	public final static EnumeratedIntegerDistribution roomDistribution;
	/** The distribution of room type requests */
	public final static EnumeratedIntegerDistribution requestDistribution;

	/** The distribution of room intrinsic qualities */
	public final static NormalDistribution qualitiesDistribution;
	/** The distribution of guest pickiness */
	public final static BetaDistribution ubSatDistribution;
	/** The distribution of guest pickiness */
	public final static BetaDistribution lbSatDistribution;
	/** The standard deviation for distribution of guest satisfaction (no upgrade) */
	public final static double randSat;
	/** The increase to mean for distribution of guest satisfaction for every upgrade */
	public final static double upgradeBonus;

	/** The distribution of cleaning times based on room type */
	public final static NormalDistribution[] cleanTime;
	/** The lower bound of cleaning time based on room type */
	public final static int[] cleanTimeLB;
	/** The upper bound of cleaning time based on room type */
	public final static int[] cleanTimeUB;

	/** The distribution of check-out times */
	public final static NormalDistribution checkout;
	/** The lower bound check-out time */
	public final static int checkoutLB;
	/** The upper bound check-out time */
	public final static int checkoutUB;

	/** The distribution of check-in times */
	public final static NormalDistribution checkin;
	/** The lower bound check-in time */
	public final static int checkinLB;
	/** The upper bound check-in time */
	public final static int checkinUB;

	static {
		// TODO: Not high production quality...

		Properties randParam= new Properties();
		try {
			InputStream is= new FileInputStream("randParam.properties");
			randParam.load(is);
			is.close();
		} catch (Exception e) {
			throw new IllegalArgumentException("Issue with properties file");
		}

		TIME_INTERVAL= Integer.parseInt((String) randParam.get("timeInterval"));
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

		double qualityMean= Double.parseDouble((String) randParam.get("qualityMean"));
		double qualityStd= Double.parseDouble((String) randParam.get("qualityStd"));
		qualitiesDistribution= new NormalDistribution(qualityMean, qualityStd);

		double ubBetaA= Double.parseDouble((String) randParam.get("ubBetaA"));
		double ubBetaB= Double.parseDouble((String) randParam.get("ubBetaB"));
		ubSatDistribution= new BetaDistribution(ubBetaA, ubBetaB);

		double lbBetaA= Double.parseDouble((String) randParam.get("lbBetaA"));
		double lbBetaB= Double.parseDouble((String) randParam.get("lbBetaB"));
		lbSatDistribution= new BetaDistribution(lbBetaA, lbBetaB);

		randSat= Double.parseDouble((String) randParam.get("randSat"));
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
}