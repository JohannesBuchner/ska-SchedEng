package local.radioschedulers.ga.watchmaker;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.interactive.InteractiveSelection;

public class PopulationEvolutionEngine<T> extends
		GenerationalEvolutionEngine<T> {
    private final Set<PopulationObserver<T>> observers = new CopyOnWriteArraySet<PopulationObserver<T>>();

	public PopulationEvolutionEngine(CandidateFactory<T> candidateFactory,
			EvolutionaryOperator<T> evolutionScheme,
			InteractiveSelection<T> selectionStrategy, Random rng) {
		super(candidateFactory, evolutionScheme, selectionStrategy, rng);
	}

	public PopulationEvolutionEngine(CandidateFactory<T> candidateFactory,
			EvolutionaryOperator<T> evolutionScheme,
			FitnessEvaluator<? super T> fitnessEvaluator,
			SelectionStrategy<? super T> selectionStrategy, Random rng) {
		super(candidateFactory, evolutionScheme, fitnessEvaluator,
				selectionStrategy, rng);
	}

	public void addPopulationObserver(PopulationObserver<T> o) {
		observers.add(o);
	}
	
	@Override
	protected List<EvaluatedCandidate<T>> nextEvolutionStep(
			List<EvaluatedCandidate<T>> evaluatedPopulation, int eliteCount,
			Random rng) {
		List<EvaluatedCandidate<T>> pop = super.nextEvolutionStep(evaluatedPopulation, eliteCount, rng);
		for (PopulationObserver<T> o : this.observers) {
			o.updatePopulation(pop);
		}
		return pop;
	}
}
