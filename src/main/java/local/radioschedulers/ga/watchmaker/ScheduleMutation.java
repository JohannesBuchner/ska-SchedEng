package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
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

public class ScheduleMutation implements EvolutionaryOperator<Schedule> {
	private final NumberGenerator<Probability> mutationProbability;
	private ScheduleSpace possibles;

	public GeneticHistory<Schedule, ?> history;

	public void setHistory(GeneticHistory<Schedule, ?> history) {
		this.history = history;
	}

	public ScheduleMutation(ScheduleSpace possibles, Probability probability) {
		this(possibles, new ConstantGenerator<Probability>(probability));
	}

	public ScheduleMutation(ScheduleSpace possibles,
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
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			if (mutationProbability.nextValue().nextEvent(rng)) {
				Set<JobCombination> jcs = possibles.get(t);
				if (jcs != null && jcs.size() > 0) {
					JobCombination jc = (JobCombination) jcs.toArray()[rng
							.nextInt(jcs.size())];
					s2.add(t, jc);
				}
				i++;
			} else {
				s2.add(t, s1.get(t));

			}
			n++;
		}
		if (history != null) {
			history.derive(s2, s1, i * 1. / n);
			// rest is random
		}

		return s2;
	}

}
