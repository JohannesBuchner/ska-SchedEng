package local.radioschedulers.run;

import java.io.PrintStream;
import java.util.List;

import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.watchmaker.WFScheduler;

import org.jgap.InvalidConfigurationException;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

public class EvaluateWF extends EvaluateGA {
	
	public static void main(String[] args) throws Exception {
		EvaluateGA ev = new EvaluateWF();
		ev.handleParams(args);
		ev.run();
	}
	
	@Override
	protected List<Schedule> evolveGA(final PrintStream ps, ScheduleSpace template,
			List<Schedule> population, ScheduleFitnessFunction f,
			final PrintStream p) throws InvalidConfigurationException {
		WFScheduler wfs = new WFScheduler(f);
		GeneticAlgorithmScheduler scheduler = wfs;
		scheduler.setNumberOfGenerations(numberOfEvaluations / populationSize);
		scheduler.setEliteSize(0);
		scheduler.setCrossoverProbability(crossoverProb);
		scheduler.setMutationProbability(mutationProb);
		scheduler.setPopulationSize(populationSize);
		scheduler.setPopulation(population);
		wfs.setObserver(new EvolutionObserver<Schedule>() {

			@Override
			public void populationUpdate(PopulationData<? extends Schedule> data) {
				// not doing this :(
				p.println(data.getGenerationNumber() + "\t"
						+ data.getBestCandidateFitness());
				p.println(data.getGenerationNumber()
						+ "\t"
						+ (data.getMeanFitness() + data
								.getFitnessStandardDeviation()));
				p.println(data.getGenerationNumber()
						+ "\t"
						+ (data.getMeanFitness() - 3 * data
								.getFitnessStandardDeviation()));
				// p.println(i + "\t" + v);

				System.out.println("Gen. " + data.getGenerationNumber()
						+ ", pop.-qual. avg: " + data.getMeanFitness()
						+ " best: " + data.getBestCandidateFitness()
						+ " stdev: " + data.getFitnessStandardDeviation());
				ps.println("Gen. " + data.getGenerationNumber()
						+ ", pop.-qual. avg: " + data.getMeanFitness()
						+ " best: " + data.getBestCandidateFitness()
						+ " stdev: " + data.getFitnessStandardDeviation());
			}
		});

		scheduler.schedule(template);

		population = scheduler.getPopulation();
		return population;

	}
}
