package local.radioschedulers.exporter;

import java.io.File;

public class ExportFactory {
	public static IExport getExport(File f, String title) {
		return new HtmlExportWithFitness(f, title);
	}
	public static IExport getHtmlExport(File f, String title) {
		return new HtmlExportWithFitness(f, title);
	}
}
