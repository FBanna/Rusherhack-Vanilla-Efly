package org.efly;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
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


    private final BooleanSetting FireWorks = new BooleanSetting("Fireworks", false);

    private final NumberSetting<Integer> FireWorkExtraHeight = new NumberSetting<>("Extra Height", 10, 0, 100)
            .incremental(5);

    private final NumberSetting<Integer> FireworkMaintainPitch = new NumberSetting<>("maintain pitch", 0, -90, 90)
            .incremental(5);

    private final NumberSetting<Integer> FireworkCoolDown = new NumberSetting<>("cooldown", 5, 0, 30)
            .incremental(1);
    private final NumberSetting<Integer> wiggletime = new NumberSetting<>("wiggle time", 0, 0, 100)
            .incremental(5);

    private final NumberSetting<Integer> wigglepitch = new NumberSetting<>("wiggle pitch", 0, 0, 90)
            .incremental(5);

    private final BooleanSetting mode2b2t = new BooleanSetting("2B2T", false);

    public EflyModule() {
        super("FBanna's Efly", "efly description", ModuleCategory.MOVEMENT);

        this.mode2b2t.addSubSettings(this.wiggletime, this.wigglepitch);
        this.FireWorks.addSubSettings(this.FireWorkExtraHeight, this.FireworkMaintainPitch, this.mode2b2t, this.FireworkCoolDown);

        this.registerSettings(
                this.EflyUpPitch,
                this.EflyDownPitch,
                this.MaxHeight,
                this.MinHeight,
                this.Steps,
                this.FireWorks
        );
    }



    @Subscribe
    private void onUpdate(EventUpdate event) {

        mc.player.setXRot(pitch);

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

                // do the 2B2T wiggle
                if (this.mode2b2t.getValue()) {

                    wiggle = wiggle + 1;

                    if (wiggle < this.wiggletime.getValue()) {
                        ChatUtils.print("up");
                        tempPitch = mc.player.getXRot();
                        target = this.EflyUpPitch.getValue() - this.wigglepitch.getValue();
                        i = 0;

                    } else if (wiggle < (this.wiggletime.getValue() * 2)) {

                        ChatUtils.print("down");
                        tempPitch = mc.player.getXRot();
                        target = this.EflyUpPitch.getValue() + this.wigglepitch.getValue();
                        i = 0;
                    } else {
                        wiggle = 0;
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


            if(mc.player.getY() <= this.MinHeight.getValue()) {
                tempPitch = mc.player.getXRot();
                goingUp = true;
                target = this.EflyUpPitch.getValue();
                i = 0;
            }
        }



        lastY = (float) mc.player.getY();

    }


    //setup
    @Override
    public void onEnable() {

        pitch = mc.player.getXRot();

        if(mc.player.getY() < this.MaxHeight.getValue()) {
            if( this.FireWorks.getValue() == false) {
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
