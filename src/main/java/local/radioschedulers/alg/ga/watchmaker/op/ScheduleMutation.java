package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;

/**
 * mutates random slots
 * 
 * @author Johannes Buchner
 */
public class ScheduleMutation extends AbstractScheduleMutation {
	public ScheduleMutation(ScheduleSpace possibles, Probability probability) {
		super(possibles, probability);
	}

	public ScheduleMutation(ScheduleSpace possibles,
			NumberGenerator<Probability> mutationProbability) {
		super(possibles, mutationProbability);
	}

	@Override
	protected Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;
		boolean lastFailed = false;
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			if (s1.get(t) != null)
				s2.add(t, s1.get(t));
			Set<JobCombination> jcs = possibles.get(t);
			if (!jcs.isEmpty()) {
				if (lastFailed
						|| mutationProbability.nextValue().nextEvent(rng)) {
					JobCombination jc = (JobCombination) jcs.toArray()[rng
							.nextInt(jcs.size())];
					s2.add(t, jc);
					if (jc.equals(e.getValue())) {
						lastFailed = true;
					} else {
						i++;
						lastFailed = false;
					}
				}
			}
			n++;
		}
		updateHistory(s2, s1, i, n);
		updateCounters(s2, s1, i);

		return s2;
	}

}