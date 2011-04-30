package local.radioschedulers.run;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.serial.SerialListingScheduler;
import local.radioschedulers.alg.serial.ShortestFirstSelector;
import local.radioschedulers.exporter.ExportFactory;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.PopulationGeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

public class RunMain {

	private static int ndays = 365;

	public static void main(String[] args) throws Exception {
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals)
			System.out.println(p.toString());
		IScheduler s = getScheduler();
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		ScheduleSpace template = tlg.schedule(proposals, ndays);
		Schedule schedule = s.schedule(template);

		display(schedule);
	}

	private static void display(Schedule schedule) {
		try {
			IExport ex = ExportFactory.getHtmlExport(new File("schedule.html"), "current schedule");
			ex.export(schedule);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static IScheduler getScheduler() {
		return new SerialListingScheduler(new ShortestFirstSelector());
		// return new LinearScheduler2();
	}

	private static IProposalReader getProposalReader() throws Exception {
		// SqliteProposalReader pr = new SqliteProposalReader();
		PopulationGeneratingProposalReader pr = new PopulationGeneratingProposalReader();
		pr.fill(ndays);
		return pr;
	}

}
