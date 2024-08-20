package br.com.plutomc.core.bukkit.utils.permission.injector.regexperms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.plutomc.core.bukkit.utils.permission.PermissionManager;
import br.com.plutomc.core.bukkit.utils.permission.injector.FieldReplacer;
import br.com.plutomc.core.bukkit.utils.permission.injector.PermissionCheckResult;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissiblePEX extends PermissibleBase {
   private static final FieldReplacer<PermissibleBase, Map> PERMISSIONS_FIELD = new FieldReplacer<>(PermissibleBase.class, "permissions", Map.class);
   private static final FieldReplacer<PermissibleBase, List> ATTACHMENTS_FIELD = new FieldReplacer<>(PermissibleBase.class, "attachments", List.class);
   private static final Method CALC_CHILD_PERMS_METH;
   private final Map<String, PermissionAttachmentInfo> permissions;
   private final List<PermissionAttachment> attachments;
   private static final AtomicBoolean LAST_CALL_ERRORED;
   protected final Player player;
   protected final PermissionManager plugin;
   private Permissible previousPermissible = null;
   protected final Map<String, PermissionCheckResult> cache = new ConcurrentHashMap<>();
   private final Object permissionsLock = new Object();

   public PermissiblePEX(Player player, PermissionManager plugin) {
      super(player);
      this.player = player;
      this.plugin = plugin;
      this.permissions = new LinkedHashMap<String, PermissionAttachmentInfo>() {
         private static final long serialVersionUID = 1L;

         public PermissionAttachmentInfo put(String k, PermissionAttachmentInfo v) {
            PermissionAttachmentInfo existing = this.get(k);
            return existing != null ? existing : super.put(k, v);
         }
      };
      PERMISSIONS_FIELD.set(this, this.permissions);
      this.attachments = ATTACHMENTS_FIELD.get(this);
      this.recalculatePermissions();
   }

   public Permissible getPreviousPermissible() {
      return this.previousPermissible;
   }

   public void setPreviousPermissible(Permissible previousPermissible) {
      this.previousPermissible = previousPermissible;
   }

   @Override
   public boolean hasPermission(String permission) {
      PermissionCheckResult res = this.permissionValue(permission);
      switch(res) {
         case TRUE:
         case FALSE:
            return res.toBoolean();
         case UNDEFINED:
         default:
            if (super.isPermissionSet(permission)) {
               boolean ret = super.hasPermission(permission);
               return ret;
            } else {
               Permission perm = this.player.getServer().getPluginManager().getPermission(permission);
               return perm == null ? Permission.DEFAULT_PERMISSION.getValue(this.player.isOp()) : perm.getDefault().getValue(this.player.isOp());
            }
      }
   }

   @Override
   public boolean hasPermission(Permission permission) {
      PermissionCheckResult res = this.permissionValue(permission.getName());
      switch(res) {
         case TRUE:
         case FALSE:
            return res.toBoolean();
         case UNDEFINED:
         default:
            return super.isPermissionSet(permission.getName()) ? super.hasPermission(permission) : permission.getDefault().getValue(this.player.isOp());
      }
   }

   @Override
   public void recalculatePermissions() {
      if (this.cache != null && this.permissions != null && this.attachments != null) {
         synchronized(this.permissionsLock) {
            this.clearPermissions();
            this.cache.clear();
            ListIterator<PermissionAttachment> it = this.attachments.listIterator(this.attachments.size());

            while(it.hasPrevious()) {
               PermissionAttachment attach = it.previous();
               this.calculateChildPerms(attach.getPermissions(), false, attach);
            }

            for(Permission p : this.player.getServer().getPluginManager().getDefaultPermissions(this.isOp())) {
               this.permissions.put(p.getName(), new PermissionAttachmentInfo(this.player, p.getName(), null, true));
               this.calculateChildPerms(p.getChildren(), false, null);
            }
         }
      }
   }

   protected void calculateChildPerms(Map<String, Boolean> children, boolean invert, PermissionAttachment attachment) {
      try {
         CALC_CHILD_PERMS_METH.invoke(this, children, invert, attachment);
      } catch (IllegalAccessException var5) {
      } catch (InvocationTargetException var6) {
         throw new RuntimeException(var6);
      }
   }

   @Override
   public boolean isPermissionSet(String permission) {
      return super.isPermissionSet(permission) || this.permissionValue(permission) != PermissionCheckResult.UNDEFINED;
   }

   @Override
   public Set<PermissionAttachmentInfo> getEffectivePermissions() {
      synchronized(this.permissionsLock) {
         return new LinkedHashSet<>(this.permissions.values());
      }
   }

   private PermissionCheckResult checkSingle(String expression, String permission, boolean value) {
      return this.plugin.getPermissionMatcher().isMatches(expression, permission) ? PermissionCheckResult.fromBoolean(value) : PermissionCheckResult.UNDEFINED;
   }

   protected PermissionCheckResult permissionValue(String permission) {
      try {
         Validate.notNull(permission, "Permissions being checked must not be null!");
         permission = permission.toLowerCase();
         PermissionCheckResult res = this.cache.get(permission);
         if (res != null) {
            return res;
         } else {
            res = PermissionCheckResult.UNDEFINED;
            synchronized(this.permissionsLock) {
               for(PermissionAttachmentInfo pai : this.permissions.values()) {
                  if ((res = this.checkSingle(pai.getPermission(), permission, pai.getValue())) != PermissionCheckResult.UNDEFINED) {
                     break;
                  }
               }
            }

            if (res == PermissionCheckResult.UNDEFINED) {
               for(Entry<String, Boolean> ent : this.plugin.getRegexPerms().getPermissionList().getParents(permission)) {
                  if ((res = this.permissionValue(ent.getKey())) != PermissionCheckResult.UNDEFINED) {
                     res = PermissionCheckResult.fromBoolean(!(res.toBoolean() ^ ent.getValue()));
                     break;
                  }
               }
            }

            this.cache.put(permission, res);
            LAST_CALL_ERRORED.set(false);
            return res;
         }
      } catch (Throwable var8) {
         if (LAST_CALL_ERRORED.compareAndSet(false, true)) {
            var8.printStackTrace();
         }

         return PermissionCheckResult.UNDEFINED;
      }
   }

   static {
      try {
         CALC_CHILD_PERMS_METH = PermissibleBase.class.getDeclaredMethod("calculateChildPermissions", Map.class, Boolean.TYPE, PermissionAttachment.class);
      } catch (NoSuchMethodException var1) {
         throw new ExceptionInInitializerError(var1);
      }

      CALC_CHILD_PERMS_METH.setAccessible(true);
      LAST_CALL_ERRORED = new AtomicBoolean(false);
   }
}
