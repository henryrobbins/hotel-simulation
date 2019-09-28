package Hotel;

import java.util.LinkedHashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/** Maintains a feasible assignment of guests to hotel rooms for a given instance. <br>
 * Furthermore, it maintains statistics for the assignment */
public class RoomAssignment {

	/** The instance that this room assignment is for */
	private Instance instance;
	/** The assignment of rooms to guests */
	private LinkedHashMap<Room, Guest> assignment= new LinkedHashMap<>();

	/** The set of met preferences for every guest */
	private DescriptiveStatistics metPreferences= new DescriptiveStatistics();
	/** the set of satisfactions for every guest */
	private DescriptiveStatistics satisfaction= new DescriptiveStatistics();
	/** the set of upgrades for every guest */
	private DescriptiveStatistics upgrades= new DescriptiveStatistics();

	/** Construct an empty assignment for the instance
	 *
	 * @param instance The instance that this room assignment is for (not null) */
	public RoomAssignment(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		this.instance= instance;
		for (Room room : instance.getRooms()) {
			assignment.put(room, null);
		}
	}

	/** Return the current room assignment */
	public LinkedHashMap<Room, Guest> getAssignment() {
		return assignment;
	}

	/** Return the sum of met preferences for the current room assignment */
	public int getMetPreferences() {
		return (int) metPreferences.getSum();
	}

	/** Return the minimum number of met preferences for the current room assignment */
	public int getMinimumPreferences() {
		return (int) metPreferences.getMin();
	}

	/** Return the mean guest satisfaction for the current room assignment */
	public double getAverageSatisfaction() {
		return satisfaction.getMean();
	}

	/** Return minimum guest satisfaction for the current room assignment */
	public double getMinimumSatisfaction() {
		return satisfaction.getMin();
	}

	/** Return the sum of upgrades for the current room assignment */
	public int getTotalUpgrades() {
		return (int) upgrades.getSum();
	}

	/** Prints a report of statistics for the current room assignment */
	public String getStatsReport() {
		StringBuilder sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT STATISTICS \n");
		sb.append(String.format("Met Preferences: %d \n", getMetPreferences()));
		sb.append(String.format("Minimum Met Preferences: %d \n", getMinimumPreferences()));
		sb.append(String.format("Average Satisfaction: %.4f \n", getAverageSatisfaction()));
		sb.append(String.format("Minimum Satisfaction: %.4f \n", getMinimumSatisfaction()));
		sb.append(String.format("Total Upgrades: %d \n", getTotalUpgrades()));
		return sb.toString();

	}

	/** Assigns the specified guest to the specified room
	 *
	 * @param room  The room the guest is assigned (The room is in this instance, satisfies <br>
	 *              the guest's requested type, and is not currently assigned a guest)
	 * @param guest The guest to be assigned (The guest is in this instance and <br>
	 *              yet to be assigned a room) */
	public void assign(Room room, Guest guest) {
		if (!instance.getRooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		if (!instance.getGuests().contains(guest)) throw new IllegalArgumentException("Guest not in instance");
		int upgrade= room.getType() - guest.getType();
		if (upgrade < 0) throw new IllegalArgumentException("Room type does not satisfy guest request");
		if (!isRoomOpen(room)) throw new IllegalArgumentException("Room already assigned");
		if (isGuestAssigned(guest)) throw new IllegalArgumentException("Guest already assigned");
		assignment.put(room, guest);
		metPreferences.addValue(guest.getMetPreferences(room));
		satisfaction.addValue(guest.getSatisfaction(room));
		upgrades.addValue(upgrade);
		assignment.put(room, guest);
	}

	/** Return true if this room assignment is for the given instance; false otherwise */
	public boolean isAssignmentFor(Instance instance) {
		return this.instance.equals(instance);
	}

	/** Return true if the room has no assigned guest; false otherwise
	 *
	 * @param room The room whose occupancy is to be tested (must be in instance) */
	public boolean isRoomOpen(Room room) {
		if (!instance.getRooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		return assignment.get(room) == null;
	}

	/** Return true if the guest has been assigned to a Room; false otherwise
	 *
	 * @param guest The guest whose assignment status is to be tested (must be in instance) */
	public boolean isGuestAssigned(Guest guest) {
		if (!instance.getGuests().contains(guest)) throw new IllegalArgumentException("Guest not in instance");
		return assignment.containsValue(guest);
	}

	/** Reset the room assignment leaving it empty */
	public void reset() {
		assignment.clear();
		for (Room room : instance.getRooms()) {
			assignment.put(room, null);
		}
		metPreferences= new DescriptiveStatistics();
		satisfaction= new DescriptiveStatistics();
		upgrades= new DescriptiveStatistics();
	}

	/** Return a string representing the room assignment */
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("-------------\n");
		sb.append("ROOM\t" + "GUEST" + "\n");
		for (Room room : assignment.keySet()) {
			sb.append(String.format("%-4s\t", room));
			if (assignment.get(room) != null) {
				sb.append(String.format("%-5s\n", assignment.get(room)));

			} else {
				sb.append("null \n");
			}
		}
		sb.append("-------------\n");
		return sb.toString();
	}
}