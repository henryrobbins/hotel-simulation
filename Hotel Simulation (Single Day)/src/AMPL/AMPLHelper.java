package AMPL;

import java.nio.file.Paths;
import java.util.ArrayList;

import com.ampl.AMPL;
import com.ampl.DataFrame;
import com.ampl.Environment;

import Hotel.Assignment;
import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;

/** Helper class that contains static methods used by various solvers */
public class AMPLHelper {

	/** Runs the IP called model on the given Instance and returns the Assignment */
	public static Assignment runIP(String model, Instance instance) {

		AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));
		ampl.setOption("solver", "gurobi");

		uploadModel(ampl, model);
		uploadData(ampl, instance);
		ampl.solve();

		return generateAssignment(ampl, instance);
	}

	/** returns the Assignment for the given instance associated with the current state <br>
	 * of 'assign' for the given instance of AMPL */
	public static Assignment generateAssignment(AMPL ampl, Instance instance) {

		Assignment assignment= new Assignment(instance);

		for (Room room : instance.getRooms()) {
			for (Guest guest : instance.getGuests()) {
				if (ampl.getVariable("assign").get(Integer.toString(room.getNumber()),
					Integer.toString(guest.getID())).value() == 1.0) {
					assignment.assign(room, guest);
					break;
				}
			}
		}

		return assignment;
	}

	/** Upload IP named (model) to the given instance of AMPL */
	public static void uploadModel(AMPL ampl, String model) {
		try {
			ampl.read(Paths.get("AMPL", "Models", model + ".mod").toString());
		} catch (Exception e) {
			ampl.close();
		}
	}

	/** Uploads standard data from instance to the given instance of AMPL */
	public static void uploadData(AMPL ampl, Instance instance) {

		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		DataFrame guestDF= new DataFrame(1, "GUESTS");

		int gSize= guests.size();
		String[] g= new String[gSize];
		int[] gType= new int[gSize];

		int i= 0;
		for (Guest guest : guests) {
			g[i]= Integer.toString(guest.getID());
			gType[i]= guest.getType();
			i++ ;
		}

		guestDF.setColumn("GUESTS", g);
		guestDF.addColumn("guestType", gType);
		ampl.setData(guestDF, "GUESTS");

		DataFrame roomDF= new DataFrame(1, "ROOMS");

		int rSize= rooms.size();
		String[] r= new String[rSize];
		int[] rType= new int[rSize];

		i= 0;
		for (Room room : rooms) {
			r[i]= Integer.toString(room.getNumber());
			rType[i]= room.getType();
			i++ ;
		}

		roomDF.setColumn("ROOMS", r);
		roomDF.addColumn("roomType", rType);
		ampl.setData(roomDF, "ROOMS");

		int[][] preferences= new int[rSize][gSize];
		int n= 0;
		int m= 0;
		for (Room room : rooms) {
			m= 0;
			for (Guest guest : guests) {
				preferences[n][m]= guest.getMetPreferences(room);
				m++ ;
			}
			n++ ;
		}

		DataFrame prefDF= new DataFrame(2, "ROOMS", "GUESTS", "metPreferences");
		prefDF.setMatrix(preferences, r, g);
		ampl.setData(prefDF);

		double[][] satisfaction= new double[rSize][gSize];
		n= 0;
		m= 0;
		for (Room room : rooms) {
			m= 0;
			for (Guest guest : guests) {
				satisfaction[n][m]= guest.getAverageSatisfaction(room);
				m++ ;
			}
			n++ ;
		}

		DataFrame satisfactionDF= new DataFrame(2, "ROOMS", "GUESTS", "satisfaction");
		satisfactionDF.setMatrix(satisfaction, r, g);
		ampl.setData(satisfactionDF);

	}

}
