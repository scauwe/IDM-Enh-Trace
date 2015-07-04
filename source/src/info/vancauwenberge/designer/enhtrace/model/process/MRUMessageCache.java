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
package info.vancauwenberge.designer.enhtrace.model.process;

import java.util.LinkedList;


public class MRUMessageCache {
	private static final int MAX_QUEUSIZE = 1000;
	private LinkedList<String> items = new LinkedList<String>();
	
	public String getItem(String item){
		//We do not cache strings with newline characters or color codes.
		if (item.contains("\n"))
			return item;
		
		synchronized (items) {
			int index = items.indexOf(item);
			if (index != -1){
				String cached = items.remove(index);
				items.add(cached);
				return cached;
			}else{
				if (items.size()>=MAX_QUEUSIZE)
					items.poll();
				items.add(item);
				return item;			
			}					
		}
	}
	
	public void clear(){
		synchronized (items) {
			items.clear();
		}
		
	}
}
