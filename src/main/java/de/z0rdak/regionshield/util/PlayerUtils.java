package de.z0rdak.regionshield.util;

import de.z0rdak.regionshield.config.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.OpEntry;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.IOException;
import java.util.List;

public final class PlayerUtils {

    private final static OkHttpClient client = new OkHttpClient();

    public static MCPlayerInfo queryPlayerUUIDByName(String playerName){
        Request request = new Request.Builder()
                .url("https://api.mojang.com/users/profiles/minecraft/" + playerName)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if(response.code() == 200) {
                String json = response.body().string();
                MCPlayerInfo player = new MCPlayerInfo(); // new Gson().fromJson(json, MCPlayerInfo.class);
                player.playerUUID = json.substring(json.indexOf("\"id\":\"") + 6, json.length() - 2);
                player.playerUUID = toUUIDWithHyphens(player.playerUUID);
                player.playerName = json.substring(json.indexOf("\"name\":\"") + 8, json.indexOf("\"id\":\"") - 2);
                return player;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toUUIDWithHyphens(String uuidWithoutHyphens){
        return java.util.UUID.fromString(uuidWithoutHyphens
                        .replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5")
        ).toString();
    }

    public static boolean hasPermission(PlayerEntity player){
        return hasConfigPermission(player);
    }

    public static boolean hasConfigPermission(PlayerEntity player){
        List<String> playersInConfig = (List<String>) ServerConfigBuilder.PLAYERS_WITH_PERMISSION.get();
        return playersInConfig.contains(player.getStringUUID());
    }

    public static boolean hasNeededOpLevel(PlayerEntity player) {
        OpEntry opPlayerEntry = ServerLifecycleHooks.getCurrentServer()
                .getPlayerList()
                .getOps()
                .get(player.getGameProfile());
        if (opPlayerEntry != null) {
            return opPlayerEntry.getLevel() >= ServerConfigBuilder.RS_CMD_OP_LEVEL.get();
        }
        return false;
    }

    public static class MCPlayerInfo {
        public String playerName;
        public String playerUUID;
    }
}
