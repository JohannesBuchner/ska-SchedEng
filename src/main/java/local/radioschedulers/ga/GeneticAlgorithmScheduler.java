package local.radioschedulers.ga;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.Schedule;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.FairPrioritizedSelector;
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.cpu.ShortestFirstSelector;
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.lp.ParallelLinearScheduler;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

public abstract class GeneticAlgorithmScheduler implements IScheduler {
	protected HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();
	protected RequirementGuard requirementGuard;
	protected int ndays;
	protected int ngenes;

	private double crossoverProbability = 0.1;
	private double mutationProbability = 0.3;
	private int populationSize = 100;
	private int eliteSize = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(ScheduleSpace possibles) {
		this.ndays = possibles.getLastEntry().day.intValue();
		this.ngenes = ndays * ScheduleSpace.LST_SLOTS_PER_DAY;
		Collection<Schedule> s = getStartSchedules(possibles);

		Schedule bestschedule;
		try {
			bestschedule = evolveSchedules(possibles, s);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return bestschedule;
	}

	protected abstract Schedule evolveSchedules(
			ScheduleSpace possibles, Collection<Schedule> s)
			throws Exception;

	protected Collection<Schedule> getStartSchedules(
			ScheduleSpace timeline) {
		List<Schedule> schedules = new ArrayList<Schedule>();

		List<IScheduler> schedulers = new ArrayList<IScheduler>();

		schedulers.add(new CPULikeScheduler(new FairPrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new PrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new ShortestFirstSelector(),
				new ParallelRequirementGuard()));

		CPULikeScheduler rand = new CPULikeScheduler(new RandomizedSelector(),
				new ParallelRequirementGuard());
		schedulers.add(rand);
		schedulers.add(rand);
		schedulers.add(rand);
		schedulers.add(rand);

		schedulers.add(new ParallelLinearScheduler());

		for (IScheduler s : schedulers) {
			log("scheduling using " + s);

			Schedule schedule = s.schedule(timeline);
			log("scheduling done");
			schedules.add(schedule);
			File f = new File("schedule" + schedules.size() + ".html");
			HtmlExport ex = new HtmlExport(f, s.toString());
			try {
				ex.export(schedule);

				Desktop d = Desktop.getDesktop();
				d.open(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return schedules;
	}

	private void log(String string) {
		System.out.println("DEBUG " + string);
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public void setEliteSize(int eliteSize) {
		this.eliteSize = eliteSize;
	}

	public int getEliteSize() {
		return eliteSize;
	}
}
