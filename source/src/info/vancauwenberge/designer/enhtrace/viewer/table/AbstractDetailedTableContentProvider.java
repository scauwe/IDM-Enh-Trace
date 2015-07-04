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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.IStaticInputListener;
import info.vancauwenberge.designer.enhtrace.editors.DetailedTraceComposite;

public abstract class AbstractDetailedTableContentProvider implements IStaticInputListener, IStructuredContentProvider{

	private DetailedTraceComposite viewer;
	private ILogMessage rootMessage;
	
	
	public AbstractDetailedTableContentProvider(DetailedTraceComposite detailedTraceComposite, ILogMessage rootMessage) {
		this.viewer = detailedTraceComposite;
		this.rootMessage = rootMessage;
		((StaticTraceEditorInput)detailedTraceComposite.getEditor().getEditorInput()).addListener(this);
	}
	
	
	@Override
	public void notifyRootChanged(ILogMessage newValue, ILogMessage oldValue) {
		rootMessage = newValue;
		viewer.refresh();
	}
	
	@Override
	public void dispose() {
		rootMessage = null;
		viewer = null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	//System.out.println(TableContentProvider.class.getName()+"-inputChanged ("+viewer+", "+oldInput+", "+newInput+")");
    }


}
