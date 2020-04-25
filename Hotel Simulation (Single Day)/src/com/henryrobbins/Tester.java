package com.henryrobbins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.jupiter.api.Test;

import com.ampl.AMPL;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.decision.Statistic;
import com.henryrobbins.decision.Statistic.PercentBelowTau;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Hotel;
import com.henryrobbins.hotel.HotelFactory;
import com.henryrobbins.hotel.Housekeeper;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.InstanceFactory;
import com.henryrobbins.hotel.Room;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.BestFirst;
import com.henryrobbins.solver.assignment.Linear;
import com.henryrobbins.solver.assignment.MaxMeanSatSTMinIP;
import com.henryrobbins.solver.assignment.MinBelowTau;
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
	private static int t= 25;
	/** The size of the randomly generated hotels */
	private static int n= 10;

	/** The test instances */
	private static Instance[] test= new Instance[6];

	// Instantiate the test instances
	static {
		try {
			for (int i= 0; i < 6; i++ ) {
				Path dir= ResourcesPath.path().resolve("tests").resolve(String.valueOf(i));
				Hotel hotel= HotelFactory.readCSV(dir.resolve("hotel.csv"));
				Path arrivals= dir.resolve(String.valueOf(0)).resolve("arrivals.csv");
				Path weights= dir.resolve(String.valueOf(0)).resolve("weights.csv");
				test[i]= InstanceFactory.readCSV(hotel, arrivals, weights);
			}
		} catch (Exception e) {
			System.out.println("Error reading test files");
			e.printStackTrace();
		}
	}

	@Test
	void testRoom() {

		// tests illegal arguments for constructor
		assertThrows(IllegalArgumentException.class, () -> { new Room(0, 2, 0.5, 0, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 0, 0.5, 0, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 2, 0.5, -1, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 2, 0.5, 0, 0); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 2, -1, 1, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 2, 2, 1, 2); });

		// tests constructor
		Room room= new Room(1, 2, 0.5, 1, 2);
		assertEquals(1, room.num());
		assertEquals(2, room.type());
		assertEquals(0.5, room.quality());
		assertEquals(1, room.release());
		assertEquals(2, room.process());
		assertEquals("1", room.toString());

		// tests ordering
		assertEquals(true, new Room(1, 3, 0.5, 1, 2).compareTo(room) > 0);

		// tests equals
		assertEquals(false, room.equals(null));
		assertEquals(false, room.equals(new Guest(1, 2, 3)));
		assertEquals(false, room.equals(new Room(2, 2, 0.5, 1, 2)));
		assertEquals(false, room.equals(new Room(1, 3, 0.5, 1, 2)));
		assertEquals(false, room.equals(new Room(1, 2, 0.25, 1, 2)));
		assertEquals(false, room.equals(new Room(1, 2, 0.5, 2, 2)));
		assertEquals(false, room.equals(new Room(1, 2, 0.5, 1, 3)));
		assertEquals(true, room.equals(new Room(1, 2, 0.5, 1, 2)));

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

		// tests ordering
		assertEquals(true, new Guest(2, 3, 1).compareTo(guest) > 0);

		// tests equals
		assertEquals(false, guest.equals(null));
		assertEquals(false, guest.equals(new Room(2, 2, 1, 1, 2)));
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

		Room room1= new Room(1, 1, 1, 0, 2);
		Room room2= new Room(2, 1, 1, 3, 2);
		Room room3= new Room(3, 1, 1, 4, 2);
		Room room4= new Room(4, 1, 1, 2, 2);

		housekeeper.appendRoom(room1);
		assertEquals(false, housekeeper.addRoom(room2, 2));
		assertEquals(false, housekeeper.addRoom(room3, 4));
		housekeeper.addRoom(room3, 6);
		housekeeper.addRoom(room2, 4);
		assertEquals(false, housekeeper.addRoom(room4, 3));

		assertEquals(1, housekeeper.id());
		assertEquals(new ArrayList<>(Arrays.asList(room1, room2, room3)), housekeeper.getSchedule());
		assertEquals(1, housekeeper.getStartTime(room1));
		assertEquals(4, housekeeper.getStartTime(room2));
		assertEquals(6, housekeeper.getStartTime(room3));
		assertThrows(IllegalArgumentException.class, () -> { housekeeper.getStartTime(room4); });
		assertEquals(7, housekeeper.getMakespan());

		assertEquals("Housekeeper: 1, Schedule: 1,2,3", housekeeper.toString());

	}

	@Test
	void testHotel() {

		// Does NOT test toCSV method

		Hotel.Builder builder= new Hotel.Builder();

		// tests for illegal arguments
		assertThrows(IllegalArgumentException.class, () -> { builder.setH(0); });
		assertThrows(IllegalArgumentException.class, () -> { builder.addRoom((Room) null); });

		// tests 1 room minimum
		assertThrows(IllegalArgumentException.class, () -> { builder.build(); });

		Room room1= new Room(1, 1, 1, 1, 2);
		builder.addRoom(room1);

		// tests 1 housekeeper minimum
		assertThrows(IllegalArgumentException.class, () -> { builder.build(); });

		// tests illegal arguments for adding a room to the builder (duplicate room number)
		Room room12= new Room(1, 2, 1, 2, 2);
		assertThrows(IllegalArgumentException.class, () -> { builder.addRoom(room12); });

		// tests add room method for builder
		Room room2= new Room(2, 3, 1, 2, 2);
		Room room3= new Room(3, 3, 1, 3, 2);
		builder.addRoom(room2);
		builder.setH(2);
		Hotel differentRooms= builder.build();
		builder.addRoom(room3);
		builder.setH(1);
		Hotel differentH= builder.build();
		builder.setH(2);

		ArrayList<Room> rooms= new ArrayList<>();
		rooms.add(room1);
		rooms.add(room2);
		rooms.add(room3);

		Hotel hotel= builder.build();
		Hotel hotel2= hotel;

		// tests equals
		assertEquals(false, hotel.equals(null));
		assertEquals(false, hotel.equals(room1));
		assertEquals(false, hotel.equals(differentH));
		assertEquals(false, hotel.equals(differentRooms));
		assertEquals(hotel2, hotel);

		assertEquals(room1, hotel.room(1));
		assertEquals(room2, hotel.room(2));
		assertEquals(room3, hotel.room(3));
		assertEquals(rooms, hotel.rooms());
		assertEquals(2, hotel.getH());
		assertEquals(2, hotel.typeSize());

		HashMap<Integer, Integer> typeFreq= new HashMap<>();
		typeFreq.put(1, 1);
		typeFreq.put(3, 2);
		assertEquals(typeFreq, hotel.typeFreq());

		StringBuilder sb= new StringBuilder();
		sb.append("HOTEL\n");
		sb.append("----------------------------------------------------------\n");
		sb.append("ROOM\t" + "TYPE\t" + "QUALITY \t" + "CHECKOUT\t" + "CLEAN TIME\n");
		for (Room room : rooms) {
			sb.append(String.format("%-4d \t", room.num()));
			sb.append(String.format("%-4d \t", room.type()));
			sb.append(String.format("%-8.3f\t", room.quality()));
			sb.append(String.format("%-7d \t", room.release()));
			sb.append(String.format("%-9d \n", room.process()));
		}
		sb.append("----------------------------------------------------------\n");
		sb.append("Housekeeping Team Size: " + hotel.getH() + "\n\n");
		assertEquals(sb.toString(), hotel.toString());
	}

	@Test
	void testInstance() {

		// Does NOT test toCSV methods

		Hotel.Builder hbuilder= new Hotel.Builder();

		Room room1= new Room(1, 1, 1, 1, 2);
		Room room2= new Room(2, 2, 1, 2, 2);
		Room room3= new Room(3, 3, 1, 3, 2);
		Room room4= new Room(4, 1, 1, 1, 1);
		Room room5= new Room(5, 1, 1, 1, 1);

		ArrayList<Room> rooms= new ArrayList<>();
		rooms.add(room1);
		rooms.add(room2);
		rooms.add(room3);

		for (Room room : rooms) {
			hbuilder.addRoom(room);
		}
		hbuilder.setH(2);
		Hotel hotel= hbuilder.build();

		hbuilder.addRoom(room4);
		hbuilder.addRoom(room5);

		Instance.Builder builder= new Instance.Builder(hotel);

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
		assertThrows(IllegalArgumentException.class,
			() -> { builder.addWeight(guest1, new Room(2, 2, 1, 2, 3), 0.5); });
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
		assertEquals(hotel, instance.hotel());
		assertEquals(rooms, instance.rooms());
		assertEquals(room1, instance.room(1));
		assertEquals(guests, instance.guests());
		assertEquals(guest1, instance.guest(1));
		assertEquals(1.0, instance.weight(3, 2), 0.0001);
		assertEquals(0.3, instance.weight(guest2, room3), 0.0001);
		assertEquals(wgts, instance.weights());
		HashMap<Integer, Integer> histo= new HashMap<>();
		histo.put(1, 2);
		histo.put(2, 1);
		assertEquals(histo, instance.reqeustFreq());
		assertEquals(true, instance.feasible());
		assertEquals(2, instance.getH());
		assertEquals(3, instance.typeSize());

		Hotel.Builder builder2Hotel= new Hotel.Builder();
		builder2Hotel.setH(3);
		builder2Hotel.addRoom(new Room(1, 2, 1, 1, 1));
		builder2Hotel.addRoom(new Room(2, 2, 1, 1, 1));
		builder2Hotel.addRoom(new Room(3, 2, 1, 1, 1));
		builder2Hotel.addRoom(new Room(4, 3, 1, 1, 1));
		Instance.Builder builder2= new Instance.Builder(builder2Hotel.build());
		builder2.addGuest(new Guest(1, 1, 1));
		builder2.addGuest(new Guest(2, 3, 1));
		builder2.addGuest(new Guest(3, 3, 1));
		builder2.addGuest(new Guest(4, 3, 1));
		instance= builder2.build();

		assertEquals(-1, instance.maxFeasibleTypeRequest());
		assertEquals(false, instance.feasible());

		// TESTS INSTANCE EQUALS METHOD

		Hotel.Builder primaryBuilderHotel= new Hotel.Builder();
		primaryBuilderHotel.setH(3);
		primaryBuilderHotel.addRoom(new Room(1, 3, 1, 1, 2));
		primaryBuilderHotel.addRoom(new Room(2, 2, 1, 4, 2));
		primaryBuilderHotel.addRoom(new Room(3, 1, 1, 2, 3));
		Hotel primaryHotel= primaryBuilderHotel.build();
		Instance.Builder primaryBuilder= new Instance.Builder(primaryHotel);
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

		Instance.Builder compareBuilder= new Instance.Builder(primaryHotel);
		compareBuilder.addGuest(new Guest(2, 1, 4));
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

		assertEquals(false, primary.equals(null));
		assertEquals(false, primary.equals(compareBuilder));
		assertEquals(false, primary.equals(differentGuests));
		assertEquals(false, primary.equals(differentWeights));
		assertEquals(compare, primary);

		// TESTS BUILDER BUILD METHOD (COPY)

		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder((Hotel) null); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder((Instance) null); });
		Instance copy= new Instance.Builder(primary).build();
		assertEquals(primary, copy);

		// TESTS 1 ROOM MINIMUM AND 1 GUEST MINIMUM

		Instance.Builder noGuests= new Instance.Builder(primaryHotel);
		assertThrows(IllegalArgumentException.class, () -> { noGuests.build(); });

		// TESTS INSTANCE TOSTRING METHOD

		StringBuilder sb= new StringBuilder();
		sb.append("HOTEL\n");
		sb.append("----------------------------------------------------------\n");
		sb.append("ROOM	TYPE	QUALITY 	CHECKOUT	CLEAN TIME\n");
		sb.append("1    	3    	1.000   	1       	2         \n");
		sb.append("2    	2    	1.000   	4       	2         \n");
		sb.append("3    	1    	1.000   	2       	3         \n");
		sb.append("----------------------------------------------------------\n");
		sb.append("Housekeeping Team Size: " + 3 + "\n\n");
		sb.append("ARRIVALS\n");
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
		assertEquals(sb.toString(), primary.toString());
	}

	@Test
	void testHotelFectory() throws Exception {

		// Also tests Hotel toCSV method

		// Build test3 manually for comparison
		Hotel.Builder builderHotel= new Hotel.Builder();
		builderHotel.setH(2);

		builderHotel.addRoom(new Room(1, 1, 0.4975, 2, 2));
		builderHotel.addRoom(new Room(2, 1, 0.79, 2, 2));
		builderHotel.addRoom(new Room(3, 1, 0.565, 4, 2));
		builderHotel.addRoom(new Room(4, 1, 0.4975, 3, 2));
		builderHotel.addRoom(new Room(5, 2, 0.525, 5, 2));
		builderHotel.addRoom(new Room(6, 3, 0.33267, 1, 2));

		Hotel hotel= builderHotel.build();
		assertEquals(hotel, test[2].hotel());

		Path tests= ResourcesPath.path().resolve("tests");
		hotel.writeCSV(tests, "testHotelFactory");
		assertEquals(hotel, HotelFactory.readCSV(tests.resolve("testHotelFactory.csv")));

		for (int i= 0; i < t; i++ ) {
			Hotel rand= HotelFactory.randHotel(n);
			rand.writeCSV(tests, "t" + i);
			assertEquals(rand, HotelFactory.readCSV(tests.resolve("t" + i + ".csv")));
		}

	}

	@Test
	void testInstanceFactory() throws Exception {

		// Also tests Instance toCSV method

		// Build test3 manually for comparison
		Hotel.Builder builderHotel= new Hotel.Builder();
		builderHotel.setH(3);

		builderHotel.addRoom(new Room(1, 1, 1, 2, 2));
		builderHotel.addRoom(new Room(2, 1, 1, 2, 2));
		builderHotel.addRoom(new Room(3, 1, 1, 4, 2));
		builderHotel.addRoom(new Room(4, 1, 1, 3, 2));
		builderHotel.addRoom(new Room(5, 2, 1, 5, 2));
		builderHotel.addRoom(new Room(6, 3, 1, 1, 2));

		Instance.Builder builder= new Instance.Builder(builderHotel.build());

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
		assertEquals(instance, test[5]);
		Path tests= ResourcesPath.path().resolve("tests");
		for (int i= 0; i < t; i++ ) {
			Instance rand= InstanceFactory.randInstance(n);
			rand.hotel().writeCSV(tests, "hotel" + i);
			rand.writeCSV(tests, "inst" + i);
			Path hotelPath= tests.resolve("hotel" + i + ".csv");
			Path arrivalsPath= tests.resolve("inst" + i).resolve("arrivals.csv");
			Path weightsPath= tests.resolve("inst" + i).resolve("weights.csv");
			assertEquals(rand, InstanceFactory.readCSV(hotelPath, arrivalsPath, weightsPath));
		}

		// tests create random instance method
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.randInstance(0); });
		InstanceFactory.randInstance(100);

		// tests add guest method
		Hotel.Builder builder2Hotel= new Hotel.Builder();

		builder2Hotel.setH(3);
		builder2Hotel.addRoom(new Room(1, 1, 1, 2, 2));
		builder2Hotel.addRoom(new Room(2, 3, 1, 2, 2));
		builder2Hotel.addRoom(new Room(3, 2, 1, 4, 2));

		Instance.Builder builder2= new Instance.Builder(builder2Hotel.build());

		builder2.addGuest(new Guest(1, 1, 4));
		builder2.addGuest(new Guest(2, 3, 4));

		builder2.addWeight(builder2.guest(1), builder2.room(1), 1.0);
		builder2.addWeight(builder2.guest(1), builder2.room(2), 0.1);
		builder2.addWeight(builder2.guest(1), builder2.room(3), 0.7);
		builder2.addWeight(builder2.guest(2), builder2.room(1), 0.9);
		builder2.addWeight(builder2.guest(2), builder2.room(2), 0.4);
		builder2.addWeight(builder2.guest(2), builder2.room(3), 0.3);

		Instance before= builder2.build();
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.addGuestTo(null); });
		Instance after= InstanceFactory.addGuestTo(before);
		assertEquals(2, before.maxFeasibleTypeRequest());
		int newGuestType= after.guest(3).type();
		assertEquals(true, newGuestType == 1 || newGuestType == 2);

		Instance full= test[2];
		assertEquals(null, InstanceFactory.addGuestTo(full));

	}

	@Test
	void testRoomAssignment() {

		assertThrows(IllegalArgumentException.class, () -> { new Assignment((Instance) null); });

		// tests assign method
		Instance instance= test[2];
		Assignment assignment= new Assignment(instance);

		// tests isAssignmentFor method
		assertEquals(true, assignment.isAssignmentFor(instance));
		assertEquals(false, assignment.isAssignmentFor(null));

		assertThrows(IllegalArgumentException.class,
			() -> { assignment.assign(instance.guest(1), new Room(2, 3, 1, 4, 1)); });
		assertThrows(IllegalArgumentException.class,
			() -> { assignment.assign(new Guest(7, 2, 1), instance.room(1)); });
		assertEquals(false, assignment.assign(instance.guest(5), instance.room(1)));
		assignment.assign(instance.guest(1), instance.room(5));
		assignment.assign(instance.guest(2), instance.room(6));
		assertThrows(IllegalArgumentException.class,
			() -> { assignment.getMinType(new Guest(7, 2, 1)); });
		assertThrows(IllegalArgumentException.class,
			() -> { assignment.getMinType(instance.guest(3)); });

		assignment.reset();

		assignment.assign(instance.guest(6), instance.room(1));
		assignment.assign(instance.guest(4), instance.room(2));
		assignment.assign(instance.guest(1), instance.room(3));

		assertEquals(false, assignment.assign(instance.guest(2), instance.room(2)));
		assertEquals(false, assignment.assign(instance.guest(1), instance.room(4)));

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

		Instance instance= test[2];
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
		assertEquals("Percent Below 0.8", stats.get(2).toString());
		assertEquals("Mean Satisfaction", stats.get(3).toString());
		assertEquals("Max Upgrade", stats.get(4).toString());
		assertEquals("Min Upgrade", stats.get(5).toString());
		assertEquals("Mean Upgrade", stats.get(6).toString());
		assertEquals("Sum Upgrade", stats.get(7).toString());

		assertEquals(1.0, stats.get(0).getStat(assignment), 0.0001);
		assertEquals(0.5, stats.get(1).getStat(assignment), 0.0001);
		assertEquals(0.33333, stats.get(2).getStat(assignment), 0.0001);
		assertEquals(0.86, stats.get(3).getStat(assignment), 0.0001);
		assertEquals(1, stats.get(4).getStat(assignment), 0.0001);
		assertEquals(0, stats.get(5).getStat(assignment), 0.0001);
		assertEquals(1.0 / 6.0, stats.get(6).getStat(assignment), 0.0001);
		assertEquals(1, stats.get(7).getStat(assignment), 0.0001);

	}

	@Test
	void testHousekeepingSchedule() {

		Instance instance= test[0];
		assertThrows(IllegalArgumentException.class, () -> { new Schedule((Instance) null); });
		Schedule schedule= new Schedule(instance);
		assertEquals(false, schedule.isScheduleFor(test[1]));

		ArrayList<Room> rooms= instance.rooms();
		Room room1= rooms.get(0);
		Room room2= rooms.get(1);
		Room room3= rooms.get(2);
		Room room4= rooms.get(3);
		Room room5= rooms.get(4);
		Room room6= rooms.get(5);
		Room room7= new Room(7, 2, 1, 1, 2);

		ArrayList<Housekeeper> housekeepers= schedule.getHousekeepers();
		Housekeeper housekeeper1= housekeepers.get(0);
		Housekeeper housekeeper2= housekeepers.get(1);
		Housekeeper housekeeper3= housekeepers.get(2);
		Housekeeper housekeeper4= new Housekeeper(1);

		assertThrows(IllegalArgumentException.class, () -> { schedule.append(housekeeper4, room1); });
		assertThrows(IllegalArgumentException.class, () -> { schedule.append(housekeeper1, room7); });
		schedule.add(housekeeper1, room4, 4);
		assertEquals(false, schedule.append(housekeeper2, room4));
		schedule.append(housekeeper2, room2);
		schedule.append(housekeeper3, room3);
		schedule.add(housekeeper1, room1, 2);
		schedule.append(housekeeper2, room5);
		schedule.add(housekeeper3, room6, 7);
		assertEquals(5, schedule.completion(room3));
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

		Instance instance= test[0];
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

		Instance instance= test[2];
		Instance dummyInstance= test[3];

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

		Instance instance= test[2];

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

		Instance instance= test[2];

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
		Instance instance= test[2];
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
		Instance instance= test[2];
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);
		assertEquals(0.75, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.33, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("Best First", solver.toString());

	}

	@Test
	void testWorstFirst() {

		WorstFirst solver= new WorstFirst();
		Instance instance= test[2];
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);
		assertEquals(0.527, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("Worst First", solver.toString());

	}

	@Test
	void testMeanSatIP() {

		assertThrows(IllegalArgumentException.class, () -> { new AssignmentIPSolver(null); });
		AssignmentIPSolver solver= new AssignmentIPSolver("Mean_Satisfaction");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= test[2];
		Assignment assignment= solver.solve(instance);
		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("Mean_Satisfaction", solver.toString());
	}

	@Test
	void testMinSatIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Min_Satisfaction");
		Instance instance= test[2];
		Assignment assignment= solver.solve(instance);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("Min_Satisfaction", solver.toString());

	}

	@Test
	void testMinBelowTau() {

		MinBelowTau solver= new MinBelowTau(0.8);
		Instance instance= test[2];
		Assignment assignment= solver.solve(instance);
		assertEquals(0.333333, new PercentBelowTau(0.8).getStat(assignment), 0.0001);
		assertEquals("Min Below 0.8 IP", solver.toString());

	}

	@Test
	void testSatisfactionIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Satisfaction");
		Instance instance= test[2];
		Assignment assignment= solver.solve(instance);
		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("Satisfaction", solver.toString());

	}

	@Test
	void testUpgrades() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Upgrades");
		Instance instance= test[2];
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
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals("Mean_Wait_Time ST Sum_Completion_Time", solver.toString());

	}

	@Test
	void testMaxWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver, "Max_Wait_Time");
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.tardinessStats().getMax());
		assertEquals("Max_Wait_Time ST Sum_Completion_Time", solver.toString());

	}

	@Test
	void testMeanSatisfactionMaxWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver,
			"Mean_Satisfaction_And_Wait_Time");
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals((double) 31 / 36, solution.assignment().satisfactionStats().getMean(), 0.01);
		assertEquals("Mean_Satisfaction_And_Wait_Time ST Sum_Completion_Time",
			solver.toString());

	}

	@Test
	void testMaxMeanSatSTMinIP() {

		MaxMeanSatSTMinIP solver= new MaxMeanSatSTMinIP();
		Instance instance= test[2];
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals("maxMeanSatSTMin IP", solver.toString());

		AssignmentIPSolver minSat= new AssignmentIPSolver("Min_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.randInstance(n);
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
		Instance instance= test[2];
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals(0.50, assignment.satisfactionStats().getMin(), 0.01);
		assertEquals(1, assignment.upgradeStats().getSum());
		assertEquals("minUpgradesSTSat IP", solver.toString());

		MaxMeanSatSTMinIP avgAndMin= new MaxMeanSatSTMinIP();

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.randInstance(n);
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
		Instance instance= test[2];
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals("Online Algorithm", solver.toString());

		AssignmentIPSolver maxAvg= new AssignmentIPSolver("Mean_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.randInstance(n);
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
		Instance instance= test[2];
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.satisfactionStats().getMean(), 0.01);
		assertEquals("Suggestive Algorithm", solver.toString());

		AssignmentIPSolver maxAvg= new AssignmentIPSolver("Mean_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.randInstance(n);
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
				instance= InstanceFactory.randInstance(n);
				instanceAddOne= InstanceFactory.addGuestTo(instance);
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
		Instance instance= test[0];
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

		Instance instance4= test[3];
		Instance instance5= test[4];
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

		Instance instance4= test[3];
		Instance instance5= test[4];
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
		Instance instance= test[5];
		FirstAvailable compare= new FirstAvailable();
		Schedule schedule= solver.solve(instance);

		assertEquals(true, 7 <= schedule.completionStats().getMax());
		assertEquals(29, schedule.completionStats().getSum());
		assertEquals("Sum_Completion_Time", solver.toString());

		for (int i= 0; i < t; i++ ) {
			Instance inst= InstanceFactory.randInstance(n);
			int minSum= (int) solver.solve(inst).completionStats().getSum();
			int sum= (int) compare.solve(inst).completionStats().getSum();
			assertEquals(true, minSum - sum <= 0);
		}
	}

	@Test
	void testMinMakespan() {

		Instance instance= test[5];
		ScheduleIPSolver solver= new ScheduleIPSolver("Makespan");
		FirstAvailable compare= new FirstAvailable();
		Schedule schedule= solver.solve(instance);

		assertEquals(true, 7 <= schedule.completionStats().getMax());
		assertEquals("Makespan", solver.toString());

		for (int i= 0; i < t; i++ ) {
			Instance inst= InstanceFactory.randInstance(n);
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
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals("Sum_Tardiness ST Mean_Satisfaction", solver.toString());
	}

	@Test
	void testMaxTardinessSchedule() {
		AssignmentIPSolver assignmentSolver= new AssignmentIPSolver("Mean_Satisfaction");
		ScheduleSTAssignmentIP solver= new ScheduleSTAssignmentIP(assignmentSolver, "Max_Tardiness");
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.tardinessStats().getMax());
		assertEquals("Max_Tardiness ST Mean_Satisfaction", solver.toString());
	}

	@Test
	void testFirstRoom() {

		FirstAvailable housekeepingSolver= new FirstAvailable();
		FirstRoom assignmentSolver= new FirstRoom(housekeepingSolver);
		Instance instance= test[5];
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
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals("Solution (Sum_Tardiness)", solver.toString());
	}

	@Test
	void testMaxTardiness() {
		SolutionIPSolver solver= new SolutionIPSolver("Max_Tardiness");
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.tardinessStats().getMax());
		assertEquals("Solution (Max_Tardiness)", solver.toString());
	}

	@Test
	void testMeanSatisfactionSum_Tardiness() {

		SolutionIPSolver solver= new SolutionIPSolver("Mean_Satisfaction_And_Sum_Tardiness");
		Instance instance= test[2];
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.tardinessStats().getSum());
		assertEquals((double) 31 / 36, solution.assignment().satisfactionStats().getMean(), 0.01);
		assertEquals("Solution (Mean_Satisfaction_And_Sum_Tardiness)", solver.toString());

	}
}
