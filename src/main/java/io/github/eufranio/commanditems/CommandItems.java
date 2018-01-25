package io.github.eufranio.commanditems;

import com.google.inject.Inject;
import io.github.eufranio.commanditems.config.*;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(
        id = "commanditems",
        name = "CommandItems",
        description = "Item based commands, permissions, cooldowns and more!",
        authors = {
                "Eufranio"
        }
)
public class CommandItems {

    private static CommandItems instance;

    @Inject
    public Logger logger;

    @Inject
    public GuiceObjectMapperFactory mapper;

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    private ConfigManager<ItemsConfig> itemsConfig;
    private ConfigManager<CooldownStorage> cooldowns;
    private ConfigManager<MessagesConfig> messages;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        instance = this;
        itemsConfig = ConfigManager.of(ItemsConfig.class, configDir, "CommandItems.conf", mapper, false, this);
        cooldowns = ConfigManager.of(CooldownStorage.class, configDir, "Cooldowns.conf", mapper, true, this);
        messages = ConfigManager.of(MessagesConfig.class, configDir, "Messages.conf", mapper, false, this);

        logger.info("CommandItems is starting!");

        CommandSpec newKey = CommandSpec.builder()
                .permission("commanditems.newkey")
                .executor((sender, context) -> {
                    getConfig().createEmptyItem();
                    itemsConfig.reload();
                    return CommandResult.success();
                }).build();

        CommandSpec give = CommandSpec.builder()
                .permission("commanditems.give")
                .arguments(GenericArguments.string(Text.of("uuid")), GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .executor((sender, context) -> {
                    Player player = null;
                    if (context.<Player>getOne("player").isPresent()) {
                        player = context.<Player>getOne("player").get();
                    } else if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        throw new CommandException(Text.of("You must specifiy a player when running this command from console!"));
                    }
                    String uuid = context.<String>getOne("uuid").get();
                    CommandItem item = getConfig().getItemFor(uuid);
                    if (item != null) {
                        ItemStack stack = item.type.getTemplate().createStack();
                        stack.offer(Keys.DISPLAY_NAME, toText(item.name));
                        stack.offer(Keys.ITEM_LORE, item.lore.stream().map(CommandItems::toText).collect(Collectors.toList()));
                        stack = ItemStack.builder().fromContainer(stack.toContainer().set(DataQuery.of("UnsafeData", "CommandItem"), uuid)).build();
                        player.getInventory().offer(stack);
                        player.sendMessage(toText(getMessages().YOU_GOT_A_ITEM.replace("%item%", TextSerializers.FORMATTING_CODE.serialize(toText(item.name)))));
                    }
                    return CommandResult.success();
                }).build();

        CommandSpec cmd = CommandSpec.builder()
                .permission("commanditems.main")
                .executor((sender, context) -> {
                    sender.sendMessage(Text.of(
                            Text.NEW_LINE,
                            TextColors.GREEN, "    /citems give <uuid> [player]",
                            TextColors.GRAY, " > ",
                            TextColors.GRAY, "Gives yourself (or [player]) the item associated to this UUID, from the config",
                            Text.NEW_LINE,
                            TextColors.GREEN, "    /citems newkey",
                            TextColors.GRAY, " > ",
                            TextColors.GRAY, "Creates a new default item on the config (so you can edit it)",
                            Text.NEW_LINE
                    ));
                    return CommandResult.success();
                })
                .child(give, "give")
                .child(newKey, "newkey")
                .build();

        Sponge.getCommandManager().register(this, cmd, "commanditems", "citems", "ci");

        Sponge.getEventManager().registerListeners(this, new CIListener());
    }

    @Listener
    public void onReload(GameReloadEvent e) {
        this.itemsConfig.load();
    }

    public static ItemsConfig getConfig() {
        return instance.itemsConfig.getConfig();
    }

    public static CooldownStorage getCooldowns() {
        return instance.cooldowns.getConfig();
    }

    public static Text toText(String s) {
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    public static void log(String text) {
        instance.logger.warn(text);
    }

    public static CommandItems getInstance() {
        return instance;
    }

    public static MessagesConfig getMessages() {
        return instance.messages.getConfig();
    }

}
