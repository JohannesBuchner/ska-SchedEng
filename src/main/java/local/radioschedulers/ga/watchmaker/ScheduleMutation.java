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

	private Schedule mutateSchedule(Schedule s, Random rng) {
		for (Entry<LSTTime, JobCombination> e : s) {
			if (mutationProbability.nextValue().nextEvent(rng)) {
				LSTTime t = e.getKey();
				s.clear(t);
				Set<JobCombination> jcs = possibles.get(t);
				if (jcs != null && jcs.size() > 0) {
					JobCombination jc = (JobCombination) jcs.toArray()[rng
							.nextInt(jcs.size())];
					s.add(t, jc);
				}
			}
		}
		return s;
	}

}
