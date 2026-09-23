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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class GoetyDelightConfigScreen extends Screen {
    private static final String PREFIX = "goetydelight.configuration.";

    /** 底部按钮区域高度（像素）。ConfigList 不会覆盖这个区域。 */
    private static final int BOTTOM_BUTTON_AREA_HEIGHT = 32;

    private final Set<String> collapsedGroups = new HashSet<>();
    private final Screen parent;
    private ConfigList list;

    /** 当前帧待绘制的 tooltip。 */
    private List<Component> pendingTooltip = null;

    /** 当前持有焦点的 EditBox（用于键盘输入路由）。 */
    private EditBox focusedBox = null;

    /** 待保存的修改：key 为 ConfigValue 引用，value 为新值。 */
    private final Map<ForgeConfigSpec.ConfigValue<?>, Object> pendingChanges = new LinkedHashMap<>();

    /** 保存按钮引用，用于根据脏数据状态切换是否可用。 */
    private Button saveButton;

    public GoetyDelightConfigScreen(Screen parent) {
        super(Component.translatable(PREFIX + "title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // 列表底部边界：预留底部按钮区域，避免覆盖底部按钮
        int listBottom = this.height - BOTTOM_BUTTON_AREA_HEIGHT;

        this.list = new ConfigList(this.minecraft, this.width, this.height, 32, listBottom, 25);
        this.addWidget(this.list);

        int buttonY = this.height - 27;
        int buttonWidth = 100;
        int gap = 8;
        int totalWidth = buttonWidth * 3 + gap * 2;
        int startX = this.width / 2 - totalWidth / 2;

        // 保存按钮
        this.saveButton = Button.builder(
                        Component.translatable(PREFIX + "save"),
                        button -> this.saveChanges())
                .bounds(startX, buttonY, buttonWidth, 20)
                .build();

        // 撤销按钮
        Button discardButton = Button.builder(
                        Component.translatable(PREFIX + "discard"),
                        button -> this.discardChanges())
                .bounds(startX + buttonWidth + gap, buttonY, buttonWidth, 20)
                .build();

        // 完成按钮
        Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(startX + (buttonWidth + gap) * 2, buttonY, buttonWidth, 20)
                .build();

        this.addRenderableWidget(this.saveButton);
        this.addRenderableWidget(discardButton);
        this.addRenderableWidget(doneButton);

        this.updateSaveButtonState();
    }

    // ========================================================================
    //                            暂存管理
    // ========================================================================

    /** 记录一个待保存的修改。 */
    void stageChange(ForgeConfigSpec.ConfigValue<?> cv, Object newValue) {
        this.pendingChanges.put(cv, newValue);
        this.updateSaveButtonState();
    }

    /** 判断某项是否已被暂存修改。 */
    boolean hasPendingChange(ForgeConfigSpec.ConfigValue<?> cv) {
        return this.pendingChanges.containsKey(cv);
    }

    /** 取暂存值，若没有则返回 null。 */
    Object getPendingChange(ForgeConfigSpec.ConfigValue<?> cv) {
        return this.pendingChanges.get(cv);
    }

    /** 提交所有暂存修改并写入磁盘。 */
    @SuppressWarnings("unchecked")
    private void saveChanges() {
        if (this.pendingChanges.isEmpty()) {
            return;
        }
        if (!Config.SPEC.isLoaded()) {
            GoetyDelight.LOGGER.warn("[ConfigScreen] Spec not loaded, cannot save");
            return;
        }

        for (Map.Entry<ForgeConfigSpec.ConfigValue<?>, Object> entry : this.pendingChanges.entrySet()) {
            try {
                ((ForgeConfigSpec.ConfigValue<Object>) entry.getKey()).set(entry.getValue());
            } catch (Exception e) {
                GoetyDelight.LOGGER.warn("[ConfigScreen] Failed to set value: {}", e.toString());
            }
        }

        try {
            Config.SPEC.save();
        } catch (Exception e) {
            GoetyDelight.LOGGER.warn("[ConfigScreen] Failed to save config: {}", e.toString());
        }

        this.pendingChanges.clear();
        this.updateSaveButtonState();
        this.list.rebuild(); // 刷新显示
    }

    /** 丢弃所有暂存修改。 */
    private void discardChanges() {
        this.pendingChanges.clear();
        this.updateSaveButtonState();
        this.list.rebuild();
    }

    private void updateSaveButtonState() {
        if (this.saveButton != null) {
            this.saveButton.active = !this.pendingChanges.isEmpty();
        }
    }

    // ========================================================================
    //                            渲染 & 生命周期
    // ========================================================================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.pendingTooltip = null;

        this.renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (this.pendingTooltip != null && !this.pendingTooltip.isEmpty()) {
            this.renderTooltipNoShadow(graphics, this.pendingTooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            if (!this.pendingChanges.isEmpty()) {
                GoetyDelight.LOGGER.info("[ConfigScreen] Discarding {} unsaved change(s)",
                        this.pendingChanges.size());
                this.pendingChanges.clear();
            }
            this.minecraft.setScreen(this.parent);
        }
    }

    // ========================================================================
    //                            配置列表
    // ========================================================================

    private class ConfigList extends ObjectSelectionList<ConfigList.Row> {

        /** 底部禁交互区域的起始 Y 坐标（即此 Y 值以下不响应事件与 hover）。 */
        private final int bottomMaskY;

        ConfigList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight) {
            super(mc, width, height, y0, y1, itemHeight);
            this.setRenderHeader(false, 0);
            // 列表实际的底部（getBottom()）以下都是禁交互区域
            this.bottomMaskY = y1;
            this.rebuild();
        }

        /** 判断某个坐标是否落在底部遮罩区域内。 */
        boolean isInBottomMask(double mouseX, double mouseY) {
            return mouseY >= this.bottomMaskY;
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
        // 布局：把每行及内部 widget 的坐标刷新一遍
        // --------------------------------------------------------------------

        void layoutRows() {
            for (int i = 0; i < this.getItemCount(); i++) {
                Row row = this.children().get(i);
                if (row instanceof ValueRow vr) {
                    int top = this.getRowTop(i);
                    int left = this.getRowLeft();
                    int width = this.getRowWidth();
                    int height = this.itemHeight;
                    vr.layout(left, top, width, height);
                }
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.layoutRows();

            // 如果鼠标落在底部遮罩区域，向内部 widget 传递一个"无效"的鼠标坐标，
            // 这样 widget 的 hover 状态和 tooltip 都不会被触发。
            boolean masked = isInBottomMask(mouseX, mouseY);
            int renderMouseX = masked ? -1 : mouseX;
            int renderMouseY = masked ? -1 : mouseY;

            super.render(graphics, renderMouseX, renderMouseY, partialTick);
        }

        // --------------------------------------------------------------------
        // 事件转发：找到鼠标下的 ValueRow
        // --------------------------------------------------------------------

        private ValueRow rowAt(double mouseX, double mouseY) {
            // 底部遮罩区域：直接返回 null，不命中任何行
            if (isInBottomMask(mouseX, mouseY)) {
                return null;
            }
            for (Row row : this.children()) {
                if (row instanceof ValueRow vr && vr.hitTest(mouseX, mouseY)) {
                    return vr;
                }
            }
            return null;
        }

        /** 清除所有 EditBox 的焦点，并同步屏幕的 focusedBox。 */
        void clearEditFocus() {
            for (Row r : this.children()) {
                if (r instanceof ValueRow vr && vr.widget instanceof EditBox box) {
                    box.setFocused(false);
                }
            }
            GoetyDelightConfigScreen.this.focusedBox = null;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // 底部遮罩区域：放行给底层按钮
            if (isInBottomMask(mouseX, mouseY)) {
                return false;
            }

            this.layoutRows();
            ValueRow row = rowAt(mouseX, mouseY);
            if (row != null && row.mouseClicked(mouseX, mouseY, button)) {
                if (row.widget instanceof EditBox box && box.isFocused()) {
                    GoetyDelightConfigScreen.this.focusedBox = box;
                } else {
                    GoetyDelightConfigScreen.this.focusedBox = null;
                }
                return true;
            }

            clearEditFocus();
            // 没有命中任何行：不消耗事件，让事件继续传递（比如到底层按钮）
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (isInBottomMask(mouseX, mouseY)) {
                return false;
            }
            this.layoutRows();
            ValueRow row = rowAt(mouseX, mouseY);
            if (row != null && row.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (isInBottomMask(mouseX, mouseY)) {
                return false;
            }
            ValueRow row = rowAt(mouseX, mouseY);
            if (row != null && row.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            // 鼠标在底部遮罩区域时，不响应滚动
            if (isInBottomMask(mouseX, mouseY)) {
                return false;
            }
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            EditBox focused = GoetyDelightConfigScreen.this.focusedBox;
            if (focused != null && focused.isFocused()) {
                for (Row row : this.children()) {
                    if (row instanceof ValueRow vr && vr.widget == focused) {
                        if (vr.keyPressed(keyCode, scanCode, modifiers)) {
                            return true;
                        }
                        break;
                    }
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            EditBox focused = GoetyDelightConfigScreen.this.focusedBox;
            if (focused != null && focused.isFocused()) {
                for (Row row : this.children()) {
                    if (row instanceof ValueRow vr && vr.widget == focused) {
                        if (vr.charTyped(codePoint, modifiers)) {
                            return true;
                        }
                        break;
                    }
                }
            }
            return super.charTyped(codePoint, modifiers);
        }

        // --------------------------------------------------------------------
        // 行基类
        // --------------------------------------------------------------------
        abstract class Row extends ObjectSelectionList.Entry<Row> {
        }

        // --------------------------------------------------------------------
        // 分组标题行
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

            /** 用于在 EditBox responder 里避免递归触发（构造时 setValue 会触发 responder）。 */
            private boolean suppressResponder = false;

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
                if (value instanceof ForgeConfigSpec.BooleanValue) {
                    boolean current = false;
                    Object pending = GoetyDelightConfigScreen.this.getPendingChange(value);
                    if (pending instanceof Boolean b) {
                        current = b;
                    } else {
                        try {
                            Object raw = value.get();
                            if (raw instanceof Boolean b) {
                                current = b;
                            }
                        } catch (Exception e) {
                            GoetyDelight.LOGGER.warn("[ConfigScreen] Cannot read boolean for {}: {}",
                                    key, e.toString());
                        }
                    }
                    return CycleButton.onOffBuilder(current)
                            .displayOnlyValue()
                            .create(0, 0, 130, 20, label, (button, newValue) -> setValue(newValue));
                }

                if (value instanceof ForgeConfigSpec.IntValue
                        || value instanceof ForgeConfigSpec.DoubleValue
                        || value instanceof ForgeConfigSpec.LongValue) {
                    EditBox box = new EditBox(GoetyDelightConfigScreen.this.font, 0, 0, 130, 20, label);
                    box.setMaxLength(32);

                    // 先设值（此时 responder 还没设置，不会触发）
                    box.setValue(displayValue());

                    // 设置 responder：每次输入都尝试暂存
                    box.setResponder(text -> {
                        if (suppressResponder) {
                            return;
                        }
                        if (value instanceof ForgeConfigSpec.IntValue) {
                            applyInteger(text);
                        } else if (value instanceof ForgeConfigSpec.DoubleValue) {
                            applyDouble(text);
                        } else if (value instanceof ForgeConfigSpec.LongValue) {
                            applyLong(text);
                        }
                    });
                    return box;
                }

                return Button.builder(Component.literal(displayValue()), button -> openEditor())
                        .width(130)
                        .build();
            }

            private String displayValue() {
                // 优先读取暂存值
                Object current = GoetyDelightConfigScreen.this.getPendingChange(this.value);
                if (current == null) {
                    try {
                        current = value.get();
                    } catch (Exception e) {
                        return "<unloaded>";
                    }
                }
                if (current instanceof List<?> list) {
                    return "[" + list.size() + "]";
                }
                return String.valueOf(current);
            }

            private void openEditor() {
                EditValueScreen editor;
                Object current;
                Object pending = GoetyDelightConfigScreen.this.getPendingChange(this.value);
                if (pending != null) {
                    current = pending;
                } else {
                    try {
                        current = value.get();
                    } catch (Exception e) {
                        GoetyDelight.LOGGER.warn("[ConfigScreen] Cannot read value for {}: {}", key, e.toString());
                        return;
                    }
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
                    // 只暂存，不立刻写入磁盘
                    GoetyDelightConfigScreen.this.stageChange(this.value, newValue);

                    // 同步 Button（非 CycleButton）的显示
                    if (widget instanceof Button btn && !(widget instanceof CycleButton)) {
                        // 抑制 responder，避免 setMessage 递归
                        suppressResponder = true;
                        btn.setMessage(Component.literal(displayValue()));
                        suppressResponder = false;
                    }
                } catch (Exception e) {
                    GoetyDelight.LOGGER.warn("[ConfigScreen] Failed to stage value for {}: {}", key, e.toString());
                }
            }

            @SuppressWarnings("unchecked")
            private void resetToDefault() {
                Object def = spec.getDefault();

                // 直接同步 UI，再暂存，避免 setValue 里 setMessage 引起死循环
                if (widget instanceof EditBox box) {
                    suppressResponder = true;
                    box.setValue(String.valueOf(def));
                    suppressResponder = false;
                    box.setTextColor(0xFFFFFF);
                } else if (widget instanceof CycleButton<?> cycle) {
                    ((CycleButton<Boolean>) cycle).setValue((Boolean) def);
                } else if (widget instanceof Button btn) {
                    // 按钮的显示下面统一刷新
                }

                setValue(def);
            }

            // ----------------------------------------------------------------
            // 布局 / 交互判定
            // ----------------------------------------------------------------

            /** 由 ConfigList.layoutRows() 调用，设置本行及内部 widget 的坐标。 */
            void layout(int left, int top, int width, int height) {
                int resetW = 20;
                int gap = 4;
                int widgetW = this.widget.getWidth();
                int widgetX = left + width - resetW - gap - widgetW - 5;
                int resetX = left + width - resetW - 5;
                this.widget.setX(widgetX);
                this.widget.setY(top + 1);
                this.resetButton.setX(resetX);
                this.resetButton.setY(top + 1);
            }

            boolean hitTest(double mouseX, double mouseY) {
                return this.widget.isMouseOver(mouseX, mouseY)
                        || this.resetButton.isMouseOver(mouseX, mouseY);
            }

            boolean isEditing() {
                return this.widget instanceof EditBox box && box.isFocused();
            }

            // ----------------------------------------------------------------
            // 渲染
            // ----------------------------------------------------------------

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                int color = 0xFFFFFF;
                // 有未保存修改时用黄色显示
                if (GoetyDelightConfigScreen.this.hasPendingChange(this.value)) {
                    color = 0xFFDD44;
                }
                graphics.drawString(GoetyDelightConfigScreen.this.font, this.label,
                        left + 5, top + 6, color);

                // 底部遮罩区域：向 widget 传递无效坐标，禁止 hover / tooltip
                boolean masked = ConfigList.this.isInBottomMask(mouseX, mouseY);
                int renderMouseX = masked ? -1 : mouseX;
                int renderMouseY = masked ? -1 : mouseY;

                this.widget.render(graphics, renderMouseX, renderMouseY, partialTick);
                this.resetButton.render(graphics, renderMouseX, renderMouseY, partialTick);

                if (!masked) {
                    boolean hoverValue = this.widget.isMouseOver(mouseX, mouseY);
                    boolean hoverReset = this.resetButton.isMouseOver(mouseX, mouseY);
                    if (!tooltipLines.isEmpty() && (hoverValue || hoverReset)) {
                        GoetyDelightConfigScreen.this.pendingTooltip = tooltipLines;
                    }
                }
            }

            // ----------------------------------------------------------------
            // 事件
            // ----------------------------------------------------------------

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.resetButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                if (this.widget.isMouseOver(mouseX, mouseY)) {
                    if (this.widget instanceof EditBox box) {
                        box.setFocused(true);
                    }
                    if (this.widget.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                boolean a = this.resetButton.mouseReleased(mouseX, mouseY, button);
                boolean b = this.widget.mouseReleased(mouseX, mouseY, button);
                return a || b;
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                boolean a = this.resetButton.mouseDragged(mouseX, mouseY, button, dragX, dragY);
                boolean b = this.widget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
                return a || b;
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (this.widget instanceof EditBox box && box.isFocused()) {
                    // ESC：退出编辑
                    if (keyCode == 256 /* ESC */) {
                        box.setFocused(false);
                        GoetyDelightConfigScreen.this.focusedBox = null;
                        return true;
                    }
                    // ENTER：立即校验（值已在 responder 中暂存）
                    if (keyCode == 257 /* ENTER */) {
                        boolean ok;
                        if (value instanceof ForgeConfigSpec.IntValue
                                || value instanceof ForgeConfigSpec.DoubleValue
                                || value instanceof ForgeConfigSpec.LongValue) {
                            ok = applyNumeric(box.getValue());
                        } else {
                            ok = applyString(box.getValue());
                        }
                        box.setTextColor(ok ? 0xFFFFFF : 0xFF5555);
                        return true;
                    }
                    return box.keyPressed(keyCode, scanCode, modifiers);
                }
                return this.widget.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                if (this.widget instanceof EditBox box && box.isFocused()) {
                    return box.charTyped(codePoint, modifiers);
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return this.label;
            }
        }

        private String relativeKey(String key) {
            int idx = key.indexOf('.');
            return idx >= 0 ? key.substring(idx + 1) : key;
        }
    }

    // ========================================================================
    //                          无阴影 tooltip
    // ========================================================================

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

        graphics.fill(tx, ty, tx + tooltipWidth, ty + tooltipHeight, 0xFF100010);
        graphics.fill(tx, ty, tx + tooltipWidth, ty + 1, 0xFF505000);
        graphics.fill(tx, ty + tooltipHeight - 1, tx + tooltipWidth, ty + tooltipHeight, 0xFF505000);
        graphics.fill(tx, ty, tx + 1, ty + tooltipHeight, 0xFF505000);
        graphics.fill(tx + tooltipWidth - 1, ty, tx + tooltipWidth, ty + tooltipHeight, 0xFF505000);

        int lineY = ty + padding;
        for (Component line : lines) {
            graphics.drawString(this.font, line, tx + padding, lineY, 0xFFFFFF, false);
            lineY += lineHeight;
        }

        graphics.pose().popPose();
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