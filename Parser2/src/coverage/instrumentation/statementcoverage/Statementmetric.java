package coverage.instrumentation.statementcoverage;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.jdom.Comment;
import org.jdom.Element;

import coverage.instrumentation.ActivityTools;
import coverage.instrumentation.BasisActivity;
import coverage.instrumentation.IMetric;
import coverage.instrumentation.StructuredActivity;

public class Statementmetric implements IMetric {

	private Hashtable<String, String> logging_before_activity;

	private Hashtable<String, String> activities_to_respekt;

	private List<Element> elements_to_log;

	private static int count = 0;

	public Statementmetric() {
		activities_to_respekt = new Hashtable<String, String>();
		elements_to_log = new ArrayList<Element>();
		logging_before_activity = new Hashtable<String, String>();
		logging_before_activity.put(BasisActivity.THROW_ACTIVITY, BasisActivity.THROW_ACTIVITY);
		logging_before_activity.put(BasisActivity.RETHROW_ACTIVITY, BasisActivity.RETHROW_ACTIVITY);
		logging_before_activity.put(BasisActivity.COMPENSATE_ACTIVITY, BasisActivity.COMPENSATE_ACTIVITY);
		logging_before_activity.put(BasisActivity.COMPENSATESCOPE_ACTIVITY, BasisActivity.COMPENSATESCOPE_ACTIVITY);
		logging_before_activity.put(BasisActivity.EXIT_ACTIVITY, BasisActivity.EXIT_ACTIVITY);
	}

	public void insertMarker(Element element) {
		BasicActivitiesFilter filter = new BasicActivitiesFilter(element
				.getNamespace(), activities_to_respekt);
		for (Iterator iter = element.getDescendants(filter); iter.hasNext();) {
			elements_to_log.add((Element) iter.next());
		}
		insertMarkerForEachElement(elements_to_log);
	}

	private void insertMarkerForEachElement(List<Element> elements_to_log) {
		Element element;
		Element targetelement;
		for (int i = 0; i < elements_to_log.size(); i++) {
			element = elements_to_log.get(i);
			Element parent = element.getParentElement();
			targetelement = element.getChild("targets",
					ActivityTools.NAMESPACE_BPEL_2);
			if (targetelement != null) {
				Element sequence = ActivityTools
						.encloseActivityInSequence(element);
				sequence.addContent(0, targetelement.detach());
			} else if (!parent.getName().equals(
					StructuredActivity.SEQUENCE_ACTIVITY)) {
				ActivityTools.ensureActivityIsInSequence(element);
			}
			insertMarkerForThisActivity(element);
		}
	}

	private void insertMarkerForThisActivity(Element element) {
		Element parent = element.getParentElement();
		String element_name = element.getName();
		Comment mark = new Comment(element_name + (count++) + " "
				+ element.getName());
		int index = parent.indexOf(element);
		if (logging_before_activity.contains(element_name)) {
			parent.addContent(index, mark);
		} else {
			parent.addContent(index + 1, mark);
		}

	}

	public void addBasicActivity(String activity_name) {
		activities_to_respekt.put(activity_name, activity_name);
	}

	public void removeBasicActivity(String activity_name) {
		activities_to_respekt.remove(activity_name);
	}
}
