package HousekeepingSolvers;

import java.nio.file.Paths;
import java.util.ArrayList;

import com.ampl.AMPL;
import com.ampl.DataFrame;
import com.ampl.Environment;

import AMPL.AMPLHelper;
import Hotel.Guest;
import Hotel.HousekeepingSchedule;
import Hotel.Instance;
import Hotel.Room;

/** Housekeeping solver which uses a Time-Indexed IP (noOverlapFeasibility.mod) to determine if a
 * schedule with no overlap is feasible. It's objective function is arbitrary. */
public class NoOverlapFeasibilityIP implements ScheduleSolver {

	/** Return the housekeeping schedule produced by the IP noOverlapFeasibility.mod */
	@Override
	public HousekeepingSchedule solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));
		ampl.setOption("solver", "gurobi");

		AMPLHelper.uploadModel(ampl, "noOverlapFeasibility");
		AMPLHelper.uploadRoomAndGuestData(ampl, instance);
		AMPLHelper.uploadHousekeepingData(ampl, instance);

		int maxMakespan= 0;
		int maxCheckOut= 0;
		for (Room room : rooms) {
			maxMakespan+= room.getCleanTime();
			maxCheckOut= Math.max(maxCheckOut, room.getCheckOut());
		}
		maxMakespan+= maxCheckOut;
		Double[] time= new Double[maxMakespan + 1];
		for (int j= 0; j <= maxMakespan; j++ ) {
			time[j]= Double.valueOf(j);
		}

		Double[][] numOfType= new Double[maxMakespan + 1][8];
		for (int i= 0; i <= maxMakespan; i++ ) {
			for (int j= 0; j < 8; j++ ) {
				numOfType[i][j]= 0.0;
			}
		}

		for (Guest guest : guests) {
			int checkin= guest.getArrivalTime();
			for (int t= checkin; t <= maxMakespan; t++ ) {
				numOfType[t][guest.getType() - 1]++ ;
			}
		}

		for (int i= 0; i <= maxMakespan; i++ ) {
			for (int j= 0; j < 8; j++ ) {
			}
		}

		DataFrame numNeeded= new DataFrame(2, "TIME", "TYPE",
			"numberNeeded");
		numNeeded.setMatrix(numOfType, time, new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0 });
		ampl.setData(numNeeded);

		ampl.solve();

		return AMPLHelper.generateSchedule(ampl, instance);

	}

	@Override
	public String toString() {
		return "noOverlapFeasibility IP";
	}

}
