# Story Wiki

## Buch `book`

## Kapitel `chapter`

``` toml
name = "<name>"
end_message = "<message>"
players = [<number1>,<number2>, ...]
world = "<world_name>"
start_quest = "<quest_name>"
max_deaths = <number>
```

### Tagebuch `diary`

## Quest `quest`

``` toml
[quest.<name>]
type = "main/optional"
next = ["<quest_name1>", "<quest_name2>"]
location = [<x>, <y>, <z>]
```

**Variablen**

``` toml
[quest.<name>.var]
<name1> = "<value1>" # <-- string variable
<name2> = <number> # <-- int variable
<name3> = "[<lower>..<upper>]" # <-- random int between lower and upper bound
<name4> = "[<number1>,<number2>, ...]" # <-- random int from given list
```

## Action `action`

Alle Aktionen werden hintereinander ausgeführt. Der Trigger einer Aktion startet diese. Falls kein
Trigger angegeben
wird die Aktion direkt gestartet. Falls keine Aktion und nur ein Trigger angegeben wird, startet die
Aktion direkt.

``` toml
[quest.<name>.1] # <-- action id 1
# see action types

[quest.<name>.2] # <-- action id 2
# see action types
```

### Aktionstypen

**Gespräch `talk`**

``` toml
character = "<name>" # <-- defined in characters file
messages = [
    "c: <text>", # <-- spoken text by the character
    "p: <text>" # <-- spoken text by the player
]
# optional
location = [<x>,<y>,<z>] # <-- text location, if should not be above player
character_look_direction = [<yaw>,<pitch>]
```

**Gedanken `thought`**

``` toml
messages = [
    "<text>", # <--- spoken text by the player
    "<text>" # <--/
]
```

**Item-Suche `item_collect`**

``` toml
item = "<material/story_item>"
angle = <angle>` # <--- Item-Winkel (held by an armorstand)
```

**Item-Übergabe `item_give`**

``` toml
item = <material/story_item>
location = [<x>,<y>,<z>] # <-- drop location
```

**Item-Loot `item_loot`**

``` toml
items = ["<material1/story_item1>", "<material2/story_item2>", ...]
location = [<x>,<y>,<z>] # <-- location of the block with an inventory, like chests
```

**Block-Abbauen `block_break`**

``` toml
materials = ["<material1>", "<material2>", ...]
min_height = <number>
max_height = <number>

polygon = [[<x1>, <z1>], [<x2>, <z2>], ...]
# or
block = [<x>,<y>,<z>]
```

**Wachen-Spawnen `spawn_guard`**

``` toml
action = "spawn_guard"
location = [<x>,<y>,<z>]
type = "<pillager/vindicator/ravager>"
amount = <number>

```

**Verzögerung `delay`**

``` toml
delay = <seconds>
```

**Wetter `weather`**

``` toml
weather = "<clear/downfall>"
```

**Block-Interaktion `block_interact`**

Interagiert mit Türen Hebel, Knöpfen, ...

``` toml
location = [<x>,<y>,<z>]
```

### Tirgger-Typen

**Bereich `area`**

``` toml
location = [<x>,<y>,<z>]
# or (if not equal with action location)
trigger_location = [<x>,<y>,<z>]

radius = <number>
```

**Item-Drop an Position `drop_at`**

``` toml 
location = [<x>,<y>,<z>]
# or (if not equal with action location)
trigger_location = [<x>,<y>,<z>]

item = "<material/story_item>"

# optional
amount = <number>
```

**Item-Drop `drop`**

``` toml
item = "<material/story_item>"

# optional
amount = <number>
```

**Chat-Code `drop_at`**

``` toml
code = "<code>"
# or
code = ["<code1>", "<code2>", ...]
```

**Sleep `sleep`**

``` toml
# nothing
```

**Delay `delay`**

``` toml
delay = <seconds>
```