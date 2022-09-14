package com.shanebeestudios.skbee.elements.text.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import com.shanebeestudios.skbee.api.NBT.NBTApi;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation") // Paper deprecations
@Name("Text Component - Hover Event")
@Description({"Create a new hover event. Can show text or an item to a player. 'showing %itemtype%' requires Minecraft 1.16.2+",
        "When showing an ItemType from a variable, use `...showing item {var}`, this is just a weird quirk of this."})
@Examples({"set {_t} to text component from \"Check out my cool tool!\"",
        "set hover event of {_t} to a new hover event showing player's tool",
        "send component {_t} to player"})
@Since("1.5.0")
public class ExprHoverEvent extends SimpleExpression<HoverEvent> {

    private static final boolean HAS_ITEM_META_NBT = Skript.methodExists(ItemMeta.class, "getAsString");

    static {
        Skript.registerExpression(ExprHoverEvent.class, HoverEvent.class, ExpressionType.COMBINED,
                "[a] [new] hover event showing %strings%",
                "[a] [new] hover event showing [item] %itemtype%");
    }

    private int pattern;
    private Expression<Object> object;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        if (matchedPattern == 1 && !HAS_ITEM_META_NBT && !NBTApi.isEnabled()) {
            Skript.error("NBTApi is currently unavailable, thus this expression with ItemType cannot be used right now.",
                    ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        this.pattern = matchedPattern;
        object = (Expression<Object>) exprs[0];
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Nullable
    @Override
    protected HoverEvent[] get(Event event) {
        if (object == null) return null;

        if (pattern == 0) {
            String[] string = ((String[]) this.object.getArray(event));
            List<Content> texts = new ArrayList<>();
            for (int i = 0; i < string.length; i++) {
                BaseComponent[] baseComponents = TextComponent.fromLegacyText(string[i] + (i < (string.length - 1) ? System.lineSeparator() : ""));
                texts.add(new Text(baseComponents));
            }
            return new HoverEvent[]{new HoverEvent(Action.SHOW_TEXT, texts)};
        } else if (pattern == 1) {
            ItemType itemType = (ItemType) object.getSingle(event);
            if (itemType == null) return null;

            ItemStack itemStack = itemType.getRandom();

            String id = itemStack.getType().getKey().toString();
            String nbt;
            if (HAS_ITEM_META_NBT) {
                nbt = itemStack.getItemMeta().getAsString();
            } else {
                nbt = new NBTItem(itemStack).toString();
            }
            Item item = new Item(id, itemStack.getAmount(), ItemTag.ofNbt(nbt));
            return new HoverEvent[]{new HoverEvent(Action.SHOW_ITEM, item)};
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends HoverEvent> getReturnType() {
        return HoverEvent.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean d) {
        return "hover event showing " + object.toString(e, d);
    }

}
