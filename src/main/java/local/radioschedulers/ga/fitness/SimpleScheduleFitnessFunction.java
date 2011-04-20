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
		Map<Job, Double> timeleftMap = new HashMap<Job, Double>();

		for (Entry<LSTTime, JobCombination> entry : s) {
			JobCombination jc = entry.getValue();
			if (jc == null)
				continue;

			value += evaluateSlot(entry.getKey(), entry.getValue(),
					previousEntry, timeleftMap);
			previousEntry = jc;
		}
		return value;
	}

	protected double evaluateSlot(LSTTime t, JobCombination jc,
			JobCombination previousEntry, Map<Job, Double> timeleftMap) {
		double timeleft;
		double value = 0;
		boolean inPreviousSlot;

		for (Job j : jc.jobs) {
			if (!timeleftMap.containsKey(j)) {
				timeleftMap.put(j, j.hours * 60);
			}
			timeleft = timeleftMap.get(j);

			inPreviousSlot = previousEntry != null
					&& previousEntry.jobs.contains(j);

			timeleft = evaluateSlotJob(t, j, timeleft, inPreviousSlot);
			value += timeleft;
			timeleftMap.put(j, timeleftMap.get(j) - timeleft);
		}
		return value * jc.calculatePriority();
	}

	protected double evaluateSlotJob(LSTTime t, Job j, double timeleft,
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