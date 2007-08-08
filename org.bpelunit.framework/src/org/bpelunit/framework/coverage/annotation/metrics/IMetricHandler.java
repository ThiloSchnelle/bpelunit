package org.bpelunit.framework.coverage.annotation.metrics;

import java.util.List;

import org.bpelunit.framework.coverage.exceptions.BpelException;
import org.jdom.Element;

/**
 * Die Schnittstelle ist f�r Handler vorgesehen, die die Instrumentierung f�r die Metriken �bernehmen.
 * @author Alex
 *
 */
public interface IMetricHandler {

	/**
	 * F�gt die Marker an den richtigen Stellen in
	 * BPEL-Process-Element ein (Instrumentierung). Anhand dieser Marker werden
	 * danach entsprechende Invoke aufrufe generiert und dadurch die Ausf�hrung
	 * bestimmter Aktivit�ten geloggt.
	 * 
	 * @param process_elements
	 * @throws BpelException 
	 */
	public void insertMarkersForMetric(List<Element> process_elements) throws BpelException;
	
}
