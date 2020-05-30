package io.github.eufranio.commanditems;

import com.google.inject.Inject;
import io.github.eufranio.commanditems.api.Storage;
import io.github.eufranio.commanditems.config.CommandItem;
import io.github.eufranio.commanditems.config.MainConfig;
import io.github.eufranio.commanditems.data.CommandItemData;
import io.github.eufranio.commanditems.managers.ItemManager;
import io.github.eufranio.commanditems.storage.DatabaseStorage;
import io.github.eufranio.commanditems.storage.FileStorage;
import io.github.eufranio.config.Config;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.TypeTokens;

import java.io.File;
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

    @Inject
    public Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    public Config<MainConfig> config;
    public Storage storage;
    public ItemManager itemManager = new ItemManager(this);

    public static Key<Value<String>> KEY;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        this.config = new Config<>(MainConfig.class, "CommandItems.conf", configDir);

        if (config.get().enableDatabaseStorage)
            storage = new DatabaseStorage();
        else
            storage = new FileStorage();
        storage.init(this);

        logger.info("CommandItems is starting!");

        CommandSpec create = CommandSpec.builder()
                .permission("commanditems.create")
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
                .executor((sender, context) -> {
                    String name = context.<String>getOne("name").orElse(null);
                    this.itemManager.createItem(name);

                    sender.sendMessage(Text.of(TextColors.GREEN, "Successfully created new item!"));
                    return CommandResult.success();
                }).build();

        CommandSpec give = CommandSpec.builder()
                .permission("commanditems.give")
                .arguments(
                        GenericArguments.flags().flag("d", "-drop").buildWith(
                                GenericArguments.seq(
                                        GenericArguments.string(Text.of("name")),
                                        GenericArguments.integer(Text.of("amount")),
                                        GenericArguments.playerOrSource(Text.of("player"))
                                )
                        )
                )
                .executor((sender, context) -> {
                    Player player = context.requireOne("player");
                    String name = context.requireOne("name");
                    int amount = context.requireOne("amount");

                    CommandItem item = this.itemManager.getItem(name);
                    if (item == null)
                        throw new CommandException(Text.of("Unknown CommandItem!"));

                    ItemStack stack = ItemStack.builder()
                            .itemType(item.type)
                            .quantity(amount)
                            .add(Keys.DISPLAY_NAME, toText(item.displayName))
                            .add(Keys.ITEM_LORE, item.lore.stream().map(CommandItems::toText).collect(Collectors.toList()))
                            .itemData(new CommandItemData(name))
                            .build();

                    InventoryTransactionResult result = player.getInventory().offer(stack);
                    if (context.hasAny("d") && result.getType() != InventoryTransactionResult.Type.SUCCESS) {
                        result.getRejectedItems().forEach(i -> {
                            Entity entity = player.getWorld().createEntity(EntityTypes.ITEM, player.getPosition());
                            entity.offer(Keys.REPRESENTED_ITEM, i);
                            player.getWorld().spawnEntity(entity);
                        });
                    }

                    player.sendMessage(toText(this.config.get().messages.YOU_GOT_A_ITEM.replace("%item%", TextSerializers.FORMATTING_CODE.serialize(toText(item.displayName)))));

                    return CommandResult.success();
                })
                .build();

        CommandSpec cmd = CommandSpec.builder()
                .permission("commanditems.main")
                .executor((sender, context) -> {
                    sender.sendMessage(Text.of(Text.NEW_LINE,
                            TextColors.GREEN, "    /citems give <name> [player]",
                            TextColors.GRAY, " > Gives yourself (or [player]) the item associated to this UUID, from the config", Text.NEW_LINE,
                            TextColors.GREEN, "    /citems create [name]",
                            TextColors.GRAY, " > Creates a new default item on the config with the specified name (so you can edit it)", Text.NEW_LINE
                    ));
                    return CommandResult.success();
                })
                .child(give, "give")
                .child(create, "create")
                .build();

        Sponge.getCommandManager().register(this, cmd, "commanditems", "citems", "ci");
    }

    @Listener
    public void onReload(GameReloadEvent e) {
        this.config.reload();
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        KEY = Key.builder()
                .type(TypeTokens.STRING_VALUE_TOKEN)
                .query(DataQuery.of("commanditem"))
                .id("commanditem")
                .name("CommandItems Command")
                .build();
    }

    @Listener
    public void registerKeys(GameRegistryEvent.Register<Key<?>> event) {
        event.register(KEY);
    }

    @Listener
    public void onRegister(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        DataRegistration.builder()
                .dataClass(CommandItemData.class)
                .immutableClass(CommandItemData.Immutable.class)
                .builder(new CommandItemData.Builder())
                .id("commanditem_data")
                .name("CommandItems Command")
                .build();
    }

    @Listener
    public void onItemInteract(InteractItemEvent e, @Root Player p) {
        e.getItemStack().get(CommandItems.KEY).ifPresent(name -> {
            CommandItem item = config.get().items.get(name);
            if (item != null) {
                if (item.cancelOriginalAction)
                    e.setCancelled(true);

                boolean success = this.itemManager.useItem(item, name, p, e instanceof InteractItemEvent.Secondary);
                if (success && item.consume)
                    p.getInventory()
                        .query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(e.getItemStack().createStack()))
                        .poll(1);
            }
        });
    }

    public static Text toText(String s) {
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

}
