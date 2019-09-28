package Hotel;

import java.util.HashSet;

/** An instance maintains information about a hotel room */
public class Room {

	/** a room number (at least 1) */
	private int number;
	/** a room type (at least 1) */
	private int type;
	/** attributes of the room (empty if no attributes) */
	private HashSet<String> attributes= new HashSet<>();

	/** Constructor: a room with room number n, type t, and attributes a <br>
	 * Precondition: n > 0, t > 0, and a is not null */
	public Room(int n, int t, HashSet<String> a) {
		number= n;
		type= t;
		attributes= a;
	}

	/** Return this room's number */
	public int getNumber() {
		return number;
	}

	/** Return this room's type */
	public int getType() {
		return type;
	}

	/** Return this room's attributes */
	public HashSet<String> getAttributes() {
		return attributes;
	}

	/** Checks to see if two rooms have the same number, type, and attributes */
	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Room.class) return false;
		Room room= (Room) ob;
		if (number != room.number) return false;
		if (type != room.type) return false;
		if (!attributes.equals(room.attributes)) return false;
		return true;
	}

	/** Return this room as a string with its information */
	@Override
	public String toString() {
		return "Room " + number + ": Type: " + type + ", Attributes: " + attributes.toString();
	}
}
