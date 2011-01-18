package local.radioschedulers.ga;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.FairPrioritizedSelector;
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.cpu.ShortestFirstSelector;
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.lp.ParallelLinearScheduler;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;

public class HeuristicsScheduleCollector {
	
	private static Logger log = Logger
			.getLogger(HeuristicsScheduleCollector.class);

	public static List<Schedule> getStartSchedules(ScheduleSpace timeline) {
		List<Schedule> schedules = new ArrayList<Schedule>();

		List<IScheduler> schedulers = new ArrayList<IScheduler>();

		schedulers.add(new CPULikeScheduler(new FairPrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new PrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new ShortestFirstSelector(),
				new ParallelRequirementGuard()));

		CPULikeScheduler rand = new CPULikeScheduler(new RandomizedSelector(),
				new ParallelRequirementGuard());
		schedulers.add(rand);
		schedulers.add(rand);
		schedulers.add(rand);
		schedulers.add(rand);

		schedulers.add(new ParallelLinearScheduler());

		for (IScheduler s : schedulers) {
			log.debug("scheduling using " + s);

			Schedule schedule = s.schedule(timeline);
			log.debug("scheduling done");
			schedules.add(schedule);
			File f = new File("schedule" + schedules.size() + ".html");
			HtmlExport ex = new HtmlExport(f, s.toString());
			try {
				ex.export(schedule);

				Desktop d = Desktop.getDesktop();
				d.open(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return schedules;
	}

}
