import java.util.ArrayList;
import java.util.HashSet;

/** An instance maintains information about a hotel room: room number and attributes <br>
 * Precondition: the room becomes available at some point during the simulated day */
public class Room implements Comparable<Room> {

	/** a unique room number (at least 1) */
	private int number;
	/** attributes of the room (empty if no attributes) */
	private ArrayList<String> attributes= new ArrayList<>();
	/** a set of all used room numbers */
	private static HashSet<Integer> roomNumbers= new HashSet<>();

	/** Constructor: a room with room number n and attributes a <br>
	 * Precondition: n is a unique room number and a is not null */
	public Room(int n, ArrayList<String> a) {
		assert !roomNumbers.contains(n);
		assert a != null;
		number= n;
		roomNumbers.add(n);
		attributes= a;
	}

	/** Return this room's number */
	public int getNumber() {
		return number;
	}

	/** Return this room's attributes */
	public ArrayList<String> getAttributes() {
		return attributes;
	}

	/** Reset current room numbers (for testing ) */
	public static void reset() {
		roomNumbers.clear();
	}

	/** Return this room as a string with its information */
	@Override
	public String toString() {
		return "Room " + number + ": Attributes: " + attributes.toString();
	}

	/** Allows for sorting by ascending room number */
	@Override
	public int compareTo(Room room) {
		return number - room.getNumber();
	}

}
