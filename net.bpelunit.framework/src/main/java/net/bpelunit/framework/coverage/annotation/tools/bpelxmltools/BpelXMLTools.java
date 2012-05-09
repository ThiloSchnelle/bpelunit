package net.bpelunit.framework.coverage.annotation.tools.bpelxmltools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bpelunit.framework.coverage.CoverageConstants;
import net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.exprlang.ExpressionLanguage;
import net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.exprlang.XpathLanguage;
import net.bpelunit.util.JDomUtil;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Die Klasse stellt zur Verf�gung Methoden, mit denen man neue Elemente der
 * BPEL Sprache erzeugen und in den BPEL-Prozess einf�gen.
 * 
 * @author Alex Salnikow
 */
public final class BpelXMLTools {

	private BpelXMLTools() {
	}
	
	public static final Namespace NAMESPACE_BPEL_1_1 = Namespace
			.getNamespace("http://schemas.xmlsoap.org/ws/2003/03/business-process/");
	public static final Namespace NAMESPACE_BPEL_2_0 = Namespace
			.getNamespace("http://docs.oasis-open.org/wsbpel/2.0/process/executable");

	private static int sequenceCount = 0;
	private static int flowCount = 0;
	private static int invokeCount = 0;

	/* Elements from namespace of BPEL */
	public static final String PROCESS_ELEMENT = "process";

	public static final String VARIABLE_ELEMENT = "variable";

	private static final String VARIABLES_ELEMENT = "variables";

	public static final String ASSIGN_ELEMENT = "assign";

	public static final String COPY_ELEMENT = "copy";

	public static final String FROM_ELEMENT = "from";

	public static final String TO_ELEMENT = "to";

	public static final String LITERAL_ELEMENT = "literal";

	public static final String IF_ELEMENT = "if";

	public static final String CONDITION_ELEMENT = "condition";

	public static final String ELSE_ELEMENT = "else";

	public static final String SOURCES_ELEMENT = "sources";
	public static final String TARGETS_ELEMENT = "targets";
	public static final String SOURCE_ELEMENT = "source";
	public static final String TARGET_ELEMENT = "target";

	public static final String ELSE_IF_ELEMENT = "elseif";

	public static final String PARTNERLINKS_ELEMENT = "partnerLinks";

	public static final String PARTNERLINK_ELEMENT = "partnerLink";

	public static final String PARTNERLINK_ATTRIBUTE = PARTNERLINK_ELEMENT;

	public static final String PARTNERLINKTYPE_ATTRIBUTE = "partnerLinkType";

	public static final String OPERATION_ATTRIBUTE = "operation";

	public static final String PORTTYPE_ATTRIBUTE = "portType";

	public static final String CATCH_ELEMENT = "catch";

	public static final String CATCHALL_ELEMENT = "catchAll";

	public static final String INT_VARIABLE_TYPE = "xsd:int";

	public static final String STRING_VARIABLE_TYPE = "xsd:string";

	public static final String FAULT_HANDLERS = "faultHandlers";

	public static final String COMPENSATION_HANDLER = "compensationHandler";
	public static final String SWITCH_CASE_ELEMENT = "case";
	public static final String SWITCH_OTHERWISE_ELEMENT = "otherwise";

	/* Attributes of BPEL */

	public static final String FOREACH_PARALLEL_ATTR = "parallel";
	public static final String CREATE_INSTANCE_ATTR = "createInstance";

	public static final String FOREACH_COUNTERNAME_ATTR = "counterName";

	public static final String FOREACH_COUNTER_STARTVALUE_ATTR = "startCounterValue";

	public static final String FOREACH_COUNTER_FINALVALUE_ATTR = "finalCounterValue";

	public static final String FOREACH_PARALLEL_ATTR_VALUE_YES = "yes";

	public static final String INPUTVARIABLE_ATTR = "inputVariable";

	public static final String PARTNERROLE_ATTR_AND_ELEMENT = "partnerRole";

	public static final String PART_ATTR = "part";

	public static final String VARIABLE_ATTR = "variable";

	public static final String TYPE_ATTR = "type";

	public static final String NAME_ATTR = "name";

	public static final String MESSAGETYPE_ATTR = "messageType";

	public static final String EXPRESSION_LANGUAGE_ATTR = "expressionLanguage";

	private static final String PREFIX_FOR_NEW_VARIABLE = "_zyx";

	private static int count = 0;

	// TODO XXX no public vars and especially not static!
	private static Element processElement;

	public static synchronized int incrementCounter() {
		count++;
		
		return count;
	}
	
	public static synchronized void resetCounter() {
		count = 0;
	}
	
	public static Namespace getProcessNamespace() {
		return getProcessElement().getNamespace();
	}

	public static String createVariableName() {
		return PREFIX_FOR_NEW_VARIABLE + (count++);
	}

	public static Element createVariable(String name, String messageType,
			String type) {
		String newName = name;
		if (newName == null) {
			newName = createVariableName();
		}
		Element variable = createBPELElement(BpelXMLTools.VARIABLE_ELEMENT);
		if (type != null) {
			variable.setAttribute(TYPE_ATTR, type);
		}
		if (messageType != null) {
			variable.setAttribute(MESSAGETYPE_ATTR, messageType);
		}
		variable.setAttribute(BpelXMLTools.NAME_ATTR, newName);
		return variable;
	}

	public static Element insertNewStringVariable(String variableName,
			Element scope) {
		String realVariableName = variableName;
		if (realVariableName == null) {
			realVariableName = createVariableName();
		}
		Element variable = new Element(VARIABLE_ELEMENT, getProcessNamespace());
		variable.setAttribute(NAME_ATTR, realVariableName);
		variable.setAttribute(TYPE_ATTR, STRING_VARIABLE_TYPE);
		insertVariable(variable, scope);
		return variable;
	}

	/**
	 * Erzeugt ein Variable-Element ohne es in BPEL einzuf�gen.
	 * 
	 * @param scope
	 *            kann null sein, dann wird die Variable in den globelen
	 *            Prozess-Scope eingef�gt.
	 * @param name
	 *            Variablenname
	 * @return variable-Element
	 */
	public static Element insertNewIntVariable(Element scope, String name) {
		String realName = name;
		if (realName == null) {
			realName = createVariableName();
		}
		Element variable = createBPELElement(VARIABLE_ELEMENT);
		variable.setAttribute(NAME_ATTR, realName);
		variable.setAttribute(TYPE_ATTR, INT_VARIABLE_TYPE);
		insertVariable(variable, scope);
		return variable;
	}

	/**
	 * F�gt Variable in dem Scope ein. Wenn ein Varables-Element fehlt, dann
	 * wird ein hinzugef�gt.
	 * 
	 * @param variable
	 * @param scope
	 */
	public static void insertVariable(Element variable, Element scope) {
		Element realScope = scope;
		if (realScope == null) {
			realScope = getProcessElement();
		}
		Element variables = realScope.getChild(VARIABLES_ELEMENT,
				getProcessNamespace());
		if (variables == null) {
			variables = new Element(VARIABLES_ELEMENT, getProcessNamespace());
			realScope.addContent(0, variables);
		}
		List<Element> allVariables = JDomUtil.getChildren(variables,
				VARIABLE_ELEMENT, getProcessNamespace());
		boolean exist = false;
		String variableName = variable.getAttributeValue(NAME_ATTR);
		for (Iterator<Element> iter = allVariables.iterator(); iter.hasNext();) {
			Element element = iter.next();
			if (element.getAttributeValue(NAME_ATTR).equals(variableName)) {
				exist = true;
				break;
			}
		}
		if (!exist) {
			variables.addContent(variable);
		}
	}

	/**
	 * Schlie�t das Element in eine Sequence ein.
	 * 
	 * @param activity
	 *            : Element, das in eine Sequence eingeschlo�en werden soll.
	 * @return Umschlie�ende Sequence-Element
	 */
	public static Element encloseInSequence(Element activity) {
		Element parent = activity.getParentElement();
		int index = parent.indexOf(activity);
		Element sequence = createSequence();
		sequence.addContent(activity.detach());
		parent.addContent(index, sequence);
		return sequence;
	}

	/**
	 * �berpr�ft, ob das Element in Sequence eingeschlo�en ist. Wenn nicht, dann
	 * wird es in ein neues Sequence-Element eingeschlo�en.
	 * 
	 * @param activity
	 * @return Sequence-Element
	 */
	public static Element ensureElementIsInSequence(Element activity) {
		Element parent = activity.getParentElement();
		// TODO Check for equal namespace
		if (parent.getName().equals(StructuredActivities.SEQUENCE_ACTIVITY)) {
			return parent;
		} else {
			return encloseInSequence(activity);
		}
	}

	/**
	 * �berpr�ft, ob das Element in Sequence eingeschlo�en oder selbst Sequence
	 * ist. Wenn nicht, dann wird es in ein neues Sequence-Element
	 * eingeschlo�en.
	 * 
	 * @param activity
	 * @return Sequence-Element
	 */
	public static boolean isSequence(Element activity) {
		return activity.getName()
				.equals(StructuredActivities.SEQUENCE_ACTIVITY);
	}

	/**
	 * Sucht bei dem �bergebenen Element innerhalb seiner direkten
	 * Kind-Elementen nach der ersten Aktivit�t und gibt diese zur�ck.
	 * 
	 * @param element
	 * @return Das erste Kind-Element, das eine Aktivit�t ist. Falls nicht
	 *         vorhanden, dann null;
	 */
	public static Element getFirstEnclosedActivity(Element element) {
		Element activity = null;
		List<Element> children = JDomUtil.getElementsInContent(element);
		for (Element child : children) {
			if (isActivity(child)) {
				activity = child;
				break;
			}
		}
		return activity;
	}

	public static boolean canCreateInstance(Element activity) {
		String value = activity.getAttributeValue(CREATE_INSTANCE_ATTR, "no");
		
		return "yes".equals(value);
	}

	/**
	 * Schlie�t das Element in eine Flow ein.
	 * 
	 * @param activity
	 *            : Element, das in eine Flow eingeschlo�en werden soll.
	 * @return Umschlie�ende Flow-Element
	 */
	public static Element encloseElementInFlow(Element activity) {
		Element parent = activity.getParentElement();
		int index = parent.indexOf(activity);
		Element flow = createBPELElement(StructuredActivities.FLOW_ACTIVITY);
		activity.detach();
		flow.addContent(activity);
		parent.addContent(index, flow);
		return flow;
	}

	/**
	 * �berpr�ft, ob das Element selbst Flow ist. Wenn nicht, dann wird es in
	 * ein neues Flow-Element eingeschlo�en.
	 * 
	 * @param activity
	 * @return Flow-Element
	 */
	public static Element ensureElementIsInFlow(Element activity) {

		Element parent = activity.getParentElement();
		if (!parent.getName().equals(StructuredActivities.FLOW_ACTIVITY)) {
			return encloseElementInFlow(activity);
		}
		return activity;
	}

	public static boolean isFlow(Element activity) {
		return activity.getName().equals(StructuredActivities.FLOW_ACTIVITY);
	}

	/**
	 * Erzeugt ein Sequence-Element
	 * 
	 * @return sequence-Element
	 */
	public static Element createSequence() {
		sequenceCount++;
		return new Element(StructuredActivities.SEQUENCE_ACTIVITY,
				getProcessNamespace());
	}

	public static boolean isStructuredActivity(Element activity) {
		return StructuredActivities.isStructuredActivity(activity);
	}

	public static boolean isBasicActivity(Element activity) {
		return BasicActivities.isBasisActivity(activity);
	}

	public static boolean isActivity(Element element) {
		return isBasicActivity(element) || isStructuredActivity(element);
	}

	/**
	 * Erzeugt ein Assign-Element f�r eine Count-Variable und setzt auf 0.
	 * 
	 * @param countVariable
	 * @return Assign-Element
	 */
	public static Element createInitializeAssign(Element countVariable) {
		Element assign = createBPELElement(ASSIGN_ELEMENT);
		Element copy = createBPELElement(COPY_ELEMENT);
		Element from = createBPELElement(FROM_ELEMENT);
		Element to = createBPELElement(TO_ELEMENT);
		Element literal = createBPELElement(LITERAL_ELEMENT);
		literal.setText("0");
		from.addContent(literal);
		to.setAttribute(VARIABLE_ATTR, countVariable
				.getAttributeValue(NAME_ATTR));
		copy.addContent(from);
		copy.addContent(to);
		assign.addContent(copy);
		return assign;
	}

	/**
	 * Erzeugt ein Assign-Element f�r die Erh�hung der Count-Variable um 1.
	 * 
	 * @param countVariable
	 * @return Assign-Element
	 */
	public static Element createIncreesAssign(Element countVariable) {
		Element assign = createBPELElement(ASSIGN_ELEMENT);
		Element copy = createBPELElement(COPY_ELEMENT);
		Element from = createBPELElement(FROM_ELEMENT);
		Element to = createBPELElement(TO_ELEMENT);
		from.setText(ExpressionLanguage.getInstance(
				CoverageConstants.EXPRESSION_LANGUAGE).valueOf(
				countVariable.getAttributeValue(NAME_ATTR))
				+ " + 1");
		to.setAttribute(VARIABLE_ATTR, countVariable
				.getAttributeValue(NAME_ATTR));
		copy.addContent(from);
		copy.addContent(to);
		assign.addContent(copy);
		return assign;
	}

	/**
	 * F�gt in das If-Element Else-Zweig ein.
	 * 
	 * @param element
	 *            - If-Element
	 * @return Else-Element
	 */
	public static Element insertElseBranch(Element element) {
		Element elseElement = createBPELElement(ELSE_ELEMENT);
		// elseElement.addContent(BpelXMLTools.createSequence());
		element.addContent(elseElement);
		return elseElement;
	}

	public static Element createIfActivity(String conditionContent) {
		Element ifElement = createBPELElement(IF_ELEMENT);
		Element condition = createBPELElement(CONDITION_ELEMENT);
		condition.setAttribute("expressionLanguage",
				XpathLanguage.LANGUAGE_SPEZIFIKATION);
		condition.setText(conditionContent);
		ifElement.addContent(condition);
		return ifElement;
	}

	public static Element createAssign(Element from, Element to) {
		Element assign = createBPELElement(ASSIGN_ELEMENT);
		addCopyElement(assign, from, to);
		return assign;
	}

	public static void addCopyElement(Element assign, Element from, Element to) {
		Element copy = createBPELElement(COPY_ELEMENT);
		copy.addContent(from);
		copy.addContent(to);
		assign.addContent(copy);
	}

	public static void sysout(Element element) {
		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlOutputter.output(element, System.out);
		} catch (IOException e) {
			// TODO How to handle?
		}
	}

	public static Element getSurroundScope(Content content) {
		Element scope = null;
		Element parent = content.getParentElement();
		String name;
		while (scope == null && parent != null) {
			name = parent.getName();
			if (name.equals(StructuredActivities.SCOPE_ACTIVITY)
					|| name.equals("process")) {
				scope = parent;
				break;
			} else {
				parent = parent.getParentElement();
			}
		}
		return scope;
	}

	public static Element createBPELElement(String name) {
		if (name.equals(StructuredActivities.FLOW_ACTIVITY)) {
			flowCount++;
		}

		if (name.equals(BasicActivities.INVOKE_ACTIVITY)) {
			invokeCount++;
		}
		
		return new Element(name, getProcessNamespace());
	}

	public static boolean isFaultHandlers(Element element) {
		return element.getName().equals(FAULT_HANDLERS);
	}

	public static boolean isCompensationHandler(Element element) {
		return element.getName().equals(COMPENSATION_HANDLER);
	}

	public static List<Element> getCatchBlocks(Element faultHandler) {
		List<Element> catchBlocks = new ArrayList<Element>();
		List<Element> children = JDomUtil.getChildren(faultHandler,
				CATCH_ELEMENT, getProcessNamespace());
		Iterator<Element> iter = children.iterator();
		while (iter.hasNext()) {
			catchBlocks.add(iter.next());
		}
		Element catchAll = faultHandler.getChild(CATCHALL_ELEMENT,
				getProcessNamespace());
		if (catchAll != null) {
			catchBlocks.add(catchAll);
		}
		return catchBlocks;
	}

	public static Element insertOtherwiseBranch(Element element) {
		Element otherwiseElement = createBPELElement(SWITCH_OTHERWISE_ELEMENT);
		element.addContent(otherwiseElement);
		return otherwiseElement;
	}

	public static void setProcessElement(Element processElement) {
		BpelXMLTools.processElement = processElement;
	}

	public static Element getProcessElement() {
		return processElement;
	}

}
