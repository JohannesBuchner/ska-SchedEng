package local.radioschedulers.ga.jgap;

import java.util.Collection;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.Schedule;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.MutationOperator;
import org.jgap.impl.SetGene;

public class JGAPScheduler extends GeneticAlgorithmScheduler {

	private int NUMBER_OF_EVOLUTIONS = 100;

	private Configuration conf;

	public JGAPScheduler(ScheduleFitnessFunction f)
			throws InvalidConfigurationException {
		conf = new DefaultConfiguration();
		conf.setFitnessFunction(new JGAPFitnessFunction(f));
		conf.setPopulationSize(2);
	}

	@Override
	protected Schedule evolveSchedules(ScheduleSpace possibles,
			Collection<Schedule> ss) throws Exception {
		Genotype genotype;
		// population = Genotype.randomInitialGenotype(conf);
		Population pop = new Population(conf);

		for (Schedule s : ss) {
			pop.addChromosome(getChromosomeFromSpecificSchedule(possibles, s));
		}

		conf.setPopulationSize(getPopulationSize());

		addGeneticOperators();
		if (getEliteSize() > 0)
			conf.setPreservFittestIndividual(true);

		// conf.setSampleChromosome(a_sampleChromosomeToSet);
		genotype = new Genotype(conf, pop);
		genotype.evolve(NUMBER_OF_EVOLUTIONS);
		return getScheduleFromChromosome(genotype.getFittestChromosome());
	}

	protected void addGeneticOperators() throws InvalidConfigurationException {
		conf.getGeneticOperators().clear();
		conf.addGeneticOperator(new CrossoverOperator(conf,
				getCrossoverProbability()));
		conf.addGeneticOperator(new MutationOperator(conf,
				(int) (1 / getMutationProbability())));
		// TODO: add more fancy operators
	}

	/**
	 * Converts a Chromosome back to a Schedule
	 * 
	 * @param c
	 * @return
	 */
	public static Schedule getScheduleFromChromosome(IChromosome c) {
		int i;
		Gene[] genes = c.getGenes();
		Schedule s = new Schedule();

		for (i = 0; i < genes.length; i++) {
			LSTTime t = new LSTTime(i / Schedule.LST_SLOTS_PER_DAY, i
					% Schedule.LST_SLOTS_PER_DAY);
			JobCombination jc = (JobCombination) genes[i].getAllele();
			s.add(t, jc);
		}

		return s;
	}

	/**
	 * Build a Chromosome, where a Gene is a scheduling slot, and each possible
	 * Job(Combination) is a Allele.
	 * 
	 * @param s
	 * @return
	 * @throws InvalidConfigurationException
	 */
	protected IChromosome getChromosomeFromSpecificSchedule(
			ScheduleSpace possibles, Schedule s)
			throws InvalidConfigurationException {
		Gene[] genes = new Gene[ngenes];
		for (int i = 0; i < ngenes; i++) {
			LSTTime t = new LSTTime(i / ngenes, i % ngenes);

			SetGene g = new SetGene(conf);
			// add all possible jobs, and select the one specified
			for (JobCombination jc : possibles.get(t)) {
				g.addAllele(jc);

				if (jc.equals(s.get(t)))
					g.setAllele(jc);
			}
			genes[i] = g;
		}

		Chromosome c = new Chromosome(conf, genes);
		return c;
	}
}
