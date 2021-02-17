package plugin.nomore.qolclicks.utils.scene;

import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import plugin.nomore.qolclicks.QOLClicksPlugin;
import plugin.nomore.qolclicks.utils.automation.Format;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Npcs
{

    @Inject private Client client;
    @Inject private QOLClicksPlugin plugin;
    @Inject private Format string;

    public List<NPC> get()
    {
        return plugin.getNpcList();
    }

    public NPC getClosest()
    {
        if (client.getLocalPlayer() == null)
        {
            return null;
        }
        return get()
                .stream()
                .min(Comparator.comparing(entityType
                        -> entityType
                        .getLocalLocation()
                        .distanceTo(client.getLocalPlayer()
                                .getLocalLocation())))
                .orElse(null);
    }

    public NPC getClosestMatchingName(String npcName)
    {
        if (client.getLocalPlayer() == null)
        {
            return null;
        }
        return get()
                .stream()
                .filter(npc
                        -> npc != null
                        && npc.getName() != null
                        && npc.getName().equalsIgnoreCase(npcName))
                .collect(Collectors.toList())
                .stream()
                .min(Comparator.comparing(entityType
                        -> entityType
                        .getLocalLocation()
                        .distanceTo(client.getLocalPlayer()
                                .getLocalLocation()))).orElse(null);
    }

    public NPC getClosestMatchingId(int npcId)
    {
        if (client.getLocalPlayer() == null)
        {
            return null;
        }
        return get()
                .stream()
                .filter(npc
                        -> npc != null
                        && npc.getId() == npcId)
                .collect(Collectors.toList())
                .stream()
                .min(Comparator.comparing(entityType
                        -> entityType
                        .getLocalLocation()
                        .distanceTo(client.getLocalPlayer()
                                .getLocalLocation()))).orElse(null);
    }

    public List<NPC> getNpcsMatchingName(String... npcNames)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return get()
                .stream()
                .filter(i -> i != null
                        && i.getName() != null
                        && Arrays.stream(npcNames)
                        .anyMatch(s -> string.removeWhiteSpaces(s)
                                .equalsIgnoreCase(string.removeWhiteSpaces(i.getName()))))
                .sorted(Comparator.comparing(entityType -> entityType
                        .getLocalLocation()
                        .distanceTo(client.getLocalPlayer()
                                .getLocalLocation())))
                .collect(Collectors.toList());
    }

    public List<NPC> getNpcsMatchingId(int... npcIds)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return get()
                .stream()
                .filter(i -> i != null
                        && Arrays.stream(npcIds)
                        .anyMatch(itemId -> itemId == i.getId()))
                .sorted(Comparator.comparing(entityType -> entityType
                        .getLocalLocation()
                        .distanceTo(client.getLocalPlayer()
                                .getLocalLocation())))
                .collect(Collectors.toList());
    }

    public NPC getClosestNpcWithMenuAction(String action)
    {
        if (client.getLocalPlayer() == null)
        {
            return null;
        }
        return get()
                .stream()
                .filter(npc -> Arrays.stream(client.getNpcDefinition(npc.getId()).getActions())
                .anyMatch(npcAction -> !Strings.isNullOrEmpty(npcAction)
                        && npcAction.equalsIgnoreCase(action)))
                .min(Comparator.comparing(entityType -> entityType
                        .getLocalLocation()
                        .distanceTo(client.getLocalPlayer()
                                .getLocalLocation())))
                .orElse(null);
    }

    public List<NPC> getNpcsWithMenuAction(String action)
    {
        assert client.isClientThread();

        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        return get()
                .stream()
                .filter(npc -> Arrays.stream(client.getNpcDefinition(npc.getId()).getActions())
                        .anyMatch(npcAction -> !Strings.isNullOrEmpty(npcAction)
                                && npcAction.equalsIgnoreCase(action)))
                .min(Comparator.comparing(entityType -> entityType
                        .getLocalLocation()
                        .distanceTo(client.getLocalPlayer()
                                .getLocalLocation())))
                .stream()
                .collect(Collectors.toList());
    }



}
