package com.henryrobbins.solver.solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** This solver takes any housekeeping solver as an argument. A housekeeping schedule is chosen via
 * that housekeeping solver. As guests arrive, they are assigned to the first available room of the
 * minimum type to satisfy their request. This heuristic minimizes overlap for a given schedule */
public class FirstRoom implements Solver<Solution> {

	private Solver<Schedule> housekeepingSolver;

	public FirstRoom(Solver<Schedule> housekeepingSolver) {
		if (housekeepingSolver == null) throw new IllegalArgumentException("Housekeeping Solver was null");
		this.housekeepingSolver= housekeepingSolver;
	}

	/** Assign guests to the first available room as they arrive
	 *
	 * @param solver A housekeeping solver to generate a housekeeping schedule before <br>
	 *               beginning the room assignment heuristic (not null) */
	@Override
	public Solution solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Schedule schedule= housekeepingSolver.solve(instance);

		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.rooms();
		ArrayList<Guest> guests= instance.guests();

		Collections.sort(guests, Comparator.comparingInt(Guest::arrival));
		Collections.sort(rooms, new Comparator<Room>() {
			@Override
			public int compare(Room r1, Room r2) {
				return schedule.completion(r1) - schedule.completion(r2);
			}
		});

		for (Guest guest : guests) {
			int i= guest.type();
			while (!assignment.isGuestAssigned(guest)) {
				for (Room room : rooms) {
					if (assignment.isRoomOpen(room) && room.type() == i) {
						assignment.assign(guest, room);
						break;
					}
				}
				i++ ;
			}
		}

		return new Solution(instance, schedule, assignment);

	}

	@Override
	public String toString() {
		return "First Room";
	}

}
