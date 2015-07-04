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
import info.vancauwenberge.designer.enhtrace.api.IRootLogMessage;
import info.vancauwenberge.designer.enhtrace.editor.input.LiveTraceEditorInput;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.swt.graphics.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class RootLogMessage extends SyntheticParentLogMessage implements IRootLogMessage{
	private String association;
	private String srcDn;
	private String operationName;
//	private EnumMap<IPolicySetLogMessage.PolicySet, ILogMessage> policySetMap = new EnumMap<IPolicySetLogMessage.PolicySet, ILogMessage>(IPolicySetLogMessage.PolicySet.class);
	private String className;
	private DocumentBuilderFactory dbf;
	private String destDn;
	private Status operationStatus=null;
	private String thread;
	private boolean transactionClosed = false;
	
	/*
	 * The tracename. If not set, then this is by default the driver name.
	 */
	private String traceName;
	private LiveTraceEditorInput liveTraceEditor;


	/**
	 * All the events that are the real roots of an event.
	 */
	public static final String[] knownRoots = {
		"Start transaction.",
		//"Processing events for transaction.",
		"Receiving DOM document from application.",
		"Submitting identification query to subscriber shim:",
		"Injecting User Agent XDS command document into Subscriber channel.",
		"In publisher thread.",
		"Subscriber thread starting.",
		"Received state change event.",
	};
	
	
	
	private RootLogMessage(){
		this.dbf = DocumentBuilderFactory.newInstance();		
	}
	

	public RootLogMessage(String message, String thread, int tabIndex,
			String traceName, int idmColor, long eventDateMilis,
			Color swtColor, LiveTraceEditorInput liveTraceEditor, String rawData) {
		this();
		this.thread = thread;
		this.traceName = traceName;
		this.liveTraceEditor = liveTraceEditor;
		setMessage(message);
		//setIdmColor(idmColor);
		setSwtColor(swtColor);
		setTabIndex(tabIndex);
		setEventDateMilis(eventDateMilis);
		setRawData(rawData);
	}

	@Override
	public String getAssociation() {
		extractMetaData();
		return association;
	}


	/*
	 * Extract the dn, operationName and association from the event
	 */
	private void extractMetaData(){
		if (association==null && srcDn==null && destDn==null && operationName==null && className==null){
			//System.out.println("extractMetaData");
			
			DocumentBuilder db=null;
			try {
				db = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			String detail = getMessageDetail();
			if (detail != null)
				if (extractMetaData(db, detail))
					return;
			if (hasChildren()){
				List<ILogMessage> children = getChildren();
				//This is called 
				for (ILogMessage iLogMessage : children) {
					detail = iLogMessage.getMessageDetail();
					if (detail != null){
						if (extractMetaData(db, detail))
							return;
					}
				}
			}
		}
	}


	private boolean extractMetaData(DocumentBuilder db, String detail) {
		try{
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(detail));

			Document doc = db.parse(is);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			//The operation
			NodeList operations = (NodeList) xPath.compile("/nds/input/*").evaluate(doc, XPathConstants.NODESET);
			if (operations.getLength()>0){
				for (int i = 0; i < operations.getLength(); i++) {
					Element element = (Element) operations.item(i);
					operationName = element.getNodeName();
					NamedNodeMap attributes = element.getAttributes();
					Node eventDnNode = attributes.getNamedItem("src-dn");
					if (eventDnNode != null)
						srcDn = eventDnNode.getNodeValue();
					eventDnNode = attributes.getNamedItem("dest-dn");
					if (eventDnNode != null)
						destDn = eventDnNode.getNodeValue();
					Node classNode = attributes.getNamedItem("class-name");
					if (classNode != null)
						className = classNode.getNodeValue();
					Node associationNode = (Node) xPath.compile("association").evaluate(element, XPathConstants.NODE);
					if (associationNode != null){
						Node textNode = associationNode.getFirstChild();
						//if (textNode instanceof org.w3c.dom.CharacterData) {
							org.w3c.dom.CharacterData cd = (org.w3c.dom.CharacterData) textNode;
							association = cd.getData();
						//}
					}
					//OK, we found our node
					return true;
				}
			}
		}catch (Exception e) {
			System.out.println("Unable to parse as XML:\n"+detail);
			//e.printStackTrace();
		}
		return false;
	}
	
	/*
	@Override
	public ILogMessage getPolicyMessage(IPolicySetLogMessage.PolicySet policySet){
		return policySetMap.get(policySet);
	}
	*/
	
	/*
	@Override
	public String getPolicyInput(IPolicySetLogMessage.PolicySet policySet){
		ILogMessage message = policySetMap.get(policySet);
		if (message!=null){
			List<ILogMessage> children = getChildren();
			int index = children.indexOf(message);
			for (int i = index; i >=0; i--) {
				ILogMessage previousMessage = children.get(i);
				String detail = previousMessage.getMessageDetail();
				if (detail != null)
					return detail;				
			}
		}
		return null;
	}*/
/*	
	@Override
	public String getPolicyOutput(IPolicySetLogMessage.PolicySet policySet){
		ILogMessage message = policySetMap.get(policySet);
		if (message!=null){
			List<ILogMessage> children = getChildren();
			int index = children.indexOf(message);
			for (int i = index; i <children.size(); i++) {
				ILogMessage previousMessage = children.get(i);
				String detail = previousMessage.getMessageDetail();
				if (detail != null)
					return detail;
			}
		}
		return null;
		
	}
	*/
	@Override
	public String getThread() {
		return thread;
	}

	public LiveTraceEditorInput getOriginatingServer(){
		return liveTraceEditor;
	}

	@Override
	public String getSrcDn() {
		extractMetaData();
		return srcDn;
	}

	public String getDestDn() {
		extractMetaData();
		return destDn;
	}
	
	public IRootLogMessage getRootMessage(){
		return this;
	}

/*
	@Override
	void addChild(LogMessage logEvent) {
		super.addChild(logEvent);
		
		if (logEvent instanceof IPolicySetLogMessage){
			policySetMap.put(((IPolicySetLogMessage)logEvent).getPolicySet(), logEvent);
		}
	}
*/
/*	@Override
	public ILogMessage getPolicySetRoot(IPolicySetLogMessage.PolicySet policySet){
		return policySetMap.get(policySet);
	}
*/

	@Override
	public String getOperationName() {
		extractMetaData();
		return operationName;
	}

	@Override
	public Status getOperationStatus() {
		return operationStatus;
	}

	@Override
	public String getOperationClass() {
		extractMetaData();
		return className;
	}
	
	/**
	 * RootLogMessage accepts all except other rootlogmessage candidates
	 */
	public boolean _acceptAsChild(Class<? extends LogMessage> childClass, int childTabIndex, String childMessage, boolean thisIsKnownRoot){
		return (!transactionClosed) && (!isKnownRootCandidate(childMessage, childTabIndex));
	}
	
	public boolean acceptAsChild(LogMessage childCandidate){
		return !(childCandidate instanceof RootLogMessage);
		//return _acceptAsChild(childCandidate, true);
	}

	/*
	public boolean profileEquals(LogMessage siblingCandidate){
		return _profileEquels(siblingCandidate, true);
	}*/

	protected void handleSetStatus(StatusLogMessage statusMessage) {
		Status status = statusMessage.getStatus();
		if (status==null)
			return;
		if ((operationStatus==null) || (operationStatus.getLevel() < status.getLevel()))
			this.operationStatus = status;
	}


	public static boolean isKnownRootCandidate(final String message, final int tabIndex){
		//Test if the message is a well known root (start of an event thread on subscriber or publisher).
		if ((tabIndex == 0) && (message!= null) && !message.trim().equals("")){
			for (int i = 0; i < knownRoots.length; i++) {
				if (message.startsWith(knownRoots[i])){
					return true;
				}
			}
		}
		return false;
	}


	protected void setThread(String thread) {
		this.thread = thread;		
	}


	public void setTraceName(String traceName) {
		this.traceName = traceName;
	}


	@Override
	public String getTraceName() {
		return traceName;
	}
	
	@Override
	public boolean isTransactionClosed(){
		return transactionClosed;
	}

	public void setTransactionClosed(boolean closed){
		this.transactionClosed = closed;
	}

	
}
