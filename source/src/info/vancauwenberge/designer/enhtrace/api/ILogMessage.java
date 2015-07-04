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

import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;

import java.util.List;

import org.eclipse.swt.graphics.Color;

public interface ILogMessage {

	public abstract long getTimeStamp();

	public abstract String getMessage();

	public abstract String getTraceName();

	public abstract String getThread();

	public abstract Color getSWTColor();

	public abstract int getTabIndex();

	public abstract ILogMessage getParent();

	public abstract List<ILogMessage> getChildren();
	
	public abstract boolean hasChildren();
	public abstract boolean hasDetails();
	
	public abstract String getMessageDetail();
	
	public abstract LiveTraceEditorInput getOriginatingServer();

	public abstract IRootLogMessage getRootMessage();

}