package local.radioschedulers.exporter;

import java.io.IOException;

import local.radioschedulers.Schedule;

public interface IExport {

	public void export(Schedule schedule) throws IOException;
	
}