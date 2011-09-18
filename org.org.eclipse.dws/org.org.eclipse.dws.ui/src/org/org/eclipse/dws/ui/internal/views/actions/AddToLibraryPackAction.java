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
package org.org.eclipse.dws.ui.internal.views.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;
import org.org.eclipse.dws.core.internal.bridges.LibraryPackModelPersistence;
import org.org.eclipse.dws.core.internal.dialogs.LibraryPackNamePromptDialog;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion.Target;
import org.org.eclipse.dws.ui.internal.views.LibraryPacksView;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;

/**
 * The Class CopyDependencyInfoAction.
 * 
 * @author pagregoire
 */
public class AddToLibraryPackAction extends AbstractDWSViewAction {
	/**
	 * The Constructor.
	 * 
	 * @param clipboard
	 *            the clipboard
	 * @param actionHost
	 *            the action host
	 */
	public AddToLibraryPackAction(IActionHost actionHost) {
		super(actionHost);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		List<Object> selectedItems = (selection).toList();
		final RootModelItem<LibraryPack> libraryPacks = RootModelItem.<LibraryPack> getInstance(ModelConstants.LIBRARYPACKS_ROOT);
		Set<String> libraryPackNameAutocompleteProposals = LibraryPackModelPersistence.getLibraryPackNameAutocompleteProposals();
		libraryPackNameAutocompleteProposals.addAll(completeWithCurrentlyLoadedLibraries(libraryPacks));
		LibraryPackNamePromptDialog dialog = new LibraryPackNamePromptDialog(getActionHost().getShell(), libraryPackNameAutocompleteProposals);
		dialog.setValidator(new IFieldsValidator() {

			@SuppressWarnings("rawtypes")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				StringHolder descriptionValueHolder = (StringHolder) fieldValueHolders.get(LibraryPackNamePromptDialog.LIBRARY_PACK_DESCRIPTION_FIELD);
				StringHolder labelValueHolder = (StringHolder) fieldValueHolders.get(LibraryPackNamePromptDialog.LIBRARY_PACK_NAME_FIELD);
				if (libraryPacks.hasChild((String) labelValueHolder.getValue())) {
					if (!(descriptionValueHolder.getValue() == null || descriptionValueHolder.getValue().trim().equals(""))) {
						validationResult.append("You cannot redefine description for an existing library.");
					}
				}
				return validationResult;
			}

		});
		if (dialog.open() == Window.OK) {
			String label = dialog.getLibraryName();
			String description = dialog.getDescription();
			LibraryPack libraryPack = libraryPacks.hasChild(label) ? libraryPacks.getChild(label) : addNewLibraryPack(libraryPacks, label, description);
			for (Object selectedItem : selectedItems) {
				if (selectedItem instanceof ArtifactVersion) {
					ArtifactVersion artifactVersion = (ArtifactVersion) selectedItem;
					Artifact artifact = artifactVersion.getParent();
					Group group = artifact.getParent();
					Group newGroup = libraryPack.hasChild(group.getUID()) ? libraryPack.getChild(group.getUID()) : addNewGroup(libraryPack, group);
					Artifact newArtifact = newGroup.hasChild(artifact.getUID()) ? newGroup.getChild(artifact.getUID()) : addNewArtifact(newGroup, artifact);
					ArtifactVersion newArtifactVersion = newArtifact.hasChild(artifactVersion.getUID()) ? newArtifact.getChild(artifactVersion.getUID()) : addNewArtifactVersion(newArtifact, artifactVersion);
					LibraryPacksView.refreshViewer();
					LibraryPacksView.showViewAndFocusOnElement(newArtifactVersion);
				}
			}
			LibraryPackModelPersistence.addLibraryPackNameAutocompleteProposal(label);
			LibraryPackModelPersistence.setWorkspacePersistenceStatus(LibraryPackModelPersistence.OUT_OF_SYNC);
			LibraryPacksView.refreshViewer();
		}
	}

	private Set<String> completeWithCurrentlyLoadedLibraries(RootModelItem<LibraryPack> libraryPacks) {
		Set<String> currentlyLoaded = new HashSet<String>();
		for (LibraryPack libraryPack : libraryPacks.getChildren()) {
			currentlyLoaded.add(libraryPack.getLabel());
		}
		return currentlyLoaded;
	}

	private ArtifactVersion addNewArtifactVersion(Artifact newArtifact, ArtifactVersion artifactVersion) {
		LibraryPackArtifactVersion newArtifactVersion = new LibraryPackArtifactVersion();
		newArtifactVersion.setUrl(artifactVersion.getUrl());
		newArtifactVersion.setPomUrl(artifactVersion.getPomUrl());
		newArtifactVersion.setSourcesUrl(artifactVersion.getSourcesUrl());
		newArtifactVersion.setJavadocUrl(artifactVersion.getJavadocUrl());
		newArtifactVersion.setType(artifactVersion.getType());
		newArtifactVersion.setId(artifactVersion.getId());
		newArtifactVersion.setClassifier(artifactVersion.getClassifier());
		newArtifactVersion.setVersion(artifactVersion.getVersion());
		newArtifactVersion.setTargets(Target.BUNDLED_FOR_RUNTIME);
		newArtifact.addChild(newArtifactVersion);
		return newArtifactVersion;
	}

	private Artifact addNewArtifact(Group newGroup, Artifact artifact) {
		Artifact newArtifact = new Artifact(artifact.getId());
		newGroup.addChild(newArtifact);
		return newArtifact;
	}

	private Group addNewGroup(LibraryPack libraryPack, Group group) {
		Group newGroup = new Group(group.getName());
		libraryPack.addChild(newGroup);
		return newGroup;
	}

	private LibraryPack addNewLibraryPack(RootModelItem<LibraryPack> libraryPacks, String label, String description) {
		LibraryPack libraryPack = new LibraryPack(label, description);
		libraryPacks.addChild(libraryPack);
		return libraryPack;
	}
}
