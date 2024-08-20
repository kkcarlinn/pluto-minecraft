package br.com.plutomc.core.bukkit.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntFunction;

import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.utils.string.Line;

public class ChatManager {
   private Map<UUID, Info> chatMap = new HashMap<>();

   public String callback(UUID uniqueId, String message, boolean cancel) {
      if (this.chatMap.containsKey(uniqueId)) {
         Info info = this.chatMap.get(uniqueId);
         if (cancel) {
            info.callback.callback(true, info.answers.stream().toArray(x$0 -> new String[x$0]));
         } else {
            info.answer(message);
            if (info.hasNextQuestion()) {
               return info.question();
            }

            info.callback.callback(false, info.answers.stream().toArray(x$0 -> new String[x$0]));
            this.chatMap.remove(uniqueId);
         }
      }

      return null;
   }

   public boolean validate(UUID uniqueId, String message) {
      if (this.chatMap.containsKey(uniqueId)) {
         Info info = this.chatMap.get(uniqueId);
         if (info.validator != null) {
            return info.validator.validate(message, info.index);
         }
      }

      return true;
   }

   public boolean isClearChat(UUID uniqueId) {
      return this.chatMap.containsKey(uniqueId) ? this.chatMap.get(uniqueId).isClearChat() : false;
   }

   public Info loadChat(CommandSender sender, Callback callback, String... questions) {
      return this.loadChat(sender, callback, null, questions);
   }

   public Info loadChat(CommandSender sender, Callback callback, Line... questions) {
      return this.loadChat(sender, callback, (Validator) null, Arrays.asList(questions).stream().map(Line::toString).toArray(String[]::new));
   }

   public Info loadChat(CommandSender sender, Callback callback, Validator validator, String... questions) {
      sender.sendMessage(questions[0]);
      Info info = new Info(callback, validator, questions);
      this.chatMap.put(sender.getUniqueId(), info);
      return info;
   }

   public Info loadChat(CommandSender sender, Callback callback, Validator validator, Line... questions) {
      return this.loadChat(sender, callback, validator, Arrays.asList(questions).stream().map(Line::toString).toArray(String[]::new));
   }

   public String getAnswers(UUID uniqueId, int index) {
      return this.chatMap.get(uniqueId).getAnswers().get(index);
   }

   public boolean containsChat(UUID uniqueId) {
      return this.chatMap.containsKey(uniqueId);
   }

   public void remove(UUID uniqueId) {
      this.chatMap.remove(uniqueId);
   }

   public interface Callback {
      void callback(boolean var1, String... var2);
   }

   public class Info {
      private final Callback callback;
      private final Validator validator;
      private final String[] questions;
      private List<String> answers = new ArrayList<>();
      private int index = 0;
      private boolean clearChat;
      private boolean stopChat;

      public boolean hasNextQuestion() {
         return this.index < this.questions.length;
      }

      public String question() {
         return this.questions[this.index];
      }

      public void answer(String answer) {
         this.answers.add(answer);
         ++this.index;
      }

      public Info clearChat() {
         this.clearChat = !this.clearChat;
         return this;
      }

      public Info stopChat() {
         this.stopChat = !this.stopChat;
         return this;
      }

      public Callback getCallback() {
         return this.callback;
      }

      public Validator getValidator() {
         return this.validator;
      }

      public String[] getQuestions() {
         return this.questions;
      }

      public List<String> getAnswers() {
         return this.answers;
      }

      public int getIndex() {
         return this.index;
      }

      public boolean isClearChat() {
         return this.clearChat;
      }

      public boolean isStopChat() {
         return this.stopChat;
      }

      public Info(Callback callback, Validator validator, String[] questions) {
         this.callback = callback;
         this.validator = validator;
         this.questions = questions;
      }
   }

   public interface Validator {
      boolean validate(String var1, int var2);
   }
}
