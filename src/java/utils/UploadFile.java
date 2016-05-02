////////////////////////////////////////////////////////////////////////////////////////////////////
// UploadFile.java
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////
package utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.Part;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

import net.pdfix.docx.PdfDocx;
import net.pdfix.html.*;
import net.pdfix.pdfixlib.*;

@MultipartConfig(maxFileSize = 10485760L)

////////////////////////////////////////////////////////////////////////////////////////////////////
// Class UploadFile 
////////////////////////////////////////////////////////////////////////////////////////////////////
public class UploadFile extends HttpServlet {
  // Handles the HTTP <code>POST</code> method.
  // @param request servlet request
  // @param response servlet response
  // @throws ServletException if a servlet-specific error occurs
  // @throws IOException if an I/O error occurs

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    try {
      if (!PdfixUtils.LoadPdfix(request)) {
        throw new Exception("Unable to load pdfix.dll");
      }

      //processRequest(request, response);
      request.setCharacterEncoding("UTF-8");

      int file_id = 0;
      Date date = new Date();
      String id = "" + date.getTime();
      String hash = "";

      File filesDir = new File(PdfixSettings.FILE_DIR);
      filesDir.mkdirs();

      //save files on disk       
      File pdfFile = null;
      String fileName = "";

      for (Part part : request.getParts()) {
        if (!"upload".equals(part.getName())) {
          fileName = getFileName(part);
          File tmp = new File(fileName);
          fileName = tmp.getName();

          hash = utils.PdfixUtils.computeFileHash(part.getInputStream());
          pdfFile = new File(PdfixSettings.FILE_DIR + File.separator + hash + ".pdf");
          if (!pdfFile.exists()) {
            try {
              Files.copy(part.getInputStream(), pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
            }
          }

          //just one file at a time
          break;
        }
      }

      if (pdfFile == null) {
        throw new Exception("Unable to upload file");
      }

      String savePath = utils.PdfixSettings.CONV_DIR + File.separator + id + File.separator;
      File saveDir = new File(savePath);
      saveDir.mkdirs();

      FileOutputStream nameTxt = new FileOutputStream(savePath + "name.txt");
      nameTxt.write(fileName.getBytes());
      nameTxt.close();
      FileOutputStream fileTxt = new FileOutputStream(savePath + "file.txt");
      fileTxt.write((hash + ".pdf").getBytes());
      fileTxt.close();

      PdfHtmlSettings settings = new PdfHtmlSettings();
      settings.rootPath = "../../../";

      // convert to html - fixed layout
      try {
        settings.htmlPath = savePath + "fixed" + File.separator;
        settings.responsiveLayout = false;
        ConvertToHtml(pdfFile, settings);
      } catch (Exception e) {
        response.getWriter().write(e.getMessage());
      }

      // convert to html - responsive layout
      try {
        settings.htmlPath = savePath + "responsive" + File.separator;
        settings.responsiveLayout = true;
        ConvertToHtml(pdfFile, settings);
      } catch (Exception e) {
        response.getWriter().write(e.getMessage());
      }

      // convert to docx
      try {
        String docxPath = savePath + id + ".docx";
        ConvertToDocx(pdfFile, docxPath);
      } catch (Exception e) {
        response.getWriter().write(e.getMessage());
      }
      response.sendRedirect("index.jsp?id=" + id);
    } catch (Exception e) {
      response.getWriter().write(e.getMessage());
    }
  }

  // ConvertToHtml - save file and convert to html
  private boolean ConvertToHtml(File pdfFile, PdfHtmlSettings settings) throws IOException {
    String absoluteFileName = pdfFile.toPath().toString();

    File htmlDir = new File(settings.htmlPath);
    htmlDir.mkdirs();

    Pdfix pdfix = new Pdfix();
    if (pdfix == null) {
      return false;
    }
    if (!pdfix.Authorize(PdfixSettings.AUTH_EMAIL, PdfixSettings.AUTH_KEY)) {
      return false;
    }

    boolean ret = false;
    PdfDoc pdfDoc = pdfix.OpenDoc(absoluteFileName, "");
    if (pdfDoc != null) {
      try {
        PdfHtmlDoc htmlDoc = new PdfHtmlDoc(pdfDoc);
        htmlDoc.CreateHtml(settings);
        ret = true;
      } catch (Exception e) {
      }
      pdfDoc.Close();
    }
    pdfix.Destroy();
    return ret;
  }

  // ConvertToDocx
  private boolean ConvertToDocx(File pdfFile, String docxPath) {
    String absoluteFileName = pdfFile.toPath().toString();

    File docxFile = new File(docxPath);
    File path = new File(docxFile.getParent());
    path.mkdirs();

    Pdfix pdfix = new Pdfix();
    if (pdfix == null) {
      return false;
    }
    if (!pdfix.Authorize(PdfixSettings.AUTH_EMAIL, PdfixSettings.AUTH_KEY)) {
      return false;
    }
    boolean ret = false;
    PdfDoc pdfDoc = pdfix.OpenDoc(absoluteFileName, "");
    if (pdfDoc != null) {
      try {
        PdfDocx pdfDocx = new PdfDocx(pdfDoc);
        pdfDocx.CreateDocx(docxPath);
        ret = true;
      } catch (Exception e) {
      }
      pdfDoc.Close();
    }
    pdfix.Destroy();
    return ret;
  }

  // Utility method to get file name from HTTP header content-disposition
  private String getFileName(Part part) {
    String contentDisp = part.getHeader("content-disposition");
    System.out.println("content-disposition header= " + contentDisp);
    String[] tokens = contentDisp.split(";");
    for (String token : tokens) {
      if (token.trim().startsWith("filename")) {
        return token.substring(token.indexOf("=") + 2, token.length() - 1);
      }
    }
    return "";
  }

  private String readFile(String path, Charset encoding)
          throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  // Returns a short description of the servlet.
  // @return a String containing servlet description
  @Override
  public String getServletInfo() {
    return "Upload file to index manager";
  }// </editor-fold>
}
