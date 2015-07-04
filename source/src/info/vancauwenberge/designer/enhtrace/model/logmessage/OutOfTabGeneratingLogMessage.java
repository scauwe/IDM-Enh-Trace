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


import org.eclipse.swt.graphics.Color;

public class OutOfTabGeneratingLogMessage extends LogMessage {

	private boolean messageAdded = false;
	
	public OutOfTabGeneratingLogMessage(){
		super();
	}
	
	@Override
	void addChild(LogMessage logEvent) {
		synchronized (this) {
			super.addChild(logEvent);
			if (logEvent.getTabIndex()< this.getTabIndex()){
				messageAdded=true;
				logEvent.setTabIndex(getTabIndex()+2);
			}			
		}
	}

	public boolean _acceptAsChild(Class<? extends LogMessage> childClass, int childTabIndex, String childMessage, boolean thisIsKnownRoot){
		boolean result = super._acceptAsChild(childClass, childTabIndex, childMessage, thisIsKnownRoot);
		if (result)
			return true;

		//This is a message we normally do not accept, but for once we will accept it.
		if (!messageAdded && (childTabIndex < this.getTabIndex())){
			//If it is not a root message, return true
			//if (EventQueue.isKnownRootMessage(childMessage, childTabIndex))
			//	return false;
			return true;
		}
		return false;
	}

	/*
	@Override
	public boolean acceptAsChild(LogMessage childCandidate) {
		synchronized (this) {
			boolean result = super.acceptAsChild(childCandidate);
			if (result)
				return true;
			if (getTraceName().equals("Permission Driver") && getThread().equals("PT") && (childCandidate instanceof StatusLogMessage)){
				System.out.println("");
			}
			if (!messageAdded && (childCandidate.getTabIndex()< this.getTabIndex())){
				//If it is not a root message, return true
				if (childCandidate instanceof RootLogMessage)
					return false;
				return true;
			}
			return false;			
		}
	}*/
	
	

	
}
