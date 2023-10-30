/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.msh.web.gui.dlg;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.property.MSHInProperty;
import si.laurentius.msh.web.gui.InMailDataView;
import si.laurentius.msh.web.pmode.AbstractPModeJSFView;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("dialogMailInEditPropertiesView")
public class DialogMailInEditPropertiesView extends AbstractPModeJSFView<MSHInProperty> {

    public static final SEDLogger LOG = new SEDLogger(DialogMailInEditPropertiesView.class);

    @Inject
    InMailDataView inMailDataView;

    public InMailDataView getInMailDataView() {
        return inMailDataView;
    }

    public void setInMailDataView(InMailDataView pmpw) {
        this.inMailDataView = pmpw;
    }

    @Override
    public boolean validateData() {
        return true;
    }

    @Override
    public void createEditable() {
        LOG.formatedlog("createEditable");
        MSHInProperty p = new MSHInProperty();
        setNew(p);
    }

    @Override
    public List<MSHInProperty> getList() {
        final MSHInMail currentMail = inMailDataView.getCurrentMail();
        if (currentMail != null) {
            return currentMail.getMSHInProperties().getMSHInProperties();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean persistEditable() {
        LOG.formatedlog("persist editable");
        boolean bsuc = false;

        MSHInProperty ecj = getEditable();

        if (ecj != null) {
            LOG.formatedlog("persist editable %s", ecj.getName());
            inMailDataView.getCurrentMail().getMSHInProperties().getMSHInProperties().stream()
                    .filter(mshInProperty -> mshInProperty.getId().equals(ecj.getId()))
                    .findFirst()
                    .ifPresent(mshInProperty -> {
                        LOG.formatedlog("found it value=%s", ecj.getValue());
                        mshInProperty.setValue(ecj.getValue());
                    });
            // inMailDataView.getCurrentMail().getMSHInProperties().getMSHInProperties().set(idx, ecj);
            bsuc = true;
        } else {
            addError("No editable property!");
        }
        return bsuc;
    }

    @Override
    public boolean removeSelected() {
        LOG.formatedlog("remove selected");
        boolean bSuc = false;
        MSHInProperty ecj = getSelected();

        if (ecj != null) {
            bSuc = inMailDataView.getCurrentMail().getMSHInProperties().getMSHInProperties().remove(ecj);
        } else {
            addError("No editable payload");
        }

        return bSuc;
    }

    @Override
    public boolean updateEditable() {
        LOG.formatedlog("updating editable");
        boolean bSuc = false;

        MSHInProperty ecj = getEditable();
        if (ecj != null) {
            LOG.formatedlog("updating editable: %s, value=%s", ecj.getName(), ecj.getValue());
            getSelected().setType(ecj.getType());
            getSelected().setValue(ecj.getValue());
            getSelected().setName(ecj.getName());

            bSuc = true;
        } else {
            addError("No editable payload!");
        }
        return bSuc;
    }

    public void selected(){
        LOG.formatedlog("getSelectedDesc");
        final MSHInProperty selected = getSelected();
        LOG.formatedlog("selected: %s=%s", selected.getName(), selected.getValue());
    }

    @Override
    public String getSelectedDesc() {
        LOG.formatedlog("getSelectedDesc");
        return getSelected() != null ? getSelected().toString() : "";
    }

}
