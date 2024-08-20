package br.com.plutomc.core.common.utils.string;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import br.com.plutomc.core.common.utils.supertype.OptionalBoolean;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;

public class StringFormat {
   public static String formatString(String separator, Number... numbers) {
      StringBuilder stringBuilder = new StringBuilder();

      for(int i = 0; i < numbers.length; ++i) {
         stringBuilder.append(CommonConst.DECIMAL_FORMAT.format(numbers[i].doubleValue()));
         if (i != numbers.length - 1) {
            stringBuilder.append(separator);
         }
      }

      return stringBuilder.toString().trim();
   }

   public static String formatString(Enum<?> toFormat) {
      return formatString(toFormat.name().replace("_", " "));
   }

   public static String centerString(String string) {
      return centerString(string, 154);
   }

   public static String centerString(String string, int center) {
      if (string != null && !string.equals("")) {
         string = string.replace("&", "§");
         int messagePxSize = 0;
         boolean previousCode = false;
         boolean isBold = false;

         for(char c : string.toCharArray()) {
            if (c == 167) {
               previousCode = true;
            } else if (previousCode) {
               previousCode = false;
               if (c != 'l' && c != 'L') {
                  isBold = false;
               } else {
                  isBold = true;
               }
            } else {
               DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
               messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
               ++messagePxSize;
            }
         }

         int halvedMessageSize = messagePxSize / 2;
         int toCompensate = center - halvedMessageSize;
         int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
         int compensated = 0;

         StringBuilder stringBuilder;
         for(stringBuilder = new StringBuilder(); compensated < toCompensate; compensated += spaceLength) {
            stringBuilder.append(" ");
         }

         return stringBuilder.toString() + string;
      } else {
         return "";
      }
   }

   public static String join(List<String> input, String separator) {
      if (input != null && input.size() > 0) {
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < input.size(); ++i) {
            sb.append(input.get(i));
            if (i != input.size() - 1) {
               sb.append(separator);
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   public static String join(String[] input, String separator) {
      if (input != null && input.length > 0) {
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < input.length; ++i) {
            sb.append(input[i]);
            if (i != input.length - 1) {
               sb.append(separator);
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   public static String format(int time) {
      if (time >= 3600) {
         int hours = time / 3600;
         int minutes = time % 3600 / 60;
         int seconds = time % 3600 % 60;
         return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
      } else {
         int minutes = time / 60;
         int seconds = time % 60;
         return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
      }
   }

   public static String formatTime(int time) {
      return formatTime(time, TimeFormat.SHORT);
   }

   public static String formatTime(Language language, int time) {
      return formatTime(language, time, TimeFormat.SHORT);
   }

   public static String formatTime(Language language, int time, TimeFormat timeFormat) {
      int days = time / 3600 / 24;
      int hours = time / 3600 % 24;
      int minutes = time % 3600 / 60;
      int seconds = time % 3600 % 60;
      switch(timeFormat) {
         case SIMPLIFIED:
            if (days > 0) {
               return days + " " + language.t("day") + (days == 1 ? "" : "s");
            } else if (hours > 0) {
               return hours + " " + language.t("hour") + (hours == 1 ? "" : "s");
            } else {
               if (minutes > 0) {
                  return minutes + " " + language.t("minute") + (minutes == 1 ? "" : "s");
               }

               return seconds + " " + language.t("second") + (seconds == 1 ? " " : "s");
            }
         case SHORT_SIMPLIFIED:
            if (days > 0) {
               return days + "d";
            } else if (hours > 0) {
               return hours + "h";
            } else {
               if (minutes > 0) {
                  return minutes + "m";
               }

               return seconds + "s";
            }
         case SHORT:
            return (days > 0 ? days + "d" + (hours <= 0 && minutes <= 0 && seconds <= 0 ? "" : " ") : "")
               + (hours > 0 ? hours + "h" + (seconds <= 0 && minutes <= 0 ? "" : " ") : "")
               + (minutes > 0 ? minutes + "m" + (seconds > 0 ? " " : "") : "")
               + (seconds != 0 || days <= 0 && hours <= 0 && minutes <= 0 ? seconds + "s" : "");
         case NORMAL:
            return (days > 0 ? days + " " + language.t("day") + (days == 1 ? "" : "s") + (hours <= 0 && minutes <= 0 && seconds <= 0 ? "" : " ") : "")
               + (hours > 0 ? hours + " " + language.t("hour") + (days == 1 ? "" : "s") + (seconds <= 0 && minutes <= 0 ? "" : " ") : "")
               + (minutes > 0 ? minutes + " " + language.t("minute") + (minutes == 1 ? "" : "s") + (seconds > 0 ? " " : "") : "")
               + (seconds != 0 || days <= 0 && hours <= 0 && minutes <= 0 ? seconds + " " + language.t("second") + (seconds == 1 ? "" : "s") : "");
         case DOUBLE_DOT:
            return ""
               + (hours > 0 ? (hours >= 10 ? hours : "0" + hours) + ":" : "")
               + (minutes >= 10 ? minutes : "0" + minutes)
               + ":"
               + (seconds >= 10 ? seconds : "0" + seconds);
         default:
            return "";
      }
   }

   public static String formatTime(int time, TimeFormat timeFormat) {
      return formatTime(CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage(), time, timeFormat);
   }

   public static String formatString(String string) {
      if (string.isEmpty()) {
         return string;
      } else {
         char[] stringArray = string.toLowerCase().toCharArray();
         stringArray[0] = Character.toUpperCase(stringArray[0]);
         return new String(stringArray);
      }
   }

   public static void main(String[] args) {
   }

   public static String formatToCamelCase(String string) {
      if (string.isEmpty()) {
         return string;
      } else {
         boolean camelCase = true;
         StringBuilder stringBuilder = new StringBuilder();

         for(char test : string.toCharArray()) {
            if (camelCase) {
               stringBuilder.append(Character.toUpperCase(test));
               camelCase = false;
            } else {
               if (test == ' ') {
                  camelCase = true;
               }

               stringBuilder.append(Character.toLowerCase(test));
            }
         }

         return stringBuilder.toString().trim();
      }
   }

   public static String getName(Enum<?> e) {
      String name = e.name();
      String[] names = name.split("_");

      for(int i = 0; i < names.length; ++i) {
         names[i] = i == 0 ? formatString(names[i]) : names[i].toUpperCase();
      }

      return join(names, " ");
   }

   public static String getName(String string) {
      return toReadable(string);
   }

   public static String toReadable(String string) {
      String[] names = string.split("_");

      for(int i = 0; i < names.length; ++i) {
         names[i] = names[i].substring(0, 1) + names[i].substring(1).toLowerCase();
      }

      return join(names, " ");
   }

   public static OptionalLong parseLong(String string) {
      try {
         Long integer = Long.parseLong(string);
         return OptionalLong.of(integer);
      } catch (NumberFormatException var2) {
         return OptionalLong.empty();
      }
   }

   public static OptionalLong parseLong(Object object) {
      return parseLong(String.valueOf(object));
   }

   public static OptionalInt parseInt(String string) {
      try {
         Integer integer = Integer.parseInt(string);
         return OptionalInt.of(integer);
      } catch (NumberFormatException var2) {
         return OptionalInt.empty();
      }
   }

   public static OptionalInt parseInt(Object object) {
      return parseInt(String.valueOf(object));
   }

   public static OptionalDouble parseDouble(String string) {
      try {
         Double integer = Double.parseDouble(string);
         return OptionalDouble.of(integer);
      } catch (NumberFormatException var2) {
         return OptionalDouble.empty();
      }
   }

   public static OptionalDouble parseDouble(Object object) {
      return parseDouble(String.valueOf(object));
   }

   public static OptionalBoolean parseBoolean(String string) {
      return !string.equalsIgnoreCase("true") && !string.equalsIgnoreCase("false")
         ? OptionalBoolean.empty()
         : OptionalBoolean.of(string.equalsIgnoreCase("true"));
   }

   public static OptionalBoolean parseBoolean(Language language, String string) {
      String trueString = language.t("true");
      return !string.equalsIgnoreCase("true") && !string.equalsIgnoreCase("false") && !string.equalsIgnoreCase(trueString)
         ? OptionalBoolean.empty()
         : OptionalBoolean.of(string.equalsIgnoreCase("true") || string.equalsIgnoreCase(trueString));
   }

   public static String getPositionFormat(int position) {
      return (position == 1 ? "§a" : (position == 2 ? "§e" : (position == 3 ? "§c" : "§7"))) + position + "° ";
   }

   public static String formatRomane(int level) {
      switch(level) {
         case 1:
            return "I";
         case 2:
            return "II";
         case 3:
            return "III";
         case 4:
            return "IV";
         case 5:
            return "V";
         default:
            return String.valueOf(level);
      }
   }

   public static enum DefaultFontInfo {
      A('A', 5),
      a('a', 5),
      B('B', 5),
      b('b', 5),
      C('C', 5),
      c('c', 5),
      D('D', 5),
      d('d', 5),
      E('E', 5),
      e('e', 5),
      F('F', 5),
      f('f', 4),
      G('G', 5),
      g('g', 5),
      H('H', 5),
      h('h', 5),
      I('I', 3),
      i('i', 1),
      J('J', 5),
      j('j', 5),
      K('K', 5),
      k('k', 4),
      L('L', 5),
      l('l', 1),
      M('M', 5),
      m('m', 5),
      N('N', 5),
      n('n', 5),
      O('O', 5),
      o('o', 5),
      P('P', 5),
      p('p', 5),
      Q('Q', 5),
      q('q', 5),
      R('R', 5),
      r('r', 5),
      S('S', 5),
      s('s', 5),
      T('T', 5),
      t('t', 4),
      U('U', 5),
      u('u', 5),
      V('V', 5),
      v('v', 5),
      W('W', 5),
      w('w', 5),
      X('X', 5),
      x('x', 5),
      Y('Y', 5),
      y('y', 5),
      Z('Z', 5),
      z('z', 5),
      NUM_1('1', 5),
      NUM_2('2', 5),
      NUM_3('3', 5),
      NUM_4('4', 5),
      NUM_5('5', 5),
      NUM_6('6', 5),
      NUM_7('7', 5),
      NUM_8('8', 5),
      NUM_9('9', 5),
      NUM_0('0', 5),
      EXCLAMATION_POINT('!', 1),
      AT_SYMBOL('@', 6),
      NUM_SIGN('#', 5),
      DOLLAR_SIGN('$', 5),
      PERCENT('%', 5),
      UP_ARROW('^', 5),
      AMPERSAND('&', 5),
      ASTERISK('*', 5),
      LEFT_PARENTHESIS('(', 4),
      RIGHT_PERENTHESIS(')', 4),
      MINUS('-', 5),
      UNDERSCORE('_', 5),
      PLUS_SIGN('+', 5),
      EQUALS_SIGN('=', 5),
      LEFT_CURL_BRACE('{', 4),
      RIGHT_CURL_BRACE('}', 4),
      LEFT_BRACKET('[', 3),
      RIGHT_BRACKET(']', 3),
      COLON(':', 1),
      SEMI_COLON(';', 1),
      DOUBLE_QUOTE('"', 3),
      SINGLE_QUOTE('\'', 1),
      LEFT_ARROW('<', 4),
      RIGHT_ARROW('>', 4),
      QUESTION_MARK('?', 5),
      SLASH('/', 5),
      BACK_SLASH('\\', 5),
      LINE('|', 1),
      TILDE('~', 5),
      TICK('`', 2),
      PERIOD('.', 1),
      COMMA(',', 1),
      SPACE(' ', 3),
      DEFAULT('a', 4);

      private char character;
      private int length;

      private DefaultFontInfo(char character, int length) {
         this.character = character;
         this.length = length;
      }

      public char getCharacter() {
         return this.character;
      }

      public int getLength() {
         return this.length;
      }

      public int getBoldLength() {
         return this == SPACE ? this.getLength() : this.length + 1;
      }

      public static DefaultFontInfo getDefaultFontInfo(char c) {
         for(DefaultFontInfo dFI : values()) {
            if (dFI.getCharacter() == c) {
               return dFI;
            }
         }

         return DEFAULT;
      }
   }

   public static enum TimeFormat {
      NORMAL,
      SIMPLIFIED,
      SHORT,
      SHORT_SIMPLIFIED,
      DOUBLE_DOT;
   }
}
