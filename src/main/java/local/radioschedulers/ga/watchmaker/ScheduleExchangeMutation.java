package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

/**
 * selects random slot and tries to exchange with the same slot from a previous
 * day
 * 
 * @author Johannes Buchner
 */
public class ScheduleExchangeMutation implements EvolutionaryOperator<Schedule> {
	private final NumberGenerator<Probability> mutationProbability;
	private ScheduleSpace possibles;

	public GeneticHistory<Schedule, ?> history;
	private MutationCounter<Schedule, String> counter;

	public void setHistory(GeneticHistory<Schedule, ?> history) {
		this.history = history;
	}

	public void setCounter(MutationCounter<Schedule, String> counter) {
		this.counter = counter;
	}

	public ScheduleExchangeMutation(ScheduleSpace possibles,
			Probability probability) {
		this(possibles, new ConstantGenerator<Probability>(probability));
	}

	public ScheduleExchangeMutation(ScheduleSpace possibles,
			NumberGenerator<Probability> mutationProbability) {
		this.possibles = possibles;
		this.mutationProbability = mutationProbability;
	}

	@Override
	public List<Schedule> apply(List<Schedule> selectedCandidates, Random rng) {
		List<Schedule> mutatedPopulation = new ArrayList<Schedule>(
				selectedCandidates.size());
		for (Schedule s : selectedCandidates) {
			mutatedPopulation.add(mutateSchedule(s, rng));
		}
		return mutatedPopulation;
	}

	private Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;
		Probability prob = mutationProbability.nextValue();
		Probability u = new Probability(1. / 3);
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			s2.add(t, e.getValue());
			Set<JobCombination> jcs = possibles.get(t);
			if ((i * 1. / n) < prob.doubleValue() && u.nextEvent(rng)) {
				if (!jcs.isEmpty()) {
					JobCombination jc = e.getValue();
					List<LSTTime> timeCandidates = new ArrayList<LSTTime>();
					if (t.day > 3)
						timeCandidates.add(new LSTTime(t.day - 3, t.minute));
					if (t.day > 2)
						timeCandidates.add(new LSTTime(t.day - 2, t.minute));
					if (t.day > 1)
						timeCandidates.add(new LSTTime(t.day - 1, t.minute));

					Collections.shuffle(timeCandidates);
					// find a exchange partner
					boolean foundPartner = false;
					for (LSTTime t2 : timeCandidates) {
						JobCombination jc2 = s1.get(t2);
						Set<JobCombination> jcs2 = possibles.get(t2);
						/*
						 * partners should be exchangeable, but not the same,
						 * and not both null
						 */
						if ((jc == null || jcs2.contains(jc))
								&& (jc2 == null || jcs.contains(jc2))
								&& !(jc2 == null && jc == null)
								&& (jc == null || !jc.equals(jc2))) {

							// switch
							if (jc != null)
								s2.add(t2, jc);
							if (jc2 != null)
								s2.add(t, jc2);
							foundPartner = true;
							break;
						}
					}
					if (foundPartner) {
						i++; /* we let this count */
					} else {
						s2.add(t, e.getValue());
					}
				}
			}
			n++;
		}
		if (history != null) {
			history.derive(s2, s1, i * 1. / n);
		}
		if (counter != null) {
			counter.derive(s2, s1);
			counter.add(s2, this.toString(), i);
		}

		return s2;
	}

}
