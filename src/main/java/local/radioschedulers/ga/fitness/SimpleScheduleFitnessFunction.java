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
import local.radioschedulers.SpecificSchedule;
import local.radioschedulers.ga.ScheduleFitnessFunction;

public final class SimpleScheduleFitnessFunction implements
		ScheduleFitnessFunction {
	@Override
	public double evaluate(SpecificSchedule s) {
		double v = 0.;
		JobCombination previousEntry = null;
		double time;
		Long timeleft;
		Map<Job, Long> timeleftMap = new HashMap<Job, Long>();

		for (Entry<LSTTime, JobCombination> entry : s) {
			JobCombination jc = entry.getValue();

			for (Job j : jc.jobs) {
				if (timeleftMap.containsKey(j)) {
					timeleftMap.put(j, j.hours);
				}
				timeleft = timeleftMap.get(j)
						- SpecificSchedule.LST_SLOTS_MINUTES;
				timeleftMap.put(j, timeleft);

				if (previousEntry != null && previousEntry.jobs.contains(j)) {
					// full time for continued
					time = SpecificSchedule.LST_SLOTS_MINUTES;
				} else {
					// some time lost for new observation
					time = SpecificSchedule.LST_SLOTS_MINUTES * 0.7;
				}
				if (timeleft < 0) {
					/* we are over desired limit already, no benefits */
					time = 0;
				}
				
				// TODO: add checks that the observation can actually be made
				if (!j.isAvailable(entry.getKey())) {
					time = 0;
				}

				// TODO: add benefit based on observation conditions

				v = Math
						.log(Math.exp(v) + Math.exp(j.proposal.priority * time));
			}
			previousEntry = jc;

		}
		return v;
	}
}