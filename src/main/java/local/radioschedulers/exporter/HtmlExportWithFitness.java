package local.radioschedulers.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.Schedule;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;

public class HtmlExportWithFitness extends HtmlExport {

	public HtmlExportWithFitness(File f, String title) {
		super(f, title);
	}

	public HtmlExportWithFitness(File f) {
		super(f);
	}

	@Override
	protected void appendAdditionalStats(Schedule schedule, FileWriter fw,
			Map<Job, Integer> jobs, int nslots) throws IOException {
		super.appendAdditionalStats(schedule, fw, jobs, nslots);
		this.appendFitnessValue(schedule, fw);
	}

	private void appendFitnessValue(Schedule schedule, FileWriter fw)
			throws IOException {
		fw.append("<h2>Fitness value: " + getValue(schedule) + "</h2>");
	}

	private double getValue(Schedule s) {
		return new SimpleScheduleFitnessFunction().evaluate(s);
	}

}
