package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.uncommons.maths.random.Probability;

/**
 * Operator that randomly mutates a slot and tries to extend the previous Job if
 * possible.
 * 
 * @author Johannes Buchner
 */
public class ScheduleSimilarPrevMutation extends AbstractScheduleMutation {

	public ScheduleSimilarPrevMutation(ScheduleSpace possibles,
			Probability probability) {
		super(possibles, probability);
	}

	@Override
	protected Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;
		JobCombination lastJc = null;
		Probability u = new Probability(2. / 3);
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			JobCombination jc = s1.get(t);

			Set<JobCombination> jcs = possibles.get(t);
			if (e.getValue() != null && !jcs.isEmpty()) {
				s2.add(t, jc);
				if (lastJc != null && !lastJc.equals(jc)) {
					if (u.nextEvent(rng)
							&& mutationProbability.nextValue().nextEvent(rng)) {
						jc = getMostSimilar(lastJc, jcs);
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

}
