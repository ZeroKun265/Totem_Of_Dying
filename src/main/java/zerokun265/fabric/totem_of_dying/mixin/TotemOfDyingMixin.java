package zerokun265.fabric.totem_of_dying.mixin;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zerokun265.fabric.totem_of_dying.TotemOfDying;
import zerokun265.fabric.totem_of_dying.advancement.ModAdvancements;
import zerokun265.fabric.totem_of_dying.item.ModItems;

@Mixin(PlayerEntity.class)
public class TotemOfDyingMixin {

    public int TotemsOfDyingUsed = 0;
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

        if(TotemsOfDyingUsed == 2) {
            giveAchievement(player);
        }

        if(main_hand.getItem().equals(ModItems.TOTEM_OF_DYING) || off_hand.getItem().equals(ModItems.TOTEM_OF_DYING)) {
            if (main_hand.getItem().equals(Items.TOTEM_OF_UNDYING) || off_hand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                // Special behaviour if we also have a totem of undying
            }
            else {
                // We try destroying the totem, this will increase the Totems Counter by 1
                destroyTotem(main_hand, off_hand);
                // We damage the player to kill him, but if he has another totem(other hand) this will run again but now it will
                // set TotemsUsed to 2, so we can use it for an achievement
                player.damage(source, 100000000000f);
            }
        }
    }

    private void giveAchievement(PlayerEntity player) {
        if(!player.getWorld().isClient) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if(serverPlayer.server.getAdvancementLoader().get(new Identifier(TotemOfDying.MODID,"general/double_dead")) != null) {
                serverPlayer.getAdvancementTracker().grantCriterion(serverPlayer.server.getAdvancementLoader().get(
                        new Identifier(TotemOfDying.MODID, "general/double_dead")), "double_dead");
            }
        }
    }
}
