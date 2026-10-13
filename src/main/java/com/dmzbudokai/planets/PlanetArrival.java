package com.dmzbudokai.planets;

import com.dmzbudokai.DMZBudokai;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * DMZ's space pod arrives at the same X/Y/Z it left from, which on a mountainous planet can mean inside
 * rock or over a lava sea. When a pod arrives on a Budokai planet, move it onto solid ground near there.
 * The player is mounted on the pod right after, so they land with it.
 */
@Mod.EventBusSubscriber(modid = DMZBudokai.MOD_ID)
public final class PlanetArrival {
	private static final ResourceLocation SPACE_POD = new ResourceLocation("dragonminez", "spacepod");
	private static final int SEARCH_RADIUS = 96;
	private static final int SEARCH_STEP = 8;

	private PlanetArrival() {
	}

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)) return;
		Entity entity = event.getEntity();
		if (!SPACE_POD.equals(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()))) return;
		if (!PlanetRegistry.isPlanet(level.dimension().location().toString())) return;

		BlockPos landing = findLanding(level, entity.getBlockX(), entity.getBlockZ());
		if (landing != null) {
			entity.setPos(landing.getX() + 0.5, landing.getY() + 1.0, landing.getZ() + 0.5);
		}
	}

	/** Nearest dry, solid surface, searching outward in rings. */
	private static BlockPos findLanding(ServerLevel level, int x, int z) {
		for (int r = 0; r <= SEARCH_RADIUS; r += SEARCH_STEP) {
			for (int dx = -r; dx <= r; dx += SEARCH_STEP) {
				for (int dz = -r; dz <= r; dz += SEARCH_STEP) {
					if (r > 0 && Math.abs(dx) != r && Math.abs(dz) != r) continue; // ring only
					BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x + dx, 0, z + dz));
					BlockState below = level.getBlockState(surface.below());
					if (below.getFluidState().isEmpty() && below.isSolid()) return surface;
				}
			}
		}
		return null;
	}
}
