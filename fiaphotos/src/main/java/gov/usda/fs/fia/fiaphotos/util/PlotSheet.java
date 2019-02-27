/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.util;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author sdelucero
 */
public class PlotSheet {

    private List<LobDocPlotLink> photos = null;
    private List<List> groups = null;
    private boolean incOthers;
    private boolean landscape = false;

    public class Sorter implements Comparator<LobDocPlotLink> {

        public int compare(LobDocPlotLink a, LobDocPlotLink b) {
            return a.getLobDocument().getFileName().compareTo(a.getLobDocument().getFileName());
        }
    }

    public PlotSheet(List<LobDocPlotLink> photos, boolean incOthers, boolean landscape) {
        this.photos = new ArrayList<LobDocPlotLink>();
        this.photos.addAll(photos);
        this.incOthers = incOthers;
        this.landscape = landscape;

    }

    public boolean isSamePlot(PlotPhotoData last, PlotPhotoData next) {
        if (last == null || next == null) {
            return false;
        }
        return (last.getStateCode() == next.getStateCode()
                && last.getCountyCode() == next.getCountyCode()
                && last.getPlotCode() == next.getPlotCode()
                && last.getYear() == next.getYear()
                && last.getSubplot() == next.getSubplot());
    }

    public boolean isPrimary(PlotPhotoData ppd) {

        return !"Other".equals(ppd.getDirection());

    }

    public void prepare() throws Exception {
        Collections.sort(photos, new Sorter());
        groups = new ArrayList<List>();
        List<PlotPhotoData> group = new ArrayList<PlotPhotoData>();
        List<PlotPhotoData> others = new ArrayList<PlotPhotoData>();
        PlotPhotoData last = null;
        for (LobDocPlotLink dpl : photos) {
            PlotPhotoData ppd = new PlotPhotoData(dpl);
            if (isSamePlot(last, ppd)) {
                if (isPrimary(ppd)) {
                    group.add(ppd);
                } else {
                    others.add(ppd);
                }
            } else {
                if (!group.isEmpty()) {
                    if (!others.isEmpty() && incOthers) {
                        group.addAll(others);
                    }
                    groups.add(group);
                    group = new ArrayList<PlotPhotoData>();
                    others.clear();
                }
                if (isPrimary(ppd)) {
                    group.add(ppd);
                } else {
                    others.add(ppd);
                }

            }
            last = ppd;
        }
        if (!group.isEmpty()) {
            if (!others.isEmpty() && incOthers) {
                group.addAll(others);
            }
            groups.add(group);
        }

    }

    public ByteArrayOutputStream generate() throws Exception {

        prepare();

        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, new BaseColor(0, 0, 0));
        Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 6, Font.NORMAL, new BaseColor(0, 0, 0));
        Document document = new Document((landscape ? PageSize.LETTER.rotate() : PageSize.LETTER), 36, 36, 18, 18);// new Document();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        for (List<PlotPhotoData> group : groups) {
            PlotPhotoData ppd = group.get(0);
            document.newPage();
            int rows=0;
            addHeader(document, fontNormal, ppd);
            for (Iterator<PlotPhotoData> iterator = group.iterator(); iterator.hasNext();) {
                PlotPhotoData pi1 = iterator.next();
                PlotPhotoData pi2 = null;
                if (iterator.hasNext()) {
                    pi2 = iterator.next();
                }
                PdfPTable table = new PdfPTable((pi2 == null ? 1 : 2));
                table.setWidthPercentage((pi2 == null ? 50 : 100));
                table.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.setKeepTogether(true);
                table.addCell(createImageCell(pi1, fontSmall));
                if (pi2 != null) {
                    table.addCell(createImageCell(pi2, fontSmall));
                }
                if (! isPrimary(pi1) || rows > 3) {
                    document.newPage();
                    addHeader(document, fontNormal, ppd);
                    rows=0;
                }
                document.add(table);
                rows++;
            }

        }

        document.close(); // no need to close PDFwriter?
        return baos;
    }

    public PdfPCell createImageCell(PlotPhotoData ppd, Font font) throws Exception {
        PdfPCell cell = null;
        Image img = Image.getInstance(ppd.getContent());
        // img.scaleAbsolute(240, 180);
        float pct = (landscape ? 15.0f : 13.0f);
        img.scalePercent(pct);
        img.setAlignment(Element.ALIGN_CENTER);
        
        cell = new PdfPCell();
        cell.addElement(img);

        Paragraph pp = new Paragraph(ppd.getDirection() + ": " + ppd.getFileName(), font);
        pp.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pp);

        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        cell.setPadding(2f);
        return cell;
    }

    public void addHeader(Document document, Font font, PlotPhotoData ppd) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mma");
        addCell(document, font,Element.ALIGN_CENTER,"FIA Plot Photo Sheet");
        addCell(document, font,Element.ALIGN_CENTER,sdf.format(new Date()));
        addCell(document, font,Element.ALIGN_CENTER," ");
        PdfPTable table = new PdfPTable(4);
        PdfPCell cell = new PdfPCell(new Paragraph("", font));
        cell.setPadding(2f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.setKeepTogether(true);
        table.setWidthPercentage(100);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPhrase(new Paragraph("State", font));
        table.addCell(cell);
        cell.setPhrase(new Paragraph("County", font));
        table.addCell(cell);
        cell.setPhrase(new Paragraph("Plot", font));
        table.addCell(cell);
        cell.setPhrase(new Paragraph("Year", font));
        table.addCell(cell);

        cell.setPhrase(new Paragraph(ppd.getStateName(), font));
        table.addCell(cell);
        cell.setPhrase(new Paragraph(ppd.getCountyName(), font));
        table.addCell(cell);
        cell.setPhrase(new Paragraph(ppd.getPlotCode().toString(), font));
        table.addCell(cell);
        cell.setPhrase(new Paragraph(ppd.getYear().toString(), font));
        table.addCell(cell);

        addCell(document, font,Element.ALIGN_CENTER," ");
        
        document.add(table);
    }

    public void addCell(Document document, Font font, int align, String txt) throws Exception {
        PdfPCell cell = new PdfPCell(new Paragraph("", font));
        cell.setPadding(2f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setHorizontalAlignment(align);
        cell.setPhrase(new Paragraph(txt, font));
        table.addCell(cell);
        document.add(table);
    }

    public void addCell(Document document, Font font, String labl, String valu) throws Exception {
        PdfPCell cell1 = new PdfPCell(new Paragraph("", font));
        cell1.setPadding(2f);
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell1.setPhrase(new Paragraph(labl, font));

        PdfPCell cell2 = new PdfPCell(new Paragraph("", font));
        cell2.setPadding(2f);
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell2.setPhrase(new Paragraph(valu, font));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell1);
        table.addCell(cell2);
        document.add(table);
    }

    public void addCell(Document document, PdfPTable table, Font font, String labl, String valu) throws Exception {
        PdfPCell cell1 = new PdfPCell(new Paragraph("", font));
        cell1.setPadding(2f);
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell1.setPhrase(new Paragraph(labl, font));

        PdfPCell cell2 = new PdfPCell(new Paragraph("", font));
        cell2.setPadding(2f);
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell2.setPhrase(new Paragraph(valu, font));

        table.addCell(cell1);
        table.addCell(cell2);
    }

}
