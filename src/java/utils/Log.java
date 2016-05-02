/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author jozef
 */
public class Log {
  public static void write(String data) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(PdfixSettings.LOG_FILE, true));
      out.write(data + "\n");
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
