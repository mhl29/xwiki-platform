/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.wysiwyg.client.plugin.link.ui;

import org.xwiki.gwt.user.client.FocusCommand;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;

/**
 * Wizard step to collect the data about an external link (e.g. http: or mailto:). Extends the default link
 * configuration wizard step by adding the field to collect the external link URL.
 * 
 * @version $Id$
 */
public abstract class AbstractExternalLinkWizardStep extends LinkConfigWizardStep
{
    /**
     * The text box to store the URI of the created link.
     */
    private final TextBox urlTextBox = new TextBox();

    /**
     * The label to display the url label for the created link.
     */
    private final Label urlErrorLabel = new Label();

    /**
     * The main panel of this wizard step.
     */
    private FlowPanel mainPanel;

    /**
     * Default constructor.
     */
    public AbstractExternalLinkWizardStep()
    {
        super();
        Panel urlLabel = new FlowPanel();
        urlLabel.setStyleName(INFO_LABEL_STYLE);
        urlLabel.add(new InlineLabel(getURLLabel()));
        InlineLabel mandatoryLabel = new InlineLabel(Strings.INSTANCE.mandatory());
        mandatoryLabel.addStyleName("xMandatory");
        urlLabel.add(mandatoryLabel);
        Label helpUrlLabel = new Label(getURLHelpLabel());
        helpUrlLabel.setStyleName(HELP_LABEL_STYLE);

        urlErrorLabel.addStyleName(ERROR_LABEL_STYLE);
        urlErrorLabel.setVisible(false);

        urlTextBox.setTitle(getURLTextBoxTooltip());
        urlTextBox.addKeyPressHandler(this);

        getLabelTextBox().setTitle(getLabelTextBoxTooltip());

        mainPanel = new FlowPanel();
        mainPanel.removeStyleName(DEFAULT_STYLE_NAME);
        mainPanel.addStyleName("xLinkToUrl");

        FlowPanel urlPanel = new FlowPanel();
        urlPanel.addStyleName("url");
        urlPanel.add(urlLabel);
        urlPanel.add(helpUrlLabel);
        urlPanel.add(urlErrorLabel);
        urlPanel.add(urlTextBox);

        mainPanel.add(urlPanel);
        mainPanel.add(getMainPanel());
    }

    /**
     * {@inheritDoc}
     */
    public void init(Object data, final AsyncCallback< ? > cb)
    {
        super.init(data, new AsyncCallback<Boolean>()
        {
            public void onSuccess(Boolean result)
            {
                urlTextBox.setText(getLinkData().getUrl() == null ? "" : getLinkData().getUrl());
                cb.onSuccess(null);
            }

            public void onFailure(Throwable caught)
            {
                cb.onFailure(caught);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setFocus()
    {
        // on the label error label if it's an error on label and not on url, otherwise on the url text box
        if (getLabelErrorLabel().isVisible() && !urlErrorLabel.isVisible()) {
            DeferredCommand.addCommand(new FocusCommand(getLabelTextBox()));
        } else {
            DeferredCommand.addCommand(new FocusCommand(urlTextBox));
        }
    }

    /**
     * {@inheritDoc}
     */
    public Widget display()
    {
        return mainPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean validateForm()
    {
        // validate everything: super first
        boolean result = super.validateForm();
        // then this form
        if (urlTextBox.getText().trim().length() == 0) {
            displayURLError(getURLErrorMessage());
            result = false;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveForm()
    {
        super.saveForm();
        String linkUri = buildURL();
        getLinkData().setUrl(linkUri);
        getLinkData().setReference(linkUri);
    }

    /**
     * @return the urlTextBox
     */
    public TextBox getUrlTextBox()
    {
        return urlTextBox;
    }

    /**
     * @return the label text for the particular external resource link to be created.
     */
    protected abstract String getURLLabel();

    /**
     * @return the label text for the help label for the url of the external link to be created.
     */
    protected abstract String getURLHelpLabel();

    /**
     * @return the error message to be displayed when the user uri is missing.
     */
    protected abstract String getURLErrorMessage();

    /**
     * Builds an URL to the external resource to be linked from the user input, adding protocols, parsing user input,
     * etc.
     * 
     * @return the URL to the external resource from the user input.
     */
    protected abstract String buildURL();

    /**
     * @return the tooltip for URL text box.
     */
    protected String getURLTextBoxTooltip()
    {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hideErrors()
    {
        super.hideErrors();
        // hide this dialog's specific errors
        urlErrorLabel.setVisible(false);
        urlTextBox.removeStyleName(FIELD_ERROR_STYLE);
    }

    /**
     * Displays the URL error message and markers.
     * 
     * @param errorMessage the error message to display
     */
    protected void displayURLError(String errorMessage)
    {
        urlErrorLabel.setText(errorMessage);
        urlErrorLabel.setVisible(true);
        urlTextBox.addStyleName(FIELD_ERROR_STYLE);
    }
}