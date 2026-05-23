package net.v_black_cat.goetydelight.compat.jade;

import net.v_black_cat.goetydelight.entities.DollEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ModPlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(DollEntityComponentProvider.INSTANCE, DollEntity.class);
    }
}
