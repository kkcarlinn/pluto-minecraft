package br.com.plutomc.core.common.utils.string;

import br.com.plutomc.core.common.CommonConst;

public class CodeCreator {
   public static final CodeCreator DEFAULT_CREATOR = new CodeCreator(12).setSpecialCharacters(false);
   public static final CodeCreator DEFAULT_CREATOR_LETTERS_ONLY = new CodeCreator(12).setNumbers(false).setSpecialCharacters(false).setUpperCase(false);
   public static final CodeCreator DEFAULT_CREATOR_SPECIAL = new CodeCreator(12);
   private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
   private static final String NUMBERS = "123456789";
   private static final String SPECIAL_CHARACTERS = "@_-()*&%$#!";
   private final int characters;
   private boolean upperCase = true;
   private boolean numbers = true;
   private boolean specialCharacters = true;

   public CodeCreator setUpperCase(boolean upperCase) {
      this.upperCase = upperCase;
      return this;
   }

   public CodeCreator setNumbers(boolean numbers) {
      this.numbers = numbers;
      return this;
   }

   public CodeCreator setSpecialCharacters(boolean specialCharacters) {
      this.specialCharacters = specialCharacters;
      return this;
   }

   public String random() {
      return this.random(this.characters);
   }

   public String random(int characters) {
      String avaiableCharacters = "abcdefghijklmnopqrstuvwxyz";
      if (this.upperCase) {
         avaiableCharacters = avaiableCharacters + "abcdefghijklmnopqrstuvwxyz".toUpperCase();
      }

      if (this.numbers) {
         avaiableCharacters = avaiableCharacters + "123456789";
      }

      if (this.specialCharacters) {
         avaiableCharacters = avaiableCharacters + "@_-()*&%$#!";
      }

      char[] chars = avaiableCharacters.toCharArray();
      StringBuilder code = new StringBuilder();

      for(int x = 1; x <= characters; ++x) {
         code.append(chars[CommonConst.RANDOM.nextInt(chars.length)]);
      }

      return code.toString();
   }

   public CodeCreator(int characters) {
      this.characters = characters;
   }

   public CodeCreator(int characters, boolean upperCase, boolean numbers, boolean specialCharacters) {
      this.characters = characters;
      this.upperCase = upperCase;
      this.numbers = numbers;
      this.specialCharacters = specialCharacters;
   }

   public int getCharacters() {
      return this.characters;
   }

   public boolean isUpperCase() {
      return this.upperCase;
   }

   public boolean isNumbers() {
      return this.numbers;
   }

   public boolean isSpecialCharacters() {
      return this.specialCharacters;
   }
}
