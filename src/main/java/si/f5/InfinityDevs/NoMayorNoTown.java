package si.f5.InfinityDevs;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoMayorNoTown extends JavaPlugin implements Listener {

    // ===== ここだけ触ればOK =====
    private static final String MAYOR_OFFLINE_LIMIT = "1m";
    private static final String CHECK_INTERVAL = "1s";
    // ===========================

    private File dataFile;
    private YamlConfiguration data;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new RuinOutlawClearListener(), this);
        Bukkit.getPluginManager().registerEvents(new ReclaimHomeblockOnlyListener(), this);

        dataFile = new File(getDataFolder(), "mayor_last_logout.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        long intervalTicks = millisToTicks(parseDuration(CHECK_INTERVAL));
        Bukkit.getScheduler().runTaskTimer(this, this::checkTowns, 20L, intervalTicks);

        getLogger().info("NoMayorNoTown enabled | limit=" + MAYOR_OFFLINE_LIMIT);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    // ===== 市長ログアウト時刻を保存 =====
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        data.set(p.getUniqueId().toString(), System.currentTimeMillis());
        saveData();
    }

    // ===== ログインしたらカウント解除 =====
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        data.set(e.getPlayer().getUniqueId().toString(), null);
        saveData();
    }

    private void checkTowns() {
        long now = System.currentTimeMillis();
        long limit = parseDuration(MAYOR_OFFLINE_LIMIT);

        for (Town town : TownyAPI.getInstance().getTowns()) {
            try {
                Resident mayor = town.getMayor();
                UUID uuid = mayor.getUUID();

                Player online = Bukkit.getPlayer(uuid);
                if (online != null && online.isOnline()) continue;

                long logoutTime = data.getLong(uuid.toString(), -1);
                if (logoutTime <= 0) continue;

                if (now - logoutTime >= limit) {
                    getLogger().warning("Deleting town: " + town.getName());
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "ta town " + town.getName() + " delete"
                    );
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "confirm"
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ===== 時間パース =====
    private long parseDuration(String input) {
        Pattern pattern = Pattern.compile("(\\d+)([smhdy])");
        Matcher matcher = pattern.matcher(input.toLowerCase());

        long ms = 0;
        while (matcher.find()) {
            long v = Long.parseLong(matcher.group(1));
            switch (matcher.group(2).charAt(0)) {
                case 's' -> ms += v * 1000L;
                case 'm' -> ms += v * 60_000L;
                case 'h' -> ms += v * 3_600_000L;
                case 'd' -> ms += v * 86_400_000L;
                case 'y' -> ms += v * 31_536_000_000L;
            }
        }
        return ms;
    }

    private long millisToTicks(long ms) {
        return ms / 50L;
    }

    private void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
