package io.github.eufranio.commanditems.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Frani on 23/01/2018.
 */
@ConfigSerializable
public class MessagesConfig {

    @Setting
    public String NO_PERMISSION_TO_USE_ITEM = "You don't have permission to use this item!";

    @Setting
    public String COOLDOWN = "You must wait %seconds% seconds before using this item again!";

    @Setting
    public String ITEM_USED = "&aYou successfully used this %item%&a!";

    @Setting
    public String YOU_GOT_A_ITEM = "&aYou were given a %item%&a! Enjoy!";

}
