package com.henryrobbins.solver.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** As guests arrive, this heuristic assigns guests to the first room of the minimum type to satisfy
 * their request. The heuristic assumes no knowledge of arrival times. */
public class Linear implements Solver<Assignment> {

	/** Assign guests to the first room of the minimum type */
	@Override
	public Assignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.rooms();
		ArrayList<Guest> guests= instance.guests();

		Collections.sort(guests, Comparator.comparingInt(Guest::arrival));
		Collections.sort(rooms, Comparator.comparingInt(Room::num));
		for (Guest guest : guests) {
			int min= assignment.getMinType(guest);
			for (Room room : rooms) {
				if (room.type() == min && assignment.isRoomOpen(room)) {
					assignment.assign(guest, room);
					break;
				}
			}
		}

		return assignment;

	}

	@Override
	public String toString() {
		return "Linear";
	}

}
