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

import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;
import info.vancauwenberge.designer.enhtrace.editors.AbstractTraceComposite.IColmnnDefinition;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

public class LabelProvider <E extends Enum<E> & info.vancauwenberge.designer.enhtrace.editors.AbstractTraceComposite.IColmnnDefinition> implements ITableLabelProvider, ITableColorProvider, Listener{
	
	private int tmpMaxHeigth = 0;
	private boolean supportsMultiline;
	private int rootIndentation;
	private IColmnnDefinition[] columns;
	public static final int TEXT_MARGIN = 3;
	private static final Pattern beginOfLine = Pattern.compile("^", Pattern.MULTILINE);
	
	public LabelProvider(Class<E> columnEnumClass, boolean supportsMultiline, int rootIndentation) {
		super();
		this.supportsMultiline = supportsMultiline;
		this.rootIndentation = rootIndentation;
		E [] enumColumns = columnEnumClass.getEnumConstants();
		this.columns = new IColmnnDefinition[enumColumns.length];
		for (E e : enumColumns) {
			columns[e.getColumnIndex()] = e;
		}			
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public org.eclipse.swt.graphics.Color getBackground(Object element,
			int columnIndex) {
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		IColmnnDefinition columDef = columns[columnIndex];
		return columDef.getMessageFor((ILogMessageProvider)element, supportsMultiline,rootIndentation);
	}
	
	
	@Override
	public org.eclipse.swt.graphics.Color getForeground(Object element, int columnIndex) {
		return ((ILogMessageProvider)element).getLogMessage().getSWTColor();
	}
	
	@Override
	public void handleEvent(Event event) {
		Object element = event.item.getData();
		switch (event.type) {
		case SWT.MeasureItem:
			measure(event, element);
			break;
		case SWT.PaintItem:
			paint(event, element);
			break;
		case SWT.EraseItem:
			erase(event, element);
			break;
		default:
        	System.out.println("Unsoported event:"+event.type);
		}
	}

	public static String getIndentedString(int baseTabIndex, int indentation, String message){
    	indentation = indentation - baseTabIndex;
    	final String strIndent;
    	if (indentation>0){
    		char[] indent = new char[indentation];
    		Arrays.fill(indent, ' ');
    		strIndent = new String(indent);
    		//pre-pend the spaces to any beginning of line
			return beginOfLine.matcher(message).replaceAll(strIndent);
    	}else{
    		return message;
    	}

	}

	
	protected void paint(Event event, Object element) {
		IColmnnDefinition columDef = columns[event.index];
		columDef.paint(event, (ILogMessageProvider)element, rootIndentation);
	}

	protected void erase(Event event, Object element) {
		event.detail &= ~SWT.FOREGROUND;
	}
	
	protected void measure(Event event, Object element) {
		TableItem tableItem = (TableItem) event.item;
		String text = tableItem.getText(event.index);
		if (!supportsMultiline){
			if (text.contains("\n")){
				System.out.println("<><><><><><><><>Multiple lines detected while it should be single line!!!!");
				System.out.println(text);
			}
		}
		Point size = event.gc.textExtent(text);
		event.width = size.x + 2 * LabelProvider.TEXT_MARGIN;
		event.height = size.y;// + TEXT_MARGIN; //Math.max(event.height, size.y + TEXT_MARGIN);
		if (event.height>tmpMaxHeigth){
			tmpMaxHeigth = event.height;
			System.out.println("<><><><><><><><>NEW HEIGHTS:");
			System.out.println("<><><><><><><><>NEW HEIGHTS:"+tmpMaxHeigth);
			System.out.println("<><><><><><><><>NEW HEIGHTS:");
			System.out.println(text);
		}
	}

}