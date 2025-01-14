package org.efly;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class EflyPlugin extends Plugin{

    @Override
    public void onLoad() {
        this.getLogger().info("FBanna's Efly loaded! (Fixed by EOT)");

        final EflyModule eflyModule = new EflyModule();
        final ElytraAutodisconnect elytraAutodisconnectModule = new ElytraAutodisconnect();
        RusherHackAPI.getModuleManager().registerFeature(eflyModule);
        RusherHackAPI.getModuleManager().registerFeature(elytraAutodisconnectModule);
    }

    @Override
    public void onUnload() {
        this.getLogger().info("FBanna's Efly unloaded!");
    }

}