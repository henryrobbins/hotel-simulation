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

/** Maintains information about an immutable hotel */
public final class Hotel {

	/** The set of hotel rooms (unique room numbers) */
	private final ArrayList<Room> rooms;
	/** The map of unique room numbers to rooms */
	private final HashMap<Integer, Room> roomMap;
	/** The number of rooms of a given type */
	private final HashMap<Integer, Integer> typeFrequency;
	/** The number of housekeepers */
	private final int h;

	/** Return a unique id for this hotel */
	public int id() {
		return System.identityHashCode(this);
	}

	/** Return the list of rooms */
	public ArrayList<Room> rooms() {
		return new ArrayList<>(rooms);
	}

	/** Return the room with the given room number. Return null if no room with this number. */
	public Room room(int num) {
		return roomMap.get(num);
	}

	/** Return the size of the housekeeping team */
	public int getH() {
		return h;
	}

	/** Return the number of room types in this instance */
	public int typeSize() {
		return typeFrequency.size();
	}

	/** Return the frequencies of room types */
	public HashMap<Integer, Integer> typeFreq() {
		return typeFrequency;
	}

	/** Builder class used to create the immutable Instance */
	public static class Builder {

		/** The set of hotel rooms (unique room numbers) */
		private ArrayList<Room> rooms= new ArrayList<>();
		/** The set of used room numbers */
		private HashSet<Integer> usedNums= new HashSet<>();
		/** The map of unique room numbers to rooms */
		private HashMap<Integer, Room> roomMap= new HashMap<>();
		/** The number of rooms of a given type */
		private HashMap<Integer, Integer> typeFrequency= new HashMap<>();
		/** The number of housekeepers */
		private int h;

		/** Set the number of housekeepers */
		public Builder setH(int h) {
			this.h= h;
			return this;
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

		/** Construct an instance from this Builder instance */
		public Hotel build() {
			return new Hotel(rooms, roomMap, typeFrequency, h);
		}
	}

	/** Construct an immutable hotel
	 *
	 * @param id            The unique hotel id
	 * @param rooms         The set of hotel rooms with unique room numbers (nonzero)
	 * @param roomMap       The map from the set of room numbers to rooms
	 * @param typeFrequency The frequency of every room type
	 * @param h             The number of housekeepers (nonzero) */
	private Hotel(ArrayList<Room> rooms, HashMap<Integer, Room> roomMap,
		HashMap<Integer, Integer> typeFrequency, int h) {
		if (rooms.size() < 1) throw new IllegalArgumentException("No rooms in hotel");
		if (h < 1) throw new IllegalArgumentException("No housekeepers in hotel");
		this.rooms= new ArrayList<>(rooms);
		this.roomMap= new HashMap<>(roomMap);
		this.typeFrequency= new HashMap<>(typeFrequency);
		this.h= h;
	}

	/** Write the hotel.csv file. Place it in a folder named id locates in the given directory
	 *
	 * @param dir The directory where the hotel.csv file will be placed */
	public void writeCSV(Path dir) {
		try {
			File file= new File(Paths.get(dir.toString(), String.valueOf(id())).toString());
			file.mkdir();
			file= Paths.get(file.toString(), "hotel.csv").toFile();
			FileWriter fw= new FileWriter(file);
			fw.write("Room Number,Type,Quality,Checkout Time,Cleaning Time\n");
			for (Room room : rooms) {
				fw.write(room.num() + "," + room.type() + "," + room.quality() + "," + room.release() + "," +
					room.process() + "\n");
			}
			fw.write("" + h);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Write the hotel.csv file and place it in the Simulations directory */
	public void writeCSV() {
		writeCSV(Paths.get("Simulations"));
	}

	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Hotel.class) return false;
		Hotel hotel= (Hotel) ob;
//		if (id != hotel.id) return false;
		if (h != hotel.getH()) return false;
		Collections.sort(rooms, Comparator.comparingInt(Room::num));
		Collections.sort(hotel.rooms, Comparator.comparingInt(Room::num));
		if (!rooms.equals(hotel.rooms)) return false;
		return true;
	}

	@Override
	public String toString() {

		Collections.sort(rooms, Comparator.comparingInt(Room::num));

		StringBuilder sb= new StringBuilder();
		sb.append("HOTEL\n");
		sb.append("----------------------------------------------------------\n");
		sb.append("ROOM\t" + "TYPE\t" + "QUALITY \t" + "CHECKOUT\t" + "CLEAN TIME\n");
		for (Room room : rooms) {
			sb.append(String.format("%-4d \t", room.num()));
			sb.append(String.format("%-4d \t", room.type()));
			sb.append(String.format("%-8.3f\t", room.quality()));
			sb.append(String.format("%-7d \t", room.release()));
			sb.append(String.format("%-9d \n", room.process()));
		}
		sb.append("----------------------------------------------------------\n");
		sb.append("Housekeeping Team Size: " + h + "\n\n");
		return sb.toString();
	}
}
