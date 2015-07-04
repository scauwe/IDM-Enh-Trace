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

import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageList;
import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage;
import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage.PolicySet;
import info.vancauwenberge.designer.enhtrace.model.logmessage.OutOfTabGeneratingLogMessage;
import info.vancauwenberge.designer.enhtrace.model.logmessage.PolicyRootLogMessage;
import info.vancauwenberge.designer.enhtrace.model.logmessage.StatusLogMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class StaticTraceEditorInput implements IEditorInput {

	public enum Channel{
		PT,ST;
		
	    public static Channel forName(String name)
	    {
	    	try{
	    		return Channel.valueOf(name);
	    	}catch (Exception e) {
	    		return null;
			}
	    }
	}
	
	public enum PolicySteps{
		INPUT_TRANSFORM(null,IPolicySetLogMessage.PolicySet.ITP, Channel.PT),
		OUTPUT_TRANSFORM(null,IPolicySetLogMessage.PolicySet.OTP,Channel.PT),
		PUB_SCHEMA_MAP(Channel.PT,IPolicySetLogMessage.PolicySet.SCHEMA,Channel.PT),
		SUB_SCHEMA_MAP(Channel.ST,IPolicySetLogMessage.PolicySet.SCHEMA,Channel.ST),
		PUB_EVENT(Channel.PT,IPolicySetLogMessage.PolicySet.ETP,Channel.PT),
		PUB_MATCHING(Channel.PT,IPolicySetLogMessage.PolicySet.MP,Channel.PT),
		PUB_CREATION(Channel.PT,IPolicySetLogMessage.PolicySet.CP,Channel.PT),
		PUB_PLACEMENT(Channel.PT,IPolicySetLogMessage.PolicySet.PP,Channel.PT),
		PUB_COMMAND(Channel.PT,IPolicySetLogMessage.PolicySet.CTP,Channel.PT),
		SUB_EVENT(Channel.ST,IPolicySetLogMessage.PolicySet.ETP,Channel.ST),
		SUB_MATCHING(Channel.ST,IPolicySetLogMessage.PolicySet.MP,Channel.ST),
		SUB_CREATION(Channel.ST,IPolicySetLogMessage.PolicySet.CP,Channel.ST),
		SUB_PLACEMENT(Channel.ST,IPolicySetLogMessage.PolicySet.PP,Channel.ST),
		SUB_COMMAND(Channel.ST,IPolicySetLogMessage.PolicySet.CTP,Channel.ST),

		PUB_NOTIFY_FILTER(Channel.PT,IPolicySetLogMessage.PolicySet.NOTIFY_FILTER,Channel.PT),
		SUB_NOTIFY_FILTER(Channel.ST,IPolicySetLogMessage.PolicySet.NOTIFY_FILTER,Channel.ST),
		SUB_SYNC_FILTER(Channel.ST,IPolicySetLogMessage.PolicySet.SUB_SYNC_FILTER,Channel.ST),
		PUB_SYNC_FILTER(Channel.PT,IPolicySetLogMessage.PolicySet.PUB_SYNC_FILTER,Channel.PT),
		SHIM(null,IPolicySetLogMessage.PolicySet.SHIM,Channel.PT),
		PUB_ADD_PROCESSOR(Channel.PT,IPolicySetLogMessage.PolicySet.ADD_PROCESSOR,Channel.PT),
		SUB_ADD_PROCESSOR(Channel.ST,IPolicySetLogMessage.PolicySet.ADD_PROCESSOR,Channel.ST),
		PUB_ASS_PROCESSOR(null,IPolicySetLogMessage.PolicySet.PUB_ASSOCIATION_PROCESSOR,Channel.PT),
		SUB_ASS_PROCESSOR(null,IPolicySetLogMessage.PolicySet.SUB_ASSOCIATION_PROCESSOR,Channel.ST),
		PUB_RESET_INJECTION(null,IPolicySetLogMessage.PolicySet.PUB_RESET_INJECTION,Channel.PT),
		SUB_SHUTDOWN(Channel.ST,IPolicySetLogMessage.PolicySet.SHUTDOWN,Channel.ST),
		PUB_STARTUP(Channel.PT,IPolicySetLogMessage.PolicySet.STARTUP,Channel.PT),
		;
		
		private PolicySet associatedLogSet;
		private Channel defaultChannel;
		private Channel nextChannel;
		private PolicySteps(Channel defaultChannel, IPolicySetLogMessage.PolicySet associatedLogSet, Channel nextChannel){
			this.associatedLogSet = associatedLogSet;
			this.defaultChannel = defaultChannel;
			this.nextChannel = nextChannel;
		}
		
		public static PolicySteps getPolicyStepFor(Channel thread, IPolicySetLogMessage.PolicySet policySetLogContext){
			for (PolicySteps aPolicySet : PolicySteps.values()) {
				if (aPolicySet.associatedLogSet==policySetLogContext){
					if (aPolicySet.defaultChannel==null || aPolicySet.defaultChannel==thread){
						System.out.println("getPolicySetsFor = "+ aPolicySet);
						return aPolicySet;
					}
				}
			}
			return null;
		}

		public Channel getDefaultChannel() {
			return defaultChannel;
		}

		public Channel getNextChannel() {
			return nextChannel;
		}
		
	}
	
	public interface IStaticInputListener{

		void notifyRootChanged(ILogMessage newValue, ILogMessage oldValue);
		
	}
	private Set<IStaticInputListener> listeners = new HashSet<IStaticInputListener>();
	private ILogMessage detailRoot;
	private Channel rootChannel;
	
	private Map<PolicySteps, List<IPolicySetLogMessage>> policy2MessageMap = new EnumMap<PolicySteps, List<IPolicySetLogMessage>>(PolicySteps.class);
	private Map<IPolicySetLogMessage,List<StatusLogMessage>> policy2StatusMap = new HashMap<IPolicySetLogMessage,List<StatusLogMessage>>();
	private Map<IPolicySetLogMessage,List<ILogMessage>> policy2TraceMap=new HashMap<IPolicySetLogMessage,List<ILogMessage>>();

	//private ILogMessageList list;

	public StaticTraceEditorInput(ILogMessage detailRoot, Channel rootChannel){
		this.detailRoot = detailRoot;
		this.rootChannel = rootChannel;
		analyzeData();
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public ILogMessage getRoot(){
		return detailRoot;
	}
	
	/*
	 * Get the logmessage for the given policy set
	 */
	public IPolicySetLogMessage getMessageRootFor(IPolicySetLogMessage.PolicySet policySet) {
		List<ILogMessage> children = detailRoot.getChildren();
		for (ILogMessage iLogMessage : children) {
			if (iLogMessage instanceof IPolicySetLogMessage){
				IPolicySetLogMessage policyLog = (IPolicySetLogMessage)iLogMessage;
				if (policyLog.getPolicySet() == policySet)
					return policyLog;
			}
		}
		return null;
	}

	/**
	 * Dispose of the object, clearing all references to other objects.
	 */
	public void dispose() {
		detailRoot = null;
		listeners.clear();
		listeners = null;
		//list = null;
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
		StringBuilder sb = new StringBuilder();
		sb.append(detailRoot.getTraceName()).append("-").append(detailRoot.getThread()).append(":").append(detailRoot.getMessage());
		return sb.toString();
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Trace detail";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof StaticTraceEditorInput){
			StaticTraceEditorInput otherInput = (StaticTraceEditorInput)obj;
			return detailRoot == otherInput.detailRoot;
		}
		return false;
	}
	
	
	private boolean haveSameSignature(List<ILogMessage> path, int pathIndex, ILogMessage compareTo){
		ILogMessage pathItem = path.get(pathIndex);
		
		if ( (pathItem.getThread()==compareTo.getThread()) 
				&& (pathItem.getTraceName()==compareTo.getTraceName()) 
				&&	pathItem.getMessage().equals(compareTo.getMessage()) && compareTo.getClass()==pathItem.getClass()){
					if (pathIndex==0)
						return true;
					List<ILogMessage> children = compareTo.getChildren();
					for (ILogMessage iLogMessage : children) {
						if (haveSameSignature(path, pathIndex-1, iLogMessage))
							return true;
					}
				}
		return false;
	}
	
	
	/**
	 * Get's the active list by searhing for the active live trace and retrieving the list from that object
	 * @return
	 */
	private ILogMessageList getActiveList(){
    	//We create a dummy input so that we can search for the editor linked to the server
		LiveTraceEditorInput input = detailRoot.getOriginatingServer();
		if (input.isDisposed())
			return null;
		
    	IWorkbenchPage localIWorkbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart currentEditor = localIWorkbenchPage.findEditor(input);
		
		if (currentEditor!= null){
			return (ILogMessageList)currentEditor.getAdapter(ILogMessageList.class);
		}
		return null;
	}
	
	
	/**
	 * Rescan the list of logmessages to set the root to the latest message with the same 'signature' as the current root.
	 */
	public void rescan(){
		//Create the path
		System.out.println("StaticTraceEditorInput.rescan(): start");
		List<ILogMessage> path = new ArrayList<ILogMessage>();
		ILogMessage pathItem = detailRoot;
		while(pathItem != null){
			path.add(pathItem);
			pathItem = pathItem.getParent();
		}
		System.out.println("StaticTraceEditorInput.rescan(): Current path:"+path);
		
		

		
		
		//The last item in the pathList is the root. Get the latest root from the list corresponding to this
		ILogMessageList messageList = getActiveList();
		if (messageList==null){
			MessageDialog.open(MessageDialog.WARNING, null, "No active live trace", "No live trace editor could be found", SWT.NONE);
			return;
		}
		
		ILogMessage[] events = messageList.getMessages();
		
		//Start with the last message
		if (events.length > 0){
			int index = events.length -1; 
			while (index>=0){
				ILogMessage previous = events[index];
				index--;
				if (previous.getParent()==null){
					if (haveSameSignature(path, path.size()-1, previous)){
						System.out.println("StaticTraceEditorInput.rescan(): found   :"+previous);
						System.out.println("StaticTraceEditorInput.rescan(): previous:"+detailRoot);
						if (detailRoot == previous){
							System.out.println("StaticTraceEditorInput.rescan(): Last found is equal to current");
							break;
						}
						System.out.println("StaticTraceEditorInput.rescan(): notifying listeners.");
						ILogMessage oldValue = detailRoot;
						detailRoot = previous;
						for (IStaticInputListener aListener : listeners) {
							aListener.notifyRootChanged(detailRoot, oldValue);
						}
						break;
					}
				}
			}
		}
		System.out.println("StaticTraceEditorInput.rescan(): end");
	}
	
	/**
	 * Add an editor listener
	 * @param newListener
	 */
	public void addListener(IStaticInputListener newListener){
		listeners.add(newListener);
	}

	/**
	 * Remove an editor listener
	 * @param newListener
	 */
	public void removeListener(IStaticInputListener newListener){
		listeners.remove(newListener);
	}


	/**
	 * Append all status messages to the allStatusses list that are a child of the given message (but no a known policyset log message)
	 * @param allStatusses List to append to
	 * @param message start point
	 */
	private void appendChildStatusses(List<StatusLogMessage> allStatusses, ILogMessage message) {
		if (message.hasChildren()){
			/*
			if (message instanceof PolicyRootLogMessage){
				if (((PolicyRootLogMessage)message).getPolicySet()==PolicySet.ADD_PROCESSOR)
					System.out.println("breakpoint");
			}*/
			List<ILogMessage> children = message.getChildren();
			for (ILogMessage iLogMessage : children) {
				if (iLogMessage instanceof StatusLogMessage)
					allStatusses.add((StatusLogMessage)iLogMessage);
				else{
					//Do not recurse into another found policy set
					boolean isKnownPolicy = false;
					Collection<List<IPolicySetLogMessage>> policysetLogMessages = policy2MessageMap.values();
					for (List<IPolicySetLogMessage> list : policysetLogMessages) {
						if (list.contains(iLogMessage)){
							System.out.println(this.getClass().getName()+"-appendChildStatusses() not analyzing known child:"+iLogMessage);
							isKnownPolicy = true;
							break;
						}
					}
					if (!isKnownPolicy)
						//OK, it is not somewhere else: process this child
						appendChildStatusses(allStatusses, iLogMessage);
				}
			}
		}
	}
	
	

	/**
	 * Append all trace messages to the allTraces list that are a child of the given message (but no a known policyset log message)
	 * @param allTraces
	 * @param message
	 */
	private void appendChildTraces(List<ILogMessage> allTraces, ILogMessage message) {
		if (message.hasChildren()){
			List<ILogMessage> children = message.getChildren();
			for (ILogMessage iLogMessage : children) {
				if (iLogMessage instanceof OutOfTabGeneratingLogMessage){
					List<ILogMessage> traceCandidates = iLogMessage.getChildren(); 
					if (traceCandidates != null && traceCandidates.size()>=1)
						allTraces.add(traceCandidates.get(traceCandidates.size()-1));
				}
				else{
					//Do not recurse into another found policy set
					Collection<List<IPolicySetLogMessage>> policysetLogMessages = policy2MessageMap.values();
					for (List<IPolicySetLogMessage> list : policysetLogMessages) {
						if (list.contains(iLogMessage)){
							return;
						}
					}
					//OK, it is not somewhere else: process this child
					appendChildTraces(allTraces, iLogMessage);
				}
			}
		}
	}
	
	private void findPolicysetRoots(Channel channel, ILogMessage root) {
		if (root.hasChildren()) {
			for (ILogMessage logChild : root.getChildren()) {
				if (logChild instanceof IPolicySetLogMessage) {
					IPolicySetLogMessage logMsg = (IPolicySetLogMessage) logChild;
					PolicySteps set = PolicySteps.getPolicyStepFor(channel, logMsg.getPolicySet());
					if (set != null) {
						//Move up the channel
						channel = set.nextChannel;
						List<IPolicySetLogMessage> currentList = policy2MessageMap.get(set);
						if (currentList == null) {
							currentList = new ArrayList<IPolicySetLogMessage>();
							policy2MessageMap.put(set, currentList);
						}
						currentList.add(logMsg);
						// If this is a subflow root, find all children...
						if (logMsg.getPolicySet().isSubflowRoot()) {
							findPolicysetRoots(channel, logMsg);
						}
					}
				}
			}
		}
	}
	
	/**
	 * (re)analyze the data, building a cahce for all policy sets, statusses etc
	 */
	private void analyzeData(){
		System.out.println(this.getClass().getName()+" analyze() start");
		policy2MessageMap.clear();
		policy2StatusMap.clear();
		policy2TraceMap.clear();
		
		findPolicysetRoots(rootChannel, detailRoot);

		//Find all statusses and traces per LogMessage of a policy
		Collection<List<IPolicySetLogMessage>> values = policy2MessageMap.values();
		for (List<IPolicySetLogMessage> iPolicyList : values) {
			for (IPolicySetLogMessage iPolicySetLogMessage : iPolicyList) {
				List<StatusLogMessage> allStatusses = new ArrayList<StatusLogMessage>();
				List<ILogMessage> allTraces = new ArrayList<ILogMessage>();
				appendChildStatusses(allStatusses, iPolicySetLogMessage);
				appendChildTraces(allTraces, iPolicySetLogMessage);
				if (allStatusses.size()>0)
					policy2StatusMap.put(iPolicySetLogMessage, allStatusses);
				if (allTraces.size()>0)
					policy2TraceMap.put(iPolicySetLogMessage, allTraces);			
			}
		}

		
		
		//buildNonpolicyStatusList(root);
		System.out.println(this.getClass().getName()+" analyze() end");
	}

	public void refresh() {
		analyzeData();
	}

	public Map<PolicySteps, List<IPolicySetLogMessage>> getPolicy2MessageMap() {
		return policy2MessageMap;
	}

	public Map<IPolicySetLogMessage,List<StatusLogMessage>> getPolicy2StatusMap() {
		return policy2StatusMap;
	}

	public Map<IPolicySetLogMessage,List<ILogMessage>> getPolicy2TraceMap() {
		return policy2TraceMap;
	}
	

	
}
