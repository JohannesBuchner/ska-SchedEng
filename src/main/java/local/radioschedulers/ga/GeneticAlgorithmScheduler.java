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
import local.radioschedulers.Proposal;
import local.radioschedulers.SchedulePossibilities;
import local.radioschedulers.SpecificSchedule;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.FairPrioritizedSelector;
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.cpu.ShortestFirstSelector;
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.lp.LinearScheduler2;
import local.radioschedulers.parallel.ParallelRequirementGuard;

public abstract class GeneticAlgorithmScheduler implements IScheduler {
	protected HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();
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
	public SpecificSchedule schedule(Collection<Proposal> proposals, int ndays) {
		this.ndays = ndays;
		this.ngenes = ndays * SchedulePossibilities.LST_SLOTS_PER_DAY;
		SchedulePossibilities possibles = getPossibleSchedules(proposals, ndays);
		Collection<SpecificSchedule> s = getStartSchedules(proposals);

		SpecificSchedule bestschedule;
		try {
			bestschedule = evolveSchedules(possibles, s);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return bestschedule;
	}

	protected SchedulePossibilities getPossibleSchedules(
			Collection<Proposal> proposals, int ndays2) {
		// TODO Auto-generated method stub
		return null;
	}

	protected abstract SpecificSchedule evolveSchedules(
			SchedulePossibilities possibles, Collection<SpecificSchedule> s)
			throws Exception;

	protected Collection<SpecificSchedule> getStartSchedules(
			Collection<Proposal> proposals) {
		List<SpecificSchedule> schedules = new ArrayList<SpecificSchedule>();

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

		schedulers.add(new LinearScheduler2());

		for (IScheduler s : schedulers) {
			log("scheduling using " + s);

			SpecificSchedule schedule = s.schedule(proposals, ndays);
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
