package org.efly;

import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.logging.Logger;

public class ElytraAutodisconnect extends ToggleableModule {
    private final NumberSetting<Integer> minElytra = new NumberSetting<>("Min Elytra", 1, 0, 27 + 9)
            .incremental(1);

    private final BooleanSetting disable = new BooleanSetting("Disable on dc", true);

    public ElytraAutodisconnect() {
        super("Elytra Autodisconnect", "Disconnects when you have x number of elytra left", ModuleCategory.PLAYER);

        this.registerSettings(
                minElytra,
                disable
        );
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null) return;



        int numberOfElytra = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack item = mc.player.getInventory().getItem(i);
            if (!item.is(Items.ELYTRA)) continue;
            if (item.getMaxDamage() - item.getDamageValue() > item.getMaxDamage() / 2) numberOfElytra+=item.getCount();
        }

        if (numberOfElytra <= minElytra.getValue()) {
            Logger.getLogger("FBanna").info("im here please help; me ");
            //if(disable.getValue()) {
            //    this.setToggled(false);
            //}

            mc.player.connection.onDisconnect(new DisconnectionDetails(Component.literal("NO ELYTRAS")));
            //mc.player.connection.onDisconnect();
        }

    }



}
