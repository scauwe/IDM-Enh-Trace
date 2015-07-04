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
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.Channel;
import info.vancauwenberge.designer.enhtrace.viewer.table.DetailedLinuxTableContentProvider;
import info.vancauwenberge.designer.enhtrace.viewer.table.DetailedWindowsTableContentProvider;
import info.vancauwenberge.designer.enhtrace.viewer.table.LogMessageWrapper;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

public class DetailedTraceComposite extends AbstractTraceComposite<info.vancauwenberge.designer.enhtrace.editors.DetailedTraceComposite.Column> {
	enum Column implements info.vancauwenberge.designer.enhtrace.editors.AbstractTraceComposite.IColmnnDefinition{
		MESSAGE("Message",100,0);
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

		
		@Override
		public String getMessageFor(ILogMessageProvider element, boolean supportsMultiline, int baseTabIndex) {
            if(element instanceof ILogMessage){
				ILogMessage message = (ILogMessage)element;
				if (supportsMultiline){
					StringBuilder sb = new StringBuilder(message.getMessage());
					String detail = message.getMessageDetail();
					if (detail != null){
						sb.append('\n');
						sb.append(detail);
					}
					return LabelProvider.getIndentedString(baseTabIndex, message.getTabIndex(), sb.toString());
				}else{
					String messageStr = message.getMessage();
					return LabelProvider.getIndentedString(baseTabIndex, message.getTabIndex(), messageStr.split("\\n")[0]);

				}
            }
            if (element instanceof ILogMessageProvider){
				ILogMessage message = ((ILogMessageProvider)element).getLogMessage();
            	return LabelProvider.getIndentedString(baseTabIndex, message.getTabIndex(), ((ILogMessageProvider)element).getMessage());
            }
			return "";
		}

		@Override
		public void paint(Event event, ILogMessageProvider element, int baseTabIndex){
			if (element instanceof ILogMessage){
				ILogMessage message = ((ILogMessage)element); 
				String detail = message.getMessageDetail();
				if (message.hasDetails()){
					String normalText = LabelProvider.getIndentedString(baseTabIndex, message.getTabIndex(), message.getMessage());
					TableItem tableItem = (TableItem) event.item;					
					String fullText = tableItem.getText(event.index);
					Point fullTextSize = event.gc.textExtent(fullText);
					Point normalTextsize = event.gc.textExtent(normalText);
					int yOffset = Math.max(0, (event.height - fullTextSize.y) / 2);
					event.gc.drawText(normalText, event.x + LabelProvider.TEXT_MARGIN, event.y
							+ yOffset, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
					
					event.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
					event.gc.drawText(detail, event.x + LabelProvider.TEXT_MARGIN, event.y + yOffset + normalTextsize.y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
					return;
				}
			}else if (element instanceof LogMessageWrapper){
    			LogMessageWrapper wrapper = (LogMessageWrapper)element;
    			if (!wrapper.isMainMessagePart())
    				event.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
    		}

   			TableItem tableItem = (TableItem) event.item;
   			String text = tableItem.getText(event.index);
   			Point size = event.gc.textExtent(text);
   			int yOffset = Math.max(0, (event.height - size.y) / 2);
   			// event.gc.drawText(str, event.x, event.y + i, true);
   			event.gc.drawText(text, event.x + LabelProvider.TEXT_MARGIN, event.y
   					+ yOffset, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
		}
		
	}

	public DetailedTraceComposite(Composite parentComposit, DetailedTraceEditor editor) {
		super(Column.class, parentComposit, 0, editor);
	}


	 protected Object createInput(AbstractTraceEditor<?> editor){
		 return  ((StaticTraceEditorInput)editor.getEditorInput()).getRoot();
	 }
	 
	 protected IContentProvider createContentProvider(AbstractTraceEditor<?> editor, TableViewer logViewerTable, boolean supportsVariableRowHeight){
		 ILogMessage root =   ((StaticTraceEditorInput)editor.getEditorInput()).getRoot();
		 if (supportsVariableRowHeight)
			 return new DetailedLinuxTableContentProvider(this, root);
		 else
			 return new DetailedWindowsTableContentProvider(this, root);
	 }
	 
	 protected LabelProvider<?> createLabelProvider(AbstractTraceEditor<?> editor, boolean supportsVariableRowHeight){
		 ILogMessage root =   ((StaticTraceEditorInput)editor.getEditorInput()).getRoot();
		 return new LabelProvider<Column>(Column.class, supportsVariableRowHeight, root.getTabIndex());
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