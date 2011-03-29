package local.radioschedulers.run.demo;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.exporter.HtmlExport;

public class HtmlScheduleExport implements ScheduleExport {
	private static final boolean LAUNCH = false;
	public static File htmlExportFile = new File("live-schedule.html");

	@Override
	public void export(Schedule s) {
		try {
			HtmlExport ex = new HtmlExport(htmlExportFile, "current schedule");
			ex.export(s);
			if (LAUNCH) {
				Desktop d = Desktop.getDesktop();
				d.open(htmlExportFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void export(Schedule s, LSTTime t) {
		this.export(s);
		if (t == null)
			return;
		PrintStream p;
		try {
			p = new PrintStream(new FileOutputStream(htmlExportFile, true));
			p
					.println("<h2>Current time: day "
							+ t.day
							+ " hour "
							+ t.minute
							/ Schedule.LST_SLOTS_MINUTES
							/ Schedule.LST_SLOTS_PER_HOUR
							+ " minute "
							+ t.minute
							% (Schedule.LST_SLOTS_MINUTES * Schedule.LST_SLOTS_PER_HOUR)
							+ "</h2>");
			p.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
