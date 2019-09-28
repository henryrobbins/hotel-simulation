package Hotel;

import java.util.HashSet;

/** Maintains information about a hotel room. This includes the room number, room type, <br>
 * time interval in which the current guest checks out and the room becomes available <br>
 * to be cleaned by housekeeping, time it takes to clean this room, and a set of <br>
 * attributes which correspond to guest preferences. <br>
 * <br>
 * Note: this class has a natural ordering that is inconsistent with equals */
public class Room implements Comparable<Room> {

	/** This room's room number (at least 1) */
	private int number;
	/** This room's type (at least 1) */
	private int type;
	/** The time interval in which the current guest checks out (at least 0) */
	private int checkOut;
	/** The time it will take housekeeping to clean this room (at least 1) */
	private int cleanTime;
	/** The set of this room's attributes (not null - empty if no attributes) */
	private HashSet<String> attributes= new HashSet<>();

	/** Construct a room with specified number, type, check-out, clean time, and attributes.
	 *
	 * @param number     This room's room number (at least 1)
	 * @param type       This room's type (at least 1)
	 * @param checkOut   The time interval in which the current guest checks out (at least 0)
	 * @param cleanTime  The time it will take housekeeping to clean this room (at least 1)
	 * @param attributes The set of this room's attributes (not null - empty if no attributes) */
	public Room(int number, int type, int checkOut, int cleanTime, HashSet<String> attributes) {
		if (number < 1) throw new IllegalArgumentException("Room number less than 1");
		if (type < 1) throw new IllegalArgumentException("Room type less than 1");
		if (checkOut < 0) throw new IllegalArgumentException("Check out time was negative");
		if (cleanTime < 1) throw new IllegalArgumentException("Clean time less than 1");
		if (attributes == null) throw new IllegalArgumentException("The set of attributes was null");
		this.number= number;
		this.type= type;
		this.checkOut= checkOut;
		this.cleanTime= cleanTime;
		this.attributes= attributes;
	}

	/** Return this room's number */
	public int getNumber() {
		return number;
	}

	/** Return this room's type */
	public int getType() {
		return type;
	}

	/** Return this room's check out time */
	public int getCheckOut() {
		return checkOut;
	}

	/** Return the time to clean this room */
	public int getCleanTime() {
		return cleanTime;
	}

	/** Returns the first time interval this room is available for check-in after being <br>
	 * cleaned (assumes that the provided housekeeping schedule is used).
	 *
	 * @param schedule A housekeeping schedule (must contain this room which has been <br>
	 *                 assigned a housekeeper and thus, becomes available at some time)
	 * @return the time interval the room is first available */
	public int getAvailability(HousekeepingSchedule schedule) {
		if (schedule == null) throw new IllegalArgumentException("Schedule is null");
		Integer startTime= schedule.getStartTime(this);
		if (startTime == null)
			throw new IllegalArgumentException("Room has not been assigned a housekeeper");
		return startTime + cleanTime;
	}

	/** Return this room's set of attributes */
	public HashSet<String> getAttributes() {
		return attributes;
	}

	/** Returns true if the specified object is the same as this room */
	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Room.class) return false;
		Room room= (Room) ob;
		if (number != room.number) return false;
		if (type != room.type) return false;
		if (checkOut != room.checkOut) return false;
		if (cleanTime != room.cleanTime) return false;
		if (!attributes.equals(room.attributes)) return false;
		return true;
	}

	/** Return this room's room number as a string */
	@Override
	public String toString() {
//		Previous implementation
//		StringBuilder sb= new StringBuilder();
//		sb.append("Room: " + number + ", ");
//		sb.append("Type: " + type + ", ");
//		sb.append("Check-out: " + checkOut + ", ");
//		sb.append("Clean Time: " + cleanTime + ", ");
//		sb.append("Attributes: " + attributes);
//		return sb.toString();
		return String.valueOf(number);
	}

	/** Compares two rooms based on room type (with a higher room type being greater) */
	@Override
	public int compareTo(Room room) {
		return type - room.getType();
	}
}