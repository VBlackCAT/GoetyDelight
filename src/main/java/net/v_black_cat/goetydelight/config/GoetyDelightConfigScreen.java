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

import java.util.*;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class GoetyDelightConfigScreen extends Screen {
    private static final String PREFIX = "goetydelight.configuration.";

    /** 底部按钮区域高度（像素）。ConfigList 不会覆盖这个区域。 */
    private static final int BOTTOM_BUTTON_AREA_HEIGHT = 32;

    /**
     * 已折叠的分组（用完整路径作为 key，例如 "food"、"food.polarice"、
     * "food.metamorphicScent.grass"）。每一层都可以独立折叠。
     */
    private final Set<String> collapsedGroups = new HashSet<>();

    /**
     * 已展开的列表配置 key 集合。
     * 由于 ConfigList.rebuild() 会重建所有 ValueRow，展开状态不能放在 ValueRow 里，
     * 必须提升到 Screen 层，按 key 保存。
     */
    private final Set<String> expandedListKeys = new HashSet<>();

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
        int listBottom = this.height - BOTTOM_BUTTON_AREA_HEIGHT;

        this.list = new ConfigList(this.minecraft, this.width, this.height, 32, listBottom, 25);
        this.addWidget(this.list);

        int buttonY = this.height - 27;
        int buttonWidth = 100;
        int gap = 8;
        int totalWidth = buttonWidth * 3 + gap * 2;
        int startX = this.width / 2 - totalWidth / 2;

        this.saveButton = Button.builder(
                        Component.translatable(PREFIX + "save"),
                        button -> this.saveChanges())
                .bounds(startX, buttonY, buttonWidth, 20)
                .build();

        Button discardButton = Button.builder(
                        Component.translatable(PREFIX + "discard"),
                        button -> this.discardChanges())
                .bounds(startX + buttonWidth + gap, buttonY, buttonWidth, 20)
                .build();

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

    void stageChange(ForgeConfigSpec.ConfigValue<?> cv, Object newValue) {
        this.pendingChanges.put(cv, newValue);
        this.updateSaveButtonState();
    }

    boolean hasPendingChange(ForgeConfigSpec.ConfigValue<?> cv) {
        return this.pendingChanges.containsKey(cv);
    }

    Object getPendingChange(ForgeConfigSpec.ConfigValue<?> cv) {
        return this.pendingChanges.get(cv);
    }

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
        this.list.rebuild();
    }

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
    //                          语言判断（类级）
    // ========================================================================

    /** 当前客户端语言是否为中文。 */
    private static boolean isChineseLanguage() {
        try {
            String code = Minecraft.getInstance().getLanguageManager().getSelected();
            if (code == null) return false;
            code = code.toLowerCase(Locale.ROOT);
            return code.startsWith("zh") || code.equals("lzh");
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    //                            配置列表
    // ========================================================================

    private class ConfigList extends ObjectSelectionList<ConfigList.Row> {

        private final int bottomMaskY;

        ConfigList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight) {
            super(mc, width, height, y0, y1, itemHeight);
            this.setRenderHeader(false, 0);
            this.bottomMaskY = y1;
            this.rebuild();
        }

        boolean isInBottomMask(double mouseX, double mouseY) {
            return mouseY >= this.bottomMaskY;
        }

        // --------------------------------------------------------------------
        // 树形结构
        // --------------------------------------------------------------------

        /**
         * 树节点：
         * - valueRow != null 时表示这是个值节点（叶子）。
         * - 否则是分组节点，children 里是它的子节点。
         */
        class Node {
            final String name;
            final String fullPath;
            final int depth;
            ValueRow valueRow;
            final List<Node> children = new ArrayList<>();

            Node(String name, String fullPath, int depth) {
                this.name = name;
                this.fullPath = fullPath;
                this.depth = depth;
            }
        }

        private Node buildTree() {
            Node root = new Node("", "", -1);

            if (!Config.SPEC.isLoaded()) {
                GoetyDelight.LOGGER.warn("[ConfigScreen] SPEC not loaded, config list will be empty");
                return root;
            }

            buildTreeRecursive(Config.SPEC.getValues(), "", root, 0);
            return root;
        }

        @SuppressWarnings("unchecked")
        private void buildTreeRecursive(Object node, String prefix, Node parent, int depth) {
            Map<String, Object> map;

            if (node instanceof com.electronwill.nightconfig.core.Config nc) {
                map = nc.valueMap();
            } else if (node instanceof Map<?, ?> m) {
                map = (Map<String, Object>) m;
            } else {
                return;
            }

            List<String> keys = new ArrayList<>(map.keySet());
            keys.sort(Comparator.naturalOrder());

            for (String keyName : keys) {
                Object v = map.get(keyName);
                String fullPath = prefix.isEmpty() ? keyName : prefix + "." + keyName;

                if (v instanceof com.electronwill.nightconfig.core.Config) {
                    Node child = new Node(keyName, fullPath, depth);
                    parent.children.add(child);
                    buildTreeRecursive(v, fullPath, child, depth + 1);
                } else if (v instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                    ForgeConfigSpec.ValueSpec valueSpec =
                            Config.SPEC.getSpec().getRaw(configValue.getPath());
                    if (valueSpec == null) {
                        GoetyDelight.LOGGER.warn("[ConfigScreen] No ValueSpec for path {}",
                                configValue.getPath());
                        continue;
                    }
                    Node child = new Node(keyName, fullPath, depth);
                    child.valueRow = new ValueRow(fullPath, valueSpec, configValue, depth);
                    parent.children.add(child);
                }
            }
        }

        void rebuild() {
            this.clearEntries();

            Node root = buildTree();
            for (Node child : root.children) {
                addNodeRecursive(child);
            }
        }

        private void addNodeRecursive(Node node) {
            if (node.valueRow != null) {
                this.addEntry(node.valueRow);
                if (node.valueRow.expanded) {
                    node.valueRow.rebuildElementRows();
                    for (ListElementRow er : node.valueRow.elementRows) {
                        this.addEntry(er);
                    }
                }
                return;
            }

            this.addEntry(new HeaderRow(node.name, node.fullPath, node.depth));

            if (collapsedGroups.contains(node.fullPath)) {
                return;
            }
            for (Node child : node.children) {
                addNodeRecursive(child);
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

        void layoutRows() {
            for (int i = 0; i < this.getItemCount(); i++) {
                Row row = this.children().get(i);
                int top = this.getRowTop(i);
                int left = this.getRowLeft();
                int width = this.getRowWidth();
                int height = this.itemHeight;

                if (row instanceof ValueRow vr) {
                    vr.layout(left, top, width, height);
                } else if (row instanceof ListElementRow er) {
                    er.layout(left, top, width, height);
                }
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.layoutRows();

            boolean masked = isInBottomMask(mouseX, mouseY);
            int renderMouseX = masked ? -1 : mouseX;
            int renderMouseY = masked ? -1 : mouseY;

            super.render(graphics, renderMouseX, renderMouseY, partialTick);
        }

        private boolean isInsideRow(int i, double mouseX, double mouseY) {
            int top = this.getRowTop(i);
            int left = this.getRowLeft();
            int width = this.getRowWidth();
            int height = this.itemHeight;
            return mouseX >= left && mouseX < left + width
                    && mouseY >= top && mouseY < top + height;
        }

        private ValueRow rowAt(double mouseX, double mouseY) {
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

        void clearEditFocus() {
            for (Row r : this.children()) {
                if (r instanceof ValueRow vr && vr.widget instanceof EditBox box) {
                    box.setFocused(false);
                }
                if (r instanceof ListElementRow er) {
                    er.box.setFocused(false);
                }
            }
            GoetyDelightConfigScreen.this.focusedBox = null;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isInBottomMask(mouseX, mouseY)) {
                return false;
            }

            this.layoutRows();

            for (int i = 0; i < this.getItemCount(); i++) {
                Row r = this.children().get(i);
                if (!isInsideRow(i, mouseX, mouseY)) {
                    continue;
                }

                if (r instanceof HeaderRow hr) {
                    hr.mouseClicked(mouseX, mouseY, button);
                    return true;
                }
                if (r instanceof ListElementRow er) {
                    if (er.mouseClicked(mouseX, mouseY, button)) {
                        GoetyDelightConfigScreen.this.focusedBox =
                                er.box.isFocused() ? er.box : null;
                        return true;
                    }
                }
                if (r instanceof ValueRow vr) {
                    if (vr.mouseClicked(mouseX, mouseY, button)) {
                        if (vr.widget instanceof EditBox box && box.isFocused()) {
                            GoetyDelightConfigScreen.this.focusedBox = box;
                        } else {
                            GoetyDelightConfigScreen.this.focusedBox = null;
                        }
                        return true;
                    }
                }
            }

            clearEditFocus();
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (isInBottomMask(mouseX, mouseY)) {
                return false;
            }
            this.layoutRows();

            for (Row r : this.children()) {
                if (r instanceof ListElementRow er && er.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }

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
            for (Row r : this.children()) {
                if (r instanceof ListElementRow er && er.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
            }
            ValueRow row = rowAt(mouseX, mouseY);
            if (row != null && row.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
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
                    if (row instanceof ListElementRow er && er.box == focused) {
                        if (er.keyPressed(keyCode, scanCode, modifiers)) {
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
                    if (row instanceof ListElementRow er && er.box == focused) {
                        if (er.charTyped(codePoint, modifiers)) {
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
        // 分组标题行（显示名走 GROUP_NAME_MAP）
        // --------------------------------------------------------------------
        class HeaderRow extends Row {
            private final String groupName;
            private final String fullPath;
            private final int depth;
            private final Component title;

            HeaderRow(String groupName, String fullPath, int depth) {
                this.groupName = groupName;
                this.fullPath = fullPath;
                this.depth = depth;

                boolean collapsed = collapsedGroups.contains(fullPath);
                String display = displayName(fullPath, groupName);
                String indent = "  ".repeat(Math.max(0, depth));
                this.title = Component.literal("§e" + indent + (collapsed ? "▶ " : "▼ ") + display);
            }

            private static String displayName(String fullPath, String fallback) {
                String[] mapped = Config.GROUP_NAME_MAP.get(fullPath);
                if (mapped != null) {
                    String chosen = isChineseLanguage() && mapped.length >= 2 ? mapped[1] : mapped[0];
                    if (chosen != null && !chosen.isBlank()) {
                        return chosen;
                    }
                }
                return fallback;
            }

            private void toggle() {
                if (collapsedGroups.contains(fullPath)) {
                    collapsedGroups.remove(fullPath);
                } else {
                    collapsedGroups.add(fullPath);
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
        // 列表元素子行
        // --------------------------------------------------------------------
        class ListElementRow extends Row {
            final ValueRow parent;
            final int index;
            final EditBox box;
            final Button removeBtn;

            boolean suppressResponder = false;

            ListElementRow(ValueRow parent, int index, String initial) {
                this.parent = parent;
                this.index = index;

                this.box = new EditBox(GoetyDelightConfigScreen.this.font, 0, 0, 160, 18,
                        Component.literal("element " + index));
                this.box.setMaxLength(4096);
                this.box.setValue(initial);
                this.box.setCursorPosition(0);
                this.box.setHighlightPos(0);

                this.box.setResponder(text -> {
                    if (suppressResponder) {
                        return;
                    }
                    List<String> list = new ArrayList<>(parent.readStringList());
                    if (index < list.size()) {
                        list.set(index, text);
                        parent.stageList(list);
                    }
                });

                this.removeBtn = Button.builder(Component.literal("✖"), b -> {
                    List<String> list = new ArrayList<>(parent.readStringList());
                    if (index < list.size()) {
                        list.remove(index);
                        parent.stageList(list);
                        parent.rebuildElementRows();
                        ConfigList.this.rebuild();
                    }
                }).bounds(0, 0, 18, 18).build();
            }

            void layout(int left, int top, int width, int height) {
                int removeW = 18;
                int gap = 4;
                this.box.setX(left + 24);
                this.box.setY(top + 2);
                this.box.setWidth(width - 24 - removeW - gap - 8);
                this.removeBtn.setX(left + width - removeW - 5);
                this.removeBtn.setY(top + 1);
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                graphics.drawString(GoetyDelightConfigScreen.this.font,
                        Component.literal("§7" + (this.index + 1) + "."),
                        left + 8, top + 6, 0xFFFFFF);

                boolean masked = ConfigList.this.isInBottomMask(mouseX, mouseY);
                int rmx = masked ? -1 : mouseX;
                int rmy = masked ? -1 : mouseY;

                this.box.render(graphics, rmx, rmy, partialTick);
                this.removeBtn.render(graphics, rmx, rmy, partialTick);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.removeBtn.isMouseOver(mouseX, mouseY)) {
                    this.removeBtn.mouseClicked(mouseX, mouseY, button);
                    this.removeBtn.mouseReleased(mouseX, mouseY, button);
                    return true;
                }
                if (this.box.isMouseOver(mouseX, mouseY)) {
                    this.box.setFocused(true);
                    return this.box.mouseClicked(mouseX, mouseY, button);
                }
                return false;
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                return this.box.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                return this.box.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (this.box.isFocused()) {
                    if (keyCode == 256 /* ESC */) {
                        this.box.setFocused(false);
                        GoetyDelightConfigScreen.this.focusedBox = null;
                        return true;
                    }
                    return this.box.keyPressed(keyCode, scanCode, modifiers);
                }
                return false;
            }

            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                if (this.box.isFocused()) {
                    return this.box.charTyped(codePoint, modifiers);
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal("element " + this.index);
            }
        }

        // --------------------------------------------------------------------
        // 配置项行
        // --------------------------------------------------------------------
        class ValueRow extends Row {
            private final String key;
            private final ForgeConfigSpec.ValueSpec spec;
            private final ForgeConfigSpec.ConfigValue<?> value;
            private final int depth;
            private final Component label;
            private final AbstractWidget widget;
            private final Button resetButton;
            private final List<Component> tooltipLines;

            boolean expanded;

            final List<ListElementRow> elementRows = new ArrayList<>();

            private boolean suppressResponder = false;

            private boolean clickHandledInPress = false;

            ValueRow(String key, ForgeConfigSpec.ValueSpec spec, ForgeConfigSpec.ConfigValue<?> value, int depth) {
                this.key = key;
                this.spec = spec;
                this.value = value;
                this.depth = depth;
                this.label = buildLabel(key, spec);
                this.expanded = GoetyDelightConfigScreen.this.expandedListKeys.contains(key);
                this.widget = createWidget();
                this.resetButton = Button.builder(Component.literal("↺"), b -> resetToDefault())
                        .bounds(0, 0, 20, 20)
                        .build();
                this.tooltipLines = buildTooltip();
            }

            private static Component buildLabel(String key, ForgeConfigSpec.ValueSpec spec) {
                String[] mapped = Config.COMMENT_MAP.get(key);
                if (mapped != null) {
                    String chosen = isChineseLanguage() && mapped.length >= 2 ? mapped[1] : mapped[0];
                    if (chosen != null && !chosen.isBlank()) {
                        return Component.literal(chosen);
                    }
                }

                String comment = spec.getComment();
                if (comment != null && !comment.isBlank() && !comment.startsWith("Range:")) {
                    String[] lines = comment.split("\n");
                    String chosen = isChineseLanguage() && lines.length >= 2
                            ? lines[lines.length - 1]
                            : lines[0];
                    chosen = chosen.trim();
                    if (!chosen.isEmpty()) {
                        return Component.literal(chosen);
                    }
                }

                int idx = key.lastIndexOf('.');
                return Component.literal(idx >= 0 ? key.substring(idx + 1) : key);
            }

            private List<Component> buildTooltip() {
                List<Component> lines = new ArrayList<>();

                String[] mapped = Config.COMMENT_MAP.get(this.key);
                if (mapped != null) {
                    for (String line : mapped) {
                        if (line != null && !line.isBlank()) {
                            lines.add(Component.literal("§7" + line));
                        }
                    }
                } else {
                    String comment = spec.getComment();
                    if (comment != null && !comment.isBlank() && !comment.startsWith("Range:")) {
                        for (String line : comment.split("\n")) {
                            lines.add(Component.literal("§7" + line));
                        }
                    }
                }

                if (!isListValue()) {
                    lines.add(Component.literal("§7Default: §f" + formatDefault(spec.getDefault())));
                }

                if (spec.needsWorldRestart()) {
                    lines.add(Component.literal(""));
                    lines.add(Component.literal("§cRequires world restart"));
                }

                return lines;
            }

            private String formatDefault(Object def) {
                if (def == null) {
                    return "<null>";
                }
                if (def instanceof List<?> list) {
                    if (list.isEmpty()) {
                        return "[]";
                    }
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(String.valueOf(list.get(i)));
                    }
                    sb.append("]");
                    return sb.toString();
                }
                return String.valueOf(def);
            }

            Object readCurrentValue() {
                Object pending = GoetyDelightConfigScreen.this.getPendingChange(this.value);
                if (pending != null) {
                    return pending;
                }
                try {
                    return this.value.get();
                } catch (Exception e) {
                    return null;
                }
            }

            boolean isListValue() {
                return readCurrentValue() instanceof List<?>;
            }

            List<String> readStringList() {
                Object current = readCurrentValue();
                List<String> out = new ArrayList<>();
                if (current instanceof List<?> list) {
                    for (Object o : list) {
                        out.add(String.valueOf(o));
                    }
                }
                return out;
            }

            void stageList(List<String> list) {
                if (spec.test(list)) {
                    setValue(list);
                }
            }

            void rebuildElementRows() {
                elementRows.clear();
                Object current = readCurrentValue();
                if (!(current instanceof List<?> list)) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    elementRows.add(new ListElementRow(this, i, String.valueOf(list.get(i))));
                }
            }

            private void toggleExpanded() {
                boolean nowExpanded = !GoetyDelightConfigScreen.this.expandedListKeys.contains(this.key);
                if (nowExpanded) {
                    GoetyDelightConfigScreen.this.expandedListKeys.add(this.key);
                } else {
                    GoetyDelightConfigScreen.this.expandedListKeys.remove(this.key);
                }
                this.expanded = nowExpanded;
                GoetyDelight.LOGGER.info("[ConfigScreen] toggleExpanded {} -> {}", this.key, nowExpanded);
                refreshListButtonMessage();
                ConfigList.this.rebuild();
            }

            private void refreshListButtonMessage() {
                if (this.widget instanceof Button btn && !(this.widget instanceof CycleButton)) {
                    btn.setMessage(Component.literal(displayValue() + "  " + (expanded ? "▼" : "▶")));
                }
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
                    box.setValue(displayValue());
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

                if (isListValue()) {
                    return Button.builder(
                                    Component.literal(displayValue() + "  " + (expanded ? "▼" : "▶")),
                                    button -> toggleExpanded())
                            .width(130)
                            .build();
                }

                return Button.builder(Component.literal(displayValue()), button -> openEditor())
                        .width(130)
                        .build();
            }

            private String displayValue() {
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
                    GoetyDelightConfigScreen.this.stageChange(this.value, newValue);

                    if (widget instanceof Button btn && !(widget instanceof CycleButton)) {
                        if (isListValue()) {
                            refreshListButtonMessage();
                        } else {
                            suppressResponder = true;
                            btn.setMessage(Component.literal(displayValue()));
                            suppressResponder = false;
                        }
                    }
                } catch (Exception e) {
                    GoetyDelight.LOGGER.warn("[ConfigScreen] Failed to stage value for {}: {}", key, e.toString());
                }
            }

            @SuppressWarnings("unchecked")
            private void resetToDefault() {
                Object def = spec.getDefault();

                if (widget instanceof EditBox box) {
                    suppressResponder = true;
                    box.setValue(String.valueOf(def));
                    suppressResponder = false;
                    box.setTextColor(0xFFFFFF);
                } else if (widget instanceof CycleButton<?> cycle) {
                    ((CycleButton<Boolean>) cycle).setValue((Boolean) def);
                }

                setValue(def);

                if (isListValue()) {
                    rebuildElementRows();
                    ConfigList.this.rebuild();
                }
            }

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

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                int color = 0xFFFFFF;
                if (GoetyDelightConfigScreen.this.hasPendingChange(this.value)) {
                    color = 0xFFDD44;
                }
                int indent = this.depth * 10;
                graphics.drawString(GoetyDelightConfigScreen.this.font, this.label,
                        left + 5 + indent, top + 6, color);

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

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.clickHandledInPress = false;

                if (this.resetButton.isMouseOver(mouseX, mouseY)) {
                    this.resetButton.mouseClicked(mouseX, mouseY, button);
                    this.resetButton.mouseReleased(mouseX, mouseY, button);
                    this.clickHandledInPress = true;
                    return true;
                }

                if (this.widget.isMouseOver(mouseX, mouseY)) {
                    if (this.widget instanceof EditBox box) {
                        box.setFocused(true);
                        return box.mouseClicked(mouseX, mouseY, button);
                    }
                    if (this.widget instanceof Button btn) {
                        btn.mouseClicked(mouseX, mouseY, button);
                        btn.mouseReleased(mouseX, mouseY, button);
                        this.clickHandledInPress = true;
                        return true;
                    }
                    return this.widget.mouseClicked(mouseX, mouseY, button);
                }
                return false;
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (this.clickHandledInPress) {
                    this.clickHandledInPress = false;
                    return true;
                }
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
                    if (keyCode == 256 /* ESC */) {
                        box.setFocused(false);
                        GoetyDelightConfigScreen.this.focusedBox = null;
                        return true;
                    }
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