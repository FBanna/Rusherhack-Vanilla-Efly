package org.efly;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.setting.NumberSetting;

public class ElytraAutodisconnect extends ToggleableModule {
    private final NumberSetting<Integer> minElytra = new NumberSetting<>("Min Elytra", 1, 0, 27 + 9)
            .incremental(1);
    public ElytraAutodisconnect() {
        super("Elytra Autodisconnect", "Disconnects when you have x number of elytra left", ModuleCategory.PLAYER);

        this.registerSettings(
                minElytra
        );
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null) return;

        int numberOfElytra = 0;
        for (int i = 9; i < 45; i++) {
            ItemStack item = mc.player.getInventory().getItem(i);
            if (!item.is(Items.ELYTRA)) continue;
            if (item.getMaxDamage() - item.getDamageValue() > item.getMaxDamage() / 2) numberOfElytra++; // mid check but is oke
        }
        if (numberOfElytra <= minElytra.getValue()) mc.disconnect();
    }



}
