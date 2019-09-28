package Solvers;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.ampl.AMPL;
import com.ampl.Environment;

import AMPL.AMPLHelper;
import Hotel.Assignment;
import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;

public class OnlineSTAverage implements Solver {

	@Override
	public Assignment solve(Instance instance) {

		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		double average= AMPLHelper.runIP("maxAverageSatisfaction", instance).getAverageSatisfaction();
		double totalSatisfaction= average * guests.size();

		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalPosition));

		AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));
		ampl.setOption("solver", "gurobi");

		for (Guest guest : guests) {

			ampl.reset();

			AMPLHelper.uploadModel(ampl, "onlineSTAverage");
			AMPLHelper.uploadData(ampl, instance);

			ampl.getParameter("totalSatisfaction").set(totalSatisfaction);
			ampl.getParameter("guest").set(Integer.toString(guest.getID()));

			for (Room room : rooms) {
				Guest g= assignment.getAssignment().get(room);
				if (g != null) {
					ampl.getVariable("assign").get(Integer.toString(room.getNumber()),
						Integer.toString(g.getID())).fix(1.0);
				}
			}

			ampl.solve();

			for (Room room : rooms) {
				if (ampl.getVariable("assign").get(Integer.toString(room.getNumber()),
					Integer.toString(guest.getID())).value() == 1.0) {
					assignment.assign(room, guest);
				}
			}
		}

		ampl.close();

		return assignment;

	}

	@Override
	public String toString() {
		return "Online ST Avg";
	}

}
