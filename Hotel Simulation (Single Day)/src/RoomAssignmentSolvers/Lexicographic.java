package RoomAssignmentSolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;

/** As guests arrive, this heuristic assigns guests to a room maximizing their satisfaction while
 * still being of the minimum type to satisfy their request. The heuristic operates independently of
 * future guest's satisfaction and assumes no knowledge of arrival times. */
public class Lexicographic implements AssignmentSolver {

	/** Assign guests to the room of the minimum type while maximizing their satisfaction */
	@Override
	public RoomAssignment solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		RoomAssignment assignment= new RoomAssignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalTime));
		for (Guest guest : guests) {
			Collections.sort(rooms, (a, b) -> guest.getMetPreferences(b) - guest.getMetPreferences(a));
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

		return assignment;

	}

	@Override
	public String toString() {
		return "Lexicographic";
	}

}
