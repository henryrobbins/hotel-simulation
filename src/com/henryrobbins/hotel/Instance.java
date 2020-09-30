package com.henryrobbins.hotel;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections4.map.MultiKeyMap;

/** Maintains an immutable set of incoming hotel guests on some day for the specified hotel */
public final class Instance {

	/** Hotel */
	private final Hotel hotel;
	/** List of hotel guests (with unique guest IDs) */
	private final ArrayList<Guest> guests;
	/** Map of unique guest IDs to guests */
	private final HashMap<Integer, Guest> guestMap;
	/** Map from room types to the number of requests */
	private final HashMap<Integer, Integer> requestFrequency;
	/** Map of weights for every guest-room pair */
	private final MultiKeyMap<Object, Double> weights;

	/** Return the hotel in this instance */
	public Hotel hotel() {
		return hotel;
	}

	/** Return a copy of the incoming guests list */
	public ArrayList<Guest> guests() {
		return new ArrayList<>(guests);
	}

	/** Return the guest with the given guest ID. Return null if no guest with this ID. */
	public Guest guest(int id) {
		return guestMap.get(id);
	}

	/** Return a copy of the list of rooms */
	public ArrayList<Room> rooms() {
		return new ArrayList<>(hotel.rooms());
	}

	/** Return the room with the given room number. Return null if no room with this number. */
	public Room room(int num) {
		return hotel.room(num);
	}

	/** Return the room type request frequency map */
	public HashMap<Integer, Integer> reqeustFreq() {
		return new HashMap<>(requestFrequency);
	}

	/** Return the weight of the given guest-room pair */
	public Double weight(Guest guest, Room room) {
		return weights.get(guest, room);
	}

	/** Return the weight of the given guest-room pair */
	public Double weight(int g, int r) {
		return weight(guest(g), room(r));
	}

	/** Return the map of weights */
	public MultiKeyMap<Object, Double> weights() {
		MultiKeyMap<Object, Double> weights= new MultiKeyMap<>();
		for (Guest guest : guests) {
			for (Room room : hotel.rooms()) {
				weights.put(guest, room, this.weights.get(guest, room));
			}
		}
		return weights;
	}

	/** Return the size of the housekeeping team */
	public int getH() {
		return hotel.getH();
	}

	/** Return the number of room types in this instance */
	public int typeSize() {
		return hotel.typeFreq().size();
	}

	/** Return the maximum type t such that a guest requesting type t^* <= t can be accommodated. <br>
	 * Hence, 0 is returned if no guest can be added. Return -1 if currently infeasible */
	public int maxFeasibleTypeRequest() {
		// If the highest requested type is larger than the highest available type, clearly infeasible
		int maxType= Collections.max(hotel.typeFreq().keySet());
		int maxRequest= Collections.max(requestFrequency.keySet());
		if (maxType < maxRequest) return -1;

		// Set the number of available rooms of type 1 or greater
		int available= hotel.rooms().size();
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
			Integer avail= hotel.typeFreq().get(t);
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

	/** Builder class used to create the immutable Instance */
	public static class Builder {

		/** Hotel */
		private Hotel hotel;
		/** List of hotel guests (with unique guest IDs) */
		private ArrayList<Guest> guests= new ArrayList<>();
		/** The set of used guest ids */
		private HashSet<Integer> usedIDs= new HashSet<>();
		/** Map of unique guest IDs to guests */
		private HashMap<Integer, Guest> guestMap= new HashMap<>();
		/** Map from room types to the number of requests */
		private HashMap<Integer, Integer> requestFrequency= new HashMap<>();
		/** Map of weights for every guest-room pair */
		private MultiKeyMap<Object, Double> weights= new MultiKeyMap<>();

		/** Construct a Builder for an Instance on a given hotel */
		public Builder(Hotel hotel) {
			if (hotel == null) throw new IllegalArgumentException("Hotel was null");
			this.hotel= hotel;
		}

		/** Construct a Builder that is a copy of the given instance */
		public Builder(Instance instance) {
			if (instance == null) throw new IllegalArgumentException("Instance was null");
			hotel= instance.hotel;
			guests= new ArrayList<>(instance.guests);
			for (Guest guest : guests) {
				usedIDs.add(guest.id());
			}
			guestMap= new HashMap<>(instance.guestMap);
			requestFrequency= new HashMap<>(instance.requestFrequency);
			weights.putAll(instance.weights);
		}

		/** Return the hotel this is a builder for */
		public Hotel hotel() {
			return hotel;
		}

		/** Return the list of guests */
		public ArrayList<Guest> guests() {
			return new ArrayList<>(guests);
		}

		/** Return the guest with the given guest ID. Return null if no guest with this ID. */
		public Guest guest(int id) {
			return guestMap.get(id);
		}

		/** Return the room with the given room number. Return null if no room with this number. */
		public Room room(int num) {
			return hotel.room(num);
		}

		/** Return the map of weights */
		public MultiKeyMap<Object, Double> weights() {
			return weights;
		}

		/** Add the given guest to this set of arrivals
		 *
		 * @param guest A guest to be added (with unique guest ID) */
		public void addGuest(Guest guest) {
			if (guest == null) throw new IllegalArgumentException("Guest was null");
			int id= guest.id();
			int type= guest.type();
			if (usedIDs.contains(id)) throw new IllegalArgumentException("Non-unique guest ID");
			guests.add(guest);
			guestMap.put(id, guest);
			Integer prev= requestFrequency.get(type);
			if (prev == null) {
				requestFrequency.put(type, 1);
			} else {
				requestFrequency.put(type, prev + 1);
			}
			usedIDs.add(id);
		}

		/** Add the given weight between the given guest and room
		 *
		 * @param guest The guest the weight is associated with (in the instance)
		 * @param room  The room the weight is associated with (in the instance)
		 * @param wgt   The weight to be added (represents satisfaction) in 0..1 */
		public Builder addWeight(Guest guest, Room room, Double wgt) {
			if (!guests.contains(guest)) throw new IllegalArgumentException("Guest not in instance");
			if (!hotel.rooms().contains(room)) throw new IllegalArgumentException("Room not in instance");
			if (wgt < 0 || wgt > 1) throw new IllegalArgumentException("Weight not in [0,1]");
			weights.put(guest, room, wgt);
			return this;
		}

		/** Construct an Instance from this Builder */
		public Instance build() {
			return new Instance(hotel, guests, guestMap, requestFrequency, weights);
		}
	}

	/** Construct an instance with the respective list of rooms, guests, weights, and team size. <br>
	 * Any guest-room pair not given a weight will be given a default weight of zero.
	 *
	 * @param hotel            The hotel
	 * @param guests           The set of arrivals (at least one guest)
	 * @param guestMap         Map of unique guest ids to Guests
	 * @param requestFrequency Map of room types to request frequency
	 * @param weights          The weights between guest-room pairs (in 0..1) */
	private Instance(Hotel hotel, ArrayList<Guest> guests, HashMap<Integer, Guest> guestMap,
		HashMap<Integer, Integer> requestFrequency, MultiKeyMap<Object, Double> weights) {
		this.hotel= hotel;
		if (guests.size() < 1) throw new IllegalArgumentException("No guests in instance");
		this.guests= new ArrayList<>(guests);
		this.guestMap= new HashMap<>(guestMap);
		this.requestFrequency= new HashMap<>(requestFrequency);
		this.weights= new MultiKeyMap<>();
		for (Guest guest : this.guests) {
			for (Room room : hotel.rooms()) {
				Double wgt= weights.get(guest, room);
				this.weights.put(guest, room, wgt == null ? 0.0 : wgt);
			}
		}
	}

	/** Create a directory called name in the specified directory and write arrivals.csv and weights.csv
	 * to it representing this instance's arrivals and weights respectively
	 *
	 * @throws Exception */
	public void writeCSV(Path dir, String name) throws Exception {
		File instDir= Paths.get(dir.toString(), name).toFile();
		instDir.mkdirs();
		writeArrivalsCSV(instDir.toPath(), "arrivals");
		writeWeightsCSV(instDir.toPath(), "weights");
		hotel.writeCSV(instDir.toPath(), "hotel");

	}

	/** Write a CSV file representing the weights called name to the specified directory
	 *
	 * @param dir  directory where the CSV file will be written
	 * @param name name of the csv file
	 * @throws Exception */
	private void writeWeightsCSV(Path dir, String name) throws Exception {
		ArrayList<Room> rooms= hotel.rooms();
		File file= new File(Paths.get(dir.toString(), name + ".csv").toString());
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
	}

	/** Write a CSV file representing the arrivals called name to the specified directory
	 *
	 * @param dir  directory where the CSV file will be written
	 * @param name name of the csv file
	 * @throws Exception */
	private void writeArrivalsCSV(Path dir, String name) throws Exception {
		File file= new File(Paths.get(dir.toString(), name + ".csv").toString());
		FileWriter fw= new FileWriter(file);
		fw.write("Guest ID, Requested Room Type, Arrival Time \n");
		for (Guest guest : guests) {
			fw.write(guest.id() + "," + guest.type() + "," + guest.arrival() + "\n");
		}
		fw.close();
	}

	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Instance.class) return false;
		Instance instance= (Instance) ob;
		if (!hotel.equals(instance.hotel)) return false;
		Collections.sort(guests, Comparator.comparingInt(Guest::id));
		Collections.sort(instance.guests, Comparator.comparingInt(Guest::id));
		if (!guests.equals(instance.guests)) return false;
		for (Guest guest : guests) {
			for (Room room : hotel.rooms()) {
				int g= guest.id();
				int r= room.num();
				if (!weight(g, r).equals(instance.weight(g, r))) { return false; }
			}
		}
		return true;
	}

	@Override
	public String toString() {

		ArrayList<Room> rooms= hotel.rooms();

		Collections.sort(rooms, Comparator.comparingInt(Room::num));
		Collections.sort(guests, Comparator.comparingInt(Guest::id));

		StringBuilder sb= new StringBuilder();
		sb.append(hotel);
		sb.append("ARRIVALS\n");
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
			sb.append("----------");
		}
		sb.append("\n");
		sb.append("     ");
		for (Room room : rooms) {
			sb.append(String.format("%-10d", room.num()));
		}
		sb.append("\n");
		for (Guest guest : guests) {
			sb.append(String.format("%-5d", guest.id()));
			for (Room room : rooms) {
				Double wgt= weights.get(guest, room);
				sb.append(String.format("%-10.5f", wgt));
			}
			sb.append("\n");
		}
		for (int i= 0; i < guests.size(); i++ ) {
			sb.append("----------");
		}
		sb.append("---\n");
		return sb.toString();
	}
}