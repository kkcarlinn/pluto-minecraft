package br.com.plutomc.core.common.backend.data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.backend.mongodb.MongoQuery;

public interface ServerData extends Data<MongoQuery> {
   int getTime(String var1);

   long getStartTime(String var1);

   MinigameState getState(String var1);

   String getMap(String var1);

   Map<String, Map<String, String>> loadServers();

   Set<UUID> getPlayers(String var1);

   void startServer(int var1);

   void updateStatus(String var1, MinigameState var2, String var3, int var4);

   void updateStatus(MinigameState var1, String var2, int var3);

   void updateStatus(MinigameState var1, int var2);

   void updateStatus();

   void setJoinEnabled(String var1, boolean var2);

   void setJoinEnabled(boolean var1);

   void stopServer();

   void setTotalMembers(int var1);

   void joinPlayer(UUID var1, int var2);

   void leavePlayer(UUID var1, int var2);

   void sendPacket(Packet var1);

   void closeConnection();
}
