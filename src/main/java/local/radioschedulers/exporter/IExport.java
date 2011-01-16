package local.radioschedulers.exporter;

import java.io.IOException;

import local.radioschedulers.SpecificSchedule;

public interface IExport {

	public void export(SpecificSchedule schedule) throws IOException;
	
}