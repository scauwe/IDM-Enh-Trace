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
package info.vancauwenberge.designer.enhtrace.editors;

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageList;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.Channel;
import info.vancauwenberge.designer.enhtrace.viewer.outline.LogMessageOutlinePage;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.novell.core.CoreImages;

public class LiveTraceEditor extends AbstractTraceEditor<info.vancauwenberge.designer.enhtrace.editors.LiveTraceEditor.Actions>
{
	protected enum Actions implements AbstractTraceEditor.IToolbarAction{
		LOAD("Load","Load a previously saved trace file",false,CoreImages.getImageDescriptor("icons/folder_open.gif"),false), 
		SAVE("Save","Save the current trace to a trace file", false,CoreImages.getImageDescriptor("icons/save.gif"),true), 
	
		STOP("Stop","Stop the live trace", true,CoreImages.getImageDescriptor("icons/driver_stop.gif"), false),
		RESTART("Restart","Restart the live trace", false,CoreImages.getImageDescriptor("icons/driver_start.gif"),true),
		
		CLEAR("Clear","Clear the trace", true,CoreImages.getImageDescriptor("icons/clear.gif"),false),
		
		CONFIG("Config","Configure the trace", false,CoreImages.getImageDescriptor("icons/dstrace/dsConfigure.gif"),false);

		private String label;
		private String toolTip;
		private boolean defaultEnabled;
		private ImageDescriptor image;
		private boolean terminate;

		private Actions(String label, String toolTip, boolean defaultEnabled, ImageDescriptor image, boolean terminate){
			this.label = label;
			this.toolTip = toolTip;
			this.defaultEnabled = defaultEnabled;
			this.image = image;
			this.terminate = terminate;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public String getToolTip() {
			return toolTip;
		}

		@Override
		public boolean isDefaultEnabled() {
			return defaultEnabled;
		}

		@Override
		public ImageDescriptor getImage() {
			return image;
		}

		@Override
		public boolean addSeperator() {
			return terminate;
		}
	}

	private LiveTraceComposite m_liveTraceComposite;
	private info.vancauwenberge.designer.enhtrace.viewer.outline.LogMessageOutlinePage myOutlinePage;
	private boolean isSelecting;


	public LiveTraceEditor() {
		super(Actions.class);
	}

	public void dispose() {
		System.out.println(this.getClass().getName()+" dispose() start");
		IEditorInput source = getEditorInput();
		if (source instanceof LiveTraceEditorInput)
			((LiveTraceEditorInput)getEditorInput()).dispose();
		
	    if (this.m_liveTraceComposite != null)
	    {
	      this.m_liveTraceComposite.dispose();
	      this.m_liveTraceComposite = null;
	    }
	    //((LiveTraceEditorInput)getEditorInput()).dispose();
	    super.dispose();
		System.out.println(this.getClass().getName()+" dispose() end");
	}
	
	protected AbstractTraceComposite<?> createTraceContentComposit(Composite parentComposite){
   	  	this.m_liveTraceComposite = new LiveTraceComposite(parentComposite, this);
   	  	final GridData m_traceLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
   	  	this.m_liveTraceComposite.setLayoutData(m_traceLayoutData);
   	  	return m_liveTraceComposite;
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	    
	//    ((LiveTraceEditorInput)getEditorInput()).restart();
   	  	setPartName("Trace");
	}
	
	
	
	 public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
	  {
		 System.out.println(this.getClass().getName()+" - getAdapter for "+adapter.getName() + "("+adapter+")");
		 
		 /*
	    if (IFindReplaceTarget.class.equals(adapter))
	    {
	      if (this.m_savedTraceComposite != null)
	        return this.m_savedTraceComposite.getFindReplaceTarget();
	    }*/
	    //For help:
	    //else if (IContextProvider.class.equals(paramClass))
	    //  localObject = this;
	    //else 
	    if (IContentOutlinePage.class.equals(adapter)){
			if (myOutlinePage == null){
				myOutlinePage = new LogMessageOutlinePage(this);
			}
			return myOutlinePage;
		}else if (ILogMessageList.class.equals(adapter)){
			return getInput();
		}
	      return super.getAdapter(adapter);
	  }

	  protected  String createToolbarLabel(){
		  return "Connected to "+((LiveTraceEditorInput)getEditorInput()).getServer()+" as "+((LiveTraceEditorInput)getEditorInput()).getUserName();
		  
	  }
	  
	
	protected void doaction(Actions anAction) {
		LiveTraceEditorInput source = (LiveTraceEditorInput) getEditorInput();
		switch (anAction) {
		case CLEAR:
			source.clearAll();
			break;
		case STOP:
    		source.stop();
    	    getAction(Actions.STOP).setEnabled(false);
    	    getAction(Actions.RESTART).setEnabled(true);
			break;
		case RESTART:
    		source.restart();
    		getAction(Actions.STOP).setEnabled(true);
    		getAction(Actions.RESTART).setEnabled(false);
			break;
		case CONFIG:
			//TODO: create our own preference page...
			PreferencesUtil.createPreferenceDialogOn(null, "com.novell.core.dstraceviewer.liveTrace.tracePrefs", null, null).open();
			//TODO: how to get the preferences when they are changed
			break;
		default:
			break;
		}
	}

	public ILogMessageList getInput() {
		return ((LiveTraceEditorInput)getEditorInput());
	}


	
	public void setSelection(IStructuredSelection selection) {
		if (isSelecting)//prevent looping
			return;
		isSelecting = true;
		myOutlinePage.setSelection(selection);
		m_liveTraceComposite.setSelection(selection);
		isSelecting=false;
	}

	@Override
	/**
	 * A live trace editor does not keep track of channels. The channel of the message equals the thread of the message
	 */
	public Channel getChannelFor(ILogMessage message) {
		return Channel.forName(message.getThread());
	}

	@Override
	/**
	 * A live trace editor does not have a current root.
	 */
	public ILogMessage getCurrentRoot() {
		return null;
	}


}
