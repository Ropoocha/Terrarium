package net.gegy1000.terrarium.client.gui.customization;

import net.gegy1000.terrarium.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.terrarium.client.gui.widget.map.SlippyMapWidget;
import net.gegy1000.terrarium.client.gui.widget.map.component.MarkerMapComponent;
import net.gegy1000.terrarium.server.world.coordinate.SpawnpointDefinition;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class SelectSpawnpointGui extends GuiScreen {
    private static final int SELECT_BUTTON = 0;
    private static final int CANCEL_BUTTON = 1;

    private final TerrariumCustomizationGui parent;

    private SlippyMapWidget mapWidget;
    private MarkerMapComponent markerComponent;

    public SelectSpawnpointGui(TerrariumCustomizationGui parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        if (this.mapWidget != null) {
            this.mapWidget.onGuiClosed();
        }

        this.mapWidget = new SlippyMapWidget(20, 20, this.width - 40, this.height - 60);

        GenerationSettings settings = this.parent.getSettings();
        PropertyContainer properties = settings.getProperties();
        SpawnpointDefinition spawnpointDefinition = settings.getGenerator().getSpawnpointDefinition();

        double spawnpointX = properties.getDouble(spawnpointDefinition.getPropertyX());
        double spawnpointZ = properties.getDouble(spawnpointDefinition.getPropertyZ());
        this.markerComponent = new MarkerMapComponent(new SlippyMapPoint(spawnpointX, spawnpointZ));
        this.mapWidget.addComponent(this.markerComponent);

        this.addButton(new GuiButton(SELECT_BUTTON, this.width / 2 - 154, this.height - 28, 150, 20, I18n.format("gui.done")));
        this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.cancel")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            this.mc.displayGuiScreen(this.parent);
            if (button.id == SELECT_BUTTON) {
                SlippyMapPoint marker = this.markerComponent.getMarker();
                if (marker != null) {
                    this.parent.applySpawnpoint(marker.getSpawnpointX(), marker.getSpawnpointZ());
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        this.mapWidget.draw(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("gui.terrarium.spawnpoint"), this.width / 2, 4, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.mapWidget.mouseClicked(mouseX, mouseY);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        this.mapWidget.mouseDragged(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        this.mapWidget.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.mapWidget.onGuiClosed();
    }
}
