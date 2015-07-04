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
package info.vancauwenberge.designer.enhtrace.api;




public interface IPolicySetLogMessage extends ILogMessage{
	public static final String[] processor_operations = new String[]{"add","modify","status","query","request","sync","add-association","delete","instance","move","password","modify-password","trigger","rename","modify-association","remove-association"};
	public enum PolicySet{
		ETP(false, false, new String[]{"Applying event transformation policies.","No event transformation policies."}),
		MP(false, false, new String[]{"Applying object matching policies.","No object matching policies."}),
		CP(false, false, new String[]{"Applying object creation policies.","No object creation policies."}),
		PP(false, false, new String[]{"Applying object placement policies.","No object placement policies."}),
		CTP(false, false, new String[]{"Applying command transformation policies.","No command transformation policies."}),
		OTP(false, false, new String[]{"Applying output transformation policies.","No output transformation policies."}),
		ITP(false, false, new String[]{"Applying input transformation policies.","No input transformation policies."}), 
		//SUB_SCHEMA(new String[]{}),
		//PUB_SCHEMA(new String[]{}),
		
		SCHEMA(false, false, new String[]{"Applying schema mapping policies to output.","Applying schema mapping policies to input.", "No schema mapping policies."}),
		PUB_SYNC_FILTER(true, false, new String[]{"Applying publisher filter.","Skipping publisher filter on operation query."}),
		SUB_SYNC_FILTER(false, false, new String[]{"Applying subscriber filter."}),
		NOTIFY_FILTER(false, false, new String[]{"Filtering out notification-only attributes."}),
		STARTUP(false, false, new String[]{"Applying startup policies.","No startup policies."}),
		SHUTDOWN(false, false, new String[]{"Applying shutdown policies.","No shutdown policies."}),
		SHIM(false, false, new String[]{"Submitting document to subscriber shim:"}),
		PUB_ASSOCIATION_PROCESSOR(false, true, new String[]{"Resolving association references."}),
		SUB_ASSOCIATION_PROCESSOR(false, true, new String[]{"Fixing up association references."}),
		PUB_RESET_INJECTION(true, false, new String[]{"Resetting eDirectory with application values."}),
		ADD_PROCESSOR(true, false, processor_operations){
		/*new String[]{
				"Subscriber processing modify for ","Subscriber processing add for ", "Subscriber processing status for ","Subscriber processing query for ","Subscriber processing request for ", "Subscriber processing sync for ",
				"Publisher processing modify for ","Publisher processing add for ","Publisher processing status for ","Publisher processing query for ",}){
			
		}*/
			/**
			 * We test if the losMessage starts with: "<channel> processing <operation> for ". If found, return true. False otherwise.
			 */
			protected boolean isPolicysetTrigger(String logMessage){
				for (String operation : processor_operations) {
					StringBuilder sb = new StringBuilder("Subscriber processing ");
					sb.append(operation);
					sb.append(" for ");
					if (logMessage.startsWith(sb.toString()))
						return true;
					sb = new StringBuilder("Publisher processing ");
					sb.append(operation);
					sb.append(" for ");
					if (logMessage.startsWith(sb.toString()))
						return true;					
				}
				return false;
			}
		},
		;
		
		private String[] equalsStrings;
		private boolean isSubflowRoot;
		private boolean statusChildOnly;

		private PolicySet(boolean isSubflowRoot, boolean statusChildOnly, String[] equalsString){
			this.equalsStrings = equalsString;
			this.isSubflowRoot = isSubflowRoot;
			this.statusChildOnly = statusChildOnly;
		}
		
		protected boolean isPolicysetTrigger(String logMessage){
			for (String aLogMessage : equalsStrings) {
				if (aLogMessage.equals(logMessage)){
					return true;
				}
			}
			return false;
		}
		
		public static PolicySet getPoliciSetFor( String logMessage){
			for (PolicySet policySets : values()) {
				if (policySets.isPolicysetTrigger(logMessage))
					return policySets;
			}
			return null;
		}
		
		public boolean isSubflowRoot(){
			return isSubflowRoot;
		}
		
		public boolean isStatusChildOnly(){
			return statusChildOnly;
		}
	}

	public PolicySet getPolicySet();

}
