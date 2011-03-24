package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;
import org.uncommons.watchmaker.framework.CandidateFactory;

public class ScheduleFactory implements CandidateFactory<Schedule> {
	private ScheduleSpace possibles;
	private GeneticHistory<Schedule, String> history;
	private static Logger log = Logger.getLogger(ScheduleFactory.class);

	public void setHistory(GeneticHistory<Schedule, String> history) {
		this.history = history;
	}

	public ScheduleFactory(ScheduleSpace possibles) {
		this.possibles = possibles;
	}

	@Override
	public List<Schedule> generateInitialPopulation(int populationSize,
			Random rng) {
		return generateInitialPopulation(populationSize, null, rng);
	}

	@Override
	public List<Schedule> generateInitialPopulation(int populationSize,
			Collection<Schedule> seedCandidates, Random rng) {
		log.debug("got " + seedCandidates.size()
				+ " seed candidates, need to make " + populationSize);
		List<Schedule> list = new ArrayList<Schedule>();
		if (seedCandidates != null && !seedCandidates.isEmpty()) {
			while (list.size() < populationSize) {
				// log.debug("making copies of seed candidates");
				list.addAll(seedCandidates);
			}
		}
		while (list.size() < populationSize) {
			// log.debug("adding random seed candidate");
			list.add(generateRandomCandidate(rng));
		}
		return list;
	}

	@Override
	public Schedule generateRandomCandidate(Random rng) {
		Schedule s = new Schedule();
		for (Entry<LSTTime, Set<JobCombination>> e : possibles) {
			LSTTime t = e.getKey();
			Set<JobCombination> jcs = e.getValue();
			if (jcs.size() > 0) {
				JobCombination jc = (JobCombination) jcs.toArray()[rng
						.nextInt(jcs.size())];
				s.add(t, jc);
			}
		}
		if (history != null) {
			history.initiated(s, getClass().getSimpleName().toString());
		}
		return s;
	}

}
