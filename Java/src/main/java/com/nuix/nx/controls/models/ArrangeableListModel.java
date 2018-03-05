/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ArrangeableListModel<E> extends AbstractListModel<E> {
	private List<E> elements = new ArrayList<E>();
	
	@Override
	public E getElementAt(int index) {
		return elements.get(index);
	}

	@Override
	public int getSize() {
		if(elements == null){
			return 0;
		} else {
			return elements.size();
		}
	}
	
	public int size(){
		if(elements == null){
			return 0;
		} else {
			return elements.size();
		}
	}

	public boolean shiftRowsUp(int firstRowIndex, int lastRowIndex){
		if(firstRowIndex <= 0)
			return false;
		
		for (int i = firstRowIndex; i <= lastRowIndex; i++) {
			Collections.swap(elements, i, i-1);
		}
		
		fireContentsChanged(this, firstRowIndex, lastRowIndex);
		
		return true;
	}
	
	public boolean shiftRowsDown(int firstRowIndex, int lastRowIndex){
		if(lastRowIndex >= elements.size() - 1)
			return false;
		
		for (int i = firstRowIndex; i <= lastRowIndex; i++) {
			Collections.swap(elements, i, i+1);
		}
		
		fireContentsChanged(this, firstRowIndex, lastRowIndex);
		
		return true;
	}
	
	public void addElement(E element){
		SwingUtilities.invokeLater(()->{
			elements.add(element);
			fireIntervalAdded(this, elements.size()-1, elements.size()-1);
		});
	}
	
	public void remove(int index){
		SwingUtilities.invokeLater(()->{
			elements.remove(index);
			fireIntervalRemoved(this, index, index);
		});
	}
	
	public void clear(){
		SwingUtilities.invokeLater(()->{
			try {
				int lastIndex = elements.size()-1;
				elements.clear();
				fireIntervalRemoved(this, 0, lastIndex);
			} catch (Exception e) {
			}
		});
	}
}
