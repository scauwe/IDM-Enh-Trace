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
package info.vancauwenberge.designer.enhtrace.viewer.table;

import info.vancauwenberge.designer.enhtrace.api.IListListener;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageList;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractLiveTableContentProvider implements IStructuredContentProvider, IListListener<ILogMessage>{
	private volatile boolean isAutoScroll = true;

	private TableViewer viewer;
	private ILogMessageList data;
	
	public AbstractLiveTableContentProvider(TableViewer logViewerTable, ILogMessageList messages) {
		this.viewer = logViewerTable;
		this.data = messages;
		data.addListListener(this);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
    	//System.out.println(TableContentProvider.class.getName()+"-getElements("+inputElement+")");
        if(inputElement instanceof ILogMessageList){
        	return createElements(((ILogMessageList)inputElement).getMessages());
        }
        return null;
    }
	
	protected abstract Object[] createElements(ILogMessage[] messages);
	protected abstract Object[] createElements(ILogMessage iLogMessage);
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
		data.removeListListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	//System.out.println(TableContentProvider.class.getName()+"-inputChanged ("+viewer+", "+oldInput+", "+newInput+")");
    }
    

	@Override
	public void listClreared() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed())
					viewer.refresh();
			}
		});
	}
	
	/*
	@Override
	public void itemRemoved(final ILogMessage message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				//System.out.println("Updating view - itemRemoved");
				if (viewer != null)
					viewer.remove(createElements(message));
			}
		});
	}
	
	@Override
	public void itemAdded(final ILogMessage message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				//System.out.println("Updating view - itemAdded");
				if (viewer != null){
					viewer.add(createElements(message));
					if (isAutoScroll)
						viewer.reveal(message);
				}
			}
		});
	}*/

	@Override
	public void itemsAdded(final ILogMessage[] messages) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				//System.out.println("Updating view - itemsAdded:"+messages);
				viewer.add(createElements(messages));
				if (isAutoScroll)
					viewer.reveal(messages[messages.length-1]);
			}
		});
	}

	
	@Override
	/**
	 * Remove the items. 
	 * Note: this implementation assumes that the removedItems array contains the items to remove in sequence!!!
	 */
	public void itemsRemoved(final ILogMessage[] removedItems) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed()){
					//We build a map with the for all items in the removedItemsList
					List<ILogMessageProvider> removedList = new ArrayList<ILogMessageProvider>(removedItems.length);
					Table table = viewer.getTable();
					/**
					 * We assume that the list in 'removedItems' is in sequence!!!!
					 */
					int lastTablePos = 0;
					for (int i = 0; i < removedItems.length; i++) {
						ILogMessage iLogMessage = removedItems[i];
						while(true){
							ILogMessageProvider data = (ILogMessageProvider) table.getItem(lastTablePos).getData();
							//We start with the 'root'
							if (data.getLogMessage()==iLogMessage){
								removedList.add(data);
								lastTablePos++;
							}else{
								break;
							}
						}
						
					}
					viewer.remove(removedList.toArray());
										
					//viewer.refresh();
					//viewer.remove(createElements(removedItems));
				}
			}
		});
	}

	
}
