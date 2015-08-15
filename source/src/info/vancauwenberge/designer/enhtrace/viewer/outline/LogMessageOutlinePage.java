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
import info.vancauwenberge.designer.enhtrace.action.OpenDetailAction;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editors.LiveTraceEditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.novell.core.Core;


public class LogMessageOutlinePage extends ContentOutlinePage {
	
	private LiveTraceEditor editor;
	private MenuManager menuManager;
	protected Combo combo;
	private Action m_expandAction;
	private Action m_collapseAction;
	private TraceFilter traceFilter;
	
	private class ComboContributionItem extends ControlContribution implements Listener{

		public ComboContributionItem() {
	        super("info.vancauwenberge.designer.enhtrace.viewer.outline.LogMessageOutlinePage.ComboContributionItem");
	    }

	    @Override
	    protected Control createControl(Composite parent) {
	    	System.out.println(this.getClass().getName()+" createControl() start");
    		combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
    		combo.setToolTipText("Define a tracename filter");
    		
   			combo.setItems(traceFilter.getOptions());
   			combo.select(traceFilter.getSelectedIndex());
   			
            //Listen for selection on the combo
    		combo.addListener(SWT.DefaultSelection, this);
    		combo.addListener(SWT.Selection, this);
    		
    		traceFilter.addListener(TraceFilter.OPTIONS_ADDED, this);
    		
	    	System.out.println(this.getClass().getName()+" createControl() end");    		
	        return combo;
	    }
	    
	    @Override
		public void handleEvent(final Event event) {
	    	switch (event.type) {
			case TraceFilter.OPTIONS_ADDED:
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (!combo.isDisposed()){
							combo.setItems((String[]) event.data);
							combo.select(event.index);
							//combo.pack();
						}
					}
				});
				break;

			default:
	    		traceFilter.setFiltereValue(combo.getText());
				getTreeViewer().refresh();
				break;
			}
	    }
	}
	
	
	
	private class DoubleClickDefaultMenuHandler implements IDoubleClickListener,MenuListener, IOpenListener{
		
		
		
		private Menu menu;
		public DoubleClickDefaultMenuHandler(Menu menu) {
			this.menu = menu;
		}

		@Override
		public void doubleClick(DoubleClickEvent event) {
			System.out.println("DBLclikc selection:"+event.getSelection());
			System.out.println("DBLclikc source:"+event.getSource());
			System.out.println("DBLclikc viewer:"+event.getViewer());
			try{
				menu.notifyListeners(SWT.Show, new Event());
				try{
					OpenDetailAction action = new OpenDetailAction((IStructuredSelection) event.getSelection(), editor);
					action.run(null);
				}catch (Exception e) {
					MessageBox dialog = 
							new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Detailed Trace");
					dialog.setMessage("No details to show for this message.");
					dialog.open(); 
				}
			}catch (Exception e) {
				Activator.log("Failed to notify listeners", e);
			}

			/*
			 * 
			//MenuItem defaultItem = getDefaultMenuItem();
			//if (defaultItem != null) {
				//CommandContributionItem contribution = (CommandContributionItem) defaultItem.getData();
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				//ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
				try {
					//System.out.println("Default:"+defaultItem.getData());					
					Event evt = new Event();
					evt.data = event;
					evt.display = event.getViewer().getControl().getDisplay();
					evt.widget = event.getViewer().getControl();
					evt.item = event.getViewer().getControl();
					handlerService.executeCommand(OpenDetailCommand.class.getName(), evt);
					//commandService.getCommand("commandId").
					
					//handlerService.executeCommand(contribution.getCommand(), null);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			//}
			 * */
			System.out.println("DBLclikc done.");
		}
		
		@Override
		public void menuShown(MenuEvent e) {
			menu.setDefaultItem(getDefaultMenuItem());				
		}
		
		@Override
		public void menuHidden(MenuEvent e) {
			
			
		}
		private MenuItem getDefaultMenuItem() {
			System.out.println("Default");
			for (MenuItem menuItem : menu.getItems()) {
				Object data = menuItem.getData();
				System.out.println("Default:"+menuItem.getID());
				System.out.println("Default:"+data);
				if (data instanceof CommandContributionItem) {
					CommandContributionItem contributionItem = (CommandContributionItem) data;
					if (contributionItem.getId() != null && contributionItem.getId().endsWith(".default")) {
						return menuItem;
					}
				}else if (data instanceof ActionContributionItem){
					ActionContributionItem contributionItem = (ActionContributionItem) data;
					if (contributionItem.getId() != null && contributionItem.getId().endsWith(".default")) {
						return menuItem;
					}
				}
			}
			return null;
		}

		@Override
		public void open(OpenEvent event) {
			System.out.println("open:"+event);
			// TODO Auto-generated method stub
			
		}
	}

	public LogMessageOutlinePage(LiveTraceEditor enhTraceEditor) {
		this.editor = enhTraceEditor;
	}

	public void dispose(){
		System.out.println(this.getClass().getName()+" dispose() start");
		menuManager.dispose();
		super.dispose();
		System.out.println(this.getClass().getName()+" dispose() end");
	}
	
	@Override 
	public void makeContributions(      IMenuManager menuManager,      IToolBarManager toolBarManager,      IStatusLineManager statusLineManager){
		super.makeContributions(menuManager,toolBarManager,statusLineManager);
        //contentOutlineStatusLineManager=statusLineManager;
	}
	
	
	
	public void createControl(Composite parent){
		System.out.println("LogMessageOutlinePage: createControl() start");
		super.createControl(parent);
		final TreeViewer viewer = getTreeViewer();
		//viewer.setAutoExpandLevel(3);
		viewer.setContentProvider(new OutlineContentProvider(viewer, ((LiveTraceEditorInput)editor.getEditorInput())));
		viewer.setLabelProvider(new OutlineLabelProvider());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if(event.getSource()==this)
					return;
				//System.out.println(event);
				editor.setSelection((ITreeSelection)viewer.getSelection());
			}
		});
		
		viewer.setInput(editor.getEditorInput());
		
		//Add a context menu extention point
		menuManager =  new  MenuManager ();
		//allow additions
		menuManager.add ( new  Separator( IWorkbenchActionConstants.MB_ADDITIONS)) ; 
		menuManager.setRemoveAllWhenShown ( true );

		Control control = viewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);

		//Handle double click on teh default item
		DoubleClickDefaultMenuHandler handler = new DoubleClickDefaultMenuHandler(menu);
		
		viewer.addDoubleClickListener(handler);
		//viewer.addOpenListener(handler);
		//menu.addMenuListener(handler);
		
		//allow contributions using this id
		getSite().registerContextMenu("info.vancauwenberge.designer.enhtrace.viewer.outline", menuManager, viewer);
		
		
		if (traceFilter == null)
			traceFilter = new TraceFilter(((LiveTraceEditorInput)editor.getEditorInput()));

		viewer.addFilter(traceFilter);
		IActionBars actionBars = getSite().getActionBars();
        IToolBarManager itoolbarmanager = actionBars.getToolBarManager();
        
        //
        //Expand and collapse actions
        //
        if (m_expandAction == null){
        	m_expandAction = new Action(null, Core.getImageDescriptor("icons/expand_all.gif")) {

        		public void run()
        		{
        			getTreeViewer().expandAll();

        		}

        	};
        	m_expandAction.setToolTipText("Expand all");
        }
        itoolbarmanager.add(m_expandAction);
        
        if (m_collapseAction == null){
        	m_collapseAction = new Action(null, Core.getImageDescriptor("icons/collapse_all.gif")) {

        		public void run()
        		{
        			getTreeViewer().collapseAll();
        		}
        	};
        	m_collapseAction.setToolTipText("Collapse all");
        }

        itoolbarmanager.add(m_collapseAction);
        itoolbarmanager.add(new Separator());

	    itoolbarmanager.add(new ComboContributionItem());
	    
		System.out.println("LogMessageOutlinePage: createControl() end");

		
	}
	

	
	public void setSelection(IStructuredSelection selection){
		//super.setSelection(selection);
    	Object selected = selection.getFirstElement();
    	if (selected != null && selected instanceof ILogMessageProvider){
    		final ILogMessageProvider provider = (ILogMessageProvider)selected;
    		//TODO: this does not seem to work :-(
    		System.out.println("Outline: setting focus on "+selected);
    		System.out.println("Outline: setting focus on "+provider.getLogMessage());
    		TreeViewer viewer = getTreeViewer();
    		viewer.setSelection(new IStructuredSelection() {
				
				@Override
				public boolean isEmpty() {
					return false;
				}
				
				@Override
				public List<?> toList() {
					List<ILogMessage> result = new ArrayList<ILogMessage>(1);
					result.add(provider.getLogMessage());
					return result;
				}
				
				@Override
				public Object[] toArray() {
					return new Object[]{provider.getLogMessage()};
				}
				
				@Override
				public int size() {
					return 1;
				}
				
				@Override
				public Iterator<?> iterator() {
					return toList().iterator();
				}
				
				@Override
				public Object getFirstElement() {
					return provider.getLogMessage();
				}
			}, true);
    		viewer.expandToLevel(provider.getLogMessage(), 1);
    		//viewer.reveal(provider.getLogMessage());
    	}
	}


}
