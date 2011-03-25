package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
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
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			JobCombination jc = s1.get(t);
			Set<JobCombination> jcs = possibles.get(t);
			if (e.getValue() != null && !jcs.isEmpty()) {
				s2.add(t, jc);
				if ((mutationProbability.nextValue().nextEvent(rng))) {
					i += makeSimilarAround(t, possibles, s1, s2);
				}
				n++;
			}
		}
		log.debug("changed " + i + " of " + n);
		if (history != null) {
			history.derive(s2, s1, i * 1. / n);
			// rest is random
		}

		return s2;
	}

	private int makeSimilarAround(LSTTime t, ScheduleSpace template,
			Schedule s, Schedule s2) {
		boolean posContinue = false; // disabled completely
		boolean negContinue = true;
		LSTTime last = template.findLastEntry();
		JobCombination thisjc = s.get(t);
		int countChanged = 0;
		LSTTimeIterator it = new LSTTimeIterator(new LSTTime(1, 0),
				Schedule.LST_SLOTS_MINUTES);
		it.next(); // skip 0
		for (; it.hasNext();) {
			LSTTime tDelta = it.next();
			if (posContinue) {
				LSTTime tPlus = new LSTTime(t.day + tDelta.day, t.minute
						+ tDelta.minute);
				if (tPlus.minute > Schedule.LST_SLOTS_PER_DAY
						* Schedule.LST_SLOTS_MINUTES) {
					long extraDays = tPlus.minute
							/ (Schedule.LST_SLOTS_PER_DAY * Schedule.LST_SLOTS_MINUTES);
					tPlus.day += extraDays;
					tPlus.minute -= extraDays
							* (Schedule.LST_SLOTS_PER_DAY * Schedule.LST_SLOTS_MINUTES);
				}
				log.debug("tPlus " + tPlus);

				if (tPlus.isAfter(last))
					posContinue = false;

				JobCombination jc = getMostSimilar(thisjc, template.get(tPlus));
				if (jc != null) {
					s2.add(t, jc);
					countChanged++;
				} else {
					posContinue = false;
				}
			}
			if (negContinue) {
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
					negContinue = false;

				JobCombination jc = getMostSimilar(thisjc, template.get(tMinus));
				if (jc != null) {
					s2.add(t, jc);
					countChanged++;
				} else {
					negContinue = false;
				}
			}

			if (!negContinue && !posContinue) {
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
