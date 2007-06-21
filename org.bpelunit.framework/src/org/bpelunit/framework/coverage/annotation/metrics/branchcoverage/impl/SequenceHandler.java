package org.bpelunit.framework.coverage.annotation.metrics.branchcoverage.impl;

import static org.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.getProcessNamespace;
import static org.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.isActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bpelunit.framework.coverage.annotation.metrics.branchcoverage.BranchMetricHandler;
import org.bpelunit.framework.coverage.annotation.metrics.branchcoverage.IStructuredActivityHandler;
import org.bpelunit.framework.coverage.receiver.MarkersRegisterForArchive;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;


/**
 * Die Klasse ist f�r das Einf�gen der Markierungen in der Sequence-Aktivit�t
 * verantwortlich, die f�r die Messung der Zweigabdeckung verwendet werden.
 * 
 * @author Alex Salnikow
 */
public class SequenceHandler implements IStructuredActivityHandler {

	private MarkersRegisterForArchive markersRegistry;

	public SequenceHandler(MarkersRegisterForArchive markersRegistry) {
		this.markersRegistry = markersRegistry;
	}

	/**
	 * F�gt Markierungen in Sequence-Elemente ein, die sp�ter, um die Ausf�hrung
	 * der Zweige zu erfassen, durch Invoke-Aufrufe protokolliert werden.
	 * 
	 * @param sequence
	 */
	public void insertBranchMarkers(Element sequence) {
		List<Element> children = sequence.getContent(new ElementFilter(getProcessNamespace()));
		Element child;
		List<Element> activities=new ArrayList<Element>();
		for (Iterator<Element> iter = children.iterator(); iter.hasNext();) {
			activities.add(iter.next());
		}
		
		Element previousActivity = null;
		for (int i = 0; i < activities.size(); i++) {
			child = activities.get(i);
			if (isActivity(child)) {
				if (previousActivity != null) {
					{
						markersRegistry.addMarker(BranchMetricHandler.insertLabelBevorActivity(child));
					}
				}
				previousActivity = child;
			}
		}
	}
}
