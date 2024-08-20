package br.com.plutomc.core.common.utils.string;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

public class MessageBuilder {
   private String message;
   private boolean hoverable;
   private HoverEvent hoverEvent;
   private boolean clickable;
   private ClickEvent clickEvent;
   private List<TextComponent> componentList;

   public MessageBuilder(String message) {
      this.message = message;
      this.componentList = new ArrayList<>();
   }

   public MessageBuilder setMessage(String message) {
      this.message = message;
      return this;
   }

   public MessageBuilder setHoverable(boolean hoverable) {
      this.hoverable = hoverable;
      return this;
   }

   public MessageBuilder setHoverEvent(HoverEvent hoverEvent) {
      this.hoverEvent = hoverEvent;
      this.hoverable = true;
      return this;
   }

   public MessageBuilder setHoverEvent(Action action, String text) {
      this.hoverEvent = new HoverEvent(action, TextComponent.fromLegacyText(text));
      this.hoverable = true;
      return this;
   }

   public MessageBuilder setHoverEvent(String text) {
      this.hoverEvent = new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(text));
      this.hoverable = true;
      return this;
   }

   public MessageBuilder setClickable(boolean clickable) {
      this.clickable = clickable;
      return this;
   }

   public MessageBuilder setClickEvent(ClickEvent clickEvent) {
      this.clickEvent = clickEvent;
      this.clickable = true;
      return this;
   }

   public MessageBuilder setClickEvent(ClickEvent.Action action, String text) {
      this.clickEvent = new ClickEvent(action, text);
      this.clickable = true;
      return this;
   }

   public MessageBuilder setClickEvent(String text) {
      this.clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, text);
      this.clickable = true;
      return this;
   }

   public MessageBuilder extra(String message) {
      this.componentList.add(new TextComponent(message));
      return this;
   }

   public MessageBuilder extra(TextComponent textComponent) {
      this.componentList.add(textComponent);
      return this;
   }

   public MessageBuilder extra(List<TextComponent> extra) {
      this.componentList.addAll(extra);
      return this;
   }

   public TextComponent create() {
      TextComponent textComponent = new TextComponent(this.message);
      if (this.hoverable) {
         textComponent.setHoverEvent(this.hoverEvent);
      }

      if (this.clickable) {
         textComponent.setClickEvent(this.clickEvent);
      }

      for(TextComponent text : this.componentList) {
         textComponent.addExtra(text);
      }

      return textComponent;
   }

   public String getMessage() {
      return this.message;
   }

   public boolean isHoverable() {
      return this.hoverable;
   }

   public HoverEvent getHoverEvent() {
      return this.hoverEvent;
   }

   public boolean isClickable() {
      return this.clickable;
   }

   public ClickEvent getClickEvent() {
      return this.clickEvent;
   }

   public List<TextComponent> getComponentList() {
      return this.componentList;
   }
}
