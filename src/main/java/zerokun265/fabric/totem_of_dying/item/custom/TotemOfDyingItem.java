package zerokun265.fabric.totem_of_dying.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import zerokun265.fabric.totem_of_dying.TotemOfDying;
import zerokun265.fabric.totem_of_dying.item.ModItems;

import java.util.logging.Logger;

public class TotemOfDyingItem extends Item {
    // TODO: Mixin into the Item class and make it so if the item is a totem we call reset_stats
    public float getHolders_health() {
        return holders_health;
    }

    public void setHolders_health(float holders_health) {
        this.holders_health = holders_health;
    }

    private float holders_health;
    private boolean is_held = false;
    private boolean is_health_set = false;

    public TotemOfDyingItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
    // TODO: redo the entire thing *sigh* There is a bug that makes it so if you have 2 totems in your inventory they don't work
        // So it needs to be made into a mixin for some method of the LivingEntity i guess

        // If a player is holding the totem..
        if(entity.isPlayer()) {
            PlayerEntity player = (PlayerEntity) entity;
            is_held = selected || player.getOffHandStack().getItem().equals(ModItems.TOTEM_OF_DYING);
            if(is_held) {
                if(!is_health_set){
                    setHolders_health(player.getHealth());
                    is_health_set = true;
                }
                if (getHolders_health() > player.getHealth()) {
                    // TODO: Destroy the item once player is dead
                    player.getOffHandStack().setCount(0);
                    player.getMainHandStack().setCount(0);
                    player.kill();
                }

            }
            if(!is_held){
                reset_stats();
            }

        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private void reset_stats() {
        setHolders_health(0f);
        is_health_set = false;
    }


}
