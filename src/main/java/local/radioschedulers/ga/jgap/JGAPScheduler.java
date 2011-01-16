package local.radioschedulers.ga.jgap;

import java.util.Collection;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
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
	protected Schedule evolveSchedules(Collection<Schedule> ss)
			throws Exception {
		Genotype population;
		// population = Genotype.randomInitialGenotype(conf);
		Population pop = new Population(conf);

		for (Schedule s : ss) {
			pop.addChromosome(getChromosomeFromSchedule(s));
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
	protected IChromosome getChromosomeFromSchedule(Schedule s)
			throws InvalidConfigurationException {
		Gene[] genes = new Gene[NDAYS];
		int i;
		for (i = 0; i < NDAYS * Schedule.LST_SLOTS_PER_DAY; i++) {
			LSTTime t = new LSTTime(i / Schedule.LST_SLOTS_PER_DAY, i
					% Schedule.LST_SLOTS_PER_DAY);

			SetGene g = new SetGene(conf);
			g.addAllele(s.get(t));
			genes[i] = g;
		}

		Chromosome c = new Chromosome(conf, genes);

		return c;
	}
}
