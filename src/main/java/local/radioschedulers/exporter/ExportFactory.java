package local.radioschedulers.exporter;

import java.io.File;
import java.util.Collection;

import local.radioschedulers.Proposal;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.ScheduleFitnessFunction;
import local.radioschedulers.alg.ga.fitness.NormalizedScheduleFitnessFunction;
import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;

public class ExportFactory {
	private static ScheduleFitnessFunction fitness = new SimpleScheduleFitnessFunction();

	public static IExport getExport(File f, String title) {
		return new HtmlExportWithFitness(f, title, fitness);
	}

	public static IExport getHtmlExport(File f, String title) {
		return new HtmlExportWithFitness(f, title, fitness);
	}

	public static void setSpace(ScheduleSpace template, Collection<Proposal> proposals) {
		NormalizedScheduleFitnessFunction n = new NormalizedScheduleFitnessFunction(
				new SimpleScheduleFitnessFunction());
		n.setupNormalization(template, proposals);
		ExportFactory.fitness = n;
	}
}
