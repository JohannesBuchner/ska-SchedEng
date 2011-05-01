package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;

/**
 * Operator that randomly mutates a slot and tries to extend the selection as
 * long as possible (max 1 day).
 * 
 * @author Johannes Buchner
 */
public class ScheduleKeepingMutation extends ScheduleSimilarMutation {
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
		int toSkip = 0;

		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			JobCombination jc = e.getValue();
			Set<JobCombination> jcs = possibles.get(t);
			if (!jcs.isEmpty() && s2.isEmpty(t)) {
				if (jc != null)
					s2.add(t, jc);
				if (toSkip > 0) {
					toSkip--;
				} else {
					if (normalizeProbability(rng)
							&& mutationProbability.nextValue().nextEvent(rng)) {
						// randomly choose a task
						jc = chooseRandomOther(rng, jcs, jc);

						s2.add(t, jc);
						toSkip = makeSimilarAround(t, jc, possibles, s2);
						i += 1 + toSkip;
						log.debug("mutated and made " + toSkip + " similar");
					}
				}
				n++;
			}
		}
		if (log.isDebugEnabled())
			log.debug("changed " + i + " of " + n);
		updateHistory(s2, s1, i, n);
		updateCounters(s2, s1, i);

		return s2;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
