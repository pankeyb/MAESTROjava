/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import org.hibernate.FetchMode;
import org.hibernate.annotations.Fetch;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author sdelucero
 */
/*
@Data
@Entity
@Table(name = "NICE_PHOTOS_V", schema = "FS_NICE")
*/
public class Photo {

    @Id
    @Column(name = "DOC_CN")
    private Long Cn;

    @Column(name = "CAPTION")
    private String caption;

    @Column(name = "PHOTOGRAPHER_NAME")
    private String photographer;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "ORIGINAL_FILE_NAME")
    private String fileName;

    @Lob
    @Column(name = "CONTENT", columnDefinition = "BLOB")
    @Basic(fetch = FetchType.LAZY)
    private Blob content;

    @Transient
    private boolean selected=false;
    
    public Photo() {
        super();
    }

    public StreamedContent getBlobContent() {
        StreamedContent sc = null;
        try {
            long len = content.length();
            sc = new DefaultStreamedContent(content.getBinaryStream(), "image/jpeg");
        } catch (SQLException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sc;
    }    
    public boolean isSelected() {
        return this.selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
