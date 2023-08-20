package de.z0rdak.yawp.config.server;

import java.util.*;
import java.util.stream.*;

import net.luckperms.api.*;
import net.luckperms.api.model.group.*;
import net.luckperms.api.model.user.*;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import net.minecraft.entity.player.*;

final class LuckPermsAPI {
	
	private static LuckPerms api = null;
	
	public static final void init() {
		api = LuckPermsProvider.get();
	}
	
	public static final User getUserForPlayer(PlayerEntity p) {
		return api.getPlayerAdapter(PlayerEntity.class).getUser(p);
	}
	
	public static final boolean hasPlayerPermission(PlayerEntity p, String perm) {
		return (getUserForPlayer(p).getCachedData().getPermissionData().checkPermission(perm) == Tristate.TRUE ? true : false);
	}
	
}
