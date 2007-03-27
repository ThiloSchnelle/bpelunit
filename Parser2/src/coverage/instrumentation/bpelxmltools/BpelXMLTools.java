package coverage.instrumentation.bpelxmltools;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ContentFilter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import coverage.instrumentation.bpelxmltools.exprlang.impl.XpathLanguage;

/**
 * Die Klasse stellt zur Verf�gung Methoden, mit denen man neue Elemente der
 * BPEL Sprache erzeugen und in den BPEL-Prozess einf�gen.
 * 
 * @author Alex Salnikow
 */
public class BpelXMLTools {

	public static final Namespace NAMESPACE_BPEL_2 = Namespace
			.getNamespace("http://schemas.xmlsoap.org/ws/2003/03/business-process/");

	public static final String PROCESS_ELEMENT = "process";

	private static int count = 0;

	public static String namespacePrefix;

	public static Element process_element;

	public static final String VARIABLE_TAG = "variable";

	public static final String ATTRIBUTE_VARIABLE = "variable";

	private static final String VARIABLES_TAG = "variables";

	private static final String ASSIGN_TAG = "assign";

	private static final String COPY_TAG = "copy";

	public static final String FROM_TAG = "from";

	public static final String TO_TAG = "to";

	public static final String LITERAL_TAG = "literal";

	private static final String INT_VARIABLE_TYPE = "xsd:int";

	private static final String STRING_VARIABLE_TYPE = "xsd:string";

	// TODO prefix

	private static final String ATTRIBUTE_TYPE = "type";

	public static final String ATTRIBUTE_NAME = "name";

	private static final String VARIABLE_NAME = "_ZXYYXZ_";

	private static final String ELSE_ELEMENT = "else";

	private static final String IF_TAG = "if";

	private static final String CONDITION_TAG = "condition";

	public static final String EXPRESSION_LANGUAGE_ATTRIBUTE = "expressionLanguage";

	public static Namespace getBpelNamespace() {
		return process_element.getNamespace();
	}

	/**
	 * Schlie�t das Element in eine Sequence ein.
	 * 
	 * @param activity:
	 *            Element, das in eine Sequence eingeschlo�en werden soll.
	 * @return Umschlie�ende Sequence-Element
	 */
	public static Element encloseInSequence(Element activity) {
		Element parent = activity.getParentElement();
		int index = parent.indexOf(activity);
		Element sequence = createSequence();
		sequence.addContent(activity.detach());
		parent.addContent(index, sequence);
		activity = sequence;
		return activity;
	}

	/**
	 * �berpr�ft, ob das Element in Sequence eingeschlo�en oder selbst Sequence
	 * ist. Wenn nicht, dann wird es in ein neues Sequence-Element
	 * eingeschlo�en.
	 * 
	 * @param activity
	 * @return Sequence-Element
	 */
	public static Element ensureElementIsInSequence(Element activity) {
		Element parent = activity.getParentElement();
		if (parent.getName().equals(StructuredActivity.SEQUENCE_ACTIVITY)) {
			activity = parent;
		} else {
			activity = encloseInSequence(activity);
		}
		return activity;
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
		return activity.getName().equals(StructuredActivity.SEQUENCE_ACTIVITY);
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
		List children = element.getContent(new ContentFilter(
				ContentFilter.ELEMENT));
		Element child;
		for (int i = 0; i < children.size(); i++) {
			child = (Element) children.get(i);
			if (isActivity(child)) {
				activity = child;
				break;
			} else if (isScope(child)) {
				activity = getFirstEnclosedActivity(child);
			}
		}
		return activity;
	}

	public static boolean isScope(Element child) {
		if (child.getName().equals(StructuredActivity.SCOPE_ACTIVITY)) {
			return true;
		}
		return false;
	}

	/**
	 * Schlie�t das Element in eine Flow ein.
	 * 
	 * @param activity:
	 *            Element, das in eine Flow eingeschlo�en werden soll.
	 * @return Umschlie�ende Flow-Element
	 */
	public static Element encloseElementInFlow(Element activity) {
		Element parent = activity.getParentElement();
		int index = parent.indexOf(activity);
		Element flow = new Element(StructuredActivity.FLOW_ACTIVITY,
				getBpelNamespace());
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
		if (!parent.getName().equals(StructuredActivity.FLOW_ACTIVITY)) {
			activity = encloseElementInFlow(activity);
		}
		return activity;
	}

	public static boolean isFlow(Element activity) {
		return activity.getName().equals(StructuredActivity.FLOW_ACTIVITY);
	}

	/**
	 * Erzeugt ein Sequence-Element
	 * 
	 * @return
	 */
	public static Element createSequence() {
		return new Element(StructuredActivity.SEQUENCE_ACTIVITY,
				getBpelNamespace());
	}

	public static boolean isStructuredActivity(Element activity) {
		return StructuredActivity.isStructuredActivity(activity);
	}

	public static boolean isBasicActivity(Element activity) {
		return BasisActivity.isBasisActivity(activity);
	}

	public static boolean isActivity(Element element) {
		return isBasicActivity(element) || isStructuredActivity(element);
	}

	/**
	 * Erzeugt ein Variable-Element ohne es in BPEL einzuf�gen.
	 * 
	 * @param document
	 * @return
	 */
	public static Element createIntVariable() {
		Element variable = new Element(VARIABLE_TAG, getBpelNamespace());
		variable.setAttribute(ATTRIBUTE_NAME, createVariableName());
		variable.setAttribute(ATTRIBUTE_TYPE, INT_VARIABLE_TYPE);
		return variable;
	}

	public static Element createStringVariable() {
		Element variable = new Element(VARIABLE_TAG, getBpelNamespace());
		variable.setAttribute(ATTRIBUTE_NAME, createVariableName());
		variable.setAttribute(ATTRIBUTE_TYPE, STRING_VARIABLE_TYPE);
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
		Element variables = scope.getChild(VARIABLES_TAG, getBpelNamespace());
		if (variables == null) {
			variables = new Element(VARIABLES_TAG, getBpelNamespace());
			scope.addContent(0, variables);
		}
		List allVariables = variables.getChildren("variable", BpelXMLTools
				.getBpelNamespace());
		boolean exist = false;
		String variableName = variable.getAttributeValue(ATTRIBUTE_NAME);
		for (Iterator iter = allVariables.iterator(); iter.hasNext();) {
			Element element = (Element) iter.next();
			if (element.getAttributeValue(ATTRIBUTE_NAME).equals(variableName)) {
				exist = true;
				break;
			}
		}
		if (!exist)
			variables.addContent(variable);
	}

	public static void insertVariable(Element variable) {
		insertVariable(variable, process_element);
	}

	/**
	 * Erzeugt ein Assign-Element f�r eine Count-Variable und setzt auf 0.
	 * 
	 * @param countVariable
	 * @return Assign-Element
	 */
	public static Element createInitializeAssign(Element countVariable) {
		Element assign = new Element(ASSIGN_TAG, getBpelNamespace());
		Element copy = new Element(COPY_TAG, getBpelNamespace());
		Element from = new Element(FROM_TAG, getBpelNamespace());
		Element to = new Element(TO_TAG, getBpelNamespace());
		Element literal = new Element(LITERAL_TAG, getBpelNamespace());
		literal.setText("0");
		from.addContent(literal);
		to.setAttribute(ATTRIBUTE_VARIABLE, countVariable
				.getAttributeValue(ATTRIBUTE_NAME));
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
		Element assign = new Element(ASSIGN_TAG, getBpelNamespace());
		Element copy = new Element(COPY_TAG, getBpelNamespace());
		Element from = new Element(FROM_TAG, getBpelNamespace());
		Element to = new Element(TO_TAG, getBpelNamespace());
		from.setText(XpathLanguage.valueOf(countVariable.getAttributeValue(ATTRIBUTE_NAME))
				+ " + 1");
		to.setAttribute(ATTRIBUTE_VARIABLE, countVariable
				.getAttributeValue(ATTRIBUTE_NAME));
		copy.addContent(from);
		copy.addContent(to);
		assign.addContent(copy);
		return assign;
	}

	public static String createVariableName() {
		return VARIABLE_NAME + (count++);
	}

	/**
	 * F�gt in das If-Element Else-Zweig ein.
	 * 
	 * @param element -
	 *            If-Element
	 * @return Else-Element
	 */
	public static Element insertElseBranch(Element element) {
		Element elseElement = new Element(ELSE_ELEMENT, getBpelNamespace());
		elseElement.addContent(BpelXMLTools.createSequence());
		element.addContent(elseElement);
		return elseElement;
	}

	public static Element createIfActivity(String conditionContent) {
		Element if_element = new Element(IF_TAG, getBpelNamespace());
		Element condition = new Element(CONDITION_TAG, getBpelNamespace());
		condition.setAttribute("expressionLanguage",
				XpathLanguage.LANGUAGE_SPEZIFIKATION);
		condition.setText(conditionContent);
		if_element.addContent(condition);
		return if_element;
	}

	public static Element createAssign(Element from, Element to) {
		Element assign = new Element(ASSIGN_TAG, getBpelNamespace());
		addCopyElement(assign, from, to);
		return assign;
	}

	public static void addCopyElement(Element assign, Element from, Element to) {
		Element copy = new Element(COPY_TAG, getBpelNamespace());
		copy.addContent(from);
		copy.addContent(to);
		assign.addContent(copy);
	}

	public static void sysout(Element element) {
		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlOutputter.output(element, System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sysout(Document doc) {
		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try {
			xmlOutputter.output(doc, System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Element getSurroundScope(Content content) {
		Element scope=null;
		Element parent=content.getParentElement();
		while(scope==null&&parent!=null){
			if(parent.getName().equals("scope")||parent.getName().equals("process")){
				scope=parent;
				break;
			}else{
				parent=parent.getParentElement();
			}
		}
		return scope;
	}

}
