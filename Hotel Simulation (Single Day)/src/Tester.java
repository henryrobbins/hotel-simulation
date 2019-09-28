import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

class Tester {

	@Test
	void testRoom() {

		HashSet<String> attributes= new HashSet<>();
		attributes.add("A");
		attributes.add("B");
		Room test= new Room(1, 2, attributes);

		assertEquals(1, test.getNumber());
		assertEquals(2, test.getType());
		assertEquals(attributes, test.getAttributes());
		assertEquals("Room 1: Type: 2, Attributes: [A, B]", test.toString());

	}

	@Test
	void testGuest() {

		HashSet<String> prefs= new HashSet<>();
		prefs.add("A");
		prefs.add("B");
		HashSet<String> attributes= new HashSet<>();
		attributes.add("A");
		Guest test= new Guest(1, 2, 3, prefs);
		Room testRoom= new Room(1, 1, attributes);

		assertEquals(1, test.getID());
		assertEquals(2, test.getArrivalPosition());
		assertEquals(3, test.getType());
		assertEquals(prefs, test.getPreferences());
		assertEquals(1, test.getMetPreferences(testRoom));
		assertEquals(0.5, test.getAverageSatisfaction(testRoom));
		assertEquals("Guest 1: Type: 3, Preferences: [A, B]", test.toString());

	}

	@Test
	void testSortMethods() {

		HashSet<String> one= new HashSet<>();
		one.add("A");
		one.add("B");
		HashSet<String> two= new HashSet<>();
		two.add("C");
		HashSet<String> three= new HashSet<>();
		three.add("D");
		three.add("E");
		HashSet<String> four= new HashSet<>();
		four.add("A");
		four.add("B");
		four.add("C");
		HashSet<String> five= new HashSet<>();
		five.add("D");
		five.add("B");
		HashSet<String> six= new HashSet<>();
		six.add("C");
		six.add("E");

		ArrayList<Room> rooms= new ArrayList<>();
		rooms.add(new Room(1, 2, one));
		rooms.add(new Room(2, 4, two));
		rooms.add(new Room(3, 5, three));
		rooms.add(new Room(4, 3, four));
		rooms.add(new Room(5, 1, five));
		rooms.add(new Room(6, 1, six));

		ArrayList<Guest> guests= new ArrayList<>();
		guests.add(new Guest(1, 3, 1, one));
		guests.add(new Guest(2, 2, 1, null));
		guests.add(new Guest(3, 6, 2, null));
		guests.add(new Guest(4, 1, 3, null));
		guests.add(new Guest(5, 4, 5, null));
		guests.add(new Guest(6, 5, 4, null));

		Collections.sort(rooms, new Simulation.SortByRoomNumber());
		String test1= "";
		for (Room room : rooms) {
			test1+= room.getNumber() + " ";
		}
		assertEquals("1 2 3 4 5 6 ", test1);

		Collections.sort(rooms, new Simulation.SortByRoomTypeAscending());
		String test2= "";
		for (Room room : rooms) {
			test2+= room.getType() + " ";
		}
		assertEquals("1 1 2 3 4 5 ", test2);

		Collections.sort(rooms, new Simulation.SortByRoomTypeDescending());
		String test3= "";
		for (Room room : rooms) {
			test3+= room.getType() + " ";
		}
		assertEquals("5 4 3 2 1 1 ", test3);

		Guest g= new Guest(1, 3, 1, four);
		Collections.sort(rooms, new Simulation.SortBySatisfactionFor(g));
		String test4= "";
		for (Room room : rooms) {
			test4+= g.getMetPreferences(room) + " ";
		}
		assertEquals("3 2 1 1 1 0 ", test4);

		Collections.sort(guests, new Simulation.SortByGuestType());
		String test5= "";
		for (Guest guest : guests) {
			test5+= guest.getType() + " ";
		}
		assertEquals("5 4 3 2 1 1 ", test5);

		Collections.sort(guests, new Simulation.SortByGuestID());
		String test6= "";
		for (Guest guest : guests) {
			test6+= guest.getID() + " ";
		}
		assertEquals("1 2 3 4 5 6 ", test6);

		Collections.sort(guests, new Simulation.SortByArrivalPosition());
		String test7= "";
		for (Guest guest : guests) {
			test7+= guest.getArrivalPosition() + " ";
		}
		assertEquals("1 2 3 4 5 6 ", test7);

	}

	@Test
	void threeTestCases() {

		Simulation sim1= new Simulation("test1");
		sim1.assignLinearly();
		assertEquals(0, sim1.getTotalUpgrades());
		assertEquals(1, sim1.getMetPreferences());
		assertEquals((double) 4 / 18, sim1.getAverageSatisfaction());
		sim1.reset();
		sim1.assignLexicographically();
		assertEquals(0, sim1.getTotalUpgrades());
		assertEquals(1, sim1.getMetPreferences());
		assertEquals((double) 4 / 18, sim1.getAverageSatisfaction());
		sim1.reset();
		sim1.maximizeMetPreferences();
		assertEquals(0, sim1.getTotalUpgrades());
		assertEquals(1, sim1.getMetPreferences());
		assertEquals((double) 2 / 9, sim1.getAverageSatisfaction());

		Simulation sim2= new Simulation("test2");
		sim2.assignLinearly();
		assertEquals(2, sim2.getTotalUpgrades());
		assertEquals(6, sim2.getMetPreferences());
		assertEquals((double) 2 / 3, sim2.getAverageSatisfaction());
		sim2.reset();
		sim2.assignLexicographically();
		assertEquals(2, sim2.getTotalUpgrades());
		assertEquals(6, sim2.getMetPreferences());
		assertEquals((double) 2 / 3, sim2.getAverageSatisfaction());
		sim2.reset();
		sim2.maximizeMetPreferences();
		assertEquals(2, sim2.getTotalUpgrades());
		assertEquals(10, sim2.getMetPreferences());
		assertEquals(0.9444444444444443, sim2.getAverageSatisfaction());

		Simulation sim3= new Simulation("test3");
		sim3.assignLinearly();
		assertEquals(1, sim3.getTotalUpgrades());
		assertEquals(8, sim3.getMetPreferences());
		assertEquals((double) 19 / 36, sim3.getAverageSatisfaction());
		assertEquals(0, sim3.getMinimumSatisfaction());
		assertEquals(0, sim3.getMinimumPreferences());
		sim3.reset();
		sim3.assignLexicographically();
		assertEquals(1, sim3.getTotalUpgrades());
		assertEquals(11, sim3.getMetPreferences());
		assertEquals(0.7499999999999999, sim3.getAverageSatisfaction());
		assertEquals((double) 1 / 3, sim3.getMinimumSatisfaction());
		assertEquals(1, sim3.getMinimumPreferences());
		sim3.reset();
		sim3.maximizeMetPreferences();
		assertEquals(1, sim3.getTotalUpgrades());
		assertEquals(13, sim3.getMetPreferences());
		assertEquals(0.861111111111111, sim3.getAverageSatisfaction());
		assertEquals((double) 1 / 2, sim3.getMinimumSatisfaction());
		assertEquals(1, sim3.getMinimumPreferences());

	}

	@Test
	void exceptions() {

		assertThrows(IllegalArgumentException.class, () -> { new Simulation("accommodation"); });
		assertThrows(IllegalArgumentException.class, () -> { new Simulation("repeatRoom"); });
		assertThrows(IllegalArgumentException.class, () -> { new Simulation("repeatArrival"); });
		assertThrows(IllegalArgumentException.class, () -> { new Simulation("repeatID"); });

	}

	@Test
	void randomConstructor() {

		for (int i= 0; i < 25; i++ ) {

			Simulation sim= new Simulation(25, "Tester");

			double[][] results= new double[5][5];

			sim.maximizeMetPreferences();
			results[0][0]= sim.getMetPreferences();
			results[0][1]= sim.getMinimumPreferences();
			results[0][2]= sim.getAverageSatisfaction();
			results[0][3]= sim.getMinimumSatisfaction();
			results[0][4]= sim.getTotalUpgrades();
			sim.reset();
			sim.maximizeMinimumPreferences();
			results[1][0]= sim.getMetPreferences();
			results[1][1]= sim.getMinimumPreferences();
			results[1][2]= sim.getAverageSatisfaction();
			results[1][3]= sim.getMinimumSatisfaction();
			results[1][4]= sim.getTotalUpgrades();
			sim.reset();
			sim.maximizeAverageSatisfaction();
			results[2][0]= sim.getMetPreferences();
			results[2][1]= sim.getMinimumPreferences();
			results[2][2]= sim.getAverageSatisfaction();
			results[2][3]= sim.getMinimumSatisfaction();
			results[2][4]= sim.getTotalUpgrades();
			sim.reset();
			sim.maximizeMinimumSatisfaction();
			results[3][0]= sim.getMetPreferences();
			results[3][1]= sim.getMinimumPreferences();
			results[3][2]= sim.getAverageSatisfaction();
			results[3][3]= sim.getMinimumSatisfaction();
			results[3][4]= sim.getTotalUpgrades();
			sim.reset();
			sim.minimizeUpgrades();
			results[4][0]= sim.getMetPreferences();
			results[4][1]= sim.getMinimumPreferences();
			results[4][2]= sim.getAverageSatisfaction();
			results[4][3]= sim.getMinimumSatisfaction();
			results[4][4]= sim.getTotalUpgrades();

			DecimalFormat df= new DecimalFormat("##.#####");

			for (int j= 0; j < 5; j++ ) {
				for (int k= 0; k < 5; k++ ) {
					results[j][k]= Double.parseDouble(df.format(results[j][k]));
				}
			}

			assertEquals(true, results[0][0] >= results[1][0]);
			assertEquals(true, results[0][0] >= results[2][0]);
			assertEquals(true, results[0][0] >= results[3][0]);
			assertEquals(true, results[0][0] >= results[4][0]);

			assertEquals(true, results[1][1] >= results[0][1]);
			assertEquals(true, results[1][1] >= results[2][1]);
			assertEquals(true, results[1][1] >= results[3][1]);
			assertEquals(true, results[1][1] >= results[4][1]);

			assertEquals(true, results[2][2] >= results[0][2]);
			assertEquals(true, results[2][2] >= results[1][2]);
			assertEquals(true, results[2][2] >= results[3][2]);
			assertEquals(true, results[2][2] >= results[4][2]);

			assertEquals(true, results[3][3] >= results[0][3]);
			assertEquals(true, results[3][3] >= results[1][3]);
			assertEquals(true, results[3][3] >= results[2][3]);
			assertEquals(true, results[3][3] >= results[4][3]);

			assertEquals(true, results[4][4] <= results[0][4]);
			assertEquals(true, results[4][4] <= results[1][4]);
			assertEquals(true, results[4][4] <= results[2][4]);
			assertEquals(true, results[4][4] <= results[3][4]);

		}

	}

}
