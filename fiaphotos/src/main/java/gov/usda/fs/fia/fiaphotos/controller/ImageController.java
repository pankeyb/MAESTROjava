/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import gov.usda.fs.fia.fiaphotos.service.PlotPhotoService;
import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import gov.usda.fs.fia.fiaphotos.model.Photo;
import gov.usda.fs.fia.fiaphotos.util.PlotPhotoData;
import gov.usda.fs.fia.fiaphotos.util.Utils;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "imgService")
@ELBeanName(value = "imgService")
// @Join(path = "/", to = "/hello.jsf")
public class ImageController {

    @Autowired
    @Qualifier("viewPhotoService")
    private PlotPhotoService plotPhotoService;

    public StreamedContent getImage() throws IOException, SQLException {
        StreamedContent content = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            content = new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Return a real StreamedContent with the image bytes.
            String cn = context.getExternalContext().getRequestParameterMap().get("Cn");
            // Optional<Photo> oPhoto = photoRepository.findById(Long.valueOf(Cn));
            Optional<LobDocPlotLink> oPhoto = plotPhotoService.findById(cn);
            if (oPhoto.isPresent()) {
                LobDocument doc = oPhoto.get().getLobDocument();
                Blob blob = doc.getContent();
                /* 
                ImageIO.setUseCache(true);
                BufferedInputStream bis = new BufferedInputStream(blob.getBinaryStream());
                BufferedImage originalImage = ImageIO.read(bis);
                int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
                BufferedImage newImage = resizeImage(originalImage, type, originalImage.getWidth()/10, originalImage.getHeight()/10);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(newImage, "jpg", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                content = new DefaultStreamedContent(is);
                */
                content = new DefaultStreamedContent(blob.getBinaryStream(),doc.getContentType(),doc.getFileName(),new Integer(doc.getFileSize().intValue()));

            } else {
                content = new DefaultStreamedContent();
            }
        }

        return content;
    }

    public StreamedContent getUploadedImage() throws IOException {
        StreamedContent content = null;

        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            content = new DefaultStreamedContent();
        } else {
            try {
                String uuid = context.getExternalContext().getRequestParameterMap().get("uuid");
                String beanName = context.getExternalContext().getRequestParameterMap().get("beanName");

                EditImageContent imgContent = Utils.getImageContentBean(beanName);
                InputStream in = null;
                in = new ByteArrayInputStream(imgContent.getEditImageContent(uuid));
                content = new DefaultStreamedContent(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

}
