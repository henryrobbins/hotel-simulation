package AMPL;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import com.ampl.AMPL;
import com.ampl.DataFrame;
import com.ampl.Environment;
import com.ampl.Objective;

import Hotel.Guest;
import Hotel.Housekeeper;
import Hotel.HousekeepingSchedule;
import Hotel.Instance;
import Hotel.Room;
import Hotel.RoomAssignment;
import Hotel.Solution;

/** This helper class contains static methods used by various solvers */
public class AMPLHelper {

	/** Run the housekeepingSchedule model with the given objective function for the given instance
	 *
	 * @param obj      The name of the objective function (defined in the model)
	 * @param instance The instance to generate a housekeeping schedule for (not null)
	 * @return A housekeeping schedule for the given instance */
	public static HousekeepingSchedule runScheduleIP(Instance instance, String obj) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= createAMPL();
		uploadModel(ampl, "housekeepingSchedule");
		setObjectiveFunction(ampl, obj);
		uploadRoomAndGuestData(ampl, instance);
		uploadHousekeepingData(ampl, instance);
		ampl.solve();

		return generateSchedule(ampl, instance);

	}

	/** Run the roomAssignment model with the given objective function for the given instance
	 *
	 * @param obj      The name of the objective function (defined in the model)
	 * @param instance The instance to generate a room assignment for (not null)
	 * @return A room assignment for the given instance */
	public static RoomAssignment runAssignmentIP(Instance instance, String obj) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= createAMPL();
		uploadModel(ampl, "roomAssignment");
		setObjectiveFunction(ampl, obj);
		uploadRoomAndGuestData(ampl, instance);
		ampl.solve();

		return generateAssignment(ampl, instance);

	}

	/** Run the scheduleAndAssignment model with the given objective function for the given instance
	 *
	 * @param obj      The name of the objective function (defined in the model)
	 * @param instance The instance to generate an assignment and schedule for (not null)
	 * @return A solution with room assignment and housekeeping schedule for the given instance */
	public static Solution runSolutionIP(Instance instance, String obj) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= createAMPL();
		uploadModel(ampl, "scheduleAndAssignment");
		setObjectiveFunction(ampl, obj);
		uploadRoomAndGuestData(ampl, instance);
		uploadHousekeepingData(ampl, instance);
		ampl.solve();

		return generateSolution(ampl, instance);
	}

	/** Set the current objective function of given instance of ampl
	 *
	 * @param obj The name of objective function (defined in the current model) */
	public static void setObjectiveFunction(AMPL ampl, String obj) {

		HashSet<String> objectives= new HashSet<>();
		for (Objective objective : ampl.getObjectives()) {
			objectives.add(objective.name());
		}
		if (!objectives.contains(obj)) throw new IllegalArgumentException("Objective function undefined");
		ampl.eval("objective " + obj + ";");

	}

	/** Returns the housekeeping schedule associated with the current state of 'assign' in the given
	 * instance of AMPL. "Assign" indicates when a given housekeeper starts cleaning a given room.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance that returned housekeeping schedule is for (not null)
	 * @return The housekeeping schedule for the instance */
	public static HousekeepingSchedule generateSchedule(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		HousekeepingSchedule schedule= new HousekeepingSchedule(instance);
		ArrayList<Room> rooms= instance.getRooms();

		for (Room room : rooms) {
			for (Housekeeper housekeeper : schedule.getHousekeepers()) {
				for (Object t : ampl.getSet("TIME").toArray()) {
					if (ampl.getVariable("schedule")
						.get(Integer.toString(room.getNumber()), Integer.toString(housekeeper.getID()), t)
						.value() == 1.0) {
						schedule.add(housekeeper, room, ((Double) t).intValue());
					}
				}
			}
		}

		return schedule;

	}

	/** Returns the room assignment associated with the current state of 'assign' in the given instance
	 * of AMPL. "Assign" indicates which room is assigned to each guest.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance that returned housekeeping schedule is for (not null)
	 * @return The room assignment for the instance */
	public static RoomAssignment generateAssignment(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		RoomAssignment assignment= new RoomAssignment(instance);

		for (Room room : instance.getRooms()) {
			for (Guest guest : instance.getGuests()) {
				if (ampl.getVariable("assign").get(Integer.toString(room.getNumber()),
					Integer.toString(guest.getID())).value() == 1.0) {
					assignment.assign(room, guest);
					break;
				}
			}
		}

		return assignment;
	}

	/** Returns the solution associated with the current state of 'assign' and 'roomAssign' in <br>
	 * the given instance of AMPL. "Assign" indicates when a housekeeper cleans a given room and <br>
	 * "roomAssign" indicates which guest is assigned to which room.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance that returned housekeeping schedule is for (not null)
	 * @return The solution for the instance */
	public static Solution generateSolution(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		HousekeepingSchedule schedule= generateSchedule(ampl, instance);
		RoomAssignment assignment= generateAssignment(ampl, instance);

		return new Solution(instance, schedule, assignment);
	}

	/** Upload IP named model to the given instance of AMPL
	 *
	 * @param ampl  An instance of AMPL (not null)
	 * @param model The name of the .mod file in AMPL (not null and at least one character) */
	public static void uploadModel(AMPL ampl, String model) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (model == null || model.length() < 1) throw new IllegalArgumentException("Name less than one character");
		try {
			ampl.read(Paths.get("AMPL", "Models", model + ".mod").toString());
		} catch (Exception e) {
			ampl.close();
		}
	}

	/** Create an instance of AMPL with proper options */
	public static AMPL createAMPL() {
		AMPL ampl= new AMPL(new Environment(Paths.get("AMPL").toString()));
		ampl.setOption("solver", "gurobi");
		ampl.setOption("presolve_eps", "1e-10");
		ampl.setOption("constraint_drop_tol", "1e-10");
		return ampl;
	}

	/** Uploads data for every room and guest in the given instance. This includes the <br>
	 * room number, room type, check-out time, and cleaning time for every room and <br>
	 * guest ID, requested room type, and check-in time for every guest. Furthermore, <br>
	 * the satisfaction and preferences for every room-guest pair is uploaded.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance whose room and guest data will be uploaded (not null) */
	public static void uploadRoomAndGuestData(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		ArrayList<Room> rooms= instance.getRooms();
		DataFrame roomDF= new DataFrame(1, "ROOMS");

		int rSize= rooms.size();
		String[] r= new String[rSize];
		int[] rType= new int[rSize];
		int[] checkout= new int[rSize];
		int[] cleanTime= new int[rSize];

		int i= 0;
		for (Room room : rooms) {
			r[i]= Integer.toString(room.getNumber());
			rType[i]= room.getType();
			checkout[i]= room.getCheckOut();
			cleanTime[i]= room.getCleanTime();
			i++ ;
		}

		roomDF.setColumn("ROOMS", r);
		roomDF.addColumn("roomType", rType);
		roomDF.addColumn("checkout", checkout);
		roomDF.addColumn("cleanTime", cleanTime);
		ampl.setData(roomDF, "ROOMS");

		ArrayList<Guest> guests= instance.getGuests();
		DataFrame guestDF= new DataFrame(1, "GUESTS");

		int gSize= guests.size();
		String[] g= new String[gSize];
		int[] gType= new int[gSize];
		int[] checkin= new int[gSize];

		i= 0;
		for (Guest guest : guests) {
			g[i]= Integer.toString(guest.getID());
			gType[i]= guest.getType();
			checkin[i]= guest.getArrivalTime();
			i++ ;
		}

		guestDF.setColumn("GUESTS", g);
		guestDF.addColumn("requestType", gType);
		guestDF.addColumn("checkin", checkin);
		ampl.setData(guestDF, "GUESTS");

		int[][] preferences= new int[rSize][gSize];
		int n= 0;
		int m= 0;
		for (Room room : rooms) {
			m= 0;
			for (Guest guest : guests) {
				preferences[n][m]= guest.getMetPreferences(room);
				m++ ;
			}
			n++ ;
		}

		DataFrame prefDF= new DataFrame(2, "ROOMS", "GUESTS", "preferences");
		prefDF.setMatrix(preferences, r, g);
		ampl.setData(prefDF);

		double[][] satisfaction= new double[rSize][gSize];
		n= 0;
		m= 0;
		for (Room room : rooms) {
			m= 0;
			for (Guest guest : guests) {
				satisfaction[n][m]= guest.getSatisfaction(room);
				m++ ;
			}
			n++ ;
		}

		DataFrame satisfactionDF= new DataFrame(2, "ROOMS", "GUESTS", "satisfaction");
		satisfactionDF.setMatrix(satisfaction, r, g);
		ampl.setData(satisfactionDF);

	}

	/** Uploads data for the number of housekeepers and the maximum makespan. <br>
	 * The maximum makespan is calculated by summing the cleaning times of every <br>
	 * room and adding it to the latest checkout time.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance whose data will be uploaded (not null) */
	public static void uploadHousekeepingData(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		DataFrame housekeeperDF= new DataFrame(1, "HOUSEKEEPERS");

		int n= instance.getNumOfHousekeepers();
		String[] id= new String[n];
		for (int j= 1; j <= n; j++ ) {
			id[j - 1]= String.valueOf(j);
		}

		housekeeperDF.setColumn("HOUSEKEEPERS", id);
		ampl.setData(housekeeperDF, "HOUSEKEEPERS");

		DataFrame timeDF= new DataFrame(1, "TIME");

		int maxMakespan= 0;
		int maxCheckOut= 0;
		for (Room room : instance.getRooms()) {
			maxMakespan+= room.getCleanTime();
			maxCheckOut= Math.max(maxCheckOut, room.getCheckOut());
		}
		maxMakespan+= maxCheckOut;
		int[] time= new int[maxMakespan + 1];
		for (int j= 0; j <= maxMakespan; j++ ) {
			time[j]= j;
		}

		timeDF.setColumn("TIME", time);
		ampl.setData(timeDF, "TIME");

	}
}
