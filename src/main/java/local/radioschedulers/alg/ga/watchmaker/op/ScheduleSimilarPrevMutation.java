package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;
import org.uncommons.maths.random.Probability;

/**
 * Operator that randomly mutates a slot and tries to extend the previous Job if
 * possible.
 * 
 * @author Johannes Buchner
 */
public class ScheduleSimilarPrevMutation extends AbstractScheduleMutation {
	private static Logger log = Logger
			.getLogger(ScheduleSimilarPrevMutation.class);

	public ScheduleSimilarPrevMutation(ScheduleSpace possibles,
			Probability probability) {
		this(possibles, probability, true);
	}

	ScheduleSimilarPrevMutation(ScheduleSpace possibles,
			Probability probability, boolean normalize) {
		super(possibles, normalize ? new Probability(Math.min(1., probability
				.doubleValue()
				* Schedule.LST_SLOTS_PER_HOUR)) : probability);
	}

	@Override
	protected Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;
		JobCombination lastJc = null;
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			JobCombination jc = e.getValue();

			Set<JobCombination> jcs = possibles.get(t);
			if (!jcs.isEmpty()) {
				if (jc != null)
					s2.add(t, jc);
				if (lastJc != null && !lastJc.equals(jc)
						&& containsOneOf(jcs, lastJc)) {
					if (mutationProbability.nextValue().nextEvent(rng)) {
						if (log.isDebugEnabled())
							log.debug("@" + t + " changing from " + jc);
						jc = getMostSimilar(lastJc, jcs);
						if (log.isDebugEnabled())
							log.debug("@" + t + " changing  to  " + jc);
						if (jc != null) {
							s2.add(t, jc);
							i++;
						}
					}
					n++;
				}
			}
			lastJc = jc;
		}
		updateHistory(s2, s1, i, n);
		updateCounters(s2, s1, i);

		return s2;
	}

	private boolean containsOneOf(Set<JobCombination> jcs, JobCombination lastJc) {
		if (jcs.contains(lastJc))
			return true;
		for (Job j : lastJc.jobs) {
			JobCombination jc = new JobCombination();
			jc.jobs.add(j);
			if (jcs.contains(j))
				return true;
		}
		return false;
	}

}
