package local.radioschedulers.alg.ga.jgap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.alg.ga.ScheduleFitnessFunction;

import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.MutationOperator;

public class JGAPScheduler extends GeneticAlgorithmScheduler {
	// private static Logger log = Logger.getLogger(JGAPScheduler.class);

	private Configuration conf;
	private JGAPGeneScheduleConverter conv;
	private JGAPFitnessFunction fitness;

	public JGAPScheduler(ScheduleFitnessFunction f)
			throws InvalidConfigurationException {
		fitness = new JGAPFitnessFunction(f);
	}

	@Override
	protected List<Schedule> evolveSchedules(ScheduleSpace possibles,
			Collection<Schedule> ss) throws Exception {
		Configuration.reset();
		conf = new DefaultConfiguration();
		conf.setFitnessFunction(fitness);
		conv = new JGAPGeneScheduleConverter(conf);

		Genotype genotype;
		// population = Genotype.randomInitialGenotype(conf);
		Population pop = new Population(conf);

		IChromosome lastChromo = null;
		for (Schedule s : ss) {
			IChromosome chromo = conv.getChromosomeFromSpecificSchedule(
					possibles, s);
			pop.addChromosome(chromo);
			lastChromo = chromo;
		}
		if (ss.isEmpty()) {
			lastChromo = conv
					.getChromosomeFromSpecificSchedule(possibles, null);
			pop.addChromosome(lastChromo);
		}
		conf.setSampleChromosome(lastChromo);

		conf.setPopulationSize(getPopulationSize());

		addGeneticOperators();
		if (getEliteSize() > 0)
			conf.setPreservFittestIndividual(true);

		genotype = new Genotype(conf, pop);
		genotype.evolve(getNumberOfGenerations());
		List<Schedule> survivers = new ArrayList<Schedule>();
		for (Object chrome : genotype.getPopulation().getChromosomes()) {
			survivers.add(JGAPGeneScheduleConverter
					.getScheduleFromChromosome((IChromosome) chrome));
		}
		return survivers;
	}

	protected void addGeneticOperators() throws InvalidConfigurationException {
		conf.getGeneticOperators().clear();
		conf.addGeneticOperator(new CrossoverOperator(conf,
				getCrossoverProbability()));
		conf.addGeneticOperator(new MutationOperator(conf,
				(int) (1 / getMutationProbability())));
		// TODO: add more fancy operators
	}

}
