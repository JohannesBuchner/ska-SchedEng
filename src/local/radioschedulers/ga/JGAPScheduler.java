package local.radioschedulers.ga;

import java.util.Collection;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.BooleanGene;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.SetGene;

public class JGAPScheduler extends GeneticAlgorithmScheduler {
	private static final int NDAYS = 90;
	private Configuration conf;

	public JGAPScheduler() throws InvalidConfigurationException {
		conf = new DefaultConfiguration();
		conf.setFitnessFunction(getFitnessFunction());
		conf.setPopulationSize(2);
	}

	private FitnessFunction getFitnessFunction() {
		return null;
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
		population.evolve(100);
		return getScheduleFromChromosome(population.getFittestChromosome());
	}

	private Schedule getScheduleFromChromosome(IChromosome c) {
		int i;
		Gene[] genes = c.getGenes();
		Schedule s = new Schedule();
		
		for (i = 0; i < genes.length; i++) {
			LSTTime t = new LSTTime(i / LST_SLOTS, i % LST_SLOTS);
			Job j = (Job)genes[i].getAllele();
			s.add(t, j);
		}
		
		return s;
	}

	private IChromosome getChromosomeFromSchedule(Schedule s) throws InvalidConfigurationException {
		Gene[] genes = new Gene[NDAYS];
		int i;
		for (i = 0; i < NDAYS * LST_SLOTS; i++) {
			LSTTime t = new LSTTime(i / LST_SLOTS, i % LST_SLOTS);

			SetGene g = new SetGene(conf);
			for (Job jc : s.get(t)) {
				g.addAllele(jc);
			}
			genes[i] = g;
		}

		Chromosome c = new Chromosome(conf, genes);

		return c;
	}
}
