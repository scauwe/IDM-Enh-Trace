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


import info.vancauwenberge.designer.enhtrace.api.IListListener;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageList;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

public class OutlineContentProvider implements ITreeContentProvider, IListListener<ILogMessage> {
	private TreeViewer viewer;
	private ILogMessageList data;
	private List<OutlineTraceNode> allRoots = new ArrayList<OutlineTraceNode>();

	
	
	//or implements ITreePathContentProvider


	public OutlineContentProvider(TreeViewer viewer, ILogMessageList messages) {
		this.viewer = viewer;
		this.data = messages;
		data.addListListener(this);
	}

	@Override
	public void dispose() {
		System.out.println(this.getClass().getName()+" dispose() start");
		allRoots.clear();
		System.out.println(this.getClass().getName()+" dispose() end");
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
		// TODO Auto-generated method stub

	}

	
	@Override
	public Object[] getElements(Object inputElement) {
		 if(inputElement instanceof LiveTraceEditorInput)
	            return allRoots.toArray();
		        //((TraceEditorInput)inputElement).getMessages().getRootMessages().toArray();
	        return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		 if(parentElement instanceof ILogMessage){
	            List<ILogMessage> children =  ((ILogMessage)parentElement).getChildren();
	            if (children!=null)
	            	return children.toArray();
	            return new Object[]{};
		 }else if(parentElement instanceof IOutlineParentNode)
	            return ((IOutlineParentNode)parentElement).getChildren().toArray();
		 return null;
	}

	@Override
	public Object getParent(Object element) {
		 if(element instanceof ILogMessage){
	         ILogMessage tmpParent = ((ILogMessage)element).getParent();
			 if (tmpParent == null && !(tmpParent instanceof OutlineThreadNode)){
				 //TODO tmpParent
			 }
		 }
		 return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return ((children!=null) && children.length>0);
	}

	@Override
	public void listClreared() {
		System.out.println(getClass().getName()+" - listClreared");
		allRoots.clear();
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				Control control = viewer.getControl();
				if (control==null || control.isDisposed())
					return;
				viewer.refresh();
			}
		});
	}

	/*
	@Override
	public void itemRemoved(final ILogMessage message) {
		System.out.println(getClass().getName()+" - itemRemoved");
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				Control control = viewer.getControl();
				if (control==null || control.isDisposed())
					return;
				viewer.remove(message);
			}
		});
	}

	@Override
	public void itemAdded(final ILogMessage message) {
		//System.out.println(getClass().getName()+" - itemAdded");
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				Control control = viewer.getControl();
				if (control==null || control.isDisposed())
					return;
				Object parent = message.getParent();
				if (parent == null){
					OutlineThreadNode threadNode = getOrCreateThreadNodeFor(message);
					if (!threadNode.hasChild(message)){
						threadNode.addChild(message);
						//Show the newly created thread node
						if (threadNode.getChildren().size()<=1){
							//viewer.expandToLevel(threadNode,0);
							viewer.expandToLevel(threadNode.getParent(), 1);
							//viewer.expandToLevel(threadNode, AbstractTreeViewer.ALL_LEVELS);
							//viewer.expandToLevel(threadNode, 1);
							//viewer.reveal(threadNode);
						}
					}
					viewer.add(threadNode, message);
				}else
					viewer.add(parent, message);
				//Do not auto-reveal in the outline tree viewer
				//viewer.reveal(message);
			}

		});

		
	}
	*/
	
	/**
	 * Return the outline treeview tracenode for this message. This node is the actual root
	 * @param message
	 * @return
	 */
	private OutlineTraceNode getTraceNodeFor(ILogMessage message){
		String traceName = message.getTraceName();
		for (OutlineTraceNode aRoot : allRoots) {
			if (aRoot.getLabel().equals(traceName))
				return aRoot;
		}
		return null;
	}
	
	/**
	 * Return the outline treeview threadnode for this message. This node is a child of an actual root
	 * @param message
	 * @return
	 */
	private OutlineThreadNode getThreadNodeFor(ILogMessage message) {
		OutlineTraceNode traceNode = getTraceNodeFor(message);
		if (traceNode != null){
			String threadToSearch = message.getThread();
			@SuppressWarnings("unchecked")
			List<OutlineThreadNode> threadNodes = (List<OutlineThreadNode>) traceNode.getChildren();
			for (OutlineThreadNode iLogMessage : threadNodes) {
				if (iLogMessage.getLabel().equals(threadToSearch))//For the threadNodes, the message is the thread.
					return (OutlineThreadNode)iLogMessage;
			}
		}
		return null;
	}

	/**
	 * In the outline view, if a logmessage is the root, we add it to the <tracename>-<threadname> node.
	 * This method creates or finds this node for the current (root) message
	 * @param message
	 * @return
	 */
	private OutlineThreadNode getOrCreateThreadNodeFor(ILogMessage message) {
		OutlineThreadNode threadNode = getThreadNodeFor(message);
		if (threadNode == null){
			OutlineTraceNode traceNode = getTraceNodeFor(message);
			if (traceNode==null){
				traceNode = new OutlineTraceNode(message.getTraceName());
				allRoots.add(traceNode);
				viewer.refresh();//We added a root item. Update the viewer
			}
			threadNode = new OutlineThreadNode(message.getThread(), traceNode);
			traceNode.addChild(threadNode);
			viewer.add(traceNode, threadNode);
		}
		return threadNode;
	}

	private static boolean itemInArray(Object item, ILogMessage[] array){
		for (ILogMessage iLogMessage : array) {
			if (item==iLogMessage)
				return true;
		}
		return false;
	}
	
	@Override
	public void itemsAdded(final ILogMessage[] newItems) {
		
		
		//Now update the view per parent
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				Control control = viewer.getControl();
				if (control==null || control.isDisposed())
					return;
				Map<Object,List<ILogMessage>> messagesPerparent = new HashMap<Object,List<ILogMessage>>();
				//Sort the items per parent
				for (ILogMessage iLogMessage : newItems) {
					Object parent = iLogMessage.getParent();
					if (itemInArray(parent,newItems))
						continue;//Do not include children of children of this list.
					if (parent == null){
						OutlineThreadNode threadNode = getOrCreateThreadNodeFor(iLogMessage);
						if (!threadNode.hasChild(iLogMessage)){
							threadNode.addChild(iLogMessage);
							//Show the newly created thread node
							if (threadNode.getChildren().size()<=1){
								//viewer.expandToLevel(threadNode,0);
								viewer.expandToLevel(threadNode.getParent(), 1);
								//viewer.expandToLevel(threadNode, AbstractTreeViewer.ALL_LEVELS);
								//viewer.expandToLevel(threadNode, 1);
								//viewer.reveal(threadNode);
							}
						}
						parent = threadNode;
					}
					//viewer.add(parent,iLogMessage);
					
					List<ILogMessage> parentList = messagesPerparent.get(parent);
					if (parentList==null){
						parentList = new ArrayList<ILogMessage>();
						messagesPerparent.put(parent, parentList);
					}
					parentList.add(iLogMessage);
					
				}
				//No update the view per parent
				Set<Object> parents = messagesPerparent.keySet();
				for (Object aParent : parents) {
					//System.out.println("Adding to parent "+aParent);
					viewer.add(aParent, messagesPerparent.get(aParent).toArray());
				}
			}

		});
		
		
		//For every RootMessage that is not in the list, but having one or more children in the list, send an entryChanged notification
		for (ILogMessage iLogMessage : newItems) {
			ILogMessage parent = iLogMessage;
			while (parent!=null) {
				ILogMessage parentParent = parent.getParent();
				if (parentParent==null)//Item whithout a parent is a root
					if (!itemInArray(parent,newItems)){//Do not add it to the itemchanged list when the item was just added
						itemChanged(parent);
					}
				parent = parentParent;					
			}
		}

		
	}

	@Override
	public void itemsRemoved(final ILogMessage[] removedItems) {
		System.out.println(getClass().getName()+" - itemRemoved");
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				Control control = viewer.getControl();
				if (control==null || control.isDisposed())
					return;
				viewer.remove(removedItems);
			}
		});
	}

	private void itemChanged(final ILogMessage message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				Control control = viewer.getControl();
				if (control==null || control.isDisposed())
					return;
				viewer.refresh(message, true);
			}
		});
	}

}
