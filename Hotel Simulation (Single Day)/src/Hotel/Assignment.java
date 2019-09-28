package Hotel;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/** An instance maintains a feasible assignment of guests to hotel rooms for a given Instance <br>
 * Furthermore, it maintains a list of statistics for the assignment */
public class Assignment {

	/** a list of rooms (must have unique room numbers and fully accommodate guests) */
	private ArrayList<Room> rooms= new ArrayList<>();
	/** a list of guests (must have unique guestIDs and arrival positions) */
	private ArrayList<Guest> guests= new ArrayList<>();
	/** a dictionary of rooms and their assigned guest (null if unassigned) */
	private LinkedHashMap<Room, Guest> assignment= new LinkedHashMap<>();

	/** the total number of preferences met for the current assignment */
	private int metPreferences= 0;
	/** the minimum number of met preferences of a guest for the current room assignment */
	private int minimumPreferences= 0;
	/** the average satisfaction of guests for the current assignment */
	private double averageSatisfaction= 0.0;
	/** the minimum satisfaction of a guest for the current room assignment */
	private double minimumSatisfaction= 0.0;
	/** the total number of upgrades made in the current assignments */
	private int totalUpgrades= 0;

	/** Constructor: creates an empty assignment for the Instance (instance) */
	public Assignment(Instance instance) {
		rooms= instance.getRooms();
		guests= instance.getGuests();

		for (Room room : rooms) {
			assignment.put(room, null);
		}
	}

	/** return current assignment */
	public LinkedHashMap<Room, Guest> getAssignment() {
		return assignment;
	}

	// GETTERS FOR STATISTICS

	/** Returns the total met preferences for the current room assignment */
	public int getMetPreferences() {
		return metPreferences;
	}

	/** Returns the minimum number of preferences met for the current room assignment */
	public int getMinimumPreferences() {
		return minimumPreferences;
	}

	/** Returns the average satisfaction for the current room assignment */
	public double getAverageSatisfaction() {
		return averageSatisfaction;
	}

	/** Returns minimum satisfaction of a guest for the current room assignment */
	public double getMinimumSatisfaction() {
		return minimumSatisfaction;
	}

	/** Returns the total number of upgrades for the current room assignment */
	public int getTotalUpgrades() {
		return totalUpgrades;
	}

	/** Prints a report of statistics for current room assignments */
	public void printStats() {

		System.out.println("STATISTICS");
		System.out.println("Met Preferences: " + metPreferences);
		System.out.println("Minimum Met Preferences: " + minimumPreferences);
		System.out.println("Average Satisfaction: " + averageSatisfaction);
		System.out.println("Minimum Satisfaction: " + minimumSatisfaction);
		System.out.println("Total Upgrades: " + totalUpgrades);
		System.out.println();

	}

	/** Assigns the Guest guest to Room room and maintains the class invariant <br>
	 * for all of the statistics fields <br>
	 * Precondition: room has no guest and vice versa */
	public void assign(Room room, Guest guest) {
		assert assignment.get(room) == null;
		assert !assignment.containsValue(guest);

		metPreferences+= guest.getMetPreferences(room);
		averageSatisfaction+= guest.getSatisfaction(room) / guests.size();
		totalUpgrades+= room.getType() - guest.getType();

		if (assignmentEmpty()) {
			minimumSatisfaction= guest.getSatisfaction(room);
			minimumPreferences= guest.getMetPreferences(room);
		} else {
			minimumSatisfaction= Math.min(minimumSatisfaction, guest.getSatisfaction(room));
			minimumPreferences= Math.min(minimumPreferences, guest.getMetPreferences(room));
		}

		assignment.put(room, guest);
	}

	/** Returns true iff there are currently no guests assigned to a room */
	private boolean assignmentEmpty() {
		for (Room room : rooms) {
			if (assignment.get(room) != null) return false;
		}
		return true;
	}

	/** return true if room has no assigned Guest; false otherwise */
	public boolean isRoomOpen(Room room) {
		return assignment.get(room) == null;
	}

	/** return true if guest has been assigned to a Room; false otherwise */
	public boolean isGuestAssigned(Guest guest) {
		return assignment.containsValue(guest);
	}

	/** Resets the room assignments */
	public void reset() {
		assignment.clear();
		for (Room room : rooms) {
			assignment.put(room, null);
		}
		metPreferences= 0;
		minimumPreferences= 0;
		averageSatisfaction= 0.0;
		minimumSatisfaction= 0.0;
		totalUpgrades= 0;
	}

}
