package io.github.eufranio.commanditems.managers;

import io.github.eufranio.commanditems.CommandItems;
import io.github.eufranio.commanditems.config.CommandItem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ItemManager {

    CommandItems plugin;
    public ItemManager(CommandItems plugin) {
        this.plugin = plugin;
    }

    public CommandItem getItem(String name) {
        return plugin.config.get().items.get(name);
    }

    public void createItem(String name) {
        plugin.config.get().items.putIfAbsent(name == null ? UUID.randomUUID().toString() : name, new CommandItem());
        plugin.config.save();
    }

    public boolean useItem(CommandItem item, String name, Player player, boolean rightClick) {
        if (item.click.equals("RIGHT") && !rightClick) return false;
        if (item.click.equals("LEFT") && rightClick) return false;
        if (!player.hasPermission(item.permission)) {
            player.sendMessage(Text.of(TextColors.RED, plugin.config.get().messages.NO_PERMISSION_TO_USE_ITEM));
            return false;
        }

        Instant now = Instant.now();
        Instant lastUse = plugin.storage.getLastUse(player.getUniqueId(), name);
        if (lastUse != null && ChronoUnit.SECONDS.between(lastUse, now) < item.cooldown) {
            int time = (int) ChronoUnit.SECONDS.between(lastUse.plusSeconds(item.cooldown), now);
            time = time == 0 ? -1 : time;
            player.sendMessage(Text.of(
                    TextColors.RED, plugin.config.get().messages.COOLDOWN.replace("%seconds%", ""+ -time))
            );
            return false;
        }

        item.commands.forEach(command -> {
            String[] cmd = command.split(";");
            CommandSource src = cmd[1].equals("console") ? Sponge.getServer().getConsole() : player;
            Sponge.getCommandManager().process(src, cmd[0].replace("%player%", player.getName()));
        });

        player.sendMessage(CommandItems.toText(
                plugin.config.get().messages.ITEM_USED.replace("%item%",
                        TextSerializers.FORMATTING_CODE.serialize(CommandItems.toText(item.displayName)))
            )
        );

        UUID uuid = player.getUniqueId();
        Task.builder()
                .delay(item.cooldown, TimeUnit.SECONDS)
                .execute(() -> plugin.storage.removeCooldown(uuid, name))
                .submit(plugin);

        plugin.storage.updateLastUsed(uuid, name);
        return true;
    }

}
