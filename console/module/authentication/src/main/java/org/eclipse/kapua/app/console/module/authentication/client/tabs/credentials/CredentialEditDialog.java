/*******************************************************************************
 * Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.module.authentication.client.tabs.credentials;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.kapua.app.console.module.api.client.util.validator.ConfirmPasswordUpdateFieldValidator;
import org.eclipse.kapua.app.console.module.api.client.util.validator.PasswordUpdateFieldValidator;
import org.eclipse.kapua.app.console.module.api.shared.model.session.GwtSession;
import org.eclipse.kapua.app.console.module.authentication.shared.model.GwtCredential;
import org.eclipse.kapua.app.console.module.authentication.shared.model.GwtCredentialType;

import java.util.Date;

public class CredentialEditDialog extends CredentialAddDialog {

    private final GwtCredential selectedCredential;

    private final Label lockedUntil = new Label();

    public CredentialEditDialog(GwtSession currentSession, GwtCredential selectedCredential, String selectedUserId, String selectedUserName) {
        super(currentSession, selectedUserId, selectedUserName);
        this.selectedCredential = selectedCredential;
    }

    @Override
    public void submit() {
        // TODO read enabled and expire date
        selectedCredential.setCredentialKey(password.getValue());
        selectedCredential.setExpirationDate(expirationDate.getValue());
        selectedCredential.setCredentialStatus(credentialStatus.getValue().getValue().toString());
        selectedCredential.setOptlock(optlock.getValue().intValue());
        GWT_CREDENTIAL_SERVICE.update(xsrfToken, selectedCredential, new AsyncCallback<GwtCredential>() {

            @Override
            public void onFailure(Throwable caught) {
                unmask();

                submitButton.enable();
                cancelButton.enable();
                status.hide();

                exitStatus = false;
                exitMessage = MSGS.dialogEditError(caught.getLocalizedMessage());

                hide();
            }

            @Override
            public void onSuccess(GwtCredential result) {
                exitStatus = true;

                if (selectedCredential.getCredentialTypeEnum() == GwtCredentialType.API_KEY) {
                    exitMessage = MSGS.dialogEditConfirmationAPI();
                } else if (selectedCredential.getCredentialTypeEnum() == GwtCredentialType.PASSWORD) {
                    exitMessage = MSGS.dialogEditConfirmationPassword();
                }

                hide();
            }
        });
    }

    @Override
    public void createBody() {
        super.createBody();
        loadCredential();
    }

    private void loadCredential() {
        credentialType.setSimpleValue(selectedCredential.getCredentialTypeEnum());
        expirationDate.setValue(selectedCredential.getExpirationDate());
        credentialStatus.setSimpleValue(selectedCredential.getCredentialStatusEnum());
        optlock.setValue(selectedCredential.getOptlock());
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        credentialFormPanel.remove(credentialType);
        credentialTypeLabel.setVisible(true);
        credentialTypeLabel.setValue(selectedCredential.getCredentialType());
        password.setValidator(new PasswordUpdateFieldValidator(password));
        password.setFieldLabel(MSGS.dialogEditFieldNewPassword());
        password.setAllowBlank(true);
        password.addListener(Events.Change, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                confirmPassword.setAllowBlank(password.getValue() == null || password.getValue().equals(""));
            }
        });
        confirmPassword.setValidator(new ConfirmPasswordUpdateFieldValidator(confirmPassword, password));
        confirmPassword.setFieldLabel(MSGS.dialogEditFieldConfirmNewPassword());
        confirmPassword.setAllowBlank(true);
        if (selectedCredential.getLockoutReset() != null && selectedCredential.getLockoutReset().after(new Date())) {
            lockedUntil.setText(MSGS.dialogEditLockedUntil(selectedCredential.getLockoutResetFormatted()));
            credentialFormPanel.add(lockedUntil);
        }
    }

    @Override
    public String getHeaderMessage() {
        return MSGS.dialogEditHeader(selectedCredential.getUsername());
    }

    @Override
    public String getInfoMessage() {
        return MSGS.dialogEditInfo();
    }
}
