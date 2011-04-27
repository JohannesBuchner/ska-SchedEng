package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;

import org.apache.log4j.Logger;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

public class WFScheduler extends GeneticAlgorithmScheduler {
	private static Logger log = Logger.getLogger(WFScheduler.class);

	private WFFitnessFunction fitness;
	private EvolutionObserver<Schedule> observer;
	private GeneticHistory<Schedule, String> history;
	private MutationCounter<Schedule, String> counter;

	private double mutationKeepingProbability = 0.;
	private double mutationSimilarPrevProbability = 0.;
	private double mutationSimilarBackwardsProbability = 0.;
	private double mutationSimilarForwardsProbability = 0.;
	private double mutationExchangeProbability = 0.;
	private double mutationJobPlacementProbability = 0.;
	private double doubleCrossoverProbability = 0.;
	private int crossoverDays = 0;

	public void setHistory(GeneticHistory<Schedule, String> history) {
		this.history = history;
	}

	public void setCounter(MutationCounter<Schedule, String> counter) {
		this.counter = counter;
	}

	public GeneticHistory<Schedule, String> getHistory() {
		return history;
	}

	public MutationCounter<Schedule, String> getCounter() {
		return counter;
	}

	public WFScheduler(ScheduleFitnessFunction f) {
		fitness = new WFFitnessFunction(f);
	}

	@Override
	protected List<Schedule> evolveSchedules(ScheduleSpace possibles,
			Collection<Schedule> ss) throws Exception {

		EvolutionaryOperator<Schedule> pipeline = getOperators(possibles);

		SelectionStrategy<Object> selection = new RouletteWheelSelection();
		// SelectionStrategy<Object> selection = new TournamentSelection(
		// new Probability(0.99));
		// SelectionStrategy<Object> selection = new TruncationSelection(0.5);

		Random rng = new MersenneTwisterRNG();

		ScheduleFactory factory = new ScheduleFactory(possibles);
		factory.setHistory(history);

		PopulationEvolutionEngine<Schedule> engine = new PopulationEvolutionEngine<Schedule>(
				factory, pipeline, fitness, selection, rng);

		if (observer != null)
			engine.addEvolutionObserver(observer);

		engine.addPopulationObserver(new PopulationObserver<Schedule>() {

			@Override
			public void updatePopulation(List<EvaluatedCandidate<Schedule>> pop) {
				if (history != null)
					history.retain(pop);
				if (counter != null)
					counter.retain(pop);
			}
		});

		log.debug("# of generations: " + getNumberOfGenerations());
		log.debug("# of chromosomes: " + getPopulationSize());

		List<EvaluatedCandidate<Schedule>> pop = engine.evolvePopulation(
				getPopulationSize(), getEliteSize(), ss, new GenerationCount(
						getNumberOfGenerations()));

		log.debug("# in final population: " + pop.size());

		List<Schedule> survivers = new ArrayList<Schedule>();
		for (EvaluatedCandidate<Schedule> chrome : pop) {
			survivers.add(chrome.getCandidate());
		}

		log.debug("# of survivors: " + survivers.size());
		return survivers;
	}

	private EvolutionaryOperator<Schedule> getOperators(ScheduleSpace possibles) {
		// Create a pipeline that applies cross-over then mutation.
		List<EvolutionaryOperator<Schedule>> operators = new LinkedList<EvolutionaryOperator<Schedule>>();
		// new StringMutation(null, new NumberGener);

		if (getCrossoverProbability() > 0) {
			ScheduleCrossover crossover = new ScheduleCrossover(1,
					new Probability(getCrossoverProbability()));
			crossover.setHistory(history);
			crossover.setCounter(counter);
			crossover.setSingleCrossover(true);
			operators.add(crossover);
		}
		if (getDoubleCrossoverProbability() > 0) {
			ScheduleCrossover crossover = new ScheduleCrossover(1,
					new Probability(getDoubleCrossoverProbability()));
			crossover.setHistory(history);
			crossover.setCounter(counter);
			crossover.setSingleCrossover(false);
			crossover.setMaxdays(getCrossoverDays());
			operators.add(crossover);
		}
		if (getMutationProbability() > 0) {
			ScheduleMutation mutation = new ScheduleMutation(possibles,
					new Probability(getMutationProbability()));
			mutation.setHistory(history);
			mutation.setCounter(counter);
			operators.add(mutation);
		}
		if (getMutationKeepingProbability() > 0) {
			ScheduleKeepingMutation mutation = new ScheduleKeepingMutation(
					possibles, new Probability(getMutationKeepingProbability()));
			mutation.setHistory(history);
			mutation.setCounter(counter);
			operators.add(mutation);
		}
		if (getMutationSimilarPrevProbability() > 0) {
			ScheduleSimilarPrevMutation mutation = new ScheduleSimilarPrevMutation(
					possibles, new Probability(
							getMutationSimilarPrevProbability()));
			mutation.setHistory(history);
			mutation.setCounter(counter);
			operators.add(mutation);
		}
		if (getMutationSimilarBackwardsProbability() > 0) {
			ScheduleSimilarMutation mutationSimilar = new ScheduleSimilarMutation(
					possibles, new Probability(
							getMutationSimilarBackwardsProbability()));
			mutationSimilar.setBackwardsKeep(true);
			mutationSimilar.setForwardsKeep(false);
			mutationSimilar.setHistory(history);
			mutationSimilar.setCounter(counter);
			operators.add(mutationSimilar);
		}
		if (getMutationSimilarForwardsProbability() > 0) {
			ScheduleSimilarMutation mutationSimilar = new ScheduleSimilarMutation(
					possibles, new Probability(
							getMutationSimilarForwardsProbability()));
			mutationSimilar.setBackwardsKeep(false);
			mutationSimilar.setForwardsKeep(true);
			mutationSimilar.setHistory(history);
			mutationSimilar.setCounter(counter);
			operators.add(mutationSimilar);
		}
		if (getMutationExchangeProbability() > 0) {
			ScheduleExchangeMutation mutationExchange = new ScheduleExchangeMutation(
					possibles,
					new Probability(getMutationExchangeProbability()));
			mutationExchange.setHistory(history);
			mutationExchange.setCounter(counter);
			operators.add(mutationExchange);
		}

		if (getMutationJobPlacementProbability() > 0) {
			ScheduleJobPlacementMutation mutPlace = new ScheduleJobPlacementMutation(
					possibles, new Probability(
							getMutationJobPlacementProbability()));
			mutPlace.setHistory(history);
			mutPlace.setCounter(counter);
			operators.add(mutPlace);
		}

		// operators.add(new StringExchangeMutation(new Probability(0.001)));
		EvolutionaryOperator<Schedule> pipeline = new EvolutionPipeline<Schedule>(
				operators);
		return pipeline;
	}

	private int getCrossoverDays() {
		return crossoverDays;
	}

	private double getDoubleCrossoverProbability() {
		return doubleCrossoverProbability;
	}

	private double getMutationSimilarPrevProbability() {
		return mutationSimilarPrevProbability;
	}

	public double getMutationExchangeProbability() {
		return mutationExchangeProbability;
	}

	public double getMutationKeepingProbability() {
		return mutationKeepingProbability;
	}

	public double getMutationSimilarBackwardsProbability() {
		return mutationSimilarBackwardsProbability;
	}

	public double getMutationSimilarForwardsProbability() {
		return mutationSimilarForwardsProbability;
	}

	public double getMutationJobPlacementProbability() {
		return mutationJobPlacementProbability;
	}

	public void setMutationKeepingProbability(double mutationKeepingProbability) {
		this.mutationKeepingProbability = mutationKeepingProbability;
	}

	public void setMutationSimilarBackwardsProbability(
			double mutationSimilarBackwardsProbability) {
		this.mutationSimilarBackwardsProbability = mutationSimilarBackwardsProbability;
	}

	public void setMutationSimilarForwardsProbability(
			double mutationSimilarForwardsProbability) {
		this.mutationSimilarForwardsProbability = mutationSimilarForwardsProbability;
	}

	public void setMutationSimilarPrevProbability(
			double mutationSimilarPrevProbability) {
		this.mutationSimilarPrevProbability = mutationSimilarPrevProbability;
	}

	public void setMutationExchangeProbability(
			double mutationExchangeProbability) {
		this.mutationExchangeProbability = mutationExchangeProbability;
	}

	public void setMutationJobPlacementProbability(
			double mutationJobPlacementProbability) {
		this.mutationJobPlacementProbability = mutationJobPlacementProbability;
	}

	public void setCrossoverDays(int crossoverDays) {
		this.crossoverDays = crossoverDays;
	}

	public void setDoubleCrossoverProbability(double doubleCrossoverProbability) {
		this.doubleCrossoverProbability = doubleCrossoverProbability;
	}

	public void setObserver(EvolutionObserver<Schedule> observer) {
		this.observer = observer;
	}

	public EvolutionObserver<Schedule> getObserver() {
		return observer;
	}

}