/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.sourceitem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import com.nuix.nx.NuixConnection;

import nuix.SourceItem;
import nuix.SourceItemFactory;

/***
 * A helper class for visiting all items recursively in a tree of source items.
 * @author Jason Wells
 *
 */
public class SourceItemVisitor {
	private SourceItemVisitCallback visitCallback;
	
	/***
	 * Provides a callback which will be called once for each source item located.
	 * @param callback A callback which will be invoked each time a source item is visited.
	 */
	public void onVisit(SourceItemVisitCallback callback){
		visitCallback = callback;
	}
	
	/***
	 * Creates a SourceItemFactory and begins recursing the source item tree for the provided file.
	 * @param file The file to recurse.
	 * @param sourceItemFactorySettings Settings you might provide to a call to Utilities.getSourceItemFactory, can be null to use defaults
	 * @param fileSettings Settings you might provide to a call to SourceItemFactory.openFile, can be null to use defaults
	 * @throws FileNotFoundException Thrown by SourceItemFactory if the provided file does not exist.
	 */
	public void visit(File file, Map<?,?> sourceItemFactorySettings, Map<?,?> fileSettings) throws FileNotFoundException{
		SourceItemFactory factory = null;
		SourceItem root = null;
		
		if(sourceItemFactorySettings != null)
			factory = NuixConnection.getUtilities().createSourceItemFactory(sourceItemFactorySettings);
		else
			factory = NuixConnection.getUtilities().createSourceItemFactory();
		
		if(fileSettings != null)
			root = factory.openFile(file,fileSettings);
		else
			root = factory.openFile(file);
		
		recursivelyVisit(root);
		factory.close();
	}
	
	/***
	 * Creates a SourceItemFactory and begins recursing the source item tree for the provided file.
	 * @param file The file to recurse.
	 * @param sourceItemFactorySettings Settings you might provide to a call to Utilities.getSourceItemFactory, can be null to use defaults
	 * @param fileSettings Settings you might provide to a call to SourceItemFactory.openFile, can be null to use defaults
	 * @throws FileNotFoundException Thrown by SourceItemFactory if the provided file does not exist.
	 */
	public void visit(String file, Map<?,?> sourceItemFactorySettings, Map<?,?> fileSettings) throws FileNotFoundException{
		visit(new java.io.File(file),sourceItemFactorySettings, fileSettings);
	}
	
	/***
	 * Recursively visits source items and their children unless callback specifies skipping children for a given source item.
	 * @param sourceItem The source item to visit and potentially recursively visit children of.
	 */
	protected void recursivelyVisit(SourceItem sourceItem){
		boolean visitChildren = visitCallback.visitSourceItem(sourceItem);
		if(visitChildren){
			for(SourceItem child : sourceItem.getChildren()){
				recursivelyVisit(child);
				child.close();
			}
		}
		sourceItem.close();
	}
}
