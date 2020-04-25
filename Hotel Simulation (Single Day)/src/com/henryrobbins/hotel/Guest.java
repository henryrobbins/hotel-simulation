package com.henryrobbins.hotel;

/** Maintains information about an immutable hotel guest. Here, hotel "guest" refers <br>
 * to a group of any size that will occupy a single room. EX: A family of four who <br>
 * will share a room would be represented by a single Guest object. */
public final class Guest implements Comparable<Guest> {

	/** Guest ID (at least 1) */
	private final int id;
	/** Requested room type (at least 1) */
	private final int type;
	/** Arrival time (interval) (at least 0) */
	private final int arrival;

	/** Construct a guest with the specified ID, requested room type, and arrival time
	 *
	 * @param id      This guest's ID (at least 1)
	 * @param type    The type of room this guest is requesting (at least 1)
	 * @param arrival The time (interval) in which this guest arrives (at least 0) */
	public Guest(int id, int type, int arrival) {
		if (id < 1) throw new IllegalArgumentException("Guest ID less than 1");
		if (type < 1) throw new IllegalArgumentException("Requested type less than 1");
		if (arrival < 0) throw new IllegalArgumentException("Arrival time less than 0");
		this.id= id;
		this.type= type;
		this.arrival= arrival;
	}

	/** Return this guest's ID */
	public int id() {
		return id;
	}

	/** Return the time interval in which this guest arrives */
	public int arrival() {
		return arrival;
	}

	/** Return this guest's requested room type */
	public int type() {
		return type;
	}

	/** Return true iff the specified object is the same as this guest */
	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Guest.class) return false;
		Guest guest= (Guest) ob;
		if (id != guest.id) return false;
		if (type != guest.type) return false;
		if (arrival != guest.arrival) return false;
		return true;
	}

	/** Return this guest's ID as a String */
	@Override
	public String toString() {
//		PREVIOUS IMPLEMENTATION
//		StringBuilder sb= new StringBuilder();
//		sb.append("Guest: " + id + ", ");
//		sb.append("Type: " + type + ", ");
//		sb.append("Arrival Time: " + arrivalTime + ", ");
//		sb.append("Preferences: " + preferences);
//		return sb.toString();
		return String.valueOf(id);
	}

	@Override
	public int compareTo(Guest g) {
		return id - g.id;
	}
}