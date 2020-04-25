package com.henryrobbins.decision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Housekeeper;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;

/** Maintains a housekeeping schedule for a given instance. */
public class Schedule implements Decision {

	/** The instance that the housekeeping schedule is for */
	private Instance instance;
	/** Set of housekeepers whose size is consistent with the size in the instance */
	private ArrayList<Housekeeper> housekeepers;
	/** Map from rooms to the time they begin getting cleaned */
	private HashMap<Room, Integer> startTimes;
	/** Map from rooms to the housekeeper that is assigned to clean them */
	private HashMap<Room, Housekeeper> assign;

	// MAINTAINS STATISTICS
	/** Set of finishing times for every assigned room */
	private DescriptiveStatistics completionStats= new DescriptiveStatistics();
	/** The number of rooms available at every relevant time interval */
	private int[] roomsAvailable;

	/** Construct an empty housekeeping schedule for the instance
	 *
	 * @param inst The instance that this housekeeping schedule is for (not null) */
	public Schedule(Instance inst) {
		if (inst == null) throw new IllegalArgumentException("Instance is null");
		instance= inst;
		housekeepers= new ArrayList<>();
		for (int i= 1; i <= instance.getH(); i++ ) {
			housekeepers.add(new Housekeeper(i));
		}
		startTimes= new HashMap<>();
		assign= new HashMap<>();
		for (Room room : instance.rooms()) {
			startTimes.put(room, null);
			assign.put(room, null);
		}
	}

	/** Construct a copy of the given Schedule */
	public Schedule(Schedule schedule) {
		instance= new Instance.Builder(schedule.instance).build();
		housekeepers= new ArrayList<>(schedule.housekeepers);
		startTimes= new HashMap<>(schedule.startTimes);
		assign= new HashMap<>(schedule.assign);
		completionStats= new DescriptiveStatistics(schedule.completionStats);
		roomsAvailable= Arrays.copyOf(schedule.roomsAvailable, schedule.roomsAvailable.length);
	}

	/** Return the list of housekeepers */
	public ArrayList<Housekeeper> getHousekeepers() {
		return housekeepers;
	}

	/** Return the housekeeper assigned to the given room
	 *
	 * @param The room whose housekeeper is in question (room must be in instance) */
	public Housekeeper getAssignment(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		if (!instance.rooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		return assign.get(room);
	}

	/** Return the completion time of the given room */
	public int completion(Room room) {
		return startTimes.get(room) + room.process() - 1;
	}

	/** Return the statistics for completions */
	public DescriptiveStatistics completionStats() {
		return new DescriptiveStatistics(completionStats);
	}

	/** Return the makespan of the schedule */
	public int makespan() {
		return (int) completionStats.getMax();
	}

	/** Return the number of rooms available at every relevant time interval */
	public int[] getRoomsAvailable() {
		return roomsAvailable;
	}

	/** Return the number of rooms available at time interval t */
	public int getRoomsAvailableAt(int t) {
		return roomsAvailable[t];
	}

	/** Return the lateness of the given room if assigned the given guest. Return null if <br>
	 * the given room has not yet been assigned a start time in this schedule.
	 *
	 * @param room  A hotel room (in the instance this is a schedule for)
	 * @param guest A guest (in the instance this is a schedule for) */
	public Integer latenessOf(Guest guest, Room room) {
		Integer start= startTimes.get(room);
		if (start == null) throw new IllegalArgumentException("This room was not assigned a housekeeper");
		return start + room.process() - guest.arrival();
	}

	/** Return the tardiness of the given room if assigned the given guest. Return null if <br>
	 * the given room has not yet been assigned a start time in this schedule.
	 *
	 * @param room  A hotel room (in the instance this is a schedule for)
	 * @param guest A guest (in the instance this is a schedule for) */
	public Integer tardinessOf(Guest guest, Room room) {
		return Math.max(0, latenessOf(guest, room));
	}

	/** Calculates the number of rooms available at every relevant time interval. <br>
	 * Relevant time intervals include all intervals leading up to and including the <br>
	 * interval in which all rooms are cleaned and available for check in. */
	private void calculateRoomsAvailable() {
		int[] roomsAvailable= new int[makespan() + 2];
		for (int i= 0; i < makespan() + 2; i++ ) {
			roomsAvailable[i]= calculateRoomsAvailableAt(i);
		}
		this.roomsAvailable= roomsAvailable;

	}

	/** Return the number of rooms available at a given time interval */
	private int calculateRoomsAvailableAt(int t) {
		int counter= 0;
		for (Room room : startTimes.keySet()) {
			Integer startTime= startTimes.get(room);
			if (startTime != null && startTime + room.process() - 1 < t) { counter++ ; }
		}
		return counter;
	}

	/** Return a report of statistics for the current housekeeping schedule */
	public String getStatsReport() {
		StringBuilder sb= new StringBuilder();
		sb.append("HOUSEKEEPING STATISTICS\n");
		sb.append("Completion " + completionStats + "\n");
		sb.append("Rooms Available: " + Arrays.toString(getRoomsAvailable()) + "\n");
		return sb.toString();
	}

	/** Append the room to the given housekeeper's cleaning schedule at the soonest start time. <br>
	 * Return true if added to housekeeper's schedule successfully; false otherwise
	 *
	 * @param housekeeper The housekeeper who will be assigned this room (in the set of housekeepers)
	 * @param room        The room to be assigned (in the instance and not yet assigned) */
	public boolean append(Housekeeper housekeeper, Room room) {
		int start= Math.max(housekeeper.getMakespan(), room.release()) + 1;
		return add(housekeeper, room, start);
	}

	/** Add the room to the given housekeeper's cleaning schedule at the given start time <br>
	 * Return true if added to housekeeper's schedule successfully; false otherwise
	 *
	 * @param housekeeper The housekeeper who will be assigned this room (in the set of housekeepers)
	 * @param room        The room to be assigned (in the instance)
	 * @param start       The time the housekeeper will begin cleaning this room */
	public boolean add(Housekeeper housekeeper, Room room, int start) {
		if (!housekeepers.contains(housekeeper))
			throw new IllegalArgumentException("Schedule does not contain this housekeeper");
		if (!instance.rooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		if (assign.get(room) != null) return false;
		if (housekeeper.addRoom(room, start)) {
			assign.put(room, housekeeper);
			startTimes.put(room, start);
			completionStats.addValue(start + room.process() - 1);
			calculateRoomsAvailable();
			return true;
		} else {
			return false;
		}
	}

	/** Return true if every room has been assigned to a housekeeper; false otherwise */
	public boolean isValid() {
		for (Room room : startTimes.keySet()) {
			if (startTimes.get(room) == null) return false;
		}
		return true;
	}

	/** Return true if this housekeeping schedule is for the given instance; false otherwise */
	public boolean isScheduleFor(Instance instance) {
		return this.instance.equals(instance);
	}

	/** Reset the housekeeping schedule leaving it empty */
	public void reset() {
		housekeepers= new ArrayList<>();
		int h= instance.getH();
		for (int i= 1; i <= h; i++ ) {
			housekeepers.add(new Housekeeper(i));
		}
		startTimes= new HashMap<>();
		for (Room room : instance.rooms()) {
			startTimes.put(room, null);
		}

		completionStats= new DescriptiveStatistics();
		roomsAvailable= new int[] {};
	}

	/** Return a visual representation of the housekeeping schedule */
	public String getVisual() {
		StringBuilder sb= new StringBuilder();
		int minT= makespan() + 1;
		for (Room room : instance.rooms()) {
			minT= Math.min(minT, room.release());
		}
		int maxT= makespan() + 1;
		sb.append("#   ");
		for (int t= minT; t <= maxT; t++ ) {
			sb.append(String.format("%-4d", t));
		}
		sb.append("\n");
		for (Room room : instance.rooms()) {
			sb.append(String.format("%-4d", room.num()));
			for (int t= minT; t <= maxT; t++ ) {
				if (t <= room.release()) {
					sb.append(String.format("%-4s", "X"));
				} else {
					Integer start= startTimes.get(room);
					if (start != null && t >= start && t < start + room.process()) {
						sb.append(String.format("%-4d", assign.get(room).id()));
					} else {
						sb.append(String.format("%-4s", " "));
					}
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Schedule.class) return false;
		Schedule schedule= (Schedule) ob;
		if (!instance.equals(schedule.instance)) return false;
		if (!housekeepers.equals(schedule.housekeepers)) return false;
		if (!startTimes.equals(schedule.startTimes)) return false;
		if (!assign.equals(schedule.assign)) return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("--------------------------\n");
		sb.append("HOUSEKEEPER\t" + "SCHEDULE\n");
		for (Housekeeper housekeeper : housekeepers) {
			sb.append(String.format("%-12d\t", housekeeper.id()));
			sb.append(housekeeper.getSchedule() + "\n");
		}
		sb.append("--------------------------\n");
		return sb.toString();
	}
}