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



public class MergerLogMessage extends LogMessage {
	private static final String[] mergerTrigger = new String[]{
		"Reading relevant attributes from",
		/*
		"Subscriber processing modify for ",
		"Subscriber processing add for ", 
		"Subscriber processing status for ",
		"Publisher processing modify for ",
		"Publisher processing add for ",
		"Publisher processing status for ",*/
		};
	
	//We keep on accepting all messages until a status is added or until one of the closure messages is addded as a child
	private static final String[] mergerClosure = new String[]{
		"Read result:",
		//"Processing returned document.",
		};
	
	public static boolean isMergerCandidate(String message){
		if (message==null)
			return false;
		for (String aMessage : mergerTrigger) {
			if (message.startsWith(aMessage))
				return true;
		}
		
		return false;
	}

	private boolean endAccept=false;
	
	public MergerLogMessage(){
		super();
	}
	
	public boolean _acceptAsChild(Class<? extends LogMessage> childClass, int childTabIndex, String childMessage, boolean thisIsKnownRoot){
		if (childClass==RootLogMessage.class)
			return false;
		if (!endAccept)
			return true;
		return super._acceptAsChild(childClass, childTabIndex, childMessage, thisIsKnownRoot);
	}

	private boolean isMergerClosure(String message){
		for (String aClosureMEssage : mergerClosure) {
			if (aClosureMEssage.equals(message))
				return true;
		}
		return false;
	}
	
	@Override
	void addChild(LogMessage logEvent) {
		super.addChild(logEvent);
		//We accept as long as:
		// - we did not encounter a status as direct child (eg: reading failed)
		// - we did not encounter a policy result as a direct child
		if (!endAccept){
			if (logEvent instanceof StatusLogMessage)
				endAccept = true;
			else if (isMergerClosure(logEvent.getMessage()))
				endAccept = true;
		}
	}
	
	


}
