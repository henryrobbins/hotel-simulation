package com.henryrobbins.hotel;

/** Maintains information about an immutable hotel room. */
public final class Room implements Comparable<Room> {

	/** Hotel room number (at least 1) */
	private final int number;
	/** Room type (at least 1) */
	private final int type;
	/** Intrinsic quality of this room (in [0,1]) */
	private final double quality;
	/** The time (interval) when the room is released to be cleaned (at least 0) <br>
	 * [In other words, the time interval in which the previous guest leaves] */
	private final int release;
	/** The time (in intervals) it takes housekeeping to process (clean) this room (at least 1) */
	private final int process;

	/** Construct a room with specified number, type, quality, and release and processing time
	 *
	 * @param number  This room's room number (at least 1)
	 * @param type    This room's type (at least 1)
	 * @param quality This room's intrinsic quality (in [0,1])
	 * @param release The time (interval) when the room is released to be cleaned (at least 0)
	 * @param process The time (in intervals) it takes housekeeping to process this room (at least 1) */
	public Room(int number, int type, double quality, int release, int process) {
		if (number < 1) throw new IllegalArgumentException("Room number less than 1");
		if (type < 1) throw new IllegalArgumentException("Room type less than 1");
		if (quality < 0) throw new IllegalArgumentException("Quality not in [0,1]");
		if (quality > 1) throw new IllegalArgumentException("Quality not in [0,1]");
		if (release < 0) throw new IllegalArgumentException("Release time was negative");
		if (process < 1) throw new IllegalArgumentException("Processing time less than 1");
		this.number= number;
		this.type= type;
		this.quality= quality;
		this.release= release;
		this.process= process;
	}

	/** Return this room's number */
	public int num() {
		return number;
	}

	/** Return this room's type */
	public int type() {
		return type;
	}

	/** Return this room's quality */
	public double quality() {
		return quality;
	}

	/** Return this room's release time */
	public int release() {
		return release;
	}

	/** Return the time to clean this room */
	public int process() {
		return process;
	}

	/** Return true iff the specified object is the same as this room */
	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Room.class) return false;
		Room room= (Room) ob;
		if (number != room.number) return false;
		if (type != room.type) return false;
		if (quality != room.quality) return false;
		if (release != room.release) return false;
		if (process != room.process) return false;
		return true;
	}

	/** Return this room's room number as a string */
	@Override
	public String toString() {
//		PREVIOUS IMPLEMENTATION
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
		return type - room.type();
	}
}