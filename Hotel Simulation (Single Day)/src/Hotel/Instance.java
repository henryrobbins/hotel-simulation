package Hotel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/** An instance maintains a list of hotel rooms and a list of guests arriving for a <br>
 * single day horizon. Essentially maintains all data needed for the AMPL .dat file */
public class Instance {

	/** a list of rooms (must have unique room numbers and fully accommodate guests) */
	private ArrayList<Room> rooms= new ArrayList<>();
	/** a list of guests (must have unique guestIDs and arrival positions) */
	private ArrayList<Guest> guests= new ArrayList<>();

	// GETTERS

	/** Returns an ArrayList of Rooms in the hotel */
	public ArrayList<Room> getRooms() {
		return rooms;
	}

	/** Returns an ArrayList of Guests arriving over a single day horizon */
	public ArrayList<Guest> getGuests() {
		return guests;
	}

	/** Builder class helps to create the immutable Instance */
	public static class Builder {

		/** the name of the instance (for CSV file access and naming purposes) */
		private final String instanceName;
		/** a list of rooms (must have unique room numbers and fully accommodate guests) */
		private ArrayList<Room> rooms= new ArrayList<>();
		/** a list of guests (must have unique guestIDs and arrival positions) */
		private ArrayList<Guest> guests= new ArrayList<>();

		// SETTERS

		public Builder(String instanceName) {
			this.instanceName= instanceName;
		}

		public Builder addRoom(Room room) {
			rooms.add(room);
			return this;
		}

		public Builder addGuest(Guest guest) {
			guests.add(guest);
			return this;
		}

		/** Returns true iff the current rooms list can accommodate ALL guests in guests list */
		public boolean canAccommodate() {

			Collections.sort(rooms, Comparator.comparingInt(Room::getType).reversed());
			Collections.sort(guests, Comparator.comparingInt(Guest::getType).reversed());

			for (int i= 0; i < guests.size(); i++ ) {
				if (rooms.get(i).getType() < guests.get(i).getType()) { return false; }
			}
			return true;
		}

		/** creates associated CSV files in Simulations file in folder named (instanceName) */
		public void convertToCSV() {

			Collections.sort(rooms, Comparator.comparingInt(Room::getNumber));
			Collections.sort(guests, Comparator.comparingInt(Guest::getID));

			File rooms= new File(Paths.get("AMPL", "Simulations", instanceName, "rooms.csv").toString());
			File guests= new File(Paths.get("AMPL", "Simulations", instanceName, "guests.csv").toString());
			FileWriter roomWriter= null;
			FileWriter guestWriter= null;
			try {

				roomWriter= new FileWriter(rooms);
				guestWriter= new FileWriter(guests);
				roomWriter.write("number,type,attributes\n");
				guestWriter.write("id,position,type,prefs\n");

				for (Room room : this.rooms) {

					roomWriter.write(room.getNumber() + "," + room.getType() + ",");
					Iterator<String> iterator= room.getAttributes().iterator();
					while (iterator.hasNext()) {
						String attribute= iterator.next();
						if (!iterator.hasNext()) {
							roomWriter.write(attribute);
						} else {
							roomWriter.write(attribute + ":");
						}
					}
					roomWriter.write("\n");
				}

				for (Guest guest : this.guests) {
					guestWriter.write(guest.getID() + "," + guest.getArrivalPosition() + "," + guest.getType() + ",");

					Iterator<String> iterator= guest.getPreferences().iterator();
					while (iterator.hasNext()) {
						String preferences= iterator.next();
						if (!iterator.hasNext()) {
							guestWriter.write(preferences);
						} else {
							guestWriter.write(preferences + ":");
						}
					}
					guestWriter.write("\n");
				}

				roomWriter.close();
				guestWriter.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public Instance build() {
			return new Instance(rooms, guests);
		}
	}

	/** Constructor: creates an instance with the respective rooms and guests ArrayLists */
	private Instance(ArrayList<Room> rooms, ArrayList<Guest> guests) {
		this.rooms= new ArrayList<>(rooms); // create a copy of rooms to guarantee immutability
		this.guests= new ArrayList<>(guests);
	}

	/** Checks to see if two instances have the same set of guests and rooms (in any order) */
	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Instance.class) return false;
		Instance instance= (Instance) ob;
		Collections.sort(guests, Comparator.comparingInt(Guest::getID));
		Collections.sort(rooms, Comparator.comparingInt(Room::getNumber));
		Collections.sort(instance.guests, Comparator.comparingInt(Guest::getID));
		Collections.sort(instance.rooms, Comparator.comparingInt(Room::getNumber));
		if (!rooms.equals(instance.rooms)) return false;
		if (!guests.equals(instance.guests)) return false;
		return true;
	}

	@Override
	public String toString() {
		System.out.println("ROOMS");
		for (Room room : rooms) {
			System.out.println(room);
		}
		System.out.println();
		System.out.println("GUESTS");
		for (Guest guest : guests) {
			System.out.println(guest);
		}
		return "DONE";
	}
}