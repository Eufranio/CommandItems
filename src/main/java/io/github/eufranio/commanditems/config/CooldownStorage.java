package io.github.eufranio.commanditems.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.eufranio.commanditems.CommandItems;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 23/01/2018.
 */
@ConfigSerializable
public class CooldownStorage {

    @Setting
    public Map<String, List<ItemEntry>> items = Maps.newHashMap(); // UUID of player <-> items

    @ConfigSerializable
    public static class ItemEntry {

        public ItemEntry(){}

        public ItemEntry(String item, String lastUsed) {
            this.item = item;
            this.lastUsed = lastUsed;
        }

        @Setting
        public String item; // UUID

        @Setting
        public String lastUsed;

    }

    public List<ItemEntry> getOrCreateEntries(UUID player) {
        if (this.items.containsKey(player.toString())) {
            return this.items.get(player.toString());
        } else {
            List<ItemEntry> entries = Lists.newArrayList();
            this.items.put(player.toString(), entries);
            return entries;
        }
    }

    public ItemEntry getEntry(UUID player, String itemUuid) {
        return this.getOrCreateEntries(player)
                .stream()
                .filter(e -> e.item.equals(itemUuid))
                .findFirst().orElse(null);
    }

    public Instant getLastUse(Player player, String itemUuid) {
        ItemEntry entry = this.getEntry(player.getUniqueId(), itemUuid);
        if (entry != null) {
            return Instant.parse(entry.lastUsed);
        }
        return null;
    }

    public void addOrUpdateCooldown(Player player, String itemUuid) {
        ItemEntry entry = this.getEntry(player.getUniqueId(), itemUuid);
        if (entry == null) {
            entry = new ItemEntry(itemUuid, Instant.now().toString());
            this.getOrCreateEntries(player.getUniqueId()).add(entry);
        } else {
            entry.lastUsed = Instant.now().toString();
        }
    }
    
    public void removeEntry(Player p, ItemEntry entry) {
        this.removeEntry(p.getUniqueId(), entry);
    }

    public void removeEntry(UUID uuid, ItemEntry entry) {
        List<ItemEntry> entries = this.items.get(uuid.toString());
        if (entries != null) entries.remove(entry);
    }

    public void removeCooldown(UUID p, String itemUuid) {
        ItemEntry entry = this.getEntry(p, itemUuid);
        this.removeEntry(p, entry);
    }

}
