package local.radioschedulers.run.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.ResourceRequirement;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.alg.ga.HeuristicsScheduleCollector;
import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.alg.ga.watchmaker.GeneticHistory;
import local.radioschedulers.alg.ga.watchmaker.MutationCounter;
import local.radioschedulers.alg.ga.watchmaker.WFScheduler;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;

/**
 * TODO: add executor that does SCHED/FS execution
 * 
 * Working:
 * 
 * - Heuristics, GA
 * 
 * - reacting to resource availability (information from monitoring system)
 * 
 * - advising control system for execution
 * 
 * - taking past into account, and the fact that the past isn't open for
 * scheduling
 * 
 * 
 * @author Johannes Buchner
 * 
 */
public class ReusableScheduler {
	private static final String PREFERRED_HEURISTIC = "jobselector PrioritizedSelector";

	private static final boolean GA_ENABLED = false;

	private static Logger log = Logger.getLogger(ReusableScheduler.class);

	private ScheduleSpace template;
	private HashMap<String, Schedule> heuristicschedules;
	private Schedule bestSchedule;
	private Collection<Schedule> schedules;

	private int maxParallel = 4;
	private int numberOfEvaluations = 20;
	private int populationSize = 10;
	private double crossoverProb = 0.0;
	private double mutationProb = 0.0;
	private double mutationExchangeProb = 0.00;
	private double mutationSimilarProb = 0.3;

	private HashMap<String, Collection<String>> resources = new HashMap<String, Collection<String>>();

	private GeneticHistory<Schedule, String> history;

	private int ndays;

	private MutationCounter<Schedule, String> counter;

	public void setNdays(int ndays) {
		this.ndays = ndays;
	}

	public void setAvailableResources(String resType, Collection<String> items) {
		resources.put(resType, items);
	}

	public Schedule getCurrentSchedule() {
		return this.bestSchedule;
	}

	public void updateScheduleSpace(Collection<Proposal> proposals,
			LSTTime currentTime, Schedule previousSchedule) {
		log.debug("creating schedule space");
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				getRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		log.debug("created schedule space " + template.findLastEntry());

		for (Entry<LSTTime, Set<JobCombination>> e : template) {
			// remove choice from past
			if (currentTime != null && e.getKey().isBeforeOrEqual(currentTime)) {
				template.clear(e.getKey());
				JobCombination jc = previousSchedule.get(e.getKey());
				if (jc != null)
					template.add(e.getKey(), jc);
			}

			System.out.print(e.getKey() + " :: ");
			for (JobCombination jc : e.getValue()) {
				System.out.print(jc + " or ");
			}
			System.out.println();
		}
	}

	private RequirementGuard getRequirementGuard() {
		return new ParallelRequirementGuard(maxParallel) {
			@Override
			public boolean isDateCompatible(Job j, LSTTime date) {
				if (super.isDateCompatible(j, date)) {
					if (j instanceof JobWithResources) {
						JobWithResources jwr = (JobWithResources) j;
						boolean b = resourcesAvailable(jwr, j);
						if (!b)
							log.debug("@" + j + " - " + jwr
									+ " resources are NOT AVAILABLE.");
						return b;
					}
					return true;
				} else {
					return false;
				}
			}
		};
	}

	protected boolean resourcesAvailable(JobWithResources jwr, Job j) {
		return resourceReqFulfilled(jwr.resources.get("antennas"), resources
				.get("antennas"))
				&& resourceReqFulfilled(jwr.resources.get("backends"),
						resources.get("backends"));
	}

	protected boolean resourceReqFulfilled(ResourceRequirement req,
			Collection<String> collection) {
		int count = 0;
		for (Object e : req.possibles) {
			if (collection.contains(e)) {
				count++;
			}
		}
		return count >= req.numberrequired;
	}

	public void advanceSchedules() {
		log.debug("creating heuristic initial population");
		Map<IScheduler, Schedule> schedules2 = HeuristicsScheduleCollector
				.getStartSchedules(template);
		heuristicschedules = new HashMap<String, Schedule>();
		for (Entry<IScheduler, Schedule> e : schedules2.entrySet()) {
			heuristicschedules.put(e.getKey().toString(), e.getValue());
			if (e.getKey().toString().contains(PREFERRED_HEURISTIC)) {
				bestSchedule = e.getValue();
			}
		}
		log.debug("created heuristic initial population");

		schedules = heuristicschedules.values();
		if (GA_ENABLED) {
			advanceSchedulesWithGA();
		}
	}

	private void advanceSchedulesWithGA() {
		history = new GeneticHistory<Schedule, String>();
		counter = new MutationCounter<Schedule, String>();
		for (Entry<String, Schedule> e : heuristicschedules.entrySet()) {
			history.initiated(e.getValue(), e.getKey());
		}
		
		List<Schedule> population = new ArrayList<Schedule>(schedules);
		SimpleScheduleFitnessFunction f = new SimpleScheduleFitnessFunction();
		f.setSwitchLostMinutes(60);

		WFScheduler wfs = new WFScheduler(f);
		wfs.setHistory(history);
		wfs.setCounter(counter);
		GeneticAlgorithmScheduler scheduler = wfs;
		scheduler.setNumberOfGenerations(numberOfEvaluations / populationSize);
		scheduler.setEliteSize(1);
		scheduler.setCrossoverProbability(crossoverProb);
		scheduler.setMutationProbability(mutationProb);
		scheduler.setPopulationSize(populationSize);
		scheduler.setPopulation(population);
		wfs.setMutationExchangeProbability(mutationExchangeProb);
		wfs.setMutationSimilarBackwardsProbability(mutationSimilarProb);

		bestSchedule = scheduler.schedule(template);
		schedules = scheduler.getPopulation();
	}

}
