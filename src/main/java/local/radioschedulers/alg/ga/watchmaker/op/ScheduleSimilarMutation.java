package local.radioschedulers.alg.ga.watchmaker.op;

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

	protected Probability u = new Probability(1. / 15.);
	protected boolean normalize = true;

	protected boolean normalizeProbability(Random rng) {
		if (normalize)
			return u.nextEvent(rng);
		else
			return true;
	}

	/* for tests */
	void disableNormalization() {
		normalize = false;
	}

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

		int toSkip = 0;
		// Probability u;
		// if (forwardsKeep) u = new Probability();
		boolean lastFailed = false;
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			JobCombination jc = e.getValue();
			Set<JobCombination> jcs = possibles.get(t);
			// can only make non-empty similar
			if (jc != null && !jcs.isEmpty() && s2.isEmpty(t)) {
				s2.add(t, jc);
				if (toSkip > 0) {
					toSkip--;
				} else {
					/* only if we have a change, we should consider it */
					if (lastFailed || normalizeProbability(rng)
							&& mutationProbability.nextValue().nextEvent(rng)) {
						if (log.isDebugEnabled())
							log.debug("mutating around " + t);
						toSkip = makeSimilarAround(t, jc, possibles, s2);
						i += toSkip;
						if (toSkip == 0) {
							lastFailed = true;
						} else {
							lastFailed = false;
						}
						// lastFailed = false;
						// if (forwardsKeep)
						// toSkip = 4 * toSkip + 16;
						// if (backwardsKeep)
						// toSkip = 4 * toSkip + 16;
					}
				}
				n++;
			}
		}
		if (log.isDebugEnabled())
			log.debug("changed " + i + " of " + n);
		updateHistory(s2, s1, i, n);
		updateCounters(s2, s1, i);

		return s2;
	}

	protected int makeSimilarAround(LSTTime t, JobCombination thisjc,
			ScheduleSpace template, Schedule s2) {
		boolean posContinue = forwardsKeep;
		boolean negContinue = backwardsKeep;
		int countChanged = 0;
		LSTTime last = template.findLastEntry();
		LSTTimeIterator it = new LSTTimeIterator(new LSTTime(1, 0),
				Schedule.LST_SLOTS_MINUTES);
		// skip t
		it.next();

		if (log.isDebugEnabled())
			log.debug("@" + t + " -- jc " + thisjc);
		while (it.hasNext() && (posContinue || negContinue)) {
			LSTTime tDelta = it.next();
			if (log.isDebugEnabled())
				log.debug("tDelta " + tDelta);

			if (posContinue) {
				LSTTime tPlus = LSTTime.add(t, tDelta);
				if (log.isDebugEnabled())
					log.debug("tPlus " + tPlus);

				if (tPlus.isAfter(last)) {
					posContinue = false;
				} else {
					Set<JobCombination> jcs = template.get(tPlus);
					if (!jcs.isEmpty()) {
						JobCombination jc = getMostSimilar(thisjc, jcs);
						if (jc != null) {
							if (log.isDebugEnabled())
								log.debug(" new jc " + jc);
							s2.add(tPlus, jc);
							// if (!jc.equals(thisjc))
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
				LSTTime tMinus = LSTTime.minus(t, tDelta);
				if (log.isDebugEnabled())
					log.debug("tMinus " + tMinus);

				if (tMinus.day < 0) {
					negContinue = false;
				} else {
					Set<JobCombination> jcs = template.get(tMinus);
					if (!jcs.isEmpty()) {
						JobCombination jc = getMostSimilar(thisjc, jcs);
						if (jc != null) {
							if (log.isDebugEnabled())
								log.debug(" new jc " + jc);
							s2.add(tMinus, jc);
							// if (!jc.equals(thisjc))
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + " forwards:" + this.forwardsKeep
				+ " backwards:" + this.backwardsKeep;
	}

}
