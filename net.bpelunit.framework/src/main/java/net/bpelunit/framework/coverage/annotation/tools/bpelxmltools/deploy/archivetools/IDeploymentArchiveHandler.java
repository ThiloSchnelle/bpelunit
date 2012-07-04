package net.bpelunit.framework.coverage.annotation.tools.bpelxmltools.deploy.archivetools;


import java.io.File;
import java.util.Set;

import net.bpelunit.framework.coverage.exceptions.ArchiveFileException;
import net.bpelunit.framework.coverage.exceptions.BpelException;
import org.jdom.Document;

/**
 * Interface dient als Schnittstelle für den Zugriff auf die (deployment)
 * Archivedateien, die spezifisch für die BPELEngine sind.
 * 
 * @author Alex Salnikow
 * 
 */

public interface IDeploymentArchiveHandler {

	/**
	 * Erzeugt eine Kopie des Archivs, auf der die Instrumentierung durchgeführt wird.
	 * 
	 * @param archiv Deploymentarchive
	 * @return ArchivCopy
	 * @throws ArchiveFileException
	 */
	String createArchivecopy(String archiv) throws ArchiveFileException;

	/**
	 * 
	 * @return die Namen aller BPEL-Dateien, die im Archiv enthalten sind.
	 */
	Set<String> getAllBPELFileNames();

	/**
	 * 
	 * @param fileName Name der BPEL-Datei
	 * @return BPEL-Prozessbeschreibung als XML-Dokumnet
	 * @throws BpelException
	 */
	Document getDocument(String fileName) throws BpelException;

	/**
	 * Schreibt den BPEL-Prozess in Form eines XML-Dokumentes in den Archiv
	 * @param doc BPEL-Prozess als XML-Dokument
	 * @param fileName Name der BPEL-Datei
	 * @throws ArchiveFileException
	 */
	void writeDocument(Document doc, String fileName) throws ArchiveFileException;

	/**
	 * Fügt in den Archive WSDL-Datei und registriert sie, falls nötig
	 * @param wsdlFile
	 * @throws ArchiveFileException
	 */
	void addWSDLFile(File wsdlFile) throws ArchiveFileException;

	/**
	 * Gibt die reservierten Ressourcen (Streams) frei. 
	 *
	 */
	void closeArchive();

}
