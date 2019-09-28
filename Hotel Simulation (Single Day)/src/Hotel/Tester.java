package Hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import Solvers.AssignLexicographically;
import Solvers.AssignLinearly;
import Solvers.IPSolver;
import Solvers.MaxAvgSatisfactionSTMin;
import Solvers.OnlineSTAverage;
import Solvers.minUpgradesSTAvgAndMin;
import Solvers.suggestiveSTAverage;

class Tester {

	/** The number of random instances to test */
	private int t= 10;
	/** The size of the randomly generated hotels */
	private int n= 5;

	@Test
	void testRoom() {

		Room test= new Room(1, 2, new HashSet<>(Arrays.asList("A", "B")));

		assertEquals(1, test.getNumber());
		assertEquals(2, test.getType());
		assertEquals(new HashSet<>(Arrays.asList("A", "B")), test.getAttributes());
		assertEquals("Room 1: Type: 2, Attributes: [A, B]", test.toString());

	}

	@Test
	void testGuest() {

		Guest test= new Guest(1, 2, 3, new HashSet<>(Arrays.asList("A", "B")));
		Room testRoom= new Room(1, 1, new HashSet<>(Arrays.asList("A")));

		assertEquals(1, test.getID());
		assertEquals(2, test.getArrivalPosition());
		assertEquals(3, test.getType());
		assertEquals(new HashSet<>(Arrays.asList("A", "B")), test.getPreferences());
		assertEquals(1, test.getMetPreferences(testRoom));
		assertEquals(0.5, test.getAverageSatisfaction(testRoom));
		assertEquals("Guest 1: Type: 3, Preferences: [A, B]", test.toString());

	}

	@Test
	void testInstanceFactory() {

		// tests create InstanceFactory constructor that uses to CSV files: rooms and guests
		Instance test1= InstanceFactory.createInstance("test1");
		Instance test2= InstanceFactory.createInstance("test2");
		Instance test3= InstanceFactory.createInstance("test3");

		ArrayList<Room> test1Rooms= new ArrayList<>();
		test1Rooms.add(new Room(1, 2, new HashSet<>(Arrays.asList("A", "B"))));
		test1Rooms.add(new Room(2, 4, new HashSet<>(Arrays.asList("C"))));
		test1Rooms.add(new Room(3, 5, new HashSet<>(Arrays.asList("D", "E"))));
		test1Rooms.add(new Room(4, 3, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test1Rooms.add(new Room(5, 1, new HashSet<>(Arrays.asList("D", "B"))));
		test1Rooms.add(new Room(6, 1, new HashSet<>(Arrays.asList("C", "E"))));
		ArrayList<Guest> test1Guests= new ArrayList<>();
		test1Guests.add(new Guest(1, 3, 1, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test1Guests.add(new Guest(2, 2, 1, new HashSet<>(Arrays.asList("A"))));
		test1Guests.add(new Guest(3, 6, 2, new HashSet<>(Arrays.asList("D", "E"))));
		test1Guests.add(new Guest(4, 1, 3, new HashSet<>(Arrays.asList())));
		test1Guests.add(new Guest(5, 4, 5, new HashSet<>(Arrays.asList("C", "B"))));
		test1Guests.add(new Guest(6, 5, 4, new HashSet<>(Arrays.asList("D"))));
		ArrayList<Room> test2Rooms= new ArrayList<>();
		test2Rooms.add(new Room(1, 1, new HashSet<>(Arrays.asList("A", "B"))));
		test2Rooms.add(new Room(2, 1, new HashSet<>(Arrays.asList("B", "C"))));
		test2Rooms.add(new Room(3, 1, new HashSet<>(Arrays.asList("A", "C"))));
		test2Rooms.add(new Room(4, 2, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test2Rooms.add(new Room(5, 2, new HashSet<>(Arrays.asList("C"))));
		test2Rooms.add(new Room(6, 3, new HashSet<>(Arrays.asList("A", "C"))));
		ArrayList<Guest> test2Guests= new ArrayList<>();
		test2Guests.add(new Guest(1, 1, 1, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test2Guests.add(new Guest(2, 2, 2, new HashSet<>(Arrays.asList("C"))));
		test2Guests.add(new Guest(3, 3, 1, new HashSet<>(Arrays.asList("A", "B"))));
		test2Guests.add(new Guest(4, 4, 2, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test2Guests.add(new Guest(5, 5, 1, new HashSet<>(Arrays.asList("B", "C"))));
		test2Guests.add(new Guest(6, 6, 1, new HashSet<>(Arrays.asList())));
		ArrayList<Room> test3Rooms= new ArrayList<>();
		test3Rooms.add(new Room(1, 1, new HashSet<>(Arrays.asList("A", "B"))));
		test3Rooms.add(new Room(2, 1, new HashSet<>(Arrays.asList("A", "C", "D"))));
		test3Rooms.add(new Room(3, 1, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test3Rooms.add(new Room(4, 1, new HashSet<>(Arrays.asList("C", "D"))));
		test3Rooms.add(new Room(5, 2, new HashSet<>(Arrays.asList("A", "B"))));
		test3Rooms.add(new Room(6, 3, new HashSet<>(Arrays.asList("C"))));
		ArrayList<Guest> test3Guests= new ArrayList<>();
		test3Guests.add(new Guest(1, 1, 1, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test3Guests.add(new Guest(2, 2, 1, new HashSet<>(Arrays.asList("C", "D"))));
		test3Guests.add(new Guest(3, 3, 2, new HashSet<>(Arrays.asList("A", "C"))));
		test3Guests.add(new Guest(4, 4, 1, new HashSet<>(Arrays.asList("A", "C", "D"))));
		test3Guests.add(new Guest(5, 5, 2, new HashSet<>(Arrays.asList("A", "B", "C"))));
		test3Guests.add(new Guest(6, 6, 1, new HashSet<>(Arrays.asList("A", "B"))));

		assertEquals(test1Rooms, test1.getRooms());
		assertEquals(test1Guests, test1.getGuests());
		assertEquals(test2Rooms, test2.getRooms());
		assertEquals(test2Guests, test2.getGuests());
		assertEquals(test3Rooms, test3.getRooms());
		assertEquals(test3Guests, test3.getGuests());

		// tests the toCSV method
		// tests create InstanceFactory constructor that creates instance from random distribution
		Instance testA= InstanceFactory.createRandomInstance(5, "testInstanceFactory");
		Instance testB= InstanceFactory.createInstance("testInstanceFactory");
		assertEquals(testA, testB);

	}

	@Test
	void testAssignment() {

		Instance testInstance= InstanceFactory.createRandomInstance(5, "testAssignment");
		Assignment testAssignment= new Assignment(testInstance);

		assertEquals(testInstance.getRooms(), new ArrayList<>(testAssignment.getAssignment().keySet()));

		assertEquals(0.0, testAssignment.getAverageSatisfaction());
		assertEquals(0.0, testAssignment.getMinimumSatisfaction());
		assertEquals(0, testAssignment.getMetPreferences());
		assertEquals(0, testAssignment.getMinimumPreferences());
		assertEquals(0, testAssignment.getTotalUpgrades());

		Room room= testInstance.getRooms().get(0);
		Guest guest= testInstance.getGuests().get(0);

		assertEquals(false, testAssignment.isGuestAssigned(guest));
		assertEquals(true, testAssignment.isRoomOpen(room));

		testAssignment.assign(room, guest);

		assertEquals(true, testAssignment.isGuestAssigned(guest));
		assertEquals(false, testAssignment.isRoomOpen(room));
		assertEquals(guest, testAssignment.getAssignment().get(room));

		testAssignment.reset();

		assertEquals(0.0, testAssignment.getAverageSatisfaction());
		assertEquals(0.0, testAssignment.getMinimumSatisfaction());
		assertEquals(0, testAssignment.getMetPreferences());
		assertEquals(0, testAssignment.getMinimumPreferences());
		assertEquals(0, testAssignment.getTotalUpgrades());

		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= new Assignment(instance);

		assignment.assign(instance.getRooms().get(0), instance.getGuests().get(5));
		assignment.assign(instance.getRooms().get(1), instance.getGuests().get(3));
		assignment.assign(instance.getRooms().get(2), instance.getGuests().get(0));
		assignment.assign(instance.getRooms().get(3), instance.getGuests().get(1));
		assignment.assign(instance.getRooms().get(4), instance.getGuests().get(4));
		assignment.assign(instance.getRooms().get(5), instance.getGuests().get(2));

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals(13, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

	}

	@Test
	void testLinear() {

		AssignLinearly solver= new AssignLinearly();
		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 19 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.0, assignment.getMinimumSatisfaction());
		assertEquals(8, assignment.getMetPreferences());
		assertEquals(0, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

	}

	@Test
	void testLexicographic() {

		AssignLexicographically solver= new AssignLexicographically();
		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 3 / 4, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals((double) 1 / 3, assignment.getMinimumSatisfaction(), 0.000001);
		assertEquals(11, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

	}

	@Test
	void testIPSolver() {

		IPSolver solver= new IPSolver("maxMetPrefs");
		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals(13, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

	}

	@Test
	void testMaxAvgSatisfactionSTMin() {

		MaxAvgSatisfactionSTMin solver= new MaxAvgSatisfactionSTMin();
		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals(13, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

		IPSolver minSat= new IPSolver("maxMinimumSatisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testMaxAvgSatisfactionSTMin");
			double realMin= minSat.solve(instance).getMinimumSatisfaction();
			double min= solver.solve(instance).getMinimumSatisfaction();
			assertEquals(true, realMin >= min);
		}

	}

	@Test
	void testMinUpgradesSTAvgAndMin() {

		minUpgradesSTAvgAndMin solver= new minUpgradesSTAvgAndMin(1, 1);
		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals(13, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

		MaxAvgSatisfactionSTMin avgAndMin= new MaxAvgSatisfactionSTMin();

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testMinUpgradesSTAvgAndMin");
			Assignment compare= avgAndMin.solve(instance);
			double realMin= compare.getMinimumSatisfaction();
			double realAvg= compare.getAverageSatisfaction();
			Assignment real= solver.solve(instance);
			double min= real.getMinimumSatisfaction();
			double avg= real.getAverageSatisfaction();
			assertEquals(true, Math.abs(realMin - min) <= 0.000001);
			assertEquals(true, Math.abs(realAvg - avg) <= 0.000001);
		}

	}

	@Test
	void testOnlineSTAverage() {

		OnlineSTAverage solver= new OnlineSTAverage();
		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals(13, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

		IPSolver maxAvg= new IPSolver("maxAverageSatisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testOnlineSTAverage");
			double realAvg= maxAvg.solve(instance).getAverageSatisfaction();
			double avg= solver.solve(instance).getAverageSatisfaction();
			assertEquals(true, Math.abs(realAvg - avg) <= 0.000001);
		}

	}

	@Test
	void testSuggestiveSTAverage() {

		suggestiveSTAverage solver= new suggestiveSTAverage(1);
		Instance instance= InstanceFactory.createInstance("test3");
		Assignment assignment= solver.solve(instance);

		assertEquals((double) 31 / 36, assignment.getAverageSatisfaction(), 0.000001);
		assertEquals(0.50, assignment.getMinimumSatisfaction());
		assertEquals(13, assignment.getMetPreferences());
		assertEquals(1, assignment.getMinimumPreferences());
		assertEquals(1, assignment.getTotalUpgrades());

		IPSolver maxAvg= new IPSolver("maxAverageSatisfaction");

		for (int i= 0; i < t; i++ ) {
			instance= InstanceFactory.createRandomInstance(n, "testSuggestiveSTAverage");
			double realAvg= maxAvg.solve(instance).getAverageSatisfaction();
			double avg= solver.solve(instance).getAverageSatisfaction();
			assertEquals(true, Math.abs(realAvg - avg) <= 0.000001);
		}

	}

}
