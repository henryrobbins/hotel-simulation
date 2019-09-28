package Solvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Hotel.Guest;
import Hotel.HousekeepingSchedule;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;
import Hotel.Solution;
import HousekeepingSolvers.ScheduleSolver;

/** This solver takes any housekeeping solver as an argument. A housekeeping schedule is chosen via
 * that housekeeping solver. As guests arrive, they are assigned to the first available room of the
 * minimum type to satisfy their request. This heuristic minimizes overlap for a given schedule */
public class FirstRoom implements Solver {

	private ScheduleSolver housekeepingSolver;

	public FirstRoom(ScheduleSolver housekeepingSolver) {
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

		HousekeepingSchedule schedule= housekeepingSolver.solve(instance);

		RoomAssignment assignment= new RoomAssignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalTime));
		Collections.sort(rooms, new Comparator<Room>() {
			@Override
			public int compare(Room r1, Room r2) {
				return r1.getAvailability(schedule) - r2.getAvailability(schedule);
			}
		});

		for (Guest guest : guests) {
			int i= guest.getType();
			while (!assignment.isGuestAssigned(guest)) {
				for (Room room : rooms) {
					if (assignment.isRoomOpen(room) && room.getType() == i) {
						assignment.assign(room, guest);
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
