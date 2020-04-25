package com.henryrobbins.hotel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/** Maintains information about a hotel housekeeper */
public class Housekeeper {

	/** Housekeeper's ID (at least 1) */
	private int id;
	/** Ordered list of rooms this housekeeper is assigned */
	private LinkedList<Room> schedule= new LinkedList<>();
	/** Map from assigned rooms to the time this housekeeper begins cleaning them */
	private HashMap<Room, Integer> start= new HashMap<>();
	/** Set of time intervals for which this housekeeper is currently scheduled */
	private HashSet<Integer> occupied= new HashSet<>();
	/** Makespan of the Housekeeper's individual schedule */
	private int makespan= 0;

	/** Construct a housekeeper with given ID and an empty schedule
	 *
	 * @param id This housekeeper's id (at least 1) */
	public Housekeeper(int id) {
		if (id < 1) throw new IllegalArgumentException("Housekeeper ID less than 1");
		this.id= id;
	}

	/** Return this housekeeper's ID */
	public int id() {
		return id;
	}

	/** Return a copy of the ordered list of rooms this housekeeper is assigned */
	public LinkedList<Room> getSchedule() {
		return new LinkedList<>(schedule);
	}

	/** Return the time this housekeeper begins cleaning the specified room <br>
	 *
	 * @param room A room (in this housekeeper's schedule) */
	public int getStartTime(Room room) {
		Integer startTime= start.get(room);
		if (startTime == null) {
			throw new IllegalArgumentException("Housekeeper not assigned this room");
		} else {
			return startTime;
		}
	}

	/** Return the makespan of this housekeeper's schedule */
	public int getMakespan() {
		return makespan;
	}

	/** Append the specified room to the housekeeper's schedule with soonest start time
	 *
	 * @param room The room to be appended (not null) */
	public boolean appendRoom(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		int start= Math.max(makespan, room.release()) + 1;
		addRoom(room, start);
		return true;
	}

	/** Add the specified room to the housekeeper's schedule with given start time <br>
	 * Return true if added successfully and false otherwise
	 *
	 * @param room      The room to be appended (not null)
	 * @param startTime The time that the room will begin to be cleaned */
	public boolean addRoom(Room room, int startTime) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		// Check if room has been released by this time
		if (startTime <= room.release()) return false;
		int endTime= startTime + room.process() - 1;
		// Check if housekeeper is available at every needed time interval
		for (int t= startTime; t <= endTime; t++ ) {
			if (occupied.contains(t)) return false;
		}
		if (schedule.size() == 0 || start.get(schedule.getLast()) < startTime) {
			schedule.add(room);
		} else {
			int i= 0;
			while (start.get(schedule.get(i)) < startTime) {
				i++ ;
			}
			schedule.add(i, room);
		}
		start.put(room, startTime);
		for (int t= startTime; t <= endTime; t++ ) {
			occupied.add(t);
		}
		makespan= Math.max(makespan, endTime);
		return true;
	}

	/** Return this housekeeper as a string with it's ID and schedule */
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("Housekeeper: " + id + ", Schedule: ");
		for (Room room : schedule) {
			sb.append(room.num() + ",");
		}
		sb= sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

}
