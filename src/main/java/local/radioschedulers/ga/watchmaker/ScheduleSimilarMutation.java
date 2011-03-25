package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

import org.apache.log4j.Logger;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

public class ScheduleSimilarMutation implements EvolutionaryOperator<Schedule> {
	private final NumberGenerator<Probability> mutationProbability;
	private ScheduleSpace possibles;

	private static Logger log = Logger.getLogger(ScheduleSimilarMutation.class);

	public GeneticHistory<Schedule, ?> history;

	public void setHistory(GeneticHistory<Schedule, ?> history) {
		this.history = history;
	}

	public ScheduleSimilarMutation(ScheduleSpace possibles,
			Probability probability) {
		this(possibles, new ConstantGenerator<Probability>(probability));
	}

	public ScheduleSimilarMutation(ScheduleSpace possibles,
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

		JobCombination lastJc = null;

		for (Iterator<Entry<LSTTime, JobCombination>> it = s1.iterator(); it
				.hasNext();) {
			Entry<LSTTime, JobCombination> e = it.next();
			LSTTime t = e.getKey();
			// log.debug("considering " + t);
			JobCombination jc = e.getValue();
			Set<JobCombination> jcs = possibles.get(t);
			if (jc != null && !jcs.isEmpty()) {
				s2.add(t, jc);
				/* only if we have a change, we should consider it */
				if ((lastJc == null || !lastJc.equals(jc))
						&& (mutationProbability.nextValue().nextEvent(rng))) {
					log.debug("mutating around " + t);
					i += makeSimilarAround(t, jc, possibles, s2);
				}
				n++;
			}
			lastJc = jc;
		}
		log.debug("changed " + i + " of " + n);
		if (history != null) {
			history.derive(s2, s1, i * 1. / n);
			// rest is random
		}

		return s2;
	}

	private int makeSimilarAround(LSTTime t, JobCombination thisjc,
			ScheduleSpace template, Schedule s2) {
		int countChanged = 0;
		LSTTimeIterator it = new LSTTimeIterator(new LSTTime(0, 1),
				new LSTTime(1, 0), Schedule.LST_SLOTS_MINUTES);
		log.debug("jc " + thisjc);
		for (; it.hasNext();) {
			LSTTime tDelta = it.next();
			// log.debug("tDelta " + tDelta);
			LSTTime tMinus = new LSTTime(t.day - tDelta.day, t.minute
					- tDelta.minute);
			if (tMinus.minute < 0) {
				long extraDays = tMinus.minute
						/ (Schedule.LST_SLOTS_PER_DAY * Schedule.LST_SLOTS_MINUTES)
						+ 1;
				tMinus.day -= extraDays;
				tMinus.minute += extraDays
						* (Schedule.LST_SLOTS_PER_DAY * Schedule.LST_SLOTS_MINUTES);
			}
			log.debug("tMinus " + tMinus);

			if (tMinus.day < 0)
				break;

			Set<JobCombination> jcs = template.get(tMinus);
			log.debug("jcs " + jcs);
			if (!jcs.isEmpty()) {
				JobCombination jc = getMostSimilar(thisjc, jcs);
				if (jc != null) {
					log.debug("jc " + jc);
					s2.add(tMinus, jc);
					countChanged++;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return countChanged;
	}

	private JobCombination getMostSimilar(final JobCombination lastJc,
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
