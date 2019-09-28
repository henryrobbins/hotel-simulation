package Solvers;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import com.ampl.AMPL;
import com.ampl.Environment;

import AMPL.AMPLHelper;
import Hotel.Assignment;
import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;

public class suggestiveSTAverage implements Solver {

	private double alpha= 0.0;

	public suggestiveSTAverage(double alpha) {
		this.alpha= alpha;
	}

	@Override
	public Assignment solve(Instance instance) {

		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		int[] flexiblitity= new int[guests.size()];

		double average= AMPLHelper.runIP("maxAverageSatisfaction", instance).getAverageSatisfaction();
		double totalSatisfaction= average * guests.size() * alpha;

		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalPosition));

		AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));
		ampl.setOption("solver", "gurobi");

		for (Guest guest : guests) {

			// TODO: Constrain to room type

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

			double maxSatisfaction= ampl.getObjective("Guest_Satisfaction").value();

			ampl.reset();

			HashSet<Room> feasible= new HashSet<>();

			for (Room room : rooms) {

				if (assignment.getAssignment().get(room) == null &&
					guest.getAverageSatisfaction(room) >= maxSatisfaction) {

					ampl.reset();
					AMPLHelper.uploadModel(ampl, "suggestiveSTAverage");
					AMPLHelper.uploadData(ampl, instance);
					ampl.getParameter("totalSatisfaction").set(totalSatisfaction);

					ampl.getVariable("assign").get(Integer.toString(room.getNumber()),
						Integer.toString(guest.getID())).fix(1.0);

					for (Room r : rooms) {
						Guest g= assignment.getAssignment().get(room);
						if (g != null) {
							ampl.getVariable("assign").get(Integer.toString(r.getNumber()),
								Integer.toString(g.getID())).fix(1.0);
						}
					}

					ampl.solve();

					if (ampl.getObjective("feasible").result().equals("solved")) {
						feasible.add(room);
					}
				}
			}

			// Arbitrarily picks random feasible room to assign
			assignment.assign((Room) feasible.toArray()[new Random().nextInt(feasible.size())], guest);

			flexiblitity[guest.getID() - 1]= feasible.size();

		}

		ampl.reset();

		// TODO: (Temporary) will actually make an assignment in future implementation
		for (int i= 0; i < guests.size(); i++ ) {
			System.out.println(i + 1 + " " + flexiblitity[i]);
		}

		return assignment;
	}

	@Override
	public String toString() {
		return "Suggestive ST Avg";
	}

}
