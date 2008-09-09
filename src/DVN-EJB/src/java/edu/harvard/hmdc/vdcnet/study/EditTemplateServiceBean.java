/*
 * Dataverse Network - A web application to distribute, share and analyze quantitative data.
 * Copyright (C) 2007
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * EditStudyServiceBean.java
 *
 * Created on September 29, 2006, 1:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.harvard.hmdc.vdcnet.study;

import edu.harvard.hmdc.vdcnet.admin.NetworkRoleServiceLocal;
import edu.harvard.hmdc.vdcnet.admin.RoleServiceLocal;
import edu.harvard.hmdc.vdcnet.admin.VDCUser;
import edu.harvard.hmdc.vdcnet.dsb.DSBWrapper;
import edu.harvard.hmdc.vdcnet.gnrs.GNRSServiceLocal;
import edu.harvard.hmdc.vdcnet.index.IndexServiceLocal;
import edu.harvard.hmdc.vdcnet.mail.MailServiceLocal;
import edu.harvard.hmdc.vdcnet.vdc.ReviewState;
import edu.harvard.hmdc.vdcnet.vdc.VDC;
import edu.harvard.hmdc.vdcnet.vdc.VDCNetwork;
import edu.harvard.hmdc.vdcnet.vdc.VDCNetworkServiceLocal;
import edu.harvard.hmdc.vdcnet.study.DataFileFormatType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.context.FacesContext;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 *
 * @author Ellen Kraffmiller
 */
@Stateful
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EditTemplateServiceBean implements edu.harvard.hmdc.vdcnet.study.EditTemplateService, java.io.Serializable {
    @EJB VDCNetworkServiceLocal vdcNetworkService;
  
    @PersistenceContext(type = PersistenceContextType.EXTENDED,unitName="VDCNet-ejbPU")
    EntityManager em;
    Template template;
    private boolean newTemplate=false;
    private Long createdFromStudyId;
  
    
    /**
     *  Initialize the bean with a Template for editing
     */
    public void setTemplate(Long id ) {
        template = em.find(Template.class,id);
        if (template==null) {
            throw new IllegalArgumentException("Unknown template id: "+id);
        }
        
      
   
    }
    
    public void newTemplate(Long vdcId ) {
        template = new Template();
        newTemplate=true;   
        em.persist(template);
        VDC vdc = em.find(VDC.class, vdcId);      
        template.setVdc(vdc);
        vdc.getTemplates().add(template);
      //  addFields(vdcId);
        
    }
    
    public void addFields(Long vdcId) {
          VDC vdc = em.find(VDC.class, vdcId);      
           // Copy template fields from default template
        // When the template form adds ability to edit "recommended/required",
        // then we won't need this (unless we want to use the default)
        Template defTemplate = vdc.getDefaultTemplate();
     //   template.setTemplateFields(new ArrayList<TemplateField>());
        for ( TemplateField templateField: defTemplate.getTemplateFields()) {
            TemplateField tf = new TemplateField(templateField.getFieldInputLevel());
            tf.setStudyField(templateField.getStudyField());
            tf.setTemplate(template);
            template.getTemplateFields().add(tf);
        }
    }
    private Template createTemplate(Long vdcId) {
        Template createdTemplate = new Template();
        newTemplate=true;        
        VDC vdc = em.find(VDC.class, vdcId);      
        createdTemplate.setVdc(vdc);
        vdc.getTemplates().add(createdTemplate);
        // Copy template fields from default template
        // When the template form adds ability to edit "recommended/required",
        // then we won't need this (unless we want to use the default)
        Template defTemplate = vdc.getDefaultTemplate();
        createdTemplate.setTemplateFields(new ArrayList<TemplateField>());
        for ( TemplateField templateField: defTemplate.getTemplateFields()) {
            TemplateField tf = new TemplateField(templateField.getFieldInputLevel());
            tf.setStudyField(templateField.getStudyField());
            tf.setTemplate(createdTemplate);
            createdTemplate.getTemplateFields().add(tf);
        }
        // Also copy default file categories
        createdTemplate.setTemplateFileCategories(new ArrayList<TemplateFileCategory>());
        for(TemplateFileCategory templateFileCategory: defTemplate.getTemplateFileCategories()) {
            TemplateFileCategory tfc = new TemplateFileCategory();
            tfc.setName(templateFileCategory.getName());
            tfc.setDisplayOrder(templateFileCategory.getDisplayOrder());
            tfc.setTemplate(templateFileCategory.getTemplate());
        }
        return createdTemplate;
    }
    public void  newTemplate(Long vdcId, Long studyId) {
        template = createTemplate(vdcId);       
        Study study = em.find(Study.class, studyId);
        study.getMetadata().copyMetadata(template.getMetadata());
      
        createdFromStudyId=studyId;
        em.persist(template);
    }
    
    public void removeCollectionElement(Collection coll, Object elem) {
        coll.remove(elem);
        em.remove(elem);
    }
    
    public void removeCollectionElement(Iterator iter, Object elem) {
        iter.remove();
        em.remove(elem);
    }
    
    public  Template getTemplate() {
        return template;
    }
    
    
    @Remove
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void deleteStudy() {
       em.remove(template);
        
    }
    
    private HashMap studyMap;
    public HashMap getStudyMap() {
        return studyMap;
    }
    
    public void setStudyMap(HashMap studyMap) {
        this.studyMap=studyMap;
    }
    
    @Remove
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void save() {
      // Don't need to do anything specific, just commit the transaction 
            
      
    }
    
    
    /**
     * Remove this Stateful Session bean from the EJB Container without
     * saving updates to the database.
     */
    @Remove
    public void cancel() {
        
    }
    
  
   
    
    /**
     * Creates a new instance of EditStudyServiceBean
     */
    public EditTemplateServiceBean() {
    }
    
 
    
    
   
    
 
    
    public boolean isNewTemplate() {
        return newTemplate;
    }
    
    public void setCreatedFromStudy(Long createdFromStudyId) {
        this.createdFromStudyId=createdFromStudyId;
    }
    
    public Long getCreatedFromStudyId() {
        return createdFromStudyId;
    }
   
    
}

