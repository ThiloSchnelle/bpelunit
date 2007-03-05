package coverage.instrumentation.metrics.branchcoverage;

import org.jdom.Element;

import exception.BpelException;

/**
 * @author Alex Salnikow
 */
public interface IStructuredActivity {
	/**
	 * F�gt Markierungen, die sp�ter durch Invoke-Aufrufe protokolliert werden,
	 * um die Ausf�hrung der Zweige zu erfassen.
	 * 
	 * @param structured_activity
	 * @throws BpelException 
	 */
	public void insertMarkerForBranchCoverage(Element structured_activity) throws BpelException;
}
