/*
 Copyright (C) 2005-2012, by the President and Fellows of Harvard College.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Dataverse Network - A web application to share, preserve and analyze research data.
 Developed at the Institute for Quantitative Social Science, Harvard University.
 Version 3.1.
 */
package edu.harvard.iq.dvn.core.web.admin;

import edu.harvard.iq.dvn.core.vdc.*;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author skraffmiller
 */
@ViewScoped
@Named("GuestBookResponseDataPage")
public class GuestBookResponseDataPage extends VDCBaseBean implements java.io.Serializable {

    @EJB
    GuestBookResponseServiceBean guestBookResponseServiceBean;
    private List<GuestBookResponse> guestBookResponses = new ArrayList();
    private List<GuestBookResponseDisplay> guestBookResponsesDisplay = new ArrayList();
    private List<GuestBookResponse> guestBookResponsesAll = new ArrayList();
    private List<String> columnHeadings = new ArrayList();
    private List<Long> customQuestionIds = new ArrayList();
    private VDC vdc;

    public void init() {
        guestBookResponsesAll = guestBookResponseServiceBean.findAll();
        vdc = getVDCRequestBean().getCurrentVDC();
        for (GuestBookResponse gbr : guestBookResponsesAll) {
            if (gbr.getStudy().getOwner().equals(vdc)) {
                guestBookResponses.add(gbr);
                if (!gbr.getCustomQuestionResponses().isEmpty()) {
                    for (CustomQuestionResponse cqr : gbr.getCustomQuestionResponses()) {
                        if (!customQuestionIds.contains(cqr.getCustomQuestion().getId())) {
                            customQuestionIds.add(cqr.getCustomQuestion().getId());
                            columnHeadings.add(cqr.getCustomQuestion().getQuestionString());
                        }
                    }
                }
            }
        }
        if (!customQuestionIds.isEmpty()) {
            for (GuestBookResponse gbr : guestBookResponses) {
                GuestBookResponseDisplay guestBookResponseDisplay = new GuestBookResponseDisplay();
                guestBookResponseDisplay.setGuestBookResponse(gbr);
                List<String> customQuestionResponseStrings = new ArrayList(customQuestionIds.size());
                for (int i=0; i<customQuestionIds.size(); i++){
                    customQuestionResponseStrings.add(i, "");
                }
                if (!gbr.getCustomQuestionResponses().isEmpty()) {
                    for (Long id : customQuestionIds) {
                        int index = customQuestionIds.indexOf(id);
                        for (CustomQuestionResponse cqr : gbr.getCustomQuestionResponses()) {
                            if (cqr.getCustomQuestion().getId().equals(id)) {
                                customQuestionResponseStrings.set(index, cqr.getResponse());
                            }
                        }
                    }
                }
                guestBookResponseDisplay.setCustomQuestionResponses(customQuestionResponseStrings);
                guestBookResponsesDisplay.add(guestBookResponseDisplay);
            }
        } else {
            for (GuestBookResponse gbr : guestBookResponses) {
                GuestBookResponseDisplay guestBookResponseDisplay = new GuestBookResponseDisplay();
                guestBookResponseDisplay.setGuestBookResponse(gbr);
                guestBookResponsesDisplay.add(guestBookResponseDisplay);
            }
            
        }
    }
    
    public List<GuestBookResponse> getGuestBookResponses() {
        return guestBookResponses;
    }

    public void setGuestBookResponses(List<GuestBookResponse> guestBookResponses) {
        this.guestBookResponses = guestBookResponses;
    }
    
    
    public List<String> getColumnHeadings() {
        return columnHeadings;
    }

    public void setColumnHeadings(List<String> columnHeadings) {
        this.columnHeadings = columnHeadings;
    }

    public List<GuestBookResponseDisplay> getGuestBookResponsesDisplay() {
        return guestBookResponsesDisplay;
    }

    public void setGuestBookResponsesDisplay(List<GuestBookResponseDisplay> guestBookResponsesDisplay) {
        this.guestBookResponsesDisplay = guestBookResponsesDisplay;
    }
    
    public String cancel_action() {
        return "/admin/OptionsPage?faces-redirect=true&vdcId=" + getVDCRequestBean().getCurrentVDC().getId();
    }
    
}