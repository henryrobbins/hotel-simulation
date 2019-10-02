package com.henryrobbins.hotel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections4.map.MultiKeyMap;

/** Maintains an immutable list of hotel rooms and guests arriving over a single day horizon. <br>
 * Furthermore, stores the satisfaction of every guest-room pair. An instance is analogous <br>
 * to a bipartite graph in which the left nodes are guests, the right nodes are rooms, <br>
 * and the edges between them have weights corresponding to the satisfaction of that <br>
 * assignment. Additionally, an instance stores the size of the housekeeping team. */
public final class Instance {

	/** The name of the instance (for CSV file access and naming purposes) */
	private String name;
	/** The list of hotel rooms (must have unique room numbers and fully accommodate guests) */
	private final ArrayList<Room> rooms;
	/** The map of unique room numbers to rooms */
	private final HashMap<Integer, Room> roomMap;
	/** The number of rooms of a given type */
	private final HashMap<Integer, Integer> typeFrequency;
	/** The list of hotel guests (must have unique guest IDs) */
	private final ArrayList<Guest> guests;
	/** The map of unique guest IDs to guests */
	private final HashMap<Integer, Guest> guestMap;
	/** The number of requests for a given room type */
	private final HashMap<Integer, Integer> requestFrequency;
	/** The weight of every guest-room pair */
	private final MultiKeyMap<Object, Double> weights;
	/** The size of the housekeeping team (must be greater than 0) */
	private final int teamSize;

	/** Return the name of the instance */
	public String name() {
		return new String(name);
	}

	/** Return the list of guests */
	public ArrayList<Guest> guests() {
		return new ArrayList<>(guests);
	}

	/** Return the guest with the given guest ID. Return null if no guest with this ID. */
	public Guest guest(int id) {
		return guestMap.get(id);
	}

	/** Return the list of rooms */
	public ArrayList<Room> rooms() {
		return new ArrayList<>(rooms);
	}

	/** Return the room with the given room number. Return null if no room with this number. */
	public Room room(int num) {
		return roomMap.get(num);
	}

	/** Return the weight of the given guest-room pair
	 *
	 * @param g   The unique ID of the guest
	 * @param r   The unique room number of the room
	 * @param wgt The weight of the edge between guest g and room r */
	public Double weight(int g, int r) {
		return weight(guest(g), room(r));
	}

	/** Return the weight of the given guest-room pair */
	public Double weight(Guest guest, Room room) {
		return weights.get(guest, room);
	}

	/** Return the map of weights */
	public MultiKeyMap<Object, Double> weights() {
		MultiKeyMap<Object, Double> weights= new MultiKeyMap<>();
		for (Guest guest : guests) {
			for (Room room : rooms) {
				weights.put(guest, room, this.weights.get(guest, room));
			}
		}
		return weights;
	}

	/** Return the size of the housekeeping team */
	public int teamSize() {
		return teamSize;
	}

	/** Return the number of room types in this instance */
	public int typeSize() {
		return typeFrequency.size();
	}

	/** Return the maximum type t such that a guest requesting type t^* <= t can be accommodated. <br>
	 * Hence, 0 is returned if no guest can be added. Return -1 if currently infeasible */
	public int maxFeasibleTypeRequest() {
		// If the highest requested type is larger than the highest available type, clearly infeasible
		int maxType= Collections.max(typeFrequency.keySet());
		int maxRequest= Collections.max(requestFrequency.keySet());
		if (maxType < maxRequest) return -1;

		// Set the number of available rooms of type 1 or greater
		int available= rooms.size();
		// Set the number of requests for rooms of type 1 or greater
		int requests= guests.size();
		// Set the minimum room type where available = requests (no slack)
		int noSlack= maxType + 1;

		// Iterate through all possible room types
		for (int t= 1; t <= maxType; t++ ) {
			// Must be infeasible
			if (available < requests) return -1;
			// Identify the lowest tier in which there is no slack
			if (available == requests) { noSlack= Math.min(noSlack, t); }
			Integer avail= typeFrequency.get(t);
			Integer req= requestFrequency.get(t);
			// Set the number of available rooms of type t+1 or greater
			available-= avail == null ? 0 : avail;
			// Set the number of requests for rooms of type t+1 or greater
			requests-= req == null ? 0 : req;
		}
		return noSlack - 1;

	}

	/** Return true if the list of rooms can accommodate all guests (where "accommodate" entails <br>
	 * that every guest can be assigned a room type satisfying their request); false otherwise */
	public boolean feasible() {
		return maxFeasibleTypeRequest() >= 0;
	}

	/** Return the quality of every room. A room's quality is considered to be the <br>
	 * average satisfaction of a guest who can feasibly be assigned this room */
	public HashMap<Room, Double> roomQualities() {

		HashMap<Room, Double> quality= new HashMap<>();
		for (Room room : rooms) {
			int feasibleGuests= 0;
			int totalQuality= 0;
			for (Guest guest : guests) {
				if (room.type() >= guest.type()) {
					feasibleGuests++ ;
					totalQuality+= weights.get(guest, room);
				}
			}
			quality.put(room, feasibleGuests != 0 ? totalQuality / feasibleGuests : 0.0);
		}
		return quality;
	}

	/** Builder class used to create the immutable Instance */
	public static class Builder {

		/** the name of the instance (for CSV file access and naming purposes) */
		private String name;
		/** The list of hotel rooms (must have unique room numbers and fully accommodate guests) */
		private ArrayList<Room> rooms= new ArrayList<>();
		/** The map of unique room numbers to rooms */
		private HashMap<Integer, Room> roomMap= new HashMap<>();
		/** The number of rooms of a given type */
		private HashMap<Integer, Integer> typeFrequency= new HashMap<>();
		/** The set of used room numbers (to verify uniqueness) */
		private HashSet<Integer> usedNums= new HashSet<>();
		/** The list of hotel guests (must have unique guest IDs) */
		private ArrayList<Guest> guests= new ArrayList<>();
		/** The map of unique guest IDs to guests */
		private HashMap<Integer, Guest> guestMap= new HashMap<>();
		/** The number of requests for a given room type */
		private HashMap<Integer, Integer> requestFrequency= new HashMap<>();
		/** The set of used guest IDs (to verify uniqueness) */
		private HashSet<Integer> usedIDs= new HashSet<>();
		/** The weight of every guest-room pair */
		private MultiKeyMap<Object, Double> weights= new MultiKeyMap<>();
		/** The size of the housekeeping team (must be greater than 0) */
		private int teamSize;

		/** Construct a Builder with given name and number of housekeepers
		 *
		 * @param name The name of the instance (at least one character)
		 * @param h    The size of the housekeeping team (greater than 1) */
		public Builder(String name, int h) {
			if (name == null || name.length() < 1) throw new IllegalArgumentException("Name less than one character");
			if (h < 1) throw new IllegalArgumentException("The housekeeping team has a size less than 1");
			this.name= name;
			teamSize= h;
		}

		/** Construct a Builder that is a copy of the given instance
		 *
		 * @param instance The instance that this Builder is a copy of */
		public Builder(String name, Instance instance) {
			if (name == null || name.length() < 1) throw new IllegalArgumentException("Name less than one character");
			if (instance == null) throw new IllegalArgumentException("Instance was null");
			this.name= new String(name);
			teamSize= instance.teamSize;
			for (Room room : instance.rooms) {
				addRoom(room);
			}
			for (Guest guest : instance.guests) {
				addGuest(guest);
			}
			weights.putAll(instance.weights);
		}

		/** Return the list of guests */
		public ArrayList<Guest> guests() {
			return guests;
		}

		/** Return the guest with the given guest ID. Return null if no guest with this ID. */
		public Guest guest(int id) {
			return guestMap.get(id);
		}

		/** Return the list of rooms */
		public ArrayList<Room> rooms() {
			return rooms;
		}

		/** Return the room with the given room number. Return null if no room with this number. */
		public Room room(int num) {
			return roomMap.get(num);
		}

		/** Return the map of weights */
		public MultiKeyMap<Object, Double> weights() {
			return weights;
		}

		/** Add the given room to the list
		 *
		 * @param room A room to be added (with unique room number) */
		public Builder addRoom(Room room) {
			if (room == null) throw new IllegalArgumentException("Room was null");
			int num= room.num();
			int type= room.type();
			if (usedNums.contains(num)) throw new IllegalArgumentException("Non-unique room number");
			rooms.add(room);
			roomMap.put(num, room);
			if (!typeFrequency.containsKey(type)) {
				typeFrequency.put(type, 1);
			} else {
				int prev= typeFrequency.get(type);
				typeFrequency.put(type, prev + 1);
			}
			usedNums.add(num);
			return this;
		}

		/** Add the given guest to the list
		 *
		 * @param guest A guest to be added (with unique guest ID) */
		public Builder addGuest(Guest guest) {
			if (guest == null) throw new IllegalArgumentException("Guest was null");
			int id= guest.id();
			int type= guest.type();
			if (usedIDs.contains(id)) throw new IllegalArgumentException("Non-unique guest ID");
			guests.add(guest);
			guestMap.put(id, guest);
			if (!requestFrequency.containsKey(type)) {
				requestFrequency.put(type, 1);
			} else {
				int prev= requestFrequency.get(type);
				requestFrequency.put(type, prev + 1);
			}
			usedIDs.add(id);
			return this;
		}

		/** Add the given weight between the given guest and room
		 *
		 * @param guest The guest the weight is associated with (in the instance)
		 * @param room  The room the weight is associated with (in the instance)
		 * @param wgt   The weight to be added (represents satisfaction) in 0..1 */
		public Builder addWeight(Guest guest, Room room, Double wgt) {
			if (!guests.contains(guest)) throw new IllegalArgumentException("Guest not in instance");
			if (!rooms.contains(room)) throw new IllegalArgumentException("Room not in instance");
			if (wgt < 0 || wgt > 1) throw new IllegalArgumentException("Weight not in [0,1]");
			weights.put(guest, room, wgt);
			return this;
		}

		/** Construct an instance from this Builder instance */
		public Instance build() {
			return new Instance(name, rooms, roomMap, typeFrequency,
				guests, guestMap, requestFrequency, weights, teamSize);
		}
	}

	/** Construct an instance with the respective list of rooms, guests, weights, and team size. <br>
	 * Any guest-room pair not given a weight will be given a defualt weight of zero.
	 *
	 * @param rooms  The list of rooms in this instance (unique room numbers)
	 * @param guests The list of guest in this instance (unique guest IDs)
	 * @param weight The weights between guest-room pairs (in 0..1)
	 * @param h      The size of the housekeeping team (greater than 0) */
	private Instance(String name, ArrayList<Room> rooms, HashMap<Integer, Room> roomMap,
		HashMap<Integer, Integer> typeFrequency, ArrayList<Guest> guests,
		HashMap<Integer, Guest> guestMap, HashMap<Integer, Integer> requestFrequency,
		MultiKeyMap<Object, Double> weights, int teamSize) {
		this.name= new String(name);
		this.rooms= new ArrayList<>(rooms);
		this.roomMap= new HashMap<>(roomMap);
		this.typeFrequency= new HashMap<>(typeFrequency);
		this.guests= new ArrayList<>(guests);
		this.guestMap= new HashMap<>(guestMap);
		this.requestFrequency= new HashMap<>(requestFrequency);
		this.weights= new MultiKeyMap<>();
		for (Guest guest : guests) {
			for (Room room : rooms) {
				Double wgt= weights.get(guest, room);
				this.weights.put(guest, room, wgt == null ? 0.0 : wgt);
			}
		}
		this.teamSize= teamSize;
	}

	/** Create a directory under the same name as the instance containing three <br>
	 * CSV files describing this instance in the given directory
	 *
	 * @param dir The directory where the CSV file directory will be placed */
	public void writeCSV(Path dir) {

		dir= Paths.get(dir.toString(), name);
		new File(dir.toString()).mkdirs();
		writeGuestsCSV(dir);
		writeRoomsCSV(dir);
		writeWeightsCSV(dir);
	}

	/** Create a directory under the same name as the instance containing three <br>
	 * CSV files describing this instance in the "Simulations" directory
	 *
	 * @param dir The directory where the CSV file directory will be placed */
	public void writeCSV() {
		writeCSV(Paths.get("AMPL", "Simulations"));
	}

	/** Write the guests.csv file and place it in the given directory
	 *
	 * @param dir The directory where the guests.csv file will be placed */
	private void writeGuestsCSV(Path dir) {
		try {
			File file= new File(Paths.get(dir.toString(), "guests.csv").toString());
			FileWriter fw= new FileWriter(file);
			fw.write("Guest ID, Requested Room Type, Arrival Time \n");
			for (Guest guest : guests) {
				fw.write(guest.id() + "," + guest.type() + "," + guest.arrival() + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Write the rooms.csv file and place it in the given directory
	 *
	 * @param dir The directory where the rooms.csv file will be placed */
	private void writeRoomsCSV(Path dir) {
		try {
			File file= new File(Paths.get(dir.toString(), "rooms.csv").toString());
			FileWriter fw= new FileWriter(file);
			fw.write("Room Number,Type,Checkout Time,Cleaning Time\n");
			for (Room room : rooms) {
				fw.write(room.num() + "," + room.type() + "," + room.release() + "," + room.process() + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Write the weights.csv file and place it in the given directory
	 *
	 * @param dir The directory where the weights.csv file will be placed */
	private void writeWeightsCSV(Path dir) {
		try {
			File file= new File(Paths.get(dir.toString(), "weights.csv").toString());
			FileWriter fw= new FileWriter(file);
			for (Room room : rooms) {
				fw.write("," + room.num());
			}
			for (Guest guest : guests) {
				fw.write("\n" + guest.id() + "");
				for (Room room : rooms) {
					fw.write("," + weights.get(guest, room));
				}
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Instance.class) return false;
		Instance instance= (Instance) ob;
		if (teamSize != instance.teamSize) return false;
		Collections.sort(guests, Comparator.comparingInt(Guest::id));
		Collections.sort(rooms, Comparator.comparingInt(Room::num));
		Collections.sort(instance.guests, Comparator.comparingInt(Guest::id));
		Collections.sort(instance.rooms, Comparator.comparingInt(Room::num));
		if (!rooms.equals(instance.rooms)) return false;
		if (!guests.equals(instance.guests)) return false;
		for (Guest guest : guests) {
			for (Room room : rooms) {
				int g= guest.id();
				int r= room.num();
				if (!weight(g, r).equals(instance.weight(g, r))) { return false; }
			}
		}
		return true;
	}

	/** Return a string representing this instance */
	@Override
	public String toString() {

		Collections.sort(rooms, Comparator.comparingInt(Room::num));
		Collections.sort(guests, Comparator.comparingInt(Guest::id));

		StringBuilder sb= new StringBuilder();
		sb.append("ROOMS\n");
		sb.append("------------------------------------------\n");
		sb.append("ROOM\t" + "TYPE\t" + "CHECKOUT\t" + "CLEAN TIME\n");
		for (Room room : rooms) {
			sb.append(String.format("%-4d \t", room.num()));
			sb.append(String.format("%-4d \t", room.type()));
			sb.append(String.format("%-7d \t", room.release()));
			sb.append(String.format("%-9d \n", room.process()));
		}
		sb.append("------------------------------------------\n\n");
		sb.append("GUESTS\n");
		sb.append("-----------------------\n");
		sb.append("GUEST\t" + "TYPE\t" + "ARRIVAL\n");
		for (Guest guest : guests) {
			sb.append(String.format("%-5d \t", guest.id()));
			sb.append(String.format("%-4d \t", guest.type()));
			sb.append(String.format("%-6d \n", guest.arrival()));
		}
		sb.append("-----------------------\n\n");
		sb.append("GUESTS (ROW) x ROOMS (COL) WEIGHTS\n");
		sb.append("---");
		for (int i= 0; i < guests.size(); i++ ) {
			sb.append("-----");
		}
		sb.append("\n");
		sb.append("     ");
		for (Room room : rooms) {
			sb.append(String.format("%-5d", room.num()));
		}
		sb.append("\n");
		for (Guest guest : guests) {
			sb.append(String.format("%-5d", guest.id()));
			for (Room room : rooms) {
				Double wgt= weights.get(guest, room);
				sb.append(String.format("%-5.1f", wgt));
			}
			sb.append("\n");
		}
		for (int i= 0; i < guests.size(); i++ ) {
			sb.append("-----");
		}
		sb.append("---\n");

		sb.append("Housekeeping Team Size: " + teamSize + "\n");
		return sb.toString();
	}
}