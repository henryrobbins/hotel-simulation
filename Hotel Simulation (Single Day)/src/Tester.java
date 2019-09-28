import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class Tester {

	@Test
	void testRoom() {

		Room.reset();
		ArrayList<String> attributes= new ArrayList<>();
		attributes.add("A");
		attributes.add("B");
		Room test= new Room(1, attributes);

		assertEquals(1, test.getNumber());
		assertEquals(attributes, test.getAttributes());
		assertThrows(AssertionError.class, () -> { new Room(1, attributes); });
		assertThrows(AssertionError.class, () -> { new Room(1, null); });

	}

	@Test
	void testGuest() {

		Guest.reset();
		ArrayList<String> prefs= new ArrayList<>();
		prefs.add("A");
		prefs.add("B");
		Guest test= new Guest(1, 1, prefs);

		assertEquals(1, test.getID());
		assertEquals(1, test.getArrivalPosition());
		assertEquals(prefs, test.getPreferences());
		assertThrows(AssertionError.class, () -> { new Guest(1, 2, prefs); });
		assertThrows(AssertionError.class, () -> { new Guest(2, 1, prefs); });
		assertThrows(AssertionError.class, () -> { new Guest(1, 1, null); });

	}

	@Test
	void testRoomCSVReader() {

		Room.reset();
		ArrayList<Room> rooms= new ArrayList<>();
		ArrayList<String> attri= new ArrayList<>();
		ArrayList<String> attriABC= new ArrayList<>();
		attriABC.add("A");
		attriABC.add("B");
		attriABC.add("C");

		rooms= RoomCSVReader.read(Paths.get("Test Cases", "roomTest.csv").toString());

		assertEquals(4, rooms.get(1).getNumber());
		assertEquals(attriABC, rooms.get(1).getAttributes());
		assertEquals(attri, rooms.get(3).getAttributes());

	}

	@Test
	void testGuestCSVReader() {

		Guest.reset();
		ArrayList<Guest> guests= new ArrayList<>();
		ArrayList<String> prefs= new ArrayList<>();
		ArrayList<String> prefsABC= new ArrayList<>();
		prefsABC.add("A");
		prefsABC.add("B");
		prefsABC.add("C");

		guests= GuestCSVReader.read(Paths.get("Test Cases", "guestTest.csv").toString());

		assertEquals(4, guests.get(1).getID());
		assertEquals(2, guests.get(1).getArrivalPosition());
		assertEquals(prefsABC, guests.get(1).getPreferences());
		assertEquals(prefs, guests.get(3).getPreferences());

	}

}
