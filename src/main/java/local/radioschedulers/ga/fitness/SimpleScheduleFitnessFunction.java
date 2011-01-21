/**
 * 
 */
package local.radioschedulers.ga.fitness;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ga.ScheduleFitnessFunction;

public class SimpleScheduleFitnessFunction implements
		ScheduleFitnessFunction {

	private int switchLostMinutes = 5;

	public void setSwitchLostMinutes(int switchLostMinutes) {
		this.switchLostMinutes = switchLostMinutes;
	}

	@Override
	public double evaluate(Schedule s) {
		double value = 0.;
		JobCombination previousEntry = null;
		Map<Job, Long> timeleftMap = new HashMap<Job, Long>();

		for (Entry<LSTTime, JobCombination> entry : s) {
			JobCombination jc = entry.getValue();
			value += evaluateSlot(jc, previousEntry, timeleftMap);
			previousEntry = jc;
		}
		return value;
	}

	protected double evaluateSlot(JobCombination jc,
			JobCombination previousEntry, Map<Job, Long> timeleftMap) {
		Long timeleft;
		double expvalue = 0;

		if (jc == null) {
			// no points for doing nothing
		} else {
			for (Job j : jc.jobs) {
				if (!timeleftMap.containsKey(j)) {
					timeleftMap.put(j, j.hours * Schedule.LST_SLOTS_MINUTES);
				}
				timeleft = timeleftMap.get(j) - Schedule.LST_SLOTS_MINUTES;
				timeleftMap.put(j, timeleft);

				boolean inPreviousSlot = previousEntry != null
						&& previousEntry.jobs.contains(j);

				expvalue += Math.log(evaluateSlotJob(j, timeleft,
						inPreviousSlot));
			}
		}
		return Math.exp(expvalue);
	}

	private double evaluateSlotJob(Job j, Long timeleft, boolean inPreviousSlot) {
		double time;
		if (inPreviousSlot) {
			// full time for continued
			time = Schedule.LST_SLOTS_MINUTES;
		} else {
			// some time lost for new observation
			time = Schedule.LST_SLOTS_MINUTES - this.switchLostMinutes;
		}
		if (timeleft < 0) {
			/* we are over desired limit already, no benefits */
			time = 0;
		}

		// TODO: add checks that the observation can actually be
		// made
		// if (!j.isAvailable(entry.getKey())) {
		// time = 0;
		// }

		// TODO: add benefit based on observation conditions

		return Math.exp(j.proposal.priority) * time
				/ Schedule.LST_SLOTS_MINUTES;
	}
}