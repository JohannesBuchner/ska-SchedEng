package local.radioschedulers.ga;

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
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.cpu.ShortestFirstSelector;
import local.radioschedulers.lp.ParallelLinearScheduler;

import org.apache.log4j.Logger;

public class ParallelizedHeuristicsScheduleCollector {

	private static Logger log = Logger
			.getLogger(ParallelizedHeuristicsScheduleCollector.class);

	public static Map<IScheduler, Schedule> getStartSchedules(
			final ScheduleSpace timeline) {
		final Map<IScheduler, Schedule> schedules = new HashMap<IScheduler, Schedule>();

		final List<IScheduler> schedulers = new ArrayList<IScheduler>();

		schedulers.add(new ParallelLinearScheduler());

		schedulers.add(new CPULikeScheduler(new FairPrioritizedSelector()));
		schedulers.add(new CPULikeScheduler(new PrioritizedSelector()));
		schedulers.add(new CPULikeScheduler(new ShortestFirstSelector()));
		schedulers.add(new CPULikeScheduler(new RandomizedSelector()));
		schedulers.add(new CPULikeScheduler(new RandomizedSelector()));
		schedulers.add(new CPULikeScheduler(new RandomizedSelector()));
		schedulers.add(new CPULikeScheduler(new RandomizedSelector()));

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
				}
			});
		}
		tpe.shutdown();
		while (true) {
			try {
				log.debug("waiting for scheduling to complete ... "
						+ tpe.getActiveCount() + " active");
				if (tpe.awaitTermination(10, TimeUnit.SECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}

		return schedules;
	}
}
