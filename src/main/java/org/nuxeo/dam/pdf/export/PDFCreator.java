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

import java.io.OutputStream;
import java.util.List;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureResourceAdapter;
import org.nuxeo.ecm.platform.ui.web.tag.fn.Functions;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Creates a PDF from a bunch of Pictures
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
public class PDFCreator {

    private static Font font = new Font(Font.HELVETICA, 18, Font.BOLD);

    private static final String PICTURE_SCHEMA = "picture";

    private static final String ORIGINAL_JPEG_VIEW = "OriginalJpeg";

    protected List<DocumentModel> docs;

    protected NuxeoPrincipal currentUser;

    public PDFCreator(List<DocumentModel> docs, NuxeoPrincipal currentUser) {
        this.docs = docs;
        this.currentUser = currentUser;
    }

    public boolean createPDF(String title, OutputStream out)
            throws ClientException {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.addTitle(title);
            document.addAuthor(Functions.principalFullName(currentUser));
            document.addCreator(Functions.principalFullName(currentUser));

            document.open();

            document.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n"));
            Font titleFont = new Font(Font.HELVETICA, 36, Font.BOLD);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(titleParagraph);
            Font authorFont = new Font(Font.HELVETICA, 20);
            Paragraph authorParagraph = new Paragraph("By "
                    + Functions.principalFullName(currentUser), authorFont);
            authorParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(authorParagraph);

            document.newPage();

            boolean foundOnePicture = false;
            for (DocumentModel doc : docs) {
                if (!doc.hasSchema(PICTURE_SCHEMA)) {
                    continue;
                }
                foundOnePicture = true;

                PictureResourceAdapter picture = doc.getAdapter(PictureResourceAdapter.class);
                Blob blob = picture.getPictureFromTitle(ORIGINAL_JPEG_VIEW);
                Rectangle pageSize = document.getPageSize();
                if (blob != null) {
                    Paragraph imageTitle = new Paragraph(doc.getTitle(),
                            font);
                    imageTitle.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(imageTitle);

                    Image image = Image.getInstance(blob.getByteArray());
                    image.scaleToFit(pageSize.getWidth() - 20,
                            pageSize.getHeight() - 100);
                    image.setAlignment(Image.MIDDLE);
                    Paragraph imageParagraph = new Paragraph();
                    imageParagraph.add(image);
                    imageParagraph.setAlignment(Paragraph.ALIGN_MIDDLE);
                    document.add(imageParagraph);

                    document.newPage();
                }
            }
            if (foundOnePicture) {
                document.close();
                return true;
            }
        } catch (Exception e) {
            throw ClientException.wrap(e);
        }
        return false;
    }

}
