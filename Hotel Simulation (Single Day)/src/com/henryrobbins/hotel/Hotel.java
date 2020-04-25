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

/** Maintains information about an immutable hotel. */
public final class Hotel {

	/** List of all hotel rooms (with unique room numbers, at least one room) */
	private final ArrayList<Room> rooms;
	/** Map of unique room numbers to rooms */
	private final HashMap<Integer, Room> roomMap;
	/** Map of room types to the frequency of that type */
	private final HashMap<Integer, Integer> typeFrequency;
	/** Number of housekeepers (at least 1) */
	private final int h;

	/** Return this hotel's list of rooms */
	public ArrayList<Room> rooms() {
		return new ArrayList<>(rooms);
	}

	/** Return the room in this hotel with the given room number. <br>
	 * Returns null if no room with this number. */
	public Room room(int num) {
		return roomMap.get(num);
	}

	/** Return the size of the housekeeping team */
	public int getH() {
		return h;
	}

	/** Return the number of room types in this hotel */
	public int typeSize() {
		return typeFrequency.size();
	}

	/** Return the room type frequency map of this hotel */
	public HashMap<Integer, Integer> typeFreq() {
		return typeFrequency;
	}

	/** Builder class used to create the immutable Hotel */
	public static class Builder {
		/** List of all hotel rooms (with unique room numbers) */
		private ArrayList<Room> rooms= new ArrayList<>();
		/** The set of used room numbers */
		private HashSet<Integer> usedNums= new HashSet<>();
		/** Map of unique room numbers to rooms */
		private HashMap<Integer, Room> roomMap= new HashMap<>();
		/** Map of room types to the frequency of that type */
		private HashMap<Integer, Integer> typeFrequency= new HashMap<>();
		/** Number of housekeepers */
		private int h;

		/** Set the number of housekeepers (at least 1) */
		public Builder setH(int h) {
			if (h < 1) throw new IllegalArgumentException("Must be at least 1 housekeeper");
			this.h= h;
			return this;
		}

		/** Add the given hotel room to the hotel
		 *
		 * @param room A room to be added to the hotel (unique room number) */
		public Builder addRoom(Room room) {
			if (room == null) throw new IllegalArgumentException("Room was null");
			int num= room.num();
			int type= room.type();
			if (usedNums.contains(num)) throw new IllegalArgumentException("Non-unique room number");
			rooms.add(room);
			roomMap.put(num, room);
			Integer prev= typeFrequency.get(type);
			if (prev == null) {
				typeFrequency.put(type, 1);
			} else {
				typeFrequency.put(type, prev + 1);
			}
			usedNums.add(num);
			return this;
		}

		/** Construct a Hotel from this Builder */
		public Hotel build() {
			return new Hotel(rooms, roomMap, typeFrequency, h);
		}
	}

	/** Construct an immutable hotel
	 *
	 * @param rooms         The set of hotel rooms with unique room numbers (nonzero)
	 * @param roomMap       The map from the set of room numbers to rooms
	 * @param typeFrequency The frequency of every room type
	 * @param h             The number of housekeepers (at least 1) */
	private Hotel(ArrayList<Room> rooms, HashMap<Integer, Room> roomMap,
		HashMap<Integer, Integer> typeFrequency, int h) {
		if (rooms.size() < 1) throw new IllegalArgumentException("No rooms in hotel");
		if (h < 1) throw new IllegalArgumentException("No housekeepers in hotel");
		this.rooms= new ArrayList<>(rooms);
		this.roomMap= new HashMap<>(roomMap);
		this.typeFrequency= new HashMap<>(typeFrequency);
		this.h= h;
	}

	/** Write a CSV file representing this hotel called name to the specified directory
	 *
	 * @param dir  directory where the CSV file will be written
	 * @param name name of the csv file
	 * @throws Exception */
	public void writeCSV(Path dir, String name) throws Exception {
		File file= Paths.get(dir.toString(), name + ".csv").toFile();
		FileWriter fw= new FileWriter(file);
		fw.write("Room Number,Type,Quality,Checkout Time,Cleaning Time\n");
		for (Room room : rooms) {
			fw.write(room.num() + "," + room.type() + "," + room.quality() + "," + room.release() + "," +
				room.process() + "\n");
		}
		fw.write("" + h);
		fw.close();
	}

	@Override
	public boolean equals(Object ob) {
		if (ob == null) return false;
		if (ob.getClass() != Hotel.class) return false;
		Hotel hotel= (Hotel) ob;
		Collections.sort(rooms, Comparator.comparingInt(Room::num));
		Collections.sort(hotel.rooms, Comparator.comparingInt(Room::num));
		if (!rooms.equals(hotel.rooms)) return false;
		if (h != hotel.getH()) return false;
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