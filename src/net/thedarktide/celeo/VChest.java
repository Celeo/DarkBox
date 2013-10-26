package net.thedarktide.celeo;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Virtual chest that will belong to a player.
 */
public class VChest
{

	/*
	 * VARS
	 */

	/** */
	private final Core plugin;
	private final String owner;
	private Inventory inventory;
	
	/*
	 * Constructor
	 */

	public VChest(Core instance, String name)
	{
		plugin = instance;
		owner = name;
		setupInventory();
	}

	/*
	 * Functions
	 */

	public void setupInventory()
	{
		inventory = plugin.getServer().createInventory(plugin.getServer().getPlayer(owner), 45, "DarkChest");
		if (inventory == null)
			plugin.log("ERROR: Inventory is null!");
	}

	public void addItemToInventory(ItemStack itemStack)
	{
		inventory.addItem(itemStack);
	}

	public void addItemsToInventory(ItemStack... itemStack)
	{
		for (ItemStack i : itemStack)
		{
			inventory.addItem(i);
		}
	}

	public String allToString()
	{
		if (inventory == null || inventory.getContents() == null || inventory.getContents().length == 0)
			return "";
		String string = "";
		//boolean first = true;
		for (ItemStack i : inventory.getContents())
		{
			if (i == null)
				continue;
			//if (first)
			//	string = itemStackToString(i);
			//else
			string += itemStackToString(i) + ";";
			//first = false;
		}
		return string;
	}

	public static String itemStackToString(ItemStack itemStack)
	{
		return String.valueOf(itemStack.getTypeId() + "x" + itemStack.getAmount() + "x" + itemStack.getDurability());
	}

	public void loadAllFromString(String string)
	{
		for (String s : string.split(";"))
		{
			inventory.addItem(stringToItemStack(s));
		}
	}

	public static ItemStack stringToItemStack(String string)
	{
		String[] str = string.split("x");
		int[] data = new int[str.length];
		for (int i = 0; i < str.length; i ++)
			data[i] = Integer.valueOf(str[i]).intValue();
		ItemStack itemStack = new ItemStack(data[0]);
		itemStack.setAmount(data[1]);
		itemStack.setDurability((short) data[2]);
		return itemStack;
	}

	/*
	 * GET and SET
	 */

	public Core getPlugin()
	{
		return plugin;
	}

	public String getOwner()
	{
		return owner;
	}

	public Inventory getInventory()
	{
		return inventory;
	}
	
	public void setInventory(Inventory inventory)
	{
		this.inventory = inventory;
	}

}