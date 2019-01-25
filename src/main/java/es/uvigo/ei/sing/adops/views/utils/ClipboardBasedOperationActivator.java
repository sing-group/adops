/*-
 * #%L
 * ADOPS
 * %%
 * Copyright (C) 2012 - 2019 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package es.uvigo.ei.sing.adops.views.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;

public class ClipboardBasedOperationActivator implements ClipboardListener {
	private final HashMap<String, Map<Class<?>, Integer>> operationRequirements;
	
	public ClipboardBasedOperationActivator() {
		this.operationRequirements = new HashMap<>();
	}
	

	public void addRequirement(String uid, Class<?> c) {
		this.addRequirement(uid, c, 1);
	}

	public void addRequirement(String uid, Class<?> c, int count) {
		if (!this.operationRequirements.containsKey(uid)) {
			this.operationRequirements.put(uid, new HashMap<Class<?>, Integer>());
		}
		
		this.operationRequirements.get(uid).put(c, count);
	}

	private void processClipboard() {
		for (String uid : this.operationRequirements.keySet()) {
			boolean requirementsSatisfied = true;

			for (Entry<Class<?>, Integer> c : this.operationRequirements.get(uid).entrySet()) {
				if (Core.getInstance().getClipboard().getItemsByClass(c.getKey()).size() < c.getValue()) {
					requirementsSatisfied = false;
					break;
				}
			}

			if (requirementsSatisfied) {
				Core.getInstance().enableOperation(uid);
			} else {
				Core.getInstance().disableOperation(uid);
			}
		}

	}

	public void elementAdded(ClipboardItem arg0) {
		processClipboard();
	}

	public void elementRemoved(ClipboardItem arg0) {
		processClipboard();
	}
}
