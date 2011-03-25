/**
 * 
 */
package local.radioschedulers.ga.fitness;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ga.ScheduleFitnessFunction;

public class SimpleScheduleFitnessFunction implements ScheduleFitnessFunction {

	private int switchLostMinutes = 5;

	public void setSwitchLostMinutes(int switchLostMinutes) {
		this.switchLostMinutes = switchLostMinutes;
	}

	public int getSwitchLostMinutes() {
		return switchLostMinutes;
	}

	@Override
	public double evaluate(Schedule s) {
		double value = 0.;
		JobCombination previousEntry = null;
		Map<Job, Long> timeleftMap = new HashMap<Job, Long>();

		for (Entry<LSTTime, JobCombination> entry : s) {
			JobCombination jc = entry.getValue();
			value += evaluateSlot(entry.getKey(), entry.getValue(),
					previousEntry, timeleftMap);
			previousEntry = jc;
		}
		return value;
	}

	protected double evaluateSlot(LSTTime t, JobCombination jc,
			JobCombination previousEntry, Map<Job, Long> timeleftMap) {
		Long timeleft;
		double value = 0;
		boolean inPreviousSlot;

		if (jc == null) {
			// no points for doing nothing
			return 0;
		} else {
			for (Job j : jc.jobs) {
				if (!timeleftMap.containsKey(j)) {
					timeleftMap.put(j, j.hours * Schedule.LST_SLOTS_MINUTES
							* Schedule.LST_SLOTS_PER_DAY);
				}
				timeleft = timeleftMap.get(j) - Schedule.LST_SLOTS_MINUTES;
				timeleftMap.put(j, timeleft);

				inPreviousSlot = previousEntry != null
						&& previousEntry.jobs.contains(j);

				value += evaluateSlotJob(t, j, timeleft, inPreviousSlot);
			}
			return value * jc.calculatePriority();
		}
	}

	protected double evaluateSlotJob(LSTTime t, Job j, Long timeleft,
			boolean inPreviousSlot) {
		double time;
		if (inPreviousSlot) {
			// full time for continued
			time = Schedule.LST_SLOTS_MINUTES;
		} else {
			// some time lost for new observation
			time = Schedule.LST_SLOTS_MINUTES - this.switchLostMinutes;
			if (time < 0)
				time = 0;
		}
		if (timeleft < 0) {
			/* we are over desired limit already, no benefits */
			time = 0;
		}

		// checks that the observation can actually be made
		if (j instanceof JobWithResources) {
			JobWithResources jr = (JobWithResources) j;
			if (!areResourcesAvailable(jr, t)) {
				time = 0;
			}
		}

		// TODO: add benefit based on observation conditions

		return time;
	}

	protected boolean areResourcesAvailable(JobWithResources jr, LSTTime t) {
		return true;
	}
}