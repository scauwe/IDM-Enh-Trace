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



public interface IRootLogMessage extends ILogMessage {

	public enum Status{
		UNKNOWN(0,"Unknown"), 
		SUCCESS(1,"Success"),
		RETRY(2,"Retry"),
		WARNING(3,"Warning"),
		ERROR(4,"Error"),
		FATAL(5,"Fatal");

		private int level;
		private String label;

		private Status(int level, String label){
			this.level = level;
			this.label = label;
		}
		
		/**
		 * Get the severity level.
		 * @return
		 */
		public int getLevel(){
			return level;
		}
		public String geLabel(){
			return label;
		}
	}
	
	public String getAssociation();

	/**
	 * On a publisher channel, return the DN of the application
	 * On the subscriber channel, return the DN of IDM
	 * @return
	 */
	public String getSrcDn();


	/**
	 * Get the classname of the operation
	 * @return
	 */
	public abstract String getOperationClass();

	/**
	 * Get the name (add, modify,..) of the operation
	 * @return
	 */
	public abstract String getOperationName();

	public abstract String getDestDn();

	Status getOperationStatus();
	
	public abstract boolean isTransactionClosed();

	public abstract void setTransactionClosed(boolean closed);

}

