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
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.Channel;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.EditorPart;

public abstract class AbstractTraceEditor<E extends Enum<E> & info.vancauwenberge.designer.enhtrace.editors.AbstractTraceEditor.IToolbarAction> extends EditorPart{
	public interface IToolbarAction{
		public String getLabel();
		public String getToolTip();
		public boolean isDefaultEnabled();
		public ImageDescriptor getImage();
		public boolean addSeperator();
	}

	private static class ColorChanger implements Runnable 
    {
		protected Control widget;
		protected Integer swtColor;
		public ColorChanger(Control widget, Integer swtColor){
			this.widget = widget;
			this.swtColor = swtColor;
		}
            @Override
            public void run()
            {
				System.out.println("AbstractTraceEditor.setToolbarLabel().run() bold");
				if (widget.isDisposed())
					return;
				try {
					if (swtColor!=null)
						widget.setForeground(widget.getDisplay().getSystemColor(swtColor));
					else
						widget.setForeground(null);
					widget.redraw();
					widget.update();
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
    };

	
	private Label toolbarTitleLabel;
	private ToolBarManager toolBarManager;
	private Composite tableAreaComposit;
	private final Separator toolbarSeparator = new Separator("editActions");
	private Map<E, Action> actionEnumMap ;
	private Class<E> actionEnumClass;

	public AbstractTraceEditor(Class<E> actionEnumClass) {
		this.actionEnumMap = new EnumMap<E, org.eclipse.jface.action.Action>(actionEnumClass);
		this.actionEnumClass = actionEnumClass;
    }
	
	public abstract void setSelection(IStructuredSelection selection);

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setInput(input);
		setSite(site);
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		//Not yet supported
	}
	@Override
	public void doSaveAs() {
		//Not yet supported
	}
	
	@Override
	public boolean isDirty() {
		return false;
	}
	@Override
	public boolean isSaveAsAllowed() {		
		return false;
	}

	protected void setToolbarLabel(String toolbarLabel) {
		System.out.println("AbstractTraceEditor.setToolbarLabel() start");
		//Test the size of the text
		/*
		GC gc = new GC(toolbarTitleLabel.getDisplay());
		Point labelSize = toolbarTitleLabel.getSize();
		while (gc.textExtent(toolbarLabel).x > labelSize.x && toolbarLabel.length()>2){
			toolbarLabel = toolbarLabel.substring(0, toolbarLabel.length()-2) +'\u2026'; 
		}
*/
		
		this.toolbarTitleLabel.setText(toolbarLabel);
		//Blink the label so that the change is visible
		Thread blink = new Thread() {
			
			@Override
			public void run() {
				System.out.println("AbstractTraceEditor.setToolbarLabel().run() start");
				/*
				Runnable r1 = new Runnable()
                {
                        @Override
                        public void run()
                        {
    						System.out.println("AbstractTraceEditor.setToolbarLabel().run() bold");
        					if (toolbarTitleLabel.isDisposed())
        						return;
        					
            				FontData fontData = toolbarTitleLabel.getFont().getFontData()[0];
        					Font font = new Font(toolbarTitleLabel.getDisplay(), new FontData(fontData.getName(), fontData .getHeight(), SWT.BOLD));
        					try {
        						toolbarTitleLabel.setFont(font);
        						toolbarTitleLabel.redraw();
        						toolbarTitleLabel.update();
        					} catch (Exception e) {
        						e.printStackTrace();
        					}
        					font.dispose();
                        }
                };
				Runnable r2 = new Runnable()
                {
                        @Override
                        public void run()
                        {
       						System.out.println("AbstractTraceEditor.setToolbarLabel().run() normal");
        					if (toolbarTitleLabel.isDisposed())
        						return;
       						toolbarTitleLabel.setFont(null);
                        }
                };
				
					try {
						toolbarTitleLabel.getDisplay().syncExec(r1);
						Thread.sleep(1000);
						toolbarTitleLabel.getDisplay().syncExec(r2);
						Thread.sleep(1000);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				try {
					ColorChanger changer = new ColorChanger(toolbarTitleLabel, SWT.COLOR_DARK_GREEN);
					toolbarTitleLabel.getDisplay().asyncExec(changer);
					Thread.sleep(500);
					changer.swtColor=SWT.COLOR_GREEN;
					toolbarTitleLabel.getDisplay().asyncExec(changer);
					Thread.sleep(500);
					changer.swtColor=SWT.COLOR_DARK_GREEN;
					toolbarTitleLabel.getDisplay().asyncExec(changer);
					Thread.sleep(500);
					changer.swtColor=null;
					toolbarTitleLabel.getDisplay().asyncExec(changer);
				} catch (InterruptedException e) {
				}
				
				System.out.println("AbstractTraceEditor.setToolbarLabel().run() stop");
			}
		};
		blink.setDaemon(true);
		blink.setName("blinkLabel");
		blink.start();
		System.out.println("AbstractTraceEditor.setToolbarLabel() done");
	}

	
	private void createToolBarArea(Composite editorBaseComposit)
	  {
	    //No need to remember toolbarAreaComposite: it is disposed with it's parent.
		Composite toolbarAreaComposite = new Composite(editorBaseComposit, 0);
	    GridLayout localGridLayout1 = new GridLayout(2, false);
	    localGridLayout1.horizontalSpacing = 0;
	    localGridLayout1.verticalSpacing = 0;
	    localGridLayout1.marginHeight = 0;
	    localGridLayout1.marginWidth = 0;
	    toolbarAreaComposite.setBackgroundMode(SWT.INHERIT_FORCE);
	    
	    toolbarAreaComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
	    //toolbarAreaComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
	    //toolbarAreaComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
	    toolbarAreaComposite.setLayout(localGridLayout1);
	    toolbarAreaComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    
	    
	    this.toolbarTitleLabel = new Label(toolbarAreaComposite,0);
	    this.toolbarTitleLabel.setText(createToolbarLabel());
	    toolbarTitleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    
	    //No need to remember toolbar: it is disposed with it's parent.
	    ToolBar toolBar = new ToolBar(toolbarAreaComposite, SWT.HORIZONTAL | SWT.FLAT | SWT.RIGHT | SWT.WRAP);
	    toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
	    toolBarManager = new ToolBarManager(toolBar);
	    createToolBarActions(toolBarManager);
	    toolbarAreaComposite.pack();
	    this.toolBarManager.update(true);
	    //setToolbarLabel(createToolbarLabel());
	  }


	public org.eclipse.jface.action.Action getAction(E key){
		return actionEnumMap.get(key);
	}
	
	/**
	 * Create all the toolbar actions
	 * @param toolBarManager2 
	 */
	protected void createToolBarActions(ToolBarManager toolBarManager)
	  {
		E [] actions = actionEnumClass.getEnumConstants();
		

		for (int i = 0; i < actions.length; i++) {
			final E anAction = actions[i];
			org.eclipse.jface.action.Action act = new org.eclipse.jface.action.Action(anAction.getLabel(),anAction.getImage())
		    {
		    	public void run()
		    	  {
		    		doaction(anAction);
		    	  }

		    };
		    
		    act.setToolTipText(anAction.getToolTip());
		    act.setEnabled(anAction.isDefaultEnabled());
		    actionEnumMap.put(anAction, act);
		    toolBarManager.add(act);
		    if (anAction.addSeperator())
		    	toolBarManager.add(this.toolbarSeparator);
			
		}

	  }

	protected abstract String createToolbarLabel();
	
	/**
	 * One of the toolbar buttons has been pressed. Do the associated action
	 * @param anAction
	 */
	protected abstract void doaction(E anAction);
	
	public void dispose() {
		System.out.println(this.getClass().getName()+" dispose() start");
		//toolbarAreaComposite.dispose();
		//tableAreaComposit.dispose();
	    //toolBar.dispose();
	    toolBarManager.dispose();
	    toolbarSeparator.dispose();
	    super.dispose();
		System.out.println(this.getClass().getName()+" dispose() end");
	}
	
	@Override
	public void setFocus() {
		this.tableAreaComposit.setFocus();		
	}
	
	public void createPartControl(Composite parent) {
		Composite editorBaseComposit = new Composite(parent, 0);
	    GridLayout localGridLayout = new GridLayout(1, false);
	    localGridLayout.horizontalSpacing = 0;
	    localGridLayout.verticalSpacing = 0;
	    localGridLayout.marginHeight = 0;
	    localGridLayout.marginWidth = 0;
	    editorBaseComposit.setLayout(localGridLayout);
	    org.eclipse.swt.graphics.Color background = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
	    editorBaseComposit.setBackground(background);
	    createToolBarArea(editorBaseComposit);
	    tableAreaComposit = createTableArea(editorBaseComposit);
	    AbstractTraceComposite<?> traceComposit = createTraceContentComposit (tableAreaComposit);
	    //tableAreaComposit = createTraceComposit (editorBaseComposit);
	    this.tableAreaComposit.layout();
	    traceComposit.initGlobalActions();
	}


	/**
	 * create the composite that will contain the table
	 * @param editorBaseComposit
	 * @return
	 */
	private static Composite createTableArea(Composite editorBaseComposit) {
	    Composite traceComposite = new Composite(editorBaseComposit, 0);
	    traceComposite.setBackground(editorBaseComposit.getBackground());
	    GridLayout localGridLayout2 = new GridLayout(1, true);
	    localGridLayout2.horizontalSpacing = 0;
	    localGridLayout2.verticalSpacing = 0;
	    localGridLayout2.marginHeight = 0;
	    localGridLayout2.marginWidth = 0;
	    traceComposite.setLayout(localGridLayout2);
	    traceComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    return traceComposite;
	}

	/**
	 * 
	 * @param m_traceComposite2
	 * @return
	 */
	protected abstract AbstractTraceComposite<?> createTraceContentComposit(Composite m_traceComposite2);
	
	
	/**
	 * Get the channel for the given message
	 * @return
	 */
	public abstract  Channel getChannelFor(ILogMessage message);

	/**
	 * Get the current root (for details) or null if not any
	 * @return
	 */
	public abstract ILogMessage getCurrentRoot();

	
}
