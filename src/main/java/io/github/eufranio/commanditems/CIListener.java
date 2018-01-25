package io.github.eufranio.commanditems;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * Created by Frani on 23/01/2018.
 */
public class CIListener {

    @Listener
    public void onItemInteract(InteractItemEvent e, @First Player p) {
        ItemStack stack = e.getItemStack().createStack();
        if (stack.toContainer().get(DataQuery.of("UnsafeData", "CommandItem")).isPresent()) {
            String uuid = stack.toContainer().get(DataQuery.of("UnsafeData", "CommandItem")).get().toString();
            if (CommandItems.getConfig().hasItem(uuid)) {
                CommandItems.getConfig().getItemFor(uuid).proccess(p, e);
            }
        }
    }

}
