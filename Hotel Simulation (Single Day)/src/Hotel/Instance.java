package Hotel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/** Maintains an immutable list of hotel rooms and guests arriving for a single <br>
 * day horizon. Additionally, stores the size of the housekeeping team. */
public final class Instance {

	/** The list of hotel rooms (must have unique room numbers and fully accommodate guests) */
	private final ArrayList<Room> rooms;
	/** The list of guests (must have unique guestIDs) */
	private final ArrayList<Guest> guests;
	/** The size of the housekeeping team (must be greater than 0) */
	private final int housekeeperSize;

	/** Returns the list of rooms in the hotel */
	public ArrayList<Room> getRooms() {
		return new ArrayList<>(rooms);
	}

	/** Returns the list of guests arriving on this single day horizon */
	public ArrayList<Guest> getGuests() {
		return new ArrayList<>(guests);
	}

	/** Returns the size of the housekeeping team */
	public int getNumOfHousekeepers() {
		return housekeeperSize;
	}

	/** Returns the number of room types in this instance */
	public int getTypeSize() {
		HashSet<Integer> types= new HashSet<>();
		for (Room room : rooms) {
			int type= room.getType();
			if (!types.contains(type)) {
				types.add(type);
			}
		}
		return types.size();
	}

	/** Builder class used to create the immutable Instance */
	public static class Builder {

		/** the name of the instance (for CSV file access and naming purposes) */
		private String instanceName;
		/** The list of hotel rooms (must have unique room numbers and fully accommodate guests) */
		private ArrayList<Room> rooms= new ArrayList<>();
		/** The set of used room numbers (to verify uniqueness) */
		private HashSet<Integer> usedRoomNumbers= new HashSet<>();
		/** The list of guests (must have unique guestIDs) */
		private ArrayList<Guest> guests= new ArrayList<>();
		/** The set of used guest IDs (to verify uniqueness) */
		private HashSet<Integer> usedGuestIds= new HashSet<>();
		/** The size of the housekeeping team (must be greater than 1) */
		private int housekeeperSize;

		/** Construct a Builder with given name and h housekeepers
		 *
		 * @param name The name of the instance (not null and at least one character)
		 * @param h    The size of the housekeeping team (greater than 1) */
		public Builder(String name, int h) {
			if (name == null || name.length() < 1) throw new IllegalArgumentException("Name less than one character");
			if (h < 1) throw new IllegalArgumentException("The housekeeping team has a size less than 1");
			instanceName= name;
			housekeeperSize= h;
		}

		/** Add the given room to the list
		 *
		 * @param room A room to be added (with unique room number) */
		public Builder addRoom(Room room) {
			int num= room.getNumber();
			if (usedRoomNumbers.contains(num)) throw new IllegalArgumentException("Non-unique room number");
			rooms.add(room);
			usedRoomNumbers.add(num);
			return this;
		}

		/** Add the given guest to the list
		 *
		 * @param guest A guest to be added (with unique guest ID) */
		public Builder addGuest(Guest guest) {
			int id= guest.getID();
			if (usedGuestIds.contains(id)) throw new IllegalArgumentException("Non-unique guest ID");
			guests.add(guest);
			usedGuestIds.add(id);
			return this;
		}

		/** Return true if the current list of rooms can accommodate all guests in the list of <br>
		 * guests; false otherwise. In other words, every guest can be assigned to a room of a <br>
		 * type satisfying their request. */
		public boolean canAccommodate() {

			Collections.sort(rooms, Comparator.comparingInt(Room::getType).reversed());
			Collections.sort(guests, Comparator.comparingInt(Guest::getType).reversed());

			for (int i= 0; i < guests.size(); i++ ) {
				if (rooms.get(i).getType() < guests.get(i).getType()) { return false; }
			}
			return true;
		}

		/** Creates an associated CSV file in the Simulations folder named (instanceName) */
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
				roomWriter.write("number,type,check-out,cleanTime,attributes\n");
				guestWriter.write("id,type,arrival,prefs\n");

				for (Room room : this.rooms) {

					roomWriter.write(room.getNumber() + "," + room.getType() + "," + room.getCheckOut() + "," +
						room.getCleanTime() + ",");
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
					guestWriter.write(guest.getID() + "," + guest.getType() + "," + guest.getArrivalTime() + ",");

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

		/** Construct an Instance instance from this Builder instance */
		public Instance build() {
			return new Instance(rooms, guests, housekeeperSize);
		}
	}

	/** Construct an instance with the respective list of rooms, guests, and housekeeping team size
	 *
	 * @param rooms  The list of rooms in this instance (unique room numbers)
	 * @param guests The list of guest in this instance (unique guest IDs)
	 * @param h      The size of the housekeeping team (greater than 0) */
	private Instance(ArrayList<Room> rooms, ArrayList<Guest> guests, int h) {
		this.rooms= new ArrayList<>(rooms);
		this.guests= new ArrayList<>(guests);
		housekeeperSize= h;
	}

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

	/** Return a string representing this instance */
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("----------------------------------------------------------------\n");
		sb.append("ROOM\t" + "TYPE\t" + "CHECKOUT\t" + "CLEAN TIME\t" + "ATTRIBUTES\n");
		for (Room room : rooms) {
			sb.append(String.format("%-4d \t", room.getNumber()));
			sb.append(String.format("%-4d \t", room.getType()));
			sb.append(String.format("%-7d \t", room.getCheckOut()));
			sb.append(String.format("%-9d \t", room.getCleanTime()));
			sb.append(room.getAttributes() + "\n");
		}
		sb.append("----------------------------------------------------------------\n");
		sb.append("-----------------------------------------\n");
		sb.append("GUEST\t" + "TYPE\t" + "ARRIVAL\t" + "PREFERENCES \n");
		for (Guest guest : guests) {
			sb.append(String.format("%-5d \t", guest.getID()));
			sb.append(String.format("%-4d \t", guest.getType()));
			sb.append(String.format("%-6d \t", guest.getArrivalTime()));
			sb.append(guest.getPreferences() + "\n");
		}
		sb.append("-----------------------------------------\n");
		sb.append("Housekeeping Team Size: " + housekeeperSize + "\n");
		return sb.toString();
	}
}