package com.henryrobbins.solver.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.henryrobbins.decision.Schedule;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Housekeeper;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** This heuristic is identical to the NeededFirst heuristic with one minor adjustment. If a room
 * checks out and there is a housekeeper available to clean at that time interval, the room is
 * automatically cleaned weather it's room type is in the multiset or not. If it happens to be, it's
 * type will be removed from the multiset. This prevents housekeepers from waiting for rooms that
 * need to be cleaned but still prioritizes them. */
public class NeededFirstNoWait implements Solver<Schedule> {

	/** Assign housekeepers to room types needed to satisfy the incoming guests <br>
	 * first before moving on to the rooms whose types are not needed. If a <br>
	 * housekeeper is available when room checks out, clean automatically. */
	@Override
	public Schedule solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Schedule schedule= new Schedule(instance);
		ArrayList<Room> rooms= instance.rooms();
		ArrayList<Guest> guests= instance.guests();

		// contains the multiset of all available room types
		ArrayList<Integer> typeMultiset= new ArrayList<>();

		for (Room room : rooms) {
			typeMultiset.add(room.type());
		}

		// contains the "minimum" subset of room types needed to satisfy the requests
		// "minimum" in that the minimum room type is assigned to every guest
		// Note: there are cases where an upgrade is forced because of low inventory of lower type
		ArrayList<Integer> minSet= new ArrayList<>();

		for (Guest guest : guests) {
			int request= guest.type();
			while (!typeMultiset.contains(request)) {
				request++ ;
			}
			typeMultiset.remove((Integer) request);
			minSet.add(request);
		}

		LinkedList<Room> postponeQueue= new LinkedList<>();
		PriorityQueue<Housekeeper> housekeepers= new PriorityQueue<>(Comparator.comparingInt(Housekeeper::getMakespan));

		for (Housekeeper housekeeper : schedule.getHousekeepers()) {
			housekeepers.add(housekeeper);
		}

		Collections.sort(rooms, Comparator.comparingInt(Room::release));

		for (Room room : rooms) {
			int type= room.type();
			// If a housekeeper is available, assign them no matter what
			if (housekeepers.peek().getMakespan() <= room.release()) {
				Housekeeper housekeeper= housekeepers.poll();
				schedule.append(housekeeper, room);
				if (minSet.contains(type)) {
					minSet.remove((Integer) type);
				}
				housekeepers.add(housekeeper);
			} else {
				if (minSet.contains(type)) {
					Housekeeper housekeeper= housekeepers.poll();
					schedule.append(housekeeper, room);
					minSet.remove((Integer) type);
					housekeepers.add(housekeeper);

				} else {
					postponeQueue.add(room);
				}
			}
		}

		for (Room room : postponeQueue) {
			Housekeeper housekeeper= housekeepers.poll();
			schedule.append(housekeeper, room);
			housekeepers.add(housekeeper);
		}

		Collections.sort(rooms, Comparator.comparingInt(Room::num));

		return schedule;

	}

	@Override
	public String toString() {
		return "Needed First No-Wait";
	}

}