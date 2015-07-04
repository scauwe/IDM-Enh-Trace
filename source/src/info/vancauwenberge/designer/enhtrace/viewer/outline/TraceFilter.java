/*******************************************************************************
 * Copyright (c) 2014-2015 Stefaan Van Cauwenberge
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0 (the "License"). If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  	 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Initial Developer of the Original Code is
 * Stefaan Van Cauwenberge. Portions created by
 *  the Initial Developer are Copyright (C) 2007-2015 by
 * Stefaan Van Cauwenberge. All Rights Reserved.
 *
 * Contributor(s): none so far.
 *    Stefaan Van Cauwenberge: Initial API and implementation
 *******************************************************************************/
package info.vancauwenberge.designer.enhtrace.viewer.outline;

import info.vancauwenberge.designer.enhtrace.Activator;
import info.vancauwenberge.designer.enhtrace.api.IListListener;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

class TraceFilter extends ViewerFilter implements IListListener<ILogMessage> {

	public static final String NO_FILTER="<no filter>";
	public static final int OPTIONS_ADDED = 2048;

	private String traceName;
	private SortedSet<String> options = new TreeSet<String>();
	private Set<Listener> optionsChangedListener = new HashSet<Listener>();
			
	public TraceFilter(ILogMessageList traceSource) {
		traceName = Activator.getDefault().getPreferenceStore().getString("filter");
		if (traceName != null && !traceName.equals(""))
			options.add(traceName);
		options.add(NO_FILTER);
		traceSource.addListListener(this);
	}


	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		//System.out.println("select: element:"+element+"-parent:"+parentElement);
		if (traceName==null || traceName.equals(""))
			return true;
		if (NO_FILTER.equals(traceName))
				return true;
		
		if (element instanceof ILogMessage){
			ILogMessage message = (ILogMessage)element;
			return (message.getTraceName().equals(traceName));
		}else if (element instanceof OutlineTraceNode){
			return ((OutlineTraceNode)element).getLabel().equals(traceName);
		}else if (parentElement instanceof OutlineTraceNode){
			return ((OutlineTraceNode)parentElement).getLabel().equals(traceName);
		}
		
		return false;
	}


	public String getFilterValue() {
		return traceName;
	}

	private void addOption(String option){
		options.add(option);
		notifyOptionsChanged();
	}
	
	/**
	 * 
	 * @param event - not used for the moment. We sent out only one event: OPTIONS_ADDED
	 * @param listener
	 */
	public void addListener(int event, Listener listener){
		optionsChangedListener.add(listener);
	}
	
	public int getSelectedIndex(){
		if (traceName != null && !"".equals(traceName)){
			Iterator<String> it = options.iterator();
			int i = 0;
			while(it.hasNext()) {
				if (it.next().equals(traceName))
					return i;
				i++;
			}
		}
		return 0;

	}
	
	private void notifyOptionsChanged() {
		Event event = new Event();
		event.type = OPTIONS_ADDED;
		event.data = getOptions();
		event.text = traceName;
		event.index = getSelectedIndex();
		for (Listener aListener : optionsChangedListener) {
			aListener.handleEvent(event);
		}
	}


	public String[] getOptions(){
		String[] items = new String[options.size()];
		return options.toArray(items);
	}

	public void setFiltereValue(String filteredThread) {
		System.out.println("setFiltereValue:"+filteredThread);
		this.traceName = filteredThread;
		Activator.getDefault().getPreferenceStore().setValue("filter", filteredThread);
	}
	

	@Override
	public void listClreared() {
	}
/*
	@Override
	public void itemRemoved(ILogMessage removeItem) {
	}

	@Override
	public void itemAdded(ILogMessage newItem) {
		addOption(newItem.getTraceName());
	}
*/

	@Override
	public void itemsRemoved(ILogMessage[] removedItems) {
	}

	@Override
	public void itemsAdded(ILogMessage[] newItems) {
		
		//First build a set of the new traceNames
		final Set<String> traces = new HashSet<String>();
		for (ILogMessage iLogMessage : newItems) {
			traces.add(iLogMessage.getTraceName());
		}

		int countBefore = options.size();
		options.addAll(traces);
		if (countBefore != options.size())
			notifyOptionsChanged();
	}

}