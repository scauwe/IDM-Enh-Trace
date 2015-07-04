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
package info.vancauwenberge.designer.enhtrace.viewer.table;

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;

public class LogMessageWrapper implements ILogMessageProvider{

	private boolean isMainMessagePart;

	public LogMessageWrapper(String message, ILogMessage logMessage, boolean isMainMessagePart) {
		this.message = message;
		this.logMessage = logMessage;
		this.isMainMessagePart = isMainMessagePart;
	}

	private String message;
	private ILogMessage logMessage;

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public ILogMessage getLogMessage() {
		return logMessage;
	}

	
	public boolean isMainMessagePart(){
		return isMainMessagePart;
	}
}
