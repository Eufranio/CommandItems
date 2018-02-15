# CommandItems
CommandItems is a simple Sponge plugin that aims to bring the command items features from the old CraftBook plugin to Sponge. With this plugin, you can have custom items that can be used and execute commands, consume the item, execute only on right or left click, execute commands as player/console, etc!

## Setup
The plugin is drag and drop: once it loaded, edit the config as you like. If you want to add more items to the config, use `/citems newkey` in-game so it creates a new item with a random UUID and inserts in the current config.

## Commands and Permissions
* /citems newkey | `commanditems.newkey` | Creates a new key config node on your config
* /citems give <uuid> [player] | `commanditems.give` | Gives a key named `uuid` for you or `player`, if specified. The uuid can be the generated UUID from `/citems newkey` (see above), or you can rename the UUID in the config to another name and use it here.
* /citems | `commanditems.main` | Main plugin command, lists the other commands and their usage

**If you find any issues, report them to the plugin's issue tracker. If you want, you can donate for me trough PayPal, my paypal email is frani@magitechserver.com**.