/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nuix.Item;
import nuix.SingleItemExporter;
import nuix.Utilities;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.nuix.nx.NuixConnection;

/***
 * This class assists in generating combined PDFs outside of Nuix using iText.
 * @author JasonWells
 *
 */
public class CombinedPdfExporter {
	/***
	 * This method generates a new PDF by concatenating several existing PDFs.
	 * @param destination The destination to where the new PDF should be written.
	 * @param sources List of source PDFs to combine, in order they should be combined in.
	 * @throws DocumentException Possibly thrown by iText
	 * @throws IOException Possibly thrown on IO error
	 */
	public static void mergeExistingPdfFiles(File destination, List<File> sources) throws DocumentException, IOException{
		Document document = new Document();
		PdfCopy copy = new PdfCopy(document,new FileOutputStream(destination));
		document.open();
		PdfReader reader = null;
		for(File sourceFile : sources){
			reader = new PdfReader(sourceFile.getPath());
			int totalPages = reader.getNumberOfPages();
			for(int i = 0;i < totalPages;i++){
				copy.addPage(copy.getImportedPage(reader,i+1));
			}
			copy.freeReader(reader);
			reader.close();
		}
		document.close();
	}
	
	private File tempDirectory = null;
	
	/***
	 * Gets the temporary directory used to house the per item temporary PDF files.
	 * @return The temporary directory
	 */
	public File getTempDirectory() {
		return tempDirectory;
	}

	/***
	 * Sets the temporary directory used to house the per item temporary PDF files.
	 * @param tempDirectory The temporary directory
	 */
	public void setTempDirectory(File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}
	
	/***
	 * Sets the temporary directory used to house the per item temporary PDF files.
	 * @param tempDirectory The temporary directory
	 */
	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = new File(tempDirectory);
	}

	/***
	 * Creates a new instance.
	 * @param tempDirectory The temporary directory which will be used to produce the per item PDFs.
	 */
	public CombinedPdfExporter(String tempDirectory){
		this(new File(tempDirectory));
	}
	
	/***
	 * Creates a new instance.
	 * @param tempDirectory The temporary directory which will be used to produce the per item PDFs.
	 */
	public CombinedPdfExporter(File tempDirectory){
		this.tempDirectory = tempDirectory;
	}
	
	/***
	 * Creates a single PDF from several different items by exporting a PDF for each item then concatenating them and finally cleaning up the temporary PDF files.
	 * them together using {@link #mergeExistingPdfFiles(File, List)}.  See Nuix documentation <a href="https://download.nuix.com/releases/desktop/stable/docs/en/scripting/api/nuix/ImagingConfigurable.html#setImagingOptions-java.util.Map-">here</a>
	 * and <a href="https://download.nuix.com/releases/desktop/stable/docs/en/scripting/api/index.html">here</a> for more information about supported settings.
	 * @param destination The destination path for the final combined PDF
	 * @param items A list of items to create a combined PDF for, in the order to combine them in.
	 * @param settings Optional settings map (can be null)
	 * @throws IOException Thrown if there is an IO error
	 * @throws DocumentException Thrown if iText has an error
	 */
	public void exportItems(String destination, List<Item> items, Map<String,Object> settings) throws IOException, DocumentException{
		exportItems(new File(destination),items,settings);
	}
	
	/***
	 * Creates a single PDF from several different items by exporting a PDF for each item then concatenating them and finally cleaning up the temporary PDF files.
	 * them together using {@link #mergeExistingPdfFiles(File, List)}.  See Nuix documentation <a href="https://download.nuix.com/releases/desktop/stable/docs/en/scripting/api/nuix/ImagingConfigurable.html#setImagingOptions-java.util.Map-">here</a>
	 * and <a href="https://download.nuix.com/releases/desktop/stable/docs/en/scripting/api/index.html">here</a> for more information about supported settings.
	 * @param destination The destination path for the final combined PDF
	 * @param items A list of items to create a combined PDF for, in the order to combine them in.
	 * @param settings Optional settings map (can be null)
	 * @throws IOException Thrown if there is an IO error
	 * @throws DocumentException Thrown if iText has an error
	 */
	public void exportItems(File destination, List<Item> items, Map<String,Object> settings) throws IOException, DocumentException{
		Utilities utilities = NuixConnection.getUtilities();
		SingleItemExporter pdfExporter = utilities.getPdfPrintExporter();
		List<File> tempPdfFiles = new ArrayList<File>();
		//Generate temp per item PDFs
		for(Item item : items){
			File tempPdfFile = new File(tempDirectory,item.getGuid()+".pdf");
			tempPdfFiles.add(tempPdfFile);
			if(settings != null){ pdfExporter.exportItem(item, tempPdfFile, settings); }
			else { pdfExporter.exportItem(item, tempPdfFile); }
		}
		mergeExistingPdfFiles(destination, tempPdfFiles);
		//Cleanup temp PDFs
		for(File tempPdfFile : tempPdfFiles){
			tempPdfFile.delete();
		}
	}
}
