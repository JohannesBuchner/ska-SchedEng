package local.radioschedulers.exporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;

public class CsvExport implements IExport {
	private File f;

	public CsvExport(File f) {
		this.f = f;
	}

	public void export(Schedule schedule) throws IOException {
		PrintWriter p = new PrintWriter(f);
		for (Entry<LSTTime, JobCombination> e : schedule) {
			if (e.getValue() == null)
				continue;
			p.append(e.getKey().toString());
			p.append(";");
			for (Job j : e.getValue().jobs) {
				p.append(j.proposal.id);
				p.append(".");
				p.append(j.id);
				p.append(",");
			}
			p.append(";\n");
		}
		p.close();
	}
	
}
