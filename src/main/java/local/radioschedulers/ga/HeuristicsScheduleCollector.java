package local.radioschedulers.ga;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.FairPrioritizedSelector;
import local.radioschedulers.cpu.KeepingPrioritizedSelector;
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.cpu.ShortestFirstSelector;
import local.radioschedulers.exporter.ExportFactory;
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.greedy.GreedyPlacementScheduler;
import local.radioschedulers.greedy.GreedyScheduler;
import local.radioschedulers.greedy.PressureJobSortCriterion;
import local.radioschedulers.greedy.PriorityJobSortCriterion;
import local.radioschedulers.lp.ParallelLinearScheduler;

import org.apache.log4j.Logger;

public class HeuristicsScheduleCollector {

	public static boolean PARALLEL_EXECUTION = false;
	public static boolean SHOW_SCHEDULE = false;

	private static Logger log = Logger
			.getLogger(HeuristicsScheduleCollector.class);

	public static Map<IScheduler, Schedule> getStartSchedules(
			ScheduleSpace timeline) {
		if (PARALLEL_EXECUTION) {
			return getStartSchedulesParallel(timeline);
		} else {
			return getStartSchedulesSingle(timeline);
		}
	}

	private static Map<IScheduler, Schedule> getStartSchedulesSingle(
			ScheduleSpace timeline) {
		final Map<IScheduler, Schedule> schedules = new HashMap<IScheduler, Schedule>();

		List<IScheduler> schedulers = getSchedulers();

		try {
			PrintStream executionTimeLog = new PrintStream(
					new FileOutputStream("executiontime.log", true));

			for (IScheduler s : schedulers) {
				log.debug("scheduling using " + s);

				long start = System.currentTimeMillis();
				Schedule schedule = s.schedule(timeline);
				long duration = System.currentTimeMillis() - start;
				log.debug("scheduling "
						+ (schedule == null ? "FAILED" : "done"));
				executionTimeLog.println(duration + "\t" + s.toString());
				executionTimeLog.flush();

				schedules.put(s, schedule);
				exportSchedule(schedules, s, schedule);
			}
		} catch (IOException e) {
		}

		return schedules;
	}

	private static void exportSchedule(
			final Map<IScheduler, Schedule> schedules, IScheduler s,
			Schedule schedule) {
		if (!SHOW_SCHEDULE)
			return;
		File f = new File("schedule" + schedules.size() + ".html");
		IExport ex = ExportFactory.getHtmlExport(f, s.toString());
		try {
			ex.export(schedule);

			Desktop d = Desktop.getDesktop();
			d.open(f);
		} catch (IOException e) {
			log.warn(e);
		}
	}

	private static Map<IScheduler, Schedule> getStartSchedulesParallel(
			final ScheduleSpace timeline) {
		final Map<IScheduler, Schedule> schedules = new HashMap<IScheduler, Schedule>();

		final List<IScheduler> schedulers = getSchedulers();

		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				schedulers.size());

		ThreadPoolExecutor tpe = new ThreadPoolExecutor(3, 5, 10,
				TimeUnit.SECONDS, queue);
		for (final IScheduler s : schedulers) {
			tpe.execute(new ScheduleRunnable(s, timeline) {
				@Override
				public void run() {
					super.run();
					schedules.put(super.getScheduler(), getSchedule());
					exportSchedule(schedules, s, getSchedule());
				}
			});
		}
		tpe.shutdown();
		while (true) {
			try {
				log.debug("waiting for scheduling to complete ... "
						+ tpe.getActiveCount() + " active");
				if (tpe.awaitTermination(30, TimeUnit.SECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}

		return schedules;
	}

	private static List<IScheduler> getSchedulers() {
		final List<IScheduler> schedulers = new ArrayList<IScheduler>();

		schedulers.add(new ParallelLinearScheduler());

		schedulers.add(new CPULikeScheduler(new FairPrioritizedSelector()));
		schedulers.add(new CPULikeScheduler(new FairPrioritizedSelector()));
		schedulers.add(new CPULikeScheduler(new KeepingPrioritizedSelector()));
		schedulers.add(new CPULikeScheduler(new PrioritizedSelector()));
		schedulers.add(new CPULikeScheduler(new ShortestFirstSelector()));
		schedulers.add(new CPULikeScheduler(new RandomizedSelector()));
		schedulers.add(new CPULikeScheduler(new RandomizedSelector()));

		schedulers.add(new GreedyScheduler());

		schedulers.add(new GreedyPlacementScheduler(
				new PressureJobSortCriterion()));
		schedulers.add(new GreedyPlacementScheduler(
				new PriorityJobSortCriterion()));

		return schedulers;
	}
}
