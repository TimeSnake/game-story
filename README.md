# Story Wiki

## Kapitel `chapter`

**Aufbau** <br>
`name: <name>` Jedes Kapital muss einen Namen haben, dieser wird im Inhaltsverzeichnis angezeigt.

## Teile `parts`

Ein Kapitel besteht aus mehreren Teilen. Jeder Teil kann von dem Spieler über das Inhaltsverzeichnis begonnen werden.

**Aufbau** <br>
`name: <name>` Name des Teils <br>
`end_message: <text>` Nachricht bei Abschluss des Kapitels <br>
`diary:` Tagebuch des Teils (siehe Tagebuch) <br>
`sections:` Sektionen des Teils (siehe Sektionen)

### Tagebuch `diary`

Das Tagebuch gilt immer für einen Teil. <br>

**Aufbau** <br>
`1:` _Seitennummer_ <br>
&nbsp; `text:` _Text der Seite_ <br>
&nbsp; &nbsp; `- Erste Zeile Text` <br>
&nbsp; &nbsp; `- Zweite Zeile Text` <br>
&nbsp; &nbsp; `- Dritte Zeile Text` <br>
&nbsp; `date: x.y` _Veröffentlichung der Seite; x - section, y - action_

### Sektionen `sections`

Jede Sektion stellt ein Speicherpunkt der Story dar.

**Aufbau** <br>
`start:` <br>
&nbsp; `location:` _Startposition und Respawnposition_ <br>
&nbsp; &nbsp; `x: <x>` <br>
&nbsp; &nbsp; `y: <y>` <br>
&nbsp; &nbsp; `z: <z>` <br>

`actions:` _Aktionen der Sektion (siehe Aktionen)_

### Aktionen `actions`

Alle Aktionen werden hintereinander ausgeführt. Der Trigger einer Aktion startet diese. Falls kein Trigger angegeben
wird die Aktion direkt gestartet. Falls keine Aktion und nur ein Trigger angegeben wird, startet die nächste Aktion.

**Aufbau** <br>
`1:` _Aktions ID_ <br>
&nbsp; `trigger:` _Triggertyp der Aktion_ <br>
&nbsp; `action:` _Aktionstyp der Aktion_ <br>
&nbsp; `...` _Eigenschaften des Triggers und der Aktionen (siehe Trigger- und Aktionstypen)_

**Positionen `location`** <br>
Jede Position muss als `location` oder als `character` angegeben werden. Falls eine `location` angegeben ist, dann wird
diese verwendet. Falls ein `character` angegeben ist und keine `location`, so wird die Position des Charakters
verwendet.

`location:` _Koordinatenposition_ <br>
&nbsp; `x: <x>` <br>
&nbsp; `y: <y>` <br>
&nbsp; `z: <z>` <br>

`character: <id>` _ID des Charakters_

#### Triggertypen

- Bereich `area` <br>
  `location:` _Aktivierungsposition_ <br>
  `radius: <radius>` _Aktivierungsradius_
- Item-Drop an Position `drop_at` <br>
  `location:` _Dropposition_ <br>
  `item: <id>` _ID des zu droppenden Items_
- Item-Drop `drop` <br>
  `item: <id>` _ID des zu droppenden Items_ <br>
  `clear: <true/false>` _Entfernen des gedroppten Items_ <br>
- Chat-Code `chat_code` <br>
  `code:` _Code_ <br>

#### Aktionstypen

- Gespräch `talk` <br>
  `location:` _Aktivierungsposition (optional)_ <br>
  `radius: <radius>` _Aktivierungsradius_ <br>
  `character: <id>` _Gesprächspartner_ <br>
  `messages: ` _Gesprächstext_ <br>
  `- c: <text>` _Charakter spricht_ <br>
  `- p: <text>` _Spieler spricht_ <br>
- Gedanken `thought` <br>
  `messages:` _Gedankentext_ <br>
  `- <text>` _Textteile_
- Item-Suche `item_search` <br>
  `location:` _Itemposition_ <br>
  `radius: <radius>` _Einsammelradius_ <br>
  `item: <id>` _ID des zu droppenden Items_ <br>
  `angle: <angle>` _Item-Winkel_
- Item-Übergabe `item_give` <br>
  `item: <id>` _ID des zu droppenden Items_ <br>
  `radius: <radius>` _Aktivierungsradius_ <br>
  `character: <id>` _Itemgeber_ <br>
- Item-Loot `item_loot` <br>
  `location:` _Kistenposition_ <br>
  `item: [id1, id2, ...]` _IDs der Kistenitems_ <br>
- Clear-Inventory `clear_inventory` <br>

  