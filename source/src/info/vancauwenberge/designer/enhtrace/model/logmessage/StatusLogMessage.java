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
package info.vancauwenberge.designer.enhtrace.model.logmessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.vancauwenberge.designer.enhtrace.api.IRootLogMessage.Status;

import org.eclipse.swt.graphics.Color;

public class StatusLogMessage extends SyntheticParentLogMessage {
	
	public StatusLogMessage(){
		super();
	}
	
	public Status getStatus(){
		String status = extractStatus(getMessage());
		if (status != null && !"".equals(status))
			try{
				return Status.valueOf(status.toUpperCase());
			}catch (Exception e) {
				e.printStackTrace();
				return Status.UNKNOWN;
			}
		return null;
	}


	public String getStatusMessage(){
		String message = getMessage();
		return extractStatusMessage(message);
	}

	/**
	 * 
	 * @param message
	 * @return
	 * 
	 DirXML Log Event -------------------
     Driver:   \UTOPIA-TREE\system\driverset1\Permission Driver
     Channel:  Subscriber
     Object:   \UTOPIA-TREE\data\\users\\ablake
     Status:   Success

	 */
	private static String extractStatusMessage(String message) {
		String regExp = "((.*\\n)*(^     Message:  ))((.|\\n)*)";
		Pattern pattern = Pattern.compile(regExp,Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(message);
		if (matcher.matches()){
			return matcher.replaceAll("$4").trim();
		}
		return null;
	}

	@Override
	protected void setParent(LogMessage parent) {
		super.setParent(parent);
		if (getTraceName().equals("Permission Driver") && getThread().equals("ST"))
			System.out.println("break");
		handleSetStatus(this);
	}

	/**
	 * 
	 * @param message
	 * @return
	 * 
	 DirXML Log Event -------------------
     Driver:   \UTOPIA-TREE\system\driverset1\Permission Driver
     Channel:  Subscriber
     Object:   \UTOPIA-TREE\data\\users\\ablake
     Status:   Success

	 */
	private static String extractStatus(String message){
		String regExp = "(.*\\n)*(^     Status:   )(.*)(\\n.*)*";
		Pattern pattern = Pattern.compile(regExp,Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(message);
		return matcher.replaceAll("$3").trim();
	}
	
	
	public static void main(String[] args){
		String s1 = "DirXML Log Event -------------------\n"+
				"     Driver:   \\UTOPIA-TREE\\system\\driverset1\\Permission Driver\n"+
				"     Channel:  Subscriber\n"+
				"     Object:   \\UTOPIA-TREE\\data\\users\\ablake\n"+
				"     Status:   Success";
		String s2 = "DirXML Log Event -------------------\n"+
			     "     Driver:   \\UTOPIA-TREE\\system\\driverset1\\Permission Driver\n"+
			     "     Channel:  Subscriber\n"+
			     "     Object:   \\UTOPIA-TREE\\data\\users\\ablake\n"+
			     "     Status:   Success\n"+
			     "     Message:  AMessage";
		String s3 = "DirXML Log Event -------------------\n"+
			     "     Driver:   \\UTOPIA-TREE\\system\\driverset1\\Permission Driver\n"+
			     "     Channel:  Subscriber\n"+
			     "     Object:   \\UTOPIA-TREE\\data\\users\\ablake\n"+
			     "     Status:   Success\n"+
			     "     Message:  AMessage\n";
		String s4 = "DirXML Log Event -------------------\n"+
			     "     Driver:   \\UTOPIA-TREE\\system\\driverset1\\Permission Driver\n"+
			     "     Channel:  Subscriber\n"+
			     "     Object:   \\UTOPIA-TREE\\data\\users\\ablake\n"+
			     "     Status:   Success\n"+
			     "     Message:  AMessage\nPart2";
		System.out.println(extractStatus(s1));
		System.out.println(extractStatus(s2));
		System.out.println(extractStatus(s3));
		System.out.println(extractStatus(s4));
		System.out.println(extractStatusMessage(s1));
		System.out.println(extractStatusMessage(s2));
		System.out.println(extractStatusMessage(s3));
		System.out.println(extractStatusMessage(s4));
	}

	public boolean _acceptAsChild(Class<? extends LogMessage> childClass, int childTabIndex, String childMessage, boolean thisIsKnownRoot){
		return false;
	}
	/*

	@Override
	public boolean acceptAsChild(LogMessage childCandidate) {
		return false;
	}*/
	
	protected void setMessage(String message) {
		super.setMessage(message.substring(1));
	}

	
}
