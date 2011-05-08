package local.radioschedulers.run;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.lp.ParallelLinearScheduler;
import local.radioschedulers.exporter.ExportFactory;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.importer.CsvScheduleReader;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.JsonProposalReader;

import org.apache.log4j.Logger;

public class StoreLPSchedule {
	private static int ndays = 365 / 4;
	private static double oversubscriptionFactor = 0.2;
	private static Logger log = Logger.getLogger(StoreLPSchedule.class);
	private static File schedulesFile;
	private static File spaceFile;
	private static File schedulesHtmlFile;

	public static void main(String[] args) throws Exception {
		if (args.length >= 1)
			oversubscriptionFactor = Double.parseDouble(args[0]);
		int maxParallel = 4;
		if (args.length >= 2)
			maxParallel = Integer.parseInt(args[1]);
		schedulesFile = new File("schedule_testset_ndays-" + ndays
				+ "_oversubs-" + oversubscriptionFactor + "_parallel-"
				+ maxParallel + ".csv");
		schedulesHtmlFile = new File("schedule_testset_ndays-" + ndays
				+ "_oversubs-" + oversubscriptionFactor + "_parallel-"
				+ maxParallel + ".html");
		spaceFile = new File("space_testset_ndays-" + ndays + "_oversubs-"
				+ oversubscriptionFactor + "_parallel-" + maxParallel + ".csv");

		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		CsvScheduleReader csv = getScheduleReader(maxParallel, proposals);
		log.debug("reading schedule space");
		ScheduleSpace template = csv.readspace();
		log.debug("read schedule space");

		log.debug("creating linear solver solution");
		Map<String, Schedule> schedules = new HashMap<String, Schedule>();
		PrintStream executionTimeLog = new PrintStream(new FileOutputStream(
				"executiontime_lp_" + oversubscriptionFactor + "_"
						+ maxParallel + ".log", true));
		long start = System.currentTimeMillis();
		ParallelLinearScheduler scheduler = new ParallelLinearScheduler();
		Schedule s = scheduler.schedule(template);
		long duration = System.currentTimeMillis() - start;
		executionTimeLog
				.println(duration / 1000. + "\t" + scheduler.toString());
		executionTimeLog.flush();
		schedules.put(scheduler.toString(), s);
		log.debug("created linear solver solution");
		csv.write(schedules);

		schedulesHtmlFile.mkdir();
		for (Entry<String, Schedule> e : schedules.entrySet()) {
			IExport ex = ExportFactory.getHtmlExport(new File(
					schedulesHtmlFile, e.getKey() + ".html"), e.getKey());
			ex.export(e.getValue());
		}
	}

	private static CsvScheduleReader getScheduleReader(int maxParallel,
			Collection<Proposal> proposals) {

		CsvScheduleReader csv = new CsvScheduleReader(schedulesFile, spaceFile,
				proposals);
		return csv;
	}

	private static IProposalReader getProposalReader() throws Exception {
		// SqliteProposalReader pr = new SqliteProposalReader();
		// PopulationGeneratingProposalReader pr = new
		// PopulationGeneratingProposalReader();
		// pr.fill((int) (ndays * oversubscriptionFactor));
		JsonProposalReader pr = new JsonProposalReader(new File(
				"proposals_testset_ndays-" + ndays + "_oversubs-"
						+ oversubscriptionFactor + ".json"));
		return pr;
	}

}
