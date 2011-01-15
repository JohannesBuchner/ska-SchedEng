package local.radioschedulers.ga.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;

@SuppressWarnings("serial")
public class Deconv extends FitnessFunction {
	@Override
	protected double evaluate(IChromosome chrome) {
		double diff = 0;
		double supposedfunc[] = new double[N];
		int i = 0;
		//System.out.println("calcFitness: " + chrome);
		for (Gene g : chrome.getGenes()) {
			Double v = ((Double) g.getAllele());
			// System.out.println(i + " - " + v);
			supposedfunc[i] = v;
			i++;
		}
		for (i = 0; i < N; i++) {
			Double ours = 0.;
			for (int j = 0; j < N; j++) {
				int dist = j - i + N / 2;
				if (dist > N / 2 || dist < 0)
					continue;
				ours += supposedfunc[j] * leakfunc[dist];
			}
			diff += Math.pow(ours - obsfunc[i], 2);
		}
		diff = Math.sqrt(diff / N);

		System.out.println("diff: " + diff + " max: " + max);

		return max - diff;
	}

	public static void main(String[] args) throws InvalidConfigurationException, IOException {
		setupProblem();

		Configuration conf = new DefaultConfiguration();
		FitnessFunction myFunc = new Deconv();
		conf.setFitnessFunction(myFunc);

		Gene[] samplegenes = new Gene[N];
		for (int i = 0; i < N; i++) {
			DoubleGene dg = new DoubleGene(conf, 0., max);
			dg.setAllele(obsfunc[i]);
			samplegenes[i] = dg;
		}
		Chromosome mother = new Chromosome(conf, samplegenes);
		conf.setSampleChromosome(mother);

		conf.setPopulationSize(100);

		Genotype genotype;
		Population pop = new Population(conf, mother);
		genotype = new Genotype(conf, pop);

		runGA(genotype, pop);
		
		IChromosome bestSolution = genotype.getFittestChromosome();
		writeSolution(bestSolution);
	}

	private static final int N = 100;

	private static double truefunc[] = new double[N];
	private static double obsfunc[] = new double[N];
	private static double leakfunc[] = new double[N];

	private static double max = 0.;

	private static void setupProblem() {
		double domain = 30.;
		System.out.println("generating theoretical functions ...");
		for (int i = 0; i < N; i++) {
			double x = (i - N / 2) * domain / N;
			truefunc[i] = calcTrueFunc(x);
			leakfunc[i] = calcLeakFunc(x);
			obsfunc[i] = 0;
		}

		System.out.println("generating observation ... ");
		convolve(leakfunc, truefunc);
		max = sum(obsfunc);
		System.out.println("problem set up.");
	}

	private static double sum(double[] array) {
		double sum = 0;
		for (int i = 0; i < N; i++) {
			sum += array[i];
		}
		return sum;
	}

	private static void convolve(double leakfunc[], double truefunc[]) {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				int dist = j - i + N / 2;
				if (dist > N / 2 || dist < 0)
					continue;
				obsfunc[i] += truefunc[j] * leakfunc[dist];
			}
		}
	}

	private static double calcLeakFunc(double x) {
		return Math.exp(-Math.pow(x, 2));
	}

	private static double calcTrueFunc(double x) {
		double v = 1.;
		if (x != 0)
			v = Math.pow(Math.sin(x) / x, 2);
		if (x != 10)
			v = v + Math.pow(Math.sin(x - 10) / (x - 10), 2);
		if (x == 3)
			v = v + 0.3;
		return v;
	}

	private static void runGA(Genotype genotype, Population pop) {
		int iter = 0;
		
		while (iter < 200) {
			IChromosome bestSolutionSoFar = genotype.getFittestChromosome();

			if (bestSolutionSoFar.getFitnessValue() < 1)
				break;

			System.out.print("Best: ");
			printChrosome(bestSolutionSoFar);
			for (Object c : genotype.getPopulation().getChromosomes())
				printChrosome((IChromosome) c);
			genotype.evolve();
			iter++;

			/*
			 * try { System.in.read(); } catch (IOException e) {
			 * e.printStackTrace(); }
			 */
		}

		System.out.println("Pop: " + pop.size() + " Iterations: " + iter);
	}

	private static void writeSolution(IChromosome bestSolution)
			throws IOException {
		FileWriter fw = new FileWriter(new File("/tmp/gadeconv.txt"));
		for (int i = 0; i < N; i++) {
			Double v = (Double) bestSolution.getGene(i).getAllele();
			fw.append(truefunc[i] + "\t" + obsfunc[i] + "\t" + leakfunc[i] + "\t" + v + "\n");
		}
		fw.close();
	}

	private static void printChrosome(IChromosome bestSolutionSoFar) {
		Double sum = 0.;
		for (int i = 0; i < N; i++) {
			Double v = (Double) bestSolutionSoFar.getGene(i).getAllele();
			// System.out.print(v);
			// System.out.print(" ");
			sum += v;
		}
		System.out.println(sum);
	}
}
