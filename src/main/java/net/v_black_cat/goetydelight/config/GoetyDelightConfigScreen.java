package net.v_black_cat.goetydelight.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A simple, self-contained configuration screen for Forge 1.20.1.
 *
 * <p>Forge 1.20.1 does not ship the NeoForge-style {@code ConfigurationScreen}, so this screen
 * reads the built {@link Config#SPEC} generically and renders one row per config option.
 * Booleans are toggled in place; numbers and string lists open a small text editor.</p>
 */
@OnlyIn(Dist.CLIENT)
public class GoetyDelightConfigScreen extends Screen {
    private static final String PREFIX = "goetydelight.configuration.";

    private final Screen parent;
    private ConfigList list;

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
        this.renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private class ConfigList extends ObjectSelectionList<ConfigList.Entry> {
        ConfigList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight) {
            super(mc, width, height, y0, y1, itemHeight);
            this.setRenderHeader(false, 0);
            this.rebuild();
        }

        void rebuild() {
            this.clearEntries();

            Map<String, Object> specs = Config.SPEC.getSpec().valueMap();
            Map<String, Object> values = Config.SPEC.getValues().valueMap();

            List<String> keys = new ArrayList<>(specs.keySet());
            Collections.sort(keys);

            for (String key : keys) {
                Object spec = specs.get(key);
                Object value = values.get(key);
                if (spec instanceof ForgeConfigSpec.ValueSpec valueSpec
                        && value instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                    this.addEntry(new Entry(key, valueSpec, configValue));
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

        class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String key;
            private final ForgeConfigSpec.ValueSpec spec;
            private final ForgeConfigSpec.ConfigValue<?> value;
            private final Component label;
            private final AbstractWidget widget;

            Entry(String key, ForgeConfigSpec.ValueSpec spec, ForgeConfigSpec.ConfigValue<?> value) {
                this.key = key;
                this.spec = spec;
                this.value = value;
                this.label = Component.translatable(PREFIX + key);
                this.widget = createWidget();

                String comment = spec.getComment();
                if (comment != null && !comment.isBlank()) {
                    this.widget.setTooltip(Tooltip.create(Component.literal(comment)));
                }
            }

            private AbstractWidget createWidget() {
                if (value instanceof ForgeConfigSpec.BooleanValue) {
                    return CycleButton.onOffBuilder((Boolean) value.get())
                            .displayOnlyValue()
                            .create(0, 0, 80, 20, label, (button, newValue) -> setValue(newValue));
                }
                return Button.builder(Component.literal(displayValue()), button -> openEditor())
                        .width(150)
                        .build();
            }

            private String displayValue() {
                Object current = value.get();
                if (current instanceof List<?> list) {
                    return "[" + list.size() + "]";
                }
                return String.valueOf(current);
            }

            private void openEditor() {
                EditValueScreen editor = null;
                Object current = value.get();
                if (current instanceof List<?> list) {
                    List<String> asStrings = new ArrayList<>();
                    for (Object element : list) {
                        asStrings.add(String.valueOf(element));
                    }
                    String initial = String.join(", ", asStrings);
                    editor = new EditValueScreen(GoetyDelightConfigScreen.this, label, initial, 4096, this::applyList);
                } else if (value instanceof ForgeConfigSpec.IntValue) {
                    editor = new EditValueScreen(GoetyDelightConfigScreen.this, label, displayValue(), 16, this::applyInteger);
                } else if (value instanceof ForgeConfigSpec.DoubleValue) {
                    editor = new EditValueScreen(GoetyDelightConfigScreen.this, label, displayValue(), 32, this::applyDouble);
                } else if (value instanceof ForgeConfigSpec.LongValue) {
                    editor = new EditValueScreen(GoetyDelightConfigScreen.this, label, displayValue(), 24, this::applyLong);
                } else {
                    editor = new EditValueScreen(GoetyDelightConfigScreen.this, label, displayValue(), 4096, this::applyString);
                }

                if (editor != null) {
                    GoetyDelightConfigScreen.this.getMinecraft().setScreen(editor);
                }
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
                if (Config.SPEC.isLoaded()) {
                    ((ForgeConfigSpec.ConfigValue<Object>) value).set(newValue);
                    Config.SPEC.save();
                }
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                graphics.drawString(GoetyDelightConfigScreen.this.font, this.label, left + 5, top + 6, 0xFFFFFF);

                this.widget.setX(left + width - this.widget.getWidth() - 5);
                this.widget.setY(top + 2);
                this.widget.render(graphics, mouseX, mouseY, partialTick);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return this.widget.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                return this.widget.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
    }

    private static class EditValueScreen extends Screen {
        private final Screen back;
        private final Component prompt;
        private final String initial;
        private final int maxLength;
        private final Predicate<String> apply;
        private EditBox editBox;

        EditValueScreen(Screen back, Component prompt, String initial, int maxLength, Predicate<String> apply) {
            super(prompt);
            this.back = back;
            this.prompt = prompt;
            this.initial = initial;
            this.maxLength = maxLength;
            this.apply = apply;
        }

        @Override
        protected void init() {
            int boxWidth = 220;
            this.editBox = new EditBox(this.font, this.width / 2 - boxWidth / 2, this.height / 2 - 30, boxWidth, 20, this.prompt);
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

            this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.back))
                    .bounds(this.width / 2 + 5, this.height / 2, 100, 20)
                    .build());
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(graphics);
            graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(this.back);
        }
    }
}
