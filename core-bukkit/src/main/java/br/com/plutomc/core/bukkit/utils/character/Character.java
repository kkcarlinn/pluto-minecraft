package br.com.plutomc.core.bukkit.utils.character;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.core.bukkit.utils.character.handler.ActionHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Character {
   private static final Map<Integer, Character> OBSERVER_MAP = new HashMap<>();
   private ActionHandler interactHandler;
   private NPC npc;

   public Character(NPC npc, ActionHandler interactHandler) {
      this.npc = npc;
      this.interactHandler = interactHandler;
      registerCharacter(this);
   }

   public Character(String skinName, Location location, ActionHandler interactHandler) {
      this.npc = new NPC(location, skinName);
      this.interactHandler = interactHandler;
      registerCharacter(this);
   }

   public static Character createCharacter(String skinName, Location location) {
      return new Character(skinName, location, new ActionHandler() {
         @Override
         public boolean onInteract(Player player, boolean right) {
            return false;
         }
      });
   }

   public void show(Player player) {
      this.npc.show(player);
   }

   public void hide(Player player) {
      this.npc.hide(player);
   }

   public void remove() {
      this.getNpc().remove();
      unregisterCharacter(this);
   }

   public void teleport(Location location) {
      this.npc.teleport(location);
   }

   public boolean isShowing(UUID uniqueId) {
      return this.npc.getShowing().contains(uniqueId);
   }

   public static NPC createNpc(Location location, String skinName) {
      return new NPC(location, skinName);
   }

   public static void registerCharacter(Character character) {
      OBSERVER_MAP.put(character.getNpc().getEntityPlayer().getId(), character);
   }

   public static void unregisterCharacter(Integer id) {
      OBSERVER_MAP.remove(id);
   }

   public static void unregisterCharacter(Character character) {
      OBSERVER_MAP.remove(character.getNpc().getEntityPlayer().getId());
   }

   public static Character getCharacter(Integer id) {
      return OBSERVER_MAP.get(id);
   }

   public static Collection<Character> getCharacters() {
      return OBSERVER_MAP.values();
   }

   public ActionHandler getInteractHandler() {
      return this.interactHandler;
   }

   public NPC getNpc() {
      return this.npc;
   }
}
