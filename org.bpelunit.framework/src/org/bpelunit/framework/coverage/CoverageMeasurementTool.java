package org.bpelunit.framework.coverage;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.bpelunit.framework.control.deploy.activebpel.ActiveBPELDeployer;
import org.bpelunit.framework.control.ext.IBPELDeployer;
import org.bpelunit.framework.control.ext.ISOAPEncoder;
import org.bpelunit.framework.coverage.annotation.Instrumenter;
import org.bpelunit.framework.coverage.annotation.MetricsManager;
import org.bpelunit.framework.coverage.annotation.metrics.IMetric;
import org.bpelunit.framework.coverage.annotation.metrics.activitycoverage.ActivityMetricHandler;
import org.bpelunit.framework.coverage.annotation.tools.bpelxmltools.BpelXMLTools;
import org.bpelunit.framework.coverage.annotation.tools.bpelxmltools.deploy.archivetools.IDeploymentArchiveHandler;
import org.bpelunit.framework.coverage.annotation.tools.bpelxmltools.deploy.archivetools.impl.ActiveBPELDeploymentArchiveHandler;
import org.bpelunit.framework.coverage.exceptions.ArchiveFileException;
import org.bpelunit.framework.coverage.exceptions.BpelException;
import org.bpelunit.framework.coverage.exceptions.CoverageMeasurmentException;
import org.bpelunit.framework.coverage.receiver.CoverageMessageReceiver;
import org.bpelunit.framework.coverage.receiver.MarkersRegisterForArchive;
import org.bpelunit.framework.coverage.result.statistic.IFileStatistic;
import org.bpelunit.framework.exception.ConfigurationException;
import org.jdom.Document;
import org.jdom.filter.ElementFilter;

/**
 * 
 * 
 * @author Alex Salnikow
 * 
 */
public class CoverageMeasurementTool implements ICoverageMeasurmentTool {
	private Logger logger = Logger.getLogger(this.getClass());



	private boolean failure;

	private boolean error;

	private CoverageMessageReceiver messageReceiver = null;

	private MarkersRegisterForArchive markersRegistry = null;

	private MetricsManager metricManager;

	private String pathToWSDL = null;
	
	private String errorStatus=null;

	public CoverageMeasurementTool() {
		metricManager = new MetricsManager();
		markersRegistry = new MarkersRegisterForArchive(metricManager);
		messageReceiver = new CoverageMessageReceiver(markersRegistry);
	}

	// **********************Konfiguration*********************************

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#configureMetrics(java.util.Map)
	 */
	public void configureMetrics(Map<String, List<String>> configMap)
			throws ConfigurationException {
		if (configMap == null) {
			setErrorStatus("Configuration error.");
			throw new ConfigurationException(
					"Coverage metrics are not configured.");
		}
		Iterator<String> iter = configMap.keySet().iterator();
		String key;
		IMetric metric;
		while (iter.hasNext()) {
			key = iter.next();
			metric = MetricsManager.createMetric(key, configMap.get(key),
					markersRegistry);
			if (metric != null)
				metricManager.addMetric(metric);
		}
	}

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#setSOAPEncoder(org.bpelunit.framework.control.ext.ISOAPEncoder)
	 */
	public void setSOAPEncoder(ISOAPEncoder encoder) {
		messageReceiver.setSOAPEncoder(encoder);
	}

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#setPathToWSDL(java.lang.String)
	 */
	public void setPathToWSDL(String wsdl) {
		logger.info("PATHTOWSDL=" + wsdl);
		pathToWSDL = wsdl;
		messageReceiver.setPathToWSDL(wsdl);
	}

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#getEncodingStyle()
	 */
	public String getEncodingStyle() {
		return messageReceiver.getEncodingStyle();
	}

	// **********************Instrumentierung********************************

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#prepareArchiveForCoverageMeasurement(java.lang.String, java.lang.String, org.bpelunit.framework.control.ext.IBPELDeployer)
	 */
	public String prepareArchiveForCoverageMeasurement(String pathToArchive,
			String archiveFile, IBPELDeployer deployer)
			throws CoverageMeasurmentException {
		logger.info("Instrumentation is started.");
		if (pathToWSDL == null) {
			setErrorStatus("Path to WSDL file for coverage measurment failure");
			throw new CoverageMeasurmentException(
					"Path to WSDL file for coverage measurment failure");
		}
		IDeploymentArchiveHandler archiveHandler = null;
		if (deployer instanceof ActiveBPELDeployer) {
			archiveHandler = new ActiveBPELDeploymentArchiveHandler();
		}
		// else if //point for extention for other BPEL Engines

		if (archiveHandler == null) {
			setErrorStatus(deployer.toString()
					+ " is by coverage tool not supported");
			throw new CoverageMeasurmentException(deployer.toString()
					+ " is by coverage tool not supported");
		}
		String newArchiveFile = archiveHandler.createArchivecopy(FilenameUtils
				.concat(pathToArchive, archiveFile));
		prepareLoggingService(archiveHandler);
		executeInstrumentationOfBPEL(archiveHandler);
		archiveHandler.closeArchive();
		logger.info("Instrumentation is complete.");
		return newArchiveFile;
	}

	/**
	 * Startet die Instrumentierung aller BPEL-Dateien, die im Archive sind.
	 * 
	 * @param archiveHandler
	 * @throws BpelException
	 * @throws ArchiveFileException
	 */
	private void executeInstrumentationOfBPEL(
			IDeploymentArchiveHandler archiveHandler) throws BpelException,
			ArchiveFileException {

		Instrumenter instrumenter = new Instrumenter();
		Document doc;
		BpelXMLTools.count = 0;
		int count = 0;
		String bpelFile;
		ActivityMetricHandler.targetscount = 0;
		for (Iterator<String> iter = archiveHandler.getAllBPELFileNames()
				.iterator(); iter.hasNext();) {
			bpelFile = iter.next();
			markersRegistry.addRegistryForFile(bpelFile);
			doc = archiveHandler.getDocument(bpelFile);
			Iterator iter2 = doc.getDescendants(new ElementFilter("link", doc
					.getRootElement().getNamespace()));
			while (iter2.hasNext()) {
				iter2.next();
				count++;

			}
			doc = instrumenter.insertAnnotations(doc, metricManager);
			archiveHandler.writeDocument(doc, bpelFile);

			logger.info("Instrumentation of BPEL-File "+bpelFile+" is copmplete.");

		}

	}

	/**
	 * F�gt die WSDL-Datei f�r Service, der die Log-Eintr�ge empf�ngt. Diese
	 * Log-Eintr�ge dokumentieren die Ausf�hrung bestimmter Codeteile.
	 * 
	 * @param archiveHandler
	 * @param simulatedUrl
	 * @throws ArchiveFileException
	 */
	private void prepareLoggingService(IDeploymentArchiveHandler archiveHandler)
			throws ArchiveFileException {
		archiveHandler.addWSDLFile(new File(pathToWSDL));
	}

	// **********************Testlauf*********************************



	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#setErrorStatus(java.lang.String)
	 */
	public void setErrorStatus(String message) {
		logger.info(message);
		errorStatus=message;
		error = true;
	}

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#setCurrentTestCase(java.lang.String)
	 */
	public void setCurrentTestCase(String testCase) {
		messageReceiver.setCurrentTestcase(testCase);
	}

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#putMessage(java.lang.String)
	 */
	public synchronized void putMessage(String body) {
		messageReceiver.putMessage(body);
		notifyAll();
	}
	
	
	
//	 **********************Ergebnisse*********************************

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#getStatistics()
	 */
	public List<IFileStatistic> getStatistics() {
		if(error){
			return null;
		}
		return markersRegistry.getStatistics();
	}

	/* (non-Javadoc)
	 * @see org.bpelunit.framework.coverage.ICoverageMeasurmentTool#getStatus()
	 */
	public String getErrorStatus() {
		return errorStatus;
	}
}
