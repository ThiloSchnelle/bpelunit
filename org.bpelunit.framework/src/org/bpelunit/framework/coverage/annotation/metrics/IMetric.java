package org.bpelunit.framework.coverage.annotation.metrics;

import java.util.Hashtable;
import java.util.List;

import org.bpelunit.framework.coverage.exceptions.BpelException;
import org.bpelunit.framework.coverage.receiver.MarkerStatus;
import org.bpelunit.framework.coverage.result.statistic.IStatistic;
import org.jdom.Element;

/**
 * Die Schnittstelle repr�sentiert die Metriken.
 * 
 * @author Alex Salnikow
 * 
 */

public interface IMetric {

	/**
	 * 
	 * @return die Bezeichnung der Metrik
	 */
	public String getName();

	/**
	 * Liefert Pr�fixe von allen Marken dieser Metrik. Sie erm�glichen die
	 * Zuordnung der empfangenen Marken einer Metrik
	 * 
	 * @return Pr�fixe von allen Marken dieser Metrik
	 */
	public List<String> getMarkersId();

	/**
	 * Erzeugt Statistiken
	 * 
	 * @param allMarkers
	 *            alle einegf�gten Marken (von allen Metriken), nach dem Testen
	 * @return Statistik
	 */
	public IStatistic createStatistic(
			Hashtable<String, Hashtable<String, MarkerStatus>> allMarkers);

	/**
	 * Erh�lt die noch nicht modifizierte Beschreibung des BPELProzesses als
	 * XML-Element. Alle f�r die Instrumentierung ben�tigten Elemente der
	 * Prozessbeschreibung werden gespeichert
	 * 
	 * @param process
	 *            noch nicht modifiziertes BPEL-Prozess
	 */
	public void setOriginalBPELProcess(Element process);

	/**
	 * delegiert die Instrumentierungsaufgabe an eigenen Handler
	 * 
	 * @throws BpelException
	 */
	public void insertMarkers() throws BpelException;
}
