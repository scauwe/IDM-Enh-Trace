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

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageList;

import java.util.Collection;

import org.eclipse.jface.viewers.TableViewer;

public class LiveLinuxTableContentProvider extends AbstractLiveTableContentProvider
{

	public LiveLinuxTableContentProvider(TableViewer logViewerTable, ILogMessageList messages) {
		super(logViewerTable, messages);
	}
	
	
	protected Object[] createElements(ILogMessage[] messages){
		return messages;
	}
	protected Object[] createElements(ILogMessage iLogMessage){
		return new Object[]{iLogMessage};
	}
	
	/*
	@Override
	public void itemRemoved(final ILogMessage message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				//System.out.println("Updating view - itemRemoved");
				if (viewer != null)
					viewer.remove(message);
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
					viewer.add(message);
					if (isAutoScroll)
						viewer.reveal(message);
				}
			}
		});
	}

	@Override
	public void itemsAdded(final Collection<? extends ILogMessage> messages) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				//System.out.println("Updating view - itemsAdded:"+messages);
				viewer.add(messages.toArray());
				if (isAutoScroll)
					viewer.reveal(((List)messages).get(messages.size()-1));
			}
		});
	}

	@Override
	public void itemsRemoved(final Collection<? extends ILogMessage> removedItems) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				viewer.remove(removedItems.toArray());
			}
		});
	}

	*/
	
}