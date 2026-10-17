package de.weltraum;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(WeltraumMod.MODID)
public class WeltraumMod {
    public static final String MODID = "weltraum";

    public WeltraumMod(IEventBus modBus) {
        ModRegistry.register(modBus);
    }
}
