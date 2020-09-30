package com.henryrobbins.solver.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.henryrobbins.decision.Schedule;
import com.henryrobbins.hotel.Housekeeper;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.Solver;

/** Maintains a priority queue of housekeepers; their priority is the makespan of their current
 * schedule. This heuristic assumes that the checkout times of rooms are unknown. When a room checks
 * out, a housekeeper is polled from the priority queue and assigned to clean that room. They are
 * then placed back in the queue with the adjusted makespan. In effect, rooms are appended to the
 * schedule of the first available housekeeper. When cleaning times are constant, this heuristic
 * optimizes the "minimize sum of availability intervals" objective function. */
public class FirstAvailable implements Solver<Schedule> {

	@Override
	/** Assign the first available housekeeper as rooms check out */
	public Schedule solve(Instance instance) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Schedule schedule= new Schedule(instance);
		ArrayList<Room> rooms= instance.rooms();

		PriorityQueue<Housekeeper> housekeepers= new PriorityQueue<>(Comparator.comparingInt(Housekeeper::getMakespan));

		for (Housekeeper housekeeper : schedule.getHousekeepers()) {
			housekeepers.add(housekeeper);
		}

		Collections.sort(rooms, Comparator.comparingInt(Room::release));

		for (Room room : rooms) {
			Housekeeper housekeeper= housekeepers.poll();
			schedule.append(housekeeper, room);
			housekeepers.add(housekeeper);
		}

		Collections.sort(rooms, Comparator.comparingInt(Room::num));

		return schedule;

	}

	@Override
	public String toString() {
		return "First Available";
	}

}
