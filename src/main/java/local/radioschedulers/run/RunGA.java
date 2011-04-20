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
import local.radioschedulers.exporter.CsvExport;
import local.radioschedulers.exporter.HtmlExportWithFitness;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.HeuristicsScheduleCollector;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.ga.watchmaker.WFScheduler;
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
	private static final File outputDir = new File("/tmp");

	public static void main(String[] args) throws Exception {
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals) {
			System.out.println(p.toString());
		}
		// JsonProposalReader json = new JsonProposalReader(new File(
		//		"proposals_testset_mopra.json"));
		// json.write(proposals);

		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		ScheduleSpace template = tlg.schedule(proposals, ndays);

		// CsvScheduleReader csv = getScheduleReader(proposals);
		// csv.write(template);

		Map<IScheduler, Schedule> schedules = HeuristicsScheduleCollector
				.getStartSchedules(template);

		Map<String, Schedule> schedules2 = new HashMap<String, Schedule>();
		for (Entry<IScheduler, Schedule> e : schedules.entrySet()) {
			schedules2.put(e.getKey().toString(), e.getValue());
		}
		// csv.write(schedules2);

		GeneticAlgorithmScheduler scheduler = getScheduler(getFitnessFunction());

		int i = 1;
		PrintWriter o = new PrintWriter(new File(outputDir, "index.html"));
		o.println("<h2>Initial results</h2>");
		o.println("<ul>");
		for (Entry<IScheduler, Schedule> e : schedules.entrySet()) {
			File f = export(e.getKey().toString(), "schedule_" + i, e
					.getValue());
			o.println("\t<li><a href='" + f.getName() + "'>" + f.getName()
					+ " -- " + e.getKey() + "</a></li>");
			System.out.println("Schedule of " + e.getKey() + " in " + f);
			i++;
		}
		o.println("</ul>");
		o.flush();

		scheduler.setPopulation(new ArrayList<Schedule>(schedules.values()));
		scheduler.schedule(template);

		o.println("<h2>GA survivors</h2>");
		o.println("<strong><a href='/tmp/schedule_" + i
				+ ".html'>&quot;Best&quot; schedule<a></strong>");

		o.println("<h3>GA survivors</h3>");
		o.println("<ul>");
		int j = 1;
		for (Schedule s : scheduler.getPopulation()) {
			File f = export("GA survivor " + j, "schedule_" + i, s);
			o.println("\t<li><a href='" + f.getName() + "'>" + f.getName()
					+ " -- " + "GA survivor " + j + "</a></li>");
			i++;
			j++;
		}
		o.println("</ul>");
		o.close();
		System.out.println("Index of results at /tmp/index.html");
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

		scheduler.setCrossoverProbability(0.2);
		scheduler.setMutationProbability(0.0);

		scheduler.setMutationKeepingProbability(0.1);
		scheduler.setMutationSimilarForwardsProbability(0.0);
		scheduler.setMutationSimilarBackwardsProbability(0.0);
		scheduler.setMutationSimilarPrevProbability(0.0);
		scheduler.setMutationExchangeProbability(0.0);
		scheduler.setMutationJobPlacementProbability(0.1);

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
