/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jozef
 */
@WebServlet(name = "GetFile", urlPatterns = {"/GetFile"})
public class GetFile extends HttpServlet {

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
    
    Log.write("GetFile - Enter");

    try {
      String path = (String) request.getSession().getAttribute("getfile");
      if (path == null)
        throw new Exception("File not found");
      Log.write("GetFile - path: " + path + " [ok]");
      String name = (String) request.getSession().getAttribute("getfilename");
      if (name == null) {
        // extract name from the path
        int pos = path.lastIndexOf("/");
        if (pos > 0)
        name = path.substring(pos + 1);
      }
      Log.write("GetFile - name: " + name + " [ok]");
      
      request.getSession().removeAttribute("getfile");
      request.getSession().removeAttribute("getfilename");
      Log.write("GetFile - session.removeAttributes(getfile, getfilename) [ok]");

      PdfixUtils.SendFileAsResponse(path, name, response, true);
    }
    catch (Exception ex) {
      Logger.getLogger(SignFile.class.getName()).log(Level.SEVERE, null, ex);
      Log.write("GetFile - Exception: " + ex.getMessage());
    }
    Log.write("GetFile - Exit");

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
