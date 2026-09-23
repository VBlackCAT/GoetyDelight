package net.v_black_cat.goetydelight.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 一个自包含的 Forge 1.20.1 配置屏。
 *
 * <p>Forge 1.20.1 没有 NeoForge 风格的 {@code ConfigurationScreen}，所以本屏通用地
 * 读取 {@link Config#SPEC} 并为每个配置项渲染一行。布尔值用开关，数字用 EditBox（回车提交），
 * 字符串/列表打开文本编辑子屏。</p>
 *
 * <p>ForgeConfigSpec 里的嵌套节点是 {@code com.electronwill.nightconfig.core.Config} 实例
 * （不是 {@code Map}，也不是 {@code ConfigGroup}——后者在 Forge 1.20.1 里不存在）。
 * 我们递归遍历 {@code SPEC.getValues().valueMap()}，只对叶子 {@link ForgeConfigSpec.ConfigValue}
 * 创建行。</p>
 *
 * <p>特性：可折叠分组、每行"重置为默认值"按钮、tooltip 显示 {@code .comment(...)} 内容
 * （Range 已由 defineInRange 自动追加到 comment 中）。tooltip 在 {@link #render} 的最后
 * 一步绘制，保证位于所有列表条目之上；鼠标坐标直接使用屏幕传入的 GUI 坐标，不经过任何
 * 手动换算，避免 RenderGuiEvent 的坐标系错乱。</p>
 */
@OnlyIn(Dist.CLIENT)
public class GoetyDelightConfigScreen extends Screen {
    private static final String PREFIX = "goetydelight.configuration.";

    /** 已折叠的分组名（顶层 key）。 */
    private final Set<String> collapsedGroups = new HashSet<>();

    private final Screen parent;
    private ConfigList list;

    /** 当前帧待绘制的 tooltip（在所有条目之上绘制）。 */
    private List<Component> pendingTooltip = null;

    public GoetyDelightConfigScreen(Screen parent) {
        super(Component.translatable(PREFIX + "title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new ConfigList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.addWidget(this.list);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 本帧先清空 tooltip，由列表条目在 render 时重新登记。
        this.pendingTooltip = null;

        this.renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);

        // 关键：tooltip 在屏幕渲染的最后一步绘制，位于所有列表条目之上。
        // mouseX/mouseY 是 Screen.render 传入的 GUI 缩放坐标，与 widget 坐标系一致。
        if (this.pendingTooltip != null && !this.pendingTooltip.isEmpty()) {
            this.renderTooltipNoShadow(graphics, this.pendingTooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    // ========================================================================
    //                            配置列表
    // ========================================================================

    private class ConfigList extends ObjectSelectionList<ConfigList.Row> {

        ConfigList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight) {
            super(mc, width, height, y0, y1, itemHeight);
            this.setRenderHeader(false, 0);
            this.rebuild();
        }

        void rebuild() {
            this.clearEntries();

            if (!Config.SPEC.isLoaded()) {
                GoetyDelight.LOGGER.warn("[ConfigScreen] SPEC not loaded, config list will be empty");
                return;
            }

            List<ValueRow> values = new ArrayList<>();
            collectEntries(Config.SPEC.getValues(), "", values);
            values.sort(Comparator.comparing(v -> v.key));

            String currentGroup = null;
            for (ValueRow v : values) {
                String group = v.key.contains(".")
                        ? v.key.substring(0, v.key.indexOf('.'))
                        : "(root)";
                if (!group.equals(currentGroup)) {
                    this.addEntry(new HeaderRow(group));
                    currentGroup = group;
                }
                if (!collapsedGroups.contains(group)) {
                    this.addEntry(v);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void collectEntries(Object node, String prefix, List<ValueRow> out) {
            Map<String, Object> map;

            if (node instanceof com.electronwill.nightconfig.core.Config nc) {
                map = nc.valueMap();
            } else if (node instanceof Map<?, ?> m) {
                map = (Map<String, Object>) m;
            } else {
                return;
            }

            for (Map.Entry<String, Object> e : map.entrySet()) {
                String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
                Object v = e.getValue();

                if (v instanceof com.electronwill.nightconfig.core.Config) {
                    collectEntries(v, key, out);
                } else if (v instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                    ForgeConfigSpec.ValueSpec valueSpec =
                            Config.SPEC.getSpec().getRaw(configValue.getPath());
                    if (valueSpec != null) {
                        out.add(new ValueRow(key, valueSpec, configValue));
                    } else {
                        GoetyDelight.LOGGER.warn("[ConfigScreen] No ValueSpec for path {}",
                                configValue.getPath());
                    }
                }
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 20;
        }

        // --------------------------------------------------------------------
        // 公共行基类
        // --------------------------------------------------------------------
        abstract class Row extends ObjectSelectionList.Entry<Row> {
        }

        // --------------------------------------------------------------------
        // 分组标题行（整行点击可折叠）
        // --------------------------------------------------------------------
        class HeaderRow extends Row {
            private final String groupName;
            private final Component title;

            HeaderRow(String groupName) {
                this.groupName = groupName;
                boolean collapsed = collapsedGroups.contains(groupName);
                this.title = Component.literal("§e" + (collapsed ? "▶ " : "▼ ") + groupName);
            }

            private void toggle() {
                if (collapsedGroups.contains(groupName)) {
                    collapsedGroups.remove(groupName);
                } else {
                    collapsedGroups.add(groupName);
                }
                rebuild();
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                if (isMouseOver) {
                    graphics.fill(left, top, left + width, top + height, 0x33FFFFFF);
                }
                graphics.drawString(GoetyDelightConfigScreen.this.font, this.title,
                        left + 5, top + 6, 0xFFDD44);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.toggle();
                return true;
            }

            @Override
            public Component getNarration() {
                return this.title;
            }
        }

        // --------------------------------------------------------------------
        // 配置项行
        // --------------------------------------------------------------------
        class ValueRow extends Row {
            private final String key;
            private final ForgeConfigSpec.ValueSpec spec;
            private final ForgeConfigSpec.ConfigValue<?> value;
            private final Component label;
            private final AbstractWidget widget;
            private final Button resetButton;
            private final List<Component> tooltipLines;

            ValueRow(String key, ForgeConfigSpec.ValueSpec spec, ForgeConfigSpec.ConfigValue<?> value) {
                this.key = key;
                this.spec = spec;
                this.value = value;
                this.label = Component.literal(relativeKey(key));
                this.widget = createWidget();
                this.resetButton = Button.builder(Component.literal("↺"), b -> resetToDefault())
                        .bounds(0, 0, 20, 20)
                        .build();
                this.tooltipLines = buildTooltip();
            }

            /**
             * comment 直接来自 {@code .comment(...)}。
             * {@code defineInRange} 会自动把 {@code Range: min ~ max} 追加到 comment 末尾，
             * 所以这里不再手动添加 Range。
             */
            private List<Component> buildTooltip() {
                List<Component> lines = new ArrayList<>();

                String comment = spec.getComment();
                if (comment != null && !comment.isBlank()) {
                    for (String line : comment.split("\n")) {
                        lines.add(Component.literal("§7" + line));
                    }
                }

                if (spec.needsWorldRestart()) {
                    if (!lines.isEmpty()) {
                        lines.add(Component.literal(""));
                    }
                    lines.add(Component.literal("§cRequires world restart"));
                }

                return lines;
            }

            private AbstractWidget createWidget() {
                // 布尔值：开关
                if (value instanceof ForgeConfigSpec.BooleanValue) {
                    boolean current = false;
                    try {
                        Object raw = value.get();
                        if (raw instanceof Boolean b) {
                            current = b;
                        }
                    } catch (Exception e) {
                        GoetyDelight.LOGGER.warn("[ConfigScreen] Cannot read boolean for {}: {}",
                                key, e.toString());
                    }
                    return CycleButton.onOffBuilder(current)
                            .displayOnlyValue()
                            .create(0, 0, 130, 20, label, (button, newValue) -> setValue(newValue));
                }

                // 数字：EditBox，回车提交
                if (value instanceof ForgeConfigSpec.IntValue
                        || value instanceof ForgeConfigSpec.DoubleValue
                        || value instanceof ForgeConfigSpec.LongValue) {
                    EditBox box = new EditBox(GoetyDelightConfigScreen.this.font, 0, 0, 130, 20, label);
                    box.setValue(displayValue());
                    box.setMaxLength(32);
                    return box;
                }

                // 字符串/列表：打开编辑子屏
                return Button.builder(Component.literal(displayValue()), button -> openEditor())
                        .width(130)
                        .build();
            }

            private String displayValue() {
                try {
                    Object current = value.get();
                    if (current instanceof List<?> list) {
                        return "[" + list.size() + "]";
                    }
                    return String.valueOf(current);
                } catch (Exception e) {
                    return "<unloaded>";
                }
            }

            private void openEditor() {
                EditValueScreen editor;
                Object current;
                try {
                    current = value.get();
                } catch (Exception e) {
                    GoetyDelight.LOGGER.warn("[ConfigScreen] Cannot read value for {}: {}", key, e.toString());
                    return;
                }

                if (current instanceof List<?> list) {
                    List<String> asStrings = new ArrayList<>();
                    for (Object element : list) {
                        asStrings.add(String.valueOf(element));
                    }
                    String initial = String.join(", ", asStrings);
                    editor = new EditValueScreen(GoetyDelightConfigScreen.this, label, initial, 4096,
                            this::applyList, tooltipLines);
                } else {
                    editor = new EditValueScreen(GoetyDelightConfigScreen.this, label, displayValue(), 4096,
                            this::applyString, tooltipLines);
                }

                GoetyDelightConfigScreen.this.getMinecraft().setScreen(editor);
            }

            private boolean applyNumeric(String text) {
                if (value instanceof ForgeConfigSpec.IntValue) {
                    return applyInteger(text);
                } else if (value instanceof ForgeConfigSpec.DoubleValue) {
                    return applyDouble(text);
                } else if (value instanceof ForgeConfigSpec.LongValue) {
                    return applyLong(text);
                }
                return false;
            }

            private boolean applyList(String text) {
                List<String> items = new ArrayList<>();
                for (String part : text.split(",")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        items.add(trimmed);
                    }
                }
                if (!spec.test(items)) {
                    return false;
                }
                setValue(items);
                return true;
            }

            private boolean applyInteger(String text) {
                try {
                    int parsed = Integer.parseInt(text.trim());
                    if (!spec.test(parsed)) {
                        return false;
                    }
                    setValue(parsed);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            private boolean applyDouble(String text) {
                try {
                    double parsed = Double.parseDouble(text.trim());
                    if (!spec.test(parsed)) {
                        return false;
                    }
                    setValue(parsed);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            private boolean applyLong(String text) {
                try {
                    long parsed = Long.parseLong(text.trim());
                    if (!spec.test(parsed)) {
                        return false;
                    }
                    setValue(parsed);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            private boolean applyString(String text) {
                if (!spec.test(text)) {
                    return false;
                }
                setValue(text);
                return true;
            }

            @SuppressWarnings("unchecked")
            private void setValue(Object newValue) {
                if (!Config.SPEC.isLoaded()) {
                    GoetyDelight.LOGGER.warn("[ConfigScreen] Spec not loaded, cannot set {}", key);
                    return;
                }
                try {
                    ((ForgeConfigSpec.ConfigValue<Object>) value).set(newValue);
                    Config.SPEC.save();

                    if (widget instanceof Button btn && !(widget instanceof CycleButton)) {
                        btn.setMessage(Component.literal(displayValue()));
                    }
                } catch (Exception e) {
                    GoetyDelight.LOGGER.warn("[ConfigScreen] Failed to set value for {}: {}", key, e.toString());
                }
            }

            /** 重置为 spec 里注册的默认值。 */
            private void resetToDefault() {
                Object def = spec.getDefault();
                setValue(def);

                if (widget instanceof EditBox box) {
                    box.setValue(String.valueOf(def));
                    box.setTextColor(0xFFFFFF);
                } else if (widget instanceof CycleButton<?>) {
                    rebuild();
                } else if (widget instanceof Button btn) {
                    btn.setMessage(Component.literal(displayValue()));
                }
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                // 先绘制左侧标签
                graphics.drawString(GoetyDelightConfigScreen.this.font, this.label,
                        left + 5, top + 6, 0xFFFFFF);

                // 计算控件位置（屏幕绝对坐标）
                int resetW = 20;
                int gap = 4;
                int widgetW = this.widget.getWidth();
                int widgetX = left + width - resetW - gap - widgetW - 5;
                int resetX = left + width - resetW - 5;

                // 关键：先设置控件坐标，再进行 hover 检测。否则 widget 的 getX/getY 还是上一帧的，
                // 或者初始 (0,0)，导致 isMouseOver 永远返回 false。
                this.widget.setX(widgetX);
                this.widget.setY(top + 1);
                this.resetButton.setX(resetX);
                this.resetButton.setY(top + 1);

                // 绘制控件
                this.widget.render(graphics, mouseX, mouseY, partialTick);
                this.resetButton.render(graphics, mouseX, mouseY, partialTick);

                // 登记 tooltip（不在这一层绘制，避免被后续条目覆盖）。
                boolean hoverValue = this.widget.isMouseOver(mouseX, mouseY);
                boolean hoverReset = this.resetButton.isMouseOver(mouseX, mouseY);
                if (!tooltipLines.isEmpty() && (hoverValue || hoverReset)) {
                    GoetyDelightConfigScreen.this.pendingTooltip = tooltipLines;
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.resetButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                return this.widget.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                this.resetButton.mouseReleased(mouseX, mouseY, button);
                return this.widget.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                // 数字 EditBox：回车提交
                if (keyCode == 257 /* ENTER */ && this.widget instanceof EditBox box) {
                    if (applyNumeric(box.getValue())) {
                        box.setTextColor(0xFFFFFF);
                    } else {
                        box.setTextColor(0xFF5555);
                    }
                    return true;
                }
                return this.widget.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return this.widget.charTyped(codePoint, modifiers);
            }

            @Override
            public Component getNarration() {
                return this.label;
            }
        }

        /**
         * 去掉顶层路径，例如 {@code food.cake.cakeEffectRadius} -> {@code cake.cakeEffectRadius}。
         */
        private String relativeKey(String key) {
            int idx = key.indexOf('.');
            return idx >= 0 ? key.substring(idx + 1) : key;
        }
    }

    // ========================================================================
    //                          无阴影 tooltip（屏幕层）
    // ========================================================================

    /**
     * 手动绘制 tooltip，不带阴影。
     * 必须在所有列表条目绘制完成之后再调用（本类在 {@link #render} 末尾调用）。
     */
    private void renderTooltipNoShadow(GuiGraphics graphics, List<Component> lines, int mouseX, int mouseY) {
        if (lines.isEmpty()) return;

        graphics.flush();

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);

        int lineHeight = 10;
        int padding = 4;
        int maxWidth = 0;
        for (Component line : lines) {
            maxWidth = Math.max(maxWidth, this.font.width(line));
        }
        int tooltipWidth = maxWidth + padding * 2;
        int tooltipHeight = lines.size() * lineHeight + padding * 2;

        int tx = mouseX + 12;
        int ty = mouseY - 12;
        if (tx + tooltipWidth > this.width) tx = mouseX - 12 - tooltipWidth;
        if (ty + tooltipHeight > this.height) ty = mouseY - 12 - tooltipHeight;
        if (tx < 4) tx = 4;
        if (ty < 4) ty = 4;

        // 黑色背景（不透明）
        graphics.fill(tx, ty, tx + tooltipWidth, ty + tooltipHeight, 0xF0100010);
        graphics.fill(tx, ty, tx + tooltipWidth, ty + 1, 0xFF505000);
        graphics.fill(tx, ty + tooltipHeight - 1, tx + tooltipWidth, ty + tooltipHeight, 0xFF505000);
        graphics.fill(tx, ty, tx + 1, ty + tooltipHeight, 0xFF505000);
        graphics.fill(tx + tooltipWidth - 1, ty, tx + tooltipWidth, ty + tooltipHeight, 0xFF505000);
        int lineY = ty + padding;
        for (Component line : lines) {
            graphics.drawString(this.font, line, tx + padding, lineY, 0xFFFFFF, false);
            lineY += lineHeight;
        }
        graphics.flush();
    }

    // ========================================================================
    //                            编辑值子屏幕
    // ========================================================================

    private static class EditValueScreen extends Screen {
        private final Screen back;
        private final Component prompt;
        private final String initial;
        private final int maxLength;
        private final Predicate<String> apply;
        private final List<Component> tooltipLines;
        private EditBox editBox;

        EditValueScreen(Screen back, Component prompt, String initial, int maxLength,
                        Predicate<String> apply, List<Component> tooltipLines) {
            super(prompt);
            this.back = back;
            this.prompt = prompt;
            this.initial = initial;
            this.maxLength = maxLength;
            this.apply = apply;
            this.tooltipLines = tooltipLines;
        }

        @Override
        protected void init() {
            int boxWidth = 220;
            this.editBox = new EditBox(this.font, this.width / 2 - boxWidth / 2, this.height / 2 - 30,
                    boxWidth, 20, this.prompt);
            this.editBox.setMaxLength(this.maxLength);
            this.editBox.setValue(this.initial);
            this.addRenderableWidget(this.editBox);
            this.setInitialFocus(this.editBox);

            this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                if (this.apply.test(this.editBox.getValue())) {
                    this.minecraft.setScreen(this.back);
                } else {
                    this.editBox.setTextColor(0xFF5555);
                }
            }).bounds(this.width / 2 - 105, this.height / 2, 100, 20).build());

            this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button ->
                            this.minecraft.setScreen(this.back))
                    .bounds(this.width / 2 + 5, this.height / 2, 100, 20)
                    .build());
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(graphics);
            graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);

            // 在编辑框下方显示 comment / range（无阴影）
            if (!tooltipLines.isEmpty()) {
                int y = this.height / 2 + 30;
                for (Component line : tooltipLines) {
                    graphics.drawCenteredString(this.font, line, this.width / 2, y, 0xAAAAAA);
                    y += 12;
                }
            }

            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(this.back);
        }
    }
}