////////////////////////////////////////////////////////////////////////////////////////////////////
// PdfDocx.java
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////
package net.pdfix.docx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import net.pdfix.pdfixlib.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import java.math.BigInteger;
import java.util.List;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Highlight;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.PPrBase.TextAlignment;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.docx4j.wml.Text;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblPrBase.TblStyle;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcMar;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Tr;
import utils.PdfixSettings;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Class PdfDocx
////////////////////////////////////////////////////////////////////////////////////////////////////
public class PdfDocx {
  private PdfDoc doc = null;
  private PdfPage page = null;
  private PdfPageView pageView = null;
  private final int kTextFlagLink = 0x200;
  private int imgCounter = 0;
  private WordprocessingMLPackage wordMLPackage;
  private ObjectFactory factory;

  public PdfDocx(PdfDoc doc) {
    this.doc = doc;
  }

  public String Color2HexStr(PdfRGB color) {
    String str = "#";
    if (color == null) {
      str += "000000";
    } else {
      str += String.format("%02X", (0xFF & color.r));
      str += String.format("%02X", (0xFF & color.g));
      str += String.format("%02X", (0xFF & color.b));      
    }
    return str;
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdfDoc
////////////////////////////////////////////////////////////////////////////////////////////////////
  public void CreateDocx(String path) throws IOException, Exception {
    wordMLPackage = WordprocessingMLPackage.createPackage();
    MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

    factory = Context.getWmlObjectFactory();

    //process pages
    int numPages = doc.GetNumPages();
    for (int i = 0; i < numPages; i++) {
      page = doc.AcquirePage(i);
      GetPageDocx(page);
      doc.ReleasePage(page);
      if (i == 9) break;      
    }

    // Save
    wordMLPackage.save(new java.io.File(path));
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdfPage
////////////////////////////////////////////////////////////////////////////////////////////////////
  private void SetPageMargins() throws Docx4JException {
    Body body = wordMLPackage.getMainDocumentPart().getContents().getBody();
    PageDimensions page_dim = new PageDimensions();
    PgMar pgMar = page_dim.getPgMar();
    pgMar.setBottom(BigInteger.valueOf(1440 * 50 / 72));
    pgMar.setTop(BigInteger.valueOf(1440 * 50 / 72));
    pgMar.setLeft(BigInteger.valueOf(1440 * 50 / 72));
    pgMar.setRight(BigInteger.valueOf(1440 * 50 / 72));
    SectPr sectPr;
    sectPr = factory.createSectPr();
    body.setSectPr(sectPr);
    sectPr.setPgMar(pgMar);
  }

  public void GetPageDocx(PdfPage page) throws Exception {
    PdfRect bbox = page.GetCropBox();
    double zoom = 1;

    pageView = page.AcquirePageView(zoom, PdfRotate.kRotate0);
    if (pageView == null) {
      throw new Exception("Error creating page view");
    }

    PdfPageRenderParams renderParams = new PdfPageRenderParams();
    pageView.DrawPage(renderParams);

    //process elements
    PdfPageMapParams params = new PdfPageMapParams();
    PdePageMap pageMap = page.AcquirePageMap(params);
    int numElems = pageMap.GetNumElements();
    for (int i = 0; i < numElems; i++) {
      PdeElement elem = pageMap.GetElement(i);
      if (elem == null) {
        continue;
      }
      GetElementDocx(elem, null, null);
    }

    page.ReleasePageView(pageView);
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeElement
////////////////////////////////////////////////////////////////////////////////////////////////////
  public void GetElementDocx(PdeElement elem, PdeElement parent, 
    List<Object> parent_list) throws Exception {
    if (elem == null) {
      throw new Exception("Element is null");
    }

    PdfElementType elemType = elem.GetType();
    if (null != elemType) {
      switch (elemType) {
        case kPdeText:
          GetTextDocx((PdeText) elem, null, parent_list);
          break;
        case kPdeTable:
          wordMLPackage.getMainDocumentPart().addParagraphOfText("");
          GetTableDocx((PdeTable) elem, null, parent_list);
          break;
        case kPdeRect:
          wordMLPackage.getMainDocumentPart().addParagraphOfText("");
          GetRectDocx((PdeRect) elem, null, parent_list);
          break;
        case kPdeImage:
          wordMLPackage.getMainDocumentPart().addParagraphOfText("");
          GetImageDocx((PdeImage) elem, null, parent_list);
          break;
        case kPdeFormField:
          //GetFormFieldDocx((PdeFormField)elem, null);
          break;
        default:
          break;
      }
    }
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeImage
////////////////////////////////////////////////////////////////////////////////////////////////////
  private static byte[] ConvertImageToByteArray(File file)
    throws FileNotFoundException, IOException {
    InputStream is = new FileInputStream(file);
    long length = file.length();
    // You cannot create an array using a long, it needs to be an int.
    if (length > Integer.MAX_VALUE) {
      System.out.println("File too large!!");
    }
    byte[] bytes = new byte[(int) length];
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }
    // Ensure all the bytes have been read
    if (offset < bytes.length) {
      System.out.println("Could not completely read file "
        + file.getName());
    }
    is.close();
    return bytes;
  }

  private Inline CreateInlineImage(File file) throws Exception {
    byte[] bytes = ConvertImageToByteArray(file);
    BinaryPartAbstractImage imagePart
      = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
    int docPrId = 0;
    int cNvPrId = 1;
    return imagePart.createImageInline("Filename hint",
      "Alternative text", docPrId, cNvPrId, false);
  }

  private P AddInlineImageToParagraph(Inline inline) {
    // Now add the in-line image to a paragraph
    P paragraph = factory.createP();
    R run = factory.createR();
    paragraph.getContent().add(run);
    Drawing drawing = factory.createDrawing();
    run.getContent().add(drawing);
    drawing.getAnchorOrInline().add(inline);
    return paragraph;
  }

  public void GetImageDocx(PdeImage image, PdeElement parent, 
    List<Object> parent_list) throws Exception {
    PdfRect elem_rect = image.GetBBox();
    PdfDevRect elem_dev_rect = pageView.RectToDevice(elem_rect);
    int elem_width = elem_dev_rect.right - elem_dev_rect.left;
    int elem_height = elem_dev_rect.bottom - elem_dev_rect.top;
    if (elem_height == 0 || elem_width == 0) {
      return;
    }

    imgCounter++;
    // save image
    String image_name = "img" + imgCounter + ".png";
    //String bg_image_path = settings.htmlPath + settings.imgDir + bg_image;
    //File imgDir = new File(settings.htmlPath + settings.imgDir);
    String image_path = PdfixSettings.TMP_DIR + File.separator + image_name;
    File image_dir = new File(PdfixSettings.TMP_DIR + File.separator);
    image_dir.mkdirs();
    image.Save(image_path, PdfImageFormat.kImageFormatPng, pageView);

    File file = new File(image_path);
    if (!file.canRead())
      return;
    if (!file.exists())
      return;
    byte[] bytes = ConvertImageToByteArray(file);
    
    // Create inline image
    Inline inline = CreateInlineImage(file);
    // Add the in-line image to a paragraph
    P paragraph = AddInlineImageToParagraph(inline);

    if (parent_list == null) {
      wordMLPackage.getMainDocumentPart().addObject(paragraph);
    } else {
      parent_list.add(paragraph);
    }

    // process child elements - texts and form fields in the image
    /*int children_count = elem.GetNumChildren();
    for (int i = 0; i < children_count; i++) {
      PdeElement child = elem.GetChild(i);
      if (child.GetType() == PdfElementType.kElementText) {
        PdeText text = (PdeText) child;
        GetTextDocx(text, parent));
      } else if (child.GetType() == PdfElementType.kElementFormField) {
        PdeFormField form_field = (PdeFormField) child;
        GetFormFieldDocx(form_field, parent));
      }
    }*/
    
    file.delete();
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeText
////////////////////////////////////////////////////////////////////////////////////////////////////
  private int GetCharAnnotFlag(PdfRect char_bbox) {
    int flags = 0;
    // get annotations over the bbox
    int num_annots = page.GetNumAnnots();
    for (int i = 0; i < num_annots; i++) {
      PdfAnnot annot = page.GetAnnot(i);
      boolean char_has_annot = annot.RectInAnnot(char_bbox);
      if (char_has_annot) {
        PdfAnnotSubtype subtype = annot.GetSubtype();
        if (null != subtype) // support any type of annotation you want
        {
          switch (subtype) {
            case kAnnotLink:
              flags |= kTextFlagLink;
              PdfLinkAnnot link_annot = (PdfLinkAnnot) annot;
              PdfAction action = link_annot.GetAction();
              if (action != null) {
                String link = action.GetURI();
              }
              break;
            case kAnnotHighlight:
              flags |= Pdfix.kTextFlagHighlight;
              break;
            case kAnnotUnderline:
            case kAnnotSquiggly:
              flags |= Pdfix.kTextFlagUnderline;
              break;
            case kAnnotStrikeOut:
              flags |= Pdfix.kTextFlagStrikeout;
              break;
            default:
              break;
          }
        }
      }
    }
    return flags;
  }

  private void SetRunStyle(PdfTextState ts, RPr runProperties) {
    // we want to change (or remove) almost all the run properties of the
    // normal style, so we create a new one.
    String font_name;
    String sys_font_name;
    String font_family = "sans-serif";
    PdfFontCharset charset = PdfFontCharset.kFontUnknownCharset;
    PdfFont font = ts.font;
    PdfFontState fs = font.GetFontState();
    boolean bold = fs.bold == 1;
    boolean italic = fs.italic == 1;
    sys_font_name = font.GetSystemFontName();
    if (sys_font_name.length() > 0) {
      charset = font.GetSystemFontCharset();
    }

    // change font name
    font_name = font.GetFontName();
    if (sys_font_name.length() > 0) {
      RFonts runFont = new RFonts();
      runFont.setAscii(sys_font_name);
      runFont.setHAnsi(sys_font_name);
      runFont.setAscii(font_family);
      runProperties.setRFonts(runFont);
    }
    // change font size
    HpsMeasure size = new HpsMeasure();
    size.setVal(BigInteger.valueOf((int) ts.font_size * 2));
    runProperties.setSz(size);
    runProperties.setSzCs(size);

    // change bold&italic style
    if (bold) {
      runProperties.setB(new BooleanDefaultTrue());
    }
    if (italic) {
      runProperties.setI(new BooleanDefaultTrue());
    }

    org.docx4j.wml.Color color = factory.createColor();
    color.setVal(Color2HexStr(ts.color_state.fill_color));
    runProperties.setColor(color);

    // Adds a single underline to the run properties.
    if ((ts.flags & Pdfix.kTextFlagUnderline) != 0) {
      U underline = new U();
      underline.setVal(UnderlineEnumeration.SINGLE);
      runProperties.setU(underline);
    }
    if ((ts.flags & Pdfix.kTextFlagStrikeout) != 0) {
      runProperties.setStrike(new BooleanDefaultTrue());
    }
    if ((ts.flags & Pdfix.kTextFlagHighlight) != 0) {
      Highlight highlight = factory.createHighlight();
      runProperties.setHighlight(highlight);
    }
  }

  public void GetTextDocx(PdeText text, PdeElement parent, List<Object> parent_list) {
    if (text == null) {
      return;
    }
    PdfRect bbox = text.GetBBox();
    PdfDevRect rect = pageView.RectToDevice(bbox);
    if (rect.right == rect.left || rect.top == rect.bottom) {
      return;
    }
    double width = rect.right - rect.left;
    double height = rect.bottom - rect.top;
    // text indent
    double indent = text.GetIndent();
    // tab element
    //run.Tab tab = run.createRTab();
    // to add tab to a run
    //run.getContent().add(tab);
    // text alignment
    PdfTextAlignment alignment = text.GetAlignment();
    boolean join_lines = true;
    /* boolean join_lines = 
      (alignment == kAlignmentLeftReflow) ||
      (alignment == kAlignmentRightReflow) ||
      (alignment == kAlignmentJustify); */

    PdfTextState ts = text.GetTextState();

    // paragraph element / object
    P paragraph = factory.createP();
    PPr paragraphProperties = factory.createPPr();
    // creating the alignment
    TextAlignment align = new TextAlignment();
    align.setVal("center");
    paragraphProperties.setTextAlignment(align);
    // spacing
    //Spacing spacing = new Spacing();
    // todo - set depends on font size
    //spacing.setAfter(BigInteger.valueOf((int) ts.font_size));
    //spacing.setBefore(BigInteger.ZERO);
    //paragraphProperties.setSpacing(spacing);
    // set paragraph properties
    paragraph.setPPr(paragraphProperties);

    // run object - number of runs may comprise a single paragraph element
    R run = null;
    // process text characters
    int num_lines = text.GetNumTextLines();
    for (int l = 0; l < num_lines; l++) {
      PdeTextLine line = text.GetTextLine(l);
      if (line == null) {
        continue; //throw error
      }
      int num_words = line.GetNumWords();
      for (int w = 0; w < num_words; w++) {
        PdeWord word = line.GetWord(w);
        if (word == null) {
          continue; //throw error;
        }
        int length = word.GetNumChars();
        for (int i = 0; i < length; i++) {
          PdfTextState char_ts = word.GetCharTextState(i);
          PdfRect char_bbox;
          char_bbox = word.GetCharBBox(i);
          char_ts.flags |= GetCharAnnotFlag(char_bbox);
          String char_str = word.GetCharText(i);
          if ((run == null) || !ts.equals(char_ts)) {
            ts = char_ts;
            run = factory.createR();
            RPr rpr = new RPr();
            SetRunStyle(ts, rpr);
            run.setRPr(rpr);
            paragraph.getContent().add(run);
          }

          // Create the text element
          Text t = factory.createText();
          t.setValue(char_str);
          run.getContent().add(t);
        }
        // add whitespace between words
        if (w != num_words - 1) {
          Text t = factory.createText();
          t.setValue(" ");
          t.setSpace("preserve");
          run.getContent().add(t);
        }
      }
      // add whitespace between lines
      if (l != num_lines - 1) {
        Text t = factory.createText();
        t.setValue(" ");
        t.setSpace("preserve");
        run.getContent().add(t);
      }
      if (!join_lines) {
        // this Br element is used break the current and go for next line
        Br br = factory.createBr();
        paragraph.getContent().add(br);
      }
    }
    if (parent_list == null) {
      wordMLPackage.getMainDocumentPart().addObject(paragraph);
    } else {
      parent_list.add(paragraph);
    }
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeTable
////////////////////////////////////////////////////////////////////////////////////////////////////
  private void SetCellWidth(Tc tableCell, int width) {
    if (width > 0) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      TblWidth tableWidth = new TblWidth();
      tableWidth.setType("dxa");
      tableWidth.setW(BigInteger.valueOf(width));
      tableCellProperties.setTcW(tableWidth);
    }
  }

  private void SetCellVMerge(Tc tableCell, String mergeVal) {
    if (mergeVal != null) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      VMerge merge = new VMerge();
      if (!"close".equals(mergeVal)) {
        merge.setVal(mergeVal);
      }
      tableCellProperties.setVMerge(merge);
    }
  }

  private void SetCellHMerge(Tc tableCell, int horizontalMergedCells) {
    if (horizontalMergedCells > 1) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      GridSpan gridSpan = new GridSpan();
      gridSpan.setVal(new BigInteger(String.valueOf(horizontalMergedCells)));
      tableCellProperties.setGridSpan(gridSpan);
      tableCell.setTcPr(tableCellProperties);
    }
  }
  
  private void SetCellMargins(Tc tableCell, int top, int right, int bottom, int left) {
    TcPr tableCellProperties = tableCell.getTcPr();
    if (tableCellProperties == null) {
      tableCellProperties = new TcPr();
      tableCell.setTcPr(tableCellProperties);
    }
    TcMar margins = new TcMar();
    if (bottom > 0) {
      TblWidth bW = new TblWidth();
      bW.setType("dxa");
      bW.setW(BigInteger.valueOf(bottom));
      margins.setBottom(bW);
    }
    if (top > 0) {
      TblWidth tW = new TblWidth();
      tW.setType("dxa");
      tW.setW(BigInteger.valueOf(top));
      margins.setTop(tW);
    }
    if (left > 0) {
      TblWidth lW = new TblWidth();
      lW.setType("dxa");
      lW.setW(BigInteger.valueOf(left));
      margins.setLeft(lW);
    }
    if (right > 0) {
      TblWidth rW = new TblWidth();
      rW.setType("dxa");
      rW.setW(BigInteger.valueOf(right));
      margins.setRight(rW);
    }
    tableCellProperties.setTcMar(margins);
  }

  private void SetCellColor(Tc tableCell, String color) {
    if (color != null) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      CTShd shd = new CTShd();
      shd.setFill(color);
      tableCellProperties.setShd(shd);
    }
  }

  private void SetVerticalAlignment(Tc tableCell, STVerticalJc align) {
    if (align != null) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      CTVerticalJc valign = new CTVerticalJc();
      valign.setVal(align);
      tableCellProperties.setVAlign(valign);
    }
  }

  private void AddTableCell(Tr tableRow, PdeCell cell,
    int horizontalMergedCells, String verticalMergedVal) throws Exception {
    Tc tableCell = factory.createTc();
    SetCellVMerge(tableCell, verticalMergedVal);
    SetCellHMerge(tableCell, horizontalMergedCells);
    // process cell's children
    int children_count = cell.GetNumChildren();
    if (children_count > 0) {
      for (int i = 0; i < children_count; i++) {
        PdeElement child = cell.GetChild(i);
        if (child == null) {
          continue;
        }
        GetElementDocx(child, null, tableCell.getContent());
      }
    }
    if (tableCell.getContent().isEmpty()) {
      tableCell.getContent().add(
        wordMLPackage.getMainDocumentPart().createParagraphOfText(""));
    }
    tableRow.getContent().add(tableCell);
  }

  public void GetTableDocx(PdeTable table, PdeElement parent, 
    List<Object> parent_list) throws Exception {
    PdfGraphicState gstate = table.GetGraphicState();
    if (gstate.color_state.fill_type != PdfFillType.kFillTypeNone) {
      //String fill_color_str = Color2HexStr(gstate.color_state.fill_color);
    }
    if (gstate.color_state.stroke_type != PdfFillType.kFillTypeNone) {
      //String stroke_color_str = Color2HexStr(gstate.color_state.stroke_color);
    }

    Tbl tbl = factory.createTbl();
    TblPr tblPr = new TblPr();
    TblStyle tblStyle = new TblStyle();
    tblStyle.setVal("TableGrid");
    tblPr.setTblStyle(tblStyle);
    tbl.setTblPr(tblPr);
    
    int numRows = table.GetNumRows();
    int numCols = table.GetNumCols();
    for (int row = 0; row < numRows; row++) {
      Tr tableRow = factory.createTr();
      for (int coll = 0; coll < numCols; coll++) {
        PdeCell cell = table.GetCell(row, coll);
        if (cell == null) {
          continue;
        }
        int row_span = cell.GetRowSpan();
        int col_span = cell.GetColSpan();
        if (col_span > 0) {
          if (row_span > 1) {
            AddTableCell(tableRow, cell, col_span, "restart");
          } else {
            AddTableCell(tableRow, cell, col_span, null);
          }
        }
      }
      tbl.getContent().add(tableRow);
    }

    if (parent_list == null) {
      wordMLPackage.getMainDocumentPart().addObject(tbl);
    } else {
      parent_list.add(tbl);
    } 
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeRect
////////////////////////////////////////////////////////////////////////////////////////////////////
  public void GetRectDocx(PdeRect rect, PdeElement parent, 
    List<Object> parent_list) throws Exception {
    PdfGraphicState gstate = rect.GetGraphicState();
    String fill_color_str = null;
    if (gstate.color_state.fill_type != PdfFillType.kFillTypeNone) {
      fill_color_str = Color2HexStr(gstate.color_state.fill_color);
    }
    String stroke_color_str = null;
    if (gstate.color_state.stroke_type != PdfFillType.kFillTypeNone) {
      stroke_color_str = Color2HexStr(gstate.color_state.stroke_color);
    }

    int numChilds = rect.GetNumChildren();
    if (numChilds == 0)
      return;
    // if there is only image - add image
    if (numChilds == 1) {
      PdeElement child = rect.GetChild(0);
      PdfElementType childType = child.GetType();          
      if (childType == PdfElementType.kPdeImage) {
        GetImageDocx((PdeImage) child, null, parent_list);
        return;
      }
    }
    
    Tbl tbl = factory.createTbl();
    TblPr tblPr = new TblPr();
    TblStyle tblStyle = new TblStyle();
    tblStyle.setVal("TableGrid");
    tblPr.setTblStyle(tblStyle);
    tbl.setTblPr(tblPr);

    if (stroke_color_str != null) {
      tbl.setTblPr(new TblPr());
      CTBorder border = new CTBorder();
      border.setColor(stroke_color_str);
      border.setSz(new BigInteger("4"));
      border.setSpace(new BigInteger("0"));
      border.setVal(STBorder.SINGLE);

      TblBorders borders = new TblBorders();
      borders.setBottom(border);
      borders.setLeft(border);
      borders.setRight(border);
      borders.setTop(border);
      borders.setInsideH(border);
      borders.setInsideV(border);
      tbl.getTblPr().setTblBorders(borders);
    }

    Tr tableRow = factory.createTr();
    Tc tableCell = factory.createTc();
    SetCellVMerge(tableCell, null);
    SetCellHMerge(tableCell, 1);

    SetCellMargins(tableCell, 0, 0, 0, 0);

    if (fill_color_str != null) {
      SetCellColor(tableCell, fill_color_str);
    }

    //SetVerticalAlignment(tableCell, style.getVerticalAlignment());        
    for (int i = 0; i < numChilds; i++) {
      PdeElement child = rect.GetChild(i);
      GetElementDocx(child, null, tableCell.getContent());
    }

    if (tableCell.getContent().isEmpty()) {
      tableCell.getContent().add(
        wordMLPackage.getMainDocumentPart().createParagraphOfText("Error"));
    }

    tableRow.getContent().add(tableCell);
    tbl.getContent().add(tableRow);

    if (parent_list == null) {
      wordMLPackage.getMainDocumentPart().addObject(tbl);
    } else {
      parent_list.add(tbl);
    } 
  }
}
