package local.radioschedulers.alg.ga.watchmaker;

import java.util.List;

import org.uncommons.watchmaker.framework.EvaluatedCandidate;

public interface PopulationObserver<T> {

	public abstract void updatePopulation(List<EvaluatedCandidate<T>> pop);

}
