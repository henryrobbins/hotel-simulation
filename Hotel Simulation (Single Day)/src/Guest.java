import java.util.ArrayList;
import java.util.HashSet;

/** An instance maintains information about a hotel guest <br>
 * Precondition: this hotel guest arrives on the simulated day */
public class Guest implements Comparable<Guest> {

	/** a unique guest ID (at least 1) */
	private int id;
	/** the unique position in which this guest arrives (at least 1) */
	private int position;
	/** the preferences of this guest (empty if none) */
	private ArrayList<String> preferences= new ArrayList<>();
	/** a set of all used guest IDs */
	private static HashSet<Integer> guestIDs= new HashSet<>();
	/** a set of all used arrival positions */
	private static HashSet<Integer> arrivalPositions= new HashSet<>();

	/** Constructor: a guest with ID id, arrival position p, and preferences pref <br>
	 * Precondition: id and arrival position are unique, pref is not null */
	public Guest(int id, int p, ArrayList<String> pref) {
		assert !guestIDs.contains(id);
		assert !arrivalPositions.contains(p);
		assert pref != null;
		this.id= id;
		guestIDs.add(id);
		position= p;
		arrivalPositions.add(p);
		preferences= pref;
	}

	/** Return guest's ID */
	public int getID() {
		return id;
	}

	/** Return guest's arrival position */
	public int getArrivalPosition() {
		return position;
	}

	/** Return guest's preferences */
	public ArrayList<String> getPreferences() {
		return preferences;
	}

	/** Reset current guestIDs and arrivalPositions (for testing ) */
	public static void reset() {
		guestIDs.clear();
		arrivalPositions.clear();
	}

	/** Return the percent of preferences the Room room meets for this guest */
	public double getSatisfaction(Room room) {
		int prefMet= 0;
		for (String pref : preferences) {
			if (room.getAttributes().contains(pref)) {
				prefMet++ ;
			}
		}
		return (double) prefMet / (double) preferences.size();
	}

	/** Return the number of preferences the Room room meets for this guest */
	public int getMetPreferences(Room room) {
		int prefMet= 0;
		for (String pref : preferences) {
			if (room.getAttributes().contains(pref)) {
				prefMet++ ;
			}
		}
		return prefMet;
	}

	/** Return this guest as a string with its information */
	@Override
	public String toString() {
		return "Guest " + id + ": Preferences: " + preferences.toString();
	}

	/** Allows for sorting by arrival position */
	@Override
	public int compareTo(Guest guest) {
		return position - guest.position;
	}

}
