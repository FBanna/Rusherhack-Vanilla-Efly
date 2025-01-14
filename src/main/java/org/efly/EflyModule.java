package org.efly;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.client.api.utils.InventoryUtils;
import org.rusherhack.client.api.accessors.entity.IMixinFireworkRocketEntity;
import org.rusherhack.client.api.utils.WorldUtils;


public class EflyModule extends ToggleableModule {

    int i = -1;
    int wiggle = 0;
    float pitch;
    float target;
    float tempPitch;
    boolean using = false;
    boolean goingUp = false;
    float lastY;
    boolean usingFirework;
    int fireworkDelay;
    private Long timeOfLastRubberband = System.currentTimeMillis();
    private Vec3 lastPosition = new Vec3(0, 0, 0);

    private final NumberSetting<Integer> EflyUpPitch = new NumberSetting<>("Up Pitch", 0, -90, -1)
            .incremental(1)
            .onChange(c -> {
                if(goingUp){
                    pitch = c;
                }
            });

    private final NumberSetting<Integer> EflyDownPitch = new NumberSetting<>("Down Pitch", 0 , 0, 90)
            .incremental(1)
            .onChange(c -> {
                if(!goingUp) {
                    pitch = c;
                }
            });

    private final NumberSetting<Integer> MaxHeight = new NumberSetting<>("Max Height", 500, -64, 2000)
            .incremental(5);

    private final NumberSetting<Integer> MinHeight = new NumberSetting<>("Min Height", 500, -64, 2000)
            .incremental(5);

    private final NumberSetting<Integer> Steps = new NumberSetting<>("Step Count", 20, 1, 100)
            .incremental(1)
            .onChange(c -> i = c+1);

    private final NumberSetting<Integer> RubberbandThreshold = new NumberSetting<>("Min Movement", 5, 1, 200)
            .incremental(1);

    private final NumberSetting<Double> RubberbandTime = new NumberSetting<>("Max Rubberband", 4.0, 1.0/20.0, 30.0)
            .incremental(0.1);

    private final BooleanSetting FireWorks = new BooleanSetting("Fireworks", false);

    private final NumberSetting<Integer> FireWorkExtraHeight = new NumberSetting<>("Extra Height", 10, 0, 100)
            .incremental(5);

    private final NumberSetting<Integer> FireworkMaintainPitch = new NumberSetting<>("maintain pitch", 0, -90, 90)
            .incremental(5);

    private final NumberSetting<Integer> FireworkCoolDown = new NumberSetting<>("cooldown", 5, 0, 30)
            .incremental(1);

    public EflyModule() {
        super("FBanna's Efly", "efly description", ModuleCategory.MOVEMENT);

        this.FireWorks.addSubSettings(this.FireWorkExtraHeight, this.FireworkMaintainPitch, this.FireworkCoolDown);

        this.registerSettings(
                this.EflyUpPitch,
                this.EflyDownPitch,
                this.MaxHeight,
                this.MinHeight,
                this.Steps,
                this.FireWorks,
                this.RubberbandTime,
                this.RubberbandThreshold
        );
    }

    private final ToggleableModule elytraFly = (ToggleableModule)RusherHackAPI.getModuleManager().getFeature("ElytraFly").orElseThrow();



    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null) return;

        if (elytraFly.isToggled()) {
            if (timeOfLastRubberband == null) {
                // begin rubberband timer
                lastPosition = mc.player.position();
                timeOfLastRubberband = System.currentTimeMillis();
            } else if (lastPosition.distanceTo(mc.player.position()) < RubberbandThreshold.getValue()) {
                if (System.currentTimeMillis() - timeOfLastRubberband < RubberbandTime.getValue() * 1000) return;
                // has not moved in last cooldown, recover from rubberband
                elytraFly.setToggled(false);
                usingFirework = false;
                goingUp = true;
                timeOfLastRubberband = null;
            } else {
                // did not rubberband, restart checks for next interval
                timeOfLastRubberband = null;
            }
            return;
        }

        //moves to correct angle
        if (i < this.Steps.getValue() && i != -1) {

           i = i+1;
           pitch = mc.player.getXRot() + (target - tempPitch)/this.Steps.getValue();
           fireworkDelay = this.FireworkCoolDown.getValue();

        } else {

            i = -1;

            // reduce firework delay timer
            if (fireworkDelay > 0) {
                fireworkDelay = fireworkDelay - 1;
            }

        }

        if(usingFirework){

            using = false;
            //check if using firework
            for (Entity entity : WorldUtils.getEntities()) {
                if (entity instanceof FireworkRocketEntity firework) {
                    final IMixinFireworkRocketEntity fireworkAccessor = (IMixinFireworkRocketEntity) firework;
                    if(fireworkAccessor.getAttachedEntity() != null && fireworkAccessor.getAttachedEntity().equals(mc.player)) {

                        using = true;

                    }
                }
            }

            //if above the firework extra height
            if (mc.player.getY() >= (this.MaxHeight.getValue() + this.FireWorkExtraHeight.getValue()) && using) {

                tempPitch = mc.player.getXRot();
                target = this.FireworkMaintainPitch.getValue();
                i = 0;

            //if above height but below extra height
            } else if (mc.player.getY() >= this.MaxHeight.getValue() && !using ) {

                usingFirework = false;
                tempPitch = mc.player.getXRot();
                goingUp = false;
                target = this.EflyDownPitch.getValue();
                i = 0;

            // if below height
            } else {
                if (mc.player.getY() <= MaxHeight.getValue() ){

                    //go to firework
                    if (!mc.player.isHolding(Items.FIREWORK_ROCKET)) {
                        int slot = InventoryUtils.findItemHotbar(Items.FIREWORK_ROCKET);

                        if (slot == -1) {
                            RusherHackAPI.getNotificationManager().chat("NO FIREWORKS");
                        } else {

                            mc.player.getInventory().selected = slot;

                        }
                    }

                    // use if not using, spamming or turning
                    if (!using && fireworkDelay == 0 && i == -1) {

                        mc.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND,5));
                        fireworkDelay = this.FireworkCoolDown.getValue();

                    } else {

                        using = false;

                    }

                }

            }

        // no firework going up
        } else if (goingUp) {

            if (this.FireWorks.getValue() && lastY > mc.player.getY() && i == -1){
                usingFirework = true;
            }

            if(mc.player.getY() >= this.MaxHeight.getValue()) {
                tempPitch = mc.player.getXRot();
                goingUp = false;
                target = this.EflyDownPitch.getValue();
                i = 0;
            }

        //going down
        } else {

            elytraFly.setToggled(true);

//            if(mc.player.getY() <= this.MinHeight.getValue()) {
//                tempPitch = mc.player.getXRot();
//                goingUp = true;
//                target = this.EflyUpPitch.getValue();
//                i = 0;
//            }
        }

        mc.player.setXRot(pitch);
        lastY = (float) mc.player.getY();

    }


    //setup
    @Override
    public void onEnable() {

        pitch = mc.player.getXRot();

        if(mc.player.getY() < this.MaxHeight.getValue()) {
            if( !this.FireWorks.getValue() ) {
                ChatUtils.print("TOO LOW");
                toggle();

            } else {
                target = Float.valueOf(this.EflyUpPitch.getValue());
                goingUp = true;

            }
        } else {
            target = Float.valueOf(this.EflyDownPitch.getValue());
            goingUp = false;


        }
        i = 0;
        tempPitch = mc.player.getXRot();
    }
}
