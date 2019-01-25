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
package es.uvigo.ei.sing.adops.datatypes;

import static java.util.Collections.emptySet;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public abstract class AbstractOperationOutput extends Observable implements OperationOutput {
	private int state;
	protected boolean deleted;

	public AbstractOperationOutput(int state) {
		this.state = state;
		this.deleted = false;
	}

	@Override
	public int getState() {
		return this.state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public boolean isDeleted() {
		return this.deleted;
	}

	@Override
	public void delete() {
		if (!this.isDeleted()) {
			for (File file : this.getResultFiles()) {
				if (file != null && file.exists())
					file.delete();
			}

			this.deleted = true;

			this.setChanged();
			this.notifyObservers();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public File[] getResultFiles() {
		final Set<File> files = new HashSet<File>();
		final Class<? extends AbstractOperationOutput> currentClass = this.getClass();

		for (Method method : currentClass.getMethods()) {
			final String methodName = method.getName();
			final Class<?>[] parameters = method.getParameterTypes();
			final Class<?> returnType = method.getReturnType();

			if (methodName.startsWith("get") && parameters.length == 0) {
				if (
					methodName.endsWith("File") &&
						returnType.equals(File.class)
				) {
					try {
						files.add((File) method.invoke(this));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (
					methodName.endsWith("Files") &&
						returnType.isAssignableFrom(Set.class)
				) {
					try {
						files.addAll((Set<File>) method.invoke(this));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		files.removeAll(this.getIgnoredFiles());

		return files.toArray(new File[files.size()]);
	}

	protected Set<File> getIgnoredFiles() {
		return emptySet();
	}

	public boolean isComplete() {
		for (File file : this.getResultFiles()) {
			if (file == null || !file.exists())
				return false;
		}

		return true;
	}
}
