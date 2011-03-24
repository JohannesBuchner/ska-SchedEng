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

	private double mutationSimilarProbability = 0.;
	private double mutationExchangeProbability = 0.;

	public void setMutationExchangeProbability(
			double mutationExchangeProbability) {
		this.mutationExchangeProbability = mutationExchangeProbability;
	}

	public void setMutationSimilarProbability(double mutationSimilarProbability) {
		this.mutationSimilarProbability = mutationSimilarProbability;
	}

	public void setHistory(GeneticHistory<Schedule, String> history) {
		this.history = history;
	}

	public GeneticHistory<Schedule, String> getHistory() {
		return history;
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

		{
			ScheduleCrossover crossover = new ScheduleCrossover(1,
					new Probability(getCrossoverProbability()));
			crossover.setHistory(history);
			operators.add(crossover);
		}
		{
			ScheduleMutation mutation = new ScheduleMutation(possibles,
					new Probability(getMutationProbability()));
			mutation.setHistory(history);
			operators.add(mutation);
		}
		{
			ScheduleSimilarMutation mutationSimilar = new ScheduleSimilarMutation(
					possibles, new Probability(getMutationSimilarProbability()));
			mutationSimilar.setHistory(history);
			operators.add(mutationSimilar);
		}
		{
			ScheduleExchangeMutation mutationExchange = new ScheduleExchangeMutation(
					possibles,
					new Probability(getMutationExchangeProbability()));
			mutationExchange.setHistory(history);
			operators.add(mutationExchange);
		}

		// operators.add(new StringExchangeMutation(new Probability(0.001)));
		EvolutionaryOperator<Schedule> pipeline = new EvolutionPipeline<Schedule>(
				operators);
		return pipeline;
	}

	public double getMutationExchangeProbability() {
		return mutationExchangeProbability;
	}

	public double getMutationSimilarProbability() {
		return mutationSimilarProbability;
	}

	public void setObserver(EvolutionObserver<Schedule> observer) {
		this.observer = observer;
	}

	public EvolutionObserver<Schedule> getObserver() {
		return observer;
	}

}