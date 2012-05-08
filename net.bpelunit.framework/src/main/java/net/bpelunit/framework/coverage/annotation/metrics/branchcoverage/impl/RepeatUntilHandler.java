package net.bpelunit.framework.coverage.annotation.metrics.branchcoverage.impl;

import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.NAME_ATTR;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.createIfActivity;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.createIncreesAssign;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.createInitializeAssign;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.createSequence;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.ensureElementIsInSequence;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.getFirstEnclosedActivity;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.insertNewIntVariable;
import static net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools.isSequence;

import net.bpelunit.framework.coverage.CoverageConstants;
import net.bpelunit.framework.coverage.annotation.metrics.branchcoverage.BranchMetricHandler;
import net.bpelunit.framework.coverage.annotation.metrics.branchcoverage.IStructuredActivityHandler;
import net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.exprlang.ExpressionLanguage;
import net.bpelunit.framework.coverage.exceptions.BpelException;
import net.bpelunit.framework.coverage.receiver.MarkersRegisterForArchive;
import org.jdom.Element;


/**
 * Handler, der die Instrumentierung der
 * repeatUntil-Aktivitäten für die Zweigabdeckung übernehmen.
 * 
 * @author Alex Salnikow
 */

public class RepeatUntilHandler implements IStructuredActivityHandler {

	private MarkersRegisterForArchive markersRegistry;

	public RepeatUntilHandler(MarkersRegisterForArchive markersRegistry) {
		this.markersRegistry = markersRegistry;
	}

	/**
	 * Fügt Markierungen, die später durch Invoke-Aufrufe protokolliert werden,
	 * um die Ausführung der Zweige zu erfassen.
	 * 
	 * @param structured_activity
	 * @throws BpelException
	 */
	public void insertBranchMarkers(Element structured_activity)
			throws BpelException {
		branchFromConditionToActivity(structured_activity);
		branchFromActivityToCondition(structured_activity);
	}

	private void branchFromActivityToCondition(Element element)
			throws BpelException {
		Element activity = getFirstEnclosedActivity(element);
		if (element == null) {
			throw new BpelException(BpelException.MISSING_REQUIRED_ACTIVITY);
		}
		
		markersRegistry.registerMarker(BranchMetricHandler.insertLabelAfterAllActivities(activity));
	}

	private void branchFromConditionToActivity(Element element)
			throws BpelException {
		Element countVariable = insertNewIntVariable(null, null);
		Element initializeAssign = createInitializeAssign(countVariable);
		insert(initializeAssign, element);
		Element increesAssign = createIncreesAssign(countVariable);
		insertIncreesAssign(increesAssign, element);
		insertIfConstruct(element, countVariable);
	}

	/**
	 * Fügt Markierung für den Zweig von Aktivitäten zu der Condition. Dafür
	 * wird eine If-Anfrage eingefügt, die anhand der Zählvariable überprüft, ob
	 * die Aktivitäten in der Schleife ausgeführt wurden.
	 * 
	 * @param element
	 * @param countVariable
	 * @throws BpelException
	 */
	private void insertIfConstruct(Element element, Element countVariable)
			throws BpelException {
		Element activity = getFirstEnclosedActivity(element);
		if (activity == null) {
			throw new BpelException(BpelException.MISSING_REQUIRED_ACTIVITY);
		}
		
		if (!isSequence(activity)) {
			activity = ensureElementIsInSequence(activity);
		}
		
		Element ifElement = createIfActivity(ExpressionLanguage.getInstance(
				CoverageConstants.EXPRESSION_LANGUAGE).valueOf(
				countVariable.getAttributeValue(NAME_ATTR))
				+ "=1");
		Element sequence = createSequence();
		ifElement.addContent(sequence);
		activity.addContent(0, ifElement);
		markersRegistry.registerMarker(BranchMetricHandler.insertLabelBevorAllActivities(sequence));

	}

	/**
	 * Fügt ein Assign Element an letzter Stelle innerhalb des
	 * RepeatUntil-Konstrukts, das die Zaählvariable um eins erhöht und
	 * registriert damit die Ausführung der Schleife.
	 * 
	 * @param increaseAssign
	 * @param element
	 *            RepeatUntil-Element
	 * @throws BpelException
	 */
	private void insertIncreesAssign(Element increaseAssign, Element element)
			throws BpelException {
		Element activity = getFirstEnclosedActivity(element);
		if (activity == null) {
			throw new BpelException(BpelException.MISSING_REQUIRED_ACTIVITY);
		}
	
		if (!isSequence(activity)) {
			activity = ensureElementIsInSequence(activity);
		}
		activity.addContent(increaseAssign);

	}

	/**
	 * 
	 * @param initializeAssign
	 *            Assign-Element, das die Variable initialisiert.
	 * @param element
	 *            RepeatUntil-Element
	 */
	private void insert(Element initializeAssign, Element element) {
		Element sequence = ensureElementIsInSequence(element);
		sequence.addContent(sequence.indexOf(element), initializeAssign);
	}

}
