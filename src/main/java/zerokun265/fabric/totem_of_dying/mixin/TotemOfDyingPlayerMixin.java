package zerokun265.fabric.totem_of_dying.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zerokun265.fabric.totem_of_dying.TotemOfDying;
import zerokun265.fabric.totem_of_dying.item.ModItems;
import net.minecraft.text.Style;


@Mixin(PlayerEntity.class)
public abstract class TotemOfDyingPlayerMixin {


    @Shadow public abstract Text getDisplayName();

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

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
    private void destroyTotemUndying(ItemStack main_hand, ItemStack off_hand) {
        if(main_hand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
            main_hand.setCount(0);
            TotemsOfDyingUsed++;
        }
        else if(off_hand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
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
            giveAchievement(player, "double_dead");
        }

        if(main_hand.getItem().equals(ModItems.TOTEM_OF_DYING) || off_hand.getItem().equals(ModItems.TOTEM_OF_DYING)) {
            if (main_hand.getItem().equals(Items.TOTEM_OF_UNDYING) || off_hand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 50, 255));
                destroyTotem(main_hand, off_hand);
                // TotemsOfDyingUsed usually gets reset when you get killed by the damage method
                // However since here we actually do not kill, we need to reset it, or we'll keep increasing it and
                // give the achievement double dead even tho 2 totems were used one after another and not together
                TotemsOfDyingUsed = 0;
                destroyTotemUndying(main_hand, off_hand);
                player.incrementStat(Stats.DEATHS);

                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1800, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 450, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 4));

                giveAchievement(player, "between_life_and_death");
                // NOTE: Color is the integer form of any HEX Color code, in this case #BA0F29 -> 12193577 a darkish red
                player.getServer().getPlayerManager().broadcast(Text.literal(player.getDisplayName().getString() + " ").append(
                        Text.translatable("totem_of_dying.messages.dead_and_back_alive")).setStyle(Style.EMPTY.withColor(12193577)), false);

                player.getWorld().addBlockBreakParticles(player.getBlockPos().down(1), player.getWorld().getBlockState(player.getBlockPos().down(1)));

                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                serverPlayer.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));

                // This is to increase the stats of totems used
                ItemStack item;
                if(main_hand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                    item = main_hand;
                } else {
                    item = off_hand;
                }
                Criteria.USED_TOTEM.trigger(serverPlayer, item);
                if(!player.getWorld().isClient) {
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1f, 1f);

                }
            }
            else {
                // We try destroying the totem, this will increase the Totems Counter by 1
                destroyTotem(main_hand, off_hand);
                // We damage the player to kill him, but if he has another totem(other hand) this will run again, but now it will
                // set TotemsUsed to 2, so we can use it for an achievement

                MinecraftServer playerMinecraftServer = player.getServer();
                playerMinecraftServer.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, playerMinecraftServer);
                player.damage(source, 100000000.0f);
                playerMinecraftServer.getGameRules().get(GameRules.KEEP_INVENTORY).set(false, playerMinecraftServer);

            }
        }
    }

    private void giveAchievement(PlayerEntity player, String name) {
        if(!player.getWorld().isClient) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if(serverPlayer.server.getAdvancementLoader().get(new Identifier(TotemOfDying.MODID,"general/" + name)) != null) {
                serverPlayer.getAdvancementTracker().grantCriterion(serverPlayer.server.getAdvancementLoader().get(
                        new Identifier(TotemOfDying.MODID, "general/"+name)), name);
            }
        }
    }
}
