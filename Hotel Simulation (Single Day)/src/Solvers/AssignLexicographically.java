package Solvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Hotel.Assignment;
import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;

public class AssignLexicographically implements Solver {

	@Override
	/** returns a valid Assignment by assigning guests to the room of the minimum <br>
	 * valid room type while fulfilling the most preferences upon guest arrival */
	public Assignment solve(Instance instance) {

		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalPosition));
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
		return "Lexico";
	}

}
