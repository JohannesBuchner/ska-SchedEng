package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

public abstract class AbstractScheduleMutation implements
		EvolutionaryOperator<Schedule> {
	protected final NumberGenerator<Probability> mutationProbability;
	protected ScheduleSpace possibles;

	protected GeneticHistory<Schedule, ?> history;
	private MutationCounter<Schedule, Class> counter;

	public void setHistory(GeneticHistory<Schedule, ?> history) {
		this.history = history;
	}

	public void setCounter(MutationCounter<Schedule, Class> counter) {
		this.counter = counter;
	}

	public AbstractScheduleMutation(ScheduleSpace possibles,
			Probability probability) {
		this(possibles, new ConstantGenerator<Probability>(probability));
	}

	public AbstractScheduleMutation(ScheduleSpace possibles,
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

	abstract protected Schedule mutateSchedule(Schedule s1, Random rng);

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

	protected void updateCounters(Schedule s2, Schedule s1, Integer i) {
		if (counter != null) {
			counter.derive(s2, s1);
			counter.add(s2, this.getClass(), i);
		}
	}
}
