package net.thedarktide.celeo;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listener for <b>DarkBox</b>
 */
public class BoxListener implements Listener
{

	private final Core plugin;

	public BoxListener(Core instance)
	{
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		if (block == null || !block.getType().equals(Material.CHEST) || (!(block.getState() instanceof Chest) && !(block.getState() instanceof DoubleChest)))
			return;
		if (plugin.isPlayerPickingLocation(player) && event.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			plugin.setMainChestBlock(player, block);
			return;
		}
		if (!plugin.isDarkBoxChest(block) || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;
		event.setCancelled(true);
		plugin.openChestFor(player);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (Core.getBlockLocationFormatted(event.getBlock()).equals(plugin.getChestLocationFormatted()))
		{
			Player player = event.getPlayer();
			player.sendMessage("§cYou just broke the chest for DarkBox!");
			player.sendMessage("§cThat location will remain DarkBox's known chest location");
			player.sendMessage("§cuntil you change it.");
		}
	}

}