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

import info.vancauwenberge.designer.enhtrace.Activator;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class TraceActionDelegate
implements IWorkbenchWindowActionDelegate
{
	public void dispose()
	{
	}

	public void init(IWorkbenchWindow paramIWorkbenchWindow)
	{
	}

	public void run(IAction paramIAction)
	{
		try
		{
			System.out.println("paramIAction:"+paramIAction);
			IWorkbenchPage localIWorkbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			//Utopia
			LiveTraceEditorInput input = new LiveTraceEditorInput("172.17.2.91:636","cn=admin,ou=sa,o=system","netiq000",true);
			//SNCB
			//LiveTraceEditorInput input = new LiveTraceEditorInput("10.15.11.204","cn=admin,ou=system,o=NMBS-GROUP","novell",false);
			//EIDA
			//LiveTraceEditorInput input = new LiveTraceEditorInput("172.22.90.2","cn=admin,ou=sa,o=system","netiq000",false);

			localIWorkbenchPage.openEditor(input, "info.vancauwenberge.designer.enhtrace.editors.EnhTraceEditor");
		}
		catch (Exception localException)
		{
			Activator.log(localException.getMessage(), localException);
		}
	}

	public void selectionChanged(IAction paramIAction, ISelection paramISelection)
	{
	}

}