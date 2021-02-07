package plugin.nomore.nmoneclicker.item.ground.builder;

import lombok.Builder;
import lombok.Data;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.widgets.WidgetItem;

import java.awt.*;

@Builder
@Data
public class HighlightingObject
{
    String name;
    int id;
    TileItem tileItem;
    Tile tile;
    int quantity;
    int gePrice;
    int haPrice;
    boolean isNoted;
    int plane;
    Color color;
}
