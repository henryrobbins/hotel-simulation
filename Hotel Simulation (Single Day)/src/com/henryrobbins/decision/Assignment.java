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

/** Maintains an assignment of guests to hotel rooms for a given instance. */
public class Assignment implements Decision {

	/** The instance that this room assignment (matching) is for */
	private Instance instance;
	/** The matching (assignment) of guests to rooms */
	private BidiMap<Guest, Room> matching= new DualHashBidiMap<>();
	/** The set of satisfactions for every guest */

	// MAINTAINS STATISTICS
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

	/** Construct a copy of the given Assignment */
	public Assignment(Assignment assignment) {
		instance= new Instance.Builder(assignment.instance).build();
		matching= new DualHashBidiMap<>(assignment.matching);
		satisfaction= new DescriptiveStatistics(assignment.satisfaction);
		upgrades= new DescriptiveStatistics(assignment.upgrades);
	}

	/** Return the current room assignment (matching) */
	public BidiMap<Guest, Room> assignment() {
		return new DualHashBidiMap<>(matching);
	}

	/** Return the satisfaction statistics for the current room assignment (matching) */
	public DescriptiveStatistics satisfactionStats() {
		return new DescriptiveStatistics(satisfaction);
	}

	/** Return the upgrades statistics for the current room assignment(matching) */
	public DescriptiveStatistics upgradeStats() {
		return new DescriptiveStatistics(upgrades);
	}

	/** Return true if this room assignment is for the given instance; false otherwise */
	public boolean isAssignmentFor(Instance instance) {
		return this.instance.equals(instance);
	}

	/** Assigns the given guest to the given room. Returns true if assigned successfully.
	 *
	 * @param guest The guest to be assigned (in this instance)
	 * @param room  The room the guest is assigned (in this instance) */
	public boolean assign(Guest guest, Room room) {
		if (!instance.guests().contains(guest)) throw new IllegalArgumentException("Guest not in instance");
		if (!instance.rooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
		int upgrade= room.type() - guest.type();
		if (upgrade < 0) return false;
		if (!isRoomOpen(room)) return false;
		if (isGuestAssigned(guest)) return false;
		matching.put(guest, room);
		satisfaction.addValue(instance.weight(guest, room));
		upgrades.addValue(upgrade);
		return true;
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
	 * @param guest The guest in question (a guest in this instance) */
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

	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Assignment.class) return false;
		Assignment assignment= (Assignment) ob;
		if (!instance.equals(assignment.instance)) return false;
		if (!matching.equals(assignment.matching)) return false;
		return true;
	}

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