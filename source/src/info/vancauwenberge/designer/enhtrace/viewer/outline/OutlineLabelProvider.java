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

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.IRootLogMessage;
import info.vancauwenberge.designer.enhtrace.model.logmessage.StatusLogMessage;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.novell.nds.dirxml.util.XdsDN;

class OutlineLabelProvider implements ILabelProvider, IColorProvider{

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
		System.out.println(this.getClass().getName()+" dispose() start");
		System.out.println(this.getClass().getName()+" dispose() end");
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Color getForeground(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String trimSubstring(StringBuilder sb) {
	    int first, last;

	    for (first=0; first<sb.length(); first++)
	        if (!Character.isWhitespace(sb.charAt(first)))
	            break;

	    for (last=sb.length(); last>first; last--)
	        if (!Character.isWhitespace(sb.charAt(last-1)))
	            break;

	    return sb.substring(first, last);
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof IOutlineParentNode){
			return ((IOutlineParentNode)element).getLabel();
		}else if(element instanceof ILogMessage){
        	ILogMessage message = (ILogMessage)element;

        	if ("Permission Driver".equals(message.getTraceName())){
				System.out.println(">>>Updating label for: "+message);
			}

        	
        	String text = message.getMessage();
        	StringBuilder sb = new StringBuilder();
        	if (message instanceof IRootLogMessage ){
        		IRootLogMessage rootMsg = (IRootLogMessage)message;
        		//Add operation
        		String value = rootMsg.getOperationName();
        		if (value!=null){
        			sb.append(value).append(' ');
        		}

        		//Add class
        		value = rootMsg.getOperationClass();
        		if (value!=null)
        			sb.append(value).append(' ');

        		//Add DN (src/dest)
        		value = rootMsg.getSrcDn();
        		if (value==null)
        			value = rootMsg.getDestDn();
        		if (value != null)
        			sb.append(getCn(value)).append(' ');
        		//Add association
        		value = rootMsg.getAssociation();
        		if (value!=null){
            		sb.append('(');
        			sb.append(value);
        			sb.append(')');
        		}
        		String label = trimSubstring(sb);
        		if (label.length()==0)
        			label="command";
        		sb = new StringBuilder(label);
        		sb.append(": ");
        		sb.append(rootMsg.getOperationStatus());
        		sb.append('-');
        	}
        	if (text.contains("\n")){
        		int index = text.indexOf('\n');
        		sb.append(text.subSequence(0, index));
        		sb.append('\u2026');//elipsis character
        	}else{
        		sb.append(text);
        	}
        	if (element instanceof StatusLogMessage){
        		sb.append(": ").append(((StatusLogMessage)element).getStatus());
        	}
        	if ("Permission Driver".equals(message.getTraceName())){
				System.out.println(">>>Label result: "+sb.toString());
			}
    		return sb.toString();
        }
		return "";
	}

	private static String getCn(String value) {
		//Try SLASHED
		XdsDN aDn = new XdsDN(value,XdsDN.SLASH_DELIMS.toCharArray());
		if (aDn.getParseErr() != 0)
			aDn = new XdsDN(value,XdsDN.LDAP_DELIMS.toCharArray());
		if (aDn.getParseErr() != 0)
			aDn = new XdsDN(value,XdsDN.QSLASH_DELIMS.toCharArray());
		if (aDn.getParseErr() != 0)
			aDn = new XdsDN(value,XdsDN.DOT_DELIMS.toCharArray());
		if (aDn.getParseErr() != 0)
			aDn = new XdsDN(value,XdsDN.QDOT_DELIMS.toCharArray());
		if (aDn.getParseErr() != 0)
			return value;//unable to parse the DN in any known format. Return the full string
		if (aDn.getComponentCount()>0)
			return aDn.getComponent(aDn.getComponentCount()-1).getValue();
		//Unable to parse but no parse error...seems possible. Return the full string
		return value;
	}
	
	public static void main(String[] args){
		System.out.println(getCn("cn=test,ou=anOu,dc=aDc"));
		System.out.println(getCn("\\aDC\\aOU\\acn"));
		System.out.println(getCn("aDC\\aOU\\acn"));
		System.out.println(getCn("par=aDC\\aOU\\acn"));
	}
	
}