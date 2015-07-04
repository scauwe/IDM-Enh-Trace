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
package info.vancauwenberge.designer.enhtrace.editor.input;

import java.util.List;

import info.vancauwenberge.designer.enhtrace.api.IListListener;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageList;
import info.vancauwenberge.designer.enhtrace.model.process.EDirEventProcessor;
import info.vancauwenberge.designer.enhtrace.model.process.EDirEventSource;
import info.vancauwenberge.designer.enhtrace.model.process.PublishingList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class LiveTraceEditorInput implements IEditorInput, ILogMessageList {
	
	private PublishingList logMessages = new PublishingList();
	private EDirEventSource eDirEventSourceThread;	
	private EDirEventProcessor eDirEventProcessor;	
	private String server;
	private String userName;
	private String password;
	private boolean useSSL;

	
	public LiveTraceEditorInput(String server, String userName, String password, boolean useSSL) throws IllegalArgumentException{
		if (server==null || "".equals(server.trim()) || userName==null || "".equals(userName.trim()) || password==null || "".equals(password.trim())){
			throw new IllegalArgumentException("Missing host name on Server or credentails on Identity Vault object.");
		}
		
		this.server = server;
		this.userName = userName;
		this.password = password;
		this.useSSL = useSSL;
		restart();
		
	}
	
	public boolean equals(Object otherObj){
		if (otherObj != null && otherObj instanceof LiveTraceEditorInput){
			LiveTraceEditorInput other = (LiveTraceEditorInput)otherObj;
			return server.equals(other.server);
		}
		return false;
	}
	
	public ILogMessage[] getMessages(){
		return logMessages.getPublishedMessages();
	}
	
	/**
	 * Stop the source and processing threads
	 */
	public void stop(){
		if (eDirEventSourceThread != null)
			eDirEventSourceThread.stopGenerating();
		eDirEventSourceThread = null;
		
		if (eDirEventProcessor != null)
			eDirEventProcessor.stopThread();
		eDirEventProcessor=null;
	}
	
	/**
	 * Restart from scratch, clearing all current events and starting a new source and processing thread
	 */
	public void restart(){
		//Make sure that the previous threads were stopped
		stop();
		
		//Clear current events
		logMessages.clear();
		
		//Create a new thread to poll for edir events
		eDirEventSourceThread  = new EDirEventSource(server,userName,password,useSSL);
		eDirEventSourceThread.start();
		
		//Create a new thread to process the edir events
		eDirEventProcessor = new EDirEventProcessor(this, eDirEventSourceThread.getEventQueue(), logMessages);
		eDirEventProcessor.start();
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return "Trace";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Live trace";
	}

	public void clearAll(){
		logMessages.clear();
	}
	
	public boolean isDisposed(){
		return (logMessages==null);
	}
	
	public void dispose() {
		System.out.println(this.getClass().getName()+" - dispose() start");
		//Stop all processing
		stop();
		
		//Clear memory
		logMessages.clear();
		logMessages = null;
		
		System.out.println(this.getClass().getName()+" - dispose() end");
	}

	public String getServer() {
		return server;
	}

	public String getUserName() {
		return userName;
	}
	
	@Override
	public void addListListener(IListListener<ILogMessage> listener) {
		logMessages.addListListener(listener);
	}


	@Override
	public void removeListListener(IListListener<ILogMessage> listener) {
		logMessages.removeListListener(listener);
	}

}