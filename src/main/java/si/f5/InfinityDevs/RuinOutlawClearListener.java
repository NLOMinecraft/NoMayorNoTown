package si.f5.InfinityDevs;

import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class RuinOutlawClearListener implements Listener {

    @EventHandler
    public void onTownRuined(TownRuinedEvent event) {
        Town town = event.getTown();

        for (Resident resident : new ArrayList<>(town.getOutlaws())) {
            town.removeOutlaw(resident);
        }
    }
}
