import static org.junit.jupiter.api.Assertions.assertEquals;

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
		assertEquals(1, test.getTotalSatisfaction(testRoom));
		assertEquals(0.5, test.getPercentSatisfaction(testRoom));
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
			test4+= g.getTotalSatisfaction(room) + " ";
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
	void testSimulation() {

		Simulation sim1= new Simulation("rooms1.csv", "guests1.csv");
		sim1.printStats();
		sim1.assignLinearly();
		assertEquals(0, sim1.getTotalUpgrade());
		assertEquals(1, sim1.getTotalSatisfaction());
		assertEquals((double) 4 / 18, sim1.getAverageSatisfaction());
		sim1.reset();
		sim1.assignLexicographically();
		assertEquals(0, sim1.getTotalUpgrade());
		assertEquals(1, sim1.getTotalSatisfaction());
		assertEquals((double) 4 / 18, sim1.getAverageSatisfaction());
		sim1.printFullReport();

		Simulation sim2= new Simulation("rooms2.csv", "guests2.csv");
		sim2.assignLinearly();
		assertEquals(2, sim2.getTotalUpgrade());
		assertEquals(6, sim2.getTotalSatisfaction());
		assertEquals((double) 2 / 3, sim2.getAverageSatisfaction());
		sim2.reset();
		sim2.assignLexicographically();
		assertEquals(2, sim2.getTotalUpgrade());
		assertEquals(6, sim2.getTotalSatisfaction());
		assertEquals((double) 2 / 3, sim2.getAverageSatisfaction());
		sim2.printFullReport();

		Simulation sim3= new Simulation("rooms3.csv", "guests3.csv");
		sim3.assignLinearly();
		assertEquals(1, sim3.getTotalUpgrade());
		assertEquals(8, sim3.getTotalSatisfaction());
		assertEquals((double) 19 / 36, sim3.getAverageSatisfaction());
		sim3.printFullReport();
		sim3.reset();
		sim3.assignLexicographically();
		assertEquals(1, sim3.getTotalUpgrade());
		assertEquals(11, sim3.getTotalSatisfaction());
		assertEquals((double) 3 / 4, sim3.getAverageSatisfaction());
		sim3.printFullReport();

	}

}
