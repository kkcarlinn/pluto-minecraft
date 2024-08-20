package br.com.plutomc.core.common.command;

import br.com.plutomc.core.common.member.Member;

public abstract class CommandArgs {
   private final CommandSender sender;
   private final String label;
   private final String[] args;

   protected CommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
      String[] modArgs = new String[args.length - subCommand];
      System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);
      StringBuilder buffer = new StringBuilder();
      buffer.append(label);

      for(int x = 0; x < subCommand; ++x) {
         buffer.append(".").append(args[x]);
      }

      String cmdLabel = buffer.toString();
      this.sender = sender;
      this.label = cmdLabel;
      this.args = modArgs;
   }

   public Member getSenderAsMember() {
      return Member.class.cast(this.sender);
   }

   public <T extends Member> T getSenderAsMember(Class<T> t) {
      return t.cast(this.sender);
   }

   public CommandSender getSender() {
      return this.sender;
   }

   public String getLabel() {
      return this.label;
   }

   public String[] getArgs() {
      return this.args;
   }

   public abstract boolean isPlayer();
}
