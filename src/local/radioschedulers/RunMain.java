package local.radioschedulers;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.PopulationGeneratingProposalReader;
import local.radioschedulers.lp.LinearScheduler2;

public class RunMain {

	public static void main(String[] args) throws Exception {
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals)
			System.out.println(p.toString());
		IScheduler s = getScheduler();
		Schedule schedule = s.schedule(proposals);

		display(schedule);
	}

	private static void display(Schedule schedule) {
		try {
			IExport ex = new HtmlExport(new File("schedule.html"));
			ex.export(schedule);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static IScheduler getScheduler() {
		// return new RoundRobinScheduler();
		return new LinearScheduler2();
	}

	private static IProposalReader getProposalReader() throws Exception {
		//SqliteProposalReader pr = new SqliteProposalReader();
		PopulationGeneratingProposalReader pr = new PopulationGeneratingProposalReader();
		pr.fill();
		return pr;
	}

}
