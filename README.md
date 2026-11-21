# Health Plus for NeoForge

Craft and use Heart Containers to permanently gain extra hearts.

## Installation

Java 21, Minecraft 1.21.1, and NeoForge. Tested with 21.1.265. Download `health_plus-1.1.1.jar` from Releases. Install on both sides for Minecraft 1.21.1 NeoForge. A Heart Container adds one heart. Bonuses persist through death and world reloads. Use the limitExtraHearts game rule to set the limit.

合成并使用心之容器永久增加生命值。可用游戏规则设置上限。

## Build

Use JDK 21 and Python 3.11+. Create a text file with one absolute JAR path per line for the mapped Minecraft 1.21.1 and NeoForge 21.1.265 libraries.

```sh
python build.py --classpath-file classpath.txt --output health_plus-1.1.1.jar
```

## License and feedback

Apache-2.0. Unofficial NeoForge 1.21.1 port of Health Plus by TheHighTide. Original project: https://modrinth.com/mod/healthplus.

Report issues here with reproduction steps and your Minecraft, NeoForge and mod versions.
