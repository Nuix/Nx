package com.nuix.innovation.enginewrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/***
 * Provides methods for generating Nuix diagnostics files.
 * @author Jason Wells
 */
public class NuixDiagnostics {
	private final static Logger logger = LogManager.getLogger(NuixDiagnostics.class);

	/***
	 * Saves a Nuix diagnostics zip file at the specified path.
	 * @param zipFile The zip file to save Nuix diagnostics into
	 */
	public static void saveDiagnosticsToFile(File zipFile){
		List<MBeanServer> beanServers = new ArrayList<MBeanServer>();
		beanServers.add(ManagementFactory.getPlatformMBeanServer());
		beanServers.addAll(MBeanServerFactory.findMBeanServer(null));
		for (MBeanServer mBeanServer : beanServers) {
			Set<ObjectName> objectNames = mBeanServer.queryNames(null, null);
			for (ObjectName beanName : objectNames) {
				if(beanName.toString().contains("DiagnosticsControl")){
					zipFile.mkdirs();
					try {
						mBeanServer.invoke(beanName,"generateDiagnostics",new Object[] {zipFile.getPath()},new String[] {"java.lang.String"});
						return;
					} catch (Exception e) {
						logger.error("Error saving diagnostics", e);
					}
				}
			}
		}
	}

	/***
	 * Saves a Nuix diagnostics zip file at the specified path.
	 * @param zipFile The zip file to save Nuix diagnostics into
	 */
	public static void saveDiagnosticsToFile(String zipFile) { saveDiagnosticsToFile(new File(zipFile)); }

	/***
	 * Convenience method for saving a diagnostics file to a directory.  Internally calls {@link #saveDiagnosticsToFile(File)}
	 * with a file path using the specified directory and a time stamped file name.
	 * @param directory The directory to save Nuix the diagnostics zip file to.
	 */
	public static void saveDiagnosticsToDirectory(File directory) {
		DateTime timeStamp = DateTime.now();
		String timeStampString = timeStamp.toString("yyyyMMddHHmmss");
		File zipFile = new File(directory,"NuixEngineDiagnostics-"+timeStampString+".zip");
		saveDiagnosticsToFile(zipFile);
	}

	/***
	 * Convenience method for saving a diagnostics file to a directory.  Internally calls {@link #saveDiagnosticsToFile(File)}
	 * with a file path using the specified directory and a time stamped file name.
	 * @param directory The directory to save Nuix the diagnostics zip file to.
	 */
	public static void saveDiagnosticsToDirectory(String directory) {
		saveDiagnosticsToDirectory(new File(directory));
	}
}
