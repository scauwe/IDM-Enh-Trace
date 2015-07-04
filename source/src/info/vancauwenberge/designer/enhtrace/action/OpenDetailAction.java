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

package info.vancauwenberge.designer.enhtrace.action;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import info.vancauwenberge.designer.enhtrace.Activator;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;
import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage;
import info.vancauwenberge.designer.enhtrace.api.IRootLogMessage;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.Channel;
import info.vancauwenberge.designer.enhtrace.editors.AbstractTraceEditor;
import info.vancauwenberge.designer.enhtrace.editors.DetailedTraceEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class OpenDetailAction extends Action implements IActionDelegate//, IWorkbenchWindowActionDelegate, IEditorActionDelegate
{
	private IStructuredSelection m_selection;
	private IWorkbenchWindow window;
	private ILogMessage rootMessage;
	private Channel rootChannel;


	public OpenDetailAction(){	
	}

	public OpenDetailAction(IStructuredSelection selection, AbstractTraceEditor<?> editor){
		super("Open subflow");
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		System.out.println(this.getClass().getName()+": OpenDetailAction() for:"+obj);
		
		if (obj != null && obj instanceof ILogMessageProvider) {
	    	  ILogMessage selectedMessage = ((ILogMessageProvider)obj).getLogMessage();
	    	  ILogMessage openRootMessage =  getDetailRootFor(selectedMessage, editor.getCurrentRoot());
	    	  Channel channel =null;
	    	  if (openRootMessage != null){
	    		  channel = editor.getChannelFor(openRootMessage);
	    		  this. m_selection = selection;
	    		  this.rootMessage = openRootMessage;
	    		  this.rootChannel = channel;
	    		  System.out.println(this.getClass().getName()+": OpenDetailAction() will open:"+rootMessage);
	    		  System.out.println(this.getClass().getName()+": OpenDetailAction() will use channel:"+ rootChannel);
	    		  return;
	    	  }
		}
		throw new IllegalArgumentException("Nothing to show.");
	}
	/*
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

	public void dispose() {
		window = null;
		m_selection = null;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;		
	}
*/
	public void run(IAction action) {
		run();
	}

	public void run() {
		System.out.println(this.getClass().getName()+": run()");
	    if ((m_selection != null) && (m_selection.size() ==1)){
	    	System.out.println(this.getClass().getName()+": Should open new editor");
	        try
	        {
	        	IWorkbenchPage localIWorkbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	        	ILogMessageProvider selected = (ILogMessageProvider) m_selection.getFirstElement();
	        	if (rootMessage==null){
	        		rootMessage = getDetailRootFor(selected.getLogMessage());
	    	    	System.out.println(this.getClass().getName()+": Root found:"+rootMessage);
	        	}
	        	if (rootMessage != null){
	        		if (rootChannel==null){
	        			rootChannel = Channel.forName(rootMessage.getThread());
		    	    	System.out.println(this.getClass().getName()+": Channel found:"+rootChannel);
	        		}
	        		
	        		DetailedTraceEditor editor = (DetailedTraceEditor)localIWorkbenchPage.openEditor(new StaticTraceEditorInput(rootMessage, rootChannel), "info.vancauwenberge.designer.enhtrace.editors.DetailedTraceEditor");
	        		
	        		if (m_selection != null)
	        			editor.setSelection(m_selection);
	        	}else{
	        		MessageBox dialog = 
	        			  new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
	        		dialog.setText("Detailed Trace");
	        		dialog.setMessage("No details to show for this message.");
	        		dialog.open(); 
	        	}
	        }
	        catch (Exception localException)
	        {
	        	Activator.log(localException.getMessage(), localException);
	        }
	    }else{
	    	System.out.println("Not able to open editor");	    	
	    }
	}

	private ILogMessage getDetailRootFor(final ILogMessage selected) {
		ILogMessage root = selected;
		//walk to the top until you find an element that has children of type IPolicySetLogMessage
		do{
			if (root instanceof IRootLogMessage)
				return root;
			root = root.getParent();
		}while(root != null);
		return null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		System.out.println(this.getClass().getName()+": selectionChanged()");
        if(selection instanceof StructuredSelection){
            m_selection = (StructuredSelection)selection;
            rootChannel = null;
            rootMessage = null;
        }
	}

	

	/**
	 * Tests if a given logmessage has a child of class IPolicySetLogMessage
	 * Used for finding the policy to open
	 * @param message
	 * @return
	 */
	private boolean hasPolicySetChild(ILogMessage message){
		if (message.hasChildren()){
			List<ILogMessage> children = message.getChildren();
			for (ILogMessage iLogMessage : children) {
				if (iLogMessage instanceof IPolicySetLogMessage)
					return true;
			}
		}
		return false;
	}

	/**
	 * Searches for the level+1 policy starting from currentRoot towards focusOn that has a child that is a policyset message
	 * @param focusOnMessage
	 * @param currentRootMessage
	 * @return
	 */
	private ILogMessage getDetailRootFor(ILogMessage focusOnMessage, ILogMessage currentRootMessage) {
		//Create the path from the new focus to the current focus
		List<ILogMessage> path = new ArrayList<ILogMessage>();
		ILogMessage pos = focusOnMessage;
		while(pos != null && pos != currentRootMessage){
			path.add(pos);
			pos = pos.getParent();
		}
		//If we where able to find a path from the new to the current focus
		if (path.size()>0 && pos==currentRootMessage){
			//Find the correct child, going backwards in the list
			ListIterator<ILogMessage> iter = path.listIterator(path.size());
			while (iter.hasPrevious()){
				ILogMessage message = iter.previous();
				if (hasPolicySetChild(message))
					return message;				
			}
		}
		return null;		
	}


}