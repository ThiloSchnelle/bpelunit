package coverage.instrumentation.metrics.branchcoverage;

import org.jdom.Element;

import coverage.exception.BpelException;
import coverage.instrumentation.bpelxmltools.BpelXMLTools;

public class RepeatUntilActivityHandler implements IStructuredActivity {

	public void insertMarkerForBranchCoverage(Element element)
			throws BpelException {
		branchFromConditionToActivity(element);
		branchFromActivityToCondition(element);
	}

	private void branchFromActivityToCondition(Element element)
			throws BpelException {
		Element activity = BpelXMLTools.getFirstActivityChild(element);
		if (element == null) {
			throw new BpelException(BpelException.MISSING_REQUIRED_ACTIVITY);
		}
		BranchMetric.insertMarkerAfterAllActivities(activity, "");
	}

	private void branchFromConditionToActivity(Element element)
			throws BpelException {
		Element countVariable = BpelXMLTools.createVariable(element
				.getDocument());
		BpelXMLTools.insertVariable(countVariable, element.getDocument()
				.getRootElement());
		Element initializeAssign = BpelXMLTools
				.createInitializeAssign(countVariable);
		insert(initializeAssign, element);
		Element increesAssign = BpelXMLTools.createIncreesAssign(countVariable);
		insertIncreesAssign(increesAssign, element);
		insertIfConstruct(element, countVariable);
	}

	/**
	 * F�gt Markierung f�r den Zweig von Aktivit�ten zu der Condition. Daf�r
	 * wird eine If-Anfrage eingef�gt, die anhand der Z�hlvariable �berpr�ft, ob
	 * die Aktivit�ten in der Schleife ausgef�hrt wurden.
	 * 
	 * @param element
	 * @param countVariable
	 * @throws BpelException
	 */
	private void insertIfConstruct(Element element, Element countVariable)
			throws BpelException {
		Element activity = BpelXMLTools.getFirstActivityChild(element);
		if (activity == null) {
			throw new BpelException(BpelException.MISSING_REQUIRED_ACTIVITY);
		}
		if (!BpelXMLTools.isSequence(activity)) {
			activity = BpelXMLTools.ensureElementIsInSequence(activity);
		}
		Element if_element = BpelXMLTools.createIfActivity("$"
				+ countVariable.getName() + "=1");

		Element sequence = BpelXMLTools.createSequence();
		if_element.addContent(sequence);
		activity.addContent(0, if_element);
		BranchMetric.insertMarkerBevorAllActivities(sequence, "");

	}

	/**
	 * F�gt ein Assign Element an letzter Stelle innerhalb des
	 * RepeatUntil-Konstrukts, das die Za�hlvariable um eins erh�ht und
	 * registriert damit die Ausf�hrung der Schleife.
	 * 
	 * @param increesAssign
	 * @param element
	 *            RepeatUntil-Element
	 * @throws BpelException
	 */
	private void insertIncreesAssign(Element increesAssign, Element element)
			throws BpelException {
		Element activity = BpelXMLTools.getFirstActivityChild(element);
		if (activity == null) {
			throw new BpelException(BpelException.MISSING_REQUIRED_ACTIVITY);
		}
		if (!BpelXMLTools.isSequence(activity)) {
			activity = BpelXMLTools.ensureElementIsInSequence(activity);
		}
		activity.addContent(increesAssign);

	}

	/**
	 * 
	 * @param initializeAssign
	 *            Assign-Element, das die Variable initialisiert.
	 * @param element
	 *            RepeatUntil-Element
	 */
	private void insert(Element initializeAssign, Element element) {
		Element sequence = BpelXMLTools.ensureElementIsInSequence(element);
		sequence.addContent(sequence.indexOf(element), initializeAssign);
	}

}
