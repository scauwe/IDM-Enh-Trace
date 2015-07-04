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
import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.Channel;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.IStaticInputListener;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.PolicySteps;
import info.vancauwenberge.designer.enhtrace.viewer.outline.page.DetailedPolicyFlowPage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.novell.core.CoreImages;

public class DetailedTraceEditor extends AbstractTraceEditor<info.vancauwenberge.designer.enhtrace.editors.DetailedTraceEditor.Actions> implements IStaticInputListener{



	protected enum Actions implements AbstractTraceEditor.IToolbarAction{
		LOAD("Load","Load a previously saved trace file",false,CoreImages.getImageDescriptor("icons/folder_open.gif"),false), 
		SAVE("Save","Save the current detail to a trace file", false,CoreImages.getImageDescriptor("icons/save.gif"),true), 
		REFRESH("Refresh","Refresh the current trace in case the event did not complete when loaded", true,CoreImages.getImageDescriptor("icons/refresh.gif"), false),
		RESCAN("Rescan","Rescan the live trace for the latest trace with the same signature", true,CoreImages.getImageDescriptor("icons/compare_channel.gif"),true), 
		FINDREPLACE("Search","Search in the trace", true,CoreImages.getImageDescriptor("icons/search.gif"),false);
		private String label;
		private String toolTip;
		private boolean defaultState;
		private ImageDescriptor image;
		private boolean terminate;

		private Actions(String label, String toolTip, boolean defaultState, ImageDescriptor image, boolean terminate){
			this.label = label;
			this.toolTip = toolTip;
			this.defaultState = defaultState;
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
			return defaultState;
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
	
	private DetailedTraceComposite m_liveTraceComposite;
	private boolean isSelecting;

	private DetailedPolicyFlowPage myOutlinePage;

	public DetailedTraceEditor() {
		super(Actions.class);
	}

	protected AbstractTraceComposite<?> createTraceContentComposit(Composite parentComposite){
	    this.m_liveTraceComposite = new DetailedTraceComposite(parentComposite, this);
	    final GridData m_traceLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
	    this.m_liveTraceComposite.setLayoutData(m_traceLayoutData);
	    return m_liveTraceComposite;
		
	}


	/**
	 * Add a selectionchange listener to the underlaying composite
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		this.m_liveTraceComposite.addSelectionChangedListener(listener);
	}
	
	/**
	 * Remove a selectionchange listener from the underlaying composite
	 * @param listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		this.m_liveTraceComposite.removeSelectionChangedListener(listener);
	}

	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);


	    //Set the tab label
	    setPartName("Detailed Trace");
	}

	

	public void dispose() {
	    if (this.m_liveTraceComposite != null)
	    {
	      this.m_liveTraceComposite.dispose();
	      this.m_liveTraceComposite = null;
	    }
	    ((StaticTraceEditorInput)getEditorInput()).dispose();
	    super.dispose();
	    
	}
	
	private String createToolbarLabel(ILogMessage message){
	    StringBuilder title = new StringBuilder(message.getTraceName());
	    title.append(" - ").append(message.getThread());
	    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	    title.append(" - ").append(df.format(new Date(message.getTimeStamp())));
	    title.append(" - ").append(message.getMessage());
	    return title.toString();		
	}
	
	protected String createToolbarLabel(){
	    return createToolbarLabel(((StaticTraceEditorInput)getEditorInput()).getRoot());
	    
	}

	protected void doaction(Actions anAction) {
		switch (anAction) {
		case FINDREPLACE:
			
			break;

		case LOAD:
			
			break;

		case REFRESH:
			((StaticTraceEditorInput)getEditorInput()).refresh();
			m_liveTraceComposite.refresh();
			myOutlinePage.refresh();
			break;

		case RESCAN:
			//rescan the input. This will trigger a rootchanged notification if something was changed.
			((StaticTraceEditorInput)getEditorInput()).rescan();
			break;

		case SAVE:
			
			break;

		default:
			break;
		}
	}
	

	
	public void setSelection(IStructuredSelection selection) {
		System.out.println(this.getClass().getName()+".setSelection():"+selection);
		if (isSelecting)//prevent looping
			return;
		isSelecting = true;
		//myOutlinePage.setSelection(selection);
		m_liveTraceComposite.setSelection(selection);
		isSelecting=false;
	}
	

	
	 public Object getAdapter(Class adapter)
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
				myOutlinePage = new DetailedPolicyFlowPage(this);
			}
			return myOutlinePage;
		} else if (IShowInSource.class.equals(adapter)){
			//TODO: this should/might give the policset view
		      return new IShowInSource() {
				
				@Override
				public ShowInContext getShowInContext() {
					System.out.println(this.getClass().getName()+" - getShowInContext()");
					return new ShowInContext(getEditorInput(), null);
				}
			};
		} else if (IShowInTarget.class.equals(adapter)){
			//TODO: this should/might give the policset view
		      return new IShowInTarget() {

				@Override
				public boolean show(ShowInContext context) {
					System.out.println(this.getClass().getName()+" - show() for "+context);
					return false;
				}
				
			};
		} else
	      return super.getAdapter(adapter);
	  }

	@Override
	public void notifyRootChanged(ILogMessage newValue, ILogMessage oldValue) {
		setToolbarLabel(createToolbarLabel(newValue));
		
	}


	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		((StaticTraceEditorInput)input).addListener(this);
	}


	@Override
	/**
	 * Search in the mappings what the channel is for this message
	 */
	public Channel getChannelFor(ILogMessage message) {
		System.out.println("Getting channel for:"+message);
		Map<PolicySteps, List<IPolicySetLogMessage>> mapping = ((StaticTraceEditorInput)getEditorInput()).getPolicy2MessageMap();
		Set<PolicySteps> keys = mapping.keySet();
		for (PolicySteps aPolicyStep : keys) {
			List<IPolicySetLogMessage> messageList = mapping.get(aPolicyStep);
			if (messageList.contains(message)){
				Channel result = aPolicyStep.getDefaultChannel();
				if (result==null)
					result = aPolicyStep.getNextChannel();
				return result;
			}
		}
		return null;
	}

	@Override
	/**
	 * Get the current root of this detailed trace editor.
	 */
	public ILogMessage getCurrentRoot() {
		return ((StaticTraceEditorInput)getEditorInput()).getRoot();
	}


}
