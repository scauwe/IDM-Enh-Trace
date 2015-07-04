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

import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.model.logmessage.LogMessage;
import info.vancauwenberge.designer.enhtrace.model.logmessage.RootLogMessage;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import com.novell.ldap.events.edir.EdirEventIntermediateResponse;
import com.novell.ldap.events.edir.EventResponseData;
import com.novell.ldap.events.edir.eventdata.DebugEventData;
import com.novell.ldap.events.edir.eventdata.DebugParameter;


public class EDirEventProcessor extends Thread {


	private boolean isStoped = false;
	private PublishingList events = null;
	private ColorManager colors = new ColorManager();
	private MRUMessageCache messageCache = new MRUMessageCache();
	private static final String[] idmThreads = {"PT","ST","EV","??"};//?? must be the last one!!!
	private LiveTraceEditorInput liveTraceEditorInput;
	private LinkedBlockingQueue<EdirEventIntermediateResponse> eDirEventQueue;


	public EDirEventProcessor(LiveTraceEditorInput liveTraceEditorInput, LinkedBlockingQueue<EdirEventIntermediateResponse> eDirEventQueue, PublishingList events) {
		super("EdirEventProcessor");
		setDaemon(true);

		this.eDirEventQueue = eDirEventQueue;
		this.events = events;
		this.liveTraceEditorInput = liveTraceEditorInput;
	}


	
	public static boolean isOutOfTabGeneratingMessage(String message) {
		return  message.startsWith("Action: do-trace-message") || message.startsWith("Processing operation <status> for ");
	}

	public static boolean isPolicySetMessage(String message) {
		boolean result = IPolicySetLogMessage.PolicySet.getPoliciSetFor(message) != null;
		return result;
	}

	
	private static Object[] getParameters(DebugEventData eventData) {
		@SuppressWarnings("unchecked")
		List<DebugParameter> parameters = eventData.getParameters();
		Object[] paramArray = new Object[parameters.size()];
		
		for (int i = 0; i < parameters.size(); i++)
			paramArray[i] = parameters.get(i).getData();
		return paramArray;
	}

	
	
		
	public void stopThread(){
		System.out.println(this.getClass().getName()+" stopThread() start");
		this.isStoped = true;
		System.out.println(this.getClass().getName()+" stopThrezad() end");
		this.interrupt();
	}
	
	
	public void run() {
		while(!isStoped){
			try {
				EdirEventIntermediateResponse next = eDirEventQueue.take();						
				processEdirMassage(next);
			} catch (InterruptedException e) {
				System.out.println(this.getClass().getName()+" interrupted! Thread will exit.");
				isStoped = true;
			} catch (Exception e){
				e.printStackTrace();					
			}
		}
	}
		
	/**
	 * translate a colorcode from the dstrace (eg %3C, %14C) to an SWT color
	 * 
	 * @param idmColorCode
	 * @return
	 */
	private int getSWTColorCode(int idmColorCode) {
		//System.out.println("getForegroundColor(" + idmColorCode + ")");
		switch (idmColorCode) {
		case 0:
			return SWT.COLOR_BLACK;
		case 1:
			return SWT.COLOR_BLUE;
		case 2:
			return SWT.COLOR_GREEN;
		case 3:
			return SWT.COLOR_CYAN;
		case 4:
			return SWT.COLOR_RED;
		case 5:
			return SWT.COLOR_MAGENTA;
		case 6:
			return SWT.COLOR_BLUE;
		case 7:
			return SWT.COLOR_GRAY;
		case 8:
			return SWT.COLOR_GRAY;
		case 9:
			return SWT.COLOR_BLUE;
		case 10:
			return SWT.COLOR_GREEN;
		case 11:
			return SWT.COLOR_CYAN;
		case 12:
			return SWT.COLOR_RED;
		case 13:
			return SWT.COLOR_MAGENTA;
		case 14:
			return SWT.COLOR_YELLOW;
		case 15:
		}
		return SWT.COLOR_WHITE;
	}
		
		
	private void processEdirMassage(EdirEventIntermediateResponse intermediateResponse){
		LogMessage[] evt = createLogMessage(intermediateResponse);
		if (evt != null){
			events.add(evt);
		}
		else{
			EventResponseData eventResponseData = intermediateResponse.getResponsedata();
			DebugEventData eventData = (DebugEventData) eventResponseData;
			System.out.println(">>>> No new message created for "+(eventData.getDsTime() * 1000L + eventData.getMilliSeconds()) +" and msg "+eventData.getFormatString());
		}
	}
		
		
	private LogMessage[] createLogMessage(EdirEventIntermediateResponse intermediateResponse) {
		EventResponseData eventResponseData = intermediateResponse.getResponsedata();

		DebugEventData eventData = (DebugEventData) eventResponseData;

		//This seems to do nothing. No clue what it is. the parameterarray is always empty...
		String formatedString = new com.novell.core.dstraceviewer.internal.DSPrintFormat(eventData.getFormatString(), getParameters(eventData)).toString();
		//String formatedString = eventData.getFormatString();
		//String formatedString = new com.novell.core.dstraceviewer.internal.DSPrintFormat(eventData.getFormatString(), getParameters(eventData)).toString();
			
		//Nothing to do if no content
		if (formatedString==null)
			return null;
			
			
		//LogMessage result = new LogMessage();			
		String thread=null;
		String message=null;
		String traceName=null;
		int tabIndex=0;
		int idmColor=0;
		Color swtColor = null;
		long eventDateMilis=0;
		
		//Get the thread and index of the thread out of the formatted String
		int index = Integer.MAX_VALUE;
		for (int i = 0; i < idmThreads.length; i++) {
			String threadt = idmThreads[i];
			int thisIndex = formatedString.indexOf(" "+threadt+":");
			if (thisIndex != -1){
				index = Math.min(index, thisIndex);
				if (index==thisIndex){
					thread=threadt;
					break;
				}
			}
		}
			
			
		if (index > 64){//No PT ST or EV found.

			//If we do not find the ST/PT token, or the token is past the 
			//64th character (a driver cn can not be more then 64 characters)
			index = formatedString.indexOf(" :");
			if (index == -1 || index > 64){
				//This is a partial message
				addAsMessageDetailToLast(formatedString);
				//We did not create a new message, so return nothing
				return null;
			}
			
			message = formatedString.substring(index+2);
				thread = idmThreads[idmThreads.length-1];//"??";
				traceName = formatedString.substring(0, index).trim();
				//tabIndex = 0;
			}else{
				message = formatedString.substring(index+4);
				//this.thread = logEntry.substring(index+1, index+3);
				traceName = formatedString.substring(0, index).trim();
			}
			
			//Remove and count the spaces at the start of the message: tabIndex
			tabIndex = 0;
			while(message.length() > tabIndex  &&  message.charAt(tabIndex)==' '){
				tabIndex++;
			}
			message = message.substring(tabIndex,message.length());
			
			//Get the message colour (if any)
			if (traceName.charAt(0)=='%'){
				//The colour is in the format %nnC where n is a number (eg: 3,14)
				int cIndex = traceName.indexOf('C');
				if (cIndex != -1 && cIndex <=3){
					String colour = traceName.substring(0, cIndex+1);
					colour=colour.substring(1,colour.length()-1);
					idmColor = Integer.parseInt(colour);
					traceName = traceName.substring(cIndex+1);
				}
			}
			
			swtColor = colors.getColor(getSWTColorCode(idmColor));
			//We need to intern the string before searching for a parent
			//The number of drivers will be limited, so adding the tracename to the String pool
			//should save some memory without impacting the String pool (permSpace)
			//Starting at java7, the String pool is no longer in perm but in heap.
			traceName = traceName.intern();
			
			//Get the event date/time
			eventDateMilis=eventData.getDsTime() * 1000L + eventData.getMilliSeconds();

			LogMessage previous = (LogMessage) events.getLastMessage(traceName, thread);
			System.out.println("Adding message to previous:"+previous);
			if (previous != null){
				return previous.createLogMessage(message, thread, tabIndex, traceName, idmColor, eventDateMilis, swtColor,formatedString);
			}
			else//Very first message for this trace/thread combination
				return new LogMessage[]{new RootLogMessage(message, thread, tabIndex, traceName, idmColor, eventDateMilis, swtColor, liveTraceEditorInput, formatedString)};
		}



	
	/**
	 * Add the given message string as a detail to the last message added to the event list.
	 * @param formatedString
	 */
	private void addAsMessageDetailToLast(String formatedString) {
		//Blindly add it to the last message in the event list
		//We have no trace nor thread information at all...
		LogMessage lastMessage =  (LogMessage)events.getLastMessage();//ClosestDetailMessage(eventData);
		System.out.println(">>> Blindly using message: "+lastMessage);
		if (lastMessage != null){						
			//The string can still start with a colour code. Strip that.
			if (formatedString.charAt(0)=='%'){
				//The colour is in the format %nnC where n is a number (eg: 3,14)
				int cIndex = formatedString.indexOf('C');
				if (cIndex != -1 && cIndex <=3){
					formatedString = formatedString.substring(cIndex+1);
				}
			}
			lastMessage.appendMessagePart(formatedString);
		}else{
			System.out.println("Unable to add message part:"+formatedString);
			System.out.println("No message to add anything to.");
			System.out.println("Message will be lost...");
		}
	}



}
