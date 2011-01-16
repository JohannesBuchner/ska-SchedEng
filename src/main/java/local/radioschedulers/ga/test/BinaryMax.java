package local.radioschedulers.ga.test;
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

public class BinaryMax extends FitnessFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8725140255467169312L;
	private static long evals = 0L;

	@Override
	protected double evaluate(IChromosome chrome) {
		int nup = 0;
		for (Gene g : chrome.getGenes()) {
			if ((Boolean) g.getAllele())
				nup++;
		}
		evals++;
		return nup;
	}

	public static void main(String[] args) throws InvalidConfigurationException {
		Configuration conf = new DefaultConfiguration();
		FitnessFunction myFunc = new BinaryMax();
		conf.setFitnessFunction(myFunc);

		Gene[] samplegenes = new Gene[8];
		for (int i = 0; i < 8; i++) {
			samplegenes[i] = new BooleanGene(conf, false);
		}
		Chromosome mother = new Chromosome(conf, samplegenes);
		conf.setSampleChromosome(mother);

		conf.setPopulationSize(2);

		Genotype population;
		// population = Genotype.randomInitialGenotype(conf);
		Population pop = new Population(conf, mother);
		population = new Genotype(conf, pop);

		int iter = 0;
		while (true) {

			IChromosome bestSolutionSoFar = population.getFittestChromosome();
			
			if(bestSolutionSoFar.getFitnessValue() == 8)
				break;
				
			System.out.print("Best: ");
			printChrosome(bestSolutionSoFar);
			for (Object c : population.getPopulation().getChromosomes())
				printChrosome((IChromosome) c);
			population.evolve();
			iter++;

			/*try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
		
		System.out.println("Pop: " + pop.size() + " Iterations: " + iter + " Evals: " + evals);
		
	}

	private static void printChrosome(IChromosome bestSolutionSoFar) {
		for (int i = 0; i < 8; i++) {
			if ((Boolean) bestSolutionSoFar.getGene(i).getAllele())
				System.out.print("1");
			else
				System.out.print("0");
		}
		System.out.println();
	}
}
