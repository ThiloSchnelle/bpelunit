package coverage.instrumentation;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import coverage.instrumentation.branchcoverage.BranchMetric;
import coverage.instrumentation.exception.BpelException;
import coverage.instrumentation.exception.BpelVersionException;
import coverage.instrumentation.statementcoverage.Statementmetric;

/**
 * Dieses Interface wird von dem Handler implementiert, der daf�r zust�ndig ist,
 * die Instrumentierung der BPEL-Datei zu starten und dabei nur die gew�nschten
 * Metriken zu ber�cksichtigen.
 * 
 * @author Alex Salnikow
 * 
 */
public interface IMetricHandler {

	public static final String STATEMENT_METRIC = Statementmetric.METRIC_NAME;

	public static final String BRANCH_METRIC = BranchMetric.METRIC_NAME;

	/**
	 * Die �bergebene Metrik wird bei der Ausf�hrung der BPEL erhoben:
	 * 
	 * @param metricName
	 */
	public IMetric addMetric(String metricName);

	/**
	 * Die Metrik wird bei der Ausf�hrung der BPEL nicht erhoben.
	 * 
	 * @param metricName
	 */
	public void remove(String metricName);

	/**
	 * Startet die Instrumentierung der BPEL-Datei.
	 * 
	 * @param file
	 * @throws JDOMException
	 * @throws IOException
	 * @throws BpelException
	 * @throws BpelVersionException
	 */
	public File startInstrumentation(File file) throws JDOMException,
			IOException, BpelException, BpelVersionException;

	/**
	 * 
	 * @param metricName
	 * @return Metrik
	 */
	public IMetric getMetric(String metricName);

}
