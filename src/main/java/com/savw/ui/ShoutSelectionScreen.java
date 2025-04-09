package com.savw.ui;

import com.savw.shout.AbstractShout;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.savw.shout.Shouts.ALL_SHOUTS;

public class ShoutSelectionScreen extends BaseOwoScreen<GridLayout> {

    @Override
    protected @NotNull OwoUIAdapter<GridLayout> createAdapter() {
        return OwoUIAdapter.create(this, ((sizing, sizing2) ->
                Containers.grid(sizing, sizing2, 3, 5)));
    }

    private final int pageNumber;
    private final int startIndex;
    private final int endIndex;

    public ShoutSelectionScreen(int pageNumber) {
        super();
        this.pageNumber = pageNumber;
        // Each page contains 9 shouts,
        // so we calculate the start and end index
        // based on the page number.
        this.startIndex = (pageNumber - 1) * 9;
        this.endIndex = Math.min(pageNumber * 9, ALL_SHOUTS.size());
    }

    public int page() {
        return this.pageNumber;
    }

    public int startIndex() {
        return this.startIndex;
    }

    public int endIndex() {
        return this.endIndex;
    }

    @Override
    protected void build(GridLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .padding(Insets.top(32).withLeft(16));

        rootComponent.child(Components.button(Component.literal("x").withStyle(ChatFormatting.BOLD), xButton ->
                Minecraft.getInstance().setScreen(null)).tooltip(Component.literal("Close")), 0, 4);

        if (endIndex() < ALL_SHOUTS.size()) {
            rootComponent.child(CustomUiComponents.arrowButton(this, false), 1, 4);
        }

        if (page() > 1) {
            rootComponent.child(CustomUiComponents.arrowButton(this, true), 1, 0);
        }

        for (int i = startIndex(); i < endIndex(); i++) {
            AbstractShout shout = ALL_SHOUTS.get(i);

            rootComponent.child(CustomUiComponents.clickableTexture(shout),
                    (i - startIndex()) % 3, ((i - startIndex()) / 3) + 1);
        }
    }
}
