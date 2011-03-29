package local.radioschedulers.run.demo;

import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;

public interface ScheduleExport {

	public abstract void export(Schedule s);

	public abstract void export(Schedule s, LSTTime t);
	
}
