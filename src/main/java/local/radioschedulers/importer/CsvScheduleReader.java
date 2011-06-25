package local.radioschedulers.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.exporter.CsvExport;

import org.apache.log4j.Logger;

public class CsvScheduleReader {

	private static Logger log = Logger.getLogger(CsvScheduleReader.class);

	private File spaceFile;

	private File schedulesFile;

	private Map<String, Job> jobmap = new HashMap<String, Job>();

	public CsvScheduleReader(File scheduleFile, File spaceFile,
			Collection<Proposal> proposals) {
		this.schedulesFile = scheduleFile;
		this.spaceFile = spaceFile;
		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				jobmap.put(p.id + "." + j.id, j);
			}
		}
	}

	private Job lookup(String jobid) {
		Job j = jobmap.get(jobid);
		if (j == null)
			log.warn("did not find associated job/proposal for jobid=" + jobid);
		return j;
	}

	public void write(ScheduleSpace space) throws Exception {
		PrintStream p = new PrintStream(spaceFile);
		for (Entry<LSTTime, Set<JobCombination>> e : space) {
			p.print(e.getKey());
			p.print(";");
			for (JobCombination jc : e.getValue()) {
				for (Job j : jc.jobs) {
					p.print(j.proposal.id + "." + j.id);
					p.print(",");
				}
				p.print(";");
			}
			p.println();
		}
		log.debug("wrote space to " + spaceFile);
	}

	public void write(Map<String, Schedule> schedules) throws Exception {
		schedulesFile.mkdir();
		for (Entry<String, Schedule> a : schedules.entrySet()) {
			CsvExport csv = new CsvExport(new File(schedulesFile, a.getKey()));
			csv.export(a.getValue());
		}
		log.debug("wrote " + schedules.size() + " to " + schedulesFile);
	}

	public Map<String, Schedule> readall() throws IOException {
		Map<String, Schedule> schedules = new HashMap<String, Schedule>();

		for (File f : schedulesFile.listFiles()) {
			if (f.isFile()) {
				schedules.put(f.getName(), read(f));
			}
		}
		return schedules;
	}

	public Schedule read(File f) throws FileNotFoundException, IOException {
		Schedule schedule = new Schedule();
		LineNumberReader r = new LineNumberReader(new FileReader(f));
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			String[] parts = line.split(";");
			LSTTime t = new LSTTime(parts[0]);
			for (int i = 1; i < parts.length; i++) {
				JobCombination jc = new JobCombination();
				String[] subparts = parts[i].split(",");
				for (String jobid : subparts) {
					jc.jobs.add(lookup(jobid));
				}
				schedule.add(t, jc);
			}
		}
		return schedule;
	}

	public ScheduleSpace readspace() throws IOException {
		ScheduleSpace space = new ScheduleSpace();
		LineNumberReader r = new LineNumberReader(new FileReader(spaceFile));
		while (true) {
			String line = r.readLine();
			if (line == null)
				break;
			String[] parts = line.split(";");
			LSTTime t = new LSTTime(parts[0]);
			for (int i = 1; i < parts.length; i++) {
				JobCombination jc = new JobCombination();
				String[] subparts = parts[i].split(",");
				for (String jobid : subparts) {
					jc.jobs.add(lookup(jobid));
				}
				space.add(t, jc);
			}
		}
		return space;
	}

}
