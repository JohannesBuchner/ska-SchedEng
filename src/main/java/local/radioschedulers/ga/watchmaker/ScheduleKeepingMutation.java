package local.radioschedulers.ga.watchmaker;

import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

import org.apache.log4j.Logger;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;

/**
 * Operator that randomly mutates a slot and tries to extend the selection
 * forward as long as possible
 * 
 * @author Johannes Buchner
 */
public class ScheduleKeepingMutation extends AbstractScheduleMutation {
	private static Logger log = Logger.getLogger(ScheduleKeepingMutation.class);

	public ScheduleKeepingMutation(ScheduleSpace possibles,
			Probability probability) {
		super(possibles, probability);
	}

	public ScheduleKeepingMutation(ScheduleSpace possibles,
			NumberGenerator<Probability> mutationProbability) {
		super(possibles, mutationProbability);
	}

	@Override
	protected Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;
		JobCombination lastJc = null;
		boolean lastwaschanged = false;
		for (Entry<LSTTime, JobCombination> e : s1) {
			lastwaschanged = false;
			LSTTime t = e.getKey();
			JobCombination jc = s1.get(t);
			Set<JobCombination> jcs = possibles.get(t);
			if (e.getValue() != null && !jcs.isEmpty()) {
				s2.add(t, jc);
				if (lastJc != null && !lastJc.equals(jc)) {
					if ((lastwaschanged || mutationProbability.nextValue()
							.nextEvent(rng))) {
						jc = getMostSimilar(lastJc, jcs);
						if (jc != null) {
							s2.add(t, jc);
							i++;
							lastwaschanged = true;
						} else {
							lastwaschanged = false;
						}
					} else {
						lastwaschanged = false;
					}
					n++;
				}
			}
			lastJc = e.getValue();
		}
		log.debug("changed " + i + " of " + n);
		if (history != null) {
			history.derive(s2, s1, i * 1. / n);
			// rest is random
		}

		return s2;
	}

	protected JobCombination getMostSimilar(final JobCombination lastJc,
			Set<JobCombination> jcs) {
		MappingFunction<JobCombination, Integer> f = getMappingFunction(lastJc);
		JobCombination v = new SortedCollection<JobCombination>(jcs, f).first();
		if (f.map(v) == 0)
			return null;
		else
			return v;
	}

	private MappingFunction<JobCombination, Integer> getMappingFunction(
			final JobCombination lastJc) {
		return new MappingFunction<JobCombination, Integer>() {

			@Override
			public Integer map(JobCombination item) {
				int count = 0;
				for (Job j : lastJc.jobs) {
					if (item.jobs.contains(j)) {
						count++;
					}
				}
				return -count;
			}
		};
	}

}
