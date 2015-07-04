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

import info.vancauwenberge.designer.enhtrace.Activator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

import com.novell.admin.common.exceptions.SPIException;
import com.novell.core.datatools.access.nds.DSAccess;
import com.novell.core.datatools.access.nds.DSAccessException;
import com.novell.designer.model.CAttribute;
import com.novell.designer.model.CStructure;
import com.novell.idm.IdmModel;
import com.novell.idm.model.IdentityVault;
import com.novell.idm.model.Server;

/**
 * This class has been created with the sole purpose of being able to test the plugin inside eclipse without having to include all the datamodal components.
 * 
 * @author stefaanv
 *
 */
public class LiveServerTraceEditorInput extends LiveTraceEditorInput{
	private static final String STORE_SHOW_HOST_MISSING = LiveServerTraceEditorInput.class.getName()+".showMissingHost"; 
	public LiveServerTraceEditorInput(com.novell.idm.model.Server server) throws SPIException, IllegalArgumentException, DSAccessException{
		super(extractServer(server), extractuserName(server), extractPassword(server), extractUseSSL(server));		
	}
	

	private static String extractPassword(Server server) {
		IdentityVault vault = server.getIdentityVault();
		return vault.getPassword();
	}

	private static boolean extractUseSSL(Server server) {
		IdentityVault vault = server.getIdentityVault();
		return vault.isUseLDAPSecureChannel();
	}

	private static String extractuserName(Server server) throws SPIException, IllegalArgumentException, DSAccessException {
		IdentityVault vault = server.getIdentityVault();
		DSAccess access = IdmModel.getItemDSAccess(vault);
		//No need to release access. this seems to log out. I can only assume that dsaccess is reused.
		return access.convertToLDAPAcceptableFormat(vault.getUserName());
	}

	private static String extractServer(Server server) {
		String serverName = server.getDnsName();
		if (serverName==null || "".equals(serverName.trim()))
			serverName = server.getHost();
		IdentityVault vault = server.getIdentityVault();

		if (serverName==null || "".equals(serverName.trim())){
			
			serverName = vault.getHost();
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			store.setDefault(STORE_SHOW_HOST_MISSING, true);
			if (store.getBoolean(STORE_SHOW_HOST_MISSING)){
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Hostname missing", "No hostname was found on server object. Defaulting to the hostname of the Identity Vault object", "Do not show this anymore", false, Activator.getDefault().getPreferenceStore(), STORE_SHOW_HOST_MISSING);
			}
		}
		int port = 636;
		if (extractUseSSL(server)){
			port = vault.getLdapSecurePort();
		}else{
			port = vault.getLdapClearTextPort();
		}
		serverName = serverName+":"+port;
		System.out.println(LiveServerTraceEditorInput.class.getName()+" - extractServer() returned: "+serverName);
		return serverName;
	}

}
