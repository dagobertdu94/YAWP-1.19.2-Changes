package de.z0rdak.yawp.config.server;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.z0rdak.yawp.YetAnotherWorldProtector;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.stream.Collectors;

public class CommandPermissionConfig {

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final String CONFIG_NAME = YetAnotherWorldProtector.MODID + "-common.toml";
    // TODO: Dedicated permission to teleport to region
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_READ_ONLY_CMDS;
    public static final ForgeConfigSpec.ConfigValue<Integer> REQUIRED_OP_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Boolean> COMMAND_BLOCK_EXECUTION;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PLAYERS_WITH_PERMISSION;
    
    public static final ForgeConfigSpec.ConfigValue<Boolean> USE_LUCKPERMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_BYPASS;
    
    public static final ForgeConfigSpec.ConfigValue<Integer> WP_COMMAND_ALTERNATIVE;
    public static final String[] WP_CMDS = new String[]{"wp", "yawp"};
    public static String BASE_CMD = "wp";
    private static MinecraftServer serverInstance;

    static {
        final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.push("YetAnotherWorldProtector mod server configuration").build();
        
        USE_LUCKPERMS = BUILDER.comment("Uses LuckPerms instead of the vanilla logic.", "LuckPerms must be present otherwise this will ignored!")
        		.define("use_luckperms", true);
        
        ALLOW_BYPASS = BUILDER.comment("Allows players that are operator with the required OP level or are present in the UUID list of allowed players (vanilla logic) to bypass all flag check events.",
        		"If LuckPerms is present, bypassing is allowed if the permission yawp.admin is given to the target player.")
        		.define("allow_bypassing_flag_events", true);
        
        
        

        COMMAND_BLOCK_EXECUTION = BUILDER.comment("Permission for command blocks to execute mod commands")
                .define("command_block_execution", true);

        WP_COMMAND_ALTERNATIVE = BUILDER.comment("Default command alternative used in quick commands in chat.\nThis is only important if another mod uses the /wp command (like Journey Map). Defaults to 0.\n" +
                        " 0 -> /wp\n 1 -> /yawp")
                .defineInRange("wp_root_command", 0, 0, 1);

        REQUIRED_OP_LEVEL = BUILDER.comment("Minimum OP level to use mod commands.\n 0 -> everyone can use the commands.\n 1-4 -> OP with specific level can use the commands.\n 5 -> no operator can use the commands.")
                .defineInRange("command_op_level", 4, 0, 5);

        ALLOW_READ_ONLY_CMDS = BUILDER.comment("Defines whether info commands for regions can be used by every player.")
                .define("allow_info_cmds", true);

        PLAYERS_WITH_PERMISSION = BUILDER.comment("Player UUIDs with permission to use mod commands.\n Make sure to put the UUIDs in parentheses, just like a normal string.\n Example: players_with_permission = [\"614c9eac-11c9-3ca6-b697-938355fa8235\", \"b9f5e998-520a-3fa2-8208-0c20f22aa20f\"]")
                .defineListAllowEmpty(Collections.singletonList("players_with_permission"), ArrayList::new, (uuid) -> {
                    if (uuid instanceof String) {
                        try {
                            String uuidStr = (String) uuid;
                            if (uuidStr.length() != 36) {
                                throw new IllegalArgumentException("Invalid UUID - wrong length");
                            }
                            List<String> uuidTokens = Arrays.asList(uuidStr.split("-"));
                            List<String> shortTokens = uuidTokens.subList(1, 3);
                            if (uuidTokens.get(0).length() != 8 || containsBadLength(shortTokens, 4) || uuidTokens.get(4).length() != 12) {
                                throw new IllegalArgumentException("Invalid UUID - wrong token sizes");
                            }
                            return true;
                        } catch (IllegalArgumentException e) {
                            YetAnotherWorldProtector.LOGGER.warn("Invalid UUID '" + uuid + "' in config");
                            return false;
                        }
                    }
                    return false;
                });
        BUILDER.pop();
        CONFIG_SPEC = BUILDER.build();
    }

    private static boolean containsBadLength(List<String> tokens, int size) {
        return tokens.stream().anyMatch(t -> t.length() != size);
    }

    public static boolean AllowInfoCmds() {
        return ALLOW_READ_ONLY_CMDS.get();
    }

    public static Set<String> _UUIDsWithPermission() {
        return PLAYERS_WITH_PERMISSION.get()
                .stream()
                .filter(Objects::nonNull)
                .map(s -> (String) s)
                .collect(Collectors.toSet());
    }

    // FIXME: What about CommandBlockMinecarts?
    public static boolean check(ServerCommandSource source, String perm) {
        try {
            return checkForPermissionOrVanillaLogic(source.getPlayerOrThrow(), perm);
        } catch (CommandSyntaxException e) {
            boolean isServerConsole = source.getName().equals("Server");
            if (isServerConsole) {
                return true;
            } else {
                return (isCommandBlock(source) ? COMMAND_BLOCK_EXECUTION.get() : false);
            }
        }
    }

    public static boolean checkForVanillaLogic(PlayerEntity player) {
        return hasUUIDConfigEntry(player) || hasNeededOpLevel(player) || player.hasPermissionLevel(REQUIRED_OP_LEVEL.get());
    }

    public static boolean hasUUIDConfigEntry(PlayerEntity player) {
        Set<String> playersInConfig = _UUIDsWithPermission();
        return playersInConfig.contains(player.getUuidAsString());
    }


    public static void initServerInstance(MinecraftServer server) {
        serverInstance = server;
    }

    public static boolean hasNeededOpLevel(PlayerEntity player) {
    	return serverInstance.getPermissionLevel(player.getGameProfile()) >= REQUIRED_OP_LEVEL.get();
    }
    
    public static boolean isCommandBlock(ServerCommandSource src) {
    	final ServerWorld w = src.getWorld();
    	
    	if (w.getBlockState(new BlockPos(src.getPosition())).getBlock() == Blocks.COMMAND_BLOCK)
    		return true;
    	if (w.getNonSpectatingEntities(CommandBlockMinecartEntity.class, Box.from(src.getPosition())).size() > 0)
    		return true;
    	return false;
    }
    
    public static boolean isUsingLuckPerms() {
    	return LuckPermsReflector.isLuckPermsAccessible();
    }
    
    private static boolean hasPermission(PlayerEntity p, String perm) {
    	if (LuckPermsReflector.hasPermission(p, "yawp.admin"))
    		return true;
    	
    	return LuckPermsReflector.hasPermission(p, perm);
    }
    
    public static boolean hasAdminPermission(PlayerEntity p) {
    	return hasPermission(p, "yawp.admin");
    }
    
    public static boolean canBypassFlagCheck(PlayerEntity p) {
    	if (isUsingLuckPerms()) {
    		return hasAdminPermission(p);
    	} else {
    		return (checkForVanillaLogic(p));
    	}
    }
    
    public static boolean checkForPermissionOrVanillaLogic(PlayerEntity p, String perm) {
    	if (isUsingLuckPerms() && perm != null) {
    		return hasPermission(p, perm);
    	} else {
    		return (checkForVanillaLogic(p));
    	}
    }
    
}
