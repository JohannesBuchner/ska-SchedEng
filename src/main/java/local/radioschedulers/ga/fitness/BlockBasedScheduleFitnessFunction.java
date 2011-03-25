/**
 * 
 */
package local.radioschedulers.ga.fitness;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ga.ScheduleFitnessFunction;

import org.apache.log4j.Logger;

/**
 * emphasis is on successful, long, continuous observations
 * 
 * @author user
 */
public class BlockBasedScheduleFitnessFunction implements
		ScheduleFitnessFunction {
	private static final int CALIBRATION_PENALTY = Schedule.LST_SLOTS_MINUTES / 3;
	private static final int MIN_BLOCK_HOURS = 1;
	private static final boolean MINUTES_SQUARED = false;
	private static final boolean GIVE_CREDIT_FOR_FINISHED = true;

	private static Logger log = Logger
			.getLogger(BlockBasedScheduleFitnessFunction.class);

	@Override
	public double evaluate(Schedule s) {

		double value = 0.;
		Map<Job, Long> continuedJobs = new HashMap<Job, Long>();
		Map<Job, Long> timeleftMap = new HashMap<Job, Long>();

		for (Entry<LSTTime, JobCombination> entry : s) {
			JobCombination jc = entry.getValue();

			if (jc != null) {
				for (Job j : jc.jobs) {
					if (continuedJobs.containsKey(j)) {
						// just continued. give time credit
						continuedJobs.put(j, continuedJobs.get(j)
								+ Schedule.LST_SLOTS_MINUTES);
					} else {
						// new
						continuedJobs.put(j, (long) Schedule.LST_SLOTS_MINUTES);
					}
				}
			}
			Set<Job> keys = new HashSet<Job>(continuedJobs.keySet());
			for (Job j : keys) {
				if (jc != null && !jc.jobs.contains(j)) {
					value += giveCredit(continuedJobs, j, timeleftMap);
				}
			}
		}

		Set<Job> keys = new HashSet<Job>(continuedJobs.keySet());
		// all jobs stop here. give credit
		for (Job j : keys) {
			value += giveCredit(continuedJobs, j, timeleftMap);
		}

		if (GIVE_CREDIT_FOR_FINISHED) {
			// give extra credit for finished tasks
			for (Entry<Job, Long> e : timeleftMap.entrySet()) {
				if (e.getValue() <= 0) {
					Job j = e.getKey();
					value += j.hours * j.proposal.priority;
				}
			}
		}

		return value;
	}

	/**
	 * j finished a block of time
	 * 
	 * this routine removes j from continuedJobs.
	 * 
	 * @param continuedJobs
	 * @param j
	 * @param timeleftMap
	 * @return a benefit of this block
	 */
	private double giveCredit(Map<Job, Long> continuedJobs, Job j,
			Map<Job, Long> timeleftMap) {
		if (!timeleftMap.containsKey(j)) {
			timeleftMap.put(j, j.hours * Schedule.LST_SLOTS_MINUTES
					* Schedule.LST_SLOTS_PER_DAY);
		}

		long minutes = continuedJobs.remove(j);
		// log.debug("end of " + minutes + " min long block of " + j);

		// no slots shorter than 4 hours wanted
		if (minutes < 60 * MIN_BLOCK_HOURS) {
			minutes = 0; // 3 / 4;
		} else {
			minutes = minutes - CALIBRATION_PENALTY;
			long timeleft = timeleftMap.get(j) - minutes;
			if (timeleft < 0) {
				// job is done
				minutes = 0L;
			}
			timeleftMap.put(j, timeleft);
		}
		double value = j.proposal.priority * minutes / 60;

		if (MINUTES_SQUARED)
			value *= minutes / 60;

		return value;
	}

	protected boolean areResourcesAvailable(JobWithResources jr, LSTTime t) {
		return true;
	}
}