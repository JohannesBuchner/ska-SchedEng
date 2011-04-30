package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ScheduleJobPlacementMutation extends ScheduleSimilarMutation {

	protected Map<JobCombination, List<LSTTime>> possibleSlots = new HashMap<JobCombination, List<LSTTime>>();
	protected List<JobCombination> jobs;

	public ScheduleJobPlacementMutation(ScheduleSpace possibles,
			Probability probability) {
		super(possibles, probability);

		for (Entry<LSTTime, Set<JobCombination>> e : possibles) {
			for (JobCombination jc : e.getValue()) {
				List<LSTTime> l = possibleSlots.get(jc);
				if (l == null) {
					l = new ArrayList<LSTTime>();
					possibleSlots.put(jc, l);
				}
				l.add(e.getKey());
			}
		}
		setForwardsKeep(true);
		setBackwardsKeep(true);
		jobs = new ArrayList<JobCombination>(possibleSlots.keySet());
	}

	@Override
	protected Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			JobCombination jc = s1.get(t);

			Set<JobCombination> jcs = possibles.get(t);
			if (e.getValue() != null && !jcs.isEmpty()) {
				s2.add(t, jc);
			}
			n++;
		}
		for (JobCombination jc : jobs) {
			if (this.mutationProbability.nextValue().nextEvent(rng)) {
				List<LSTTime> slots = possibleSlots.get(jc);
				LSTTime t = slots.get(rng.nextInt(slots.size()));
				s2.add(t, jc);
				i += 1 + makeSimilarAround(t, jc, possibles, s2);
			}
		}
		updateHistory(s2, s1, i, n);
		updateCounters(s2, s1, i);

		return s2;
	}
}
