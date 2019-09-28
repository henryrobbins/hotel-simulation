package Hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import com.ampl.AMPL;

import AMPL.AMPLHelper;
import HousekeepingSolvers.FirstAvailable;
import HousekeepingSolvers.MinAvailabilityPAIP;
import HousekeepingSolvers.NeededFirst;
import HousekeepingSolvers.NeededFirstNoWait;
import HousekeepingSolvers.NoOverlapFeasibilityIP;
import HousekeepingSolvers.ScheduleIPSolver;
import RoomAssignmentSolvers.AssignmentIPSolver;
import RoomAssignmentSolvers.Lexicographic;
import RoomAssignmentSolvers.Linear;
import RoomAssignmentSolvers.MaxMeanSatSTMinIP;
import RoomAssignmentSolvers.MinUpgradesSTSatIP;
import RoomAssignmentSolvers.OnlineMeanSatIP;
import RoomAssignmentSolvers.SuggestiveMeanSatIP;
import Solvers.AssignmentSTScheduleIP;
import Solvers.FirstRoom;
import Solvers.ScheduleSTAssignmentIP;
import Solvers.SolutionIPSolver;

class Tester {

	/** The number of random instances to test */
	private int t= 1;
	/** The size of the randomly generated hotels */
	private int n= 10;

	@Test
	void testRoom() {

		assertThrows(IllegalArgumentException.class,
			() -> { new Room(0, 2, 0, 2, new HashSet<>(Arrays.asList("A", "B"))); });
		assertThrows(IllegalArgumentException.class,
			() -> { new Room(1, 0, 0, 2, new HashSet<>(Arrays.asList("A", "B"))); });
		assertThrows(IllegalArgumentException.class,
			() -> { new Room(1, 2, -1, 2, new HashSet<>(Arrays.asList("A", "B"))); });
		assertThrows(IllegalArgumentException.class,
			() -> { new Room(1, 2, 0, 0, new HashSet<>(Arrays.asList("A", "B"))); });
		assertThrows(IllegalArgumentException.class, () -> { new Room(1, 2, 1, 2, null); });

		Room room= new Room(1, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B")));

		assertEquals(1, room.getNumber());
		assertEquals(2, room.getType());
		assertEquals(1, room.getCheckOut());
		assertEquals(2, room.getCleanTime());
		assertEquals(new HashSet<>(Arrays.asList("A", "B")), room.getAttributes());
		assertEquals("1", room.toString());

		assertEquals(true, new Room(1, 3, 1, 2, new HashSet<>(Arrays.asList("A", "B"))).compareTo(room) > 0);
		assertEquals(true, new Room(1, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B"))).equals(room));
		assertEquals(false, room.equals(null));
		assertEquals(false, room.equals(new Guest(1, 2, 3, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, room.equals(new Room(2, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, room.equals(new Room(1, 3, 1, 2, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, room.equals(new Room(1, 2, 2, 2, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, room.equals(new Room(1, 2, 1, 3, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, room.equals(new Room(1, 2, 1, 2, new HashSet<>(Arrays.asList("A")))));

	}

	@Test
	void testGuest() {

		assertThrows(IllegalArgumentException.class,
			() -> { new Guest(0, 3, 0, new HashSet<>(Arrays.asList("A", "B"))); });
		assertThrows(IllegalArgumentException.class,
			() -> { new Guest(1, 0, 1, new HashSet<>(Arrays.asList("A", "B"))); });
		assertThrows(IllegalArgumentException.class,
			() -> { new Guest(1, 3, -1, new HashSet<>(Arrays.asList("A", "B"))); });
		assertThrows(IllegalArgumentException.class,
			() -> { new Guest(1, 3, 1, null); });

		Guest guest= new Guest(1, 3, 1, new HashSet<>(Arrays.asList("A", "B")));
		Guest guestNoPref= new Guest(1, 3, 1, new HashSet<>());

		assertEquals(1, guest.getID());
		assertEquals(3, guest.getType());
		assertEquals(1, guest.getArrivalTime());
		assertEquals(new HashSet<>(Arrays.asList("A", "B")), guest.getPreferences());
		assertEquals("1", guest.toString());

		assertEquals(false, guest.equals(null));
		assertEquals(false, guest.equals(new Room(2, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, guest.equals(new Guest(2, 3, 1, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, guest.equals(new Guest(1, 4, 1, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, guest.equals(new Guest(1, 3, 2, new HashSet<>(Arrays.asList("A", "B")))));
		assertEquals(false, guest.equals(new Guest(1, 3, 1, new HashSet<>(Arrays.asList("A")))));
		assertEquals(true, guest.equals(new Guest(1, 3, 1, new HashSet<>(Arrays.asList("A", "B")))));

		assertThrows(IllegalArgumentException.class, () -> { guest.getMetPreferences(null); });
		assertThrows(IllegalArgumentException.class, () -> { guest.getSatisfaction(null); });

		Room room= new Room(1, 3, 2, 2, new HashSet<>(Arrays.asList("A")));
		assertEquals(1, guest.getMetPreferences(room));
		assertEquals(0.5, guest.getSatisfaction(room));
		assertEquals(1, guestNoPref.getSatisfaction(room));

	}

	@Test
	void testHousekeeper() {

		assertThrows(IllegalArgumentException.class, () -> { new Housekeeper(0); });

		Housekeeper housekeeper= new Housekeeper(1);

		assertThrows(IllegalArgumentException.class, () -> { housekeeper.appendRoom(null); });
		assertThrows(IllegalArgumentException.class, () -> { housekeeper.addRoom(null, 2); });

		Room room1= new Room(1, 1, 0, 2, new HashSet<>());
		Room room2= new Room(2, 1, 3, 2, new HashSet<>());
		Room room3= new Room(3, 1, 4, 2, new HashSet<>());
		Room room4= new Room(4, 1, 2, 2, new HashSet<>());

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

		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder("", 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder(null, 2); });
		assertThrows(IllegalArgumentException.class, () -> { new Instance.Builder("test", 0); });
		Instance.Builder builder= new Instance.Builder("testInstance", 2);

		Room room1= new Room(1, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B")));
		Room room2= new Room(2, 2, 2, 2, new HashSet<>(Arrays.asList("A")));
		Room room3= new Room(3, 2, 3, 2, new HashSet<>(Arrays.asList("A", "C")));
		ArrayList<Room> rooms= new ArrayList<>(Arrays.asList(room1, room2, room3));

		builder.addRoom(new Room(1, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B"))));
		builder.addRoom(new Room(2, 2, 2, 2, new HashSet<>(Arrays.asList("A"))));
		Instance instance2= builder.build();
		builder.addRoom(new Room(3, 2, 3, 2, new HashSet<>(Arrays.asList("A", "C"))));
		assertThrows(IllegalArgumentException.class,
			() -> { builder.addRoom(new Room(3, 3, 4, 2, new HashSet<>(Arrays.asList("A", "D")))); });

		Guest guest1= new Guest(1, 1, 1, new HashSet<>());
		Guest guest2= new Guest(2, 2, 2, new HashSet<>());
		ArrayList<Guest> guests= new ArrayList<>(Arrays.asList(guest1, guest2));

		builder.addGuest(new Guest(1, 1, 1, new HashSet<>()));
		Instance instance3= builder.build();
		builder.addGuest(new Guest(2, 2, 2, new HashSet<>()));
		assertThrows(IllegalArgumentException.class,
			() -> { builder.addGuest(new Guest(2, 2, 2, new HashSet<>())); });

		Instance instance1= builder.build();
		assertEquals(rooms, instance1.getRooms());
		assertEquals(guests, instance1.getGuests());
		assertEquals(2, instance1.getNumOfHousekeepers());

		Instance instance4= builder.build();
		assertEquals(false, instance1.equals(null));
		assertEquals(false, instance1.equals(room1));
		assertEquals(false, instance1.equals(instance2));
		assertEquals(false, instance1.equals(instance3));
		assertEquals(instance1, instance4);

		StringBuilder sb= new StringBuilder();
		sb.append("----------------------------------------------------------------\n");
		sb.append("ROOM\t" + "TYPE\t" + "CHECKOUT\t" + "CLEAN TIME\t" + "ATTRIBUTES\n");
		sb.append(String.format("%-4d \t%-4d \t%-7d \t%-9d \t" + "[A, B]" + "\n", 1, 2, 1, 2));
		sb.append(String.format("%-4d \t%-4d \t%-7d \t%-9d \t" + "[A]" + "\n", 2, 2, 2, 2));
		sb.append(String.format("%-4d \t%-4d \t%-7d \t%-9d \t" + "[A, C]" + "\n", 3, 2, 3, 2));
		sb.append("----------------------------------------------------------------\n");
		sb.append("-----------------------------------------\n");
		sb.append("GUEST\t" + "TYPE\t" + "ARRIVAL\t" + "PREFERENCES \n");
		sb.append(String.format("%-5d \t%-4d \t%-6d \t" + "[]" + "\n", 1, 1, 1));
		sb.append(String.format("%-5d \t%-4d \t%-6d \t" + "[]" + "\n", 2, 2, 2));
		sb.append("-----------------------------------------\n");
		sb.append("Housekeeping Team Size: 2\n");
		assertEquals(sb.toString(), instance1.toString());

	}

	@Test
	void testInstanceFactory() {

		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.createInstance("accommodation", 2); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.createInstance("test1", 0); });
		assertThrows(IllegalArgumentException.class, () -> { InstanceFactory.createRandomInstance(0, "rand"); });

		Instance test1= InstanceFactory.createInstance("test1", 2);
		Instance test2= InstanceFactory.createInstance("test2", 2);
		Instance test3= InstanceFactory.createInstance("test3", 2);
		Instance test4= InstanceFactory.createInstance("test4", 2);

		ArrayList<Room> test1Rooms= new ArrayList<>();
		test1Rooms.add(new Room(1, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B"))));
		test1Rooms.add(new Room(2, 4, 2, 2, new HashSet<>(Arrays.asList("C"))));
		test1Rooms.add(new Room(3, 5, 3, 2, new HashSet<>(Arrays.asList("D", "E"))));
		test1Rooms.add(new Room(4, 3, 2, 2, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test1Rooms.add(new Room(5, 1, 4, 2, new HashSet<>(Arrays.asList("D", "B"))));
		test1Rooms.add(new Room(6, 1, 1, 2, new HashSet<>(Arrays.asList("C", "E"))));
		ArrayList<Guest> test1Guests= new ArrayList<>();
		test1Guests.add(new Guest(1, 1, 3, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test1Guests.add(new Guest(2, 1, 5, new HashSet<>(Arrays.asList("A"))));
		test1Guests.add(new Guest(3, 2, 4, new HashSet<>(Arrays.asList("D", "E"))));
		test1Guests.add(new Guest(4, 3, 2, new HashSet<>(Arrays.asList())));
		test1Guests.add(new Guest(5, 5, 6, new HashSet<>(Arrays.asList("C", "B"))));
		test1Guests.add(new Guest(6, 4, 4, new HashSet<>(Arrays.asList("D"))));
		ArrayList<Room> test2Rooms= new ArrayList<>();
		test2Rooms.add(new Room(1, 1, 2, 2, new HashSet<>(Arrays.asList("A", "B"))));
		test2Rooms.add(new Room(2, 1, 2, 2, new HashSet<>(Arrays.asList("B", "C"))));
		test2Rooms.add(new Room(3, 1, 1, 2, new HashSet<>(Arrays.asList("A", "C"))));
		test2Rooms.add(new Room(4, 2, 3, 2, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test2Rooms.add(new Room(5, 2, 2, 2, new HashSet<>(Arrays.asList("C"))));
		test2Rooms.add(new Room(6, 3, 4, 2, new HashSet<>(Arrays.asList("A", "C"))));
		ArrayList<Guest> test2Guests= new ArrayList<>();
		test2Guests.add(new Guest(1, 1, 3, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test2Guests.add(new Guest(2, 2, 4, new HashSet<>(Arrays.asList("C"))));
		test2Guests.add(new Guest(3, 1, 5, new HashSet<>(Arrays.asList("A", "B"))));
		test2Guests.add(new Guest(4, 2, 4, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test2Guests.add(new Guest(5, 1, 3, new HashSet<>(Arrays.asList("B", "C"))));
		test2Guests.add(new Guest(6, 1, 2, new HashSet<>(Arrays.asList())));
		ArrayList<Room> test3Rooms= new ArrayList<>();
		test3Rooms.add(new Room(1, 1, 2, 2, new HashSet<>(Arrays.asList("A", "B"))));
		test3Rooms.add(new Room(2, 1, 2, 2, new HashSet<>(Arrays.asList("A", "C", "D"))));
		test3Rooms.add(new Room(3, 1, 4, 2, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test3Rooms.add(new Room(4, 1, 3, 2, new HashSet<>(Arrays.asList("C", "D"))));
		test3Rooms.add(new Room(5, 2, 5, 2, new HashSet<>(Arrays.asList("A", "B"))));
		test3Rooms.add(new Room(6, 3, 1, 2, new HashSet<>(Arrays.asList("C"))));
		ArrayList<Guest> test3Guests= new ArrayList<>();
		test3Guests.add(new Guest(1, 1, 4, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test3Guests.add(new Guest(2, 1, 4, new HashSet<>(Arrays.asList("C", "D"))));
		test3Guests.add(new Guest(3, 2, 3, new HashSet<>(Arrays.asList("A", "C"))));
		test3Guests.add(new Guest(4, 1, 2, new HashSet<>(Arrays.asList("A", "C", "D"))));
		test3Guests.add(new Guest(5, 2, 5, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test3Guests.add(new Guest(6, 1, 2, new HashSet<>(Arrays.asList("A", "B"))));
		ArrayList<Room> test4Rooms= new ArrayList<>();
		test4Rooms.add(new Room(1, 1, 2, 2, new HashSet<>()));
		test4Rooms.add(new Room(2, 2, 1, 2, new HashSet<>()));
		test4Rooms.add(new Room(3, 1, 3, 2, new HashSet<>()));
		test4Rooms.add(new Room(4, 2, 1, 2, new HashSet<>()));
		test4Rooms.add(new Room(5, 1, 2, 2, new HashSet<>()));
		test4Rooms.add(new Room(6, 3, 0, 2, new HashSet<>()));
		ArrayList<Guest> test4Guests= new ArrayList<>();
		test4Guests.add(new Guest(1, 1, 4, new HashSet<>()));
		test4Guests.add(new Guest(2, 1, 4, new HashSet<>()));
		test4Guests.add(new Guest(3, 1, 4, new HashSet<>()));
		test4Guests.add(new Guest(4, 1, 4, new HashSet<>()));

		assertEquals(test1Rooms, test1.getRooms());
		assertEquals(test1Guests, test1.getGuests());
		assertEquals(test2Rooms, test2.getRooms());
		assertEquals(test2Guests, test2.getGuests());
		assertEquals(test3Rooms, test3.getRooms());
		assertEquals(test3Guests, test3.getGuests());
		assertEquals(test4Rooms, test4.getRooms());
		assertEquals(test4Guests, test4.getGuests());

		assertEquals(2, test1.getNumOfHousekeepers());
		assertEquals(2, test2.getNumOfHousekeepers());
		assertEquals(2, test3.getNumOfHousekeepers());

		// MORE INSTANCE TESTING (TESTS toCSV METHOD)
		Instance testA= InstanceFactory.createRandomInstance(5, "testInstanceFactory");
		Instance testB= InstanceFactory.createInstance("testInstanceFactory", 1);
		assertEquals(testA, testB);
		Instance testC= InstanceFactory.createRandomInstance(20, "testInstanceFactory");
		Instance testD= InstanceFactory.createInstance("testInstanceFactory", 2);
		assertEquals(testC, testD);

	}

	@Test
	void testRoomAssignment() {

		assertThrows(IllegalArgumentException.class, () -> { new RoomAssignment(null); });
		Instance instance= InstanceFactory.createInstance("test3", 2);

		Room room1= instance.getRooms().get(0);
		Room room2= instance.getRooms().get(1);
		Room room3= instance.getRooms().get(2);
		Room room4= instance.getRooms().get(3);
		Room room5= instance.getRooms().get(4);
		Room room6= instance.getRooms().get(5);
		Room room7= new Room(7, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B")));

		Guest guest1= instance.getGuests().get(0);
		Guest guest2= instance.getGuests().get(1);
		Guest guest3= instance.getGuests().get(2);
		Guest guest4= instance.getGuests().get(3);
		Guest guest5= instance.getGuests().get(4);
		Guest guest6= instance.getGuests().get(5);
		Guest guest7= new Guest(7, 3, 1, new HashSet<>(Arrays.asList("A", "B")));

		RoomAssignment assignment= new RoomAssignment(instance);
		assertEquals(false, assignment.isAssignmentFor(InstanceFactory.createInstance("test1", 2)));
		assertEquals(true, assignment.isAssignmentFor(instance));

		assertThrows(IllegalArgumentException.class, () -> { assignment.isGuestAssigned(guest7); });
		assertThrows(IllegalArgumentException.class, () -> { assignment.isRoomOpen(room7); });

		assertEquals(false, assignment.isGuestAssigned(guest6));
		assertEquals(true, assignment.isRoomOpen(room1));

		assertThrows(IllegalArgumentException.class, () -> { assignment.assign(room6, guest7); });
		assertThrows(IllegalArgumentException.class, () -> { assignment.assign(room7, guest6); });
		assignment.assign(room1, guest6);
		assertThrows(IllegalArgumentException.class, () -> { assignment.assign(room1, guest5); });
		assertThrows(IllegalArgumentException.class, () -> { assignment.assign(room1, guest4); });
		assertThrows(IllegalArgumentException.class, () -> { assignment.assign(room5, guest6); });

		assertEquals(true, assignment.isGuestAssigned(guest6));
		assertEquals(false, assignment.isRoomOpen(room1));
		assertEquals(instance.getGuests().get(5), assignment.getAssignment().get(instance.getRooms().get(0)));

		assignment.assign(room2, guest4);
		assignment.assign(room3, guest1);
		assignment.assign(room4, guest2);
		assignment.assign(room5, guest5);
		assignment.assign(room6, guest3);

		StringBuilder sb= new StringBuilder();
		sb.append("-------------\n");
		sb.append("ROOM\t" + "GUEST\n");
		sb.append(String.format("%-4s\t%-5s\n", "1", "6"));
		sb.append(String.format("%-4s\t%-5s\n", "2", "4"));
		sb.append(String.format("%-4s\t%-5s\n", "3", "1"));
		sb.append(String.format("%-4s\t%-5s\n", "4", "2"));
		sb.append(String.format("%-4s\t%-5s\n", "5", "5"));
		sb.append(String.format("%-4s\t%-5s\n", "6", "3"));
		sb.append("-------------\n");
		assertEquals(sb.toString(), assignment.toString());

		sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT STATISTICS \n");
		sb.append("Met Preferences: 13 \n");
		sb.append("Minimum Met Preferences: 1 \n");
		sb.append("Average Satisfaction: 0.8611 \n");
		sb.append("Minimum Satisfaction: 0.5000 \n");
		sb.append("Total Upgrades: 1 \n");
		assertEquals(sb.toString(), assignment.getStatsReport());

		assignment.reset();

		sb= new StringBuilder();
		sb.append("-------------\n");
		sb.append("ROOM\t" + "GUEST\n");
		sb.append(String.format("%-4s\t%-5s\n", "1", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "2", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "3", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "4", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "5", "null"));
		sb.append(String.format("%-4s\t%-5s\n", "6", "null"));
		sb.append("-------------\n");
		assertEquals(sb.toString(), assignment.toString());

		sb= new StringBuilder();
		sb.append("ROOM ASSIGNMENT STATISTICS \n");
		sb.append("Met Preferences: 0 \n");
		sb.append("Minimum Met Preferences: 0 \n");
		sb.append("Average Satisfaction: NaN \n");
		sb.append("Minimum Satisfaction: NaN \n");
		sb.append("Total Upgrades: 0 \n");
		assertEquals(sb.toString(), assignment.getStatsReport());

	}

	@Test
	void testHousekeepingSchedule() {

		Instance instance= InstanceFactory.createInstance("test1", 3);
		assertThrows(IllegalArgumentException.class, () -> { new HousekeepingSchedule(null); });
		HousekeepingSchedule test= new HousekeepingSchedule(instance);
		assertEquals(false, test.isScheduleFor(InstanceFactory.createInstance("test2", 3)));

		ArrayList<Room> rooms= instance.getRooms();
		Room room1= rooms.get(0);
		Room room2= rooms.get(1);
		Room room3= rooms.get(2);
		Room room4= rooms.get(3);
		Room room5= rooms.get(4);
		Room room6= rooms.get(5);
		Room room7= new Room(7, 2, 1, 2, new HashSet<>(Arrays.asList("A", "B")));

		ArrayList<Housekeeper> housekeepers= test.getHousekeepers();
		Housekeeper housekeeper1= housekeepers.get(0);
		Housekeeper housekeeper2= housekeepers.get(1);
		Housekeeper housekeeper3= housekeepers.get(2);
		Housekeeper housekeeper4= new Housekeeper(1);

		assertThrows(IllegalArgumentException.class, () -> { test.append(housekeeper4, room1); });
		assertThrows(IllegalArgumentException.class, () -> { test.append(housekeeper1, room7); });
		assertThrows(IllegalArgumentException.class, () -> { room1.getAvailability(test); });
		test.add(housekeeper1, room4, 4);
		assertThrows(IllegalArgumentException.class, () -> { test.append(housekeeper2, room4); });
		test.append(housekeeper2, room2);
		test.append(housekeeper3, room3);
		test.add(housekeeper1, room1, 2);
		test.append(housekeeper2, room5);
		test.add(housekeeper3, room6, 7);
		assertEquals(true, test.isValid());

		assertThrows(IllegalArgumentException.class, () -> { test.getAssignment(null); });
		assertThrows(IllegalArgumentException.class, () -> { test.getStartTime(null); });
		assertThrows(IllegalArgumentException.class, () -> { test.getAssignment(room7); });
		assertThrows(IllegalArgumentException.class, () -> { test.getStartTime(room7); });
		assertEquals(housekeeper3, test.getAssignment(room6));
		assertEquals((Integer) 7, test.getStartTime(room6));

		// MORE ROOM TESTING
		assertThrows(IllegalArgumentException.class, () -> { room1.getAvailability(null); });
		assertEquals(4, room1.getAvailability(test));
		// MORE GUEST TESTING
		Guest guest= new Guest(1, 1, 2, new HashSet<>());
		assertThrows(IllegalArgumentException.class, () -> { guest.getOverlap(room1, null); });
		assertThrows(IllegalArgumentException.class, () -> { guest.getOverlap(null, test); });
		assertEquals(2, guest.getOverlap(room1, test));

		StringBuilder sb= new StringBuilder();
		sb.append("--------------------------\n");
		sb.append("HOUSEKEEPER\t" + "SCHEDULE\n");
		sb.append(String.format("%-12s\t[1, 4]\n", "1"));
		sb.append(String.format("%-12s\t[2, 5]\n", "2"));
		sb.append(String.format("%-12s\t[3, 6]\n", "3"));
		sb.append("--------------------------\n");
		assertEquals(sb.toString(), test.toString());

		sb= new StringBuilder();
		sb.append("HOUSEKEEPING STATISTICS\n");
		sb.append(String.format("Makespan: %d\n", 8));
		sb.append(String.format("Sum of Availabilities: %d\n", 31));
		sb.append("Rooms Available: [0, 0, 0, 0, 1, 2, 4, 5, 5, 6]\n");
		assertEquals(4, test.getRoomsAvailableAt(6));
		assertEquals(sb.toString(), test.getStatsReport());

		sb= new StringBuilder();
		sb.append("#   1   2   3   4   5   6   7   8   9   \n");
		sb.append("1   X   1   1                           \n");
		sb.append("2   X   X   2   2                       \n");
		sb.append("3   X   X   X   3   3                   \n");
		sb.append("4   X   X       1   1                   \n");
		sb.append("5   X   X   X   X   2   2               \n");
		sb.append("6   X                       3   3       \n");
		assertEquals(sb.toString(), test.getVisual());

		test.reset();

		assertEquals(false, test.isValid());
		sb= new StringBuilder();
		sb.append("--------------------------\n");
		sb.append("HOUSEKEEPER\t" + "SCHEDULE\n");
		sb.append(String.format("%-12s\t[]\n", "1"));
		sb.append(String.format("%-12s\t[]\n", "2"));
		sb.append(String.format("%-12s\t[]\n", "3"));
		sb.append("--------------------------\n");
		assertEquals(sb.toString(), test.toString());

		sb= new StringBuilder();
		sb.append("HOUSEKEEPING STATISTICS\n");
		sb.append(String.format("Makespan: %d\n", 0));
		sb.append(String.format("Sum of Availabilities: %d\n", 0));
		sb.append("Rooms Available: []\n");
		assertEquals(sb.toString(), test.getStatsReport());

	}

	@Test
	void testSolution() {

		Instance instance= InstanceFactory.createInstance("test3", 2);
		Instance dummyInstance= InstanceFactory.createInstance("test2", 2);

		Linear assignmentSolver= new Linear();
		RoomAssignment assignment= assignmentSolver.solve(instance);
		RoomAssignment dummyAssignment= assignmentSolver.solve(dummyInstance);
		FirstAvailable housekeepingSolver= new FirstAvailable();
		HousekeepingSchedule schedule= housekeepingSolver.solve(instance);
		HousekeepingSchedule dummySchedule= housekeepingSolver.solve(dummyInstance);

		assertThrows(IllegalArgumentException.class, () -> { new Solution(null, schedule, assignment); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, null, assignment); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, schedule, null); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, dummySchedule, assignment); });
		assertThrows(IllegalArgumentException.class, () -> { new Solution(instance, schedule, dummyAssignment); });
		Solution test= new Solution(instance, schedule, assignment);
		assertEquals(20, test.getTotalOverlap());

		Instance instance2= InstanceFactory.createInstance("test5", 2);
		Lexicographic lexico= new Lexicographic();
		RoomAssignment assignment2= lexico.solve(instance2);
		NeededFirst needFirst= new NeededFirst();
		HousekeepingSchedule schedule2= needFirst.solve(instance2);
		Solution test2= new Solution(instance2, schedule2, assignment2);
		assertEquals(6, test2.getTotalOverlap());

		assertEquals(schedule, test.getSchedule());
		assertEquals(assignment, test.getAssignment());

	}

	@Test
	void testAMPLHelper() {

		Instance instance= InstanceFactory.createInstance("test3", 2);

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runScheduleIP(instance, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runScheduleIP(instance, ""); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runScheduleIP(null, "test3"); });

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runAssignmentIP(instance, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runAssignmentIP(instance, ""); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.runAssignmentIP(null, "test3"); });

		AMPL ampl= AMPLHelper.createAMPL();

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadModel(null, "minMakespanTI"); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadModel(ampl, ""); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadModel(ampl, null); });

		// AMPLHelper.uploadModel(ampl, "minMakespanTI");

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadHousekeepingData(null, instance); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadHousekeepingData(ampl, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadRoomAndGuestData(null, instance); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.uploadRoomAndGuestData(ampl, null); });

		// AMPLHelper.uploadHousekeepingData(ampl, instance);

		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.generateSchedule(null, instance); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.generateSchedule(ampl, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.generateAssignment(ampl, null); });
		assertThrows(IllegalArgumentException.class, () -> { AMPLHelper.generateAssignment(null, instance); });

	}

	@Test
	void testLinear() {

		Linear solver= new Linear();
		Instance instance= InstanceFactory.createInstance("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		RoomAssignment assignment= solver.solve(instance);

		assertEquals((double) 11 / 18, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals((double) 1 / 3, assignment.getMinimumSatisfaction());
		assertEquals(9, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());
		assertEquals("Linear", solver.toString());

	}

	@Test
	void testLexicographic() {

		Lexicographic solver= new Lexicographic();
		Instance instance= InstanceFactory.createInstance("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		RoomAssignment assignment= solver.solve(instance);

		assertEquals((double) 3 / 4, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals((double) 1 / 3, assignment.getMinimumSatisfaction(), 0.000001);
		assertEquals(11, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());
		assertEquals("Lexicographic", solver.toString());

	}

	@Test
	void testMeanSatIP() {

		assertThrows(IllegalArgumentException.class, () -> { new AssignmentIPSolver(null); });
		AssignmentIPSolver solver= new AssignmentIPSolver("Mean_Satisfaction");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.createInstance("test3", 2);
		RoomAssignment assignment= solver.solve(instance);
		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals("Assignment (Mean_Satisfaction)", solver.toString());
	}

	@Test
	void testMinSatIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Min_Satisfaction");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		RoomAssignment assignment= solver.solve(instance);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals("Assignment (Min_Satisfaction)", solver.toString());

	}

	@Test
	void testSatisfactionIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Satisfaction");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		RoomAssignment assignment= solver.solve(instance);
		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals("Assignment (Satisfaction)", solver.toString());

	}

	@Test
	void testMeanPrefIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Mean_Preferences");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		RoomAssignment assignment= solver.solve(instance);
		assertEquals(13, assignment.getMetPreferences());
		assertEquals("Assignment (Mean_Preferences)", solver.toString());

	}

	@Test
	void testMinPrefIP() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Min_Preferences");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		RoomAssignment assignment= solver.solve(instance);
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals("Assignment (Min_Preferences)", solver.toString());

	}

	@Test
	void testUpgrades() {

		AssignmentIPSolver solver= new AssignmentIPSolver("Upgrades");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		RoomAssignment assignment= solver.solve(instance);
		assertEquals(1, assignment.getTotalUpgrades());
		assertEquals("Assignment (Upgrades)", solver.toString());

	}

	@Test
	void testMeanWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		assertThrows(IllegalArgumentException.class, () -> { new AssignmentSTScheduleIP(housekeepingSolver, null); });
		assertThrows(IllegalArgumentException.class, () -> { new AssignmentSTScheduleIP(null, "Mean_Wait_Time"); });
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver, "Mean_Wait_Time");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.getTotalOverlap());
		assertEquals("Assignment (Mean_Wait_Time) ST Schedule (Sum_Completion_Time)", solver.toString());

	}

	@Test
	void testMaxWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver, "Max_Wait_Time");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.getMaximumOverlap());
		assertEquals("Assignment (Max_Wait_Time) ST Schedule (Sum_Completion_Time)", solver.toString());

	}

	@Test
	void testMeanSatisfactionMaxWaitTimeAssignment() {

		ScheduleIPSolver housekeepingSolver= new ScheduleIPSolver("Sum_Completion_Time");
		AssignmentSTScheduleIP solver= new AssignmentSTScheduleIP(housekeepingSolver,
			"Mean_Satisfaction_And_Wait_Time");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.getTotalOverlap());
		assertEquals((double) 31 / 36, solution.getAssignment().getAverageSatisfaction());
		assertEquals("Assignment (Mean_Satisfaction_And_Wait_Time) ST Schedule (Sum_Completion_Time)",
			solver.toString());

	}

	@Test
	void testMaxMeanSatSTMinIP() {

		MaxMeanSatSTMinIP solver= new MaxMeanSatSTMinIP();
		Instance instance= InstanceFactory.createInstance("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		RoomAssignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals("maxMeanSatSTMin IP", solver.toString());

		AssignmentIPSolver minSat= new AssignmentIPSolver("Min_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testMaxMeanSatSTMinIP");
			double realMin= minSat.solve(instance).getMinimumSatisfaction();
			double min= solver.solve(instance).getMinimumSatisfaction();
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
		Instance instance= InstanceFactory.createInstance("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		RoomAssignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals(1, assignment.getTotalUpgrades());
		assertEquals("minUpgradesSTSat IP", solver.toString());

		MaxMeanSatSTMinIP avgAndMin= new MaxMeanSatSTMinIP();

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testMinUpgradesSTSatIP");
			RoomAssignment compare= avgAndMin.solve(instance);
			double realMin= compare.getMinimumSatisfaction();
			double realAvg= compare.getAverageSatisfaction();
			RoomAssignment real= solver.solve(instance);
			double min= real.getMinimumSatisfaction();
			double avg= real.getAverageSatisfaction();
			assertEquals(realMin, min, 0.000001);
			assertEquals(realAvg, avg, 0.000001);
		}

	}

	@Test
	void testOnlineMeanSatIP() {

		OnlineMeanSatIP solver= new OnlineMeanSatIP();
		Instance instance= InstanceFactory.createInstance("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		RoomAssignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals("Online Algorithm", solver.toString());

		AssignmentIPSolver maxAvg= new AssignmentIPSolver("Mean_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testOnlineMeanSatIP");
			double realAvg= maxAvg.solve(instance).getAverageSatisfaction();
			double avg= solver.solve(instance).getAverageSatisfaction();
			assertEquals(true, Math.abs(realAvg - avg) <= 0.000001);
		}

	}

	@Test
	void testSuggestiveMeanSatIP() {

		assertThrows(IllegalArgumentException.class, () -> { new SuggestiveMeanSatIP(-1); });
		assertThrows(IllegalArgumentException.class, () -> { new SuggestiveMeanSatIP(2); });

		SuggestiveMeanSatIP solver= new SuggestiveMeanSatIP(1);
		Instance instance= InstanceFactory.createInstance("test3", 2);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		RoomAssignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals("Suggestive Algorithm", solver.toString());

		AssignmentIPSolver maxAvg= new AssignmentIPSolver("Mean_Satisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testSuggestiveMeanSatIP");
			double realAvg= maxAvg.solve(instance).getAverageSatisfaction();
			double avg= solver.solve(instance).getAverageSatisfaction();
			assertEquals(realAvg, avg, 0.000001);
		}

	}

	@Test
	void testFirstAvailable() {

		FirstAvailable solver= new FirstAvailable();
		Instance instance= InstanceFactory.createInstance("test1", 3);
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		HousekeepingSchedule schedule= solver.solve(instance);

		assertEquals(6, schedule.getMakespan());
		assertEquals(3, schedule.getRoomsAvailableAt(5));
		int[] availibilities= { 0, 0, 0, 0, 2, 3, 5, 6 };
		assertEquals(true, Arrays.equals(availibilities, schedule.getRoomsAvailable()));
		assertEquals(26, schedule.getSumOfAvailabilities());
		assertEquals("First Available", solver.toString());

	}

	@Test
	void testNeededFirst() {

		Instance instance4= InstanceFactory.createInstance("test4", 2);
		Instance instance5= InstanceFactory.createInstance("test5", 2);
		NeededFirst needed= new NeededFirst();
		assertThrows(IllegalArgumentException.class, () -> { needed.solve(null); });
		HousekeepingSchedule schedule4= needed.solve(instance4);
		HousekeepingSchedule schedule5= needed.solve(instance5);

		assertEquals(33, schedule4.getSumOfAvailabilities());
		assertEquals(8, schedule4.getMakespan());
		assertEquals(32, schedule5.getSumOfAvailabilities());
		assertEquals(8, schedule5.getMakespan());
		assertEquals("Needed First", needed.toString());

	}

	@Test
	void testNeededFirstNoWait() {

		Instance instance4= InstanceFactory.createInstance("test4", 2);
		Instance instance5= InstanceFactory.createInstance("test5", 2);
		NeededFirstNoWait neededNoWait= new NeededFirstNoWait();
		assertThrows(IllegalArgumentException.class, () -> { neededNoWait.solve(null); });
		HousekeepingSchedule schedule4= neededNoWait.solve(instance4);
		HousekeepingSchedule schedule5= neededNoWait.solve(instance5);

		assertEquals(27, schedule4.getSumOfAvailabilities());
		assertEquals(7, schedule4.getMakespan());
		assertEquals(24, schedule5.getSumOfAvailabilities());
		assertEquals(6, schedule5.getMakespan());
		assertEquals("Needed First No-Wait", neededNoWait.toString());

	}

	@Test
	void testMinAvailabilityPAIP() {

		Instance instance= InstanceFactory.createInstance("test3", 3);
		MinAvailabilityPAIP solver= new MinAvailabilityPAIP();
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		HousekeepingSchedule schedule= solver.solve(instance);

		assertEquals(true, 7 <= schedule.getMakespan());
		assertEquals(29, schedule.getSumOfAvailabilities());
		assertEquals("minAvailabilityPA IP", solver.toString());

	}

	@Test
	void testMinAvailability() {

		assertThrows(IllegalArgumentException.class, () -> { new ScheduleIPSolver(null); });
		ScheduleIPSolver solver= new ScheduleIPSolver("Sum_Completion_Time");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.createInstance("test3", 3);
		FirstAvailable compare= new FirstAvailable();
		HousekeepingSchedule schedule= solver.solve(instance);

		assertEquals(true, 7 <= schedule.getMakespan());
		assertEquals(29, schedule.getSumOfAvailabilities());
		assertEquals("Schedule (Sum_Completion_Time)", solver.toString());

		for (int i= 0; i < t; i++ ) {
			Instance inst= InstanceFactory.createRandomInstance(n, "rand");
			int minSum= solver.solve(inst).getSumOfAvailabilities();
			int sum= compare.solve(inst).getSumOfAvailabilities();
			assertEquals(true, minSum - sum <= 0);
		}
	}

	@Test
	void testMinMakespan() {

		Instance instance= InstanceFactory.createInstance("test3", 3);
		ScheduleIPSolver solver= new ScheduleIPSolver("Makespan");
		FirstAvailable compare= new FirstAvailable();
		HousekeepingSchedule schedule= solver.solve(instance);

		assertEquals(7, schedule.getMakespan());
		assertEquals(true, 29 <= schedule.getSumOfAvailabilities());
		assertEquals("Schedule (Makespan)", solver.toString());

		for (int i= 0; i < t; i++ ) {
			Instance inst= InstanceFactory.createRandomInstance(n, "rand");
			int minMakespan= solver.solve(inst).getMakespan();
			int makespan= compare.solve(inst).getMakespan();
			assertEquals(true, minMakespan - makespan <= 0);
		}

	}

	@Test
	void testMeanWaitTimeSchedule() {
		AssignmentIPSolver assignmentSolver= new AssignmentIPSolver("Mean_Satisfaction");
		assertThrows(IllegalArgumentException.class, () -> { new ScheduleSTAssignmentIP(assignmentSolver, null); });
		assertThrows(IllegalArgumentException.class, () -> { new ScheduleSTAssignmentIP(null, "Mean_Wait_Time"); });
		ScheduleSTAssignmentIP solver= new ScheduleSTAssignmentIP(assignmentSolver, "Mean_Wait_Time");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.getTotalOverlap());
		assertEquals("Schedule (Mean_Wait_Time) ST Assignment (Mean_Satisfaction)", solver.toString());
	}

	@Test
	void testMaxWaitTimeSchedule() {
		AssignmentIPSolver assignmentSolver= new AssignmentIPSolver("Mean_Satisfaction");
		ScheduleSTAssignmentIP solver= new ScheduleSTAssignmentIP(assignmentSolver, "Max_Wait_Time");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.getMaximumOverlap());
		assertEquals("Schedule (Max_Wait_Time) ST Assignment (Mean_Satisfaction)", solver.toString());
	}

	@Test
	void testNoOverlapFeasibilityIP() {

		Instance instance= InstanceFactory.createInstance("test6", 2);
		NoOverlapFeasibilityIP solver= new NoOverlapFeasibilityIP();
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		HousekeepingSchedule schedule= solver.solve(instance);

		assertEquals(4, schedule.getMakespan());
		assertEquals(12, schedule.getSumOfAvailabilities());
		assertEquals("noOverlapFeasibility IP", solver.toString());

	}

	@Test
	void testFirstRoom() {

		FirstAvailable housekeepingSolver= new FirstAvailable();
		FirstRoom assignmentSolver= new FirstRoom(housekeepingSolver);
		Instance instance= InstanceFactory.createInstance("test3", 3);
//		assertThrows(IllegalArgumentException.class, () -> { assignmentSolver.solve(instance); });
//		assertThrows(IllegalArgumentException.class,
//			() -> { assignmentSolver.solve(instance, housekeepingSolver, assignmentSolver); });
//		assertThrows(IllegalArgumentException.class,
//			() -> { assignmentSolver.solve(instance); });
//		assertThrows(IllegalArgumentException.class, () -> { assignmentSolver.solve(instance, assignmentSolver); });
//		assertThrows(IllegalArgumentException.class, () -> { assignmentSolver.solve(null); });
		Solution solution= assignmentSolver.solve(instance);
		HousekeepingSchedule schedule= solution.getSchedule();
		RoomAssignment assignment= solution.getAssignment();

		assertEquals((double) 5 / 12, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals((double) 1 / 3, assignment.getMinimumSatisfaction());
		assertEquals(6, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

		assertEquals(true, schedule.isValid());
		assertEquals(7, schedule.getMakespan());
		assertEquals(3, schedule.getRoomsAvailableAt(5));
		int[] availibilities= { 0, 0, 0, 0, 1, 3, 4, 5, 6 };
		assertEquals(true, Arrays.equals(availibilities, schedule.getRoomsAvailable()));
		assertEquals(29, schedule.getSumOfAvailabilities());

		assertEquals(16, solution.getTotalOverlap());
		assertEquals("First Room", assignmentSolver.toString());

	}

	@Test
	void testMeanWaitTime() {
		assertThrows(IllegalArgumentException.class, () -> { new SolutionIPSolver(null); });
		SolutionIPSolver solver= new SolutionIPSolver("Mean_Wait_Time");
		assertThrows(IllegalArgumentException.class, () -> { solver.solve(null); });
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.getTotalOverlap());
		assertEquals("Solution (Mean_Wait_Time)", solver.toString());
	}

	@Test
	void testMaxWaitTime() {
		SolutionIPSolver solver= new SolutionIPSolver("Max_Wait_Time");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(4, solution.getMaximumOverlap());
		assertEquals("Solution (Max_Wait_Time)", solver.toString());
	}

	@Test
	void testMeanSatisfactionMaxWaitTime() {

		SolutionIPSolver solver= new SolutionIPSolver("Mean_Satisfaction_And_Wait_Time");
		Instance instance= InstanceFactory.createInstance("test3", 2);
		Solution solution= solver.solve(instance);
		assertEquals(19, solution.getTotalOverlap());
		assertEquals((double) 31 / 36, solution.getAssignment().getAverageSatisfaction());
		assertEquals("Solution (Mean_Satisfaction_And_Wait_Time)", solver.toString());

	}

}
