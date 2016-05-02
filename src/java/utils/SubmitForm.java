////////////////////////////////////////////////////////////////////////////////////////////////////
// SubmitForm.java
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////
package utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.pdfix.pdfixlib.*;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Class SubmitForm
////////////////////////////////////////////////////////////////////////////////////////////////////
public class SubmitForm extends HttpServlet {

  protected void SendResponse(HttpServletResponse response, String text) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    try (PrintWriter out = response.getWriter()) {
      out.println(text);
    }
  }

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

    Log.write("SubmitForm - Enter");
    Pdfix pdfix = null;
    PdfDoc doc = null;
    try {
      Map<String, String[]> parameters = request.getParameterMap();
      Log.write("SubmitForm - params [ok]");
      if (parameters.size() == 0) {
        Log.write("SubmitForm - no params [ok]");
        response.getWriter().println(" ");
        return;
      }

      String id = (String) request.getSession().getAttribute("id");         
      Log.write("SubmitForm - id: " + id + " [ok]");
      String orig = (String) request.getSession().getAttribute("orig");
      Log.write("SubmitForm - orig: " + orig + " [ok]");
      String name = (String) request.getSession().getAttribute("name");
      Log.write("SubmitForm - name: " + name + " [ok]");
      
      
      if (id.length() == 0) {
        Log.write("SubmitForm - id empty [ok]");
        response.sendRedirect("./");
        return;
      }

      String openPath = PdfixSettings.FILE_DIR + File.separator + orig;
      Log.write("SubmitForm - openPath: " + openPath + " [ok]");
      String random = UUID.randomUUID().toString();
      Log.write("SubmitForm - random: " + random + " [ok]");
      String savePath = PdfixSettings.TMP_DIR + File.separator + random + ".pdf";
      Log.write("SubmitForm - savePath: " + savePath + " [ok]");

      File tmpDir = new File(PdfixSettings.TMP_DIR);
      tmpDir.mkdirs();
      Log.write("SubmitForm - tmpDir,mkdir: [ok]");
      
      if (!PdfixUtils.LoadPdfix(request))
        throw new Exception("failed to load Pdfix");      
      Log.write("SubmitForm - LoadPdfix [ok]");

      pdfix = new Pdfix();
      if (pdfix == null)
        throw new Exception("failed to initialize Pdfix");
      Log.write("SubmitForm - GetPdfix [ok]");

      doc = pdfix.OpenDoc(openPath, "");
      if (doc == null) {
        throw new Exception(pdfix.GetError());        
      }
      Log.write("SubmitForm - OpenDoc [ok]");

      for (String parameter : parameters.keySet()) {
        String[] values = parameters.get(parameter);
        PdfFormField field = doc.GetFormFieldByName(parameter);
        Log.write("SubmitForm - field: " + parameter + " [ok]");
        if (field != null) {
          field.SetValue(values[0]);
          Log.write("SubmitForm - SetValue: " + values[0] + " [ok]");          
        }
      }
      doc.Save(savePath, PdfSaveFlags.kSaveFull);
      Log.write("SubmitForm - Save: [ok]");
      doc.Close();
      Log.write("SubmitForm - Close: [ok]");
      doc = null;
      
      request.getSession().setAttribute("getfile", savePath);  
      Log.write("SubmitForm - session.setAttribute('getfile'): " + savePath + " [ok]");
      request.getSession().setAttribute("getfilename", name);  
      Log.write("SubmitForm - session.setAttribute('getfilename'): " + name + " [ok]");
        
//      PdfixUtils.SendPdfAsResponse(savePath, name, response, true);
    }
    catch (Exception ex) {
      Logger.getLogger(SignFile.class.getName()).log(Level.SEVERE, null, ex);
      Log.write("SubmitForm - Exception " + ex.getMessage() + "");
    }
    if (doc != null)
      doc.Close();
    if (pdfix != null)
      pdfix.Destroy();
    Log.write("SubmitForm - Exit [ok]");
            
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
