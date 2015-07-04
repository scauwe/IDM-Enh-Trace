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

import info.vancauwenberge.designer.enhtrace.action.OpenDetailAction;
import info.vancauwenberge.designer.enhtrace.action.copy.CopyMesageWithDetailsAction;
import info.vancauwenberge.designer.enhtrace.action.copy.CopyMessageAction;
import info.vancauwenberge.designer.enhtrace.action.copy.CopyMessageDetailsAction;
import info.vancauwenberge.designer.enhtrace.action.copy.CopyMessageTreeAction;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.novell.core.Core;

public abstract class AbstractTraceComposite <E extends Enum<E> & info.vancauwenberge.designer.enhtrace.editors.AbstractTraceComposite.IColmnnDefinition> extends Composite implements IDoubleClickListener{
	private static final String SAVE_OVERWRITE = "The file %1 already exists.  Would you like to overwrite it?"; // DSTraceViewerMessages.Save_Overwrite_Wrn
	private static final String SAVE_ERROR = "Unable to save contents to file %1.";// DSTraceViewerMessages.File_Save_Error
	public interface IColmnnDefinition{
		public String getTitle();
		public int getPercentage();
		public int getColumnIndex();
		public String getMessageFor(ILogMessageProvider message, boolean supportsMultiline, int baseTabIndex);
		public void paint(Event event, ILogMessageProvider message, int baseTabIndex);
	}
	
	private TableViewer logViewerTable;
	private AbstractTraceEditor<?> editor;
	private MenuManager menuMgr;
	private Action globalCopyAction;
	private Class<E> columnEnumClass;
	private Action globalFindAction;

	public AbstractTraceComposite(Class<E> columnEnumClass, Composite parent, int style, AbstractTraceEditor<?> editor) {
		super(parent, style);
		this.editor = editor;
		this.columnEnumClass = columnEnumClass;
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		createTraceTable();
		layout();

	}
	
	/**
	 * Add a selectionchange listener to the underlaying table
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		logViewerTable.addSelectionChangedListener(listener);
	}
	
	/**
	 * Remove a selectionchange listener from the underlaying table
	 * @param listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		logViewerTable.removeSelectionChangedListener(listener);
	}

	/**
	  * Refresh the table
	  */
	 public void refresh(){
	    	logViewerTable.refresh();
	 }
	 
	/**
	 * Get the selected log entries as text (for copy action)
	 * 
	 * @param tableItems
	 * @return
	 */
	protected abstract String getSelectedLog(TableItem[] selection);

	public void initGlobalActions() {
		
		//Copy Action
		this.globalCopyAction = new Action() {
			public void run() {
				TableItem[] selection =  logViewerTable.getTable().getSelection();
				if (selection==null || selection.length==0)
					return;
				String str = getSelectedLog(selection);
				
				Clipboard localClipboard = new Clipboard(logViewerTable.getTable().getDisplay());
				TextTransfer localTextTransfer = TextTransfer.getInstance();
				localClipboard.setContents(new String[] { str },
						new Transfer[] { localTextTransfer });
				localClipboard.dispose();
			}

		};
		this.globalCopyAction.setId(this.getClass().getName()+".copyText");
		this.globalCopyAction.setEnabled(true);
		
		//Search action
		this.globalFindAction = new Action() {
			public void run() {
				System.out.println("Action find called");
			}

		};
		this.globalFindAction.setId(this.getClass().getName()+".findText");
		this.globalFindAction.setEnabled(true);

		
		
		IActionBars globalActionBars = getEditor().getEditorSite().getActionBars();
		globalActionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), this.globalCopyAction);
		globalActionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), this.globalCopyAction);
		
		
		globalActionBars.updateActionBars();
	}

	 private void createColumns(Table table)
	    {
			E [] columns = columnEnumClass.getEnumConstants();
			

	        TableColumnLayout tableLayout = new TableColumnLayout();
			this.setLayout(tableLayout);

			for (int i = 0; i < columns.length; i++) {
				E column = columns[i];
		        createTableViewerColumn(column.getTitle(), column.getPercentage(), column.getColumnIndex());				
				TableColumn tableColumn = table.getColumn(column.getColumnIndex());
				tableColumn.pack();
				tableLayout.setColumnData(tableColumn, new ColumnWeightData(column.getPercentage(), table.getColumn(0).getWidth()));
				
			}
	    }
	
	protected abstract IContentProvider createContentProvider(AbstractTraceEditor<?> editor, TableViewer logViewerTable, boolean supportsVariableRowHeight);
	
	protected abstract LabelProvider<?> createLabelProvider(AbstractTraceEditor<?> editor, boolean supportsVariableRowHeight);
	
	/**
	 * Create the trace table
	 * 
	 * @param parentComposite
	 */
	private void createTraceTable() {
		FormLayout localFormLayout = new FormLayout();
		setLayout(localFormLayout);

		//this.logViewerTable = new LogMessageTableViewer(editor, parentComposite, SWT.FULL_SELECTION | SWT.MULTI);
		
		this.logViewerTable = new TableViewer(this, SWT.MULTI| SWT.FULL_SELECTION);

		Table table = logViewerTable.getTable();
        table.setBackground(getBackground());
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		createColumns(table);
		logViewerTable.setContentProvider(createContentProvider(editor, logViewerTable, false));//OS.IsLinux));
		LabelProvider<?> provider = createLabelProvider(editor, false);//OS.IsLinux);
        logViewerTable.setLabelProvider(provider);
        
		table.addListener(SWT.MeasureItem, provider);
		table.addListener(SWT.PaintItem, provider);
		table.addListener(SWT.EraseItem, provider);
		
		createMenuManager();

        
        logViewerTable.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				//System.out.println(event);
				editor.setSelection((IStructuredSelection)logViewerTable.getSelection());
			}
		});

		
        logViewerTable.setInput( createInput(editor));

        logViewerTable.addDoubleClickListener(this);

		

	}
	
	protected abstract Object createInput(AbstractTraceEditor<?> editor);

	/**
	 * Dispose this composite.
	 */
	public void dispose(){
		System.out.println(this.getClass().getName()+" dispose() start");
		if (this.globalCopyAction != null) {
			IActionBars localIActionBars = getEditor().getEditorSite().getActionBars();
			localIActionBars.clearGlobalActionHandlers();
			localIActionBars.updateActionBars();
			this.globalCopyAction.setEnabled(false);
			this.globalCopyAction = null;
		}

		if (this.logViewerTable != null) {
			this.logViewerTable.getTable().dispose();
			this.logViewerTable = null;
		}
		menuMgr.dispose();
		super.dispose();
		System.out.println(this.getClass().getName()+" dispose() end");
	}
	
	/** 
	 * Set the selection in the table, make sure the first item is visible.
	 * @param selection
	 */
	 public void setSelection(IStructuredSelection selection){
		//System.out.println(this.getClass().getName()+".setSelection():"+selection);

		 logViewerTable.setSelection(selection);
		 if (selection != null){
			 Object selected = selection.getFirstElement();
			 //System.out.println(this.getClass().getName()+".setSelection():"+selected);
			 if (selected != null){
				 //a simple reveal(selected) makes it visible, but not at the top of the table
				 //We calculate to see if it is visible. If not, we move it to the top of the table.
				 //int topindex = logViewerTable.getTable().getTopIndex();
				 //int bottomindex = topindex;
				 Rectangle bounds = logViewerTable.getTable().getClientArea();

				 //System.out.println("ClientArea:"+logViewerTable.getTable().getClientArea());
				 //System.out.println("Bounds:"+logViewerTable.getTable().getBounds());
				 /*int totalHeight = 0;
				 while(totalHeight<bounds.height && logViewerTable.getTable().getItemCount()>bottomindex){
					 Rectangle itemBounds = logViewerTable.getTable().getItem(bottomindex).getBounds();
					 System.out.println("Ading bounds:"+itemBounds);
					 totalHeight = totalHeight + itemBounds.height;
					 bottomindex++;
				 }
				 System.out.println("TopIndex:"+topindex);
				 System.out.println("BottomIndex:"+bottomindex);*/
				 TableItem[] itesm = logViewerTable.getTable().getItems();
				 for (int i = 0; i < itesm.length; i++) {
					TableItem tableItem = itesm[i];
					if (selected.equals(tableItem.getData())){
						Rectangle itemBounds = tableItem.getBounds();
						//System.out.println("Item Index:"+i);
						//System.out.println("Item Bounds:"+itemBounds);
						int maxyBound = Math.max(bounds.y,0);
						if (itemBounds.y < maxyBound || itemBounds.y+itemBounds.height > maxyBound+bounds.height){
						//if (i<=topindex || i>=bottomindex){
							//System.out.println("Moved item to top");
							logViewerTable.getTable().setTopIndex(i);
						}else{
							//System.out.println("Item already visible");
						}
						return;
					}
				}
			 }
		 }
	 }

	
	protected TableViewerColumn createTableViewerColumn(String header, int width, int idx) 
    {
        TableViewerColumn column = new TableViewerColumn(this.logViewerTable, SWT.LEFT, idx);
        column.getColumn().setText(header);
        column.getColumn().setWidth(width);
        column.getColumn().setResizable(true);
        column.getColumn().setMoveable(false);

        return column;
    }

	
	protected void createMenuManager() {
		Table table = logViewerTable.getTable();
		menuMgr = new MenuManager("#PopUp");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				Table table = logViewerTable.getTable();
				int index = table.getSelectionIndex();
				if (index == -1) 
					return; //no row selected

				TableItem item = table.getItem(index);
				Object data = item.getData();
				
				if (data != null && data instanceof ILogMessageProvider){
					final ILogMessage message = ((ILogMessageProvider)data).getLogMessage();
					menuMgr.add(new CopyMessageAction("Copy message", message));
					
					if (message.hasChildren()) {
						menuMgr.add(new CopyMessageTreeAction("Copy message tree", message));
					}

					if ( message.hasDetails()){
						menuMgr.add(new CopyMessageDetailsAction("Copy (XML) details", message));
						menuMgr.add(new CopyMesageWithDetailsAction("Copy message and (XML) detail", message));
					}
				}
			}
		});
		Control control = logViewerTable.getControl();
		final Menu menu = menuMgr.createContextMenu(control);
		control.setMenu(menu);
		/*
		 * if (getSite() != null) {
		 * logViewerTable.getSite().registerContextMenu(menuMgr,logViewerTable);
		 * }
		 */
		//table.addContextMenuManager(menuMgr);

		table.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent event) {
				menu.setLocation(event.x, event.y);
				menu.setVisible(true);
				/*while (!menu.isDisposed() && menu.isVisible()) {
					if (!menu.getDisplay().readAndDispatch())
						menu.getDisplay().sleep();
				}*/
			}
		});
	}
	
	/**
	 * Double clicked in the table
	 */
	public void doubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		 System.out.println("Selection:"+selection);
		    if (selection != null && selection instanceof IStructuredSelection) {
		      Object obj = ((IStructuredSelection) selection).getFirstElement();
			    System.out.println("Selected:"+obj);
		      // if we had a selection lets open the editor
		      if (obj != null && obj instanceof ILogMessageProvider) {
		    	  try{
		    		  OpenDetailAction action = new OpenDetailAction((IStructuredSelection) selection, editor);
		    		  action.run(null);
		    	  }catch (Exception e) {
		        		MessageBox dialog = 
			        			  new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
			        		dialog.setText("Detailed Trace");
			        		dialog.setMessage("No details to show for this message.");
			        		dialog.open(); 
				}
/*		    	  
		    	  
		    	  ILogMessage selectedMessage = ((ILogMessageProvider)obj).getLogMessage();
		    	  ILogMessage openRootMessage =  getDetailRootFor(selectedMessage, editor.getCurrentRoot());
		    	  Channel channel =null;
		    	  if (openRootMessage != null)
		    		  channel = editor.getChannelFor(openRootMessage);
		    	  
		    	  OpenDetailAction action = new OpenDetailAction(openRootMessage, channel, (IStructuredSelection)selection);
		    	  action.run(null);*/
		      }else{
		    	  System.out.println("Not correct selection");
		      }
		  }
	}
	

	
	public void saveToFile(String paramString) {
		
		if (this.logViewerTable.getTable().getItemCount() == 0)
			return;
		String str1 = getSaveFileName(paramString);
		if (str1 == null)
			return;
		File localFile = new File(str1);
		if ((localFile.exists())
				&& (!Core.questionDlg(String.format(SAVE_OVERWRITE,
						localFile.getName()))))
			return;
		try {
			_saveToFile(localFile);
		} catch (IOException localIOException) {
			String str2 = String.format(SAVE_ERROR, localFile.getName());
			Core.errorDlg(str2, localIOException);
		}
		
	}

	private void _saveToFile(File paramFile) throws IOException {
		/*
		 * FileOutputStream localFileOutputStream = new
		 * FileOutputStream(paramFile); TraceRtfGenerator localTraceRtfGenerator
		 * = new TraceRtfGenerator(new BufferedWriter(new
		 * OutputStreamWriter(localFileOutputStream)));
		 * getShell().setCursor(getShell().getDisplay().getSystemCursor(1));
		 * boolean bool = this.m_liveTraceHandler.isTracePaused(); if (!bool)
		 * pauseTrace(); for (TableItem localTableItem :
		 * this.m_liveTraceTable.getItems())
		 * localTraceRtfGenerator.write(localTableItem.getText(),
		 * localTableItem.getForeground()); if (!bool) startTrace();
		 * getShell().setCursor(null); localTraceRtfGenerator.close();
		 * localFileOutputStream.close();
		 */
	}

	private String getSaveFileName(String paramString) {
		FileDialog localFileDialog = new FileDialog(getShell(), 8192);
		String[] arrayOfString = new String[2];
		arrayOfString[0] = "*.rtf";
		arrayOfString[1] = "*.*";
		String str1 = ".rtf";
		localFileDialog.setFilterExtensions(arrayOfString);
		String str2 = paramString;
		if (-1 == str2.indexOf(str1))
			str2 = str2 + str1;
		localFileDialog.setFileName(str2);
		return localFileDialog.open();
	}

	public void updateTraceConfiguration() {
		/*
		 * String str1 = NLS.bind(DSTraceViewerMessages.Options_Dialog_Title,
		 * this.m_ldapConnection.getHost()); LDAPConfigurationDialog
		 * localLDAPConfigurationDialog = new
		 * LDAPConfigurationDialog(getShell(), str1,
		 * this.m_ldapConnection.getEventSpecifiers()); if ((1 ==
		 * localLDAPConfigurationDialog.open()) &&
		 * (!localLDAPConfigurationDialog.isPerformApply())) return; String str2
		 * = localLDAPConfigurationDialog.getEventPreferences(); if (str2 !=
		 * null) { localObject =
		 * TraceViewerPlugin.getDefault().getPluginPreferences();
		 * ((Preferences)localObject
		 * ).setValue("com.novell.core.dstracviewer.livetrace.events", str2); }
		 * Object localObject = localLDAPConfigurationDialog.getEventOptions();
		 * boolean bool = this.m_liveTraceHandler.isTracePaused(); if (!bool)
		 * pauseTrace();
		 * getShell().setCursor(getShell().getDisplay().getSystemCursor(1));
		 * this.m_ldapConnection.setEventSpecifiers((int[])localObject);
		 * getShell().setCursor(null); if (!bool) startTrace();
		 */
	}

	public AbstractTraceEditor<?> getEditor() {
		return editor;
	}
	
	/*
	 * private synchronized boolean getBooleanPreference(String paramString) {
	 * return
	 * TraceViewerPlugin.getDefault().getPluginPreferences().getBoolean(paramString
	 * ); }
	 * 
	 * private synchronized int getIntPreference(String paramString) { return
	 * TraceViewerPlugin
	 * .getDefault().getPluginPreferences().getInt(paramString); }
	 */

}
