package com.henryrobbins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.jupiter.api.Test;

import com.ampl.AMPL;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Housekeeper;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Instance.Builder;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.BestFirst;
import com.henryrobbins.solver.assignment.Linear;
import com.henryrobbins.solver.assignment.MaxMeanSatSTMinIP;
import com.henryrobbins.solver.assignment.MinUpgradesSTSatIP;
import com.henryrobbins.solver.assignment.OnlineMeanSatIP;
import com.henryrobbins.solver.assignment.PreserveEdgesMeanSat;
import com.henryrobbins.solver.assignment.SuggestiveMeanSatIP;
import com.henryrobbins.solver.assignment.WorstFirst;
import com.henryrobbins.solver.schedule.FirstAvailable;
import com.henryrobbins.solver.schedule.NeededFirst;
import com.henryrobbins.solver.schedule.NeededFirstNoWait;
import com.henryrobbins.solver.schedule.ScheduleIPSolver;
import com.henryrobbins.solver.solution.AssignmentSTScheduleIP;
import com.henryrobbins.solver.solution.FirstRoom;
import com.henryrobbins.solver.solution.ScheduleSTAssignmentIP;
import com.henryrobbins.solver.solution.SolutionIPSolver;

class Tester {

	/** The number of random instances to test */
	private int t= 5;
	/** The size of the randomly generated hotels */
	private int n= 5;

	@Test
	void testRoom() {

		// tests illegal arguments for constructor
		assertThrows(IllegalArgumentException.class, () -> { new Room(0, 2, 0, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 0, 0, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 2, -1, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 2, 0, 0); });

		// tests constructor
		Room room= new Room(1, 2, 1, 2);
		assertEquals(1, room.num());
		assertEquals(2, room.type());
		assertEquals(1, room.release());
		assertEquals(2, room.process());
		assertEquals("1", room.toString());

		// tests ordering
		assertEquals(true, new Room(1, 3, 1, 2).compareTo(room) > 0);

		// tests equals
		assertEquals(false, room.equals(null));
		assertEquals(false, room.equals(new Guest(1, 2, 3)));
		assertEquals(false, room.equals(new Room(2, 2, 1, 2)));
		assertEquals(false, room.equals(new Room(1, 3, 1, 2)));
		assertEquals(false, room.equals(new Room(1, 2, 2, 2)));
		assertEquals(false, room.equals(new Room(1, 2, 1, 3)));
		assertEquals(true, room.equals(new Room(1, 2, 1, 2)));

	}

	@Test
	void testGuest() {

		// tests illegal arguments for constructor
		assertThrows(IllegalArgumentException.class, () -> { new Guest(0, 3, 0); });
		assertThrows(IllegalArgumentException.class, () -> { new Guest(1, 0, 1); });
		assertThrows(IllegalArgumentException.class, () -> { new Guest(1, 3, -1); });

		// tests constructor
		Guest guest= new Guest(1, 3, 1);
		assertEquals(1, guest.id());
		assertEquals(3, guest.type());
		assertEquals(1, guest.arrival());
		assertEquals("1", guest.toString());

		// tests equals
		assertEquals(false, guest.equals(null));
		assertEquals(false, guest.equals(new Room(2, 2, 1, 2)));
		assertEquals(false, guest.equals(new Guest(2, 3, 1)));
		assertEquals(false, guest.equals(new Guest(1, 4, 1)));
		assertEquals(false, guest.equals(new Guest(1, 3, 2)));
		assertEquals(true, guest.equals(new Guest(1, 3, 1)));

	}

	@Test
	void testHousekeeper() {

		assertThrows(IllegalArgumentException.class, () -> { new Housekeeper(0); });

		Housekeeper housekeeper= new Housekeeper(1);

		assertThrows(IllegalArgumentException.class, () -> { housekeeper.appendRoom(null); });
		assertThrows(IllegalArgumentException.class, () -> { housekeeper.addRoom(null, 2); });

		Room room1= new Room(1, 1, 0, 2);
		Room room2= new Room(2, 1, 3, 2);
		Room room3= new Room(3, 1, 4, 2);
		Room room4= new Room(4, 1, 2, 2);

		housekeeper.appendRoom(room1);
		assertThrows(IllegalArgumentException.class, () -> { housekeeper.addRoom(room2, 2); });
		assertThrows(IllegalArgumentException.class, () -> { housekeeper.addRoom(room3, 4); });
		housekeeper.addRoom(room3, 6);
		housekeeper.addRoom(room2, 4);
		assertThrows(IllegalArgumentException.class, () -> { housekeeper.addRoom(room4, 3); });

		assertEquals(1, housekeeper.getID());
		assertEquals(new ArrayList<>(Arrays.asList(room1, room2, room3)), housekeeper.getSchedule());
		assertEquals(1, housekeeper.getStartTime(room1));
		assertEquals(4, housekeeper.getStartTime(room2));
		assertEquals(6, housekeeper.getStartTime(room3));
		assertThrows(IllegalArgumentException.class, () -> { housekeeper.getStartTime(room4); });
		assertEquals(7, housekeeper.getMakespan());

		assertEquals("Housekeeper: 1, Schedule: 1,2,3", housekeeper.toString());

	}

	@Test
	void testInstance() {

		// tests illegal arguments for constructor of builder
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder("", 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder(null, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder("test", 0); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder("test", null); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder(null, null); });

		Instance.Builder builder= new Instance.Builder("name", 2);

		// TESTS FOR ADDING ROOM TO BUILDER

		// tests illegal arguments for adding a room to the builder
		assertThrows(IllegalArgumentException.class, () -> { builder.addRoom((Room) null); });

		Room room1= new Room(1, 1, 1, 2);
		builder.addRoom(room1);

		// tests illegal arguments for adding a room to the builder (duplicate room number)
		Room room12= new Room(1, 2, 2, 2);
		assertThrows(IllegalArgumentException.class, () -> { builder.addRoom(room12); });

		// tests add room method for builder
		Room room2= new Room(2, 2, 2, 2);
		Room room3= new Room(3, 3, 3, 2);
		builder.addRoom(room2);
		builder.addRoom(room3);

		assertEquals(room1, builder.room(1));
		assertEquals(room2, builder.room(2));
		assertEquals(room3, builder.room(3));

		ArrayList<Room> rooms= new ArrayList<>();
		rooms.add(room1);
		rooms.add(room2);
		rooms.add(room3);

		assertEquals(rooms, builder.rooms());

		// TESTS FOR ADDING GUEST TO BUILDER

		// tests illegal arguments for adding a guest to the builder
		assertThrows(IllegalArgumentException.class, () -> { builder.addGuest((Guest) null); });

		Guest guest1= new Guest(1, 1, 3);
		builder.addGuest(guest1);

		// tests illegal arguments for adding a guest to the builder (duplicate guest IDs)
		Guest guest12= new Guest(1, 3, 3);
		assertThrows(IllegalArgumentException.class, () -> { builder.addGuest(guest12); });

		// tests add guest method for builder
		Guest guest2= new Guest(2, 1, 2);
		Guest guest3= new Guest(3, 2, 3);
		builder.addGuest(guest2);
		builder.addGuest(guest3);

		assertEquals(guest1, builder.guest(1));
		assertEquals(guest2, builder.guest(2));
		assertEquals(guest3, builder.guest(3));

		ArrayList<Guest> guests= new ArrayList<>();
		guests.add(guest1);
		guests.add(guest2);
		guests.add(guest3);

		assertEquals(guests, builder.guests());

		// TESTS FOR ADDING A WEIGHT TO BUILDER

		// tests illegal arguments for adding a weight to the builder
		assertThrows(IllegalArgumentException.class, () -> { builder.addWeight(null, room1, 0.5); });
		assertThrows(IllegalArgumentException.class, () -> { builder.addWeight(new Guest(2, 2, 2), room1, 0.5); });
		assertThrows(IllegalArgumentException.class, () -> { builder.addWeight(guest1, null, 0.5); });
		assertThrows(IllegalArgumentException.class, () -> { builder.addWeight(guest1, new Room(2, 2, 2, 3), 0.5); });
		assertThrows(IllegalArgumentException.class, () -> { builder.addWeight(guest1, room1, 2.0); });
		assertThrows(IllegalArgumentException.class, () -> { builder.addWeight(guest1, room1, -1.0); });

		// tests add weight method for builder
		builder.addWeight(guest1, room1, 1.0);
		builder.addWeight(guest1, room2, 0.1);
		builder.addWeight(guest1, room3, 0.7);
		builder.addWeight(guest2, room1, 0.9);
		builder.addWeight(guest2, room2, 0.4);
		builder.addWeight(guest2, room3, 0.3);
		builder.addWeight(guest3, room1, 0.8);
		builder.addWeight(guest3, room2, 1.0);
		builder.addWeight(guest3, room3, 0.0);

		MultiKeyMap<Object, Double> wgts= new MultiKeyMap<>();

		wgts.put(guest1, room1, 1.0);
		wgts.put(guest1, room2, 0.1);
		wgts.put(guest1, room3, 0.7);
		wgts.put(guest2, room1, 0.9);
		wgts.put(guest2, room2, 0.4);
		wgts.put(guest2, room3, 0.3);
		wgts.put(guest3, room1, 0.8);
		wgts.put(guest3, room2, 1.0);
		wgts.put(guest3, room3, 0.0);

		assertEquals(wgts, builder.weights());

		// TESTS BUILDER BUILD METHOD (BLANK)

		Instance instance= builder.build();
		assertEquals("name", instance.name());
		assertEquals(rooms, instance.rooms());
		assertEquals(room1, instance.room(1));
		assertEquals(guests, instance.guests());
		assertEquals(guest1, instance.guest(1));
		assertEquals(1.0, instance.weight(3, 2), 0.0001);
		assertEquals(0.3, instance.weight(guest2, room3), 0.0001);
		assertEquals(wgts, instance.weights());
		assertEquals(2, instance.teamSize());
		assertEquals(3, instance.typeSize());

		// tests methods for feasibility and feasible type requests
		assertEquals(true, instance.feasible());
		assertEquals(0, instance.maxFeasibleTypeRequest());

		builder.addGuest(new Guest(4, 1, 1));
		instance= builder.build();

		assertEquals(false, instance.feasible());
		assertEquals(-1, instance.maxFeasibleTypeRequest());

		builder.addRoom(new Room(4, 1, 1, 1));
		builder.addRoom(new Room(5, 1, 1, 1));
		instance= builder.build();

		assertEquals(true, instance.feasible());
		assertEquals(3, instance.maxFeasibleTypeRequest());

		builder.addGuest(new Guest(5, 5, 1));
		instance= builder.build();

		assertEquals(false, instance.feasible());

		Builder builder2= new Builder("name", 3);
		builder2.addRoom(new Room(1, 2, 1, 1));
		builder2.addRoom(new Room(2, 2, 1, 1));
		builder2.addRoom(new Room(3, 2, 1, 1));
		builder2.addRoom(new Room(4, 3, 1, 1));
		builder2.addGuest(new Guest(1, 1, 1));
		builder2.addGuest(new Guest(2, 3, 1));
		builder2.addGuest(new Guest(3, 3, 1));
		builder2.addGuest(new Guest(4, 3, 1));
		instance= builder2.build();

		assertEquals(-1, instance.maxFeasibleTypeRequest());

		// TESTS INSTANCE EQUALS METHOD

		Builder primaryBuilder= new Builder("build", 3);
		primaryBuilder.addRoom(new Room(1, 3, 1, 2));
		primaryBuilder.addRoom(new Room(2, 2, 4, 2));
		primaryBuilder.addRoom(new Room(3, 1, 2, 3));
		primaryBuilder.addGuest(new Guest(1, 1, 2));
		primaryBuilder.addGuest(new Guest(2, 1, 4));
		primaryBuilder.addGuest(new Guest(3, 1, 3));

		primaryBuilder.addWeight(primaryBuilder.guest(1), primaryBuilder.room(1), 1.0);
		primaryBuilder.addWeight(primaryBuilder.guest(1), primaryBuilder.room(2), 0.1);
		primaryBuilder.addWeight(primaryBuilder.guest(1), primaryBuilder.room(3), 0.7);
		primaryBuilder.addWeight(primaryBuilder.guest(2), primaryBuilder.room(1), 0.9);
		primaryBuilder.addWeight(primaryBuilder.guest(2), primaryBuilder.room(2), 0.4);
		primaryBuilder.addWeight(primaryBuilder.guest(2), primaryBuilder.room(3), 0.3);
		primaryBuilder.addWeight(primaryBuilder.guest(3), primaryBuilder.room(1), 0.8);
		primaryBuilder.addWeight(primaryBuilder.guest(3), primaryBuilder.room(2), 1.0);
		primaryBuilder.addWeight(primaryBuilder.guest(3), primaryBuilder.room(3), 0.0);

		Instance primary= primaryBuilder.build();

		Builder compareBuilder= new Builder("build", 3);
		compareBuilder.addRoom(new Room(3, 1, 2, 3));
		compareBuilder.addRoom(new Room(1, 3, 1, 2));
		compareBuilder.addGuest(new Guest(2, 1, 4));
		Instance differentRooms= compareBuilder.build();
		compareBuilder.addRoom(new Room(2, 2, 4, 2));
		compareBuilder.addGuest(new Guest(1, 1, 2));
		Instance differentGuests= compareBuilder.build();
		compareBuilder.addGuest(new Guest(3, 1, 3));

		compareBuilder.addWeight(compareBuilder.guest(3), compareBuilder.room(1), 0.8);
		compareBuilder.addWeight(compareBuilder.guest(1), compareBuilder.room(1), 1.0);
		compareBuilder.addWeight(compareBuilder.guest(2), compareBuilder.room(1), 0.9);
		compareBuilder.addWeight(compareBuilder.guest(1), compareBuilder.room(2), 0.1);
		compareBuilder.addWeight(compareBuilder.guest(2), compareBuilder.room(2), 0.4);
		compareBuilder.addWeight(compareBuilder.guest(2), compareBuilder.room(3), 0.3);
		compareBuilder.addWeight(compareBuilder.guest(3), compareBuilder.room(2), 1.0);
		Instance differentWeights= compareBuilder.build();
		compareBuilder.addWeight(compareBuilder.guest(3), compareBuilder.room(3), 0.0);
		compareBuilder.addWeight(compareBuilder.guest(1), compareBuilder.room(3), 0.7);
		Instance compare= compareBuilder.build();

		compareBuilder= new Builder("dif", 3);
		compareBuilder.addRoom(new Room(1, 1, 1, 1));
		compareBuilder.addGuest(new Guest(1, 1, 1));
		Instance differentName= compareBuilder.build();
		compareBuilder= new Builder("build", 2);
		compareBuilder.addRoom(new Room(1, 1, 1, 1));
		compareBuilder.addGuest(new Guest(1, 1, 1));
		Instance differentTeamSize= compareBuilder.build();

		assertEquals(false, primary.equals(null));
		assertEquals(false, primary.equals(compareBuilder));
		assertEquals(false, primary.equals(differentName));
		assertEquals(false, primary.equals(differentTeamSize));
		assertEquals(false, primary.equals(differentRooms));
		assertEquals(false, primary.equals(differentGuests));
		assertEquals(false, primary.equals(differentWeights));
		assertEquals(compare, primary);

		// TESTS BUILDER BUILD METHOD (COPY)

		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder("", primary); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder(null, primary); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder("copy", null); });
		Instance copy= new Builder("copy", primary).build();
		assertEquals(primary, copy);

		// TESTS 1 ROOM MINIMUM AND 1 GUEST MINIMUM

		Builder noRooms= new Builder("dif", 3);
		noRooms.addGuest(new Guest(1, 1, 1));
		assertThrows(IllegalArgumentException.class, () -> { noRooms.build(); });
		Builder noGuests= new Builder("dif", 3);
		noGuests.addRoom(new Room(1, 1, 1, 1));
		assertThrows(IllegalArgumentException.class, () -> { noGuests.build(); });

		// TESTS INSTANCE TOSTRING METHOD

		StringBuilder sb= new StringBuilder();
		sb.append("ROOMS\n");
		sb.append("------------------------------------------\n");
		sb.append("ROOM	TYPE	CHECKOUT	CLEAN TIME\n");
		sb.append("1    	3    	1       	2         \n");
		sb.append("2    	2    	4       	2         \n");
		sb.append("3    	1    	2       	3         \n");
		sb.append("------------------------------------------\n\n");
		sb.append("GUESTS\n");
		sb.append("-----------------------\n");
		sb.append("GUEST	TYPE	ARRIVAL\n");
		sb.append("1     	1    	2      \n");
		sb.append("2     	1    	4      \n");
		sb.append("3     	1    	3      \n");
		sb.append("-----------------------\n\n");
		sb.append("GUESTS (ROW) x ROOMS (COL) WEIGHTS\n");
		sb.append("------------------\n");
		sb.append("     1    2    3    \n");
		sb.append("1    1.0  0.1  0.7  \n");
		sb.append("2    0.9  0.4  0.3  \n");
		sb.append("3    0.8  1.0  0.0  \n");
		sb.append("------------------\n");
		sb.append("Housekeeping Team Size: 3\n");
		assertEquals(sb.toString(), primary.toString());
	}

	@Test
	void testInstanceFactory() {

		// Build test3 manually for comparison
		Instance.Builder builder= new Builder("test3", 3);

		builder.addRoom(new Room(1, 1, 2, 2));
		builder.addRoom(new Room(2, 1, 2, 2));
		builder.addRoom(new Room(3, 1, 4, 2));
		builder.addRoom(new Room(4, 1, 3, 2));
		builder.addRoom(new Room(5, 2, 5, 2));
		builder.addRoom(new Room(6, 3, 1, 2));

		builder.addGuest(new Guest(1, 1, 4));
		builder.addGuest(new Guest(2, 1, 4));
		builder.addGuest(new Guest(3, 2, 3));
		builder.addGuest(new Guest(4, 1, 2));
		builder.addGuest(new Guest(5, 2, 5));
		builder.addGuest(new Guest(6, 1, 2));

		builder.addWeight(builder.guest(1), builder.room(1), 0.66);
		builder.addWeight(builder.guest(1), builder.room(2), 0.66);
		builder.addWeight(builder.guest(1), builder.room(3), 1.0);
		builder.addWeight(builder.guest(1), builder.room(4), 0.33);
		builder.addWeight(builder.guest(1), builder.room(5), 0.66);
		builder.addWeight(builder.guest(1), builder.room(6), 0.33);
		builder.addWeight(builder.guest(2), builder.room(1), 0.0);
		builder.addWeight(builder.guest(2), builder.room(2), 1.0);
		builder.addWeight(builder.guest(2), builder.room(3), 0.5);
		builder.addWeight(builder.guest(2), builder.room(4), 1.0);
		builder.addWeight(builder.guest(2), builder.room(5), 0.0);
		builder.addWeight(builder.guest(2), builder.room(6), 0.5);
		builder.addWeight(builder.guest(3), builder.room(1), 0.5);
		builder.addWeight(builder.guest(3), builder.room(2), 1.0);
		builder.addWeight(builder.guest(3), builder.room(3), 1.0);
		builder.addWeight(builder.guest(3), builder.room(4), 0.5);
		builder.addWeight(builder.guest(3), builder.room(5), 0.5);
		builder.addWeight(builder.guest(3), builder.room(6), 0.5);
		builder.addWeight(builder.guest(4), builder.room(1), 0.33);
		builder.addWeight(builder.guest(4), builder.room(2), 1.0);
		builder.addWeight(builder.guest(4), builder.room(3), 0.66);
		builder.addWeight(builder.guest(4), builder.room(4), 0.66);
		builder.addWeight(builder.guest(4), builder.room(5), 0.33);
		builder.addWeight(builder.guest(4), builder.room(6), 0.33);
		builder.addWeight(builder.guest(5), builder.room(1), 0.66);
		builder.addWeight(builder.guest(5), builder.room(2), 0.66);
		builder.addWeight(builder.guest(5), builder.room(3), 1.0);
		builder.addWeight(builder.guest(5), builder.room(4), 0.33);
		builder.addWeight(builder.guest(5), builder.room(5), 0.66);
		builder.addWeight(builder.guest(5), builder.room(6), 0.33);
		builder.addWeight(builder.guest(6), builder.room(1), 1.0);
		builder.addWeight(builder.guest(6), builder.room(2), 0.5);
		builder.addWeight(builder.guest(6), builder.room(3), 1.0);
		builder.addWeight(builder.guest(6), builder.room(4), 0.0);
		builder.addWeight(builder.guest(6), builder.room(5), 1.0);
		builder.addWeight(builder.guest(6), builder.room(6), 0.0);

		Instance instance= builder.build();

		// tests read CSV method
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.readCSV((String) null, 3); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.readCSV("", 3); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.readCSV("test3", 0); });
		assertEquals(instance, InstanceFactory.readCSV("test3", 3));

		// tests write CSV method
		instance.writeCSV();
		assertEquals(instance, InstanceFactory.readCSV("test3", 3));

		// tests create random instance method
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.createRandom(null, 10); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.createRandom("", 10); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.createRandom("randTest", 0); });
		Instance rand= InstanceFactory.createRandom("randTest", 100);

		// tests add guest method
		Instance.Builder builder2= new Builder("test3", 3);

		builder2.addRoom(new Room(1, 1, 2, 2));
		builder2.addRoom(new Room(2, 3, 2, 2));
		builder2.addRoom(new Room(3, 2, 4, 2));

		builder2.addGuest(new Guest(1, 1, 4));
		builder2.addGuest(new Guest(2, 3, 4));

		builder2.addWeight(builder2.guest(1), builder2.room(1), 1.0);
		builder2.addWeight(builder2.guest(1), builder2.room(2), 0.1);
		builder2.addWeight(builder2.guest(1), builder2.room(3), 0.7);
		builder2.addWeight(builder2.guest(2), builder2.room(1), 0.9);
		builder2.addWeight(builder2.guest(2), builder2.room(2), 0.4);
		builder2.addWeight(builder2.guest(2), builder2.room(3), 0.3);

		Instance before= builder2.build();
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.addGuestTo(null, "copy"); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.addGuestTo(before, null); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.addGuestTo(before, ""); });
		Instance after= InstanceFactory.addGuestTo(before, "copy");
		assertEquals(2, before.maxFeasibleTypeRequest());
		int newGuestType= after.guest(3).type();
		assertEquals(true, newGuestType == 1 || newGuestType == 2);

	}

	@Test
	void testRoomAssignment() {

		assertThrows(IllegalArgumentException.class, () -> { new Assignment(null); });

		// tests assign method
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Assignment assignment= new Assignment(instance);

		// tests isAssignmentFor method
		assertEquals(true, assignment.isAssignmentFor(instance));
		assertEquals(false, assignment.isAssignmentFor(null));

		assertThrows(IllegalArgumentException.class,
			() -> { assignment.assign(instance.guest(1), new Room(2, 3, 4, 1)); });
		assertThrows(IllegalArgumentException.class,
			() -> { assignment.assign(new Guest(7, 2, 1), instance.room(1)); });
		assertThrows(IllegalArgumentException.class,
			() -> { assignment.assign(instance.guest(5), instance.room(1)); });

		assignment.assign(instance.guest(6), instance.room(1));
		assignment.assign(instance.guest(4), instance.room(2));
		assignment.assign(instance.guest(1), instance.room(3));

		assertThrows(IllegalArgumentException.class,
			() -> { assignment.assign(instance.guest(2), instance.room(2)); });
		assertThrows(IllegalArgumentException.class,
			() -> { assignment.assign(instance.guest(1), instance.room(4)); });

		assignment.assign(instance.guest(2), instance.room(4));
		assignment.assign(instance.guest(5), instance.room(5));
		// tests getting the minimum type a guest can be assigned given the current assignment
		assertEquals(3, assignment.getMinType(instance.guest(3)));
		assignment.assign(instance.guest(3), instance.room(6));

		// tests matching
		BidiMap<Guest, Room> matching= new DualHashBidiMap<>();
		matching.put(instance.guest(6), instance.room(1));
		matching.put(instance.guest(4), instance.room(2));
		matching.put(instance.guest(1), instance.room(3));
		matching.put(instance.guest(2), instance.room(4));
		matching.put(instance.guest(5), instance.room(5));
		matching.put(instance.guest(3), instance.room(6));
		assertEquals(matching, assignment.assignment());

		// tests satisfaction stats
		assertEquals(1.0, assignment.satisfactionStats().getMax(), 0.0001);
		assertEquals(0.5, assignment.satisfactionStats().getMin(), 0.0001);
		assertEquals(0.86, assignment.satisfactionStats().getMean(), 0.0001);
		assertEquals(5.16, assignment.satisfactionStats().getSum(), 0.0001);
		assertEquals(0.22271, assignment.satisfactionStats().getStandardDeviation(), 0.0001);

		// tests upgrades stats
		assertEquals(1, assignment.upgradeStats().getMax(), 0.0001);
		assertEquals(0, assignment.upgradeStats().getMin(), 0.0001);
		assertEquals(1.0 / 6.0, assignment.upgradeStats().getMean(), 0.0001);
		assertEquals(1, assignment.upgradeStats().getSum(), 0.0001);
		assertEquals(0.4082, assignment.upgradeStats().getStandardDeviation(), 0.0001);

		// tests to string method
		StringBuilder sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT\n");
		sb.append("---------------\n");
		sb.append("ROOM\t" + "GUEST\n");
		sb.append(String.format("%-4s\t%-5s\n", "1", "6"));
		sb.append(String.format("%-4s\t%-5s\n", "2", "4"));
		sb.append(String.format("%-4s\t%-5s\n", "3", "1"));
		sb.append(String.format("%-4s\t%-5s\n", "4", "2"));
		sb.append(String.format("%-4s\t%-5s\n", "5", "5"));
		sb.append(String.format("%-4s\t%-5s\n", "6", "3"));
		sb.append("---------------\n");
		assertEquals(sb.toString(), assignment.toString());

		sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT STATISTICS \n");
		sb.append("----------------------------------- \n");
		sb.append("Satisfaction " + assignment.satisfactionStats() + "\n");
		sb.append("Upgrades " + assignment.upgradeStats());
		sb.append("----------------------------------- \n");
		assertEquals(sb.toString(), assignment.getStatsReport());

		assignment.reset();

		sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT\n");
		sb.append("---------------\n");
		sb.append("ROOM\t" + "GUEST\n");
		sb.append(String.format("%-4s\t%-5s\n", "1", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "2", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "3", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "4", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "5", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "6", "null"));
		sb.append("---------------\n");
		assertEquals(sb.toString(), assignment.toString());

	}

	@Test
	void testAssignmentStat() {

		Instance instance= InstanceFactory.readCSV("test3", 2);
		Assignment assignment= new Assignment(instance);

		assignment.assign(instance.guest(6), instance.room(1));
		assignment.assign(instance.guest(4), instance.room(2));
		assignment.assign(instance.guest(1), instance.room(3));
		assignment.assign(instance.guest(2), instance.room(4));
		assignment.assign(instance.guest(5), instance.room(5));
		assignment.assign(instance.guest(3), instance.room(6));

		ArrayList<Statistic<Assignment>> stats= Statistic.ASSIGNMENT_STATS;

		assertEquals("Max Satisfaction", stats.get(0).toString());
		assertEquals("Min Satisfaction", stats.get(1).toString());
		assertEquals("Mean Satisfaction", stats.get(2).toString());
		assertEquals("Max Upgrade", stats.get(3).toString());
		assertEquals("Min Upgrade", stats.get(4).toString());
		assertEquals("Mean Upgrade", stats.get(5).toString());
		assertEquals("Sum Upgrade", stats.get(6).toString());

		assertEquals(1.0, stats.get(0).getStat(assignment), 0.0001);
		assertEquals(0.5, stats.get(1).getStat(assignment), 0.0001);
		assertEquals(0.86, stats.get(2).getStat(assignment), 0.0001);
		assertEquals(1, stats.get(3).getStat(assignment), 0.0001);
		assertEquals(0, stats.get(4).getStat(assignment), 0.0001);
		assertEquals(1.0 / 6.0, stats.get(5).getStat(assignment), 0.0001);
		assertEquals(1, stats.get(6).getStat(assignment), 0.0001);

	}

	@Test
	void testHousekeepingSchedule() {

		Instance instance= InstanceFactory.readCSV("test1", 3);
		assertThrows(IllegalArgumentException.class, () -> { new Schedule(null); });
		Schedule schedule= new Schedule(instance);
		assertEquals(false, schedule.isScheduleFor(InstanceFactory.readCSV("test2", 3)));

		ArrayList<Room> rooms= instance.rooms();
		Room room1= rooms.get(0);
		Room room2= rooms.get(1);
		Room room3= rooms.get(2);
		Room room4= rooms.get(3);
		Room room5= rooms.get(4);
		Room room6= rooms.get(5);
		Room room7= new Room(7, 2, 1, 2);

		ArrayList<Housekeeper> housekeepers= schedule.getHousekeepers();
		Housekeeper housekeeper1= housekeepers.get(0);
		Housekeeper housekeeper2= housekeepers.get(1);
		Housekeeper housekeeper3= housekeepers.get(2);
		Housekeeper housekeeper4= new Housekeeper(1);

		assertThrows(IllegalArgumentException.class, () -> { schedule.append(housekeeper4, room1); });
		assertThrows(IllegalArgumentException.class, () -> { schedule.append(housekeeper1, room7); });
		schedule.add(housekeeper1, room4, 4);
		assertThrows(IllegalArgumentException.class, () -> { schedule.append(housekeeper2, room4); });
		schedule.append(housekeeper2, room2);
		schedule.append(housekeeper3, room3);
		schedule.add(housekeeper1, room1, 2);
		schedule.append(housekeeper2, room5);
		schedule.add(housekeeper3, room6, 7);
		assertEquals(true, schedule.isValid());

		assertThrows(IllegalArgumentException.class, () -> { schedule.getAssignment(null); });
		assertThrows(IllegalArgumentException.class, () -> { schedule.getAssignment(room7); });
		assertEquals(housekeeper3, schedule.getAssignment(room6));

		StringBuilder sb= new StringBuilder();
		sb.append("--------------------------\n");
		sb.append("HOUSEKEEPER\t" + "SCHEDULE\n");
		sb.append(String.format("%-12s\t[1, 4]\n", "1"));
		sb.append(String.format("%-12s\t[2, 5]\n", "2"));
		sb.append(String.format("%-12s\t[3, 6]\n", "3"));
		sb.append("--------------------------\n");
		assertEquals(sb.toString(), schedule.toString());

		sb= new StringBuilder();
		sb.append("HOUSEKEEPING STATISTICS\n");
		sb.append("Completion " + schedule.completionStats() + "\n");
		sb.append("Rooms Available: [0, 0, 0, 0, 1, 2, 4, 5, 5, 6]\n");
		assertEquals(4, schedule.getRoomsAvailableAt(6));
		assertEquals(sb.toString(), schedule.getStatsReport());

		sb= new StringBuilder();
		sb.append("#   1   2   3   4   5   6   7   8   9   \n");
		sb.append("1   X   1   1                           \n");
		sb.append("2   X   X   2   2                       \n");
		sb.append("3   X   X   X   3   3                   \n");
		sb.append("4   X   X       1   1                   \n");
		sb.append("5   X   X   X   X   2   2               \n");
		sb.append("6   X                       3   3       \n");
		assertEquals(sb.toString(), schedule.getVisual());

		schedule.reset();

		assertEquals(false, schedule.isValid());
		sb= new StringBuilder();
		sb.append("--------------------------\n");
		sb.append("HOUSEKEEPER\t" + "SCHEDULE\n");
		sb.append(String.format("%-12s\t[]\n", "1"));
		sb.append(String.format("%-12s\t[]\n", "2"));
		sb.append(String.format("%-12s\t[]\n", "3"));
		sb.append("--------------------------\n");
		assertEquals(sb.toString(), schedule.toString());

		sb= new StringBuilder();
		sb.append("HOUSEKEEPING STATISTICS\n");
		sb.append("Completion " + schedule.completionStats() + "\n");
		sb.append("Rooms Available: []\n");
		assertEquals(sb.toString(), schedule.getStatsReport());

	}

	@Test
	void testScheduleStats() {

		Instance instance= InstanceFactory.readCSV("test1", 3);
		Schedule schedule= new Schedule(instance);

		ArrayList<Room> rooms= instance.rooms();
		Room room1= rooms.get(0);
		Room room2= rooms.get(1);
		Room room3= rooms.get(2);
		Room room4= rooms.get(3);
		Room room5= rooms.get(4);
		Room room6= rooms.get(5);

		ArrayList<Housekeeper> housekeepers= schedule.getHousekeepers();
		Housekeeper housekeeper1= housekeepers.get(0);
		Housekeeper housekeeper2= housekeepers.get(1);
		Housekeeper housekeeper3= housekeepers.get(2);

		schedule.add(housekeeper1, room4, 4);
		schedule.append(housekeeper2, room2);
		schedule.append(housekeeper3, room3);
		schedule.add(housekeeper1, room1, 2);
		schedule.append(housekeeper2, room5);
		schedule.add(housekeeper3, room6, 7);

		ArrayList<Statistic<Schedule>> stats= Statistic.SCHEDULE_STATS;

		assertEquals("Makespan", stats.get(0).toString());
		assertEquals("Min Completion Time", stats.get(1).toString());
		assertEquals("Mean Completion Time", stats.get(2).toString());
		assertEquals("Sum Completion Time", stats.get(3).toString());

		assertEquals(8, stats.get(0).getStat(schedule), 0.0001);
		assertEquals(3, stats.get(1).getStat(schedule), 0.0001);
		assertEquals(31.0 / 6.0, stats.get(2).getStat(schedule), 0.0001);
		assertEquals(31, stats.get(3).getStat(schedule), 0.0001);

	}

	@Test
	void testSolution() {

		Instance instance= InstanceFactory.readCSV("test3", 2);
		Instance dummyInstance= InstanceFactory.readCSV("test2", 2);

		Linear assignmentSolver= new Linear();
		FirstAvailable housekeepingSolver= new FirstAvailable();

		Assignment assignment= assignmentSolver.solve(instance);
		Assignment dummyAssignment= assignmentSolver.solve(dummyInstance);
		Schedule schedule= housekeepingSolver.solve(instance);
		Schedule dummySchedule= housekeepingSolver.solve(dummyInstance);

		assertThrows(IllegalArgumentException.class, () -> { new Solution(null, schedule, assignment); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, null, assignment); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, schedule, null); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, dummySchedule, assignment); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, schedule, dummyAssignment); });

		Solution solution= new Solution(instance, schedule, assignment);
		assertEquals(assignment, solution.assignment());
		assertEquals(schedule, solution.schedule());
		assertEquals(19, solution.latenessStats().getSum());
		assertEquals(6, solution.latenessStats().getMax());
		assertEquals(-1, solution.latenessStats().getMin());
		assertEquals(20, solution.tardinessStats().getSum());

		StringBuilder sb= new StringBuilder();
		sb.append(schedule);
		sb.append("\n");
		sb.append(assignment);
		assertEquals(sb.toString(), solution.toString());

	}

	@Test
	void testSolutionStats() {

		Instance instance= InstanceFactory.readCSV("test3", 2);

		Linear assignmentSolver= new Linear();
		FirstAvailable housekeepingSolver= new FirstAvailable();
		Assignment assignment= assignmentSolver.solve(instance);
		Schedule schedule= housekeepingSolver.solve(instance);

		Solution solution= new Solution(instance, schedule, assignment);

		ArrayList<Statistic<Solution>> stats= Statistic.SOLUTION_STATS;

		assertEquals("Max Lateness", stats.get(0).toString());
		assertEquals("Min Lateness", stats.get(1).toString());
		assertEquals("Mean Lateness", stats.get(2).toString());
		assertEquals("Max Tardiness", stats.get(3).toString());
		assertEquals("Min Tardiness", stats.get(4).toString());
		assertEquals("Mean Tardiness", stats.get(5).toString());
		assertEquals("Sum Tardiness", stats.get(6).toString());

		assertEquals(6, stats.get(0).getStat(solution), 0.0001);
		assertEquals(-1, stats.get(1).getStat(solution), 0.0001);
		assertEquals(19.0 / 6.0, stats.get(2).getStat(solution), 0.0001);
		assertEquals(6, stats.get(3).getStat(solution), 0.0001);
		assertEquals(0, stats.get(4).getStat(solution), 0.0001);
		assertEquals(20.0 / 6.0, stats.get(5).getStat(solution), 0.0001);
		assertEquals(20, stats.get(6).getStat(solution), 0.0001);

	}

	@Test
	void testAMPLHelper() {

		Instance instance= InstanceFactory.readCSV("test3", 2);

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runScheduleIP(instance, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runScheduleIP(instance, ""); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runScheduleIP(null, "test3"); });

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runAssignmentIP(instance, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runAssignmentIP(instance, ""); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runAssignmentIP(null, "test3"); });

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runSolutionIP(instance, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runSolutionIP(instance, ""); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runSolutionIP(null, "test3"); });

		AMPL ampl= AMPLHelper.createAMPL();

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadModel(null, "minMakespanTI"); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadModel(ampl, ""); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadModel(ampl, "notAModel"); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadModel(ampl, null); });

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.setHousekeepingParams(null, instance); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.setHousekeepingParams(ampl, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.setRoomAndGuestParams(null, instance); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.setRoomAndGuestParams(ampl, null); });

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.getSchedule(null, instance); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.getSchedule(ampl, null); });

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.getAssignment(ampl, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.getAssignment(null, instance); });

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.getSolution(null, instance); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.getSolution(ampl, null); });

		AMPLHelper.close(ampl);

	}

	@Test
	void testLinear() {

		Linear solver= new Linear();
		Instance instance= InstanceFactory.readCSV("test3", 2);
		System.out.println(instance);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);
		assertEquals((double) 11 / 18, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals((double) 1 / 3, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("Linear", solver.toString());

	}

	@Test
	void testBestFirst() {

		BestFirst solver= new BestFirst();
		Instance instance= InstanceFactory.readCSV("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);
		System.out.println(assignment);
		System.out.println(instance);
		assertEquals(0.748, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.33, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("Best First", solver.toString());

	}

	@Test
	void testWorstFirst() {

		WorstFirst solver= new WorstFirst();
		Instance instance= InstanceFactory.readCSV("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);
		System.out.println(assignment);
		assertEquals(0.387, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.33, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("Worst First", solver.toString());

	}

	@Test
	void testMeanSatIP() {

		assertThrows(IllegalArgumentException.class, () -> { new AssignmentIPSolver(null); });
		AssignmentIPSolver solver= new AssignmentIPSolver("Mean_Satisfaction");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Assignment assignment= solver.solve(instance);
		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("Mean_Satisfaction", solver.toString());
	}

	@Test
	void testMinSatIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Min_Satisfaction");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Assignment assignment= solver.solve(instance);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("Min_Satisfaction", solver.toString());

	}

	@Test
	void testSatisfactionIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Satisfaction");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Assignment assignment= solver.solve(instance);
		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("Satisfaction", solver.toString());

	}

	@Test
	void testUpgrades() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Upgrades");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Assignment assignment= solver.solve(instance);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("Upgrades", solver.toString());

	}

	@Test
	void testMeanWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		assertThrows(IllegalArgumentException.class, () -> { new AssignmentSTScheduleIP(housekeepingSolver, null); });
		assertThrows(IllegalArgumentException.class, () -> { new AssignmentSTScheduleIP(null, "Mean_Wait_Time"); });
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver, "Mean_Wait_Time");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals("Mean_Wait_Time ST Sum_Completion_Time", solver.toString());

	}

	@Test
	void testMaxWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver, "Max_Wait_Time");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.tardinessStats().getMax());
		assertEquals("Max_Wait_Time ST Sum_Completion_Time", solver.toString());

	}

	@Test
	void testMeanSatisfactionMaxWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver,
			"Mean_Satisfaction_And_Wait_Time");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals((double) 31 / 36, solution.assignment().satisfactionStats().getMean(), 0.01);
		assertEquals("Mean_Satisfaction_And_Wait_Time ST Sum_Completion_Time",
			solver.toString());

	}

	@Test
	void testMaxMeanSatSTMinIP() {

		MaxMeanSatSTMinIP solver= new MaxMeanSatSTMinIP();
		Instance instance= InstanceFactory.readCSV("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("maxMeanSatSTMin IP", solver.toString());

		AssignmentIPSolver minSat= new AssignmentIPSolver("Min_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandom("testMaxMeanSatSTMinIP", n);
			double realMin= minSat.solve(instance).satisfactionStats().getMin();
			double min= solver.solve(instance).satisfactionStats().getMin();
			assertEquals(true, realMin >= min);
		}

	}

	@Test
	void testMinUpgradesSTSatIP() {

		assertThrows(IllegalArgumentException.class, () -> { new MinUpgradesSTSatIP(1, -1); });
		assertThrows(IllegalArgumentException.class, () -> { new MinUpgradesSTSatIP(1, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new MinUpgradesSTSatIP(-1, 1); });
		assertThrows(IllegalArgumentException.class, () -> { new MinUpgradesSTSatIP(2, 1); });
		MinUpgradesSTSatIP solver= new MinUpgradesSTSatIP(1, 1);
		Instance instance= InstanceFactory.readCSV("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("minUpgradesSTSat IP", solver.toString());

		MaxMeanSatSTMinIP avgAndMin= new MaxMeanSatSTMinIP();

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandom("testMinUpgradesSTSatIP", n);
			Assignment compare= avgAndMin.solve(instance);
			double realMin= compare.satisfactionStats().getMin();
			double realAvg= compare.satisfactionStats().getMean();
			Assignment real= solver.solve(instance);
			double min= real.satisfactionStats().getMin();
			double avg= real.satisfactionStats().getMean();
			assertEquals(realMin, min, 0.000001);
			assertEquals(realAvg, avg, 0.000001);
		}

	}

	@Test
	void testOnlineMeanSatIP() {

		OnlineMeanSatIP solver= new OnlineMeanSatIP();
		Instance instance= InstanceFactory.readCSV("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals("Online Algorithm", solver.toString());

		AssignmentIPSolver maxAvg= new AssignmentIPSolver("Mean_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandom("testOnlineMeanSatIP", n);
			double realAvg= maxAvg.solve(instance).satisfactionStats().getMean();
			double avg= solver.solve(instance).satisfactionStats().getMean();
			assertEquals(true, Math.abs(realAvg - avg) <= 0.01);
		}

	}

	@Test
	void testSuggestiveMeanSatIP() {

		assertThrows(IllegalArgumentException.class, () -> { new SuggestiveMeanSatIP(-1); });
		assertThrows(IllegalArgumentException.class, () -> { new SuggestiveMeanSatIP(2); });

		SuggestiveMeanSatIP solver= new SuggestiveMeanSatIP(1);
		Instance instance= InstanceFactory.readCSV("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals("Suggestive Algorithm", solver.toString());

		AssignmentIPSolver maxAvg= new AssignmentIPSolver("Mean_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandom("testSuggestiveMeanSatIP", n);
			double realAvg= maxAvg.solve(instance).satisfactionStats().getMean();
			double avg= solver.solve(instance).satisfactionStats().getMean();
			assertEquals(realAvg, avg, 0.01);
		}
	}

	@Test
	void testPreserveEdges() {

		for (int trial= 0; trial < t; trial++ ) {

			Instance instance= null;
			Instance instanceAddOne= null;
			while (instanceAddOne == null) {
				instance= InstanceFactory.createRandom("compareAddGuestBefore", n);
				instanceAddOne= InstanceFactory.addGuestTo(instance, "compareAddGuestAfter");
			}

			Assignment assignment= new AssignmentIPSolver("Mean_Satisfaction").solve(instance);
			PreserveEdgesMeanSat solver= new PreserveEdgesMeanSat(instance, assignment);
			Assignment assignmentAddOne= solver.solve(instanceAddOne);
			int actual= solver.preserveEdges(instanceAddOne);
			int expected= 0;

			for (Guest guest : instance.guests()) {
				if (assignmentAddOne.assignment().get(guest).equals(assignment.assignment().get(guest))) {
					expected++ ;
				}
			}
			assertEquals(expected, actual);
			assertEquals("Mean Sat s.t. Preserved Edges", solver.toString());
		}
	}

	@Test
	void testFirstAvailable() {

		FirstAvailable solver= new FirstAvailable();
		Instance instance= InstanceFactory.readCSV("test1", 3);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Schedule schedule= solver.solve(instance);

		assertEquals(6, schedule.completionStats().getMax());
		assertEquals(3, schedule.getRoomsAvailableAt(5));
		int[] availibilities= { 0, 0, 0, 0, 2, 3, 5, 6 };
		assertEquals(true, Arrays.equals(availibilities, schedule.getRoomsAvailable()));
		assertEquals(26, schedule.completionStats().getSum());
		assertEquals("First Available", solver.toString());

	}

	@Test
	void testNeededFirst() {

		Instance instance4= InstanceFactory.readCSV("test4", 2);
		Instance instance5= InstanceFactory.readCSV("test5", 2);
		NeededFirst needed= new NeededFirst();
		assertThrows(IllegalArgumentException.class, () -> { needed.solve(null); });
		Schedule schedule4= needed.solve(instance4);
		Schedule schedule5= needed.solve(instance5);

		assertEquals(33, schedule4.completionStats().getSum());
		assertEquals(8, schedule4.completionStats().getMax());
		assertEquals(32, schedule5.completionStats().getSum());
		assertEquals(8, schedule5.completionStats().getMax());
		assertEquals("Needed First", needed.toString());

	}

	@Test
	void testNeededFirstNoWait() {

		Instance instance4= InstanceFactory.readCSV("test4", 2);
		Instance instance5= InstanceFactory.readCSV("test5", 2);
		NeededFirstNoWait neededNoWait= new NeededFirstNoWait();
		assertThrows(IllegalArgumentException.class, () -> { neededNoWait.solve(null); });
		Schedule schedule4= neededNoWait.solve(instance4);
		Schedule schedule5= neededNoWait.solve(instance5);

		assertEquals(27, schedule4.completionStats().getSum());
		assertEquals(7, schedule4.completionStats().getMax());
		assertEquals(24, schedule5.completionStats().getSum());
		assertEquals(6, schedule5.completionStats().getMax());
		assertEquals("Needed First No-Wait", neededNoWait.toString());

	}

	@Test
	void testMinSumCompletion() {

		assertThrows(IllegalArgumentException.class, () -> { new ScheduleIPSolver(null); });
		ScheduleIPSolver solver= new ScheduleIPSolver("Sum_Completion_Time");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.readCSV("test3", 3);
		FirstAvailable compare= new FirstAvailable();
		Schedule schedule= solver.solve(instance);

		assertEquals(true, 7 <= schedule.completionStats().getMax());
		assertEquals(29, schedule.completionStats().getSum());
		assertEquals("Sum_Completion_Time", solver.toString());

		for (int i= 0; i < t; i++ ) {
			Instance inst= InstanceFactory.createRandom("rand", n);
			int minSum= (int) solver.solve(inst).completionStats().getSum();
			int sum= (int) compare.solve(inst).completionStats().getSum();
			assertEquals(true, minSum - sum <= 0);
		}
	}

	@Test
	void testMinMakespan() {

		Instance instance= InstanceFactory.readCSV("test3", 3);
		ScheduleIPSolver solver= new ScheduleIPSolver("Makespan");
		FirstAvailable compare= new FirstAvailable();
		Schedule schedule= solver.solve(instance);

		assertEquals(true, 7 <= schedule.completionStats().getMax());
		assertEquals("Makespan", solver.toString());

		for (int i= 0; i < t; i++ ) {
			Instance inst= InstanceFactory.createRandom("rand", n);
			int minMakespan= (int) solver.solve(inst).completionStats().getMax();
			int makespan= (int) compare.solve(inst).completionStats().getMax();
			assertEquals(true, minMakespan - makespan <= 0);
		}

	}

	@Test
	void testSumTardinessSchedule() {
		AssignmentIPSolver assignmentSolver= new AssignmentIPSolver("Mean_Satisfaction");
		assertThrows(IllegalArgumentException.class, () -> { new ScheduleSTAssignmentIP(assignmentSolver, null); });
		assertThrows(IllegalArgumentException.class, () -> { new ScheduleSTAssignmentIP(null, "Sum_Tardiness"); });
		ScheduleSTAssignmentIP solver= new ScheduleSTAssignmentIP(assignmentSolver, "Sum_Tardiness");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals("Sum_Tardiness ST Mean_Satisfaction", solver.toString());
	}

	@Test
	void testMaxTardinessSchedule() {
		AssignmentIPSolver assignmentSolver= new AssignmentIPSolver("Mean_Satisfaction");
		ScheduleSTAssignmentIP solver= new ScheduleSTAssignmentIP(assignmentSolver, "Max_Tardiness");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.tardinessStats().getMax());
		assertEquals("Max_Tardiness ST Mean_Satisfaction", solver.toString());
	}

	@Test
	void testFirstRoom() {

		FirstAvailable housekeepingSolver= new FirstAvailable();
		FirstRoom assignmentSolver= new FirstRoom(housekeepingSolver);
		Instance instance= InstanceFactory.readCSV("test3", 3);
		assertThrows(IllegalArgumentException.class, () -> { new FirstRoom(null); });
		assertThrows(IllegalArgumentException.class, () -> { assignmentSolver.solve(null); });
		Solution solution= assignmentSolver.solve(instance);
		Schedule schedule= solution.schedule();
		Assignment assignment= solution.assignment();

		assertEquals((double) 5 / 12, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals((double) 1 / 3, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());

		assertEquals(true, schedule.isValid());
		assertEquals(7, schedule.completionStats().getMax());
		assertEquals(3, schedule.getRoomsAvailableAt(5));
		int[] availibilities= { 0, 0, 0, 0, 1, 3, 4, 5, 6 };
		assertEquals(true, Arrays.equals(availibilities, schedule.getRoomsAvailable()));
		assertEquals(29, schedule.completionStats().getSum());

		assertEquals(16, solution.tardinessStats().getSum());
		assertEquals("First Room", assignmentSolver.toString());

	}

	@Test
	void testSumTardiness() {
		assertThrows(IllegalArgumentException.class, () -> { new SolutionIPSolver(null); });
		SolutionIPSolver solver= new SolutionIPSolver("Sum_Tardiness");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals("Solution (Sum_Tardiness)", solver.toString());
	}

	@Test
	void testMaxTardiness() {
		SolutionIPSolver solver= new SolutionIPSolver("Max_Tardiness");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.tardinessStats().getMax());
		assertEquals("Solution (Max_Tardiness)", solver.toString());
	}

	@Test
	void testMeanSatisfactionSum_Tardiness() {

		SolutionIPSolver solver= new SolutionIPSolver("Mean_Satisfaction_And_Sum_Tardiness");
		Instance instance= InstanceFactory.readCSV("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals((double) 31 / 36, solution.assignment().satisfactionStats().getMean(), 0.01);
		assertEquals("Solution (Mean_Satisfaction_And_Sum_Tardiness)", solver.toString());

	}
}
