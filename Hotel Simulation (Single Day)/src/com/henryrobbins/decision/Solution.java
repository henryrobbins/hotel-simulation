package com.henryrobbins.decision;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;

/** Maintains both a housekeeping schedule and room assignment for a given instance <br>
 * Furthermore, it maintains additional statistics for the combined housekeeping <br>
 * schedule and room assignment such as the overlap or wait-time. */
public final class Solution implements Decision {

	/** The instance that this solution is for */
	private final Instance instance;
	/** A housekeeping schedule for the relevant instance */
	private final Schedule schedule;
	/** A room assignment for the relevant instance */
	private final Assignment assignment;

	/** The set of tardiness' for every guest */
	private DescriptiveStatistics tardiness;
	/** The set of lateness' for every guest */
	private DescriptiveStatistics lateness;

	/** Construct a solution for the given instance consisting of the specified housekeeping <br>
	 * schedule and room assignment. Update the maintained statistics.
	 *
	 * @param instance   The instance that this solution will be for (not null)
	 * @param schedule   The housekeeping schedule for this instance (not null)
	 * @param assignment The room assignment for this instance (not null) */
	public Solution(Instance instance, Schedule schedule, Assignment assignment) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		if (schedule == null) throw new IllegalArgumentException("Schedule was null");
		if (!schedule.isScheduleFor(instance)) throw new IllegalArgumentException("Invalid schedule");
		if (assignment == null) throw new IllegalArgumentException("Assignment was null");
		if (!assignment.isAssignmentFor(instance)) throw new IllegalArgumentException("Invalid assignment");
		this.instance= instance;
		this.schedule= schedule;
		this.assignment= assignment;
		setStats();
	}

	/** Return the housekeeping schedule */
	public Schedule schedule() {
		return schedule;
	}

	/** Return the room assignment */
	public Assignment assignment() {
		return assignment;
	}

	/** Return the tardiness statistics */
	public DescriptiveStatistics tardinessStats() {
		return tardiness;
	}

	/** Return the lateness statistics */
	public DescriptiveStatistics latenessStats() {
		return lateness;
	}

	/** Set statistics */
	private void setStats() {
		tardiness= new DescriptiveStatistics();
		lateness= new DescriptiveStatistics();
		for (Guest guest : instance.guests()) {
			Room assigned= assignment.assignment().get(guest);
			tardiness.addValue(schedule.tardinessOf(guest, assigned));
			lateness.addValue(schedule.latenessOf(guest, assigned));
		}
	}

	/** Get a string representing this solution */
	@Override
	public String toString() {
		return schedule.toString() + "\n" + assignment.toString();
	}

}
