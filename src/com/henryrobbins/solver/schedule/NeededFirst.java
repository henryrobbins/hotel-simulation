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

/** Given the set of incoming guests and their requested room type, the heuristic determines the
 * minimum multiset of room types that will satisfy the incoming requests. This process accounts for
 * potentially forced upgrades. Similarly, this heuristic maintains a priority queue of housekeepers
 * and assumes that the checkout times of rooms are unknown. If a room checks out and the multiset
 * contains it's room type, a housekeeper is polled from the priority queue and assigned to clean
 * that room. The type is then removed from the multiset and the housekeeper is placed back in the
 * queue with the adjusted makespan. If a room checks out and the multiset does not contain it's
 * room type, it is placed in a postponed rooms queue. Once the multiset is empty, any rooms in the
 * postponed queue are then cleaned according to the FirstAvailable heuristic. */
public class NeededFirst implements Solver<Schedule> {

	/** Assign housekeepers to room types needed to satisfy the incoming guests <br>
	 * first before moving on to the rooms whose types are not needed. */
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
			if (minSet.contains(type)) {
				Housekeeper housekeeper= housekeepers.poll();
				schedule.append(housekeeper, room);
				minSet.remove((Integer) type);
				housekeepers.add(housekeeper);

			} else {
				postponeQueue.add(room);
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
		return "Needed First";
	}

}
