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

import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage;

public class PolicyRootLogMessage extends SyntheticParentLogMessage implements IPolicySetLogMessage {

	public PolicyRootLogMessage(){
		super();
	}

	protected void setMessage(String message) {
		super.setMessage(message);
		policySet = PolicySet.getPoliciSetFor(message);
	}

	
	private PolicySet policySet = null;
	private boolean endAccept=false;
	
	@Override
	public PolicySet getPolicySet() {
		return policySet;
	}

	
	public boolean _acceptAsChild(Class<? extends LogMessage> childClass, int childTabIndex, String childMessage, boolean thisIsKnownRoot){

		if (policySet.isStatusChildOnly()){
			//We only accept statusses
			if (childClass!=StatusLogMessage.class )
				endAccept = true;
			return !endAccept;
		}else{
			//We do not accept statusses but only non-statusses
			if (childClass==StatusLogMessage.class){
				endAccept = true;
			}

			if (endAccept)
				return false;
		
			/*if (childMessage.equals("Resolving association references.") && getTraceName().equals("TraceTest") && getThread().equals("ST")&&
				getMessage().equals("Subscriber processing modify for \\UTOPIA-TREE\\data\\users\\aValue2 Value1."))
				System.out.println("Breakpont");
			 */
		
			if (getPolicySet().isSubflowRoot()){
				//Do not accept ourselves (or a clone of it) as a child.
				if (childTabIndex == this.getTabIndex()
						&& childClass.equals(this.getClass())
						&& getPolicySet().equals(PolicySet.getPoliciSetFor(childMessage)))
					return false;

				//Otherwise: flow is the root of other policySets. 
				//Special handling: add all what the parent would otherwise accept, unless it is another subflow candidate
				if (getParent()._acceptAsChild(childClass, childTabIndex, childMessage, thisIsKnownRoot)){ 
					
					/*
					PolicySet childPolicySet = PolicySet.getPoliciSetFor(childMessage);
					if (childPolicySet==null || (childPolicySet != null && !childPolicySet.isSubflowRoot()))*/
						return true;
				}
			

			}
		//If we have a
		//ADD_PROCESSOR flow is the root of other policySets. Special handling: add all what the parent would otherwise accept, unless it is another ADD_PROCESSOR candidate
		/*		if (policySet==PolicySet.ADD_PROCESSOR){
				if (getParent()._acceptAsChild(childTabIndex, childMessage, thisIsKnownRoot) && !(PolicySet.ADD_PROCESSOR==PolicySet.getPoliciSetFor(childMessage)))
					return true;
			}*/
			//Otherwise: follow normal rules
			return super._acceptAsChild(childClass, childTabIndex, childMessage, thisIsKnownRoot);
		}
	}
	
	
	
}
