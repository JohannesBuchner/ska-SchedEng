package local.radioschedulers.exporter;

import java.io.File;

import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;

public class ExportFactory {
	public static IExport getExport(File f, String title) {
		return new HtmlExportWithFitness(f, title,
				new SimpleScheduleFitnessFunction());
	}

	public static IExport getHtmlExport(File f, String title) {
		return new HtmlExportWithFitness(f, title,
				new SimpleScheduleFitnessFunction());
	}
}
