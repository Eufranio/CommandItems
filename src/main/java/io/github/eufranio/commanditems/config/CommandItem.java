package io.github.eufranio.commanditems.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.eufranio.commanditems.CommandItems;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 23/01/2018.
 */
@ConfigSerializable
public class CommandItem {

    public CommandItem(){
        commands.put(1, "give %player% stone 1;console");
    }

    @Setting
    public ItemType type = ItemTypes.STONE;

    @Setting
    public String name = "&eSpetacular Stone";

    @Setting
    public List<String> lore = Lists.newArrayList(
            "&aThis is a magic item! Right click",
            "&awith it to see what happens!"
    );

    @Setting
    public Map<Integer, String> commands = Maps.newHashMap();

    @Setting
    public String permission = "commanditem.spetacularstone";

    @Setting
    public String click = "RIGHT";

    @Setting
    public int cooldown = 5;

    @Setting
    public boolean cancelOriginalAction = true;

    @Setting
    public boolean consume = true;

    public void proccess(Player p, InteractItemEvent event) {
        if (cancelOriginalAction) event.setCancelled(true);
        if (click.equals("RIGHT") && !(event instanceof InteractItemEvent.Secondary)) return;
        if (click.equals("LEFT") && !(event instanceof InteractItemEvent.Primary)) return;
        if (!p.hasPermission(this.permission)) {
            p.sendMessage(Text.of(TextColors.RED, CommandItems.getMessages().NO_PERMISSION_TO_USE_ITEM));
            return;
        }
        Instant lastUse = CommandItems.getCooldowns().getLastUse(p, this.getUUID());
        if (lastUse != null && ChronoUnit.SECONDS.between(lastUse, Instant.now()) < this.cooldown) {
            int time = (int) ChronoUnit.SECONDS.between(lastUse.plusSeconds(this.cooldown), Instant.now());
            time = time == 0 ? -1 : time;
            p.sendMessage(Text.of(TextColors.RED, CommandItems.getMessages().COOLDOWN.replace("%seconds%", ""+ -time)));
            return;
        }
        if (consume) p.getInventory().query(event.getItemStack().createStack()).poll(1);
        for (int i = 0; i < this.commands.size(); i++) {
            String[] cmd = this.commands.get(i + 1).split(";");
            CommandSource src = cmd[1].equals("console") ? Sponge.getServer().getConsole() : p;
            Sponge.getCommandManager().process(src, cmd[0].replace("%player%", p.getName()));
        }
        p.sendMessage(CommandItems.toText(CommandItems.getMessages().ITEM_USED.replace("%item%", TextSerializers.FORMATTING_CODE.serialize(CommandItems.toText(this.name)))));
        UUID uuid = p.getUniqueId();
        Task.builder()
                .delay(this.cooldown, TimeUnit.SECONDS)
                .execute(() -> CommandItems.getCooldowns().removeCooldown(uuid, this.getUUID()))
                .submit(CommandItems.getInstance());
        CommandItems.getCooldowns().addOrUpdateCooldown(p, this.getUUID());
    }

    public String getUUID() {
        return CommandItems.getConfig().getUUID(this);
    }

}
