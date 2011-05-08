package local.radioschedulers.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.alg.ga.HeuristicsScheduleCollector;
import local.radioschedulers.alg.ga.ScheduleFitnessFunction;
import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.alg.ga.watchmaker.WFScheduler;
import local.radioschedulers.exporter.CsvExport;
import local.radioschedulers.exporter.HtmlExportWithFitness;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.importer.AtcaProposalReader;
import local.radioschedulers.importer.CsvScheduleReader;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.JsonProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

public class RunGA {
	private static int ndays = 365 / 2;
	private static final File outputDir = new File(".");
	private static final boolean WRITE_PROPOSALS = true;
	private static final boolean READ_SCHEDULES = true;
	private static final boolean WRITE_SCHEDULES = true;

	public static void main(String[] args) throws Exception {
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals) {
			System.out.println(p.toString());
		}
		if (WRITE_PROPOSALS) {
			JsonProposalReader json = new JsonProposalReader(new File(
					"proposals_testset_mopra.json"));
			json.write(proposals);
		}

		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		ScheduleSpace template = tlg.schedule(proposals, ndays);

		CsvScheduleReader csv = getScheduleReader(proposals);
		if (WRITE_SCHEDULES) {
			csv.write(template);
		}

		Map<String, Schedule> schedules = null;
		if (READ_SCHEDULES) {
			try {
				schedules = csv.readall();
			} catch (IOException e) {
			} catch (NullPointerException e) {
			}
		}
		if (schedules == null || schedules.isEmpty()) {
			Map<IScheduler, Schedule> schedules2 = HeuristicsScheduleCollector
					.getStartSchedules(template);

			schedules = new HashMap<String, Schedule>();
			for (Entry<IScheduler, Schedule> e : schedules2.entrySet()) {
				schedules.put(e.getKey().toString(), e.getValue());
			}
			if (WRITE_SCHEDULES) {
				csv.write(schedules);
			}
		}

		ScheduleFitnessFunction fitness = getFitnessFunction();
		SimpleScheduleFitnessFunction observatoryFitness = new SimpleScheduleFitnessFunction();
		observatoryFitness.setSwitchLostMinutes(0);
		SimpleScheduleFitnessFunction observerFitness = new SimpleScheduleFitnessFunction();
		observerFitness.setSwitchLostMinutes(60);

		GeneticAlgorithmScheduler scheduler = getScheduler(fitness);

		PrintWriter oq = new PrintWriter("quality.txt");
		int i = 1;
		File index = new File(outputDir, "index.html");
		PrintWriter o = new PrintWriter(index);
		o.println("<h2>Initial results</h2>");
		o.println("<ul>");
		for (Entry<String, Schedule> e : schedules.entrySet()) {
			Schedule s = e.getValue();
			String name = e.getKey().toString();
			File f = export(name, "schedule_" + i, s);
			o.println("\t<li><a href='" + f.getName() + "'>" + f.getName()
					+ " -- " + name + "</a></li>");
			oq.println(fitness.evaluate(s) + "\t"
					+ observatoryFitness.evaluate(s) + "\t"
					+ observerFitness.evaluate(s) + "\t" + name);
			System.out.println("Schedule of " + name + " in " + f);
			i++;
		}
		o.println("</ul>");
		o.flush();
		oq.flush();

		scheduler.setPopulation(new ArrayList<Schedule>(schedules.values()));
		scheduler.schedule(template);

		o.println("<h2>GA survivors</h2>");
		o.println("<strong><a href='schedule_" + i
				+ ".html'>&quot;Best&quot; schedule<a></strong>");

		o.println("<h3>GA survivors</h3>");
		o.println("<ul>");
		int j = 1;
		for (Schedule s : scheduler.getPopulation()) {
			String name = "GA survivor " + j;
			File f = export(name, "schedule_" + i, s);
			o.println("\t<li><a href='" + f.getName() + "'>" + f.getName()
					+ " -- " + name + "</a></li>");
			oq.println(fitness.evaluate(s) + "\t"
					+ observatoryFitness.evaluate(s) + "\t"
					+ observerFitness.evaluate(s) + "\t" + name);
			i++;
			j++;
		}
		o.println("</ul>");
		o.close();
		oq.close();
		System.out.println("Index of results at " + index);
	}

	private static CsvScheduleReader getScheduleReader(
			Collection<Proposal> proposals) {
		CsvScheduleReader csv = new CsvScheduleReader(new File(
				"schedules_mopra.csv"), new File("space_mopra.csv"), proposals);
		return csv;
	}

	private static File export(String title, String prefix, Schedule s)
			throws IOException {
		File f = new File(outputDir, prefix + ".html");
		IExport ex = new HtmlExportWithFitness(f, title, getFitnessFunction());
		ex.export(s);
		ex = new CsvExport(new File(outputDir, prefix + ".csv"));
		ex.export(s);
		return f;
	}

	private static GeneticAlgorithmScheduler getScheduler(
			ScheduleFitnessFunction f) {
		WFScheduler scheduler = new WFScheduler(f);
		scheduler.setPopulationSize(50);
		scheduler.setNumberOfGenerations(2000 / scheduler.getPopulationSize());
		scheduler.setEliteSize(1);

		scheduler.setCrossoverProbability(0.1);
		scheduler.setMutationProbability(0.08);

		scheduler.setDoubleCrossoverProbability(0.01);
		scheduler.setCrossoverDays(7);

		scheduler.setMutationKeepingProbability(0.01);
		scheduler.setMutationSimilarForwardsProbability(0.03);
		scheduler.setMutationSimilarBackwardsProbability(0.02);
		scheduler.setMutationSimilarPrevProbability(0.03);
		scheduler.setMutationExchangeProbability(0.03);
		scheduler.setMutationJobPlacementProbability(0.08);

		scheduler.setObserver(new EvolutionObserver<Schedule>() {

			@Override
			public void populationUpdate(PopulationData<? extends Schedule> data) {
				System.out.println("Gen. " + data.getGenerationNumber()
						+ ", pop.-qual. avg: " + data.getMeanFitness()
						+ " best: " + data.getBestCandidateFitness()
						+ " stdev: " + data.getFitnessStandardDeviation());
			}
		});

		return scheduler;
	}

	private static ScheduleFitnessFunction getFitnessFunction() {
		SimpleScheduleFitnessFunction f = new SimpleScheduleFitnessFunction();
		f.setSwitchLostMinutes(15);
		return f;
	}

	@SuppressWarnings("deprecation")
	private static IProposalReader getProposalReader() throws Exception {
		// SqliteProposalReader pr = new SqliteProposalReader();
		// PopulationGeneratingProposalReader pr = new
		// PopulationGeneratingProposalReader();
		// pr.fill(ndays * 4);
		AtcaProposalReader pr = new AtcaProposalReader(new File(
				"/home/user/Downloads/ata-proposals/mp-johannes.txt"),
				new Date(2011 - 1900, 1, 4), ndays);
		return pr;
	}

}
