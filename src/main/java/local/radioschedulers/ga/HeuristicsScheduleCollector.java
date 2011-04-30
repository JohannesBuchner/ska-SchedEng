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
import local.radioschedulers.exporter.ExportFactory;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.parallel.GreedyPressureScheduler;
import local.radioschedulers.parallel.PressureJobSortCriterion;
import local.radioschedulers.parallel.PriorityJobSortCriterion;
import local.radioschedulers.parallel.TrivialFirstParallelListingScheduler;
import local.radioschedulers.serial.ContinuousLeastChoiceScheduler;
import local.radioschedulers.serial.ContinuousUnlessOneChoiceScheduler;
import local.radioschedulers.serial.ExtendingLeastChoiceScheduler;
import local.radioschedulers.serial.FairPrioritizedSelector;
import local.radioschedulers.serial.JobSelector;
import local.radioschedulers.serial.KeepingPrioritizedSelector;
import local.radioschedulers.serial.PrioritizedSelector;
import local.radioschedulers.serial.RandomizedSelector;
import local.radioschedulers.serial.SerialLeastChoiceScheduler;
import local.radioschedulers.serial.SerialListingScheduler;
import local.radioschedulers.serial.ShortestFirstSelector;
import local.radioschedulers.serial.SmootheningScheduler;

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
				executionTimeLog
						.println(duration / 1000. + "\t" + s.toString());
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

	private static int getNJobSelectors() {
		return 5;
	}

	private static JobSelector getJobSelector(int i) {
		if (i == 0)
			return new KeepingPrioritizedSelector();
		if (i == 1)
			return new PrioritizedSelector();
		if (i == 2)
			return new ShortestFirstSelector();
		if (i == 3)
			return new RandomizedSelector();
		if (i == 4)
			return new FairPrioritizedSelector();
		return null;
	}

	private static List<IScheduler> getSchedulers() {
		final List<IScheduler> schedulers = new ArrayList<IScheduler>();

		// schedulers.add(new ParallelLinearScheduler());

		for (int i = 0; i < getNJobSelectors(); i++)
			schedulers.add(new SerialListingScheduler(getJobSelector(i)));

		schedulers.add(new GreedyPressureScheduler());

		schedulers.add(new TrivialFirstParallelListingScheduler(
				new PressureJobSortCriterion()));
		schedulers.add(new TrivialFirstParallelListingScheduler(
				new PriorityJobSortCriterion()));

		for (int i = 0; i < getNJobSelectors(); i++) {
			schedulers.add(new SerialLeastChoiceScheduler(getJobSelector(i)));
			schedulers.add(new ContinuousUnlessOneChoiceScheduler(
					getJobSelector(i)));
			schedulers
					.add(new ContinuousLeastChoiceScheduler(getJobSelector(i)));
			schedulers
					.add(new ExtendingLeastChoiceScheduler(getJobSelector(i)));
			schedulers.add(new SmootheningScheduler(
					new SerialLeastChoiceScheduler(getJobSelector(i))));
		}
		return schedulers;
	}
}
