package me.polaris120990.PayRanks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import com.nijiko.permissions.PermissionHandler;

public class PayRanks extends JavaPlugin
{
    public static PermissionHandler Permissions = null;
    static boolean UsePermissions;
	public final Logger logger = Logger.getLogger("Minecraft");
	public File RankFile;
	public static FileConfiguration Rank;
	HashMap<String, String> ranks = new HashMap<String, String>();
	public int Hashlen;
	public static Economy economy = null;
	public static Permission permission = null;
    
	public void onEnable()
	{
	    PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("[" + pdfFile.getName() + "] v" + pdfFile.getVersion() + " has been enabled.");
		RankFile = new File(getDataFolder(), "rankprices.yml");
	    try {
	        firstRun();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    Rank = new YamlConfiguration();
	    loadYamls();
	    setupHash();
	    setupEconomy();
		setupPermission();
	}
	
    private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
    
    private Boolean setupPermission()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return(permission != null);
    }
	public void onDisable()
	{
	    PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("[" + pdfFile.getName() + "] has been disabled.");
	}
	
	@SuppressWarnings("unchecked")
	public void setupHash()
	{
	    List<String> ranklist = Rank.getList("groupslist");
	    String[] rankarray = ranklist.toArray(new String[]{});
	    Integer i = 0;
	    Hashlen = rankarray.length;
	    while(i < rankarray.length)
	    {
	    	String rnum = i.toString();
	    	String key = ("rank" + rnum);
	    	ranks.put(key, rankarray[i]);
	    	i++;
	    }
	}
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args)
	{
		readCommand((Player) sender, CommandLabel, args);
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void readCommand(final Player sender, String command, String[] args)
	{
		if(command.equalsIgnoreCase("rankup"))
		{
			List<String> rlist= Rank.getList("groupslist");
		    String[] rarr = rlist.toArray(new String[]{});
		    Integer Len = rarr.length;
			Integer i = Len;
			Integer nextr = (Len + 1);
			while(i > -1)
			{
				String rnum = i.toString();
		    	String key = ("rank" + rnum);
		    	String rank = ranks.get(key);
		    	if(permission.playerInGroup(sender, rank))
		    	{
		    		if(i == (Len - 1))
		    		{
		    			sender.sendMessage(ChatColor.GOLD + "You are at the highest possible paid rank!");
		    			return;
		    		}
		    		else if(i < Len)
		    		{
		    			String rnumx = nextr.toString();
		    			String keyx = ("rank" + rnumx);
		    			String rankx = ranks.get(keyx);
		    			if(economy.has(sender.getName(), Rank.getInt("groups." + rankx)))
		    			{
		    				permission.playerAddGroup(sender.getWorld(), sender.getName(), rankx);
		    				permission.playerRemoveGroup(sender.getWorld(), sender.getName(), rank);
		    				economy.withdrawPlayer(sender.getName(), Rank.getInt("groups." + rankx));
		    				sender.sendMessage(ChatColor.GREEN + "You have been promoted to the rank of: " + ChatColor.BLUE + rankx);
		    				Bukkit.broadcastMessage(ChatColor.AQUA + sender.getName() + ChatColor.GREEN + " has been promoted to the rank of: " + ChatColor.BLUE + rankx);
		    				return;
		    			}
		    			else
		    			{
		    				Integer price = Rank.getInt("groups." + rankx);
		    				String pricex = price.toString();
		    				sender.sendMessage(ChatColor.RED + "You need " + ChatColor.BLUE + pricex + ChatColor.RED + " to purchase the rank of: " + ChatColor.BLUE + rankx);
		    				return;
		    			}
		    			
		    		}
		    	}
		    	nextr--;
		    	i--;
		    	
			}
		}
	}
	
	
	
    
	public void saveYamls() {
	    try {
	        Rank.save(RankFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	public void loadYamls() {
	    try {
	        Rank.load(RankFile);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	private void firstRun() throws Exception
	{
	    if(!RankFile.exists()){
	        RankFile.getParentFile().mkdirs();
	        copy(getResource("rankprices.yml"), RankFile);
	    }
	}

	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
