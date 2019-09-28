import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;

/** An instance maintains a list of guests and rooms for a simulated hotel day */
public class Simulation {

	/** a list of rooms (must have unique room numbers and fully accommodate guests) */
	private ArrayList<Room> rooms= new ArrayList<>();
	/** a list of guests (must have unique guestIDs and arrival positions */
	private ArrayList<Guest> guests= new ArrayList<>();
	/** a dictionary of rooms and their assigned guest (null if unassigned) */
	private LinkedHashMap<Room, Guest> assignments= new LinkedHashMap<>();
	/** a set of all used room numbers */
	private HashSet<Integer> roomNumbers= new HashSet<>();
	/** a set of all used guest IDs */
	private HashSet<Integer> guestIDs= new HashSet<>();
	/** a set of all used arrival positions */
	private HashSet<Integer> arrivalPositions= new HashSet<>();

	/** Constructor: a new simulation with the list of rooms and guests associated <br>
	 * with the corresponding CSV files at given directories <br>
	 * Precondition: rooms with unique room numbers and guests with unique guest IDs <br>
	 * and arrival positions. Enough rooms to satisfy guest requests (with upgrades) */
	public Simulation(String rCSV, String gCSV) {

		String roomsCSV= Paths.get("Test Cases", rCSV).toString();
		String guestsCSV= Paths.get("Test Cases", gCSV).toString();

		readRoomCSV(roomsCSV);
		readGuestCSV(guestsCSV);

		// Checks precondition that rooms can accommodate guests
		Collections.sort(rooms, new SortByRoomTypeDescending());
		Collections.sort(guests, new SortByGuestType());
		for (int i= 0; i < guests.size(); i++ ) {
			if (rooms.get(i).getType() < guests.get(i).getType()) {
				System.out.println("CSV File Error: rooms can't accomodate guests");
				System.exit(0);
			}
		}

		for (Room room : rooms) {
			assignments.put(room, null);
		}

	}

	// ROOM ASSIGNMENT ALGORITHMS

	/** Makes a valid room assignment by assigning guests to the lowest room type <br>
	 * possible upon their arrival */
	public void assignLinearly() {
		Collections.sort(guests, new SortByArrivalPosition());
		Collections.sort(rooms, new SortByRoomTypeAscending());
		for (Guest guest : guests) {
			for (Room room : rooms) {
				if (assignments.get(room) == null && room.getType() >= guest.getType()) {
					assign(room, guest);
					break;
				}
			}
		}
	}

	/** Makes a valid room assignment by assigning guests to the room of the minimum <br>
	 * valid room type while fulfilling the most preferences upon guest arrival */
	public void assignLexicographically() {
		Collections.sort(guests, new SortByArrivalPosition());
		for (Guest guest : guests) {
			Collections.sort(rooms, new SortBySatisfactionFor(guest));
			int i= guest.getType();
			while (!assignments.containsValue(guest)) {
				for (Room room : rooms) {
					if (assignments.get(room) == null && room.getType() == i) {
						assign(room, guest);
						break;
					}
				}
				i++ ;
			}
		}
	}

	// SIMULATION REPORT METHODS

	/** Prints a report for the given room assignments */
	public void printFullReport() {
		Collections.sort(rooms, new SortByRoomNumber());
		Collections.sort(guests, new SortByGuestID());
		System.out.println();
		System.out.println("HOTEL REPORT");
		System.out.println();
		System.out.println("ROOMS");
		for (Room room : rooms) {
			if (assignments.get(room) != null) {
				System.out.println(room + ", Assigned Guest: " + assignments.get(room).getID() + ", Satisfaction: " +
					assignments.get(room).getTotalSatisfaction(room) + "/" +
					assignments.get(room).getPreferences().size());

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
		printStats();
		System.out.println();
		System.out.println("END OF REPORT");

	}

	/** Prints a report of statistics for given room assignments */
	public void printStats() {

		System.out.println("STATISTICS");
		System.out.println("Total Upgrades: " + getTotalUpgrade());
		System.out.println("Average Upgrades: " + (double) getTotalUpgrade() / (double) guests.size());
		System.out.println("Total Satisfaction: " + getTotalSatisfaction());
		System.out.println("Average Satisfaction: " + getAverageSatisfaction());
		System.out.println();

	}

	/** Resets the room assignments */
	public void reset() {
		assignments.clear();
		for (Room room : rooms) {
			assignments.put(room, null);
		}
	}

	// METHODS FOR SIMULATION STATISTICS

	/** Returns the total met guest preferences for current room assignment */
	public double getTotalSatisfaction() {
		int total= 0;
		for (Room room : rooms) {
			if (assignments.get(room) != null) {
				total+= assignments.get(room).getTotalSatisfaction(room);
			}
		}
		return total;
	}

	/** Returns the average guest satisfaction for current room assignment */
	public double getAverageSatisfaction() {
		double sumOfSatisfaction= 0;
		for (Room room : rooms) {
			if (assignments.get(room) != null) {
				sumOfSatisfaction+= assignments.get(room).getPercentSatisfaction(room);
			}
		}
		return sumOfSatisfaction / guests.size();
	}

	/** Returns the total upgrades (if requested type is 2 and assigned <br>
	 * type is 4 then this assignment would add 2 to the upgrade total) */
	public int getTotalUpgrade() {
		int total= 0;
		for (Room room : rooms) {
			if (assignments.get(room) != null) {
				total+= room.getType() - assignments.get(room).getType();
			}
		}
		return total;
	}

	/** Assigns the Guest guest to Room room <br>
	 * Precondition: room has no guest and vice versa */
	private void assign(Room room, Guest guest) {
		assert assignments.get(room) == null;
		assert !assignments.containsValue(guest);
		assignments.put(room, guest);
	}

	// CSV FILE READER METHODS

	/** Updates the list of Rooms for the simulation to those in the associated <br>
	 * CSV file where the CSV file is of the following format: <br>
	 * unique room number, room type, attributes <br>
	 * EX: 1, 2, A:B:C <br>
	 * Precondition: Every room number in the CSV file is unique */
	private void readRoomCSV(String roomsCSV) {

		BufferedReader br= null;
		try {
			br= new BufferedReader(new FileReader(roomsCSV));

			// Essentially skips first line (HEADER)
			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				// Convert each row to an array of Strings
				String[] values= contentLine.split(",");

				// Get room number
				int num= Integer.parseInt(values[0]);

				// Get room type
				int type= Integer.parseInt(values[1]);

				// Get room attributes
				HashSet<String> attributes= new HashSet<>();
				if (values.length > 2) {
					String[] attr= values[2].split(":");
					for (int i= 0; i < attr.length; i++ ) {
						attributes.add(attr[i]);
					}
				}

				// Checks precondition and updates set of room numbers
				if (!roomNumbers.contains(num)) {
					roomNumbers.add(num);
				} else {
					System.out.println("CSV File Error: repeat room number");
					System.exit(0);
				}

				// Add guest to the list of guests
				rooms.add(new Room(num, type, attributes));
				contentLine= br.readLine();
			}
		} catch (

		IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				System.out.println("Error in closing the BufferedReader");
			}
		}
	}

	/** Updates the list of Guests for the simulation to those in the associated <br>
	 * CSV file where the CSV file is of the following format: <br>
	 * unique ID, unique arrival position, requested type, preferences <br>
	 * EX: 1,1,1,A:B:C <br>
	 * Precondition: Every guestId and arrival position in the CSV file is unique */
	private void readGuestCSV(String guestsCSV) {

		BufferedReader br= null;
		try {
			br= new BufferedReader(new FileReader(guestsCSV));

			// Essentially skips first line (HEADER)
			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				// Convert each row to an array of Strings
				String[] values= contentLine.split(",");

				// Get guest ID
				int id= Integer.parseInt(values[0]);

				// Get guest arrival position
				int position= Integer.parseInt(values[1]);

				// Get requested room type
				int type= Integer.parseInt(values[2]);

				// Get guest preferences
				HashSet<String> preferences= new HashSet<>();
				if (values.length > 3) {
					String[] prefs= values[3].split(":");
					for (int i= 0; i < prefs.length; i++ ) {
						preferences.add(prefs[i]);
					}
				}

				// Checks precondition and updates sets of Id's
				if (!guestIDs.contains(id)) {
					guestIDs.add(id);
				} else {
					System.out.println("CSV File Error: repeat guest ID");
					System.exit(0);
				}

				// Checks precondition and updates sets of arrival positions
				if (!arrivalPositions.contains(position)) {
					arrivalPositions.add(position);
				} else {
					System.out.println("CSV File Error: repeat guest arrival position");
					System.exit(0);
				}

				// Add guest to the list of guests
				guests.add(new Guest(id, position, type, preferences));
				contentLine= br.readLine();
			}
		} catch (

		IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				System.out.println("Error in closing the BufferedReader");
			}
		}
	}

	/** Converts the rooms and bookings lists to a .dat file called name */
	public void convert(String name) {

		Collections.sort(rooms, new SortByRoomNumber());
		Collections.sort(guests, new SortByGuestID());

		File file= new File(Paths.get("Test Cases", name).toString());
		FileWriter writer= null;
		try {

			writer= new FileWriter(file);
			writer.write("data; \n");
			writer.write("\n");

			writer.write("set GUESTS := ");
			for (Guest guest : guests) {
				writer.write(guest.getID() + " ");
			}
			writer.write(";\n");

			writer.write("set ROOMS := ");
			for (Room room : rooms) {
				writer.write(room.getNumber() + " ");
			}
			writer.write(";\n \n");

			writer.write("param: roomType :=");
			for (Room room : rooms) {
				writer.write("\n" + room.getNumber() + " " + room.getType());
			}
			writer.write(" ;\n \n");

			writer.write("param: guestType :=");
			for (Guest guest : guests) {
				writer.write("\n" + guest.getID() + " " + guest.getType());
			}
			writer.write(" ;\n \n");

			writer.write("param sat: \n  ");
			for (Guest guest : guests) {
				writer.write(guest.getID() + " ");
			}
			writer.write(" :=");
			for (Room room : rooms) {
				writer.write("\n" + room.getNumber() + " ");
				for (Guest guest : guests) {
					writer.write(guest.getTotalSatisfaction(room) + " ");
				}
			}
			writer.write(" ;");

		} catch (IOException e) {
			e.printStackTrace(); // I'd rather declare method with throws IOException and omit this catch.
		} finally {
			if (writer != null) try {
				writer.close();
			} catch (IOException ignore) {}
		}
	}

	// METHODS FOR SORTING ROOMS AND GUESTS VIA VARIOUS MEASURES

	/** Used for sorting a list of rooms in ascending order by room number */
	public static class SortByRoomNumber implements Comparator<Room> {
		@Override
		public int compare(Room a, Room b) {
			return a.getNumber() - b.getNumber();
		}
	}

	/** Used for sorting a list of guests in ascending order by guestID */
	public static class SortByGuestID implements Comparator<Guest> {
		@Override
		public int compare(Guest a, Guest b) {
			return a.getID() - b.getID();
		}
	}

	/** Used for sorting a list of rooms in ascending order by room type */
	public static class SortByRoomTypeAscending implements Comparator<Room> {
		@Override
		public int compare(Room a, Room b) {
			return a.getType() - b.getType();
		}
	}

	/** Used for sorting a list of rooms in descending order by room type */
	public static class SortByRoomTypeDescending implements Comparator<Room> {
		@Override
		public int compare(Room a, Room b) {
			return b.getType() - a.getType();
		}
	}

	/** Used for sorting a list of guests in descending order by requested room type */
	public static class SortByGuestType implements Comparator<Guest> {
		@Override
		public int compare(Guest a, Guest b) {
			return b.getType() - a.getType();
		}
	}

	/** Used for sorting a list of rooms in descending order by the number of <br>
	 * preferences met for the given Guest guest's preferences */
	public static class SortBySatisfactionFor implements Comparator<Room> {
		private Guest guest;

		SortBySatisfactionFor(Guest g) {
			guest= g;
		}

		@Override
		public int compare(Room a, Room b) {
			return guest.getTotalSatisfaction(b) - guest.getTotalSatisfaction(a);
		}
	}

	/** Used for sorting a list of guests in ascending order by arrival position */
	public static class SortByArrivalPosition implements Comparator<Guest> {
		@Override
		public int compare(Guest a, Guest b) {
			return a.getArrivalPosition() - b.getArrivalPosition();
		}

	}

}