package coverage.instrumentation.metrics.statementcoverage;


import static coverage.instrumentation.bpelxmltools.BpelXMLTools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import coverage.instrumentation.bpelxmltools.BasicActivity;
import coverage.instrumentation.bpelxmltools.StructuredActivity;
import coverage.instrumentation.metrics.IMetric;
import coverage.wstools.CoverageRegistry;

/**
 * Klasse instrumentiert ein BPEL-Prozess, um die Abdeckung der BasicActivities
 * bei der Ausf�hrung zu messen. Die einzelnen Aktivit�ten, die bei der Messung
 * ber�cksichtigt werden m�ssen, m�ssen explizit angegeben werden ({@link #addBasicActivity(String)}).
 * 
 * @author Alex Salnikow
 */
public class Statementmetric implements IMetric {

	public static final String METRIC_NAME = "Statementmetric";

	private static int count = 0;

	private HashMap<String, String> logging_before_activity;

	private HashMap<String, String> activities_to_respekt;

	public Statementmetric() {
		activities_to_respekt = new HashMap<String, String>();
		logging_before_activity = new HashMap<String, String>();
		logging_before_activity.put(BasicActivity.THROW_ACTIVITY,
				BasicActivity.THROW_ACTIVITY);
		logging_before_activity.put(BasicActivity.RETHROW_ACTIVITY,
				BasicActivity.RETHROW_ACTIVITY);
		logging_before_activity.put(BasicActivity.COMPENSATE_ACTIVITY,
				BasicActivity.COMPENSATE_ACTIVITY);
		logging_before_activity.put(BasicActivity.COMPENSATESCOPE_ACTIVITY,
				BasicActivity.COMPENSATESCOPE_ACTIVITY);
		logging_before_activity.put(BasicActivity.EXIT_ACTIVITY,
				BasicActivity.EXIT_ACTIVITY);
	}

	/**
	 * Diese Methode f�gt die Markierungen in BPEL-Process-Element ein
	 * (Instrumentierung). Anhand dieser Markierungen werden danach
	 * entsprechende Invoke aufrufe generiert und dadurch die Ausf�hrung
	 * bestimmter Aktivit�ten geloggt.
	 * 
	 * @param process_element
	 *            Prozess-Element der BPEL
	 */
	public void insertCoverageLabels(Element element) {
		ElementFilter filter = new ElementFilter(getBpelNamespace()) {
			@Override
			public boolean matches(Object obj) {
				boolean result = false;
				Element element;
				if (super.matches(obj)) {
					element = (Element) obj;
					if (activities_to_respekt.containsKey(element.getName())) {
						result = true;
					}
				}
				return result;
			}
		};
		List<Element> elements_to_log = new ArrayList<Element>();
		for (Iterator iter = element.getDescendants(filter); iter.hasNext();) {
			elements_to_log.add((Element) iter.next());
		}
		insertMarkerForEachActivity(elements_to_log);
	}

	/**
	 * F�gt Markierungen f�r jede Aktivit�t ein. Wenn es notwendig ist, dann
	 * werden die Aktivit�t und die Markierung in ein Sequence-Element
	 * eingeschlo�en, sa dass die Markierung im Kontrollflu� direkt vor oder
	 * nach der zu loggenden Aktivit�t ist.
	 * 
	 * @param elements_to_log
	 *            Aktivit�ten, die geloggt werden sollen.
	 */
	private void insertMarkerForEachActivity(List<Element> elements_to_log) {
		Element element;
		Element targetElement;
		for (int i = 0; i < elements_to_log.size(); i++) {
			element = elements_to_log.get(i);
			Element parent = element.getParentElement();
			targetElement = element.getChild(TARGETS_ELEMENT,getBpelNamespace());
			if (targetElement != null) {
				Element sequence = encloseInSequence(element);
				sequence.addContent(0, targetElement.detach());
			} else if (!parent.getName().equals(
					StructuredActivity.SEQUENCE_ACTIVITY)) {
				ensureElementIsInSequence(element);
			}
			insertMarkerForActivity(element);
		}
	}

	/**
	 * F�gt eine Markierung f�r die Aktivit�t ein. Die Markierung wird entweder
	 * direkt vor oder nach der Aktivit�t eingef�gt. Die Aktivit�ten, die in
	 * {@link #logging_before_activity} eingetragen sind, m�ssen vor der
	 * Ausf�hrung geloggt werden.
	 * 
	 * @param element
	 *            Aktivit�t
	 */
	private void insertMarkerForActivity(Element element) {
		Element parent = element.getParentElement();
		String element_name = element.getName();
		String marker=element_name + COVERAGE_LABEL_INNER_SEPARATOR + (count++);
		CoverageRegistry.getInstance().addMarker(marker);
		Comment comment = new Comment(IMetric.COVERAGE_LABEL_IDENTIFIER + marker);
		int index = parent.indexOf(element);
		if (logging_before_activity.containsKey(element_name)) {
			parent.addContent(index, comment);
		} else {
			parent.addContent(index + 1, comment);
		}

	}

	/**
	 * Registriert Aktivit�ten, die bei der Messung der Abdeckung ber�cksichtigt
	 * werden m�ssen.
	 * 
	 * @param activity_name
	 *            Aktivit�t
	 */
	public void addBasicActivity(String activity_name) {
		activities_to_respekt.put(activity_name, activity_name);
	}

	public void addAllBasicActivities() {
		addBasicActivity(BasicActivity.ASSIGN_ACTIVITY);
		addBasicActivity(BasicActivity.COMPENSATE_ACTIVITY);
		addBasicActivity(BasicActivity.COMPENSATESCOPE_ACTIVITY);
		addBasicActivity(BasicActivity.EMPTY_ACTIVITY);
		addBasicActivity(BasicActivity.INVOKE_ACTIVITY);
		addBasicActivity(BasicActivity.RECEIVE_ACTIVITY);
		addBasicActivity(BasicActivity.REPLY_ACTIVITY);
		addBasicActivity(BasicActivity.RETHROW_ACTIVITY);
		addBasicActivity(BasicActivity.THROW_ACTIVITY);
		addBasicActivity(BasicActivity.WAIT_ACTIVITY);
	}

	public void removeBasicActivity(String activity_name) {
		activities_to_respekt.remove(activity_name);
	}

	public void removeAllBasicActivities() {
		removeBasicActivity(BasicActivity.ASSIGN_ACTIVITY);
		removeBasicActivity(BasicActivity.COMPENSATE_ACTIVITY);
		removeBasicActivity(BasicActivity.COMPENSATESCOPE_ACTIVITY);
		removeBasicActivity(BasicActivity.EMPTY_ACTIVITY);
		removeBasicActivity(BasicActivity.INVOKE_ACTIVITY);
		removeBasicActivity(BasicActivity.RECEIVE_ACTIVITY);
		removeBasicActivity(BasicActivity.REPLY_ACTIVITY);
		removeBasicActivity(BasicActivity.RETHROW_ACTIVITY);
		removeBasicActivity(BasicActivity.THROW_ACTIVITY);
		removeBasicActivity(BasicActivity.WAIT_ACTIVITY);
	}

	@Override
	public String toString() {
		String name = METRIC_NAME + ": ";
		for (Iterator<String> e = activities_to_respekt.values().iterator(); e
				.hasNext();) {
			name = name + e.next() + ", ";
		}
		return name;
	}

	public Set<String> getBasisActivities() {
		return activities_to_respekt.keySet();
	}

	public String getName() {
		return METRIC_NAME;
	}
}
