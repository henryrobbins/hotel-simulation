package RoomAssignmentSolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.ampl.AMPL;

import AMPL.AMPLHelper;
import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;

/** Room assignment solver using an online IP. This solver iterates through guests as they arrive.
 * When a guest arrives, an IP onlineMeanSat.mod is run which maximizes that guest's satisfaction
 * subject to previous assignments and a future optimal assignment. Optimal as defined by a maximum
 * mean satisfaction. Guests with earlier arrivals are more satisfied than those arriving later. */
public class OnlineMeanSatIP implements AssignmentSolver {

	/** Return a room assignment by maximizing guest's satisfaction as they arrive while maintaining an
	 * optimal future assignment that maximizes mean satisfaction */
	@Override
	public RoomAssignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= AMPLHelper.createAMPL();
		RoomAssignment assignment= new RoomAssignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		// Get optimal mean satisfaction
		double average= AMPLHelper.runAssignmentIP(instance, "Mean_Satisfaction").getAverageSatisfaction();

		// Iterate through guests by arrival time
		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalTime));
		for (Guest guest : guests) {

			ampl.reset();

			// This model maximizes a given guest's satisfaction subject to a given mean satisfaction
			AMPLHelper.uploadModel(ampl, "roomAssignment");
			AMPLHelper.setObjectiveFunction(ampl, "Guest_Satisfaction");
			AMPLHelper.uploadRoomAndGuestData(ampl, instance);

			// Set constraint for average satisfaction
			ampl.getParameter("meanSatisfaction").set(average);
			// Set the currently arriving guest as 'guest'
			ampl.getParameter("guest").set(Integer.toString(guest.getID()));

			// Fix the decision variables for all prior guest assignments
			for (Room room : rooms) {
				Guest g= assignment.getAssignment().get(room);
				if (g != null) {
					ampl.getVariable("assign").get(Integer.toString(room.getNumber()),
						Integer.toString(g.getID())).fix(1.0);
				}
			}

			ampl.solve();

			// Assign the guest to the room maximizing their satisfaction
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
		return "Online Algorithm";
	}

}
