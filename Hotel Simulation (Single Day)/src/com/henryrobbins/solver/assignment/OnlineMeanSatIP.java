package com.henryrobbins.solver.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** Room assignment solver using an online IP. This solver iterates through guests as they arrive.
 * When a guest arrives, an IP onlineMeanSat.mod is run which maximizes that guest's satisfaction
 * subject to previous assignments and a future optimal assignment. Optimal as defined by a maximum
 * mean satisfaction. Guests with earlier arrivals are more satisfied than those arriving later. */
public class OnlineMeanSatIP implements Solver<Assignment> {

	/** Return a room assignment by maximizing guest's satisfaction as they arrive while maintaining an
	 * optimal future assignment that maximizes mean satisfaction */
	@Override
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= AMPLHelper.createAMPL();
		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.rooms();
		ArrayList<Guest> guests= instance.guests();

		// Get optimal mean satisfaction
		double average= AMPLHelper.runAssignmentIP(instance, "Mean_Satisfaction").satisfactionStats().getMean();

		// Iterate through guests by arrival time
		Collections.sort(guests, Comparator.comparingInt(Guest::arrival));
		for (Guest guest : guests) {

			ampl.reset();

			// This model maximizes a given guest's satisfaction subject to a given mean satisfaction
			AMPLHelper.uploadModel(ampl, "assignment");
			AMPLHelper.setObjectiveFunction(ampl, "Guest_Satisfaction");
			AMPLHelper.setRoomAndGuestParams(ampl, instance);

			// Set constraint for average satisfaction
			ampl.getParameter("minMeanMatchingWeight").set(average);
			// Set the currently arriving guest as 'guest'
			ampl.getParameter("guest").set(Integer.toString(guest.id()));

			// Fix the decision variables for all prior guest assignments
			for (Room room : rooms) {
				Guest g= assignment.assignment().getKey(room);
				if (g != null) {
					ampl.getVariable("assign").get(Integer.toString(g.id()),
						Integer.toString(room.num())).fix(1.0);
				}
			}

			ampl.solve();

			// Assign the guest to the room maximizing their satisfaction
			for (Room room : rooms) {
				if (ampl.getVariable("assign").get(Integer.toString(guest.id()),
					Integer.toString(room.num())).value() == 1.0) {
					assignment.assign(guest, room);
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
