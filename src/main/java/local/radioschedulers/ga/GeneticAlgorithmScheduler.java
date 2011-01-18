package local.radioschedulers.ga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.RequirementGuard;

public abstract class GeneticAlgorithmScheduler implements IScheduler {
	protected HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();
	protected RequirementGuard requirementGuard;
	protected int ndays;
	protected int ngenes;

	private double crossoverProbability = 0.1;
	private double mutationProbability = 0.3;
	private int populationSize = 100;
	private int eliteSize = 1;

	private List<Schedule> population = new ArrayList<Schedule>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(ScheduleSpace possibles) {
		this.ndays = possibles.findLastEntry().day.intValue();
		this.ngenes = ndays * ScheduleSpace.LST_SLOTS_PER_DAY;

		if (population == null)
			population = new ArrayList<Schedule>();
		
		Schedule bestschedule;
		try {
			population = evolveSchedules(possibles, population);
			bestschedule = population.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return bestschedule;
	}

	/**
	 * @return the full population from the last schedule() call
	 */
	public List<Schedule> getPopulation() {
		return population;
	}

	/**
	 * set the initial population
	 * 
	 * @param population
	 */
	public void setPopulation(List<Schedule> population) {
		this.population = population;
	}

	/**
	 * 
	 * @param possibles
	 *            space of possible schedules
	 * @param s
	 *            prior schedules to use
	 * @return
	 * @throws Exception
	 */
	protected abstract List<Schedule> evolveSchedules(ScheduleSpace possibles,
			Collection<Schedule> s) throws Exception;

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
