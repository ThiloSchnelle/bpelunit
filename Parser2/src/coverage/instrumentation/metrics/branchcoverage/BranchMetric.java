package coverage.instrumentation.metrics.branchcoverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import coverage.instrumentation.bpelxmltools.BpelXMLTools;
import coverage.instrumentation.bpelxmltools.StructuredActivity;
import coverage.instrumentation.metrics.IMetric;
import exception.BpelException;

/**
 * Klasse instrumentiert ein BPEL-Prozess, um die Zweigabdeckung bei der
 * Ausf�hrung zu messen.
 * 
 * @author Alex Salnikow
 */
public class BranchMetric implements IMetric {

	public static final String METRIC_NAME = "Branchmetric";

	private static final String BRANCH_LABEL = "branch";

	private static final String LINK_LABEL = "link";

	private static int count = 0;

	/**
	 * Generiert eine eindeutige Markierung.
	 * 
	 * @return eindeutige Markierung
	 */
	public static String getNextLabel() {
		return BRANCH_LABEL + (count++);
	}

	/**
	 * Generiert eindeutige Merkierung f�r die Links (in der Flow-Umgebung)
	 * 
	 * @return eindeutige Markierung
	 */
	public static String getNextLinkLabel() {
		return LINK_LABEL + getNextLabel();
	}

	/**
	 * F�gt Markierung f�r einen Zweig, der durch eine Aktivit�t repr�sentiert
	 * ist. Es wird eine Markierung davor und eine danach eingef�gt.
	 * 
	 * @param activity
	 *            Aktivit�t, die einen Zweig repr�sentiert
	 * @param additionalInfo
	 */
	public static void insertMarkerForBranch(Element activity,
			String additionalInfo) {
		insertMarkerBevorAllActivities(activity, additionalInfo);
		insertMarkerAfterAllActivities(activity, additionalInfo);
	}

	/**
	 * F�gt eine Markierung vor allen Aktivit�ten ein. Wenn es notwendig ist,
	 * dann wird die Markierung ind die Aktivit�ten in ein Sequence-Element
	 * eingeschlossen.
	 * 
	 * @param activity
	 * @param additionalInfo
	 */
	public static void insertMarkerBevorAllActivities(Element activity,
			String additionalInfo) {
		if (!BpelXMLTools.isSequence(activity)) {
			activity = BpelXMLTools.ensureElementIsInSequence(activity);
		}
		activity.addContent(0, new Comment(BranchMetric.getNextLabel()
				+ additionalInfo));
	}

	/**
	 * F�gt eine Markierung nach allen Aktivit�ten ein. Wenn es notwendig ist,
	 * dann wird die Markierung ind die Aktivit�ten in ein Sequence-Element
	 * eingeschlossen.
	 * 
	 * @param activity
	 * @param additionalInfo
	 */
	public static void insertMarkerAfterAllActivities(Element activity,
			String additionalInfo) {
		if (!BpelXMLTools.isSequence(activity)) {
			activity = BpelXMLTools.ensureElementIsInSequence(activity);
		}
		activity.addContent(new Comment(BranchMetric.getNextLabel()
				+ additionalInfo));
	}

	/**
	 * 
	 * @param activity
	 *            muss innerhalb Sequence sein
	 */
	public static void insertMarkerAfterActivity(Element activity) {
		Element parent = activity.getParentElement();
		parent.addContent(parent.indexOf(activity) + 1, new Comment(
				getNextLabel()));
	}

	private HashMap<String, IStructuredActivity> structured_activity_handler = new HashMap<String, IStructuredActivity>();

	public BranchMetric() {

		structured_activity_handler.put(StructuredActivity.FLOW_ACTIVITY,
				new FlowActivityHandler());
		structured_activity_handler.put(StructuredActivity.SEQUENCE_ACTIVITY,
				new SequenceActivityHandler());
		structured_activity_handler.put(StructuredActivity.IF_ACTIVITY,
				new IfActivityHandler());
		structured_activity_handler.put(StructuredActivity.WHILE_ACTIVITY,
				new WhileActivityHandler());
		structured_activity_handler.put(
				StructuredActivity.REPEATUNTIL_ACTIVITY,
				new RepeatUntilActivityHandler());
		structured_activity_handler.put(StructuredActivity.FOREACH_ACTIVITY,
				new ForEachActivityHandler());
		structured_activity_handler.put(StructuredActivity.PICK_ACTIVITY,
				new PickActivityHandler());
	}

	public void insertMarker(Element element) throws BpelException {
		Element next_element;
		Iterator iterator2 = element.getDescendants(new ElementFilter());
		List<Element> elements_to_log = new ArrayList<Element>();
		while (iterator2.hasNext()) {
			next_element = (Element) iterator2.next();
			if (BpelXMLTools.isStructuredActivity(next_element)) {
				elements_to_log.add(next_element);
			}
		}
		for (Iterator<Element> iter = elements_to_log.iterator(); iter
				.hasNext();) {
			next_element = iter.next();
			String next_element_name = next_element.getName();
			if (structured_activity_handler.containsKey(next_element_name)) {
				structured_activity_handler.get(next_element_name)
						.insertMarkerForBranchCoverage(next_element);
			}
		}
	}

	@Override
	public String toString() {
		return METRIC_NAME;
	}

}
