package Hotel;

import java.util.HashSet;

/** An instance maintains information about a hotel guest */
public class Guest {

	/** a guest ID (at least 1) */
	private int id;
	/** the position in which this guest arrives (at least 1) */
	private int position;
	/** the requested room type (at least 1) */
	private int type;
	/** the preferences of this guest (empty if none) */
	private HashSet<String> preferences= new HashSet<>();

	/** Constructor: a guest with ID id, arrival position p, requested room <br>
	 * type t, and preferences pref <br>
	 * Precondition: id > 0, p > 0, t > 0, pref is not null */
	public Guest(int id, int p, int t, HashSet<String> pref) {
		this.id= id;
		position= p;
		type= t;
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

	/** Return guest's requested room type */
	public int getType() {
		return type;
	}

	/** Return guest's preferences */
	public HashSet<String> getPreferences() {
		return preferences;
	}

	/** Return the total number of preferences the Room room meets for this guest */
	public int getMetPreferences(Room room) {
		int total= 0;
		for (String pref : preferences) {
			if (room.getAttributes().contains(pref)) {
				total++ ;
			}
		}
		return total;
	}

	/** Return the percent of preferences the Room room meets for this guest */
	public double getAverageSatisfaction(Room room) {

		if (preferences.size() == 0) { return 1; }

		int total= 0;
		for (String pref : preferences) {
			if (room.getAttributes().contains(pref)) {
				total++ ;
			}
		}
		return (double) total / preferences.size();
	}

	/** Checks to see if two guests have the same id, position, type, and attributes */
	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Guest.class) return false;
		Guest guest= (Guest) ob;
		if (id != guest.id) return false;
		if (position != guest.position) return false;
		if (type != guest.type) return false;
		if (!preferences.equals(guest.preferences)) return false;
		return true;
	}

	/** Return this guest as a string with its information */
	@Override
	public String toString() {
		return "Guest " + id + ": Type: " + type + ", Preferences: " + preferences.toString();
	}
}
