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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;

public class ColorManager {

	private final Map<Integer, Color> fColorTable = new HashMap<Integer, Color>(10);

	public void dispose() {
		Collection<Color> e = fColorTable.values();
		for (Color color : e) {
			color.dispose();
		}
	}
	
	public Color getColor(final int swtValue) {
		Color color = (Color) fColorTable.get(swtValue);
		
		if (color == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					fColorTable.put(swtValue, PlatformUI.getWorkbench().getDisplay().getSystemColor(swtValue));
				}
			});
			color = (Color) fColorTable.get(swtValue);
		}
		return color;
	}
}
