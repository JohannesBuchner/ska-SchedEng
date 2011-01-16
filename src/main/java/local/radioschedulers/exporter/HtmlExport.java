package local.radioschedulers.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;

public class HtmlExport implements IExport {

	private static final boolean MERGESAME = true;
	private File f;
	private String title;

	public HtmlExport(File f, String title) {
		this.f = f;
		this.title = title;
	}

	public HtmlExport(File f) {
		this.f = f;
	}

	public void export(Schedule schedule) throws IOException {
		FileWriter fw;
		fw = new FileWriter(f);
		fw.append("<html>");
		fw.append("<head>");
		if (this.title != null)
			fw.append("<title>" + this.title + "</title>");
		fw
				.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
		fw.append("</head>");
		fw.append("<table>\n\t<thead>\n\t\t<tr>\n\t\t\t<th>day \\ LST</th>");
		for (int i = 0; i < 24; i++) {
			fw.append("\n\t\t\t<th>" + i + "</th>");
			fw.append("\n\t\t\t<th>&nbsp;</th>");
			fw.append("\n\t\t\t<th>&nbsp;</th>");
			fw.append("\n\t\t\t<th>&nbsp;</th>");
		}
		fw.append("\n\t\t</tr>\n\t</thead>\n\t<tbody>");

		LSTTime lastday = schedule.getLastEntry();
		
		LSTTime t = new LSTTime(0L, 0L);
		for (t.day = 0L; t.day <= lastday.day + 1; t.day++) {
			fw.append("\n\t\t<tr>\n\t\t\t<th>" + t.day + "</th>");
			for (t.minute = 0L; t.minute < 24 * 60;) {
				fw.append("\n\t\t\t");
				JobCombination jc = schedule.get(new LSTTime(t.day, t.minute));
				int ncells = 1;
				LSTTime t2 = new LSTTime(t.day, t.minute + 15);
				if (MERGESAME)
					for (; t2.minute < 24 * 60; t2.minute += 15) {
						JobCombination jc2 = schedule.get(new LSTTime(
								t2.day, t2.minute));
						if (jc.equals(jc2)) {
							ncells++;
						} else
							break;
					}
				String params = "";
				if (jc.jobs.isEmpty())
					params += " class=\"free\" ";

				if (ncells > 1) {
					log("same between [" + t + ".." + t2 + ")");
					params += " colspan=" + ncells + " ";
				}
				fw.append("<td " + params + ">");

				if (jc.jobs.isEmpty()) {
					fw.append("&nbsp;");
				} else {
					for (Job j : jc.jobs) {
						log("@" + t + ": " + j + "");
						fw.append(j.proposal.name + "/" + j.hours + " ");
					}
				}
				t.minute = t2.minute;
				fw.append("</td>");

			}
			fw.append("\n\t\t</tr>");
		}

		fw.append("\n\t</tbody>\n</table>\n");
		fw.close();
	}

	private void log(String string) {

	}

}
