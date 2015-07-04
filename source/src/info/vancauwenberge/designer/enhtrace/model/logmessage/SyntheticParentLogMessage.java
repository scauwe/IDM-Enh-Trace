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

/**
 * Reading relevant attributes from
 * Query back based on the filter settings. If it is a query back, we accept any child until we see the end-of-capture mark
 * @author stefaanv
 *
 */
public class SyntheticParentLogMessage extends LogMessage {
	private static String[] simpleSyntheticParent=new String[]{
		"Initializing driver shim.",
		"Initializing ECMAScript extensions.",
		"Initializing subscriber",
		"Requesting 30 second retry delay.",
		"Processing returned document.",
	};
	
	public static boolean isSimpleSyntheticParentCandidate(String message){
		for (String aMessage : simpleSyntheticParent) {
			if (aMessage.equals(message))
				return true;
		}
		return false;
	}

	public SyntheticParentLogMessage(){
		super();
	}
	
	@Override
	public boolean isSyntheticParent() {
		return true;
	}


	

}
