package com.henryrobbins.decision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Housekeeper;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;

/** Maintains a feasible housekeeping schedule for a given instance. <br>
 * Furthermore, it maintains statistics for the schedule */
public class Schedule implements Decision {

	/** The instance that the housekeeping schedule is for */
	private Instance instance;
	/** The set of housekeepers whose size is consistent with the size in the instance */
	private ArrayList<Housekeeper> housekeepers;
	/** The time every assigned room begins getting cleaned. null if unassigned */
	private HashMap<Room, Integer> startTimes;
	/** The housekeeper assigned to each room. null if unassigned */
	private HashMap<Room, Housekeeper> assign;

	/** The set of finishing times for every assigned room */
	private DescriptiveStatistics completionStats= new DescriptiveStatistics();
	/** The set of makespans for every housekeeper */
	private int makespan= 0;
	/** The number of rooms available at every relevant time interval */
	private int[] roomsAvailable= {};

	/** Construct an empty housekeeping schedule for the instance
	 *
	 * @param inst The instance that this housekeeping schedule is for (not null) */
	public Schedule(Instance inst) {
		if (inst == null) throw new IllegalArgumentException("Instance is null");
		instance= inst;
		housekeepers= new ArrayList<>();
		for (int i= 1; i <= instance.teamSize(); i++ ) {
			housekeepers.add(new Housekeeper(i));
		}
		startTimes= new HashMap<>();
		assign= new HashMap<>();
		for (Room room : instance.rooms()) {
			startTimes.put(room, null);
			assign.put(room, null);
		}
	}

	/** Return the list of housekeepers */
	public ArrayList<Housekeeper> getHousekeepers() {
		return housekeepers;
	}

//	/** Return the first time interval the specified room is processed. If the room has yet <br>
//	 * to be assigned a time to be processed, returns null.
//	 *
//	 * @param room The room whose start time is in question (room must be cleaned in schedule)
//	 * @return The first time interval the specified room is processed */
//	public Integer startOf(Room room) {
//		if (room == null) throw new IllegalArgumentException("Room is null");
//		if (!instance.rooms().contains(room))
//			throw new IllegalArgumentException("The room is not in the instance for which this schedule pertains");
//		return startTimes.get(room);
//	}

	/** Return the housekeeper assigned to the given room
	 *
	 * @param The room whose housekeeper is in question (room must be in instance)
	 * @return The housekeeper cleaning this room */
	public Housekeeper getAssignment(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		if (!instance.rooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		return assign.get(room);
	}

	/** Return the completion time of the given room */
	public int completion(Room room) {
		return startTimes.get(room) + room.process();
	}

	/** Return the statistics for completions */
	public DescriptiveStatistics completionStats() {
		return completionStats;
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

	/** Prints a report of statistics for the current housekeeping schedule */
	public String getStatsReport() {
		StringBuilder sb= new StringBuilder();
		sb.append("HOUSEKEEPING STATISTICS\n");
		sb.append("Completion " + completionStats + "\n");
		sb.append("Rooms Available: " + Arrays.toString(getRoomsAvailable()) + "\n");
		return sb.toString();
	}

	/** Append the room to the given housekeeper's cleaning schedule at the soonest start time
	 *
	 * @param housekeeper The housekeeper who will be assigned this room (in the set of housekeepers)
	 * @param room        The room to be assigned (in the instance and not yet assigned) */
	public void append(Housekeeper housekeeper, Room room) {
		int start= Math.max(housekeeper.getMakespan(), room.release()) + 1;
		add(housekeeper, room, start);
	}

	/** Add the room to the given housekeeper's cleaning schedule at the given start time
	 *
	 * @param housekeeper The housekeeper who will be assigned this room (in the set of housekeepers)
	 * @param room        The room to be assigned (in the instance and not yet assigned)
	 * @param start       The time the housekeeper will begin cleaning this room */
	public void add(Housekeeper housekeeper, Room room, int start) {
		if (!housekeepers.contains(housekeeper))
			throw new IllegalArgumentException("Schedule does not contain this housekeeper");
		if (!instance.rooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		if (assign.get(room) != null) throw new IllegalArgumentException("Room already assigned housekeeper");
		housekeeper.addRoom(room, start);
		assign.put(room, housekeeper);
		startTimes.put(room, start);
		makespan= Math.max(makespan, housekeeper.getMakespan());
		completionStats.addValue(start + room.process() - 1);
		calculateRoomsAvailable();
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
		int h= instance.teamSize();
		for (int i= 1; i <= h; i++ ) {
			housekeepers.add(new Housekeeper(i));
		}
		startTimes= new HashMap<>();
		for (Room room : instance.rooms()) {
			startTimes.put(room, null);
		}

		makespan= 0;
		completionStats= new DescriptiveStatistics();
		roomsAvailable= new int[] {};
	}

	/** Prints a visual representation of the housekeeping schedule */
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
						sb.append(String.format("%-4d", assign.get(room).getID()));
					} else {
						sb.append(String.format("%-4s", " "));
					}
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/** Returns a string representing the housekeeping schedule */
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("--------------------------\n");
		sb.append("HOUSEKEEPER\t" + "SCHEDULE\n");
		for (Housekeeper housekeeper : housekeepers) {
			sb.append(String.format("%-12d\t", housekeeper.getID()));
			sb.append(housekeeper.getSchedule() + "\n");
		}
		sb.append("--------------------------\n");
		return sb.toString();
	}

}
