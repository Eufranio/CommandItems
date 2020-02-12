package io.github.eufranio.commanditems.api;

import io.github.eufranio.commanditems.CommandItems;
import io.github.eufranio.commanditems.config.CommandItem;

import java.time.Instant;
import java.util.UUID;

public interface Storage {

    void init(CommandItems plugin);

    Instant getLastUse(UUID player, String itemName);

    void updateLastUsed(UUID player, String itemName);

    void removeCooldown(UUID player, String itemName);

}
