package local.radioschedulers.run;

import java.io.PrintStream;
import java.util.List;

import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.jgap.JGAPScheduler;

import org.jgap.InvalidConfigurationException;

public class EvaluateJGAP extends EvaluateGA {
	public static void main(String[] args) throws Exception {
		EvaluateGA ev = new EvaluateJGAP();
		ev.handleParams(args);
		ev.run();
	}
	
	@Override
	protected List<Schedule> evolveGA(final PrintStream ps, ScheduleSpace template,
			List<Schedule> population, ScheduleFitnessFunction f,
			final PrintStream p) throws InvalidConfigurationException {

			for (int i = 0; i < numberOfEvaluations / populationSize; i++) {
				GeneticAlgorithmScheduler scheduler = new JGAPScheduler(f);
				scheduler.setNumberOfGenerations(1);
				scheduler.setEliteSize(0);
				scheduler.setCrossoverProbability(crossoverProb);
				scheduler.setMutationProbability(mutationProb);
				scheduler.setPopulationSize(populationSize);
				scheduler.setPopulation(population);

				scheduler.schedule(template);

				population = scheduler.getPopulation();
				scheduler = null;

				double avg = 0;
				double best = Double.NaN;
				double worst = Double.NaN;
				for (Schedule s : population) {
					double v = f.evaluate(s);
					p.println(i + "\t" + v);
					avg += v;
					if (!(best > v))
						best = v;
					if (!(worst < v))
						worst = v;
				}
				avg /= population.size();
				System.out.println("Gen. " + i + ", pop.-qual. avg: " + avg
						+ " best: " + best + " worst: " + worst);
				ps.println("Gen. " + i + ", pop.-qual. avg: " + avg + " best: "
						+ best + " worst: " + worst);
			}
			return population;
	}
}
