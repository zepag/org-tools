/*******************************************************************************
 * Copyright (c) 2008 Pierre-Antoine Grégoire.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Antoine Grégoire - initial API and implementation
 *******************************************************************************/
package org.org.eclipse.dws.ui.internal.views;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.dws.core.internal.jobs.ComputeArtifactDetailsJob;
import org.org.eclipse.dws.core.internal.jobs.ComputeArtifactVersionDetailsJob;
import org.org.eclipse.dws.core.internal.jobs.ComputeGroupDetailsJob;
import org.org.eclipse.dws.core.internal.jobs.ComputeRepositoryDetailsJob;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.Group;

/**
 * This view simply mirrors the current selection in the workbench window. It works for both, element and text selection.
 */
public class DetailsView extends ViewPart {
	private static final String REPOSITORY_DETAILS_PARSING_PARAMETERS = "Parsing parameters";

	private static final String ARTIFACT_VERSION_DETAILS_XML_EXCLUSIONS = "XML declaration with exclusions";

	private static final String ARTIFACT_VERSION_DETAILS_XML = "XML declaration";

	private static final String ARTIFACT_VERSION_DETAILS = "Artifact Version Details";

	private static final String ARTIFACT_DETAILS = "Artifact Details";

	private static final String GROUP_DETAILS = "Group Details";

	private static final String REPOSITORY_DETAILS = "CrawledRepository Details";

	private static final String LOADING = "Loading...";

	public static final String VIEW_ID = DetailsView.class.getName();

	private static final String EMPTY = "";

	private PageBook pagebook;

	private FormToolkit toolkit;

	private Map<String, ScrolledForm> forms = new ConcurrentHashMap<String, ScrolledForm>();

	private Map<String, FormText> formTexts = new ConcurrentHashMap<String, FormText>();

	private Map<String, Text> additionalTexts = new ConcurrentHashMap<String, Text>();

	// the listener we register with the selection service
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != DetailsView.this && sourcepart instanceof MavenRepositoriesView) {
				showSelection(sourcepart, selection);
			}
		}
	};

	/**
	 * Shows the given selection in this view.
	 */
	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) {
		setContentDescription(sourcepart.getTitle() + " (" + selection.getClass().getName() + ")");
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() == 1) {
				this.pagebook.showPage(forms.get(LOADING));
				final Object firstElement = ss.getFirstElement();
				if (firstElement instanceof CrawledRepository) {
					ComputeRepositoryDetailsJob job = new ComputeRepositoryDetailsJob((CrawledRepository) firstElement);
					job.addJobChangeListener(new JobDoneListener() {
						public void done(IJobChangeEvent event) {
							ComputeRepositoryDetailsJob finishedJob = (ComputeRepositoryDetailsJob) event.getJob();
							showRepository(finishedJob);
						}
					});
					job.schedule();
				} else if (firstElement instanceof Group) {
					ComputeGroupDetailsJob job = new ComputeGroupDetailsJob((Group) firstElement);
					job.addJobChangeListener(new JobDoneListener() {
						public void done(IJobChangeEvent event) {
							ComputeGroupDetailsJob finishedJob = (ComputeGroupDetailsJob) event.getJob();
							showGroup(finishedJob);
						}
					});
					job.schedule();
				} else if (firstElement instanceof Artifact) {
					ComputeArtifactDetailsJob job = new ComputeArtifactDetailsJob((Artifact) firstElement);
					job.addJobChangeListener(new JobDoneListener() {
						public void done(IJobChangeEvent event) {
							ComputeArtifactDetailsJob finishedJob = (ComputeArtifactDetailsJob) event.getJob();
							showArtifact(finishedJob);
						}
					});
					job.schedule();
				} else if (firstElement instanceof ArtifactVersion) {
					ComputeArtifactVersionDetailsJob job = new ComputeArtifactVersionDetailsJob((ArtifactVersion) firstElement);
					job.addJobChangeListener(new JobDoneListener() {
						public void done(IJobChangeEvent event) {
							ComputeArtifactVersionDetailsJob finishedJob = (ComputeArtifactVersionDetailsJob) event.getJob();
							showArtifactVersion(finishedJob);
						}
					});
					job.schedule();
				} else {
					this.pagebook.showPage(forms.get(EMPTY));
				}

			} else {
				this.pagebook.showPage(forms.get(EMPTY));
			}
		}
	}

	private void showArtifactVersion(final ComputeArtifactVersionDetailsJob finishedJob) {
		final DetailsView detailsView = this;
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (!formTexts.get(ARTIFACT_VERSION_DETAILS).isDisposed()) {
					detailsView.formTexts.get(ARTIFACT_VERSION_DETAILS).setText(finishedJob.getFormattedArtifactVersion(), true, false);
					detailsView.additionalTexts.get(ARTIFACT_VERSION_DETAILS + ARTIFACT_VERSION_DETAILS_XML).setText(finishedJob.getDependencyXML());
					detailsView.additionalTexts.get(ARTIFACT_VERSION_DETAILS + ARTIFACT_VERSION_DETAILS_XML_EXCLUSIONS).setText(finishedJob.getTransitiveDependenciesExclusions());
					detailsView.forms.get(ARTIFACT_VERSION_DETAILS).reflow(true);
					detailsView.pagebook.showPage(forms.get(ARTIFACT_VERSION_DETAILS));
				}
			}

		});

	}

	private void showArtifact(final ComputeArtifactDetailsJob finishedJob) {
		final DetailsView detailsView = this;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!formTexts.get(ARTIFACT_DETAILS).isDisposed()) {
					detailsView.formTexts.get(ARTIFACT_DETAILS).setText(finishedJob.getFormattedArtifact().toString(), true, false);
					detailsView.forms.get(ARTIFACT_DETAILS).reflow(true);
					detailsView.pagebook.showPage(forms.get(ARTIFACT_DETAILS));
				}
			}
		});
	}

	private void showGroup(final ComputeGroupDetailsJob finishedJob) {

		final DetailsView detailsView = this;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!formTexts.get(GROUP_DETAILS).isDisposed()) {
					detailsView.formTexts.get(GROUP_DETAILS).setText(finishedJob.getFormattedGroup().toString(), true, false);
					detailsView.forms.get(GROUP_DETAILS).reflow(true);
					detailsView.pagebook.showPage(forms.get(GROUP_DETAILS));
				}
			}
		});
	}

	private void showRepository(final ComputeRepositoryDetailsJob finishedJob) {
		final DetailsView detailsView = this;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!formTexts.get(REPOSITORY_DETAILS).isDisposed()) {
					detailsView.formTexts.get(REPOSITORY_DETAILS).setText(finishedJob.getFormattedRepository().toString(), true, false);
					detailsView.additionalTexts.get(REPOSITORY_DETAILS + REPOSITORY_DETAILS_PARSING_PARAMETERS).setText(finishedJob.getFormattedPatterns().toString());
					if (IHttpCrawledRepositorySetup.class.isAssignableFrom(finishedJob.getRepositorySetupType())) {
						detailsView.additionalTexts.get(REPOSITORY_DETAILS + REPOSITORY_DETAILS_PARSING_PARAMETERS).setVisible(true);
					}
					if (IFileSystemCrawledRepositorySetup.class.isAssignableFrom(finishedJob.getRepositorySetupType())) {
						detailsView.additionalTexts.get(REPOSITORY_DETAILS + REPOSITORY_DETAILS_PARSING_PARAMETERS).setVisible(false);
					}
					forms.get(REPOSITORY_DETAILS).reflow(true);
					pagebook.showPage(forms.get(REPOSITORY_DETAILS));
				}
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		// the PageBook allows simple switching between two viewers
		pagebook = new PageBook(parent, SWT.NONE);
		toolkit = new FormToolkit(parent.getDisplay());
		// create the base form
		createLoadPart();
		createEmptyPart();
		createComplexPart(ARTIFACT_VERSION_DETAILS, new String[] { ARTIFACT_VERSION_DETAILS_XML, ARTIFACT_VERSION_DETAILS_XML_EXCLUSIONS });
		createBasicPart(ARTIFACT_DETAILS);
		createBasicPart(GROUP_DETAILS);
		createBasicPart(REPOSITORY_DETAILS);
		createComplexPart(REPOSITORY_DETAILS, new String[] { REPOSITORY_DETAILS_PARSING_PARAMETERS });

		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
	}

	private void createLoadPart() {
		final String title = LOADING;
		ScrolledForm form = toolkit.createScrolledForm(pagebook);
		form.setBusy(true);
		form.setText(title);
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		FormText formText = toolkit.createFormText(form.getBody(), true);
		formText.setText("", false, true);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 1;
		formText.setLayoutData(td);
		forms.put(title, form);
		formTexts.put(title, formText);

	}

	private void createEmptyPart() {
		final String title = EMPTY;
		ScrolledForm form = toolkit.createScrolledForm(pagebook);
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		FormText formText = toolkit.createFormText(form.getBody(), true);
		formText.setText("", false, true);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 1;
		formText.setLayoutData(td);
		forms.put(title, form);
		formTexts.put(title, formText);

	}

	private void createComplexPart(String title, String[] expandTitles) {
		final ScrolledForm form = toolkit.createScrolledForm(pagebook);
		form.setText(title);
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		FormText formText = toolkit.createFormText(form.getBody(), true);
		formText.setText("", false, true);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 1;
		formText.setLayoutData(td);
		forms.put(title, form);
		formTexts.put(title, formText);
		for (String expandTitle : expandTitles) {
			Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
			td = new TableWrapData(TableWrapData.FILL);
			td.colspan = 1;
			section.setLayoutData(td);
			section.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					form.reflow(true);
				}
			});
			section.setText(expandTitle);
			section.setDescription("This is the description that goes " + "below the title");
			section.setExpanded(false);
			Composite sectionClient = toolkit.createComposite(section);
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.colspan = 1;
			td.grabVertical = true;
			sectionClient.setLayoutData(td);
			sectionClient.setLayout(new GridLayout());
			Text text = toolkit.createText(sectionClient, " \n \n \n \n \n \n \n \n", SWT.WRAP | SWT.BORDER | SWT.FLAT);
			text.setEditable(false);

			section.setClient(sectionClient);
			// ExpandableComposite expandableComposite = toolkit.createExpandableComposite(form.getBody(), ExpandableComposite.TREE_NODE);
			// expandableComposite.setText(expandTitle);
			// expandableComposite.setExpanded(false);
			// td = new TableWrapData(TableWrapData.FILL_GRAB);
			// td.colspan = 1;
			// td.grabVertical = true;
			// expandableComposite.setLayoutData(td);
			// expandableComposite.addExpansionListener(new ExpansionAdapter() {
			// @Override
			// public void expansionStateChanged(ExpansionEvent e) {
			// form.reflow(true);
			// }
			// });
			additionalTexts.put(title + expandTitle, text);
		}
	}

	private void createBasicPart(String title) {
		ScrolledForm form = toolkit.createScrolledForm(pagebook);
		form.setText(title);
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		FormText formText = toolkit.createFormText(form.getBody(), true);
		formText.setText("", false, true);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 1;
		formText.setLayoutData(td);
		forms.put(title, form);
		formTexts.put(title, formText);
	}

	@Override
	public void setFocus() {
		pagebook.setFocus();
	}

	@Override
	public void dispose() {
		// important: We need do unregister our listener when the view is disposed
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
		toolkit.dispose();
		super.dispose();
	}

	public static IViewPart showView() {
		try {
			return DWSUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DetailsView.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE/* view is only opened but not focused */);
		} catch (PartInitException pie) {
			ErrorDialog errorDialog = new ErrorDialog("Impossible to open DWS Details view", "Impossible to open DWS Details view", pie);
			errorDialog.open();
		}
		return null;
	}
}
