package br.com.plutomc.core.common.utils.string;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;

public class Line {
   private List<String> lines = new ArrayList<>();

   public Line line(String line) {
      this.lines.add(line.isEmpty() ? "§f" : line);
      return this;
   }

   @Override
   public String toString() {
      return Joiner.on('\n').join(this.lines);
   }

   public static Line create() {
      return new Line();
   }
}
