package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.watchmaker.GeneticHistory;
import local.radioschedulers.alg.ga.watchmaker.MutationCounter;
import local.radioschedulers.alg.ga.watchmaker.SortedCollection;
import local.radioschedulers.alg.ga.watchmaker.SortedCollection.MappingFunction;

import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

public abstract class AbstractScheduleMutation implements
		EvolutionaryOperator<Schedule> {
	protected final NumberGenerator<Probability> mutationProbability;
	protected ScheduleSpace possibles;

	protected GeneticHistory<Schedule, ?> history;
	private MutationCounter<Schedule, String> counter;

	public void setHistory(GeneticHistory<Schedule, ?> history) {
		this.history = history;
	}

	public void setCounter(MutationCounter<Schedule, String> counter) {
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
		if (lastJc == null)
			throw new NullPointerException("lastJc must not be null!");
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
			counter.add(s2, this.toString(), i);
		}
	}

	protected void updateHistory(Schedule s2, Schedule s1, Integer i, Integer n) {
		if (history != null) {
			history.derive(s2, s1, 1 - i * 1. / n);
			// rest is random
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected JobCombination chooseRandomOther(Random rng,
			Set<JobCombination> jcs, JobCombination jc) {
		if (jcs.isEmpty())
			throw new IllegalArgumentException("Set should not be empty");
		if (jcs.size() == 1) {
			return jcs.iterator().next();
		}
		if (jc != null)
			jcs.remove(jc);
		JobCombination newjc = (JobCombination) jcs.toArray()[rng.nextInt(jcs
				.size())];
		if (newjc == null)
			throw new NullPointerException();
		if (jc != null)
			jcs.add(jc);
		return newjc;
	}
}
