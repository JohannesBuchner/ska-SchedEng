package local.radioschedulers.run.demo;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;

import org.apache.log4j.Logger;

public abstract class RunControlAndScheduler {
	private static Logger log = Logger.getLogger(RunControlAndScheduler.class);

	private static int ndays = 1;

	public static void main(String[] args) {
		Collection<Proposal> proposals = new DemoProposalReader().readall();

		ReusableScheduler cas = new ReusableScheduler();
		cas.setNdays(ndays);

		ControlSystem cs = new PrintControlSystem();
		MonitoringSystem mon = new TextFileMonitoringSystem();
		ScheduleExport ex = new HtmlScheduleExport();

		writeAvailability();

		LSTTime currentTime = null;
		Schedule s = null;
		while (true) {
			cas.setAvailableResources("antennas", mon.getAvailableAntennas());
			cas.setAvailableResources("backends", mon.getAvailableBackends());

			cas.updateScheduleSpace(proposals, currentTime, s);

			log.info("advancing schedules ...");
			cas.advanceSchedules();
			s = cas.getCurrentSchedule();
			ex.export(s);
			log.info("executing ...");

			for (Entry<LSTTime, JobCombination> e : s) {
				LSTTime t = e.getKey();
				// on re-entry, skip forward
				if (currentTime != null && t.isBefore(currentTime)) {
					continue;
				}
				currentTime = t;
				log.info("@" + t + ": " + e.getValue());
				cs.execute(e.getValue());
				if (mon.haveResourcesChanged()) {
					log.debug("resources have changed");
					break;
				}
				if (t.minute == 4 * 60) {
					backendAvailable = false;
					writeAvailability();
				}
				if (t.minute == 5 * 60) {
					backendAvailable = true;
					writeAvailability();
				}

				if (t.minute > 10 * 60) {
					log.info("end of run at " + t);
					return;
				}
			}
		}

	}

	private static boolean backendAvailable = true;

	private static void writeAvailability() {
		try {
			PrintStream p = new PrintStream("available-backends.txt");
			if (backendAvailable)
				p.println("A");
			p.println("B");
			p.close();
		} catch (IOException e) {
			log.warn(e);
		}
	}

}
