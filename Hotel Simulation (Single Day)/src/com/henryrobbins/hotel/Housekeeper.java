package com.henryrobbins.hotel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/** Maintains information about a hotel housekeeper and their individual schedule. <br>
 * Their schedule consists of an ordered list of rooms they are assigned and a set of <br>
 * times they will start cleaning each assigned room respectively */
public class Housekeeper {

	/** This housekeeper's ID (at least 1) */
	private int id;
	/** The ordered list of rooms this housekeeper is assigned */
	private LinkedList<Room> schedule= new LinkedList<>();
	/** The time every room the housekeeper is assigned begins getting cleaned. <br>
	 * These times should be consistent with the order of rooms in the schedule. */
	private HashMap<Room, Integer> startTimes= new HashMap<>();
	/** The set of time intervals for which this housekeeper is currently scheduled */
	private HashSet<Integer> scheduledTime= new HashSet<>();
	/** The makespan of the Housekeeper's schedule */
	private int makespan= 0;

	/** Construct a housekeeper with ID and empty schedule
	 *
	 * @param id This housekeeper's id (at least 1) */
	public Housekeeper(int id) {
		if (id < 1) throw new IllegalArgumentException("Housekeeper ID less than 1");
		this.id= id;
	}

	/** Returns this housekeeper's ID */
	public int getID() {
		return id;
	}

	/** Returns the ordered list of rooms this housekeeper is assigned */
	public LinkedList<Room> getSchedule() {
		return schedule;
	}

	/** Return the time this housekeeper begins cleaning the specified room
	 *
	 * @param room A room (must be in this housekeeper's schedule) */
	public int getStartTime(Room room) {
		if (!startTimes.containsKey(room))
			throw new IllegalArgumentException("Housekeeper not assigned this room");
		return startTimes.get(room);
	}

	/** Return the makespan of this housekeeper's schedule */
	public int getMakespan() {
		return makespan;
	}

	/** Append the specified room to the housekeeper's schedule with soonest start time
	 *
	 * @param room The room to be appended (not null) */
	public void appendRoom(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		int start= Math.max(makespan, room.release()) + 1;
		addRoom(room, start);
	}

	/** Add the specified room to the housekeeper's schedule with given start time
	 *
	 * @param room      The room to be appended (not null)
	 * @param startTime The time that the room will be cleaned (greater than room checkout <br>
	 *                  time and does not overlap with other cleaning intervals for other rooms) */
	public void addRoom(Room room, int startTime) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		if (startTime <= room.release()) throw new IllegalArgumentException("Room not checked out");
		int endTime= startTime + room.process() - 1;
		for (int t= startTime; t <= endTime; t++ ) {
			if (scheduledTime.contains(t)) throw new IllegalArgumentException("Housekeeper already scheduled");
		}
		if (schedule.size() == 0 || startTimes.get(schedule.getLast()) < startTime) {
			schedule.add(room);
		} else {
			int i= 0;
			while (startTimes.get(schedule.get(i)) < startTime) {
				if (i + 1 < schedule.size()) {
					i++ ;
				}
			}
			schedule.add(i, room);
		}
		startTimes.put(room, startTime);
		for (int t= startTime; t <= endTime; t++ ) {
			scheduledTime.add(t);
		}
		makespan= Math.max(makespan, endTime);
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
