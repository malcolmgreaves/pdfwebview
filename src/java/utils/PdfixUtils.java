////////////////////////////////////////////////////////////////////////////////////////////////////
// PdfixUtils.java
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////
package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Class PdfixUtils
////////////////////////////////////////////////////////////////////////////////////////////////////
public class PdfixUtils {

  public static String errorString;

  public static boolean LoadPdfix(HttpServletRequest request) {
    String s = "";
    try {
      String dllPath = request.getSession().getServletContext().getRealPath("/")
              + "WEB-INF/lib/pdfix64.dll";
      //use System.getProperties("os.name") to determine name
      //use System.getProperties("os.arch") to determine architecture
      System.load(dllPath);
    } catch (SecurityException | NullPointerException | UnsatisfiedLinkError e) {
      errorString = e.getMessage();
      return false;
    }
    return true;
  }
  
  public static String getTempPath () {
    return utils.PdfixSettings.TMP_DIR;
  }
  
  public static String computeFileHash(InputStream in) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
      DigestInputStream dis = new DigestInputStream(in, md);
      byte[] buffer = new byte[4096];
      while (dis.read(buffer) != -1)
              ;
      dis.close();
      byte[] hash = dis.getMessageDigest().digest();
      
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < hash.length; i++) {
        if ((0xff & hash[i]) < 0x10) {
          hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
        } else {
          hexString.append(Integer.toHexString(0xFF & hash[i]));
        }      
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(PdfixUtils.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(PdfixUtils.class.getName()).log(Level.SEVERE, null, ex);
    }
    return "";    
  }
  
  public static void SendFileAsResponse(String path, String name, HttpServletResponse response, boolean delete) 
          throws IOException {
    FileInputStream input = null;
    try {
      String ext = "";
      int i = path.lastIndexOf('.');
      if (i > 0)
        ext = path.substring(i+1).toLowerCase();
      
      if (ext.equals("pdf")) {
        response.setHeader("Content-disposition", "attachment; filename=" + name);
        response.setContentType("application/pdf");        
      }
      if (ext.equals("docx")) {
        response.setHeader("Content-disposition", "attachment; filename=" + name);
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");                
      }
      else {
        response.setContentType("text/html;charset=UTF-8");        
      }
        
      ServletOutputStream output = response.getOutputStream();
      input = new FileInputStream(path);      
      byte[] buffer = new byte[2048];
      while (true) {
        int readLen = input.read(buffer, 0, 2048);
        if (readLen == -1)
          break;
        output.write(buffer, 0, readLen);
      }
    }
    catch (IOException e) {      
    }
    if (input != null)
      input.close();

    if (delete) {
      File f = new File(path);
      f.delete();      
    }
  }
  
  public static boolean PrepareFileForView(HttpServletRequest request, String id) {
        //check if id exist under webroot folder    
    try {
      String webPath = request.getSession().getServletContext().getRealPath("/") + 
              "files" + File.separator + id;   
      File webDir = new File(webPath);
      if (!webDir.exists()) {
        // copy id folder from files to web root files
        String srcPath = PdfixSettings.CONV_DIR + File.separator + id;
        File srcDir = new File(srcPath);
        if (!srcDir.exists()) {
          return false;        
        }
        webDir.mkdirs();
        // copy folder to webroot
        PdfixUtils.copyDirectory(srcDir, webDir);
      }

      //read original file name
      byte[] buffer = new byte[1024];
      FileInputStream input = new FileInputStream(webPath + File.separator + "file.txt");
      input.read(buffer);

      File origFile = new File(webPath + File.separator + "file.txt");
      String orig = PdfixUtils.readFileToString(origFile,  Charset.forName("UTF-8"));
      File nameFile = new File(webPath + File.separator + "name.txt");
      String name = PdfixUtils.readFileToString(nameFile, Charset.forName("UTF-8"));

      request.getSession().setAttribute("id", id);
      request.getSession().setAttribute("orig", orig);
      request.getSession().setAttribute("name", name);
    }
    catch (IOException e) {
      return false;
    }
    return true;

  }
  
  public static void copy(File sourceLocation, File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      copyDirectory(sourceLocation, targetLocation);
    } else {
      copyFile(sourceLocation, targetLocation);
    }
  }
 
  private static void copyDirectory(File source, File target) throws IOException {
    if (!target.exists()) {
      target.mkdirs();
    }

    for (String f : source.list()) {
      copy(new File(source, f), new File(target, f));
    }
  } 

  private static void copyFile(File source, File target) throws IOException {        
    try (
      InputStream in = new FileInputStream(source);
      OutputStream out = new FileOutputStream(target)
    ) {
      byte[] buf = new byte[1024];
      int length;
      while ((length = in.read(buf)) > 0) {
          out.write(buf, 0, length);
      }
    }
  }
  
  public static String readFileToString(File source, Charset charset) {
    StringBuffer sb = new StringBuffer();
    try (
      InputStream in = new FileInputStream(source);
    ) {
      byte[] buf = new byte[1024];
      int length;
      while ((length = in.read(buf)) > 0) {
        String decoded = new String(buf, 0, length, charset);
        sb.append(decoded);
      }
    }
    catch (IOException ex) {
    }
    
    return sb.toString();
  }


}
