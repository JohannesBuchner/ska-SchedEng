package local.radioschedulers.run;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.ga.jgap.JGAPScheduler;
import local.radioschedulers.importer.CsvScheduleReader;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.JsonProposalReader;

import org.apache.log4j.Logger;

public class EvaluateGA {
	private static int ndays = 365 / 4;
	private static double oversubscriptionFactor = 0.2;

	private static Logger log = Logger.getLogger(EvaluateGA.class);

	public static void main(String[] args) throws Exception {
		String prefix = "";
		if (args.length > 1)
			prefix = args[0];
		if (args.length > 2)
			oversubscriptionFactor = Double.parseDouble(args[1]);
		int maxParallel = 4;
		if (args.length > 3)
			maxParallel = Integer.parseInt(args[2]);
		int populationSize = 100;
		if (args.length > 4)
			populationSize = Integer.parseInt(args[3]);
		log.info("populationSize: " + populationSize);
		double crossoverProb = 0.3;
		if (args.length > 5)
			crossoverProb = Double.parseDouble(args[4]);
		double mutationProb = 0.3;
		if (args.length > 6)
			mutationProb = Double.parseDouble(args[5]);
		PrintStream ps = new PrintStream(prefix + "ga-settings.txt");
		ps.println("ndays: " + ndays);
		ps.println("oversubscriptionFactor: " + oversubscriptionFactor);
		ps.println("populationSize: " + populationSize);
		ps.println("mutationProb: " + mutationProb);
		ps.println("crossoverProb: " + crossoverProb);
		log.info("{ ndays: " + ndays + " }");
		log.info("{ oversubscriptionFactor: " + oversubscriptionFactor + " } ");
		log.info("{ populationSize: " + populationSize + " }");
		log.info("{ mutationProb: " + mutationProb + " }");
		log.info("{ crossoverProb: " + crossoverProb + " }");

		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();

		log.debug("loading schedule space");
		CsvScheduleReader sr = getScheduleReader(maxParallel, proposals);
		ScheduleSpace template = sr.readspace();
		log.debug("loaded schedule space");

		log.debug("loading heuristic initial population");
		Map<String, Schedule> schedules = sr.readall();
		log.debug("loaded heuristic initial population");

		ScheduleFitnessFunction f = getFitnessFunction();
		GeneticAlgorithmScheduler scheduler = new JGAPScheduler(f);
		scheduler.setNumberOfGenerations(1);
		scheduler.setEliteSize(0);
		scheduler.setCrossoverProbability(crossoverProb);
		scheduler.setMutationProbability(mutationProb);
		scheduler.setPopulationSize(populationSize);
		scheduler.setPopulation(new ArrayList<Schedule>(schedules.values()));

		PrintStream p = new PrintStream(prefix
				+ "ga-population-development.txt");
		for (int i = 0; i < 200 / scheduler.getPopulationSize(); i++) {
			scheduler.schedule(template);
			double avg = 0;
			double best = Double.NaN;
			double worst = Double.NaN;
			for (Schedule s : scheduler.getPopulation()) {
				double v = f.evaluate(s);
				p.println(i + "\t" + v);
				avg += v;
				if (!(best > v))
					best = v;
				if (!(worst < v))
					worst = v;
			}
			avg /= scheduler.getPopulation().size();
			System.out.println("Gen. " + i + ", pop.-qual. avg: " + avg
					+ " best: " + best + " worst: " + worst);
			ps.println("Gen. " + i + ", pop.-qual. avg: " + avg + " best: "
					+ best + " worst: " + worst);
		}
		p.close();

		p = new PrintStream(prefix + "ga-final-population-similarity.json");
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
		for (Schedule s : scheduler.getPopulation()) {
			double v = f.evaluate(s);
			p.println("\t{");
			p.println("\t\t\"value\":" + v + ",");
			p.println("\t\t\"schedulers\": {");

			for (Entry<String, Schedule> e : schedules.entrySet()) {
				String scheduler1 = e.getKey();
				Schedule s1 = e.getValue();
				double commonality = compareSchedules(s, s1);
				p.println("\t\t\t\"" + scheduler1 + "\":"
						+ commonality + ",");
			}
			p.println("\t\t\t\"thats it\":3.14");
			p.println("\t\t}");
			p.println("\t},");
		}
		p.println("\t\"thats it\"");
		p.println("]}");

	}

	private static double compareSchedules(Schedule s, Schedule s1) {
		double c = 0;
		int n = 0;

		for (Entry<LSTTime, JobCombination> e : s) {
			JobCombination jc1 = e.getValue();
			JobCombination jc2 = s1.get(e.getKey());
			if (jc1 == null && jc2 == null) {
				// both empty
				c++;
			} else if (jc1 != null && jc2 != null) {
				// same
				if (jc1.equals(jc2))
					c++;
				else {
					// give partial credit
					int subc = 0;
					for (Job j : jc2.jobs) {
						if (jc1.jobs.contains(j)) {
							subc++;
						}
					}
					c += subc * 1. / jc1.jobs.size();
				}
			}
			n++;
		}

		return c / n;
	}

	private static ScheduleFitnessFunction getFitnessFunction() {
		return new SimpleScheduleFitnessFunction();
	}

	private static IProposalReader getProposalReader() throws Exception {
		// SqliteProposalReader pr = new SqliteProposalReader();
		//PopulationGeneratingProposalReader pr = new PopulationGeneratingProposalReader();
		//pr.fill((int) (ndays * oversubscriptionFactor));
		JsonProposalReader pr = new JsonProposalReader(new File(
				"proposals_testset_ndays-" + ndays + "_oversubs-"
						+ oversubscriptionFactor + ".json"));
		return pr;
	}

	private static CsvScheduleReader getScheduleReader(int maxParallel,
			Collection<Proposal> proposals) {
		File schedulesFile = new File("schedule_testset_ndays-" + ndays
				+ "_oversubs-" + oversubscriptionFactor + "_parallel-"
				+ maxParallel + ".csv");
		File spaceFile = new File("space_testset_ndays-" + ndays
				+ "_oversubs-" + oversubscriptionFactor + "_parallel-"
				+ maxParallel + ".csv");

		CsvScheduleReader csv = new CsvScheduleReader(schedulesFile, spaceFile,
				proposals);
		return csv;
	}

}
