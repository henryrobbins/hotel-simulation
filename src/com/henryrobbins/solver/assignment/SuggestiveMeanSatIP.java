package com.henryrobbins.solver.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.ampl.AMPL;
import com.henryrobbins.AMPLHelper;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** Room assignment solver using a suggestive IP. This solver iterates through guests as they
 * arrive. When a guest arrives, an IP suggestiveMeanSat.mod is run for that guest and all
 * unassigned rooms. The IP determines if a feasible optimal (maximum mean satisfaction) assignment
 * exists when that guest is assigned to that room. The constraint can be relaxed by an alpha value
 * in the range 0..1. If the assignment is feasible, that room is then added to a set of feasible
 * assignments. An assignment is then chosen from the feasible set. In practice, this could be
 * picked by a person but it is currently implemented to be random. */
public class SuggestiveMeanSatIP implements Solver<Assignment> {

	/** The relaxation constant for mean satisfaction (in 0..1) */
	private double alpha= 0.0;

	/** Construct a suggestive IP solver relaxed by alpha
	 *
	 * @param alpha The relaxation constant for mean satisfaction (in 0..1) */
	public SuggestiveMeanSatIP(double alpha) {
		if (alpha < 0 || alpha > 1) throw new IllegalArgumentException("Alpha not in range 0..1");
		this.alpha= alpha;
	}

	/** Return a room assignment chosen by finding all feasible room assignments maximizing mean
	 * satisfaction for a guest upon arrival and selecting one */
	@Override
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.rooms();
		ArrayList<Guest> guests= instance.guests();

		int[] flexiblitity= new int[guests.size()];
		HashMap<Guest, HashSet<Room>> flex= new HashMap<>();

		double average= AMPLHelper.runAssignmentIP(instance, "Mean_Satisfaction").satisfactionStats().getMean() * alpha;

		Collections.sort(guests, Comparator.comparingInt(Guest::arrival));

		AMPL ampl= AMPLHelper.createAMPL();

		for (Guest guest : guests) {

			// TODO: Constrain to room type

//			This process skewed satisfaction such that earlier arrivals were prioritized
//
//			ampl.reset();
//			AMPLHelper.uploadModel(ampl, "onlineSTAverage");
//			AMPLHelper.uploadData(ampl, instance);
//			ampl.getParameter("totalSatisfaction").set(totalSatisfaction);
//			ampl.getParameter("guest").set(Integer.toString(guest.getID()));
//
//			for (Room room : rooms) {
//				Guest g= assignment.getAssignment().get(room);
//				if (g != null) {
//					ampl.getVariable("assign").get(Integer.toString(room.getNumber()),
//						Integer.toString(g.getID())).fix(1.0);
//				}
//			}
//
//			ampl.solve();
//
//			double maxSatisfaction= ampl.getObjective("Guest_Satisfaction").value();
//
//			ampl.reset();

			HashSet<Room> feasible= new HashSet<>();

			for (Room room : rooms) {

				if (assignment.isRoomOpen(room) && room.type() >= guest.type()) {

					ampl.reset();
					AMPLHelper.uploadModel(ampl, "assignment");
					AMPLHelper.setObjectiveFunction(ampl, "Feasible");
					AMPLHelper.setRoomAndGuestParams(ampl, instance);
					ampl.getParameter("minMeanMatchingWeight").set(average);

					// Fix this guest to this room,
					ampl.getVariable("assign").get(Integer.toString(guest.id()), Integer.toString(room.num())).fix(1.0);

					// Fix all prior room assignments
					for (Guest g : guests) {
						if (assignment.isGuestAssigned(g)) {
							Room r= assignment.assignment().get(g);
							ampl.getVariable("assign").get(Integer.toString(g.id()),
								Integer.toString(r.num())).fix(1.0);
						}
					}

					ampl.solve();

					// If a feasible schedule exists, add this room to the list of feasible rooms
					if (ampl.getObjective("Feasible").result().equals("solved")) {
						feasible.add(room);
					}
				}
			}

			// Arbitrarily picks random feasible room to assign
			assignment.assign(guest, (Room) feasible.toArray()[new Random().nextInt(feasible.size())]);

			flex.put(guest, feasible);
			flexiblitity[guest.id() - 1]= feasible.size();

		}

		AMPLHelper.close(ampl);

		return assignment;

	}

	@Override
	public String toString() {
		return "Suggestive Algorithm";
	}

}
