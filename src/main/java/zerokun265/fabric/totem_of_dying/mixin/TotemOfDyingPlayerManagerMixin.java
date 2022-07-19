package zerokun265.fabric.totem_of_dying.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


// Whenever a player wants to respawn, keep inventory is enabled and at the end disabled
// This will cause players that actually died with keep inventory on (Like the o nes that used the totem)
// To still respawn with their item
// Those who die without keep inventory on instead don't
// This currently makes the mod not compatible with keep-inventory, but I mean, if you're adding this mod
// You probably don't use keep inventory, and I'm too lazy to add compatibility
@Mixin(PlayerManager.class)
abstract class TotemOfDyingPlayerManagerMixin {
    @Inject(method = "respawnPlayer", at = @At("HEAD"))
    private void respawnTotem(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        MinecraftServer playerMinecraftServer = oldPlayer.getServer();
        playerMinecraftServer.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, playerMinecraftServer);
    }

    @Inject(method = "respawnPlayer", at = @At("TAIL"))
    private void respawnTotemAfter(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        MinecraftServer playerMinecraftServer = oldPlayer.getServer();
        playerMinecraftServer.getGameRules().get(GameRules.KEEP_INVENTORY).set(false, playerMinecraftServer);
    }
}
