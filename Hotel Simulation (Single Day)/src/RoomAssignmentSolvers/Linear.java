package RoomAssignmentSolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;

/** As guests arrive, this heuristic assigns guests to the first room of the minimum type to satisfy
 * their request. The heuristic assumes no knowledge of arrival times. */
public class Linear implements AssignmentSolver {

	/** Assign guests to the first room of the minimum type */
	@Override
	public RoomAssignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		RoomAssignment assignment= new RoomAssignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalTime));
		Collections.sort(rooms, Comparator.comparingInt(Room::getType));
		for (Guest guest : guests) {
			for (Room room : rooms) {
				if (assignment.isRoomOpen(room) && room.getType() >= guest.getType()) {
					assignment.assign(room, guest);
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
