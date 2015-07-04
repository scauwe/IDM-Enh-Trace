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

import info.vancauwenberge.designer.enhtrace.editor.input.LiveServerTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;


public class OpenTraceAction implements IObjectActionDelegate {

	private ISelection selection;

	private void openEditor(com.novell.idm.model.Server server){
		try
	    {
			System.out.println("openEditor()");
	    	IWorkbenchPage localIWorkbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			System.out.println("got page");
			LiveTraceEditorInput input = new LiveServerTraceEditorInput(server);
			
			localIWorkbenchPage.openEditor(input, "info.vancauwenberge.designer.enhtrace.editors.EnhTraceEditor");				

			/*
			System.out.println("got input");
			
			IEditorPart currentEditor = localIWorkbenchPage.findEditor(input);
			System.out.println("searched editor");
			if (currentEditor==null){
				currentEditor = localIWorkbenchPage.openEditor(input, "info.vancauwenberge.designer.enhtrace.editors.EnhTraceEditor");				
				System.out.println("created new editor");
			}else{
				localIWorkbenchPage.activate(currentEditor);
			}*/

	    }
	    catch (Exception e)
	    {
			MessageBox dialog = 
	    			  new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
	    		dialog.setText("DS Trace");
	    		dialog.setMessage("Failed to start trace: "+e.getMessage());
	    		dialog.open();
	    		return;
	    }

	}
	
	@Override
	public void run(IAction action) {
		System.out.println("OpenTraceAction.run() action="+action);
		System.out.println("OpenTraceAction.run() action="+action.getId());
		System.out.println("OpenTraceAction.run() action="+action.getText());
		System.out.println("OpenTraceAction.run() selection="+selection.getClass().getName());
		System.out.println("OpenTraceAction.run() selection="+selection.getClass().getGenericInterfaces());
		if (selection instanceof com.novell.idm.model.Server){
			com.novell.idm.model.Server server = (com.novell.idm.model.Server)selection;
			System.out.println("selectedObject:"+server.getDirectoryDN());
			openEditor(server);			
		}
		else if (selection instanceof StructuredSelection){

			StructuredSelection structuredSelection = (StructuredSelection)selection;
			Object selectedObject = structuredSelection.getFirstElement();
			System.out.println("selectedObject:"+selectedObject.getClass().getName());
			System.out.println("selectedObject:"+structuredSelection.size());
			if (selectedObject instanceof com.novell.idm.model.Server){
				com.novell.idm.model.Server server = (com.novell.idm.model.Server)selectedObject;
				System.out.println("selectedObject:"+server.getDirectoryDN());
				openEditor(server);
				//LiveTraceEditorInput input = Activator.registerDSTraceListener();
			}
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
