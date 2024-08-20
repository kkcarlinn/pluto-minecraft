package br.com.plutomc.core.common.utils;

import java.io.File;

public abstract class FileWatcher implements Runnable {
   private File file;
   private long lastModified;

   public FileWatcher(File file) {
      this.file = file;
      this.lastModified = file.lastModified();
   }

   @Override
   public void run() {
      if (this.lastModified != this.file.lastModified()) {
         this.onChange();
         this.lastModified = this.file.lastModified();
      }
   }

   public abstract void onChange();
}
