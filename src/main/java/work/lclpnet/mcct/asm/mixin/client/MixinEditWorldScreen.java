package work.lclpnet.mcct.asm.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.mcct.asm.type.client.IScreen;
import work.lclpnet.mcct.client.gui.WorldTransformScreen;

@Mixin(EditWorldScreen.class)
public class MixinEditWorldScreen {

    @Shadow @Final private LevelStorage.Session storageSession;

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    public void afterInit(CallbackInfo ci) {
        EditWorldScreen screen = (EditWorldScreen) (Object) this;

        final ButtonWidget.PressAction onPress = button -> MinecraftClient.getInstance().setScreen(new ConfirmScreen(confirmed -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (confirmed) client.setScreen(new WorldTransformScreen(screen, storageSession));
            else client.setScreen(screen);
        }, Text.translatable("mcct.confirm.transform_world"), Text.translatable("mcct.confirm.transform_world.desc")));

        final ButtonWidget transformBtn = new ButtonWidget(screen.width / 2 - 100 + 200 + 4, screen.height / 4 + 96 + 5, 120, 20,
                Text.translatable("mcct.button.transform_world"), onPress);

        ((IScreen) this).mcct$addDrawableChild(transformBtn);
    }
}
