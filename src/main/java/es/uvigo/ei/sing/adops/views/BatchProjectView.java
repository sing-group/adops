/*-
 * #%L
 * ADOPS
 * %%
 * Copyright (C) 2012 - 2017 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
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
package es.uvigo.ei.sing.adops.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences;
import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences.Confidence;
import es.uvigo.ei.sing.adops.datatypes.BatchProject;
import es.uvigo.ei.sing.adops.datatypes.BatchProjectOutput;
import es.uvigo.ei.sing.adops.datatypes.ExperimentOutput;
import es.uvigo.ei.sing.adops.datatypes.Project;

public class BatchProjectView extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;
	private static final String OPERATION_RUN_EXPERIMENT = "es.uvigo.ei.sing.adops.operations.executebatchproject";
	private static final String OPERATION_DELETE_PROJECT = "es.uvigo.ei.sing.adops.operations.deletebatchproject";

	private final BatchProject project;

	private static final Color BACKGROUND_COLOR = Color.WHITE;

	private final JXTaskPaneContainer taskPaneContainer;
	private final JButton btnLaunchExecution;
	private final JButton btnEditProperties;
	private final JButton btnDeleteProject;
	private final JButton btnCheckPrograms;

	public BatchProjectView(final BatchProject project) throws IllegalArgumentException, IOException {
		super(new BorderLayout());

		this.project = project;

		this.taskPaneContainer = new JXTaskPaneContainer();
		this.taskPaneContainer.setBackground(BatchProjectView.BACKGROUND_COLOR);

		final JXTaskPane tpProject = new JXTaskPane("Project Options");
		this.taskPaneContainer.add(tpProject);

		this.btnEditProperties = new JButton(
			new AbstractAction("Edit Properties") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					final boolean isFinished = project.isRunning() || project.isFinished();

					final EditConfigurationDialog configuration = new EditConfigurationDialog(
						project,
						Workbench.getInstance().getMainFrame(),
						isFinished ? "View Properties" : "Edit Properties",
						true,
						!isFinished
					);

					final ActionListener listener = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							project.updateProjectsConfiguration();
						}
					};
					configuration.getBtnSave().addActionListener(listener);
					configuration.getBtnClose().addActionListener(listener);

					configuration.setVisible(true);
				}
			}
		);

		this.btnLaunchExecution = new JButton(
			new AbstractAction("Launch Execution") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					btnLaunchExecution.setEnabled(false);
					btnEditProperties.setText("View Properties");

					project.clearUncompleteProjects();

					Workbench.getInstance().executeOperation(
						BatchProjectView.OPERATION_RUN_EXPERIMENT,
						null,
						Arrays.asList(project, 1)
					);
				}
			}
		);

		this.btnCheckPrograms = new JButton(
			new AbstractAction("Check Programs") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					new CheckProgramsDialog(BatchProjectView.this.project).setVisible(true);
				}
			}
		);

		this.btnDeleteProject = new JButton(
			new AbstractAction("Delete Project") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					final int option = JOptionPane.showConfirmDialog(
						BatchProjectView.this,
						"Project will be deleted (including files and experiments). Do you want to continue?",
						"Delete project",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE
					);

					if (option == JOptionPane.YES_OPTION) {
						Workbench.getInstance().executeOperation(
							BatchProjectView.OPERATION_DELETE_PROJECT,
							null,
							Arrays.asList(project)
						);
					}
				}
			}
		);

		tpProject.add(this.btnEditProperties);
		tpProject.add(this.btnDeleteProject);
		tpProject.add(this.btnCheckPrograms);
		tpProject.add(this.btnLaunchExecution);

		final File[] directories = project.getProjectDirectories();
		final Object[][] data = new Object[directories.length][2];
		for (int i = 0; i < directories.length; i++) {
			data[i][0] = directories[i].getName();
			data[i][1] = "Ready";
		}

		final JXTable table = new JXTable(new BatchProjectTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);

		this.add(this.taskPaneContainer, BorderLayout.EAST);
		this.add(new JScrollPane(table), BorderLayout.CENTER);

		this.updateButtons();
	}

	@Override
	public void update(Observable o, Object arg) {
		this.updateButtons();
	}

	private void updateButtons() {
		if (this.project.isRunning()) {
			this.btnDeleteProject.setEnabled(false);
			this.btnEditProperties.setText("View Properties");
			this.btnLaunchExecution.setEnabled(false);
			this.btnCheckPrograms.setEnabled(false);
		} else if (this.project.isFinished()) {
			this.btnDeleteProject.setEnabled(true);
			this.btnEditProperties.setText("View Properties");
			this.btnLaunchExecution.setEnabled(false);
			this.btnCheckPrograms.setEnabled(false);
		} else {
			this.btnDeleteProject.setEnabled(true);
			this.btnEditProperties.setText("Edit Properties");
			this.btnLaunchExecution.setEnabled(true);
			this.btnCheckPrograms.setEnabled(true);
		}
	}

	private class BatchProjectTableModel extends AbstractTableModel implements Observer {
		private static final long serialVersionUID = 1L;
		private final BatchProjectOutput output;
		private final Project[] projects;
		private final Map<ExperimentOutput, PSSSummary> summaries;

		public BatchProjectTableModel() throws IllegalArgumentException, IOException {
			this.output = BatchProjectView.this.project.getOutput();
			this.projects = new Project[this.output.numProjects()];
			this.summaries = new HashMap<>();
			
			for (int j = 0; j < this.output.numProjects(); j++) {
				this.projects[j] = this.output.getProject(j);
				this.projects[j].addObserver(this);
			}
		}

		@Override
		public String getColumnName(int column) {
			return new String[] {
				"Project", "Status", "PSS1", "PSS2", "PSS3", "PSS4"
			}[column];
		}

		@Override
		public int getRowCount() {
			return BatchProjectView.this.project.getFastaFiles().length;
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			try {
				final Project project = this.projects[rowIndex];

				if (columnIndex == 0) {
					return project.getName();
				} else if (columnIndex == 1) {
					if (this.output.isRunning(project)) {
						return "Running";
					} else if (this.output.isReady(project)) {
						return "Ready";
					} else if (this.output.isFinished(project)) {
						return "Finished";
					} else if (this.output.isError(project)) {
						return "Error";
					} else {
						throw new IllegalStateException("Unknown project state");
					}
				} else {
					final ExperimentOutput expOutput = this.output.getProjectOutput(project);

					if (expOutput != null) {
						if (!this.summaries.containsKey(expOutput)) {
							this.summaries.put(expOutput, new PSSSummary(expOutput.loadConfidences()));
						}

						return this.summaries.get(expOutput).psss[columnIndex - 2];
					} else {
						return null;
					}
				}
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public void update(Observable o, Object arg) {
			this.fireTableDataChanged();
			BatchProjectView.this.updateButtons();
		}
	}

	private static class PSSSummary {
		public final String pss1, pss2, pss3, pss4;
		public final String[] psss;

		public PSSSummary(AlignmentConfidences confidences) {
			final StringBuilder sb1 = new StringBuilder();
			final StringBuilder sb2 = new StringBuilder();
			final StringBuilder sb3 = new StringBuilder();
			final StringBuilder sb4 = new StringBuilder();

			for (String model : confidences.getModels()) {
				final PSSCount count = new PSSCount(confidences.getModel(model));

				if (sb1.length() > 0)
					sb1.append('/');
				sb1.append(count.pss1);
				if (sb2.length() > 0)
					sb2.append('/');
				sb2.append(count.pss2);
				if (sb3.length() > 0)
					sb3.append('/');
				sb3.append(count.pss3);
				if (sb4.length() > 0)
					sb4.append('/');
				sb4.append(count.pss4);
			}

			this.pss1 = sb1.toString();
			this.pss2 = sb2.toString();
			this.pss3 = sb3.toString();
			this.pss4 = sb4.toString();

			this.psss = new String[] {
				this.pss1, this.pss2, this.pss3, this.pss4
			};
		}
	}

	private static class PSSCount {
		public final int pss1, pss2, pss3, pss4;

		public PSSCount(Map<Integer, Confidence> pss) {
			int pss1 = 0;
			int pss2 = 0;
			int pss3 = 0;
			int pss4 = 0;

			for (Confidence confidence : pss.values()) {
				final double neb = confidence.getNeb();
				final double beb = confidence.getBeb();

				if (neb > 0.95d && beb > 0.95d) {
					pss1++;
				} else if (neb > 0.95d && beb > 0.9d) {
					pss2++;
				} else if (neb > 0.9d && beb > 0.95d) {
					pss3++;
				} else if (neb > 0.9d && beb > 0.9d) {
					pss4++;
				}
			}

			this.pss1 = pss1;
			this.pss2 = pss2;
			this.pss3 = pss3;
			this.pss4 = pss4;
		}
	}
}
