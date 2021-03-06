package net.gegy1000.terrarium.server.world.generator.customization;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TerrariumPreset {
    private final String name;
    private final ResourceLocation icon;
    private final GenerationSettings settings;

    public TerrariumPreset(String name, ResourceLocation icon, GenerationSettings settings) {
        this.name = name;
        this.icon = new ResourceLocation(icon.getResourceDomain(), "textures/preset/" + icon.getResourcePath() + ".png");
        this.settings = settings;
    }

    public static TerrariumPreset parse(JsonObject root) {
        String name = JsonUtils.getString(root, "name");
        ResourceLocation icon = new ResourceLocation(JsonUtils.getString(root, "icon"));

        return new TerrariumPreset(name, icon, GenerationSettings.deserialize(root));
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.format("preset." + this.name + ".name");
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedDescription() {
        return I18n.format("preset." + this.name + ".desc");
    }

    public ResourceLocation getIcon() {
        return this.icon;
    }

    public GenerationSettings createSettings() {
        return this.settings.copy();
    }

    public ResourceLocation getWorldType() {
        return this.settings.getWorldType();
    }
}
