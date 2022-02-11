package work.lclpnet.mcct.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import work.lclpnet.mcct.transform.WorldTransformer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorldTransformScreen extends Screen implements WorldTransformer.ProgressListener {

    protected final LevelStorage.Session session;
    protected final AtomicBoolean aBoolean = new AtomicBoolean(false);
    protected long lastDotTick = -1;
    protected int dots = 3;
    protected final EditWorldScreen parent;
    protected int steps = 0, currentStep = 0;
    protected Text text = getText(), progressText = getProgressText();
    protected int percentHash = 0;

    public WorldTransformScreen(EditWorldScreen parent, LevelStorage.Session session) {
        super(new TranslatableText("mcct.screen.world_transform"));
        this.session = Objects.requireNonNull(session);
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    protected void init() {
        super.init();
        startTransformation();
    }

    protected void startTransformation() {
        WorldTransformer.create(Objects.requireNonNull(client), session, this)
                .thenCompose(WorldTransformer::transform)
                .thenRun(() -> aBoolean.set(true));
    }

    @Override
    public void tick() {
        super.tick();
        if (aBoolean.get()) {
            final LevelSummary levelSummary = session.getLevelSummary();
            displayToast(new TranslatableText("mcct.screen.world_transform.complete"), new TranslatableText("mcct.screen.world_transform.complete.detail"));
            Objects.requireNonNull(this.client).openScreen(parent);
        }
    }

    public void displayToast(Text title, Text text) {
        if (this.client != null)
            addOrUpdateMultilineToast(this.client.getToastManager(), SystemToast.Type.WORLD_BACKUP, title, text);
    }

    public static void addOrUpdateMultilineToast(ToastManager toastManager, SystemToast.Type type, Text title, Text description) {
        SystemToast systemtoast = toastManager.getToast(SystemToast.class, type);
        if (systemtoast == null) {
            toastManager.add(new SystemToast(type, title, description));
        } else {
            systemtoast.setContent(title, description);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        Objects.requireNonNull(this.client);

        if (lastDotTick == -1 || System.currentTimeMillis() - lastDotTick > 600) {
            lastDotTick = System.currentTimeMillis();
            dots = (dots + 1) % 4;
            this.text = this.getText();
        }

        drawMultiLineCenteredString(matrices, this.client.textRenderer, this.text, 1.5F, this.width / 2, this.height / 2 - 20, 0xff007fff);
        drawCenteredText(matrices, this.client.textRenderer, this.progressText, this.width / 2, this.height / 2, 0xffffffff);

        super.render(matrices, mouseX, mouseY, delta);
    }

    protected void drawMultiLineCenteredString(MatrixStack matrices, TextRenderer textRenderer, Text text, float scale, int x, int y, int color) {
        float neg = 1F / scale;
        x *= neg;
        y *= neg;

        for (OrderedText line : textRenderer.wrapLines(text, this.width)) {
            matrices.push();
            matrices.scale(scale, scale, scale);
            textRenderer.drawWithShadow(matrices, line, (float) (x - textRenderer.getWidth(line) / 2.0), y, color);
            matrices.pop();
            y += textRenderer.fontHeight * scale;
        }
    }

    protected Text getText() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dots; i++) builder.append('.');

        return new TranslatableText("mcct.screen.world_transform.in_progress", builder.toString());
    }

    protected Text getProgressText() {
        final String percent = String.format("%.2f", percentHash / 10e+2F);
        return steps > 0
                ? new TranslatableText("mcct.screen.world_transform.progress_long", percent, currentStep, steps)
                : new TranslatableText("mcct.screen.world_transform.progress_short", percent);
    }

    @Override
    public void setSteps(int steps) {
        this.steps = steps;
    }

    @Override
    public void updateCurrentStep(int currentStep) {
        this.currentStep = currentStep;
        this.percentHash = 0;
        this.progressText = getProgressText();
    }

    @Override
    public void updateProgress(float progress) {
        int hash = (int) (progress * 10e+4F);

        if (hash > percentHash) {
            percentHash = hash;
            this.progressText = getProgressText();
        }
    }
}
