package local.radioschedulers.run.demo;

import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
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
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ParallelizedHeuristicsScheduleCollector;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.ga.watchmaker.GeneticHistory;
import local.radioschedulers.ga.watchmaker.WFScheduler;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;

/**
 * TODO: refactor into classes, move to demo package
 * 
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
public class RunControlAndScheduler {
	private static Logger log = Logger.getLogger(RunControlAndScheduler.class);

	private static long lastReadTime;

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
	private double mutationSimilarProb = 0.00;

	private HashMap<String, Collection<String>> resources = new HashMap<String, Collection<String>>();

	private GeneticHistory<Schedule, String> history;

	private int ndays = 2;

	private static File htmlExportFile = new File("live-schedule.html");
	private static File antennasFile = new File("available-antennas.txt");
	private static File backendsFile = new File("available-backends.txt");

	public static void main(String[] args) {
		Collection<Proposal> proposals = getProposals();

		RunControlAndScheduler cas = new RunControlAndScheduler();

		LSTTime currentTime = null;
		Schedule s = null;
		while (true) {
			cas.setAvailableResources("antennas", getAvailableAntennas());
			cas.setAvailableResources("backends", getAvailableBackends());

			cas.updateScheduleSpace(proposals, currentTime, s);

			log.info("advancing schedules ...");
			cas.advanceSchedules();
			s = cas.getCurrentSchedule();
			try {
				HtmlExport ex = new HtmlExport(htmlExportFile);
				ex.export(s);
				Desktop d = Desktop.getDesktop();
				d.open(htmlExportFile);
			} catch (IOException e1) {
			}
			log.info("executing ...");

			for (Entry<LSTTime, JobCombination> e : s) {
				LSTTime t = e.getKey();
				// on re-entry, skip forward
				if (currentTime != null && t.isBefore(currentTime)) {
					continue;
				}
				currentTime = t;
				execute(e.getValue());
				if (haveResourcesChanged()) {
					log.debug("resources have changed");
					break;
				}
				if (t.minute > 20 * 60) {
					log.info("end of run at " + t);
					return;
				}
			}
		}

	}

	private static boolean haveResourcesChanged() {
		if (lastReadTime < backendsFile.lastModified()
				|| lastReadTime < antennasFile.lastModified()) {
			return true;
		}
		return false;
	}

	private void setAvailableResources(String resType, Collection<String> items) {
		resources.put(resType, items);
	}

	private static Collection<String> readLines(File f) {
		List<String> seq = new ArrayList<String>();
		try {
			LineNumberReader lnr = new LineNumberReader(new FileReader(f));
			while (true) {
				String line = lnr.readLine();
				if (line == null)
					break;
				seq.add(line);
				log.debug(f.getName() + " - " + line);
			}
		} catch (IOException e) {
			log.error("error reading file", e);
		}
		return seq;
	}

	private static Collection<String> getAvailableBackends() {
		lastReadTime = Math.max(lastReadTime, backendsFile.lastModified());
		return readLines(backendsFile);
	}

	private static Collection<String> getAvailableAntennas() {
		lastReadTime = Math.max(lastReadTime, antennasFile.lastModified());
		return readLines(antennasFile);
	}

	private static void execute(JobCombination jc) {
		if (jc == null) {
			System.out.println("idling ...");
		} else {
			// if this was serious, we would assign resources between jobs now.
			for (Job j : jc.jobs) {
				System.out.println("running Job " + j);
				JobWithResources jwr = (JobWithResources) j;
				System.out.println("\tbackends used:"
						+ jwr.resources.get("backends").numberrequired + " of "
						+ jwr.resources.get("backends").possibles);
				System.out.println("\tantennas used: "
						+ jwr.resources.get("antennas").numberrequired + " of "
						+ jwr.resources.get("antennas").possibles);
			}
		}
		try {
			Thread.sleep(1000 / 4);
		} catch (InterruptedException e) {
		}
	}

	private Schedule getCurrentSchedule() {
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
			if (currentTime != null && e.getKey().compareTo(currentTime) <= 0) {
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

		log.debug("creating heuristic initial population");
		Map<IScheduler, Schedule> schedules2 = ParallelizedHeuristicsScheduleCollector
				.getStartSchedules(template);
		heuristicschedules = new HashMap<String, Schedule>();
		for (Entry<IScheduler, Schedule> e : schedules2.entrySet()) {
			heuristicschedules.put(e.getKey().toString(), e.getValue());
		}
		log.debug("created heuristic initial population");
		history = new GeneticHistory<Schedule, String>();
		for (Entry<String, Schedule> e : heuristicschedules.entrySet()) {
			history.initiated(e.getValue(), e.getKey());
		}

		schedules = heuristicschedules.values();
		advanceSchedules();
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
		List<Schedule> population = new ArrayList<Schedule>(schedules);
		SimpleScheduleFitnessFunction f = new SimpleScheduleFitnessFunction();
		f.setSwitchLostMinutes(60);

		WFScheduler wfs = new WFScheduler(f);
		wfs.setHistory(history);
		GeneticAlgorithmScheduler scheduler = wfs;
		scheduler.setNumberOfGenerations(numberOfEvaluations / populationSize);
		scheduler.setEliteSize(2);
		scheduler.setCrossoverProbability(crossoverProb);
		scheduler.setMutationProbability(mutationProb);
		scheduler.setPopulationSize(populationSize);
		scheduler.setPopulation(population);
		wfs.setMutationExchangeProbability(mutationExchangeProb);
		wfs.setMutationSimilarProbability(mutationSimilarProb);

		bestSchedule = scheduler.schedule(template);
		schedules = scheduler.getPopulation();
	}

	private static Collection<Proposal> getProposals() {
		List<Proposal> proposals = new ArrayList<Proposal>();

		JobWithResources jwr = new JobWithResources();
		ResourceRequirement req = new ResourceRequirement();
		req.possibles.add("12m");
		req.numberrequired = 1;
		jwr.resources.put("antennas", req);
		req = new ResourceRequirement();
		req.possibles.add("A");
		req.possibles.add("B");
		req.numberrequired = 1;
		jwr.resources.put("backends", req);
		proposals.add(createSimpleProposal("Less Important", 1., 2., 20., 10L,
				jwr));

		jwr = new JobWithResources();
		req = new ResourceRequirement();
		req.possibles.add("12m");
		req.numberrequired = 1;
		jwr.resources.put("antennas", req);
		req = new ResourceRequirement();
		req.possibles.add("A");
		req.numberrequired = 1;
		jwr.resources.put("backends", req);
		proposals.add(createSimpleProposal("Very Important", 2., 1., 14., 6L,
				jwr));

		return proposals;
	}

	private static int id = 0;

	private static int getNextId() {
		return ++id;
	}

	private static String getNextIdAsString() {
		return Integer.toString(getNextId());
	}

	public static Proposal createSimpleProposal(String name, double prio,
			double startlst, double endlst, Long totalhours, Job j) {
		Proposal p = new Proposal();
		p.id = getNextIdAsString();
		p.name = name + " " + p.id;
		p.priority = prio;
		p.jobs = new ArrayList<Job>();
		j.hours = totalhours;
		j.lstmax = endlst;
		j.lstmin = startlst;
		j.id = p.name;
		j.proposal = p;
		p.jobs.add(j);
		return p;
	}

}
