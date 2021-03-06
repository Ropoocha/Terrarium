package net.gegy1000.earth.client;

import net.gegy1000.earth.client.render.LoadingScreenOverlay;
import net.gegy1000.earth.server.ServerProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
    @Override
    public void onPreInit() {
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onPostInit() {
        LoadingScreenOverlay.onPostInit();
        LoadingWorldGetter.onPostInit();
    }
}
