package HousekeepingSolvers;

import java.util.ArrayList;

import com.ampl.AMPL;
import com.ampl.DataFrame;

import AMPL.AMPLHelper;
import Hotel.Housekeeper;
import Hotel.HousekeepingSchedule;
import Hotel.Instance;
import Hotel.Room;

/** Housekeeping solver which uses a Positional & Assignment Variable IP (minAvailabilityPA.mod) to
 * minimize sum of the availability intervals for each room. Much slower than Time-Indexed IP. */
public class MinAvailabilityPAIP implements ScheduleSolver {

	/** Return the housekeeping schedule produced by the IP minAvailabilityPA.mod */
	@Override
	public HousekeepingSchedule solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= AMPLHelper.createAMPL();
		AMPLHelper.uploadModel(ampl, "minAvailabilityPA");

		ArrayList<Room> rooms= instance.getRooms();

		DataFrame roomDF= new DataFrame(1, "ROOMS");

		int rSize= rooms.size();
		String[] r= new String[rSize];
		int[] rType= new int[rSize];
		int[] checkOut= new int[rSize];
		int[] cleanTime= new int[rSize];

		int i= 0;
		for (Room room : rooms) {
			r[i]= Integer.toString(room.getNumber());
			rType[i]= room.getType();
			checkOut[i]= room.getCheckOut();
			cleanTime[i]= room.getCleanTime();
			i++ ;
		}

		roomDF.setColumn("ROOMS", r);
		roomDF.addColumn("roomType", rType);
		roomDF.addColumn("checkOut", checkOut);
		roomDF.addColumn("cleanTime", cleanTime);
		ampl.setData(roomDF, "ROOMS");

		DataFrame housekeeperDF= new DataFrame(1, "HOUSEKEEPERS");

		int n= instance.getNumOfHousekeepers();
		String[] id= new String[n];
		for (int j= 1; j <= n; j++ ) {
			id[j - 1]= String.valueOf(j);
		}

		housekeeperDF.setColumn("HOUSEKEEPERS", id);
		ampl.setData(housekeeperDF, "HOUSEKEEPERS");

		DataFrame orderDF= new DataFrame(1, "ORDER");

		int[] order= new int[rSize];
		for (int j= 1; j <= rSize; j++ ) {
			order[j - 1]= j;
		}

		orderDF.setColumn("ORDER", order);
		ampl.setData(orderDF, "ORDER");

		ampl.solve();

		HousekeepingSchedule schedule= new HousekeepingSchedule(instance);

		for (Housekeeper housekeeper : schedule.getHousekeepers()) {
			for (Object o : ampl.getSet("ORDER").toArray()) {
				for (Room room : rooms) {
					if (ampl.getVariable("assign")
						.get(Integer.toString(room.getNumber()), Integer.toString(housekeeper.getID()), o)
						.value() == 1.0) {
						int start= (int) ampl.getVariable("startTime").get(Integer.toString(housekeeper.getID()),
							o).value();
						schedule.add(housekeeper, room, start);
					}
				}
			}
		}

		return schedule;

	}

	@Override
	public String toString() {
		return "minAvailabilityPA IP";
	}

}
