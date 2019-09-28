package RoomAssignmentSolvers;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.ampl.AMPL;
import com.ampl.DataFrame;

import AMPL.AMPLHelper;
import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;

public class PreserveEdgesMeanSat {

	public Object[] solve(RoomAssignment assignment, Instance instance) {

		Object[] ret= new Object[2];

		LinkedHashMap<Room, Guest> prevAssign= assignment.getAssignment();

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "presEdgesMeanSat");

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

		double[][] prev= new double[rSize][gSize];
		int n= 0;
		int m= 0;
		for (Room room : rooms) {
			m= 0;
			for (Guest guest : guests) {
				Guest assigned= prevAssign.get(room);
				if (assigned != null && assigned.equals(guest)) {
					prev[n][m]= 1;
				} else {
					prev[n][m]= 0;
				}
				m++ ;
			}
			n++ ;
		}

		DataFrame prevDF= new DataFrame(2, "ROOMS", "GUESTS", "prev");
		prevDF.setMatrix(prev, r, g);
		ampl.setData(prevDF);

		double[][] satisfaction= new double[rSize][gSize];
		n= 0;
		m= 0;
		for (Room room : rooms) {
			m= 0;
			for (Guest guest : guests) {
				satisfaction[n][m]= guest.getSatisfaction(room);
				m++ ;
			}
			n++ ;
		}

		DataFrame satisfactionDF= new DataFrame(2, "ROOMS", "GUESTS", "satisfaction");
		satisfactionDF.setMatrix(satisfaction, r, g);
		ampl.setData(satisfactionDF);

		int e= preserveEdges(assignment, instance);
		ret[1]= instance.getGuests().size() - 1 - e;
		ampl.getParameter("maxPreservedEdges").set(e);

		ampl.solve();

		ret[0]= AMPLHelper.generateAssignment(ampl, instance);
		return ret;

	}

	/** Given a room assignment from an instance and a new instance with an additional <br>
	 * feasible guest, this method returns the minimum number guest-room assignments <br>
	 * that must be changed in order to accommodate the new guest. Uses an IP
	 *
	 * @param assignment An assignment for the instance before the additional guest added
	 * @param instance   The instance the assignment is for with an additional guest */
	private static int preserveEdges(RoomAssignment assignment, Instance instance) {

		LinkedHashMap<Room, Guest> prevAssign= assignment.getAssignment();

		AMPL ampl= AMPLHelper.createAMPL();

		AMPLHelper.uploadModel(ampl, "preserveEdges");

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

		double[][] prev= new double[rSize][gSize];
		int n= 0;
		int m= 0;
		for (Room room : rooms) {
			m= 0;
			for (Guest guest : guests) {
				Guest assigned= prevAssign.get(room);
				if (assigned != null && assigned.equals(guest)) {
					prev[n][m]= 1;
				} else {
					prev[n][m]= 0;
				}
				m++ ;
			}
			n++ ;
		}

		DataFrame prevDF= new DataFrame(2, "ROOMS", "GUESTS", "prev");
		prevDF.setMatrix(prev, r, g);
		ampl.setData(prevDF);

		ampl.solve();

		return (int) ampl.getObjective("Preserved_Edges").value();
	}

}
