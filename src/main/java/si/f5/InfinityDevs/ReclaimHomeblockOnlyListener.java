package si.f5.InfinityDevs;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.town.TownPreReclaimEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ReclaimHomeblockOnlyListener implements Listener {

    @EventHandler
    public void onTownPreReclaim(TownPreReclaimEvent event) throws TownyException {
        Player player = event.getPlayer();
        Town town = event.getTown();

        if (town == null) {
            event.setCancelled(true);
            return;
        }

        TownBlock current = TownyAPI.getInstance()
                .getTownBlock(player.getLocation());

        if (current == null || !current.equals(town.getHomeBlock())) {
            event.setCancelled(true);
            event.setCancelMessage("§chomeblockに立っていない場合、reclaimは不可能です");
        }
    }
}
