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
package es.uvigo.ei.sing.adops.operations.running;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.adops.datatypes.BatchProject;
import es.uvigo.ei.sing.adops.datatypes.BatchProjectOutput;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.Project;
import es.uvigo.ei.sing.adops.operations.CreateExperiment;

@Operation(name = "Run Batch Project")
public class ExecuteBatchProject {
	private BatchProject project;
	private int numberOfThread;
	private List<ProjectExecutorRunnable> tasks;

	@Port(name = "Batch Project", description = "Batch project to execute", direction = Direction.INPUT, order = 1)
	public void setProject(BatchProject project) {
		this.project = project;
	}

	@Port(name = "Num. of Threads", description = "Number of threads to use (simultaneous project executions)", direction = Direction.INPUT, order = 2)
	public void setNumberOfThread(int numberOfThread) {
		this.numberOfThread = numberOfThread;
	}

	@Port(direction = Direction.OUTPUT, order = 1000)
	public BatchProjectOutput run() {
		this.project.setRunning(true);
		final BatchProjectOutput output = this.project.getOutput();

		final ExecutorService executor = Executors.newFixedThreadPool(this.numberOfThread);
		this.tasks = new ArrayList<>(output.numProjects());

		for (int i = 0; i < output.numProjects(); i++) {
			final ProjectExecutorRunnable task = new ProjectExecutorRunnable(i, output);

			this.tasks.add(task);
			executor.execute(task);
		}

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.tasks = null;
		this.project.setRunning(false);

		return output;
	}

	@Cancel
	public void cancel() {
		if (this.tasks != null) {
			for (ProjectExecutorRunnable task : this.tasks) {
				task.cancel();
			}
		}
	}

	private static final class ProjectExecutorRunnable implements Runnable {
		private final int index;
		private final BatchProjectOutput output;
		private ExecuteExperimentBySteps execute;
		private final AtomicBoolean canceled;
		private Project project;
		private Experiment experiment;

		private ProjectExecutorRunnable(int index, BatchProjectOutput output) {
			this.index = index;
			this.output = output;
			this.execute = null;
			this.canceled = new AtomicBoolean(false);
			this.project = null;
		}

		public void cancel() {
			synchronized (this.canceled) {
				if (this.execute != null) {
					this.execute.cancel();
				}

				this.canceled.set(true);
			}
		}

		@Override
		public void run() {
			try {
				if (this.canceled.get())
					return;

				this.project = output.getProject(index);

				if (output.isReady(project)) {
					if (this.canceled.get())
						return;

					if (!output.hasResult(project)) {
						if (this.canceled.get())
							return;

						final CreateExperiment create = new CreateExperiment();
						create.setProject(project);
						create.setName("batch");
						create.setSequences("");

						this.experiment = create.create();

						if (this.canceled.get())
							return;
						try {
							execute = new ExecuteExperimentBySteps();
							execute.setExperiment(experiment);
							execute.setUseStdOutput(true);

							if (this.canceled.get())
								return;
							execute.createExperimentOutput();
							if (this.canceled.get())
								return;
							execute.runTCoffee();
							if (this.canceled.get())
								return;
							execute.runMrBayes();
							if (this.canceled.get())
								return;
							execute.runCodeML();
							if (this.canceled.get())
								return;

							output.addResult(project, execute.getResult());
						} catch (Exception e) {
							output.addResult(project, e);
						}
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				if (this.experiment != null) {
					this.experiment.setRunning(false);
					if (!this.output.hasResult(this.project))
						this.experiment.delete();
				}
			}
		}
	}
}
