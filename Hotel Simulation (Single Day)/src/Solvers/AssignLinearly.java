package Solvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Hotel.Assignment;
import Hotel.Guest;
import Hotel.Instance;
import Hotel.Room;

public class AssignLinearly implements Solver {

	@Override
	/** Makes a valid room assignment by assigning guests to the lowest room type <br>
	 * possible upon their arrival and returns the Assignment */
	public Assignment solve(Instance instance) {

		Assignment assignment= new Assignment(instance);
		ArrayList<Room> rooms= instance.getRooms();
		ArrayList<Guest> guests= instance.getGuests();

		Collections.sort(guests, Comparator.comparingInt(Guest::getArrivalPosition));
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
