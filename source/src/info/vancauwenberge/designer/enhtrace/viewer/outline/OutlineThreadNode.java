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
package info.vancauwenberge.designer.enhtrace.viewer.outline;

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;

import java.util.ArrayList;
import java.util.List;

class OutlineThreadNode implements IOutlineParentNode {
	private String label = null;
	List<ILogMessage> children= new ArrayList<ILogMessage>();
	private IOutlineParentNode parent;
	
	public OutlineThreadNode(String label, IOutlineParentNode parent) {
		this.label = label;
		this.parent = parent;
	}

	public IOutlineParentNode getParent() {
		// TODO Auto-generated method stub
		return parent;
	}

	public void addChild(ILogMessage message){
		children.add(message);
	}
	
	/* (non-Javadoc)
	 * @see info.vancauwenberge.designer.enhtrace.viewer.outline.IOutlineParentNode#getChildren()
	 */
	@Override
	public List<?> getChildren() {
		return children;
	}
	/* (non-Javadoc)
	 * @see info.vancauwenberge.designer.enhtrace.viewer.outline.IOutlineParentNode#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	public boolean hasChild(ILogMessage message) {
		return children.contains(message);
	}
	
}