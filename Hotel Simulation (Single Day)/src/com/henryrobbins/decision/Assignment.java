package com.henryrobbins.decision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;

/** Maintains a feasible assignment of guests to hotel rooms for a given instance. <br>
 * The structure is analogous to a matching on a given bipartite graph. <br>
 * Furthermore, it maintains statistics for the assignment */
public class Assignment implements Decision {

	/** The instance that this room assignment (matching) is for */
	private Instance instance;
	/** The matching (assignment) of guests to rooms */
	private BidiMap<Guest, Room> matching= new DualHashBidiMap<>();
	/** The set of satisfactions for every guest */
	private DescriptiveStatistics satisfaction= new DescriptiveStatistics();
	/** The set of upgrades for every guest */
	private DescriptiveStatistics upgrades= new DescriptiveStatistics();

	/** Construct an empty assignment for the instance
	 *
	 * @param instance The instance that this room assignment is for (not null) */
	public Assignment(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		this.instance= instance;
		for (Guest guest : instance.guests()) {
			matching.put(guest, null);
		}
	}

	/** Return the current room assignment (matching) */
	public BidiMap<Guest, Room> assignment() {
		return matching;
	}

	/** Return the satisfaction statistics for the current room assignment (matching) */
	public DescriptiveStatistics satisfactionStats() {
		return satisfaction;
	}

	/** Return the upgrades statistics for the current room assignment(matching) */
	public DescriptiveStatistics upgradeStats() {
		return upgrades;
	}

	/** Return true if this room assignment is for the given instance; false otherwise */
	public boolean isAssignmentFor(Instance instance) {
		return this.instance.equals(instance);
	}

	/** Assigns the specified guest to the specified room
	 *
	 * @param guest The guest to be assigned (In this instance and unassigned)
	 * @param room  The room the guest is assigned (In this instance, satisfies <br>
	 *              the guest's requested type, and yet to be assigned a guest) */
	public void assign(Guest guest, Room room) {
		if (!instance.guests().contains(guest)) throw new IllegalArgumentException("Guest not in instance");
		if (!instance.rooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		int upgrade= room.type() - guest.type();
		if (upgrade < 0) throw new IllegalArgumentException("Room type does not satisfy guest request");
		if (!isRoomOpen(room)) throw new IllegalArgumentException("Room already assigned");
		if (isGuestAssigned(guest)) throw new IllegalArgumentException("Guest already assigned");
		matching.put(guest, room);
		satisfaction.addValue(instance.weight(guest, room));
		upgrades.addValue(upgrade);
	}

	/** Return true iff the given room is open and yet to be assigned */
	public boolean isRoomOpen(Room room) {
		return matching.getKey(room) == null;
	}

	/** Return true iff the given guest has been assigned a room */
	public boolean isGuestAssigned(Guest guest) {
		return matching.get(guest) != null;
	}

	/** Return the minimum room type the given guest can be assigned <br>
	 * (Given the current state of the assignment)
	 *
	 * @param guest The guest under question (a guest in this instance) */
	public int getMinType(Guest guest) {
		if (!instance.guests().contains(guest)) throw new IllegalArgumentException("Guest not in instance");
		ArrayList<Room> rooms= instance.rooms();
		Collections.sort(rooms, Comparator.comparingInt(Room::type));
		for (Room room : rooms) {
			if (room.type() >= guest.type() && matching.getKey(room) == null) { return room.type(); }
		}
		throw new IllegalArgumentException("This assignment is infeasible");
	}

	/** Reset the room assignment leaving it empty */
	public void reset() {
		matching.clear();
		for (Guest guest : instance.guests()) {
			matching.put(guest, null);
		}
		satisfaction= new DescriptiveStatistics();
		upgrades= new DescriptiveStatistics();
	}

	/** Prints a report of statistics for the current room assignment (matching) */
	public String getStatsReport() {
		StringBuilder sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT STATISTICS \n");
		sb.append("----------------------------------- \n");
		sb.append("Satisfaction " + satisfaction + "\n");
		sb.append("Upgrades " + upgrades);
		sb.append("----------------------------------- \n");
		return sb.toString();
	}

	/** Return a string representing the room assignment */
	@Override
	public String toString() {
		ArrayList<Room> rooms= instance.rooms();
		Collections.sort(instance.rooms(), Comparator.comparingInt(Room::num));
		StringBuilder sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT\n");
		sb.append("---------------\n");
		sb.append("ROOM\t" + "GUEST" + "\n");
		for (Room room : rooms) {
			sb.append(String.format("%-4s\t", room));
			if (matching.getKey(room) != null) {
				sb.append(String.format("%-5s\n", matching.getKey(room)));
			} else {
				sb.append("null \n");
			}
		}
		sb.append("---------------\n");
		return sb.toString();
	}
}