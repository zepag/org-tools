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
package org.org.eclipse.core.utils.platform.dialogs.message;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.org.eclipse.core.utils.platform.dialogs.Dialog;

/**
 * The AbstractIconAndMessageDialog is the abstract superclass of dialogs that have an icon and a message as the first two widgets. In this dialog the icon and message are direct children of the shell in order that they can be read by accessibility tools more easily.
 */
public abstract class AbstractMessageAndButtonDialog extends Dialog {
	private Image image;

	/**
	 * Return the label for the image.
	 */
	protected Label imageLabel;

	/**
	 * Message (a localized string).
	 */
	protected String message;

	/**
	 * Message label is the label the message is shown on.
	 */
	// protected Label messageLabel;
	protected FormText messageLabel;

	/**
	 * Message (a localized string).
	 */
	protected String subTitle;

	/**
	 * Constructor for AbstractIconAndMessageDialog.
	 * 
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level shell
	 */
	public AbstractMessageAndButtonDialog(Shell parentShell) {
		super(parentShell);
	}

	/*
	 * @see Dialog.createButtonBar()
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, COMPOSITE_STYLE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);

		return composite;
	}

	/*
	 * @see Dialog.createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		// initialize the dialog units
		initializeDialogUnits(parent);
		// create the top level composite for the dialog
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN) * 3 / 2;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING) * 2;
		layout.makeColumnsEqualWidth = false;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDialogAndButtonArea(parent);
		return parent;
	}

	/**
	 * Create the dialog area and the button bar for the receiver.
	 * 
	 * @param parent
	 */
	protected void createDialogAndButtonArea(Composite parent) {
		// create the dialog area and button bar
		dialogArea = createDialogArea(parent);
		buttonBar = createButtonBar(parent);
		// Apply to the parent so that the message gets it too.
	}

	/**
	 * Create the area the message will be shown in.
	 * 
	 * @param parent
	 *            The composite to parent from.
	 * @return Control
	 */
	protected Control createMessageArea(Composite parent) {

		// create composite
		Composite messageAreaContainer = new Composite(parent, SWT.NULL);
		messageAreaContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		messageAreaContainer.setLayout(new GridLayout(2, false));
		if (subTitle != null) {
			Composite titleContainer = new Composite(messageAreaContainer, SWT.BORDER);
			titleContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
			titleContainer.setBackground(JFaceColors.getBannerBackground(this.getShell().getDisplay()));
			titleContainer.setLayout(new GridLayout(1, false));
			Label nameLabel = new Label(titleContainer, SWT.NONE);
			nameLabel.setText(subTitle);
			nameLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
			nameLabel.setBackground(JFaceColors.getBannerBackground(this.getShell().getDisplay()));
			nameLabel.setForeground(JFaceColors.getBannerForeground(this.getShell().getDisplay()));
			nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		}
		// create image
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(messageAreaContainer, SWT.BORDER | SWT.FLAT);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			imageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		}
		// create message
		if (message != null) {
			ScrolledComposite scrolledComposite = new ScrolledComposite(messageAreaContainer, SWT.BORDER | SWT.FLAT | SWT.V_SCROLL | SWT.H_SCROLL);
			scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			scrolledComposite.setLayout(new GridLayout(1, false));
			FormToolkit formToolkit = new FormToolkit(scrolledComposite.getDisplay());
			messageLabel = formToolkit.createFormText(scrolledComposite, true);
			messageLabel.addHyperlinkListener(new IHyperlinkListener() {
				public void linkActivated(HyperlinkEvent e) {
					try {
						PlatformUI.getWorkbench().getBrowserSupport().createBrowser(IWorkbenchBrowserSupport.AS_VIEW, "myId", null, null).openURL( //$NON-NLS-1$
								new URL((String) e.data));
					} catch (MalformedURLException ex) {
						// forget it... we can't do anything about it...
					} catch (PartInitException ex) {
						// forget it... we don't want to do anything about it...yet ;)
					}
				}

				public void linkExited(HyperlinkEvent e) {
				}

				public void linkEntered(HyperlinkEvent e) {

				}
			});
			message = message.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			message = message.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			message = message.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			message = message.replaceAll("\n", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
			message = message.replaceAll("\f", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
			message = message.replaceAll("\r", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
			message = "<p>" + message + "</p>"; //$NON-NLS-1$ //$NON-NLS-2$
			messageLabel.setText(message, true, true);
			messageLabel.setWhitespaceNormalized(false);
			GridData data = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
			messageLabel.setLayoutData(data);
			messageLabel.setBackground(JFaceColors.getBannerBackground(scrolledComposite.getDisplay()));
			scrolledComposite.setContent(messageLabel);
			scrolledComposite.setExpandVertical(true);
			scrolledComposite.setExpandHorizontal(true);
			getShell().layout(true);
		}
		return parent;
	}

	protected Image getImage() {
		return this.image;
	}

	/**
	 * Returns the treeStyle for the message label.
	 * 
	 * @return the treeStyle for the message label
	 * 
	 * @since 3.0
	 */
	protected int getMessageTextStyle() {
		return SWT.READ_ONLY | SWT.WRAP;
	}

	protected void setImage(Image image) {
		this.image = image;
	}

}