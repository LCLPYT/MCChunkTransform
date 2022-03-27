package work.lclpnet.mcct.asm.type.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;

@Environment(EnvType.CLIENT)
public interface IScreen {

    <T extends Element & Drawable & Selectable> T mcct$addDrawableChild(T child);
}
