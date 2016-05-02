////////////////////////////////////////////////////////////////////////////////////////////////////
// PdfHtmlSettings.java
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////
package net.pdfix.html;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Class PdfHtmlSettings 
////////////////////////////////////////////////////////////////////////////////////////////////////
public class PdfHtmlSettings {

  public String htmlPath = "/";
  public String imgDir = "images/";
  public String jsDir = "js/";
  public String rootPath = "";
  public boolean acroForm = true;
  public boolean responsiveLayout = false;
  public int targetWidth = 900;

  public PdfHtmlSettings Clone() {
    PdfHtmlSettings settings = new PdfHtmlSettings();
    settings.htmlPath = this.htmlPath;
    settings.imgDir = this.imgDir;
    settings.jsDir = this.jsDir;
    settings.rootPath = this.rootPath;
    settings.acroForm = this.acroForm;
    settings.responsiveLayout = this.responsiveLayout;
    settings.targetWidth = this.targetWidth;
    return settings;
  }
}
