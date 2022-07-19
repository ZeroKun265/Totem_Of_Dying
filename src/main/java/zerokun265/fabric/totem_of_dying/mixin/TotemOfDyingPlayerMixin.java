package zerokun265.fabric.totem_of_dying.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zerokun265.fabric.totem_of_dying.TotemOfDying;
import zerokun265.fabric.totem_of_dying.item.ModItems;

@Mixin(PlayerEntity.class)
public abstract class TotemOfDyingPlayerMixin {


    public int TotemsOfDyingUsed = 0;
    private Integer PLAYER_ID;
    private void destroyTotem(ItemStack main_hand, ItemStack off_hand) {
        if(main_hand.getItem().equals(ModItems.TOTEM_OF_DYING)) {
            main_hand.setCount(0);
            TotemsOfDyingUsed++;
        }
        else if(off_hand.getItem().equals(ModItems.TOTEM_OF_DYING)) {
            off_hand.setCount(0);
            TotemsOfDyingUsed++;
        }
    }

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"))
    public void damageTotemOfDying(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack main_hand = player.getMainHandStack();
        ItemStack off_hand = player.getOffHandStack();
        if(PLAYER_ID == null) {
            PLAYER_ID = player.getId();
        }

        // If you were holding 2 totems, damage() will be called 3 times:
        // 1. On hit, we have a totem, we destroy the totem and call the function again
        // 2. Now that we used one totem we still have one, so our function still runs but the totem used counter is 1 from the prior use
        // 3. Now that both of the totems in your hands are used, the totem used counter is now 2, so we give the achievement
        if(TotemsOfDyingUsed == 2) {
            giveDoubleDeadAchievement(player);
        }

        if(main_hand.getItem().equals(ModItems.TOTEM_OF_DYING) || off_hand.getItem().equals(ModItems.TOTEM_OF_DYING)) {
            if (main_hand.getItem().equals(Items.TOTEM_OF_UNDYING) || off_hand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                // We apply blindness for a split second
                // We consume both the totems
                // Increase Death stat
                // Remove blindness
                // Give resistance. fire res, absorption and regen
                // Give advancement
                // Summon Particles
                // Play some sound
            }
            else {
                // We try destroying the totem, this will increase the Totems Counter by 1
                destroyTotem(main_hand, off_hand);
                // We damage the player to kill him, but if he has another totem(other hand) this will run again but now it will
                // set TotemsUsed to 2, so we can use it for an achievement

                MinecraftServer playerMinecraftServer = player.getServer();
                playerMinecraftServer.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, playerMinecraftServer);
                player.damage(source, 100000000.0f);
                playerMinecraftServer.getGameRules().get(GameRules.KEEP_INVENTORY).set(false, playerMinecraftServer);


            }
        }
    }

    private void giveDoubleDeadAchievement(PlayerEntity player) {
        if(!player.getWorld().isClient) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if(serverPlayer.server.getAdvancementLoader().get(new Identifier(TotemOfDying.MODID,"general/double_dead")) != null) {
                serverPlayer.getAdvancementTracker().grantCriterion(serverPlayer.server.getAdvancementLoader().get(
                        new Identifier(TotemOfDying.MODID, "general/double_dead")), "double_dead");
            }
        }
    }
}
