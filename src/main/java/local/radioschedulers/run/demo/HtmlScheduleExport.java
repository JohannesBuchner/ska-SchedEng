package local.radioschedulers.run.demo;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import local.radioschedulers.Schedule;
import local.radioschedulers.exporter.HtmlExport;

public class HtmlScheduleExport implements ScheduleExport {
	public static File htmlExportFile = new File("live-schedule.html");

	@Override
	public void export(Schedule s) {
		try {
			HtmlExport ex = new HtmlExport(htmlExportFile);
			ex.export(s);
			Desktop d = Desktop.getDesktop();
			d.open(htmlExportFile);
		} catch (IOException e1) {
		}
	}

}
