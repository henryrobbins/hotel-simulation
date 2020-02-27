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

/** Maintains a hotel and a single day of hotel guest arrivals. Furthermore, stores the <br>
 * satisfaction of every guest-room pair. An instance is analogous to a bipartite graph in <br>
 * which the left nodes are guests, the right nodes are rooms, and the edges between them <br>
 * have weights corresponding to the satisfaction of that assignment. Additionally, an <br>
 * instance stores the size of the housekeeping team. */
public final class Instance {

	/** The hotel */
	private final Hotel hotel;
	/** The list of hotel guests (must have unique guest IDs) */
	private final ArrayList<Guest> guests;
	/** The map of unique guest IDs to guests */
	private final HashMap<Integer, Guest> guestMap;
	/** The number of requests for a given room type */
	private final HashMap<Integer, Integer> requestFrequency;
	/** The weight of every guest-room pair */
	private final MultiKeyMap<Object, Double> weights;

	/** Return the unique id of this instance */
	public int id() {
		return System.identityHashCode(this);
	}

	/** Return the hotel in this instance */
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

	/** Return the room type request frequencies */
	public HashMap<Integer, Integer> reqeustFreq() {
		return requestFrequency;
	}

	/** Return the list of rooms */
	public ArrayList<Room> rooms() {
		return new ArrayList<>(hotel.rooms());
	}

	/** Return the room with the given room number. Return null if no room with this number. */
	public Room room(int num) {
		return hotel.room(num);
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

		/** The hotel */
		private Hotel hotel;
		/** The list of hotel guests (must have unique guest IDs) */
		private ArrayList<Guest> guests= new ArrayList<>();
		/** The set of used guest ids */
		private HashSet<Integer> usedIDs= new HashSet<>();
		/** The map of unique guest IDs to guests */
		private HashMap<Integer, Guest> guestMap= new HashMap<>();
		/** The number of requests for a given room type */
		private HashMap<Integer, Integer> requestFrequency= new HashMap<>();
		/** The weight of every guest-room pair */
		private MultiKeyMap<Object, Double> weights= new MultiKeyMap<>();

		/** Construct a Builder for an instance on a given hotel
		 *
		 * @param hotel The hotel */
		public Builder(Hotel hotel) {
			if (hotel == null) throw new IllegalArgumentException("Hotel was null");
			this.hotel= hotel;
		}

		/** Construct a Builder that is a copy of the given instance
		 *
		 * @param instance The instance that this Builder is a copy of */
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
			if (!requestFrequency.containsKey(type)) {
				requestFrequency.put(type, 1);
			} else {
				int prev= requestFrequency.get(type);
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

		/** Construct an instance from this Builder instance */
		public Instance build() {
			return new Instance(hotel, guests, guestMap, requestFrequency, weights);
		}
	}

	/** Construct an instance with the respective list of rooms, guests, weights, and team size. <br>
	 * Any guest-room pair not given a weight will be given a defualt weight of zero.
	 *
	 * @param id       The unique id for this instance
	 * @param hotel    The hotel
	 * @param arrivals The set of arrivals (at least one guest)
	 * @param weight   The weights between guest-room pairs (in 0..1) */
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

	/** In the given directory, check to see if a directory under the name of the <br>
	 * unique hotel id exists. If it does, it is assumed to contain a hotel.csv <br>
	 * file. If not, create this directory and write the hotel.csv file. Then, <br>
	 * create a directory under the name of the unique instance id and write <br>
	 * the arrivals.csv and weights.csv files in it.
	 *
	 * @param dir The directory where the instance directory will be placed */
	public void writeCSV(Path dir) {

		File hotelDir= Paths.get(dir.toString(), String.valueOf(hotel.id())).toFile();
		if (!hotelDir.exists()) {
			hotel.writeCSV(dir);
		}
		File instanceDir= Paths.get(hotelDir.toString(), String.valueOf(id())).toFile();
		instanceDir.mkdirs();
		writeArrivalsCSV(instanceDir.toPath());
		writeWeightsCSV(instanceDir.toPath());
	}

	/** Create the corresponding CSV files in the proper structure in the Simulations directory */
	public void writeCSV() {
		writeCSV(Paths.get("Simulations"));
	}

	/** Write the weights.csv file and place it in the given directory
	 *
	 * @param dir The directory where the weights.csv file will be placed */
	private void writeWeightsCSV(Path dir) {
		ArrayList<Room> rooms= hotel.rooms();
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

	/** Write the arrivals.csv file and place it in the given directory
	 *
	 * @param dir The directory where the guests.csv file will be placed */
	private void writeArrivalsCSV(Path dir) {
		try {
			File file= new File(Paths.get(dir.toString(), "arrivals.csv").toString());
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

	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Instance.class) return false;
		Instance instance= (Instance) ob;
//		if (id != instance.id) return false;
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

	/** Return a string representing this instance */
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
		return sb.toString();
	}
}