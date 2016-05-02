////////////////////////////////////////////////////////////////////////////////////////////////////
// PdfHtmlDoc.java
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////
package net.pdfix.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.pdfix.pdfixlib.*;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Class PdfHtmlDoc
////////////////////////////////////////////////////////////////////////////////////////////////////
public class PdfHtmlDoc {

  private PdfDoc doc = null;
  private PdfPage page = null;
  private int clsCounter = 0;
  private int imgCounter = 0;
  private final int kTextFlagLink = 0x200;

  public PdfHtmlDoc(PdfDoc doc) {
    this.doc = doc;
  }

  private String GetNewHtmlClassName() {
    String cls = "" + (clsCounter++);
    return cls;
  }

  public String Color2Str(PdfRGB color) {
    if (color != null) {
      return "rgb(" + color.r + ", " + color.g + ", " + color.b + ")";
    } else {
      return "rgb(0,0,0)";
    }
  }

  public void CreateHtml(PdfHtmlSettings settings) throws IOException, Exception {
    if (doc == null) {
      return;
    }

    PdfHtmlData data = new PdfHtmlData();

    //html head
    data.html += "<!DOCTYPE html> \n";
    data.html += "<head>\n";
    data.html += "<meta charset=\"UTF-8\"/>\n";
    data.html += "<title>" + doc.GetInfo("Title") + "</title>\n";
    data.html += "<link rel=\"stylesheet\" type=\"text/css\" href=\""
            + settings.rootPath + "style.css\"/>\n";
    if (!settings.responsiveLayout) {
      data.html += "<link rel=\"stylesheet\" type=\"text/css\" href=\""
              + settings.rootPath + "global_fixed.css\"/>\n";
    } else {
      data.html += "<link rel=\"stylesheet\" type=\"text/css\" href=\""
              + settings.rootPath + "global_responsive.css\"/>\n";
    }
    data.html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>\n";
    data.html += "<script type=\"text/javascript\" src=\"" + settings.rootPath
            + "jquery.min.js\"></script>\n";
    data.html += "<script type=\"text/javascript\" src=\"" + settings.rootPath
            + "content.js\"></script>\n";
    data.html += "<script type=\"text/javascript\" src=\"" + settings.rootPath
            + "event.js\"></script>\n";

    if (settings.acroForm) {
      data.html += "<script type=\"text/javascript\" src=\"" + settings.rootPath + "afscript.js\"></script>\n";
      data.html += "<script type=\"text/javascript\" src=\"" + settings.rootPath + "api.js\"></script>\n";
    }
    data.html += "<script type=\"text/javascript\" src=\"doc.js\"></script>\n";
    data.html += "</head>\n";

    //html body
    data.html += "<body onpageshow=\"doc_did_load();\" onload=\"doc_did_load();\">\n";
    data.html += "<jsp:include page=\"" + settings.rootPath + "toolbar.jsp\" />";
    data.html += "<form id=\"pdf-form\">\n";
    data.html += "<div class=\"pdf-document\" id=\"pdf-document\" ";
    //data.html += "style=\"max-width:" + settings.targetWidth +"px\" ";
    data.html += "layout=\"";
    if (settings.responsiveLayout) {
      data.html += "responsive";
    } else {
      data.html += "fixed";
    }
    data.html += "\"";
    data.html += ">\n";

    // acroform - todo
    if (settings.acroForm) {
      for (int i = 0; i < doc.GetNumDocumentJavaScripts(); i++) {
        data.js += doc.GetDocumentJavaScript(i).trim();
      }

      // store all document field names
      int fieldCount = doc.GetNumFormFields();
      for (int i = 0; i < fieldCount; i++) {
        PdfFormField field = doc.GetFormField(i);
        String fieldName = field.GetFullName();
        data.js += "all_fields.push([\"" + fieldName + "\", "
                + field.getId() + ", []]);\n";
      }
      data.js += "\n\n";

      // push all calculated fields into calc_fields array
      int calcFieldCount = doc.GetNumCalculatedFormFields();
      for (int i = 0; i < calcFieldCount; i++) {
        PdfFormField field = doc.GetCalculatedFormField(i);
        String fieldName = field.GetFullName();
        data.js += "calc_fields.push(\"" + fieldName + "\");\n";
        //get the calculation script
        String calcJS = "";
        PdfAction actionCalc = field.GetAAction(PdfActionEventType.kActionEventFieldCalculate);
        if (actionCalc != null) {
          calcJS = actionCalc.GetJavaScript().trim();
        }
        if (calcJS.length() != 0) {
          data.js += "function C" + field.getId() + "() {\n";
          data.js += calcJS;
          data.js += "\n}\n\n";
        }
      }
    }

    //process pages
    int numPages = doc.GetNumPages();
    for (int i = 0; i < numPages; i++) {
      page = doc.AcquirePage(i);
      data.Append(GetPageHtml(page, settings));
      doc.ReleasePage(page);
      page = null;
      if (i == 9) break;
      if (!settings.responsiveLayout && i < numPages - 1) {
        data.html += "<br><br>";
      }
    }

    data.html += "</div>\n"; // pdf-document
//    data.html += "</form>\n";
    data.html += "</body>\n</html>";

    FlushData(settings, data);
  }

  private void FlushData(PdfHtmlSettings settings, PdfHtmlData data) throws IOException {
    //save data        
    SaveStringToFile(data.html, settings.htmlPath + File.separator + "index.html");
    SaveStringToFile(data.css, settings.htmlPath + File.separator + "style.css");
    SaveStringToFile(data.js, settings.htmlPath + File.separator + "doc.js");
    data.Clear();
  }

  private void SaveStringToFile(String text, String filePath) throws IOException {
    FileOutputStream fs = new FileOutputStream(filePath);
    fs.write(text.getBytes("utf-8"));
    fs.close();
//    FileWriter fw = new FileWriter(filePath, true); //the true will append the new data
//    fw.write(text);//appends the string to the file
//    fw.close();
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdfPage
////////////////////////////////////////////////////////////////////////////////////////////////////  
  public PdfHtmlData GetPageHtml(PdfPage page, PdfHtmlSettings settings) throws Exception {
    PdfHtmlData data = new PdfHtmlData();

    PdfRect bbox = page.GetCropBox();
    double zoom = (double) settings.targetWidth / (double) (bbox.right - bbox.left);

    PdfPageView pageView = page.AcquirePageView(zoom, PdfRotate.kRotate0);
    if (pageView == null) {
      return data;
    }

    PdfPageRenderParams renderParams = new PdfPageRenderParams();
    pageView.DrawPage(renderParams);

    data.css += ".pdf-page-" + page.GetNumber() + " {\n";
    if (settings.responsiveLayout == false) {
      // Fixed Layout
      data.css += "width: 100%;\n";
      data.css += "height: " + pageView.GetDeviceHeight() + "px; \n";
      //create background image
      String bg_image = "page" + page.GetNumber() + ".jpg";
      String bg_image_path = settings.htmlPath + settings.imgDir + bg_image;
      PdfImage image = pageView.GetImage();
      if (image == null) {
        throw new Exception("Error creating image");
      }
      File imgPath = new File(settings.htmlPath + settings.imgDir);
      imgPath.mkdirs();
      image.Save(bg_image_path, PdfImageFormat.kImageFormatJpg);
      data.css += "background-image: url(\"" + settings.imgDir + bg_image + "\");\n";
    }
    data.css += "}\n";

    data.html += "<div id=\"page-" + page.GetNumber() + "\" "
            + "class=\"pdf-page pdf-page-" + page.GetNumber() + "\" "
            + "type=\"page\" "
            + "number=\"" + page.GetNumber() + "\" "
            + "r=\"" + ((double) pageView.GetDeviceHeight() / (double) pageView.GetDeviceWidth()) + "\" "
            + "id=\"pdf-page\" "
            + ">\n";

    //process elements
    PdfPageMapParams params = new PdfPageMapParams();
    PdePageMap pageMap = page.AcquirePageMap(params);
    int numElems = pageMap.GetNumElements();
    for (int i = 0; i < numElems; i++) {
      PdeElement elem = pageMap.GetElement(i);
      if (elem == null) {
        continue;
      }
      data.Append(GetElementHtml(elem, null, settings, pageView));
    }

    //terminate page
    data.html += "</div>\n";

    return data;
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeElement
////////////////////////////////////////////////////////////////////////////////////////////////////
  public PdfHtmlData GetElementHtml(PdeElement elem, PdeElement parent,
          PdfHtmlSettings settings, PdfPageView pageView) {
    PdfHtmlData data = new PdfHtmlData();
    if (elem == null) {
      return data;
    }

    PdfElementType elemType = elem.GetType();

    if (null != elemType) {
      switch (elemType) {
        case kPdeText:
          data.Append(GetTextHtml((PdeText) elem, parent, settings, pageView));
          break;
        case kPdeTable:
          if (settings.responsiveLayout)
            data.html += "<br>";
          data.Append(GetTableHtml((PdeTable) elem, parent, settings, pageView));
          break;
        case kPdeRect:
          if (settings.responsiveLayout)
            data.html += "<br>";
          data.Append(GetRectHtml((PdeRect) elem, parent, settings, pageView));
          break;
        case kPdeImage:
          PdeImage image = (PdeImage) elem;
          if (settings.responsiveLayout)
            data.html += "<br>";
          data.Append(GetImageHtml(image, settings, pageView));
          if (settings.responsiveLayout) {
            data.html += "<br>";
          }
          break;
        case kPdeFormField:
          PdeFormField formField = (PdeFormField) elem;
          data.Append(GetFormFieldHtml(formField, parent, settings, pageView));
          break;
        default:
          break;
      }
    }

    return data;
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeRect
////////////////////////////////////////////////////////////////////////////////////////////////////
  public PdfHtmlData GetRectHtml(PdeRect rect, PdeElement parent,
          PdfHtmlSettings settings, PdfPageView pageView) {
    PdfHtmlData data = new PdfHtmlData();
    if (rect == null) {
      return data;
    }

    int numChilds = rect.GetNumChildren();
    if (numChilds == 0)
      return data;
    // if there is only image - add image
    if (numChilds == 1) {
      PdeElement child = rect.GetChild(0);
      PdfElementType childType = child.GetType();          
      if (childType == PdfElementType.kPdeImage) {
        data.Append(GetImageHtml((PdeImage) child, settings, pageView));
        return data;
      }
    }
    
    if (settings.responsiveLayout) {
      String cls_id = "obj_" + GetNewHtmlClassName();
      String fill_color_str = "", stroke_color_str = "";
      PdfGraphicState gstate = rect.GetGraphicState();
      if (gstate.color_state.fill_type != PdfFillType.kFillTypeNone) {
        fill_color_str = Color2Str(gstate.color_state.fill_color);
      }
      if (gstate.color_state.stroke_type != PdfFillType.kFillTypeNone) {
        stroke_color_str = Color2Str(gstate.color_state.stroke_color);
      }
      data.html += "<br>";
      data.html += "<div class=\"" + cls_id + " pdf-rect\">";
      data.css += "." + cls_id + " {\n";
      if (fill_color_str.length() > 0) {
        data.css += "background-color: " + fill_color_str + ";\n";
      }
      if (stroke_color_str.length() > 0) {
        //c += "border: 4px solid " + stroke_color_str + " ;";
        data.css += "border: ";
        data.css += gstate.line_width;
        data.css += "px solid " + stroke_color_str + " ;";
      }
      // not to extend the rect to the full page width
      data.css += "display: inline-block;\n";
      // end not to extend the rect to the full page width
      data.css += "}\n";

    }

    for (int i = 0; i < numChilds; i++) {
      PdeElement child = rect.GetChild(i);
      data.Append(GetElementHtml(child, null, settings, pageView));
    }
    if (settings.responsiveLayout) {
      data.html += "</div><br>";
    }

    return data;
  }
  
////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeTable
////////////////////////////////////////////////////////////////////////////////////////////////////
  public PdfHtmlData GetTableHtml(PdeTable table, PdeElement parent,
          PdfHtmlSettings settings, PdfPageView pageView) {
    PdfHtmlData data = new PdfHtmlData();
    if (table == null) {
      return data;
    }

    if (settings.responsiveLayout) {
      String fill_color_str, stroke_color_str;
      PdfGraphicState gstate = table.GetGraphicState();
      if (gstate.color_state.fill_type != PdfFillType.kFillTypeNone) {
        fill_color_str = Color2Str(gstate.color_state.fill_color);
      }
      if (gstate.color_state.stroke_type != PdfFillType.kFillTypeNone) {
        stroke_color_str = Color2Str(gstate.color_state.stroke_color);
      }
      data.html += "<style>\n";
      data.html += "table, th, td{\n";
      data.html += "border: 1px solid black;\n";
      data.html += "border-collapse: collapse;\n";
      data.html += "}\n";
      data.html += "</style>\n";
    }
    int numRows = table.GetNumRows();
    int numCols = table.GetNumCols();
    if (settings.responsiveLayout) {
      data.html += "<table border=\"1\" style=\"width:100%\">\n";
 
      PdfRect table_bbox = table.GetBBox();
      double table_width = table_bbox.right - table_bbox.left;
      for (int col = 0; col < numCols; col++) {
        PdeCell cell = table.GetCell(0, col);
        if (cell == null) {
          continue;
        }
        PdfRect bbox = cell.GetBBox();
        int percent = (int) (((bbox.right - bbox.left) / table_width) * 100);
        data.html += "<col style=\"width:" + percent + "%\">\n";       
      }
    }
    
    for (int row = 0; row < numRows; row++) {
      if (settings.responsiveLayout) {
        data.html += "<tr>\n";
      }
      for (int col = 0; col < numCols; col++) {
        PdeCell cell = table.GetCell(row, col);
        if (cell == null) {
          continue;
        }
        if (settings.responsiveLayout) {
          int row_span = cell.GetRowSpan();
          int col_span = cell.GetColSpan();
          if (row_span == 0 || col_span == 0) {
            continue;
          }
          data.html += "<td ";
          data.html += "colspan = \"" + col_span + "\" ";
          data.html += "rowspan = \"" + row_span + "\" ";
          data.html += ">";
        }

        // process cell's children
        int children_count = cell.GetNumChildren();
        if (children_count > 0) {
          for (int i = 0; i < children_count; i++) {
            PdeElement child = cell.GetChild(i);
            if (child == null) {
              continue;
            }
            data.Append(GetElementHtml(child, null, settings, pageView));
          }
        }

        if (settings.responsiveLayout) {
          data.html += "</td>\n";
        }
      }
      if (settings.responsiveLayout) {
        data.html += "</tr>\n";
      }
    }
    if (settings.responsiveLayout) {
      data.html += "</table>";
    }

    return data;
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

  public PdfHtmlData GetTextHtml(PdeText text, PdeElement parent,
          PdfHtmlSettings settings, PdfPageView pageView) {
    PdfHtmlData data = new PdfHtmlData();
    if (text == null) {
      return data;
    }

    PdfRect bbox = text.GetBBox();
    PdfDevRect rect = pageView.RectToDevice(bbox);
    if (rect.right == rect.left || rect.top == rect.bottom) {
      return data;
    }
    double width = rect.right - rect.left;
    double height = rect.bottom - rect.top;

    if (!settings.responsiveLayout) {
      //get font properties
      PdfTextState ts = text.GetTextState();
      double font_size = ts.font_size;
      int flags = 0; // PdfTextElementGetFontFlags(m_element, 0);
      String fontFamily = "sans-serif";
      String fontFamilyCls = "ff_ss";
      if ((flags & Pdfix.kFontFixedPitch) != 0) {
        fontFamily = "\"Courier New\", monospace";
        fontFamilyCls = "ff_m";
      }
      if ((flags & Pdfix.kFontScript) != 0) {
        fontFamily = "Verdana, serif";
        fontFamilyCls = "ff_s";
      }

      // in case parent is set modify the rect to relative position to it's parent
      // used in responsive layout when positioning text over image
      PdfDevRect parentRect = new PdfDevRect();
      if (parent != null) {
        PdfRect parentBox = parent.GetBBox();
        parentRect = pageView.RectToDevice(parentBox);
      }

      // go through lines one by one
      int numLines = text.GetNumTextLines();
      for (int lineIndex = 0; lineIndex < numLines; lineIndex++) {
        PdeTextLine textLine = text.GetTextLine(lineIndex);
        PdfRect lineBox = textLine.GetBBox();
        PdfDevRect lineRect = pageView.RectToDevice(lineBox);
        String line_text = textLine.GetText();
        double lineWidth = lineRect.right - lineRect.left;
        double lineHeight = lineRect.bottom - lineRect.top;

        if (lineWidth == 0 || lineHeight == 0) {
          continue;
        }

        int pageWidth, pageHeight;

        if (parent != null) {
          lineRect.left -= parentRect.left;
          lineRect.top -= parentRect.top;
          lineRect.right -= parentRect.left;
          lineRect.bottom -= parentRect.top;
          pageWidth = parentRect.right - parentRect.left;
          pageHeight = parentRect.bottom - parentRect.top;
        } else {
          pageWidth = pageView.GetDeviceWidth();
          pageHeight = pageView.GetDeviceHeight();
        }

        double left = lineRect.left / (double) pageWidth * 100.;
        double top = lineRect.top / (double) pageHeight * 100.;
        double width2 = (lineRect.right - lineRect.left) / (double) pageWidth * 100.;
        double height2 = (lineRect.bottom - lineRect.top) / (double) pageHeight * 100.;

        String classId = "obj_" + GetNewHtmlClassName();
        data.css += "." + classId + " {\n";
        data.css += "left: " + left + "%; \n";
        data.css += "top: " + top + "%; \n";
        data.css += "width: " + width2 + "%; \n";
        data.css += "height: " + height2 + "%; \n";
        data.css += "font-size: " + lineHeight + "px;\n";
        data.css += "text-align: left;\n";
        data.css += "}\n";

        // dreate div element wich holds the class_id 
        data.html += "<div ";
        data.html += "class=\"" + classId + " " + fontFamilyCls + " pdf-txt-fixed pdf-obj-fixed\">\n";
        data.html += "<span ";
        data.html += "name=\"fix-text\" >";
        data.html += line_text + "\n";
        data.html += "</span>";
        data.html += "</div>\n";
      }
    } else {
      // text indent
      double indent = text.GetIndent();

      // text alignment
      PdfTextAlignment alignment = text.GetAlignment();
      boolean join_lines = true;
      /* todo
        (alignment == kAlignmentLeftReflow) ||
        (alignment == kAlignmentRightReflow) ||
        (alignment == kAlignmentJustify); */

      PdfTextState ts = text.GetTextState();
      boolean first_ts = true;

      data.html += "<div >";
      data.html += "<p >";
      
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
            PdfRect char_bbox = word.GetCharBBox(i);
            char_ts.flags |= GetCharAnnotFlag(char_bbox);
            String char_str = word.GetCharText(i);
            if (first_ts || !ts.equals(char_ts)) {
              // end previous span 
              if (!first_ts) {
                data.html += "</span>";
              }
              // start new span 
              ts = char_ts;
              String class_id = "obj_" + GetNewHtmlClassName();
              data.css += GetTextStateCSS(class_id, ts, alignment, indent);
              data.html += "<span ";
              data.html += "class=\"";
              data.html += class_id + " ";
              data.html += "pdf-obj\">";
              first_ts = false;
            }
            data.html += char_str;
          }
          // add whitespace between words
          if (w != num_words - 1) {
            data.html += " ";
          }
        }
        // add whitespace between lines
        if (l != num_lines - 1) {
          data.html += " ";
        }
        if (!join_lines) {
          data.html += "<br>";
        }
      }
      data.html += "</span>";
      data.html += "</p>";
      data.html += "</div>";
    }
    return data;
  }

  private String GetTextStateCSS(String cls_id, PdfTextState ts,
          PdfTextAlignment alignment, double indent) {
    String css = "." + cls_id + " {\n";
    String font_name, sys_font_name;
    String font_family = "sans-serif";
    PdfFontCharset charset = PdfFontCharset.kFontUnknownCharset;
    PdfFont font = ts.font;
    PdfFontState fs = font.GetFontState();
    boolean bold = fs.bold == 1;
    boolean italic = fs.italic == 1;
    int fw = 400;
    if (bold) {
      fw = 800;
    }
    sys_font_name = font.GetSystemFontName();
    if (sys_font_name.length() > 0) {
      charset = font.GetSystemFontCharset();
    }
    font_name = font.GetFontName();

    css += "font-family: ";
    if (sys_font_name.length() > 0) {
      css += "\"" + sys_font_name + "\",";
    }
    css += font_family + ";\n";
    if (italic) {
      css += "font-style: italic;\n";
    }
    css += "font-size: " + ts.font_size + "px;\n";
    css += "font-weight: " + fw + ";\n";
    css += "color: " + Color2Str(ts.color_state.fill_color) + ";\n";
    if ((ts.flags & Pdfix.kTextFlagUnderline) != 0) {
      css += "text-decoration: underline;\n";
    }
    if ((ts.flags & Pdfix.kTextFlagStrikeout) != 0) {
      css += "text-decoration: line-through;\n";
    }
    if ((ts.flags & Pdfix.kTextFlagHighlight) != 0) {
      css += "background-color: yellow;\n";
    }

    // doesn't work yet
    //switch (alignment) {
    //case kAlignmentLeft:        css += "text-align: left;\n"; break;
    //case kAlignmentLeftReflow:  css += "text-align: left;\n"; break;
    //case kAlignmentRight:       css += "text-align: right;\n"; break;
    //case kAlignmentRightReflow: css += "text-align: right;\n"; break;
    //case kAlignmentCenter:      css += "text-align: center;\n"; break;
    //case kAlignmentJustify:     css += "text-align: justify;\n"; break;
    //}
    css += "text-align: justify;\n";
    if (indent != 0) {
      css += "text-indent: " + indent + "px; \n";
    }
    css += "}\n\n";
    return css;
  }

  public String GetTextSpan(String cls_id, String text, boolean append_space) {
    String html = "";
    html += "<span ";
    html += "class=\"";
    if (cls_id.length() > 0) {
      html += cls_id + " ";
    }
    html += "pdf-obj\">";
    html += text;
    if (append_space) {
      html += " ";
    }
    html += "</span>";
    return html;
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeImage
////////////////////////////////////////////////////////////////////////////////////////////////////
  public PdfHtmlData GetImageHtml(PdeImage elem, PdfHtmlSettings settings, PdfPageView pageView) {
    PdfHtmlData data = new PdfHtmlData();
    if (settings.responsiveLayout) {
      PdfRect elem_rect = elem.GetBBox();
      PdfDevRect elem_dev_rect = pageView.RectToDevice(elem_rect);
      int elem_width = elem_dev_rect.right - elem_dev_rect.left;
      int elem_height = elem_dev_rect.bottom - elem_dev_rect.top;
      if (elem_height == 0 || elem_width == 0) {
        return data;
      }

      imgCounter++;

      // save image
      String image_name = "img" + imgCounter + ".png";
      String image_path = settings.htmlPath + settings.imgDir + image_name;

      File imgDir = new File(settings.htmlPath + settings.imgDir);
      imgDir.mkdirs();

      elem.Save(image_path, PdfImageFormat.kImageFormatPng, pageView);

      String class_id = "obj_" + GetNewHtmlClassName();
      data.css += "." + class_id + " {\n";
      data.css += "text-align: center;";
      data.css += "}\n\n";

      String pdf_obj_cls = settings.responsiveLayout ? "pdf-obj" : " pdf-obj-fixed";

      data.html += "<div ";
      data.html += "class=\"" + class_id + " " + pdf_obj_cls + "\">\n";
      String div_class_id = "obj_" + GetNewHtmlClassName();
      data.css += "." + div_class_id + " {\n";
      data.css += "display: inline-block;\n";
      data.css += "position: relative;\n";
      data.css += "}\n\n";
      data.html += "<div class=\"" + div_class_id + "\">";

      String img_class_id = "obj_" + GetNewHtmlClassName();
      data.css += "." + img_class_id + " {\n";
      data.css += "width: 100%;\n";
      data.css += "position: relative;\n";
      data.css += "top: 0px;\n";
      data.css += "max-width: " + elem_width + "px; \n";
      data.css += "}\n\n";

      data.html += "<img  ";
      data.html += "class=\"" + img_class_id + " pdf-img " + pdf_obj_cls + "\" ";
      data.html += "src=\"" + settings.imgDir + image_name;
      data.html += "\">";
    }

    // process child elements
    // all child elements should have fixed position
    PdfHtmlSettings settings2 = settings.Clone();
    settings2.responsiveLayout = false;
    PdeElement parent = null;
    if (settings.responsiveLayout) {
      parent = elem;
    }
    int children_count = elem.GetNumChildren();
    for (int i = 0; i < children_count; i++) {
      // put over image only text and form fields
      PdeElement child = elem.GetChild(i);
      if (child.GetType() == PdfElementType.kPdeText) {
        PdeText text = (PdeText) child;
        data.Append(GetTextHtml(text, parent, settings2, pageView));
      } else if (child.GetType() == PdfElementType.kPdeFormField) {
        PdeFormField form_field = (PdeFormField) child;
        data.Append(GetFormFieldHtml(form_field, parent, settings2, pageView));
      }
    }

    if (settings.responsiveLayout) {
      data.html += "</div>";
      data.html += "</div>";
    }

    return data;
  }

////////////////////////////////////////////////////////////////////////////////////////////////////
// PdeFormField
////////////////////////////////////////////////////////////////////////////////////////////////////  
  public PdfHtmlData GetFormFieldHtml(PdeFormField elem, PdeElement parent,
          PdfHtmlSettings settings, PdfPageView pageView) {
    PdfHtmlData data = new PdfHtmlData();
    if (elem == null) {
      return data;
    }

    PdfRect elemBox = elem.GetBBox();
    PdfDevRect elemRect = pageView.RectToDevice(elemBox);

    PdfWidgetAnnot widget = elem.GetWidgetAnnot();
    if (widget == null) {
      return data;
    }
    
    PdfDevRect parentRect = new PdfDevRect();
    if (parent != null) {
      PdfRect parentBox = parent.GetBBox();
      parentRect = pageView.RectToDevice(parentBox);
    }

    String classId = "obj_" + GetNewHtmlClassName();
    data.css += "." + classId + " {\n";

    int pageWidth, pageHeight;
    
    if (parent != null) {
      elemRect.left -= parentRect.left;
      elemRect.top -= parentRect.top;
      elemRect.right -= parentRect.left;
      elemRect.bottom -= parentRect.top;
      pageWidth = parentRect.right - parentRect.left;
      pageHeight = parentRect.bottom - parentRect.top;
    } else {
      pageWidth = pageView.GetDeviceWidth();
      pageHeight = pageView.GetDeviceHeight();
    }

    double left = elemRect.left / (double) pageWidth * 100.;
    double top = elemRect.top / (double) pageHeight * 100.;
    double width2 = (elemRect.right - elemRect.left) / (double) pageWidth * 100.;
    double height2 = (elemRect.bottom - elemRect.top) / (double) pageHeight * 100.;

    if (settings.responsiveLayout == false) {
      data.css += "left: " + left + "%; \n";
      data.css += "top: " + top + "%; \n";
      data.css += "width: " + width2 + "%; \n";
      data.css += "height: " + height2 + "%; \n";
    }

    int flags = widget.GetFlags();
    if ((flags & Pdfix.kAnnotFlagInvisible) != 0
            || (flags & Pdfix.kAnnotFlagHidden) != 0) {
      data.css += "display: none; \n";
    }

    PdfAnnotAppearance ap = widget.GetAppearance();
    // form field with border and transparent background
    // this cannot be applied on all annotation types
    /* if (ap.fill_type == PdfFillType.kFillTypeSolid) {
      data.css += "background-color: rgb("
              + ap.fill_color.r + ", "
              + ap.fill_color.g + ", "
              + ap.fill_color.b + "); ";
    } 
    else {
      data.css += "background-color: transparent; \n";
    }
    data.css += "border-width: " + ap.border_width + "px; ";
    if (ap.border_width > 0) {
      data.css += "border-style: solid; ";
      data.css += "border-color: rgb("
              + ap.border_color.r + ", "
              + ap.border_color.g + ", "
              + ap.border_color.b + "); ";
    } */

    // form field
    // if (subtype == PdfAnnotSubtype.kAnnotWidget) {
    PdfFormField field = widget.GetFormField();

    data.js += "field_add_annot("
            + field.getId() + ", "
            + widget.getId() + " );\n";

    PdfFieldType fieldType = field.GetType();
    boolean hasAction = false;
    //TODO: in case of Btn field (Push, Check, Radio) we may require it's appearance to be
    //drawn as an image (SimpleFormCalculations.pdf)

    int fieldFlags = field.GetFlags();

    String value = field.GetValue();
    String defaultValue = field.GetDefaultValue();

    String fullName = field.GetFullName();
    //String tooltip = field.GetTooltip();
    //if (tooltip.length() == 0)
    //tooltip = fullName;

    int maxLength = field.GetMaxLength();
    String name = "", innHtml = "", props = "";

    String actionJS = "";

    data.Append(GetActionHtml(PdfActionEventType.kActionEventAnnotMouseUp, widget));
    data.Append(GetActionHtml(PdfActionEventType.kActionEventAnnotMouseDown, widget));
    data.Append(GetActionHtml(PdfActionEventType.kActionEventFieldKeystroke, widget));
    data.Append(GetActionHtml(PdfActionEventType.kActionEventFieldFormat, widget));

    String pdfObjCls = settings.responsiveLayout ? " pdf-obj" : "pdf-obj-fixed";

    // props += action_obj_props;
    data.js += actionJS;
    props += "id=\"" + fullName + "\" ";
    props += "class=\"" + classId + " " + pdfObjCls + " pdf-form-field \"";
    props += "name=\"" + fullName + "\" ";
    props += "formid=\"" + field.getId() + "\" ";
    props += "annot=\"" + widget.getId() + "\" ";

    if ((fieldFlags & Pdfix.kFieldFlagDCommitOnSelChange) != 0) {
      props += "commitOnSelChange=\"true\" ";
    }

    //std::wstring font_name;
    //font_name.resize(PdfAnnotGetFontName(field, nullptr, 0));
    //PdfFormFieldGetFontName(field, (wchar_t*)font_name.c_str(), font_name.size());
    //double font_size = ap.font_size;
    //if (font_size != 0) {
    //css += "font-size: " + std::to_string(font_size) + "px; ";
    //}
    data.css += "padding: 0px 0px; \n";

    if ((fieldFlags & Pdfix.kFieldFlagReadOnly) != 0) {
      props += "readonly ";
    }

    if (fieldType == PdfFieldType.kFieldButton) {
      String caption = widget.GetCaption();
      name = "button";
      innHtml = caption;
    } else if (fieldType == PdfFieldType.kFieldText && (fieldFlags & Pdfix.kFieldFlagMultiline) != 0) {
      name = "textarea";
      innHtml = value;
      data.css += "resize: none; \n";
      data.css += "overflow: hidden; \n";
    } else if (fieldType == PdfFieldType.kFieldCombo || fieldType == PdfFieldType.kFieldList) {
      name = "select";
      int count = field.GetOptionCount();
      for (int i = 0; i < count; i++) {
        String optValue = field.GetOptionValue(i);
        String optCaption = field.GetOptionValue(i);
        // TODO: default selected value
        boolean selected = (value.compareTo(optValue.isEmpty() ? optCaption : optValue) == 0);
        innHtml += "<option ";
        if (selected) {
          innHtml += "selected ";
        }
        innHtml += "value=\"" + optValue + "\">" + optCaption;
        innHtml += "</option>\n";
      }
      if (fieldType == PdfFieldType.kFieldList) {
        props += "size=3 ";
      }
      if ((fieldFlags & Pdfix.kFieldFlagMultiSelect) != 0) {
        props += "multiple ";
      }
    } else {
      String type = "text";
      if (fieldType == PdfFieldType.kFieldText) {
        if ((fieldFlags & Pdfix.kFieldFlagPassword) != 0) {
          type = "password";
        } else if ((fieldFlags & Pdfix.kFieldFlagFileSelect) != 0) {
          type = "file";
        } else {
          //if (js_k.size()) {
          //  if (js_k.find(L"AFNumber_Keystroke") == 0) type = "number";
          //  else if (js_k.find(L"AFDate_Keystroke") == 0) {
          //    type = "date";
          //  }
          //  else if (js_k.find(L"AFTime_Keystroke") == 0) {
          //    type = "time";
          //  }
          //else if (js_str.find(L"AFPercent_Keystroke") == 0) {}
          //}
        }
        props += "value=\"" + value + "\" ";
        if (maxLength > 0) {
          props += "maxLength=" + maxLength + " ";
        }
      } else if (fieldType == PdfFieldType.kFieldCheck || fieldType == PdfFieldType.kFieldRadio) {
        if (fieldType == PdfFieldType.kFieldRadio) {
          type = "radio";
        } else {
          type = "checkbox";
        }
        data.css += "margin: 0px 0px 0px 0px;\n";
        //get export value of this radio button/checkbox
        String expValue = field.GetWidgetExportValue(widget);
        props += "value=\"" + expValue + "\" ";
      }

      name = "input";
      props += "type=\"" + type + "\" ";
    }
    if (defaultValue.length() > 0) {
      props += "defaultValue=\"" + defaultValue + "\" ";
    }
    // element events
    //props += "onkeypress=\"return do_change(this, false);\" ";
    //props += "onchange=\"do_change(this, false); if (do_change(this, true)) do_calculations();\" ";
    data.html += "<" + name + " "/* + style + " " */ + props + ">\n";
    data.html += innHtml + "\n";
    data.html += "</" + name + ">\n";
    // }
    data.css += "}\n\n";

    return data;
  }

  public PdfHtmlData GetActionHtml(PdfActionEventType event, PdfAnnot annot) {
    PdfHtmlData data = new PdfHtmlData();

    PdfAnnotSubtype suptype = annot.GetSubtype();

    PdfAction action = null;

    if (annot.GetSubtype() == PdfAnnotSubtype.kAnnotWidget) {
      PdfWidgetAnnot widget = (PdfWidgetAnnot) annot;
      if (event == PdfActionEventType.kActionEventAnnotMouseUp) {
        action = widget.GetAction();
      }
      if (action == null) {
        action = widget.GetAAction(event);
      }
    }

    if (action == null) {
      return data;
    }

    String prefix = "";
    switch (event) {
      case kActionEventAnnotEnter:
        break;
      case kActionEventAnnotExit:
        break;
      case kActionEventAnnotMouseDown:
        prefix = "D";
        break;
      case kActionEventAnnotMouseUp:
        prefix = "U";
        break;
      case kActionEventAnnotFocus:
        prefix = "Fo";
        break; //forms only!
      case kActionEventAnnotBlur:
        prefix = "Bl";
        break; //forms only!
      case kActionEventAnnotPageOpen:
        break;
      case kActionEventAnnotPageClose:
        break;
      case kActionEventAnnotPageVisible:
        break;
      case kActionEventAnnotPageInvisible:
        break;
      case kActionEventFieldKeystroke:
        prefix = "K";
        break;
      case kActionEventFieldFormat:
        prefix = "F";
        break;
      case kActionEventFieldValidate:
        prefix = "V";
        break;
      case kActionEventFieldCalculate:
        prefix = "C";
        break;
    }

    String actionName = prefix + annot.getId() + "()";
    PdfActionType actionType = action.GetSubtype();
    String strJS = "";

    if (null != actionType) {
      switch (actionType) {
        case kActionJavaScript:
          strJS = action.GetJavaScript().trim();
          break;
        case kActionResetForm:
          strJS = "window.resetForm(null);\n";
          break;
        //todo submit form data
        case kActionSubmitForm:
          break;
        default:
          break;
      }
    }

    if (strJS.length() > 0) {
      data.js += "function " + actionName + " {\n";
      data.js += strJS + "\n";
      data.js += "}\n\n";
    }
    return data;
  }
}
