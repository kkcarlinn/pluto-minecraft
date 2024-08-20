package br.com.plutomc.core.common.permission;

import java.util.List;
import br.com.plutomc.core.common.CommonPlugin;

public class Group {
   private int id;
   private String groupName;
   private List<String> permissions;
   private boolean defaultGroup;
   private boolean staff;

   public String getRealPrefix() {
      return CommonPlugin.getInstance().getPluginInfo().getTagByGroup(this).getRealPrefix();
   }

   public int getId() {
      return this.id;
   }

   public String getGroupName() {
      return this.groupName;
   }

   public List<String> getPermissions() {
      return this.permissions;
   }

   public boolean isDefaultGroup() {
      return this.defaultGroup;
   }

   public boolean isStaff() {
      return this.staff;
   }

   public void setId(int id) {
      this.id = id;
   }

   public void setGroupName(String groupName) {
      this.groupName = groupName;
   }

   public void setPermissions(List<String> permissions) {
      this.permissions = permissions;
   }

   public void setDefaultGroup(boolean defaultGroup) {
      this.defaultGroup = defaultGroup;
   }

   public void setStaff(boolean staff) {
      this.staff = staff;
   }

   public Group(int id, String groupName, List<String> permissions, boolean defaultGroup, boolean staff) {
      this.id = id;
      this.groupName = groupName;
      this.permissions = permissions;
      this.defaultGroup = defaultGroup;
      this.staff = staff;
   }
}
