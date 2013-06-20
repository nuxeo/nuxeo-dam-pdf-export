/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger
 */

package org.nuxeo.dam.pdf.export;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;

/**
 * PDF Export related actions.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
@Scope(CONVERSATION)
@Name("damPdfExportActions")
@Install(precedence = FRAMEWORK)
public class PDFExportActions implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected DocumentsListsManager documentsListsManager;

    @In(required = false)
    protected transient Principal currentUser;

    public String exportSelectionAsPDF() throws ClientException {
        try {
            if (documentsListsManager.isWorkingListEmpty(CURRENT_DOCUMENT_SELECTION)) {
                return null;
            }
            List<DocumentModel> docs = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);

            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            BufferedOutputStream buff = new BufferedOutputStream(
                    response.getOutputStream());

            PDFCreator creator = new PDFCreator(docs,
                    (NuxeoPrincipal) currentUser);
            if (creator.createPDF("PDF Export", buff)) {
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"PDF export.pdf\";");
                response.setContentType("application/pdf");
                response.flushBuffer();
                context.responseComplete();
            }
        } catch (Exception e) {
            throw ClientException.wrap(e);
        }
        return null;
    }

    public boolean getCanExportAsPDF() {
        List<DocumentModel> docs = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        for (DocumentModel doc : docs) {
            if (doc.hasFacet(ImagingDocumentConstants.PICTURE_FACET)) {
                return true;
            }
        }
        return false;
    }

}
