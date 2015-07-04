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
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.viewer.table.LiveLinuxTableContentProvider;
import info.vancauwenberge.designer.enhtrace.viewer.table.LiveWindowsTableContentProvider;
import info.vancauwenberge.designer.enhtrace.viewer.table.LogMessageWrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

public class LiveTraceComposite extends AbstractTraceComposite<info.vancauwenberge.designer.enhtrace.editors.LiveTraceComposite.Column> implements IDoubleClickListener {
	public static final int TEXT_MARGIN = 3;

	enum Column implements info.vancauwenberge.designer.enhtrace.editors.AbstractTraceComposite.IColmnnDefinition{
		TIMESTAMP("Timestamp",7,0) {
			public String getMessageFor(ILogMessageProvider message, boolean supportsMultiline, int baseTabIndex) {
				if (message instanceof ILogMessage){
					DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
					return df.format(new Date(message.getLogMessage().getTimeStamp()));
				}
				return "";
			}
		},
		TRACENAME("Tracename",7,1) {
			@Override
			public String getMessageFor(ILogMessageProvider message, boolean supportsMultiline, int baseTabIndex) {
				if (message instanceof ILogMessage)
					return message.getLogMessage().getTraceName();
				return "";
			}
		},
		THREAD("Thread",3,2) {
			@Override
			public String getMessageFor(ILogMessageProvider message, boolean supportsMultiline, int baseTabIndex) {
				if (message instanceof ILogMessage)
					return message.getLogMessage().getThread();
				return "";
			}
		},
		MESSAGE("Message",83,3) {
			@Override
			public String getMessageFor(ILogMessageProvider message, boolean supportsMultiline, int baseTabIndex) {
				if (message instanceof ILogMessage){
					ILogMessage logMessage = (ILogMessage)message;
					if (supportsMultiline){
						String detail = logMessage.getMessageDetail();
						if (detail != null){
							StringBuilder sb = new StringBuilder(logMessage.getMessage());
							sb.append('\n');
							sb.append(detail);
							return LabelProvider.getIndentedString(baseTabIndex, logMessage.getTabIndex(),sb.toString());
						}
						return LabelProvider.getIndentedString(baseTabIndex, logMessage.getTabIndex(),logMessage.getMessage());
					}else{
						String messageStr = logMessage.getMessage();
						return LabelProvider.getIndentedString(baseTabIndex, logMessage.getTabIndex(),messageStr.split("\\n")[0]);
					}
				}else{
					return LabelProvider.getIndentedString(baseTabIndex, message.getLogMessage().getTabIndex(), message.getMessage());
	            	}
			}
			public void paint(Event event, ILogMessageProvider message, int baseTabIndex){
        		if (message instanceof ILogMessage){
        			ILogMessage logMessage = ((ILogMessage)message); 
        			if (logMessage.hasDetails()){
            			String detail = logMessage.getMessageDetail();
        				String normalText = LabelProvider.getIndentedString(baseTabIndex,logMessage.getTabIndex(), logMessage.getMessage());
        				TableItem tableItem = (TableItem) event.item;					
        				String fullText = tableItem.getText(event.index);
        				Point fullTextSize = event.gc.textExtent(fullText);
        				Point normalTextsize = event.gc.textExtent(normalText);
        				int yOffset = Math.max(0, (event.height - fullTextSize.y) / 2);
        				event.gc.drawText(normalText, event.x + TEXT_MARGIN, event.y
							+ yOffset, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
					
        				event.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
        				event.gc.drawText(detail, event.x + TEXT_MARGIN, event.y + yOffset + normalTextsize.y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
        				return;
        			}else{
        				super.paint(event, message,baseTabIndex);
        			}
        		}
        		else if (message instanceof LogMessageWrapper){
        			LogMessageWrapper wrapper = (LogMessageWrapper)message;
        			if (!wrapper.isMainMessagePart())
        				event.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
        			super.paint(event, message,baseTabIndex);
        		}				
			}
		},
		;

		private String header;
		private int percentage;
		private int index;

		
		
		
		private Column(String header, int percentage, int index){
			this.header = header;
			this.percentage = percentage;
			this.index = index;
		}

		@Override
		public String getTitle() {
			return header;
		}

		@Override
		public int getPercentage() {
			return percentage;
		}

		@Override
		public int getColumnIndex() {
			return index;
		}
		
		public abstract String getMessageFor(ILogMessageProvider message, boolean supportsMultiline, int baseTabIndex);
		
		public void paint(Event event, ILogMessageProvider message, int baseTabIndex){
			TableItem tableItem = (TableItem) event.item;
			String text = tableItem.getText(event.index);
			Point size = event.gc.textExtent(text);
			int yOffset = Math.max(0, (event.height - size.y) / 2);
			// event.gc.drawText(str, event.x, event.y + i, true);
			event.gc.drawText(text, event.x + TEXT_MARGIN, event.y
					+ yOffset, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
		}
		
		public static Column getColumnDefenition(int index){
			Column[] values = Column.values();
			for (Column column : values) {
				if (column.getColumnIndex()==index)
					return column;
			}
			return null;
		}
		
	}

	public LiveTraceComposite(Composite parentComposit, LiveTraceEditor editor) {
		super(Column.class, parentComposit, 0, editor);
		
		// TraceViewerPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this.m_prefListener);
	}



    protected Object createInput(AbstractTraceEditor<?> editor){
		 return  ((LiveTraceEditorInput)editor.getEditorInput());
	 }

	 protected IContentProvider createContentProvider(AbstractTraceEditor<?> editor, TableViewer logViewerTable, boolean supportsVariableRowHeight){
		 if (supportsVariableRowHeight)
			 return new LiveLinuxTableContentProvider(logViewerTable,  ((LiveTraceEditorInput)editor.getEditorInput()));
		 return new LiveWindowsTableContentProvider(logViewerTable,  ((LiveTraceEditorInput)editor.getEditorInput()));
	 }

	 protected LabelProvider<?> createLabelProvider(AbstractTraceEditor<?> editor, boolean supportsVariableRowHeight){
		 return new LabelProvider<Column>(Column.class, supportsVariableRowHeight,0);
	 }
	 

	/**
	 * Get the selected log entries as text (for copy action)
	 * 
	 * @param tableItems
	 * @return
	 */
	protected String getSelectedLog(TableItem[] tableItems) {
		StringBuilder sb = new StringBuilder();
		for (TableItem tableItem : tableItems) {
			sb.append(tableItem.getText());
			sb.append('\n');
		}
		return sb.toString();
	}



}