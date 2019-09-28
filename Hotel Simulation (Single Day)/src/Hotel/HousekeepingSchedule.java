package Hotel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/** Maintains a feasible housekeeping schedule for a given instance. <br>
 * Furthermore, it maintains statistics for the schedule */
public class HousekeepingSchedule {

	/** The instance that the housekeeping schedule is for */
	private Instance instance;
	/** The set of housekeepers whose size is consistent with the size in the instance */
	private ArrayList<Housekeeper> housekeepers;
	/** The time every assigned room begins getting cleaned. null if unassigned */
	private HashMap<Room, Integer> startTimes;
	/** The housekeeper assigned to each room. null if unassigned */
	private HashMap<Room, Housekeeper> assign;

	/** The set of availability intervals for every assigned room */
	private DescriptiveStatistics availabilityIntervals= new DescriptiveStatistics();
	/** The set of makespans for every housekeeper */
	private int makespan= 0;
	/** The number of rooms available at every relevant time interval */
	private int[] roomsAvailable= {};

	/** Construct an empty housekeeping schedule for the instance
	 *
	 * @param inst The instance that this housekeeping schedule is for (not null) */
	public HousekeepingSchedule(Instance inst) {
		if (inst == null) throw new IllegalArgumentException("Instance is null");
		instance= inst;
		housekeepers= new ArrayList<>();
		for (int i= 1; i <= instance.getNumOfHousekeepers(); i++ ) {
			housekeepers.add(new Housekeeper(i));
		}
		startTimes= new HashMap<>();
		assign= new HashMap<>();
		for (Room room : instance.getRooms()) {
			startTimes.put(room, null);
			assign.put(room, null);
		}
	}

	/** Return the list of housekeepers */
	public ArrayList<Housekeeper> getHousekeepers() {
		return housekeepers;
	}

	/** Return the time the specified room is cleaned
	 *
	 * @param room The room whose start time is in question (room must be in instance)
	 * @return The time the room begins getting cleaned */
	public Integer getStartTime(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		if (!instance.getRooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		return startTimes.get(room);
	}

	/** Return the housekeeper assigned to the given room
	 *
	 * @param The room whose housekeeper is in question (room must be in instance)
	 * @return The housekeeper cleaning this room */
	public Housekeeper getAssignment(Room room) {
		if (room == null) throw new IllegalArgumentException("Room is null");
		if (!instance.getRooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		return assign.get(room);
	}

	/** Return the sum of availability intervals for every assigned room */
	public int getSumOfAvailabilities() {
		return (int) availabilityIntervals.getSum();
	}

	/** Return the makespan of the entire schedule */
	public int getMakespan() {
		return makespan;
	}

	/** Return the number of rooms available at every relevant time interval */
	public int[] getRoomsAvailable() {
		return roomsAvailable;
	}

	/** Return the number of rooms available at time interval t */
	public int getRoomsAvailableAt(int t) {
		return roomsAvailable[t];
	}

	/** Calculates the number of rooms available at every relevant time interval. <br>
	 * Relevant time intervals include all intervals leading up to and including the <br>
	 * interval in which all rooms are cleaned and available for check in. */
	private void calculateRoomsAvailable() {
		int[] roomsAvailable= new int[getMakespan() + 2];
		for (int i= 0; i < getMakespan() + 2; i++ ) {
			roomsAvailable[i]= calculateRoomsAvailableAt(i);
		}
		this.roomsAvailable= roomsAvailable;

	}

	/** Return the number of rooms available at a given time interval */
	private int calculateRoomsAvailableAt(int t) {
		int counter= 0;
		for (Room room : startTimes.keySet()) {
			Integer startTime= startTimes.get(room);
			if (startTime != null && startTime + room.getCleanTime() - 1 < t) { counter++ ; }
		}
		return counter;
	}

	/** Prints a report of statistics for the current housekeeping schedule */
	public String getStatsReport() {
		StringBuilder sb= new StringBuilder();
		sb.append("HOUSEKEEPING STATISTICS\n");
		sb.append(String.format("Makespan: %d\n", getMakespan()));
		sb.append(String.format("Sum of Availabilities: %d\n", getSumOfAvailabilities()));
		sb.append("Rooms Available: " + Arrays.toString(getRoomsAvailable()) + "\n");
		return sb.toString();
	}

	/** Append the room to the given housekeeper's cleaning schedule at the soonest start time
	 *
	 * @param housekeeper The housekeeper who will be assigned this room (in the set of housekeepers)
	 * @param room        The room to be assigned (in the instance and not yet assigned) */
	public void append(Housekeeper housekeeper, Room room) {
		int start= Math.max(housekeeper.getMakespan(), room.getCheckOut()) + 1;
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
		if (!instance.getRooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		if (assign.get(room) != null) throw new IllegalArgumentException("Room already assigned housekeeper");
		housekeeper.addRoom(room, start);
		assign.put(room, housekeeper);
		startTimes.put(room, start);
		makespan= Math.max(makespan, housekeeper.getMakespan());
		availabilityIntervals.addValue(start + room.getCleanTime() - 1);
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
		int h= instance.getNumOfHousekeepers();
		for (int i= 1; i <= h; i++ ) {
			housekeepers.add(new Housekeeper(i));
		}
		startTimes= new HashMap<>();
		for (Room room : instance.getRooms()) {
			startTimes.put(room, null);
		}

		makespan= 0;
		availabilityIntervals= new DescriptiveStatistics();
		roomsAvailable= new int[] {};
	}

	/** Prints a visual representation of the housekeeping schedule */
	public String getVisual() {
		StringBuilder sb= new StringBuilder();
		int minT= getMakespan() + 1;
		for (Room room : instance.getRooms()) {
			minT= Math.min(minT, room.getCheckOut());
		}
		int maxT= getMakespan() + 1;
		sb.append("#   ");
		for (int t= minT; t <= maxT; t++ ) {
			sb.append(String.format("%-4d", t));
		}
		sb.append("\n");
		for (Room room : instance.getRooms()) {
			sb.append(String.format("%-4d", room.getNumber()));
			for (int t= minT; t <= maxT; t++ ) {
				if (t <= room.getCheckOut()) {
					sb.append(String.format("%-4s", "X"));
				} else {
					Integer start= startTimes.get(room);
					if (start != null && t >= start && t < start + room.getCleanTime()) {
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
