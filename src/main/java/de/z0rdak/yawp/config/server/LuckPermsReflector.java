package de.z0rdak.yawp.config.server;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.*;

final class LuckPermsReflector {
	
	static {
		init();
	}
	
	private static final void init() {
		getLuckPermsClass().ifPresent((cls) -> {
			try {
				cls.getMethod("init", new Class[0]).invoke(null, new Object[0]);
			} catch (Throwable e) {}
		});
	}
	
	private static final Optional<Class<?>> getLuckPermsClass() {
		try {
			return Optional.ofNullable(Class.forName("de.z0rdak.yawp.config.server.LuckPermsAPI"));
		} catch (Throwable e) {
			return Optional.empty();
		}
	}
	
	public static final boolean isLuckPermsLoaded() {
		return FabricLoader.getInstance().isModLoaded("luckperms");
	}
	
	public static final boolean isLuckPermsAccessible() {
		return (CommandPermissionConfig.USE_LUCKPERMS.get() == true && isLuckPermsLoaded() && getLuckPermsClass().isPresent());
	}
	
	public static final boolean hasPermission(PlayerEntity p, String perm) {
		return getLuckPermsClass().map((cls) -> {
			try {
				return (boolean)cls.getMethod("hasPlayerPermission", PlayerEntity.class, String.class).invoke(null, p, perm);
			} catch (Throwable e) {
				return null;
			}
		}).orElse(false);
	}
	
}
