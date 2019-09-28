package Hotel;

import java.util.HashSet;

/** Maintains information about an individual guest. This includes an ID associated with <br>
 * the guest, the time interval in which they arrive, the type of room they are requesting, <br>
 * and a set of their preferences which correspond to hotel room attributes. */
public class Guest {

	/** This guest's ID (at least 1) */
	private int id;
	/** The room type this guest is requesting (at least 1) */
	private int type;
	/** The time interval in which this guest arrives (at least 0) */
	private int arrivalTime;
	/** This guest's set of preferences (not null - empty if no preferences) */
	private HashSet<String> preferences;

	/** Construct a guest with the specified ID, arrival time, requested room type, and preferences
	 *
	 * @param id          This guest's ID (id > 0)
	 * @param type        The type of room this guest is requesting (t > 0)
	 * @param arrivalTime The time interval in which this guest arrives (a >= 0)
	 * @param preferences This guest's set of preferences (not null - empty if no preferences) */
	public Guest(int id, int type, int arrivalTime, HashSet<String> preferences) {
		if (id < 1) throw new IllegalArgumentException("Guest ID less than 1");
		if (arrivalTime < 0) throw new IllegalArgumentException("Arrival time less than 0");
		if (type < 1) throw new IllegalArgumentException("Requested type less than 1");
		if (preferences == null) throw new IllegalArgumentException("The set of preferences was null");
		this.id= id;
		this.type= type;
		this.arrivalTime= arrivalTime;
		this.preferences= preferences;
	}

	/** Return this guest's ID */
	public int getID() {
		return id;
	}

	/** Return the time interval in which this guest arrives */
	public int getArrivalTime() {
		return arrivalTime;
	}

	/** Return this guest's requested room type */
	public int getType() {
		return type;
	}

	/** Return this guest's set of preferences */
	public HashSet<String> getPreferences() {
		return preferences;
	}

	/** Return the total number of preferences the room meets for this guest
	 *
	 * @param room A room in the hotel (not null)
	 * @return the number of met preferences */
	public int getMetPreferences(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		int total= 0;
		for (String pref : preferences) {
			if (room.getAttributes().contains(pref)) {
				total++ ;
			}
		}
		return total;
	}

	/** Return the percent of preferences the room meets for this guest. If the guest <br>
	 * has no preferences, the satisfaction is automatically 100%.
	 *
	 * @param room A room in the hotel (not null)
	 * @return the satisfaction of this guest with room */
	public double getSatisfaction(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		if (preferences.size() == 0) { return 1; }
		return (double) getMetPreferences(room) / (double) preferences.size();
	}

	/** Return the amount of overlap (wait time) between this guest and the given room <br>
	 * assuming that the provided housekeeping schedule is used.
	 *
	 * @param room     A room in the hotel (not null)
	 * @param schedule A housekeeping schedule (not null)
	 * @return the amount of overlap (wait time) */
	public int getOverlap(Room room, HousekeepingSchedule schedule) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		if (schedule == null) throw new IllegalArgumentException("Schedule is null");
		return Math.max(0, room.getAvailability(schedule) - arrivalTime);
	}

	/** Returns true if the specified object is the same as this guest */
	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Guest.class) return false;
		Guest guest= (Guest) ob;
		if (id != guest.id) return false;
		if (arrivalTime != guest.arrivalTime) return false;
		if (type != guest.type) return false;
		if (!preferences.equals(guest.preferences)) return false;
		return true;
	}

	/** Return this guest's ID as a String */
	@Override
	public String toString() {
//		Previous implementation
//		StringBuilder sb= new StringBuilder();
//		sb.append("Guest: " + id + ", ");
//		sb.append("Type: " + type + ", ");
//		sb.append("Arrival Time: " + arrivalTime + ", ");
//		sb.append("Preferences: " + preferences);
//		return sb.toString();
		return String.valueOf(id);
	}
}