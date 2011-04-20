package local.radioschedulers.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.exporter.ExportFactory;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.watchmaker.GeneticHistory;
import local.radioschedulers.ga.watchmaker.MutationCounter;
import local.radioschedulers.ga.watchmaker.WFScheduler;

import org.apache.log4j.Logger;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

public class EvaluateWF extends EvaluateGA {
	private static Logger log = Logger.getLogger(EvaluateWF.class);
	GeneticHistory<Schedule, String> history;
	MutationCounter<Schedule, String> counter;

	public static void main(String[] args) throws Exception {
		EvaluateGA ev = new EvaluateWF();
		ev.handleParams(args);
		ev.run();
	}

	@Override
	protected List<Schedule> evolveGA(final PrintStream ps,
			ScheduleSpace template, Map<String, Schedule> schedules,
			ScheduleFitnessFunction f, final PrintStream p) throws Exception {

		List<Schedule> population = new ArrayList<Schedule>(schedules.values());
		WFScheduler wfs = new WFScheduler(f);
		history = new GeneticHistory<Schedule, String>();
		counter = new MutationCounter<Schedule, String>();
		for (Entry<String, Schedule> e : schedules.entrySet()) {
			history.initiated(e.getValue(), e.getKey());
		}
		wfs.setHistory(history);
		wfs.setCounter(counter);
		GeneticAlgorithmScheduler scheduler = wfs;
		scheduler.setNumberOfGenerations(numberOfEvaluations / populationSize);
		scheduler.setEliteSize(1);
		scheduler.setCrossoverProbability(crossoverProb);
		scheduler.setMutationProbability(mutationProb);
		scheduler.setPopulationSize(populationSize);
		scheduler.setPopulation(population);
		wfs.setMutationExchangeProbability(mutationExchangeProb);
		wfs.setMutationSimilarForwardsProbability(mutationSimilarForwardsProb);
		wfs
				.setMutationSimilarBackwardsProbability(mutationSimilarBackwardsProb);
		wfs.setMutationKeepingProbability(mutationKeepingProb);
		wfs.setMutationSimilarPrevProbability(mutationSimilarPrevProb);
		wfs.setMutationJobPlacementProbability(mutationPlacementProb);

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

		printContributions(population, history, counter);

		compareInitialAndFinalPopulations(schedules, f, population);
		return population;
	}

	private void printContributions(List<Schedule> population,
			GeneticHistory<Schedule, String> history,
			MutationCounter<Schedule, String> counter)
			throws FileNotFoundException {
		if (population == null)
			return;
		log.info("saving contributions and HTML export of schedules");
		PrintStream pcontr = getOutputFile("ga-input-contributions.txt");
		PrintStream popcount = getOutputFile("ga-operator-count.best.txt");
		Map<String, Double> contributions = new HashMap<String, Double>();

		int i = 0;
		for (Schedule s : population) {
			String title = " -- ";
			{
				Map<String, Double> props = history.getProperties(s);
				if (props != null) {
					title = "";
					for (Entry<String, Double> e : props.entrySet()) {
						if (contributions.containsKey(e.getKey())) {
							contributions.put(e.getKey(), contributions.get(e
									.getKey())
									+ e.getValue());
						} else {
							contributions.put(e.getKey(), e.getValue());
						}
						title += " " + e.getKey() + " (" + e.getValue() + ")";
					}
				}
			}
			if (i == 0) {
				Map<String, Integer> opc = counter.getProperties(s);
				if (opc != null) {
					for (Entry<String, Integer> e : opc.entrySet()) {
						popcount.println(e.getKey() + "\t" + e.getValue());
					}
				}
			}
			if (i < populationSize / 2) {
				IExport ex = ExportFactory.getHtmlExport(new File(prefix
						+ "_export_" + i + ".html"), title);
				try {
					ex.export(s);
				} catch (IOException e1) {
					log.error(e1);
				}
			}
			i++;
		}
		for (Entry<String, Double> e : contributions.entrySet()) {
			pcontr.println(e.getKey() + "\t" + e.getValue());
		}
		pcontr.close();
		log.info("saving contributions done");
	}

	@Override
	protected void compareInitialAndFinalPopulations(
			Map<String, Schedule> schedules, ScheduleFitnessFunction f,
			List<Schedule> lastPopulation) throws FileNotFoundException {
		log.info("comparing input and output population ...");
		PrintStream p;
		p = getOutputFile("ga-final-population-similarity.json");
		p.println("{");
		p.println();
		p.println("\"initial\": {");
		for (Entry<String, Schedule> e : schedules.entrySet()) {
			String scheduler1 = e.getKey();
			Schedule s1 = e.getValue();
			double v = f.evaluate(s1);
			p.println("\t\"" + scheduler1 + "\":" + v + ",");
		}
		p.println("\t\"thats it\":3.14");
		p.println("},");
		p.println();
		p.println("\"final\":[");
		for (Schedule s : lastPopulation) {
			double v = f.evaluate(s);
			p.println("\t{");
			p.println("\t\t\"value\":" + v + ",");
			p.println("\t\t\"similarity\": {");

			for (Entry<String, Schedule> e : schedules.entrySet()) {
				String scheduler1 = e.getKey();
				Schedule s1 = e.getValue();
				double commonality = compareSchedules(s, s1);
				p.println("\t\t\t\"" + scheduler1 + "\":" + commonality + ",");
			}
			p.println("\t\t\t\"thats it\":3.14");
			p.println("\t\t},");
			p.println("\t\t\"history\": {");
			{
				Map<String, Double> props = history.getProperties(s);
				if (props != null) {
					for (Entry<String, Double> e : props.entrySet()) {
						p.println("\t\t\t\"" + e.getKey() + "\":"
								+ e.getValue() + ",");
					}
				}
			}
			p.println("\t\t\t\"thats it\":3.14");
			p.println("\t\t},");

			p.println("\t\t\"operators\": {");
			{
				Map<String, Integer> props = counter.getProperties(s);
				if (props != null) {
					for (Entry<String, Integer> e : props.entrySet()) {
						p.println("\t\t\t\"" + e.getKey() + "\":"
								+ e.getValue() + ",");
					}
				}
			}
			p.println("\t\t\t\"thats it\":3.14");
			p.println("\t\t}");
			p.println("\t},");
		}
		p.println("\t\"thats it\"");
		p.println("]}");
		log.info("comparing input and output population done");
	}
}
