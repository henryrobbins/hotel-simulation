import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/** An instance maintains a list of guests and rooms for a simulated hotel day <br>
 * Precondition: there can only be one instance of a Simulation */
public class Simulation {

	/** a list of rooms (must have unique room numbers) */
	private ArrayList<Room> rooms= new ArrayList<>();
	/** a list of guests in order of arrival (must have unique guestIDs and <br>
	 * arrival positions and must be less guests that rooms available) */
	private ArrayList<Guest> guests= new ArrayList<>();
	/** a dictionary of rooms and their assigned guest (null if unassigned) */
	private LinkedHashMap<Room, Guest> assignments= new LinkedHashMap<>();

	/** Constructor: a new simulation with the list of rooms and guests associated <br>
	 * with the corresponding CSV files at given directories <br>
	 * Precondition: rooms with unique room numbers and guests with unique <br>
	 * guest IDs and arrival positions. # of guests <= # of rooms */
	public Simulation(String rCSV, String gCSV) {

		String roomsCSV= Paths.get("Test Cases", rCSV).toString();
		String guestsCSV= Paths.get("Test Cases", gCSV).toString();

		rooms= RoomCSVReader.read(roomsCSV);
		guests= GuestCSVReader.read(guestsCSV);

		assert rooms.size() >= guests.size();

		rooms.sort(null);
		guests.sort(null);

		for (Room room : rooms) {
			assignments.put(room, null);
		}

	}

	/** Makes a valid room assignment using a linear approach */
	public void assignLinearly() {
		for (Guest guest : guests) {
			for (Room room : assignments.keySet()) {
				if (assignments.get(room) == null) {
					assign(room, guest);
					break;
				}
			}
		}
		printReport();
	}

	/** Makes a valid room assignment by assigning to first room to meet all preferences */
	public void assignLexicographically() {
		for (Guest guest : guests) {
			sortFor(guest);
			for (Room room : assignments.keySet()) {
				if (assignments.get(room) == null) {
					assign(room, guest);
					break;
				}
			}
		}
		printReport();
	}

	/** Prints a report for the given room assignments */
	public void printReport() {
		System.out.println();
		System.out.println("HOTEL REPORT");
		System.out.println();
		System.out.println("ROOMS");
		for (Room room : rooms) {
			if (assignments.get(room) != null) {
				System.out.println(room + ", Assigned Guest: " + assignments.get(room).getID());
			} else {
				System.out.println(room + ", Assigned Guest: null");
			}
		}
		System.out.println();
		System.out.println("GUESTS");
		for (Guest guest : guests) {
			System.out.println(guest);
		}
		System.out.println();
		System.out.println("SATISFACTION: " + getSatisfaction());
		System.out.println();
		System.out.println("END OF REPORT");

	}

	/** Resets the room assignments */
	public void reset() {
		assignments.clear();
		for (Room room : rooms) {
			assignments.put(room, null);
		}
	}

	/** Assigns the Guest guest to Room room <br>
	 * Precondition: room has no guest and vice versa */
	private void assign(Room room, Guest guest) {
		assert assignments.get(room) == null;
		assert !assignments.containsValue(guest);
		assignments.put(room, guest);
	}

	/** Returns the average satisfaction of a guest based on preferences met */
	private double getSatisfaction() {
		double sumOfSatisfaction= 0;
		for (Room room : rooms) {
			if (assignments.get(room) != null) {
				sumOfSatisfaction+= assignments.get(room).getSatisfaction(room);
			}
		}
		return sumOfSatisfaction / guests.size();
	}

	/** Sorts the list of rooms based on the given guests preferences <br>
	 * NOTE: Order is irrelevant between rooms granting the same number of preferences */
	private void sortFor(Guest guest) {
		HashMap<Room, Integer> prefsMet= new HashMap<>();

		LinkedHashMap<Room, Guest> temp= new LinkedHashMap<>();

		for (Room room : rooms) {
			prefsMet.put(room, guest.getMetPreferences(room));
		}

		for (int i= guest.getPreferences().size(); i >= 0; i-- ) {
			for (Room room : rooms) {
				if (prefsMet.get(room) == i) {
					temp.put(room, assignments.get(room));
				}
			}
		}

		assignments= temp;
	}

}
