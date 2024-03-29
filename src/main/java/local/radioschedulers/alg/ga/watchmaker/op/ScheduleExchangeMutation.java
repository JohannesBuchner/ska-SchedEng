package local.radioschedulers.alg.ga.watchmaker.op;

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

import org.apache.log4j.Logger;
import org.uncommons.maths.random.Probability;

/**
 * selects random slot and tries to exchange with the same slot from a previous
 * day
 * 
 * @author Johannes Buchner
 */
public class ScheduleExchangeMutation extends ScheduleSimilarMutation {
	private static Logger log = Logger
			.getLogger(ScheduleExchangeMutation.class);

	public ScheduleExchangeMutation(ScheduleSpace possibles,
			Probability probability) {
		super(possibles, probability);
		setForwardsKeep(true);
		setBackwardsKeep(true);
	}

	@Override
	protected Schedule mutateSchedule(Schedule s1, Random rng) {
		Schedule s2 = new Schedule();
		int i = 0;
		int n = 0;
		long nextDay = 0;
		Probability u = new Probability(1 / 3. / 3);
		for (Entry<LSTTime, JobCombination> e : s1) {
			LSTTime t = e.getKey();
			if (e.getValue() == null)
				continue;
			s2.add(t, e.getValue());
			Set<JobCombination> jcs = possibles.get(t);
			if (t.day >= nextDay && !jcs.isEmpty() && u.nextEvent(rng)
					&& (mutationProbability.nextValue().nextEvent(rng))) {
				JobCombination jc = e.getValue();
				List<LSTTime> timeCandidates = new ArrayList<LSTTime>();
				if (t.day > 3)
					timeCandidates.add(new LSTTime(t.day - 3, t.minute));
				if (t.day > 2)
					timeCandidates.add(new LSTTime(t.day - 2, t.minute));
				if (t.day > 1)
					timeCandidates.add(new LSTTime(t.day - 1, t.minute));

				Collections.shuffle(timeCandidates, rng);
				// find a exchange partner
				boolean foundPartner = false;
				for (LSTTime t2 : timeCandidates) {
					JobCombination jc2 = s1.get(t2);
					Set<JobCombination> jcs2 = possibles.get(t2);
					/*
					 * partners should be exchangeable, but not the same, and
					 * not both null
					 */
					if ((jc == null || jcs2.contains(jc))
							&& (jc2 == null || jcs.contains(jc2))
							&& !(jc2 == null && jc == null)
							&& (jc == null || !jc.equals(jc2))) {

						if (log.isDebugEnabled())
							log.debug("exchanging @" + t + " " + jc + " and @"
									+ t2 + " " + jc2);
						// switch
						if (jc != null) {
							s2.add(t2, jc);
							i += 1 + makeSimilarAround(t2, jc, possibles, s2);
						}
						if (jc2 != null) {
							s2.add(t, jc2);
							i += 1 + makeSimilarAround(t, jc2, possibles, s2);
						}
						foundPartner = true;
						nextDay = t.day + 1;
						break;
					}
				}
				if (!foundPartner) {
					s2.add(t, e.getValue());
				}
			}
			n++;
		}
		updateHistory(s2, s1, i, n);
		updateCounters(s2, s1, i);

		return s2;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
