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
package info.vancauwenberge.designer.enhtrace.util;

public class Util {

	/**
	 * "Truncate" in the middle and add the elipsis character if the string is to long
	 * @param label
	 * @return
	 */
	public static String elipsisLabelMiddle(String label, int maxSize) {
		if (label.length()>maxSize){
			StringBuilder result = new StringBuilder(label.substring(0, maxSize/2));
			result.append('\u2026');
			int charsNeeded = maxSize - result.length();
			result.append(label.substring(label.length()-charsNeeded));
			label = result.toString();
		}
		return label;
	}

	/**
	 * Truncate and add the elipsis character if the string is to long
	 * @param label
	 * @return
	 */
	public static String elipsisLabelEnd(String label, int maxSize) {
		if (label.length()>maxSize){
			label = label.substring(0, maxSize-1) + '\u2026';
		}
		return label;
	}

	/**
	 * Maximum size of a pop-up menu label (in characters).
	 * If longer, the label will be truncated and and elipsis will be added.
	 */
	public static final int MAX_LABEL_SIZE = 35;

}
