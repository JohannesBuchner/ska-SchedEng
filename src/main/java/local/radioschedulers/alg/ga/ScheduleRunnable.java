package local.radioschedulers.alg.ga;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;

public class ScheduleRunnable implements Runnable {
	private static Logger log = Logger.getLogger(ScheduleRunnable.class);

	private IScheduler scheduler;

	private ScheduleSpace timeline;

	private Schedule schedule;

	public ScheduleRunnable(IScheduler scheduler, ScheduleSpace timeline) {
		this.scheduler = scheduler;
		this.timeline = timeline;
	}

	@Override
	public void run() {
		log.debug("starting scheduler " + scheduler);
		this.schedule = this.scheduler.schedule(timeline);
		log.debug("finished with scheduler " + scheduler);
	}

	public Schedule getSchedule() {
		return schedule;
	}
	
	public IScheduler getScheduler() {
		return scheduler;
	}
	
	public ScheduleSpace getTimeline() {
		return timeline;
	}
}
