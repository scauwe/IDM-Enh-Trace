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


import java.security.GeneralSecurityException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPResponseQueue;
import com.novell.ldap.events.edir.EdirEventIntermediateResponse;
import com.novell.ldap.events.edir.EdirEventSpecifier;
import com.novell.ldap.events.edir.MonitorEventRequest;

public class EDirEventSource extends Thread{
	private static final int  EVT_DB_DIRXM = 214;
	private static final int EVT_DB_DIRXM_DRIVERS = 217;
	
	private static final String[] m_eventsList = { 
			"PT:[ ]*Applying publisher filter", 
			"PT:[ ]*Filtering out notification-only attributes", 
			"PT:[ ]*Performing operation", 
			"PT:[ ]*Pumping XDS to eDirectory", 
			"PT:[ ]*Remote Interface Driver: Document sent", 
			"PT:[ ]*Applying command transformation policies", 
			"PT:[ ]*Applying XSLT policy", 
			"PT:[ ]*Policy returned", 
			"PT:[ ]*Applying output transformation policies", 
			"PT:[ ]*Applying schema mapping policies to output", 
			"PT:[ ]*Fixing up association references", 
			"PT:[ ]*Applying publisher filter\\. \\(specific policy described\\)", 
			"ST:[ ]*Applying subscriber filter\\. \\(specific policy described\\)", 
			"PT:[ ]*Applying policy", 
			"PT:[ ]*Applying input transformation policies", 
			"PT:[ ]*No match found", 
			"Drvrs : Code\\(", 
			"ST:[ ]*Applying schema mapping policies to input", 
			"ST:[ ]*topdriver-initnds dtd version", 
			"ST:[ ]*Subscriber processing add", 
			"ST:[ ]*Apply to add", 
			"ST:[ ]*Evaluating selection criteria for rule", 
			"ST:[ ]*Applying rule", 
			"ST:[ ]*arg-match-attr", 
			"ST:[ ]*Mapping attr-name", 
			"ST:[ ]*Mapping class-name", 
			"ST:[ ]*Applying to add", 
			"ST:[ ]*Rule Selected", 
			"ST:[ ]*Action", 
			"ST:[ ]*\\(if-class-name", 
			"ST:[ ]*\\{disregard spaces\\} Applying \\(no specific policy described\\)", 
			"ST:[ ]*Applying XSLT policy", 
			"ST:[ ]*Processing events for transaction", 
			"ST:[ ]*Applying event transformation policies", 
			"ST:[ ]*Processing operation", 
			"ST:[ ]*Filtering out notification-only attributes", 
			"ST:[ ]*Pumping XDS to eDirectory", 
			"ST:[ ]*Submitting document to subscriber shim", 
			"ST:[ ]*Remote Interface Driver: Document sent", 
			"ST:[ ]*Remote Interface Driver: Sending", 
			"ST:[ ]*Remote Interface Driver: Document", 
			"ST:[ ]*Remote Interface Driver: Closing connection", 
			"ST:[ ]*Remote Interface Driver: Connection closed", 
			"ST:[ ]*Remote Interface Driver: end getSchema\\(\\)", 
			"ST:[ ]*DriverShim\\.getSchema\\(\\) returned", 
			"ST:[ ]*Fixing up association references", 
			"ST:[ ]*End transaction", 
			"ST:[ ]*Reading relevant attributes", 
			"ST:[ ]*Discarding transaction because of optimization", 
			"ST:[ ]*SubscriptionShim\\.execute\\(\\) returned", 
			"ST:[ ]*Policy returned", 
			"ST:[ ]*Applying command transformation policies",
			"ST:[ ]*Submitting add to subscriber shim", 
			"ST:[ ]*Applying object placement policies",
			"ST:[ ]*Applying object creation policies",
			"ST:[ ]*Query from policy result",
			"ST:[ ]*Applying policy", 
			"ST:[ ]*Applying output transformation policies", 
			"ST:[ ]*Applying schema mapping policies to output", 
			"ST:[ ]*Applying object matching policies", 
			"ST:[ ]*Performing operation query",
			"ST:[ ]*No event transformation policies",
			"ST:[ ]*No input transformation policies", 
			"ST:[ ]*\\{disregard spaces\\} Submitting document to subscriber shim",
			"ST:[ ]*Remote Interface Driver",
			"ST:[ ]*Resolving association references", 
			"ST:[ ]*Start transaction", 
			"ST:[ ]*Subscriber processing sync",
			"PT:[ ]*Resolving association references",
			"PT:[ ]*Applying event transformation policies",
			"PT:[ ]*Performing operation query", 
			"ST:[ ]*Synthetic add", 
			"ST:[ ]*No output transformation policies", 
			"PT:[ ]*Applying schema mapping policies to input",
			"PT:[ ]*No associated objects",
			"PT:[ ]*No input transformation policies", 
			"PT:[ ]*No output transformation policies",
			"PT:[ ]*No command transformation policies",
			"PT:[ ]*No event transformation policies",
			"ST:[ ]*\\{disregard spaces\\} Match found",
			"PT:[ ]*\\{disregard spaces\\} Match found",
			"PT:[ ]*Receiving DOM document from app", 
			"ST:[ ]*Applying input transformation policies", 
			"EV:[ ]*Writing data to cache",
			"ST:[ ]*No match found", 
			"ST:[ ]*No existing association",
			"ST:[ ]*No object placement policies", 
			"ST:[ ]*No object matching policies", 
			"ST:[ ]*No object creation policies",
			"ST:[ ]*\\r\\nDirXML Log Event", 
			"ST:[ ]*Processing returned document",
			"ST:[ ]*No(\\n|\\z|\\r)",
			"EV:[ ]*Committing", 
			"EV:[ ]*Entry ID",
			"EV:[ ]*Event: type",
			"EV:[ ]*Read",
			"EV:[ ]*Logically purged", 
			"EV:[ ]*Physically purged", 
			"EV:[ ]*Elapsed time", 
			"EV:[ ]*Wrote", 
			"EV:(\\n|\\z|\\r)" };
	private LinkedBlockingQueue<EdirEventIntermediateResponse> edirEvents = new LinkedBlockingQueue<EdirEventIntermediateResponse>(20);

	private String host;
	private String userName;
	private String password;
	private boolean useTLS;
	private int port = 389;
	private LDAPConnection connection;
	private boolean isrunning = true;
	private LDAPResponseQueue ldapresponsequeue;

	public EDirEventSource(String host, String userName, String password, boolean useTLS){
		super("LiveMessageSource");
		setDaemon(true);
		this.host = host;
		this.userName = userName;
		this.password = password;
		this.useTLS = useTLS;
		
	    //Do we have a port in the host. If so, use it.
	    int i = host.indexOf(":");
	    if (i != -1)
	    {
	      this.port = Integer.parseInt(host.substring(i + 1));
	      this.host = host.substring(0, i);
	    }
	    
	    if (useTLS && port==636){
	    	System.out.println(this.getClass().getName() + " - Warn: TLS should use clear text port (389), not SSL port (636)");
	    }

	}

	public LinkedBlockingQueue<EdirEventIntermediateResponse> getEventQueue(){
		return edirEvents;
	}


	
	public void stopGenerating() {
		System.out.println(this.getClass().getName()+" - stopGenerating()");
		isrunning  = false;
		
		edirEvents.clear();
    	//Stop the ldap event queue. This will cause the getMessage() to return
    	try {
    		if (ldapresponsequeue != null && connection != null)
    			connection.abandon(ldapresponsequeue);
		} catch (LDAPException e) {
			e.printStackTrace();
		}
		
	}

	public void run(){
		try{
			if (createLDAPConnection())
				doPolling();
		}finally{
			try{
				if (connection!= null && useTLS)
					connection.stopTLS();
				connection.disconnect();
			}catch (Exception e) {
			}
		}
	}


	private EdirEventSpecifier[] getEdirEventDescriptors() {
		EdirEventSpecifier[] specifier = new EdirEventSpecifier[2];
	    specifier[0] = new EdirEventSpecifier(EVT_DB_DIRXM, 0);
	    specifier[1] = new EdirEventSpecifier(EVT_DB_DIRXM_DRIVERS, 0);
		return specifier;
	}

	/**
	 * Do the actual polling for eDir events:
	 *  - Send out the extended operation
	 *  - poll for events.
	 *  - add the events to the eventQueue
	 */
	private void doPolling() {
		System.out.println(this.getClass().getName() + " - Initializing LDAP poll");
		try {
			//Start the queue
	        this.ldapresponsequeue = connection.extendedOperation(new MonitorEventRequest(getEdirEventDescriptors()), null, null);
		} catch (LDAPException e1) {
			throw new RuntimeException(e1);
		}


        int ai[] = ldapresponsequeue.getMessageIDs();
        if(ai.length >= 1) {
    		System.out.println(this.getClass().getName() + " - LDAP poll started");
        	int messageid = ai[ai.length-1];
        	
        	//Do the actual loop
        	while (isrunning) {
				System.out.println(this.getClass().getName() + " - Polling thread: message received");
    			LDAPMessage ldapmessage;
				try {
					//This blocks until we have a response
					ldapmessage = ldapresponsequeue.getResponse(messageid);
        			if(ldapmessage != null)
        				processmessage(ldapmessage);
				}catch(IllegalArgumentException interruptedexception) {
					System.out.println(this.getClass().getName() + " - Stopped by IllegalArgument");
					isrunning=false;
				}catch(InterruptedException interruptedexception) {
					System.out.println(this.getClass().getName() + " - Stopped by InterruptedException");
					isrunning=false;
				} catch (LDAPException e) {
					e.printStackTrace();
				}
			}
			System.out.println(this.getClass().getName() + " - Stopped");
        	
        	//Stop the queue in case it was not yet stopped
        	try {
				connection.abandon(ldapresponsequeue);
			} catch (LDAPException e) {
				e.printStackTrace();
			}

        } else {
        	//No clue what to do. Throw a runtime Exception.
            throw new RuntimeException("Unable to Obtain Message Id from LDAPResponseQueue");
        }
	}

	/**
	 * Validate the LDAP Message for exceptions and if ok, add it to te processing queue
	 * @param ldapmessage
	 * @throws InterruptedException
	 * @throws LDAPException
	 */
	private void processmessage(LDAPMessage ldapmessage) throws InterruptedException, LDAPException
    {
        if(ldapmessage instanceof EdirEventIntermediateResponse){
        	((EdirEventIntermediateResponse)ldapmessage).chkResultCode();
        	edirEvents.put((EdirEventIntermediateResponse)ldapmessage);
        }else{
        	System.out.println(this.getClass().getName() + " - unsupported response: "+ldapmessage);
        }
    }

	
    private SSLContext createSSLContext(String protocol) throws GeneralSecurityException{
    	SSLContext sslContext = SSLContext.getInstance(protocol);
    	TrustManager[] trustManagers = (new TrustManager[] {
    			new LDAPTrustManager()
            });
    	sslContext.init(null, trustManagers, null);
    	return sslContext;
    }

	/**
	 * Create the LDAP connection
	 * @return true if the connection was created.
	 * @throws Exception
	 */
	 private boolean createLDAPConnection() {
	    try{
		    //Prapare for SSL
		    if (useTLS)
		    {
		    	System.out.println(this.getClass().getName() + " - TLS setup");
		    	SSLContext sslContext = createSSLContext("TLS");
		    	
		    	LDAPJSSESecureSocketFactory localLDAPJSSESecureSocketFactory = new LDAPJSSESecureSocketFactory(sslContext.getSocketFactory());
		    	connection = new LDAPConnection(localLDAPJSSESecureSocketFactory);
		    }else{
		    	System.out.println(this.getClass().getName() + " - Non TLS");
		    	connection = new LDAPConnection();
		    }

		    
			System.out.println(this.getClass().getName() + " - Connecting to "+host+":"+port + (useTLS?" using TLS":" in clear text"));
		    connection.connect(this.host, this.port);
		    if (useTLS && port == 389){
		    	System.out.println(this.getClass().getName() + " - Starting TLS");
		    	connection.startTLS();
		    }
			System.out.println(this.getClass().getName() + " - Binding");
	    	connection.bind(3, this.userName, this.password.getBytes("UTF8"));
			System.out.println(this.getClass().getName() + " - LDAP Connection done");
	    	return true;
	    }catch (Throwable e) {
	    	e.printStackTrace();
	    	Throwable cause = e.getCause();
	    	final Throwable exception;
	    	if (cause!= null){
	    		exception=cause;
	    	}else{
	    		exception = e;
	    	}
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
			    	MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.ICON_ERROR);
			        messageBox.setMessage(exception.getClass().getName()+": "+exception.getMessage());
			         messageBox.open();
				}
			});

	        return false;
		}
	    
	  }
	
}
