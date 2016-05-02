/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.pdfix.pdfixlib.PdfCertDigSig;
import net.pdfix.pdfixlib.PdfDoc;
import net.pdfix.pdfixlib.Pdfix;

/**
 *
 * @author jozef
 */
@WebServlet(name = "GetDocx", urlPatterns = {"/GetDocx"})
public class GetDocx extends HttpServlet {

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

//    Pdfix pdfix = null;
//    PdfDoc doc = null;
//    PdfCertDigSig ds = null;
    try {
      String id = (String) request.getSession().getAttribute("id");
      String orig = (String) request.getSession().getAttribute("orig");
      String name = (String) request.getSession().getAttribute("name");
      
      if (id.length() == 0) {
        response.sendRedirect("./");
        return;
      }
      
      String docxPath = request.getSession().getServletContext().getRealPath("/")
              + "files/" + id + "/" + id + ".docx";
      String docxName = name + ".docx";
      
//      String openPath = PdfixSettings.FILE_DIR + File.separator + orig;
//      String random = UUID.randomUUID().toString();
//      String savePath = PdfixSettings.TMP_DIR + File.separator + random + ".pdf";
//      
//      File tmpDir = new File(PdfixSettings.TMP_DIR);
//      tmpDir.mkdirs();
//      
//      if (!PdfixUtils.LoadPdfix(request))
//        return;
//      
//      pdfix = new Pdfix();
//      if (pdfix == null)
//        throw new Exception("unable to initialize Pdfix");
//      
//      doc = pdfix.OpenDoc(openPath, "");
//      if (doc == null) {
//        throw new Exception(pdfix.GetError());        
//      }
//      
//      String pfxPath = request.getSession().getServletContext().getRealPath("/")
//              + "WEB-INF/resources/JohnSmith.pfx";
//      int err = 0;
//      //sign file
//      ds = pdfix.CreateCertDigSig();
//      err = ds.SetContactInfo("www.pdfix.net");
//      err = ds.SetLocation("My location");
//      err = ds.SetName("John Smith");
//      err = ds.SetReason("Approved");
//      err = ds.SetPfxFile(pfxPath, "SignByPdfix");
//
//      err = ds.SignDoc(doc, savePath);
                      
      // write pdf file content to response and delete tmp file
      PdfixUtils.SendFileAsResponse(docxPath, docxName, response, false);
    }
    catch (IOException e) {
      response.getWriter().println("Fail");
    } catch (Exception ex) {
      Logger.getLogger(SignFile.class.getName()).log(Level.SEVERE, null, ex);
    }
//    if (ds != null)
//      ds.Destroy();
//    if (doc != null)
//      doc.Close();
//    if (pdfix != null)
//      pdfix.Destroy();
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
