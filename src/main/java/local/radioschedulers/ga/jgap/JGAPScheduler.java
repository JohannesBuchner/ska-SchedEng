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
import org.jgap.impl.DefaultConfiguration;
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
		Genotype population;
		// population = Genotype.randomInitialGenotype(conf);
		Population pop = new Population(conf);

		IChromosome template = getChromosomeFromSchedule(possibles);
		pop.addChromosome(template);
		for (SpecificSchedule s : ss) {
			pop.addChromosome(getChromosomeFromSpecificSchedule(template, s));
		}
		population = new Genotype(conf, pop);
		population.evolve(NUMBER_OF_EVOLUTIONS);
		return getScheduleFromChromosome(population.getFittestChromosome());
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
			IChromosome template, SpecificSchedule s)
			throws InvalidConfigurationException {
		Gene[] genes = new Gene[NDAYS * SchedulePossibilities.LST_SLOTS_PER_DAY];
		int i;
		for (i = 0; i < NDAYS * SchedulePossibilities.LST_SLOTS_PER_DAY; i++) {
			LSTTime t = new LSTTime(
					i / SchedulePossibilities.LST_SLOTS_PER_DAY, i
							% SchedulePossibilities.LST_SLOTS_PER_DAY);

			// SetGene g = new SetGene(conf);
			SetGene g = (SetGene) template.getGene(i);
			JobCombination jc = s.get(t);
			g.setAllele(jc);
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
