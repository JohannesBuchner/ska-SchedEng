package local.radioschedulers.ga.watchmaker;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;

/**
 * Operator that randomly selects blocks and tries to extend them backwards in
 * time as far as possible (maximum 1 day).
 * 
 * @author Johannes Buchner
 */
public class ScheduleSimilarMutation extends AbstractScheduleMutation {
	private boolean forwardsKeep = false;
	private boolean backwardsKeep = true;

	private static Logger log = Logger.getLogger(ScheduleSimilarMutation.class);

	public ScheduleSimilarMutation(ScheduleSpace possibles,
			Probability probability) {
		this(possibles, new ConstantGenerator<Probability>(probability));
	}

	public void setForwardsKeep(boolean forwardsKeep) {
		this.forwardsKeep = forwardsKeep;
	}

	public void setBackwardsKeep(boolean backwardsKeep) {
		this.backwardsKeep = backwardsKeep;
	}

	public ScheduleSimilarMutation(ScheduleSpace possibles,
			NumberGenerator<Probability> mutationProbability) {
		super(possibles, mutationProbability);
	}

	@Override
	protected Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;

		JobCombination lastJc = null;

		int toSkip = 0;
		for (Iterator<Entry<LSTTime, JobCombination>> it = s1.iterator(); it
				.hasNext();) {
			Entry<LSTTime, JobCombination> e = it.next();
			LSTTime t = e.getKey();
			JobCombination jc = e.getValue();
			Set<JobCombination> jcs = possibles.get(t);
			if (jc != null && !jcs.isEmpty()) {
				s2.add(t, jc);
				if (toSkip > 0) {
					toSkip--;
				} else {
					/* only if we have a change, we should consider it */
					if ((lastJc == null || !lastJc.equals(jc))
							&& (mutationProbability.nextValue().nextEvent(rng))) {
						log.debug("mutating around " + t);
						toSkip = makeSimilarAround(t, jc, possibles, s2);
						i += toSkip;
					}
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

	protected int makeSimilarAround(LSTTime t, JobCombination thisjc,
			ScheduleSpace template, Schedule s2) {
		boolean posContinue = forwardsKeep;
		boolean negContinue = backwardsKeep;
		int countChanged = 0;
		LSTTime last = template.findLastEntry();
		LSTTimeIterator it = new LSTTimeIterator(new LSTTime(0, 1),
				new LSTTime(1, 0), Schedule.LST_SLOTS_MINUTES);
		log.debug("jc " + thisjc);
		for (; it.hasNext();) {
			LSTTime tDelta = it.next();
			// log.debug("tDelta " + tDelta);

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

				if (tPlus.isAfter(last)) {
					posContinue = false;
				} else {
					Set<JobCombination> jcs = template.get(tPlus);
					if (!jcs.isEmpty()) {
						JobCombination jc = getMostSimilar(thisjc, jcs);
						if (jc != null) {
							log.debug("jc " + jc);
							s2.add(tPlus, jc);
							countChanged++;
						} else {
							posContinue = false;
						}
					} else {
						posContinue = false;
					}
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

				if (tMinus.day < 0) {
					negContinue = false;
				} else {
					Set<JobCombination> jcs = template.get(tMinus);
					if (!jcs.isEmpty()) {
						JobCombination jc = getMostSimilar(thisjc, jcs);
						if (jc != null) {
							log.debug("jc " + jc);
							s2.add(tMinus, jc);
							countChanged++;
						} else {
							negContinue = false;
						}
					} else {
						negContinue = false;
					}
				}
			}
		}
		return countChanged;
	}

}
