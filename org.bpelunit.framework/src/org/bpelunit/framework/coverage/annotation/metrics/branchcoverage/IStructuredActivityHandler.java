package org.bpelunit.framework.coverage.annotation.metrics.branchcoverage;


import org.bpelunit.framework.coverage.exceptions.BpelException;
import org.jdom.Element;




/**
 * @author Alex Salnikow
 */
public interface IStructuredActivityHandler {
	/**
	 * F�gt Markierungen, die sp�ter durch Invoke-Aufrufe protokolliert werden,
	 * um die Ausf�hrung der Zweige zu erfassen.
	 * 
	 * @param structured_activity
	 * @throws BpelException 
	 */
	public void insertBranchMarkers(Element structured_activity) throws BpelException;
}
