package net.thedarktide.celeo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Provides an all-in-one chest for everyone!<br>
 * Started June 9th, 2012.<br>
 * <br>
 * 
 * @author Celeo, noahgolm, 1770749
 */
public class Core extends JavaPlugin
{

	/*
	 * VARS
	 */

	private final Logger log = Logger.getLogger("Minecraft");
	private final boolean DEBUGGING = true;
	private List<VChest> vChests = null;
	private String isPickingLocation = null;
	private BoxListener boxListener = null;

	/*
	 * Overrides
	 */

	@Override
	public void onEnable()
	{
		boxListener = new BoxListener(this);
		vChests = new ArrayList<VChest>();
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "/config.yml").exists())
			saveDefaultConfig();
		try
		{
			this.loadChests();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		log("Enabled");
	}

	@Override
	public void onDisable()
	{
		saveChests();
		log("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			log("Only Players can send these commands.");
			return true;
		}
		Player player = (Player) sender;
		if (args == null || args.length == 0)
		{
			player.sendMessage("§e/darkbox [parameter(s)]");
			return true;
		}
		if (!hasPermission(player, "darkbox.admin"))
		{
			player.sendMessage("§cYou cannot use that command.");
			return true;
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("view"))
		{
			String name = args[1];
			openOtherChestFor(player, name);
			return true;
		}
		if (args[0].equalsIgnoreCase("choose"))
		{
			if (isPickingLocation != null)
			{
				if (isPickingLocation.equals(player.getName()))
				{
					isPickingLocation = null;
					player.sendMessage("§cLocation choosing cancelled.");
					return true;
				}
				Player tempPlayer = getServer().getPlayer(isPickingLocation);
				if (tempPlayer != null)
					tempPlayer.sendMessage("§cAnother is choosing the location for the chest.");
			}
			isPickingLocation = player.getName();
			player.sendMessage("§bLeft-click the chest that you want to set, or use this command again to cancel.");
			return true;
		}
		if (args[0].equalsIgnoreCase("list"))
		{
			if (vChests == null)
			{
				player.sendMessage("§cVChests list is null!");
				return true;
			}
			if (vChests.isEmpty())
			{
				player.sendMessage("§cNo VChests found!");
				return true;
			}
			String list = "";
			boolean first = true;
			for (VChest vChest : vChests)
			{
				if (first)
					list = vChest.getOwner();
				else
					list += ", " + vChest.getOwner();
				first = false;
			}
			player.sendMessage("§bList of all vChests:");
			player.sendMessage("§b" + list);
			return true;
		}
		if (args[0].equalsIgnoreCase("remove"))
		{
			getConfig().set("chestlocation", "0, 0, 0");
			saveConfig();
			player.sendMessage("§aDarkBox location defaulted to 0, 0, 0");
			return true;
		}
		if (args[0].equalsIgnoreCase("save"))
		{
			saveChests();
			player.sendMessage("§aAll chests saved");
			return true;
		}
		player.sendMessage("§c/darkbox view [player name to view]");
		return true;
	}

	/*
	 * Logging
	 */

	public void log(String message)
	{
		log.info("[DarkBox] " + message);
	}

	public void debug(String message)
	{
		if (DEBUGGING)
			log("<DEBUG> " + message);
	}

	/*
	 * Functions
	 */

	public static boolean hasPermission(Player player, String node)
	{
		if (player.isOp())
			return true;
		return player.hasPermission(node);
	}

	public boolean saveChests()
	{
		if (vChests == null || vChests.isEmpty())
		{
			debug("Tried to save VChests but none were found!");
			return false;
		}
		for (VChest v : vChests)
		{
			int a = 0;
			for (ItemStack i : v.getInventory().getContents())
			{
				if (i == null)
					continue;
				getConfig().set(v.getOwner() + "." + Integer.toString(a), i);
				a++;

			}
			ConfigurationSection vPlayer = getConfig().getConfigurationSection(v.getOwner());
			if (vPlayer == null)
			{
				debug("Could not fetch ConfigurationSection for "
						+ v.getOwner());
				continue;
			}
			for (String key : vPlayer.getKeys(false))
			{
				if (!v.getInventory().contains(vPlayer.getItemStack(key)))
				{
					vPlayer.set(key, null);
				}
			}
			saveConfig();
		}
		return true;
	}

	public void loadChests() throws IOException
	{
		for (String player : getConfig().getKeys(false))
		{
			if (player.equalsIgnoreCase("chestlocation"))
				continue;

			Inventory inventory = this.getServer().createInventory(this.getServer().getPlayer(player), 54, "DarkChest");
			ConfigurationSection pConfig = getConfig().getConfigurationSection(player);
			for (String item : pConfig.getKeys(false))
			{
				ItemStack stack;
				if (!((stack = pConfig.getItemStack(item)) == null))
					inventory.addItem(stack);
			}
			VChest vChest = new VChest(this, player);
			vChest.setInventory(inventory);
			vChests.add(vChest);
		}
	}

	/**
	 * Displays the player's chest to the player itself.
	 * 
	 * @param player
	 */
	public void openChestFor(Player player)
	{
		if (player.hasPermission("darkbox.open"))
		{
			player.openInventory(getVChest(player.getName()).getInventory());
			player.sendMessage("§bViewing your DarkBox inventory.");
			debug("VChest opened for " + player.getName());
		}
		else
			player.sendMessage("§cYou must be a donator to open this box!");
	}

	/**
	 * Displays toView's chest to player. Used for staff.
	 * 
	 * @param player
	 * @param toView
	 */
	public void openOtherChestFor(Player player, String toView)
	{
		if (!hasVChest(toView))
		{
			player.sendMessage("§cThat player does not have a virtual chest.");
			player.sendMessage("§cCreating a new chest.");
			player.openInventory(addNew(toView).getInventory());
			return;
		}
		player.sendMessage("§aOpening chest belonging to §9" + toView);
		player.openInventory(getVChest(toView).getInventory());
	}

	public boolean hasVChest(String name)
	{
		for (VChest chest : vChests)
		{
			if (chest.getOwner().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public VChest getVChest(String name)
	{
		for (VChest chest : vChests)
		{
			if (chest.getOwner().equalsIgnoreCase(name))
			{
				log("VChest returned for " + name);
				return chest;
			}
		}
		return addNew(name);
	}

	public VChest addNew(String name)
	{
		VChest toAdd = new VChest(this, name);
		vChests.add(toAdd);
		debug("New VChest created for " + name);
		return toAdd;
	}

	public boolean isPlayerPickingLocation(Player player)
	{
		return isPickingLocation != null
				&& isPickingLocation.equalsIgnoreCase(player.getName());
	}

	public void setMainChestBlock(Player player, Block block)
	{
		try
		{
			isPickingLocation = null;
			getConfig().set("chestlocation", block.getX() + ", " + block.getY()
					+ ", " + block.getZ());
			saveConfig();
			player.sendMessage("§dLocation set for the chest.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			player.sendMessage("§cAn error occurred while picking that block.");
		}
	}

	public boolean isDarkBoxChest(Block block)
	{
		if (!isChest(block))
			return false;
		if (getBlockLocationFormatted(block).equals(getChestLocationFormatted()))
			return true;
		if (isChest(block.getRelative(BlockFace.EAST))
				&& getBlockLocationFormatted(block.getRelative(BlockFace.EAST)).equals(getChestLocationFormatted()))
			return true;
		if (isChest(block.getRelative(BlockFace.WEST))
				&& getBlockLocationFormatted(block.getRelative(BlockFace.WEST)).equals(getChestLocationFormatted()))
			return true;
		if (isChest(block.getRelative(BlockFace.NORTH))
				&& getBlockLocationFormatted(block.getRelative(BlockFace.NORTH)).equals(getChestLocationFormatted()))
			return true;
		if (isChest(block.getRelative(BlockFace.SOUTH))
				&& getBlockLocationFormatted(block.getRelative(BlockFace.SOUTH)).equals(getChestLocationFormatted()))
			return true;
		return false;
	}

	public static boolean isChest(Block block)
	{
		if (block != null
				&& (block.getState() instanceof Chest || block.getState() instanceof DoubleChest))
			return true;
		return true;
	}

	public static String getBlockLocationFormatted(Block block)
	{
		return block.getX() + ", " + block.getY() + ", " + block.getZ();
	}

	/*
	 * GET and SET
	 */

	public String getChestLocationFormatted()
	{
		String ret = getConfig().getString("chestlocation");
		if (ret == null || ret.equals(""))
		{
			getConfig().set("chestlocation", "0, 0, 0");
			saveConfig();
			return "0, 0, 0";
		}
		return getConfig().getString("chestlocation");
	}

	public List<VChest> getVChests()
	{
		return vChests;
	}

	public void setVChests(List<VChest> virtualChests)
	{
		this.vChests = virtualChests;
	}

	public BoxListener getBoxListener()
	{
		return boxListener;
	}

}