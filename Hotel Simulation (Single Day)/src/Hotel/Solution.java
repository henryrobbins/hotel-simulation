package Hotel;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/** Maintains both a housekeeping schedule and room assignment for a given instance <br>
 * Furthermore, it maintains additional statistics for the combined housekeeping <br>
 * schedule and room assignment such as the overlap or wait-time. */
public final class Solution {

	/** The instance that this solution is for */
	private final Instance instance;
	/** A housekeeping schedule for the relevant instance */
	private final HousekeepingSchedule schedule;
	/** A room assignment for the relevant instance */
	private final RoomAssignment assignment;

	/** The set of overlaps between guest's arrival times and room's availabilities */
	private DescriptiveStatistics overlaps;

	/** Construct a solution for the given instance consisting of the specified housekeeping <br>
	 * schedule and room assignment. Update the maintained statistics.
	 *
	 * @param instance   The instance that this solution will be for (not null)
	 * @param schedule   The housekeeping schedule for this instance (not null)
	 * @param assignment The room assignment for this instance (not null) */
	public Solution(Instance instance, HousekeepingSchedule schedule, RoomAssignment assignment) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		if (schedule == null) throw new IllegalArgumentException("Schedule was null");
		if (!schedule.isScheduleFor(instance)) throw new IllegalArgumentException("Invalid schedule");
		if (assignment == null) throw new IllegalArgumentException("Assignment was null");
		if (!assignment.isAssignmentFor(instance)) throw new IllegalArgumentException("Invalid assignment");
		this.instance= instance;
		this.schedule= schedule;
		this.assignment= assignment;
		resetStats();
	}

	/** Return the housekeeping schedule */
	public HousekeepingSchedule getSchedule() {
		return schedule;
	}

	/** Return the room assignment */
	public RoomAssignment getAssignment() {
		return assignment;
	}

	/** Return the total overlap */
	public int getTotalOverlap() {
		return (int) overlaps.getSum();
	}

	/** Return the maximum overlap */
	public int getMaximumOverlap() {
		return (int) overlaps.getMax();
	}

//	/** Set the housekeeping schedule
//	 *
//	 * @param schedule The housekeeping schedule for this instance (not null) */
//	private void setSchedule(HousekeepingSchedule schedule) {
//		if (schedule == null) throw new IllegalArgumentException("Schedule was null");
//		if (!schedule.isScheduleFor(instance)) throw new IllegalArgumentException("Invalid schedule");
//		this.schedule= schedule;
//		if (assignment != null) {
//			resetStats();
//		}
//	}
//
//	/** Set the room assignment
//	 *
//	 * @param assignment The room assignment for this instance (not null) */
//	private void setAssignment(RoomAssignment assignment) {
//		if (assignment == null) throw new IllegalArgumentException("Assignment was null");
//		if (!assignment.isAssignmentFor(instance)) throw new IllegalArgumentException("Invalid assignment");
//		this.assignment= assignment;
//		if (schedule != null) {
//			resetStats();
//		}
//	}

	/** Reset the statistics */
	private void resetStats() {
		overlaps= new DescriptiveStatistics();
		for (Room room : instance.getRooms()) {
			Guest guest= assignment.getAssignment().get(room);
			if (guest != null) {
				overlaps.addValue(guest.getOverlap(room, schedule));
			}
		}
	}

	/** Get a string representing this solution */
	@Override
	public String toString() {
		return schedule.toString() + "\n" + assignment.toString();
	}

}
