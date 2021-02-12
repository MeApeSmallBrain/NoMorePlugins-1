/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package plugin.nomore.qolclicks;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import org.pf4j.Extension;
import plugin.nomore.qolclicks.misc.Banking;
import plugin.nomore.qolclicks.misc.inventory.DropItems;
import plugin.nomore.qolclicks.misc.inventory.DropSimilar;
import plugin.nomore.qolclicks.skills.cooking.Cooking;
import plugin.nomore.qolclicks.skills.firemaking.Firemaking;
import plugin.nomore.qolclicks.skills.fishing.Fishing;
import plugin.nomore.qolclicks.skills.prayer.Prayer;
import plugin.nomore.qolclicks.utils.Inventory;
import plugin.nomore.qolclicks.utils.Menu;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Extension
@PluginDescriptor(
		name = "QOL Clicks",
		description = "QOL fixes that should be implemented.",
		tags = {"click", "nomore", "qol"},
		type = PluginType.UTILITY
)
@Slf4j
public class QOLClicksPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private QOLClicksConfig config;

	@Inject
	private Firemaking firemaking;

	@Inject
	private Cooking cooking;

	@Inject
	private Fishing fishing;

	@Inject
	private Prayer prayer;

	@Inject
	private Banking banking;

	@Inject
	private Inventory inventory;

	@Inject
	private DropSimilar dropSimilar;

	@Inject
	private DropItems dropItems;

	@Inject
	private Menu menu;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	boolean dropping = false;

	@Provides
	QOLClicksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QOLClicksConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}
		client.getNpcs().forEach(npc ->
		{
			if (npc != null)
			{
				npcList.add(npc);
			}
		});
	}

	@Override
	protected void shutDown()
	{
	}

	@Subscribe
	private void on(MenuOpened event)
	{
		if (!config.enableDropItems()
				&& !config.enableDropSimilar())
		{
			return;
		}
		if (event.getFirstEntry().getParam1() != WidgetInfo.INVENTORY.getId())
		{
			return;
		}
		MenuEntry[] originalEntries = event.getMenuEntries();
		MenuEntry dropSimilar = new MenuEntry("Drop-Similar",
				"<col=ffff00>" + client.getItemDefinition(event.getFirstEntry().getIdentifier()).getName(),
				event.getFirstEntry().getIdentifier(), MenuOpcode.ITEM_DROP.getId(),
				event.getFirstEntry().getParam0(),
				event.getFirstEntry().getParam1(),
				event.getFirstEntry().isForceLeftClick());
		MenuEntry dropItems = new MenuEntry("Drop-Items",
				"<col=ffff00>" + client.getItemDefinition(event.getFirstEntry().getIdentifier()).getName(),
				event.getFirstEntry().getIdentifier(), MenuOpcode.ITEM_DROP.getId(),
				event.getFirstEntry().getParam0(),
				event.getFirstEntry().getParam1(),
				event.getFirstEntry().isForceLeftClick());

		if (config.enableDropSimilar() && !config.enableDropItems())
		{
			if (originalEntries.length == 3)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropSimilar, originalEntries[2]});
			}
			if (originalEntries.length == 4)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropSimilar, originalEntries[2], originalEntries[3]});
			}
			if (originalEntries.length == 5)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropSimilar, originalEntries[2], originalEntries[3], originalEntries[4]});
			}
			if (originalEntries.length == 6)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropSimilar, originalEntries[2], originalEntries[3], originalEntries[4], originalEntries[5]});
			}
		}
		if (!config.enableDropSimilar() && config.enableDropItems())
		{
			if (originalEntries.length == 3)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, originalEntries[2]});
			}
			if (originalEntries.length == 4)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, originalEntries[2], originalEntries[3]});
			}
			if (originalEntries.length == 5)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, originalEntries[2], originalEntries[3], originalEntries[4]});
			}
			if (originalEntries.length == 6)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, originalEntries[2], originalEntries[3], originalEntries[4], originalEntries[5]});
			}
		}
		if (config.enableDropSimilar() && config.enableDropItems())
		{
			if (originalEntries.length == 3)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, dropSimilar, originalEntries[2]});
			}
			if (originalEntries.length == 4)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, dropSimilar, originalEntries[2], originalEntries[3]});
			}
			if (originalEntries.length == 5)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, dropSimilar, originalEntries[2], originalEntries[3], originalEntries[4]});
			}
			if (originalEntries.length == 6)
			{
				event.setMenuEntries(new MenuEntry[]{originalEntries[0], originalEntries[1], dropItems, dropSimilar, originalEntries[2], originalEntries[3], originalEntries[4], originalEntries[5]});
			}
		}
	}

	@Subscribe
	private void on(MenuOptionClicked event)
	{
		/*
		if (!paused && event.isAuthentic())
		{
			event.consume();
		}

		 */
		MenuEntry clone = event.clone();

		String origOption = event.getOption();
		String origTarget = event.getTarget();
		int origId = event.getIdentifier();
		MenuOpcode origMenuOpcode = event.getMenuOpcode();
		int origP0 = event.getParam0();
		int origP1 = event.getParam1();
		boolean origIsFLC = event.isForceLeftClick();
		boolean origIsCon = event.isConsumed();

		if (config.enableFiremaking())
		{
			if (!firemaking.menuOptionClicked(event))
			{
				event.setMenuEntry(clone);
			}
		}

		if (config.enableCooking())
		{
			if (!cooking.menuOptionClicked(event))
			{
				event.setMenuEntry(clone);
			}
		}

		if (config.enableFishingRod()
				|| config.enableLobsterPot()
				|| config.enableBarbarianRod())
		{
			if (!fishing.menuOptionClicked(event))
			{
				event.setMenuEntry(clone);
			}
		}

		if (config.enableBanking())
		{
			if (!banking.menuOptionClicked(event))
			{
				event.setMenuEntry(clone);
			}
		}

		if (config.enableUnnoteBones())
		{
			if (!prayer.menuOptionClicked(event))
			{
				event.setMenuEntry(clone);
			}
		}

		/*
		if (config.enable()
				&& !.menuOptionClicked(event))
		{
			event.consume();
		}

		 */

		if (config.enableDropSimilar()
				&& event.getOption().equals("Drop-Similar")
				&& dropSimilar.menuOptionClicked(event))
		{
			event.consume();
			inventory.dropItems(inventory.getItemsToDrop());
		}

		if (config.enableDropItems()
				&& event.getOption().equals("Drop-Items")
				&& dropItems.menuOptionClicked(event))
		{
			event.consume();
			inventory.dropItems(inventory.getItemsToDrop());
		}

		if (isDropping())
		{
			event.setOption("Drop");
			event.setTarget("<col=ff9040>" + client.getItemDefinition(event.getIdentifier()).getName());
		}

		debug(event, origOption, origTarget, origId, origMenuOpcode, origP0, origP1, origIsFLC, origIsCon);
	}

	@Subscribe
	private void on(MenuEntryAdded event)
	{
		if (config.enableFiremaking())
		{
			firemaking.menuEntryAdded(event);
		}

		if (config.enableCooking())
		{
			cooking.menuEntryAdded(event);
		}

		if (config.enableFishingRod()
				|| config.enableLobsterPot()
				|| config.enableBarbarianRod())
		{
			fishing.menuEntryAdded(event);
		}

		if (config.enableBanking())
		{
			banking.menuEntryAdded(event);
		}

		if (config.enableUnnoteBones())
		{
			prayer.menuEntryAdded(event);
		}
	}

	public static List<NPC> npcList = new ArrayList<>();
	public List<NPC> getNpcList() { return npcList; }

	@Subscribe
	private void on(NpcSpawned e)
	{
		NPC npc = e.getNpc();
		if (npc == null)
		{
			return;
		}
		npcList.add(npc);
	}

	@Subscribe
	private void on(NpcDespawned e)
	{
		npcList.remove(e.getNpc());
	}

	public void setSelected(WidgetInfo widgetInfo, int itemIndex, int itemId)
	{
		client.setSelectedItemWidget(widgetInfo.getId());
		client.setSelectedItemSlot(itemIndex);
		client.setSelectedItemID(itemId);
	}

	public void insertMenuEntry(MenuEntry e, boolean forceLeftClick)
	{
		client.insertMenuItem(
				e.getOption(),
				e.getTarget(),
				e.getOpcode(),
				e.getIdentifier(),
				e.getParam0(),
				e.getParam1(),
				forceLeftClick
		);
	}

	public static void writeTextToClipboard(String s)
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = new StringSelection(s);
		clipboard.setContents(transferable, null);
	}

	private void debug(MenuOptionClicked event, String origOption, String origTarget, int origId,MenuOpcode origMenuOpcode,int origP0,int origP1,boolean origIsFLC,boolean origIsCon)
	{
		if (config.enableDebugging()
				&& event.getOpcode() != MenuOpcode.WALK.getId())
		{
			System.out.println(
					"\n" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:S").format(new Date())
							+ "\nOrig: Option: " + origOption + "   ||   Mod: Option: " + event.getOption()
							+ "\nOrig: Target: " + origTarget + "   ||   Mod: Target: " + event.getTarget()
							+ "\nOrig: Identifier: " + origId + "   ||   Mod: Identifier: " + event.getIdentifier()
							+ "\nOrig: Opcode: " + origMenuOpcode + "   ||   Mod: Opcode: "	+ event.getMenuOpcode()
							+ "\nOrig: Param0: " + origP0 + "   ||   Mod: Param0: " + event.getParam0()
							+ "\nOrig: Param1: " + origP1 + "   ||   Mod: Param1: " + event.getParam1()
							+ "\nOrig: forceLeftClick: " + origIsFLC + "   ||   Mod: forceLeftClick: " 	+ event.isForceLeftClick()
							+ "\nOrig: isConsumed: " + origIsCon + "   ||   Mod: isConsumed: " 	+ event.isConsumed()
			);

			if (config.enableWriteToClipboard())
			{
				writeTextToClipboard(
						"```\n"
								+ "\n" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:S").format(new Date())
								+ "\nOrig: Option: " + origOption + "   ||   Mod: Option: " + event.getOption()
								+ "\nOrig: Target: " + origTarget + "   ||   Mod: Target: " + event.getTarget()
								+ "\nOrig: Identifier: " + origId + "   ||   Mod: Identifier: " + event.getIdentifier()
								+ "\nOrig: Opcode: " + origMenuOpcode + "   ||   Mod: Opcode: "	+ event.getMenuOpcode()
								+ "\nOrig: Param0: " + origP0 + "   ||   Mod: Param0: " + event.getParam0()
								+ "\nOrig: Param1: " + origP1 + "   ||   Mod: Param1: " + event.getParam1()
								+ "\nOrig: forceLeftClick: " + origIsFLC + "   ||   Mod: forceLeftClick: " 	+ event.isForceLeftClick()
								+ "\n```");
			}
		}
	}


}
