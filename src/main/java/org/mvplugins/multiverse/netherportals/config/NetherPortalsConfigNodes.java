package org.mvplugins.multiverse.netherportals.config;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.node.ConfigHeaderNode;
import org.mvplugins.multiverse.core.config.node.ConfigNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.NodeGroup;

@Service
final class NetherPortalsConfigNodes {

    private final NodeGroup nodes = new NodeGroup();

    NodeGroup getNodes() {
        return nodes;
    }

    private <N extends Node> N node(N node) {
        nodes.add(node);
        return node;
    }

    private final ConfigHeaderNode portalAutoLinkHeader = node(ConfigHeaderNode.builder("portal-auto-link-when")
            .comment("####################################################################################################")
            .comment("#                                                                                                  #")
            .comment("#                         MULTIVERSE-NETHERPORTALS CONFIGURATION                                   #")
            .comment("#                                                                                                  #")
            .comment("#    WIKI:        https://mvplugins.org/netherportals/                                             #")
            .comment("#    DISCORD:     https://discord.gg/NZtfKky                                                       #")
            .comment("#    BUG REPORTS: https://github.com/Multiverse/Multiverse-NetherPortals/issues                    #")
            .comment("#    DONATE:      https://github.com/sponsors/Multiverse                                           #")
            .comment("#                                                                                                  #")
            .comment("#    New options are added to this file automatically. If you manually made changes                #")
            .comment("#    while your server is running, use Multiverse-Core's reload command.                           #")
            .comment("#                                                                                                  #")
            .comment("####################################################################################################")
            .comment("")
            .comment("Controls the prefix and suffix used to automatically find linked dimension worlds.")
            .comment("For example, by default the world 'cat_nether' nether portal will be linked to world 'cat'.")
            .build());

    final ConfigNode<String> netherPrefix = node(ConfigNode.builder(
                    "portal-auto-link-when.nether.prefix", String.class)
            .defaultValue("")
            .name("nether-prefix")
            .build());

    final ConfigNode<String> netherSuffix = node(ConfigNode.builder(
                    "portal-auto-link-when.nether.suffix", String.class)
            .defaultValue("_nether")
            .name("nether-suffix")
            .build());

    final ConfigNode<String> endPrefix = node(ConfigNode.builder(
                    "portal-auto-link-when.end.prefix", String.class)
            .defaultValue("")
            .name("end-prefix")
            .build());

    final ConfigNode<String> endSuffix = node(ConfigNode.builder(
                    "portal-auto-link-when.end.suffix", String.class)
            .defaultValue("_the_end")
            .name("end-suffix")
            .build());

    final ConfigNode<Boolean> handleEndExitRespawn = node(ConfigNode.builder("handle-end-exit-respawn", Boolean.class)
            .comment("")
            .comment("When enabled, end portal exits from end world will override default behaviour and respawn players")
            .comment("to the linked world's spawn. This is to handle the special case where player exiting end portal")
            .comment("in end are treated as respawns instead of portal teleports in Minecraft.")
            .comment("** This feature is only supported on Paper servers.")
            .defaultValue(true)
            .name("handle-end-exit-respawn")
            .build());

    final ConfigNode<Boolean> usingBounceBack = node(ConfigNode.builder("bounceback", Boolean.class)
            .comment("")
            .comment("When enabled, players are pushed out of a portal when its destination is unavailable.")
            .defaultValue(true)
            .name("bounceback")
            .build());

    final ConfigNode<Boolean> teleportingEntities = node(ConfigNode.builder("teleport-entities", Boolean.class)
            .comment("")
            .comment("When enabled, non-player entities can travel through Nether and End portals.")
            .defaultValue(true)
            .name("teleport-entities")
            .build());

    final ConfigNode<Boolean> sendingDisabledPortalMessage = node(ConfigNode.builder(
                    "send-disabled-portal-message", Boolean.class)
            .comment("")
            .comment("When enabled, players are told when portals are disabled in the current world.")
            .defaultValue(true)
            .name("send-disabled-portal-message")
            .build());

    final ConfigNode<Boolean> sendingNoDestinationMessage = node(ConfigNode.builder(
                    "send-no-destination-message", Boolean.class)
            .comment("")
            .comment("When enabled, players are told when a portal has no valid destination.")
            .defaultValue(true)
            .name("send-no-destination-message")
            .build());

    final ConfigNode<Boolean> endPlatformDropBlocks = node(ConfigNode.builder(
                    "end-platform-drop-blocks", Boolean.class)
            .comment("")
            .comment("When enabled, blocks replaced while creating an End platform drop as items on non-default worlds to")
            .comment("mimic vanilla Minecraft behaviour.")
            .comment("Note: The default end is handled by the server and loaded even without Multiverse installed, thus")
            .comment("      multiverse will have no control over it's drop items behaviour.")
            .defaultValue(true)
            .name("end-platform-drop-blocks")
            .build());

    final ConfigNode<Double> version = node(ConfigNode.builder("version", Double.class)
            .comment("")
            .comment("")
            .comment("The configuration version. Do not edit this value.")
            .defaultValue(0.0)
            .hidden()
            .build());
}
