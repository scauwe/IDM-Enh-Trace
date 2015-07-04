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


public interface ISubflowLogMessage {
	public enum SubFlow{
		//The publisher channel contains multiple XDS events/commands without additional indentation.
		CHANNEL_FLOW_PUBLISHER(
				new String[]{"Applying publisher filter."}, 
				null, 
				new String[]{"Applying publisher filter.","Fixing up association references."}),
		//Merging on the subscriber or publisher channel results in a potential query-back without indentation		
		MERGE_FLOW(
				new String[]{"Reading relevant attributes from "}, 
				new String[]{"Resolving association references.","Pumping XDS to eDirectory."},
				null);
		
		private String[] closureAfterStartsWith;
		private String[] triggersStartsWith;
		private String[] closureBeforeStartsWith;

		private SubFlow(String[] triggersStartsWith, String[] closureAfterStartsWith, String[] closureBeforeStartsWith){
			this.triggersStartsWith = triggersStartsWith;
			this.closureAfterStartsWith = closureAfterStartsWith;
			this.closureBeforeStartsWith = closureBeforeStartsWith;
		}

		public static SubFlow getSubFlowFor(String message) {
			for (SubFlow aSubFlow : values()) {
				for (String aLogMessage : aSubFlow.triggersStartsWith) {
					if (message.startsWith(aLogMessage)){
						return aSubFlow;
					}
				}
			}
			return null;
		}

		private boolean isInArray(String[] array, String value){
			for (String string : array) {
				if (string.equals(value))
					return true;
			}
			return false;
		}
		
		public boolean isBeforeMark(String message) {
			return isInArray(closureBeforeStartsWith, message);
		}

		public boolean isAfterMark(String message) {
			return isInArray(closureAfterStartsWith, message);
		}
	}
	
	public SubFlow getSubFlow();

}
