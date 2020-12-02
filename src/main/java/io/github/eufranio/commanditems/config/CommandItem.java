package io.github.eufranio.commanditems.config;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.util.List;

/**
 * Created by Frani on 23/01/2018.
 */
@ConfigSerializable
public class CommandItem {

    @Setting
    public ItemType type = ItemTypes.STONE;

    @Setting
    public String displayName = "&eSpetacular Stone";

    @Setting
    public List<String> lore = Lists.newArrayList(
            "&aThis is a magic item! Right click",
            "&awith it to see what happens!"
    );

    @Setting
    public List<String> commands = Lists.newArrayList("give %player% stone 1;console");

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

}
