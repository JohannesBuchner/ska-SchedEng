package local.radioschedulers.run;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.HeuristicsScheduleCollector;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.ga.watchmaker.WFScheduler;
import local.radioschedulers.importer.AtcaProposalReader;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

public class RunGA {
	private static int ndays = 365 / 2;
	private static final File outputDir = new File("/tmp");

	public static void main(String[] args) throws Exception {
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals) {
			System.out.println(p.toString());
		}
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		ScheduleSpace template = tlg.schedule(proposals, ndays);
		GeneticAlgorithmScheduler scheduler = getScheduler(getFitnessFunction());

		Map<IScheduler, Schedule> schedules = HeuristicsScheduleCollector
				.getStartSchedules(template);
		int i = 1;
		PrintWriter o = new PrintWriter(new File(outputDir, "schedules.html"));
		o.println("<h2>Initial results</h2>");
		o.println("<ul>");
		for (Entry<IScheduler, Schedule> e : schedules.entrySet()) {
			File f = new File(outputDir, "schedule_" + i + ".html");
			HtmlExport ex = new HtmlExport(f, e.getKey().toString());
			ex.export(e.getValue());
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
		for (Schedule e : scheduler.getPopulation()) {
			File f = new File(outputDir, "schedule_" + i + ".html");
			HtmlExport ex = new HtmlExport(f, "GA survivor " + j);
			ex.export(e);
			o.println("\t<li><a href='" + f.getName() + "'>" + f.getName()
					+ " -- " + "GA survivor " + j + "</a></li>");
			i++;
			j++;
		}
		o.println("</ul>");
		o.close();
		System.out.println("Index of results at /tmp/schedules.html");
	}

	private static GeneticAlgorithmScheduler getScheduler(
			ScheduleFitnessFunction f) {
		WFScheduler scheduler = new WFScheduler(f);
		scheduler.setPopulationSize(30);
		scheduler.setNumberOfGenerations(10000 / scheduler.getPopulationSize());
		scheduler.setEliteSize(2);

		scheduler.setCrossoverProbability(0.2);
		scheduler.setMutationProbability(0.);

		scheduler.setMutationKeepingProbability(0.2);
		scheduler.setMutationSimilarForwardsProbability(0.1);
		scheduler.setMutationSimilarBackwardsProbability(0.15);
		scheduler.setMutationSimilarPrevProbability(0.1);
		scheduler.setMutationExchangeProbability(0.);
		return scheduler;
	}

	private static ScheduleFitnessFunction getFitnessFunction() {
		return new SimpleScheduleFitnessFunction();
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
