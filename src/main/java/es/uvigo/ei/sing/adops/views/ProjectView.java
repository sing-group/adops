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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.inputgui.Common;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.Project;
import es.uvigo.ei.sing.adops.datatypes.ProjectExperiment;
import es.uvigo.ei.sing.adops.views.utils.ClipboardItemView;
import es.uvigo.ei.sing.adops.views.utils.ViewUtils;

public class ProjectView extends JPanel implements Observer, ClipboardItemView {
	private static final String OPERATION_DELETE_PROJECT = "es.uvigo.ei.sing.adops.operations.deleteproject";
	private static final String OPERATION_DELETE_EXPERIMENT = "es.uvigo.ei.sing.adops.operations.deleteexperiment";
	private static final String OPERATION_CLEAN_EXPERIMENT = "es.uvigo.ei.sing.adops.operations.cleanexperiment";
	private static final String OPERATION_CREATE_EXPERIMENT = "es.uvigo.ei.sing.adops.operations.createexperiment";
	private static final String OPERATION_COPY_EXPERIMENT = "es.uvigo.ei.sing.adops.operations.copyexperiment";

	private static final ImageIcon IMAGE_CLOSE = new ImageIcon(ProjectView.class.getResource("images/close15.png"));
	private static final ImageIcon IMAGE_CLOSE_OVER = new ImageIcon(ProjectView.class.getResource("images/close_over15.png"));
	private static final ImageIcon IMAGE_CLOSE_PRESSED = new ImageIcon(ProjectView.class.getResource("images/close_pressed15.png"));
	private static final Dimension IMAGE_CLOSE_SIZE = new Dimension(
		IMAGE_CLOSE.getIconWidth() + 7, IMAGE_CLOSE.getIconHeight() + 7
	);

	private static final Color BACKGROUND_COLOR = Color.WHITE;

	private static final long serialVersionUID = 1L;

	private final Project project;

	private final JTabbedPane tabPanel;
	private final Map<ProjectExperiment, ExperimentView> experimentTab;
	private final Map<ExperimentView, ProjectExperiment> tabExperiment;

	private final JButton btnProps;
	private final JButton btnSequences;
	private final JButton btnCopy;
	private final JButton btnCheckPrograms;
	private final JButton btnClean;
	private final JButton btnLaunch;

	private final JXTaskPaneContainer taskPaneContainer;
	private final JXTaskPane tpExperiment;

	public ProjectView(final Project project) {
		super(new BorderLayout());

		this.project = project;

		this.experimentTab = new HashMap<>();
		this.tabExperiment = new HashMap<>();

		this.tabPanel = new DraggableJTabbedPane();
		this.tabPanel.setBackground(ProjectView.BACKGROUND_COLOR);
		this.tabPanel.setOpaque(true);

		this.btnProps = new JButton("Edit Properties");
		this.btnSequences = new JButton("Select Sequences");
		this.btnCheckPrograms = new JButton("Check Programs");
		this.btnCopy = new JButton("Copy Experiment");
		this.btnClean = new JButton("Clean Experiment");
		this.btnLaunch = new JButton("Launch");

		this.taskPaneContainer = new JXTaskPaneContainer();
		this.taskPaneContainer.setBackground(ProjectView.BACKGROUND_COLOR);

		final JXTaskPane tpProject = this.createTPProject(project);
		this.tpExperiment = this.createTPExperiment();

		this.taskPaneContainer.add((Component) tpProject);
		this.taskPaneContainer.add((Component) this.tpExperiment);

		this.tabPanel.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ProjectView.this.updateExperimentButtons();
				}
			}
		);

		this.add(this.tabPanel, BorderLayout.CENTER);
		this.add(this.taskPaneContainer, BorderLayout.EAST);

		for (ProjectExperiment experiment : project.getExperiments()) {
			this.addExperiment(experiment);
		}

		project.addObserver(this);

		this.updateExperimentButtons();
	}

	private final JXTaskPane createTPProject(final Project project) {
		final JXTaskPane tpProject = new JXTaskPane("Project Options");

		tpProject.add(
			new JButton(
				new AbstractAction("Create Experiment") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						final String name = JOptionPane.showInputDialog(
							ProjectView.this,
							"Experiment name",
							"Create new experiment",
							JOptionPane.QUESTION_MESSAGE
						);

						if (name != null) {
							Workbench.getInstance().executeOperation(
								ProjectView.OPERATION_CREATE_EXPERIMENT,
								null,
								Arrays.asList(project, name, "")
							);
						}
					}
				}
			)
		);
		tpProject.add(
			new JButton(
				new AbstractAction("View Fasta File") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						new TextFileDialog(project.getRenamedFastaFile(), Workbench.getInstance().getMainFrame(), "Fasta File", true).setVisible(true);
					}
				}
			)
		);
		tpProject.add(
			new JButton(
				new AbstractAction("View Names File") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						new TextFileDialog(project.getNamesFile(), Workbench.getInstance().getMainFrame(), "Names File", true).setVisible(true);
					}
				}
			)
		);

		tpProject.add(
			new JButton(
				new AbstractAction("Add Sequences") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						final int confirmContinue = JOptionPane.showConfirmDialog(ProjectView.this,
							"Your original Fasta file will be modified if you add more sequences. " +
							"Do you want to continue?",
							"Add Sequences",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE
						);
						
						if (confirmContinue == JOptionPane.YES_OPTION) {
							final JFileChooser fc = Common.SINGLE_FILE_CHOOSER;
							
							if (fc.showOpenDialog(ProjectView.this) == JFileChooser.APPROVE_OPTION) {
								final File fastaFile = fc.getSelectedFile();
								
								try {
									ProjectView.this.project.addSequences(fastaFile);
									JOptionPane.showMessageDialog(
										ProjectView.this,
										"<html>Sequences added to the project. New sequences will be automatically<br/>"
										+ "used in new experiments. Existing experiments were not changed.</html>",
										"Add Sequences",
										JOptionPane.INFORMATION_MESSAGE
									);
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(
										ProjectView.this,
										"Unexpected error: " + ex.getMessage(),
										"Add Sequences Error",
										JOptionPane.ERROR_MESSAGE
									);
								}
							}
						}
					}
				}
			)
		);
		
		tpProject.add(
			new JButton(
				new AbstractAction("Check Programs") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						new CheckProgramsDialog(ProjectView.this.project).setVisible(true);
					}
				}
			)
		);
		tpProject.add(
			new JButton(
				new AbstractAction("Edit Properties") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						new EditConfigurationDialog(project, Workbench.getInstance().getMainFrame(), "Edit Properties", true).setVisible(true);
					}
				}
			)
		);
		tpProject.add(
			new JButton(
				new AbstractAction("Delete Project") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						final int option = JOptionPane.showConfirmDialog(
							ProjectView.this,
							"Project will be deleted (including files and experiments). Do you want to continue?",
							"Delete project",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE
						);

						if (option == JOptionPane.YES_OPTION) {
							Workbench.getInstance().executeOperation(
								ProjectView.OPERATION_DELETE_PROJECT,
								null,
								Arrays.asList(project)
							);
						}
					}
				}
			)
		);

		return tpProject;
	}

	private final JXTaskPane createTPExperiment() {
		final JXTaskPane tpExperiment = new JXTaskPane("Experiment Options");

		tpExperiment.add(this.btnProps);
		tpExperiment.add(this.btnSequences);
		tpExperiment.add(this.btnCheckPrograms);
		tpExperiment.add(this.btnCopy);
		tpExperiment.add(this.btnClean);
		tpExperiment.add(this.btnLaunch);

		this.btnProps.setAction(
			new ExperimentAction("Edit Properties") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e, ProjectExperiment experiment) {
					new EditConfigurationDialog(
						experiment,
						Workbench.getInstance().getMainFrame(),
						"Edit Properties",
						true,
						!experiment.hasResult()
					).setVisible(true);
				}
			}
		);

		this.btnSequences.setAction(
			new ExperimentAction("Select Sequences") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled() {
					return this.hasExperiment() && !this.getExperiment().hasResult();
				}

				@Override
				public void actionPerformed(ActionEvent e, final ProjectExperiment experiment) {
					final SelectSequenceDialog dialog = new SelectSequenceDialog(experiment);
					dialog.addComponentListener(
						new ComponentAdapter() {
							@Override
							public void componentHidden(ComponentEvent e) {
								experiment.getConfiguration().setInputSequences(dialog.getSelectedIndexesAsString());
								experiment.storeAllProperties();
							}
						}
					);

					dialog.setVisible(true);
				}
			}
		);

		this.btnCheckPrograms.setAction(
			new ExperimentAction("Check Programs") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled() {
					return this.hasExperiment() && !this.getExperiment().hasResult();
				}

				@Override
				public void actionPerformed(ActionEvent e, ProjectExperiment experiment) {
					new CheckProgramsDialog(experiment).setVisible(true);
				}
			}
		);

		this.btnCopy.setAction(
			new ExperimentAction("Copy Experiment") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e, final ProjectExperiment experiment) {
					final String name = JOptionPane.showInputDialog(
						ProjectView.this,
						"Experiment name",
						"Copy Experiment",
						JOptionPane.QUESTION_MESSAGE
					);

					if (name != null) {
						Workbench.getInstance().executeOperation(
							ProjectView.OPERATION_COPY_EXPERIMENT,
							null,
							Arrays.asList(experiment, name)
						);
					}
				}
			}
		);

		this.btnClean.setAction(
			new ExperimentAction("Clean Experiment") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled() {
					return this.hasExperiment() && this.getExperiment().hasResult();
				}

				@Override
				public void actionPerformed(ActionEvent e, final ProjectExperiment experiment) {
					final int selection = JOptionPane.showConfirmDialog(
						ProjectView.this,
						"Experiment will be cleaned (all its result files will be deleted). Do you want to continue?",
						"Clean Experiment",
						JOptionPane.YES_NO_OPTION
					);

					if (selection == JOptionPane.YES_OPTION) {
						Workbench.getInstance().executeOperation(
							ProjectView.OPERATION_CLEAN_EXPERIMENT,
							null,
							Arrays.asList(experiment)
						);
					}
				}
			}
		);

		this.btnLaunch.setAction(
			new ExperimentAction("Launch") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled() {
					return this.hasExperiment() && !this.getExperiment().hasResult();
				}

				@Override
				protected void actionPerformed(ActionEvent e, ProjectExperiment experiment) {
					final int selection = JOptionPane.showConfirmDialog(
						ProjectView.this,
						"Do you want to see the execution log? (This may slow the execution)",
						"Execution Log",
						JOptionPane.YES_NO_CANCEL_OPTION
					);

					if (selection != JOptionPane.CANCEL_OPTION) {
						final ExperimentView experimentView = ProjectView.this.experimentTab.get(experiment);

						ProjectView.this.btnLaunch.setEnabled(false);
						ProjectView.this.btnSequences.setEnabled(false);
						ProjectView.this.btnCopy.setEnabled(false);
						ProjectView.this.btnProps.setEnabled(false);

						experimentView.launchExecution(selection == JOptionPane.YES_OPTION);
					}
				}
			}
		);

		return tpExperiment;
	}

	private void updateExperimentButtons() {
		final ProjectExperiment experiment = this.getActiveExperiment();

		if (experiment == null) {
			ProjectView.this.btnProps.setEnabled(false);
			ProjectView.this.btnSequences.setEnabled(false);
			ProjectView.this.btnCheckPrograms.setEnabled(false);
			ProjectView.this.btnCopy.setEnabled(false);
			ProjectView.this.btnClean.setEnabled(false);
			ProjectView.this.btnLaunch.setEnabled(false);

			ProjectView.this.btnProps.setText("Edit Properties");
		} else if (experiment.hasResult()) {
			ProjectView.this.btnProps.setEnabled(true);
			ProjectView.this.btnSequences.setEnabled(false);
			ProjectView.this.btnCheckPrograms.setEnabled(false);
			ProjectView.this.btnCopy.setEnabled(true);
			ProjectView.this.btnClean.setEnabled(!experiment.isRunning());
			ProjectView.this.btnLaunch.setEnabled(false);

			ProjectView.this.btnProps.setText("View Properties");
		} else {
			ProjectView.this.btnProps.setEnabled(true);
			ProjectView.this.btnSequences.setEnabled(true);
			ProjectView.this.btnCheckPrograms.setEnabled(true);
			ProjectView.this.btnCopy.setEnabled(true);
			ProjectView.this.btnClean.setEnabled(!experiment.isClean());
			ProjectView.this.btnLaunch.setEnabled(true);

			ProjectView.this.btnProps.setText("Edit Properties");
		}
	}

	public ProjectExperiment getActiveExperiment() {
		final Component selectedTab = this.tabPanel.getSelectedComponent();

		return (selectedTab == null) ? null : this.tabExperiment.get(selectedTab);
	}

	private void addExperimentTab(final ProjectExperiment experiment) {
		final ExperimentView experimentView = new ExperimentView((ProjectExperiment) experiment);

		final JPanel tab = new JPanel(new BorderLayout(3, 0));
		tab.setOpaque(false);

		final JLabel lblTab = new JLabel(experiment.getName());
		final JButton btnTab = new JButton(ProjectView.IMAGE_CLOSE);
		btnTab.setPressedIcon(ProjectView.IMAGE_CLOSE_PRESSED);
		btnTab.setRolloverIcon(ProjectView.IMAGE_CLOSE_OVER);
		btnTab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTab.setPreferredSize(ProjectView.IMAGE_CLOSE_SIZE);
		btnTab.setSize(ProjectView.IMAGE_CLOSE_SIZE);

		tab.add(lblTab, BorderLayout.WEST);
		tab.add(btnTab, BorderLayout.EAST);

		this.tabPanel.addTab(null, experimentView);

		final int tabIndex = this.tabPanel.getTabCount() - 1;
		this.tabPanel.setTabComponentAt(tabIndex, tab);
		this.tabPanel.setSelectedIndex(tabIndex);

		this.experimentTab.put(experiment, experimentView);
		this.tabExperiment.put(experimentView, experiment);

		btnTab.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final int selection = JOptionPane.showConfirmDialog(
						ProjectView.this,
						"Experiment will be delete (including files). Do you want to continue?",
						"Delete Experiment",
						JOptionPane.YES_NO_OPTION
					);

					if (selection == JOptionPane.YES_OPTION) {
						Workbench.getInstance().executeOperation(
							ProjectView.OPERATION_DELETE_EXPERIMENT,
							null,
							Arrays.asList(
								new Object[] {
									experiment
						}
							)
						);
					}
				}
			}
		);

		this.updateExperimentButtons();
	}

	private boolean addExperiment(final ProjectExperiment experiment) {
		if (this.experimentTab.containsKey(experiment)) {
			return false;
		} else {
			this.addExperimentTab(experiment);

			experiment.addObserver(
				new Observer() {
					@Override
					public void update(Observable o, Object param) {
						if (o instanceof Experiment) {
							final Experiment experiment = (Experiment) o;

							if (experiment == ProjectView.this.getActiveExperiment()) {
								ViewUtils.safeGUIRun(
									new Runnable() {
										@Override
										public void run() {
											ProjectView.this.updateExperimentButtons();
										}
									}
								);
							}
						}
					}
				}
			);

			return true;
		}
	}

	private boolean removeExperiment(ProjectExperiment experiment) {
		if (this.experimentTab.containsKey(experiment)) {

			this.tabPanel.remove(this.experimentTab.get(experiment));

			this.tabExperiment.remove(this.experimentTab.get(experiment));
			this.experimentTab.remove(experiment);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update(Observable observable, Object param) {
		if (param instanceof ProjectExperiment) {
			final ProjectExperiment experiment = (ProjectExperiment) param;

			ViewUtils.safeGUIRun(
				new Runnable() {
					@Override
					public void run() {
						if (experiment.isDeleted())
							ProjectView.this.removeExperiment(experiment);
						else
							ProjectView.this.addExperiment(experiment);
					}
				}
			);
		}
	}

	private abstract class ExperimentAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExperimentAction(String name) {
			super(name);
		}

		protected ProjectExperiment getExperiment() {
			return ProjectView.this.getActiveExperiment();
		}

		protected boolean hasExperiment() {
			return this.getExperiment() != null;
		}

		@Override
		public boolean isEnabled() {
			return this.hasExperiment();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (this.hasExperiment()) {
				this.actionPerformed(e, this.getExperiment());
			}
		}

		protected abstract void actionPerformed(ActionEvent e, ProjectExperiment experiment);
	}

	@Override
	public boolean canShowClipboardItem(Object clipboardItem) {
		return this.experimentTab.containsKey(clipboardItem);
	}

	@Override
	public boolean showClipboardItem(Object clipboardItem) {
		if (this.experimentTab.containsKey(clipboardItem)) {
			this.tabPanel.setSelectedComponent(
				this.experimentTab.get(clipboardItem)
			);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "ProjectView";
	}
}
