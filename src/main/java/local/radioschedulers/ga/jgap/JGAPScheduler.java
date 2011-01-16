package local.radioschedulers.ga.jgap;

import java.util.Collection;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.SchedulePossibilities;
import local.radioschedulers.SpecificSchedule;
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

	private static final int NDAYS = 90;

	private Configuration conf;

	public JGAPScheduler(ScheduleFitnessFunction f)
			throws InvalidConfigurationException {
		conf = new DefaultConfiguration();
		conf.setFitnessFunction(new JGAPFitnessFunction(f));
		conf.setPopulationSize(2);
	}

	@Override
	protected SpecificSchedule evolveSchedules(SchedulePossibilities possibles,
			Collection<SpecificSchedule> ss) throws Exception {
		Genotype genotype;
		// population = Genotype.randomInitialGenotype(conf);
		Population pop = new Population(conf);

		IChromosome template = getChromosomeFromSchedule(possibles);
		pop.addChromosome(template);
		for (SpecificSchedule s : ss) {
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
	public static SpecificSchedule getScheduleFromChromosome(IChromosome c) {
		int i;
		Gene[] genes = c.getGenes();
		SpecificSchedule s = new SpecificSchedule();

		for (i = 0; i < genes.length; i++) {
			LSTTime t = new LSTTime(i / SpecificSchedule.LST_SLOTS_PER_DAY, i
					% SpecificSchedule.LST_SLOTS_PER_DAY);
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
			SchedulePossibilities possibles, SpecificSchedule s)
			throws InvalidConfigurationException {
		Gene[] genes = new Gene[NDAYS * SchedulePossibilities.LST_SLOTS_PER_DAY];
		int i;
		for (i = 0; i < NDAYS * SchedulePossibilities.LST_SLOTS_PER_DAY; i++) {
			LSTTime t = new LSTTime(
					i / SchedulePossibilities.LST_SLOTS_PER_DAY, i
							% SchedulePossibilities.LST_SLOTS_PER_DAY);

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

	/**
	 * Build a Chromosome, where a Gene is a scheduling slot, and each possible
	 * Job(Combination) is a Allele.
	 * 
	 * @param s
	 * @return
	 * @throws InvalidConfigurationException
	 */
	protected IChromosome getChromosomeFromSchedule(SchedulePossibilities s)
			throws InvalidConfigurationException {
		Gene[] genes = new Gene[NDAYS * SchedulePossibilities.LST_SLOTS_PER_DAY];
		int i;
		for (i = 0; i < NDAYS * SchedulePossibilities.LST_SLOTS_PER_DAY; i++) {
			LSTTime t = new LSTTime(
					i / SchedulePossibilities.LST_SLOTS_PER_DAY, i
							% SchedulePossibilities.LST_SLOTS_PER_DAY);

			SetGene g = new SetGene(conf);
			for (JobCombination jc : s.get(t)) {
				g.addAllele(jc);
			}
			genes[i] = g;
		}

		Chromosome c = new Chromosome(conf, genes);

		return c;
	}
}
