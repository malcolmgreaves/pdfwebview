////////////////////////////////////////////////////////////////////////////////////////////////////
// SignFile.java
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////
package utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.pdfix.pdfixlib.*;

/**
 *
 * @author jozef
 */
@WebServlet(name = "SignFile", urlPatterns = {"/SignFile"})
public class SignFile extends HttpServlet {

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    Pdfix pdfix = null;
    PdfDoc doc = null;
    PdfDigSig ds = null;
    try {
      String id = (String) request.getSession().getAttribute("id");
      String orig = (String) request.getSession().getAttribute("orig");
      String name = (String) request.getSession().getAttribute("name");
      
      if (id.length() == 0) {
        response.sendRedirect("./");
        return;
      }
      
      String openPath = PdfixSettings.FILE_DIR + File.separator + orig;
      String random = UUID.randomUUID().toString();
      String savePath = PdfixSettings.TMP_DIR + File.separator + random + ".pdf";
      
      File tmpDir = new File(PdfixSettings.TMP_DIR);
      tmpDir.mkdirs();
      
      if (!PdfixUtils.LoadPdfix(request))
        return;
      
      pdfix = new Pdfix();
      if (pdfix == null)
        throw new Exception("unable to initialize Pdfix");
      
      doc = pdfix.OpenDoc(openPath, "");
      if (doc == null) {
        throw new Exception(pdfix.GetError());        
      }    
      
      // add watermark
      String jpgPath = request.getSession().getServletContext().getRealPath("/")
        + "WEB-INF/resources/stamp.jpg";
      
      PdfWatermarkParams params = new PdfWatermarkParams();
      params.page_range.start_page = 0;
      params.page_range.end_page = -1;
      params.page_range.page_range_spec = PdfPageRangeType.kAllPages;
      params.order_top = 1;
      params.percentage_vals = 0;
      params.h_align = PdfHorizAlign.kHorizRight;
      params.v_align = PdfVertAlign.kVertTop;
      params.h_value = -10;
      params.v_value = 10;
      params.scale = 0.5;
      params.rotation = 0;
      params.opacity = 0.5;      
      if (!doc.AddWatermarkFromImage(params, jpgPath)) {
        throw new Exception("Cannot apply watermark");
      }
      
      // sign file with pfx
      String pfxPath = request.getSession().getServletContext().getRealPath("/")
              + "WEB-INF/resources/JohnSmith.pfx";
      //sign file
      ds = pdfix.CreateDigSig();
      ds.SetContactInfo("www.pdfix.net");
      ds.SetLocation("My location");
      ds.SetName("John Smith");
      ds.SetReason("Approved");
      if (!ds.SetPfxFile(pfxPath, "SignByPdfix"))
        throw new Exception("Cannot open pfx file");        
        

      if (!ds.SignDoc(doc, savePath)) {
        throw new Exception("Cannot apply digital signature");        
      }
                      
      // write pdf file content to response and delete tmp file
      PdfixUtils.SendFileAsResponse(savePath, name, response, true);
    }
    catch (IOException e) {
      response.getWriter().println("Fail");
    } catch (Exception ex) {
      Logger.getLogger(SignFile.class.getName()).log(Level.SEVERE, null, ex);
    }
    if (ds != null)
      ds.Destroy();
    if (doc != null)
      doc.Close();
    if (pdfix != null)
      pdfix.Destroy();
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>

}
