package org.efly;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class EflyPlugin extends Plugin{

    @Override
    public void onLoad() {
        this.getLogger().info("FBanna's Efly loaded!");

        final EflyModule eflyModule = new EflyModule();
        RusherHackAPI.getModuleManager().registerFeature(eflyModule);
    }

    @Override
    public void onUnload() {
        this.getLogger().info("FBanna's Efly unloaded!");
    }

}