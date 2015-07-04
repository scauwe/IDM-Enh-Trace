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

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;
import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage.PolicySet;
import info.vancauwenberge.designer.enhtrace.api.IRootLogMessage;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.model.process.EDirEventProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Color;

public class LogMessage implements ILogMessage, ILogMessageProvider{
	private static final String statusStartsWith = "DirXML Log Event -------------------\n";
	//private static final LogMessage[] emptyArray = new LogMessage[0];
	public LogMessage(){
		
	}
	
	private LogMessage parent = null;
	private List<ILogMessage> children = null;
	private String rawData = null;
	private String message;
	private int tabIndex;
	
	//private int idmColor;
	
	private long eventDateMilis;
	//private boolean isMessageDetail=false;
	private StringBuilder details = null;
	//private boolean shouldContainDetail=false;

	//private boolean issyntheticParent=false;
	private Color swtColor;
	
	public int getLevel(){
		if (parent!= null){
			return parent.getLevel()+1;
		}else{
			return 0;
		}
	}
	

	/**
	 * Append a second part of a message to the current message. If the message has a message detail, 
	 * it is assumed that it must be added to the message detail. If not, the messagePart is added to
	 * the main message.
	 * @param formatedString
	 */
	public void appendMessagePart(String formatedString) {
		if (details != null){
			details.append('\n').append(formatedString);
		}else{
			StringBuilder sb = new StringBuilder(message).append('\n').append(formatedString);
			message = sb.toString();
		}
	}

	/**
	 * Add a message detail to the current message.
	 * @param evt
	 */
	void addMessageDetail(String detailsPart) {
		if (details == null){
			details = new StringBuilder(detailsPart);// ArrayList<>();
		}else{
			details.append(detailsPart);
		}
	}

	

	/**
	 * Add a child to this event. This is not done synchronised since only one thread will add children.
	 * TODO:
	 * Note: Another thread might request the children...maybe it should be synchronised with that?
	 * @param logEvent
	 */
	void addChild(LogMessage logEvent) {
		if (children==null)
			children=new ArrayList<ILogMessage>();
		synchronized (children) {
			children.add(logEvent);			
		}
	}


	public String getMessage() {
		return message;
	}

	public String getThread() {
		if (parent != null)
			return parent.getThread();
		return "??";
	}
	
	public LiveTraceEditorInput getOriginatingServer(){
		if (parent != null)
			return parent.getOriginatingServer();
		return null;
	}

	public int getTabIndex() {
		return tabIndex;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getName() + ": ");
		sb.append(rawData);
		sb.append(", tabIndex:");
		sb.append(tabIndex);
		sb.append(", eventDateMilis:");
		sb.append(eventDateMilis);
		if (details != null){
			sb.append(",details:");
			sb.append(details);
		}
		return sb.toString();
		/*
		sb.append(thread).append(' ');
		sb.append(traceName).append(' ');
		//Append so you will get 25 characters
		while (sb.length()<25)
			sb.append(' ');
		sb.append(" - ");
		int localTabIndex = getLevel();
		for (int i = 0; i < localTabIndex; i++) {
			sb.append(' ');
		}
		sb.append(getLevel()).append('-');
		sb.append(tabIndex).append('-');
		sb.append(issyntheticParent).append('-');
		sb.append(message);
		if (details != null)
			sb.append('\n').append(details);
		
		return sb.toString();
		
/*		if (shouldContainDetail)
			return "LogParser [dsTime="+dsTime+", eventDateMilis=" + eventDateMilis +", tabIndex=" + tabIndex + ", thread=" + thread + ", driver=" + traceName
				+ ", colour="+colour+", message=" + message + ", details="+details+"]";
		return "LogParser [dsTime="+dsTime+", eventDateMilis=" + eventDateMilis +", tabIndex=" + tabIndex + ", thread=" + thread + ", driver=" + traceName
				+ ", colour="+colour+", message=" + message + "]";*/
	}
/*
	public int getColour() {
		return idmColor;
	}
*/
	public String getTraceName() {
		if (parent != null)
			return parent.getTraceName();
		return null;
	}

	/*
	public void setSchouldContainDetails(boolean b) {
		shouldContainDetail=b;		
	}
	*/

	@Override
	public boolean equals(Object obj) {
		return this==obj;
	}

	public LogMessage getParent() {
		return parent;
	}
	
	public boolean hasChildren(){
		return (children != null && children.size()>0);
	}

	public final List<ILogMessage> getChildren() {
		if (children==null)
			return null;
		
		List<ILogMessage> newList = new ArrayList<ILogMessage>();
		synchronized (children) {
			newList.addAll(children);			
		}
		return newList;
	}


	@Override
	public long getTimeStamp() {
		return eventDateMilis;
	}


	@Override
	public Color getSWTColor() {
		return swtColor;
	}
	
	public IRootLogMessage getRootMessage(){
		if (parent != null)
			return parent.getRootMessage();
		return null;
	}

/*
	public boolean isMessageDetail() {
		return isMessageDetail;
	}
*/
	public boolean isSyntheticParent() {
		return false;
	}
/*
	protected void setSyntheticParent(boolean issyntheticParent) {
		this.issyntheticParent = issyntheticParent;
	}*/
/*
	protected boolean shouldContainDetail() {
		return shouldContainDetail;
	}
	*/
	protected void handleSetStatus(StatusLogMessage statusMessage) {
		if (parent!=null)
			parent.handleSetStatus(statusMessage);
	}


	public boolean hasDetails(){
		return (details != null && details.length()>0);
	}
	
	@Override
	public String getMessageDetail() {
		if (details==null)
			return null;
		return details.toString();
	}
	
	/*
	public boolean acceptAsChild(LogMessage childCandidate){
		IRootLogMessage root = getRootMessage();
		if (root != null && root.isTransactionClosed())
			return false;
		return _acceptAsChild(childCandidate, false);
	}
	*/
	
	private boolean isRootTransactionClosed(){
		IRootLogMessage root = getRootMessage();
		if (root != null)
			return root.isTransactionClosed();
		//We do not have a root => not closed...
		return false;		
	}
	
	/**
	 * Note: it is assumed that the thread and trace name are equal. These are not tested in this method!!!
	 * @param childClass TODO
	 * @param thisIsKnownRoot
	 * @param childCandidate
	 * @return
	 */
	public boolean _acceptAsChild(Class<? extends LogMessage> childClass, int childTabIndex, String childMessage, boolean thisIsKnownRoot){
		
		/*if ((this.thread!=childCandidate.thread) ||
				(this.traceName!= childCandidate.traceName))
			return false;*/
		
		/*
		if (( (childCandidate.tabIndex < this.tabIndex)||
				((childCandidate.tabIndex == this.tabIndex) 
						&& (childCandidate instanceof RootLogMessage)
						&& (!thisIsKnownRoot))||
				((childCandidate.getTabIndex() == this.tabIndex)
						&& (childCandidate.issyntheticParent==true)
						&& (this.issyntheticParent==false)))){
			return true;
		}*/
		/*
		if (rawData.equals("%3CPermission Driver ST:No object placement policies.") ||
				childCandidate.rawData.equals("%3CPermission Driver ST:No object creation policies.")
				||childCandidate.rawData.equals("%3CPermission Driver ST:No object placement policies.")
				||rawData.equals("%3CPermission Driver ST:No object creation policies.")){
			System.out.println("why???");
		}*/
		
		//This is either a status are a message continuation. Always accept them by default.
		if (childMessage.length()>0 && (childMessage.charAt(0)=='\n'))
			return true;
		
		if (childTabIndex > this.tabIndex)
			return true;
		
		if ((childTabIndex == this.tabIndex) 
				&& (!(RootLogMessage.isKnownRootCandidate(childMessage, childTabIndex)))
				&& (thisIsKnownRoot))
			return true;
		
		if ((childTabIndex  == this.tabIndex)
				&& (!SyntheticParentLogMessage.class.isAssignableFrom(childClass))//!isPolicySetRoot(childMessage, childTabIndex))
				&& (this.isSyntheticParent()))
			return true;
		
		return false;		
	}


	private static boolean isPolicySetRoot(String message, int tabIndex){
		return (tabIndex==0 && PolicySet.getPoliciSetFor(message)!=null);
		//return (tabIndex==0 && EventQueue.isSyntheticMessage(message));
	}
	
	/**
	 * Note: it is assumed that the thread and trace name are equal. These are not tested in this method!!!
	 * @param childCandidate
	 * @param thisIsKnownRoot
	 * @return
	 */
	public boolean _acceptAsChild(LogMessage childCandidate, boolean thisIsKnownRoot){
		/*if ((this.thread!=childCandidate.thread) ||
				(this.traceName!= childCandidate.traceName))
			return false;*/
		
		/*
		if (( (childCandidate.tabIndex < this.tabIndex)||
				((childCandidate.tabIndex == this.tabIndex) 
						&& (childCandidate instanceof RootLogMessage)
						&& (!thisIsKnownRoot))||
				((childCandidate.getTabIndex() == this.tabIndex)
						&& (childCandidate.issyntheticParent==true)
						&& (this.issyntheticParent==false)))){
			return true;
		}*/
		/*
		if (rawData.equals("%3CPermission Driver ST:No object placement policies.") ||
				childCandidate.rawData.equals("%3CPermission Driver ST:No object creation policies.")
				||childCandidate.rawData.equals("%3CPermission Driver ST:No object placement policies.")
				||rawData.equals("%3CPermission Driver ST:No object creation policies.")){
			System.out.println("why???");
		}*/
		if (childCandidate.tabIndex > this.tabIndex)
			return true;
		
		if ((childCandidate.tabIndex == this.tabIndex) 
				&& (!(childCandidate instanceof RootLogMessage))
				&& (thisIsKnownRoot))
			return true;
		
		if ((childCandidate.tabIndex  == this.tabIndex)
				&& (!childCandidate.isSyntheticParent())
				&& (this.isSyntheticParent()))
			return true;
		
		return false;		
	}
	/*
	protected boolean _profileEquels(LogMessage siblingCandidate, boolean thisIsKnownRoot){
		if ((this.thread!=siblingCandidate.thread) ||
				(this.traceName!= siblingCandidate.traceName))
			return false;
		
		if ((siblingCandidate.tabIndex==this.tabIndex) 
				&& (siblingCandidate.issyntheticParent==this.issyntheticParent)
				&&(thisIsKnownRoot?(siblingCandidate instanceof RootLogMessage):!(siblingCandidate instanceof RootLogMessage)))
			return true;
		return false;
		
	}
	
	public boolean profileEquals(LogMessage siblingCandidate){
		return _profileEquels(siblingCandidate, false);
	}*/


	protected void setTabIndex(int i) {
		tabIndex = i;
	}

	protected void setParent(LogMessage parent) {
		this.parent = parent;
	}

	@Override
	public ILogMessage getLogMessage() {
		return this;
	}

	private static Class<? extends LogMessage> getCandidateClass(int childTabIndex, String childMessage){
		//Test if the message is a message details or status (starts with CR/NL)
		if (childMessage.length()>0 && (childMessage.charAt(0)=='\n')){
			//Strip the newline character
			childMessage = childMessage.substring(1);

			if (childMessage.startsWith(statusStartsWith)){
				return StatusLogMessage.class;
			}
			else {
				return null;//We did not create anew message, only updated an old one.
			}
		}else{
			if (RootLogMessage.isKnownRootCandidate(childMessage, childTabIndex))
				return RootLogMessage.class;
			else if (EDirEventProcessor.isOutOfTabGeneratingMessage(childMessage)){
				return OutOfTabGeneratingLogMessage.class;
			}else if (SyntheticParentLogMessage.isSimpleSyntheticParentCandidate(childMessage)){
				return SyntheticParentLogMessage.class;
			}else if (EDirEventProcessor.isPolicySetMessage(childMessage)){
				return PolicyRootLogMessage.class;
			}else if (MergerLogMessage.isMergerCandidate(childMessage)){
				return MergerLogMessage.class;
			}else{
				return LogMessage.class;
			}
		}

	}

	public final LogMessage[] createLogMessage(String childMessage, String childThread, int childTabIndex, String childTraceName, int childCdmColor,
			long childEventDateMilis, Color childSwtColor, String childFormatedString) {
		
		Class<? extends LogMessage> childClass = getCandidateClass(childTabIndex, childMessage);

		if (isRootTransactionClosed()){
			//We are forced to create a new root. No reason to travel the chain...
			//Create a dummy root, needed to append the children to it
			//Only scenario until know: reset of application attributes...=> create a dummy root with this information.
			LogMessage[] result = new LogMessage[2];
			result[0] = new RootLogMessage("Reset of application with eDirectory attributes", childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, childSwtColor, getOriginatingServer(), childFormatedString);
			LogMessage[] children = result[0].createLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, childSwtColor, childFormatedString); 
			result[1] = children[0];
			return result;
		}

		
		if (childTraceName.equals("TraceTest") && childThread.equals("ST")){
			System.out.println("childClass:"+childFormatedString);
			System.out.println("childMessage:"+childClass);
		}
		
		if (childMessage.trim().startsWith("End transaction")){
			System.out.println("break");
		}

		//returnClass.newInstance();
		
		//If we accept this event as a child, create it
		if (_acceptAsChild(childClass, childTabIndex, childMessage, false)){
			LogMessage newChild = createChild(childClass, childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, childSwtColor,childFormatedString);
			if (newChild == null)
				return null;
			return new LogMessage[]{newChild};
		}
		//If we have a parent, delegate to the parent.
		if (parent != null)
			return parent.createLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, childSwtColor, childFormatedString);
		
		//We need to create a rootLogMessage out of it...no other choice.
		return new LogMessage[]{new RootLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, childSwtColor, getOriginatingServer(), childFormatedString)};
	}


	private LogMessage createChild(Class<? extends LogMessage> childClass, String childMessage, String childThread,
			int childTabIndex, String childTraceName, int childCdmColor,
			long childEventDateMilis, Color childSwtColor,
			String childFormatedString) {

		if (childClass==null){
			childMessage = childMessage.substring(1);
			addMessageDetail(childMessage);
			return null;
		}
		
		LogMessage result;
		try {
			result = childClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		if (RootLogMessage.class.equals(childClass)){
			((RootLogMessage)result).setThread(childThread);
			((RootLogMessage)result).setTraceName(childTraceName);

		}
		result.setMessage (childMessage);
		result.setTabIndex(childTabIndex);
		result.setEventDateMilis(childEventDateMilis);
		result.setSwtColor(childSwtColor);
		result.setRawData(childFormatedString);
		
		
		/*
		
		//Test if the message is a message details or status (starts with CR/NL)
		if (childMessage.length()>0 && (childMessage.charAt(0)=='\n')){
			//Strip the newline character
			childMessage = childMessage.substring(1);

			if (childMessage.startsWith(statusStartsWith)){
				result = new StatusLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, false, childSwtColor, childFormatedString);
			}
			else {
				addMessageDetail(childMessage);
				return null;//We did not create anew message, only updated an old one.
			}
		}else{
			if (RootLogMessage.isKnownRootCandidate(childMessage, childTabIndex))
				result = new RootLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, false, childSwtColor, childFormatedString);
			else if (EventQueue.isOutOfTabGeneratingMessage(childMessage)){
				result = new OutOfTabGeneratingLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, false, childSwtColor, childFormatedString);
			}else if (SyntheticParentLogMessage.isSimpleSyntheticParentCandidate(childMessage)){
				result = new SyntheticParentLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, false, childSwtColor, childFormatedString);
			}else if (EventQueue.isPolicySetMessage(childMessage)){
				result = new PolicyRootLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, false, childSwtColor, childFormatedString);
			}else if (MergerLogMessage.isMergerCandidate(childMessage))
				result = new MergerLogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, false, childSwtColor, childFormatedString);
			else
				result = new LogMessage(childMessage, childThread, childTabIndex, childTraceName, childCdmColor, childEventDateMilis, false, childSwtColor, childFormatedString);
		}*/
		
		result.setParent(this);
		addChild(result);
		
		//If this signals a closure, close the root
		//TODO: better method of doing this???
		if ("End transaction.".equals(childMessage)){
			IRootLogMessage root = getRootMessage();
			if (root != null)
				root.setTransactionClosed(true);
		}
		
		/*
		if (!result.isMessageDetail() && (tabIndex == 0) && message!= null && !message.trim().equals("")){
			if (EventQueue.isSyntheticMessage(message))
				result.setSyntheticParent(true);
		}*/
		
		return result;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

/*
	protected void setIdmColor(int idmColor) {
		this.idmColor = idmColor;
	}
*/

	protected void setSwtColor(Color swtColor) {
		this.swtColor = swtColor;
	}


	protected void setRawData(String rawData) {
		this.rawData = rawData;
	}


	protected void setEventDateMilis(long eventDateMilis) {
		this.eventDateMilis = eventDateMilis;
	}
}