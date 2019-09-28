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

	/** Return this room as a string with its information */
	@Override
	public String toString() {
		return "Room " + number + ": Type: " + type + ", Attributes: " + attributes.toString();
	}
}
