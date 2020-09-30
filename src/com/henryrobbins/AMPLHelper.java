package com.henryrobbins;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.collections4.BidiMap;

import com.ampl.AMPL;
import com.ampl.DataFrame;
import com.ampl.Environment;
import com.ampl.Objective;
import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Housekeeper;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;

/** This helper class contains static methods used by various solvers */
public abstract class AMPLHelper {

	/** Path to the AMPL folder */
	private static Path path= new File("/Users/Henry/AMPL").toPath();

	/** Set the AMPL path */
	public static void setPath(Path path) {
		AMPLHelper.path= path;
	}

	/** Run the assignment model with the given objective function on the given instance
	 *
	 * @param obj      The name of the objective function (defined in the model)
	 * @param instance The instance to generate a room assignment for (not null)
	 * @return A room assignment for the given instance */
	public static Assignment runAssignmentIP(Instance instance, String obj) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= createAMPL();
		uploadModel(ampl, "assignment");
		setObjectiveFunction(ampl, obj);
		setRoomAndGuestParams(ampl, instance);
		ampl.solve();

		Assignment assignment= getAssignment(ampl, instance);
		close(ampl);
		return assignment;
	}

	/** Run the assignment model with the given objective function on the given instance
	 *
	 * @param obj      The name of the objective function (defined in the model)
	 * @param instance The instance to generate a room assignment for (not null)
	 * @param alpha    setting for the tunable parameter alpha
	 * @param beta     setting for the tunable parameter beta
	 * @param gamma    setting for the tunable parameter gamma
	 * @param tau      setting for the tunable parameter tau
	 * @return A room assignment for the given instance */
	public static Assignment runAssignmentIP(Instance instance, String obj, double tau, double alpha, double beta,
		double gamma) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= createAMPL();
		uploadModel(ampl, "assignment");
		setObjectiveFunction(ampl, obj);
		setRoomAndGuestParams(ampl, instance);
		ampl.getParameter("tau").set(tau);
		ampl.getParameter("alpha").set(alpha);
		ampl.getParameter("beta").set(beta);
		ampl.getParameter("gamma").set(gamma);
		ampl.solve();

		Assignment assignment= getAssignment(ampl, instance);
		close(ampl);
		return assignment;
	}

	/** Run the schedule model with the given objective function for the given instance
	 *
	 * @param obj      The name of the objective function (defined in the model)
	 * @param instance The instance to generate a housekeeping schedule for (not null)
	 * @return A housekeeping schedule for the given instance */
	public static Schedule runScheduleIP(Instance instance, String obj) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= createAMPL();
		uploadModel(ampl, "schedule");
		setObjectiveFunction(ampl, obj);
		setRoomAndGuestParams(ampl, instance);
		setHousekeepingParams(ampl, instance);
		ampl.solve();

		Schedule schedule= getSchedule(ampl, instance);
		close(ampl);
		return schedule;
	}

	/** Run the solution model with the given objective function for the given instance
	 *
	 * @param obj      The name of the objective function (defined in the model)
	 * @param instance The instance to generate a solution for (not null)
	 * @return A solution with room assignment and housekeeping schedule for the given instance */
	public static Solution runSolutionIP(Instance instance, String obj) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		AMPL ampl= createAMPL();
		uploadModel(ampl, "solution");
		setObjectiveFunction(ampl, obj);
		setRoomAndGuestParams(ampl, instance);
		setHousekeepingParams(ampl, instance);
		ampl.solve();

		Solution solution= getSolution(ampl, instance);
		close(ampl);
		return solution;

	}

	/** Create an instance of AMPL with proper options */
	public static AMPL createAMPL() {
		AMPL ampl= new AMPL(new Environment(path.toString()));
		ampl.setOption("solver", "gurobi");
		ampl.setOption("presolve_eps", "1e-10");
		ampl.setOption("constraint_drop_tol", "1e-10");
		return ampl;
	}

	/** Close the given AMPL instance and call the garbage collector */
	public static void close(AMPL ampl) {
		ampl.close();
		System.gc();
	}

	/** Upload the given model file to the given instance of AMPL
	 *
	 * @param ampl  An instance of AMPL (not null)
	 * @param model The name of the .mod file in AMPL (not null and at least one character) */
	public static void uploadModel(AMPL ampl, String model) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (model == null || model.length() < 1) throw new IllegalArgumentException("Name less than one character");
		String name= model.concat(".mod");
		try {
			ampl.read(ResourcesPath.path().resolve("models").resolve(name).toString());
		} catch (Exception e) {
			throw new IllegalArgumentException("There is no .mod file named " + name + " in the Models folder");
		}
	}

	/** Set the current objective function for the given instance of AMPL
	 *
	 * @param obj The name of the objective function (defined in the current model) */
	public static void setObjectiveFunction(AMPL ampl, String obj) {

		HashSet<String> objectives= new HashSet<>();
		for (Objective objective : ampl.getObjectives()) {
			objectives.add(objective.name());
		}
		if (!objectives.contains(obj)) throw new IllegalArgumentException("Objective function undefined");
		ampl.eval("objective " + obj + ";");

	}

	/** Set parameters for the set of rooms and guests in the given instance. <br>
	 * These include the room number, room type, check-out time, and cleaning time for <br>
	 * every room and the guest ID, requested room type, and check-in time for every guest. <br>
	 * Furthermore, the satisfaction for every room-guest pair is set.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance whose room and guest data will be uploaded (not null) */
	/** @param ampl
	 * @param instance */
	public static void setRoomAndGuestParams(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		ArrayList<Room> rooms= instance.rooms();
		DataFrame roomDF= new DataFrame(1, "ROOMS");

		int rSize= rooms.size();
		String[] num= new String[rSize];
		int[] type= new int[rSize];
		int[] release= new int[rSize];
		int[] process= new int[rSize];

		for (int i= 0; i < rSize; i++ ) {
			Room room= rooms.get(i);
			num[i]= Integer.toString(room.num());
			type[i]= room.type();
			release[i]= room.release();
			process[i]= room.process();
		}

		roomDF.setColumn("ROOMS", num);
		roomDF.addColumn("type", type);
		roomDF.addColumn("release", release);
		roomDF.addColumn("process", process);
		ampl.setData(roomDF, "ROOMS");

		ArrayList<Guest> guests= instance.guests();
		DataFrame guestDF= new DataFrame(1, "GUESTS");

		int gSize= guests.size();
		String[] id= new String[gSize];
		int[] request= new int[gSize];
		int[] arrival= new int[gSize];

		for (int i= 0; i < gSize; i++ ) {
			Guest guest= guests.get(i);
			id[i]= Integer.toString(guest.id());
			request[i]= guest.type();
			arrival[i]= guest.arrival();
		}

		guestDF.setColumn("GUESTS", id);
		guestDF.addColumn("request", request);
		guestDF.addColumn("arrival", arrival);
		ampl.setData(guestDF, "GUESTS");

		double[][] weight= new double[gSize][rSize];

		for (int g= 0; g < gSize; g++ ) {
			Guest guest= guests.get(g);
			for (int r= 0; r < rSize; r++ ) {
				Room room= rooms.get(r);
				weight[g][r]= instance.weight(guest, room);
			}
		}

		DataFrame weightsDF= new DataFrame(2, "GUESTS", "ROOMS", "weight");
		weightsDF.setMatrix(weight, id, num);
		ampl.setData(weightsDF);

	}

	/** Sets parameters for set of housekeepers and time intervals. The time intervals <br>
	 * are from 0 to the maximum makespan. The maximum makespan is calculated by summing <br>
	 * over the cleaning times of every room and adding the sum to the latest checkout time.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance whose data will be uploaded (not null) */
	public static void setHousekeepingParams(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		DataFrame housekeeperDF= new DataFrame(1, "HOUSEKEEPERS");

		int n= instance.getH();
		String[] id= new String[n];
		for (int h= 0; h < n; h++ ) {
			id[h]= String.valueOf(h + 1);
		}

		housekeeperDF.setColumn("HOUSEKEEPERS", id);
		ampl.setData(housekeeperDF, "HOUSEKEEPERS");

		DataFrame timeDF= new DataFrame(1, "TIME");

		int maxMakespan= 0;
		int maxRelease= 0;
		for (Room room : instance.rooms()) {
			maxMakespan+= room.process();
			maxRelease= Math.max(maxRelease, room.release());
		}
		maxMakespan+= maxRelease;
		int[] time= new int[maxMakespan + 1];
		for (int t= 0; t <= maxMakespan; t++ ) {
			time[t]= t;
		}

		timeDF.setColumn("TIME", time);
		ampl.setData(timeDF, "TIME");

	}

	/** Sets parameters for a previous assignment. 'prev[g][r]' is set to 1 if guest g <br>
	 * and room r were previously assigned and zero otherwise.
	 *
	 * @param ampl       An instance of AMPL (not null)
	 * @param instance   The instance the previous assignment is for
	 * @param assignment The previous assignment whose data will be set */
	public static void setPreviousAssignment(AMPL ampl, Instance instance, Assignment assignment) {
		if (!assignment.isAssignmentFor(instance))
			throw new IllegalArgumentException("This assignment is not for this instance");

		ArrayList<Room> rooms= instance.rooms();
		int rSize= rooms.size();
		String[] num= new String[rSize];
		for (int i= 0; i < rSize; i++ ) {
			Room room= rooms.get(i);
			num[i]= Integer.toString(room.num());
		}

		ArrayList<Guest> guests= instance.guests();
		int gSize= guests.size();
		String[] id= new String[gSize];
		for (int i= 0; i < gSize; i++ ) {
			Guest guest= guests.get(i);
			id[i]= Integer.toString(guest.id());
		}

		BidiMap<Guest, Room> prevAssign= assignment.assignment();

		double[][] prev= new double[gSize][rSize];

		for (int g= 0; g < gSize; g++ ) {
			Guest guest= guests.get(g);
			for (int r= 0; r < rSize; r++ ) {
				Room room= rooms.get(r);
				Guest assigned= prevAssign.getKey(room);
				if (assigned != null && assigned.equals(guest)) {
					prev[g][r]= 1;
				} else {
					prev[g][r]= 0;
				}
			}
		}

		DataFrame prevDF= new DataFrame(2, "GUESTS", "ROOMS", "prev");
		prevDF.setMatrix(prev, id, num);
		ampl.setData(prevDF);
	}

	/** Return the room assignment associated with the decision variable 'assign' in the <br>
	 * given AMPL instance. "Assign" indicates which room a guest is assigned to.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance the returned room assignment is for (not null)
	 * @return The room assignment for the instance */
	public static Assignment getAssignment(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Assignment assignment= new Assignment(instance);

		for (Guest guest : instance.guests()) {
			for (Room room : instance.rooms()) {
				String g= Integer.toString(guest.id());
				String r= Integer.toString(room.num());
				double assign= ampl.getVariable("assign").get(g, r).value();
				if (assign == 1.0) {
					assignment.assign(guest, room);
					break;
				}
			}
		}

		return assignment;
	}

	/** Return the housekeeping schedule associated with the decision variable 'schedule' <br>
	 * in the given AMPL instance. "Schedule" indicates which housekeeper cleans each room <br>
	 * and the time interval in which the housekeeper begins cleaning.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance the returned housekeeping schedule is for (not null)
	 * @return The housekeeping schedule for the instance */
	public static Schedule getSchedule(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Schedule schedule= new Schedule(instance);
		ArrayList<Room> rooms= instance.rooms();

		for (Room room : rooms) {
			for (Housekeeper housekeeper : schedule.getHousekeepers()) {
				for (Object t : ampl.getSet("TIME").toArray()) {
					String r= Integer.toString(room.num());
					String h= Integer.toString(housekeeper.id());
					double sched= ampl.getVariable("schedule").get(r, h, t).value();
					if (sched == 1.0) {
						schedule.add(housekeeper, room, ((Double) t).intValue());
						break;
					}
				}
			}
		}

		return schedule;

	}

	/** Return the room assignment associated with the decision variable 'assign' and the <br>
	 * housekeeping schedule associated with the decision variable 'schedule' in the given AMPL <br>
	 * instance. "Assign" indicates which room a guest is assigned to. "Schedule" indicates which <br>
	 * housekeeper cleans each room and the time interval in which the housekeeper begins cleaning.
	 *
	 * @param ampl     An instance of AMPL (not null)
	 * @param instance The instance the returned housekeeping schedule is for (not null)
	 * @return The solution for the instance */
	public static Solution getSolution(AMPL ampl, Instance instance) {
		if (ampl == null) throw new IllegalArgumentException("AMPL is null");
		if (instance == null) throw new IllegalArgumentException("Instance is null");

		Schedule schedule= getSchedule(ampl, instance);
		Assignment assignment= getAssignment(ampl, instance);

		return new Solution(instance, schedule, assignment);
	}
}
