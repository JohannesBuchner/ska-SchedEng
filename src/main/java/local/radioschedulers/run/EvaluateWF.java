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
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.watchmaker.GeneticHistory;
import local.radioschedulers.ga.watchmaker.WFScheduler;

import org.apache.log4j.Logger;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

public class EvaluateWF extends EvaluateGA {
	private static Logger log = Logger.getLogger(EvaluateWF.class);

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
		GeneticHistory<Schedule, String> history = new GeneticHistory<Schedule, String>();
		for (Entry<String, Schedule> e : schedules.entrySet()) {
			history.initiated(e.getValue(), e.getKey());
		}
		wfs.setHistory(history);
		GeneticAlgorithmScheduler scheduler = wfs;
		scheduler.setNumberOfGenerations(numberOfEvaluations / populationSize);
		scheduler.setEliteSize(2);
		scheduler.setCrossoverProbability(crossoverProb);
		scheduler.setMutationProbability(mutationProb);
		scheduler.setPopulationSize(populationSize);
		scheduler.setPopulation(population);
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

		printContributions(population, history);

		return population;
	}

	private void printContributions(List<Schedule> population,
			GeneticHistory<Schedule, String> history)
			throws FileNotFoundException {
		if (population == null)
			return;
		log.info("saving contributions and HTML export of schedules");
		PrintStream pcontr = getOutputFile("ga-contributions.txt");
		Map<String, Double> contributions = new HashMap<String, Double>();

		int i = 0;
		for (Schedule s : population) {
			String title = " -- ";
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
			HtmlExport ex = new HtmlExport(new File(prefix + "_export_" + i
					+ ".html"), title);
			try {
				ex.export(s);
			} catch (IOException e1) {
				log.error(e1);
			}
			i++;
		}
		for (Entry<String, Double> e : contributions.entrySet()) {
			pcontr.println(e.getKey() + " - " + e.getValue());
		}
		pcontr.close();
		log.info("saving contributions done");

	}
}
