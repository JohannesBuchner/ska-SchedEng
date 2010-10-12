package local.radioschedulers.exporter;

import java.io.IOException;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.Schedule;

public abstract class IExport {

	public abstract void export(Schedule schedule) throws IOException;
	
	public static boolean same(List<Job> a, List<Job> b) {
		if (a.size() != b.size())
			return false;
		for (Job j : a) {
			if (!b.contains(j))
				return false;
		}
		for (Job j : b) {
			if (!a.contains(j))
				return false;
		}
		return true;
	}

	public IExport() {
		super();
	}

}