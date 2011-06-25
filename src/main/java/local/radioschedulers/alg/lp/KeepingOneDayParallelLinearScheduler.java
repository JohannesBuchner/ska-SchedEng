package local.radioschedulers.alg.lp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.serial.JobSelector;
import local.radioschedulers.alg.serial.PrioritizedSelector;

import org.apache.log4j.Logger;

/**
 * we determine the optimal ratios of tasks for each time slot:
 * 
 * @author Johannes Buchner
 */
public class KeepingOneDayParallelLinearScheduler extends
		OneDayParallelLinearScheduler {
	private static Logger log = Logger
			.getLogger(KeepingOneDayParallelLinearScheduler.class);
	private Random r = new Random();
	private Map<Job, Double> timeleft = new HashMap<Job, Double>();

	@Override
	protected Schedule scheduleWithRatioMap(
			Map<Long, Map<JobCombination, Double>> firstDaySchedule,
			ScheduleSpace space) {

		Schedule s = new Schedule();
		JobCombination prevJc = null;
		JobCombination jc = null;
		for (Entry<LSTTime, Set<JobCombination>> e : space) {
			LSTTime t = e.getKey();
			Set<JobCombination> jcs = e.getValue();

			if (prevJc != null && jcs.contains(prevJc)) {
				jc = prevJc;
			} else {
				jc = proportionalSelect(firstDaySchedule.get(t.minute));
			}
			if (jc != null) {
				putIntoSchedule(s, jc, t);
				makeSimilarBw(t, jc, s, space);
			}
			prevJc = jc;
		}

		JobSelector selector = new PrioritizedSelector();
		selector.setTimeleft(timeleft);

		for (Entry<LSTTime, Set<JobCombination>> e : space) {
			LSTTime t = e.getKey();
			if (!s.isEmpty(t))
				continue;

			jc = selector.select(e.getValue());
			if (jc == null)
				continue;
			putIntoSchedule(s, jc, t);
		}
		return s;
	}

	private void putIntoSchedule(Schedule s, JobCombination jc, LSTTime t) {
		s.add(t, jc);
		for (Job j : jc.jobs) {
			Double left = timeleft.get(j);
			if (left == null)
				left = j.hours;
			timeleft.put(j, left - ScheduleSpace.LST_SLOTS_MINUTES / 60.);
		}
	}

	private void makeSimilarBw(LSTTime t, JobCombination jc, Schedule s,
			ScheduleSpace space) {
		LSTTimeIterator it = new LSTTimeIterator(t, new LSTTime(0, 0),
				-ScheduleSpace.LST_SLOTS_MINUTES);
		it.next();
		while (it.hasNext()) {
			t = it.next();
			if (!s.isEmpty(t))
				break;
			if (space.get(t).contains(jc)) {
				putIntoSchedule(s, jc, t);
			} else {
				break;
			}
		}
	}

	protected JobCombination proportionalSelect(Map<JobCombination, Double> map) {
		Double sum = 0.;
		for (Entry<JobCombination, Double> e : map.entrySet()) {
			if (isFinished(e.getKey()))
				continue;
			sum += e.getValue();
		}
		if (sum == 0)
			return null;
		// throw a coin (on which job it lands is proportional to the
		// priority)
		Double coin = r.nextDouble() * sum;
		for (Entry<JobCombination, Double> e : map.entrySet()) {
			if (isFinished(e.getKey()))
				continue;
			coin -= e.getValue();
			if (coin <= 0) {
				// pick this job
				return e.getKey();
			}
		}
		throw new IllegalStateException();
	}

	protected boolean isFinished(JobCombination jc) {
		// TODO: extend for parallel jobs

		for (Job j : jc.jobs) {
			if (timeleft.containsKey(j) && timeleft.get(j) <= 0)
				return true;
		}
		return false;
	}
}
