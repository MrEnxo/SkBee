package com.shanebeestudios.skbee.elements.nbt.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import com.shanebeestudios.skbee.api.NBT.NBTApi;
import com.shanebeestudios.skbee.api.NBT.NBTCustomBlock;
import com.shanebeestudios.skbee.api.NBT.NBTCustomEntity;
import com.shanebeestudios.skbee.api.NBT.NBTCustomSlot;
import com.shanebeestudios.skbee.api.NBT.NBTCustomTileEntity;
import com.shanebeestudios.skbee.api.NBT.NBTCustomItemType;
import de.tr7zw.changeme.nbtapi.NBTChunk;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Name("NBT - Compound Of")
@Description({"Get the NBT compound of a block/entity/item/file/chunk. Optionally you can return a copy of the compound. This way you can modify it without",
        "actually modifying the original NBT compound, for example when grabbing the compound from an entity, modifying it and applying to",
        "other entities.",
        "\n'full nbt compound' from an item will return a copy of the FULL NBT of an item (this includes id, count and 'tag' compound).",
        "Modifying this will have no effect on the original item. This is useful for serializing items.",
        "\n'nbt [item] compound' from an item will be the original. This will return the 'tag' portion of an items full NBT.",
        "Modifying this will modify the original item. The '[item]' portion is deprecated and will be removed in a future version.",
        "\nNBT from a file will need to be saved manually using",
        "the 'NBT - Save File effect'. If the file does not yet exist, a new file will be created."})
@Examples({"set {_n} to nbt compound of player's tool",
        "set {_nbt} to nbt compound of target entity",
        "set {_n} to nbt compound of \"{id:\"\"minecraft:diamond_sword\"\",tag:{Damage:0,Enchantments:[{id:\"\"minecraft:sharpness\"\",lvl:3s}]},Count:1b}\"",
        "set {_nbt} to nbt compound of file \"world/playerdata/some-uuid.dat\"",
        "set {_n} to nbt compound of chunk at player"})
@Since("1.6.0")
public class ExprNbtCompound extends PropertyExpression<Object, NBTCompound> {

    static {
        // Will remove '[item]' in the future (oct 24/2022)
        Skript.registerExpression(ExprNbtCompound.class, NBTCompound.class, ExpressionType.PROPERTY,
                "[:full] nbt [:item] [compound] [:copy] (of|from) %blocks/entities/itemtypes/itemstacks/slots/strings/chunks%",
                "nbt [compound] [:copy] (of|from) file[s] %strings%");
    }

    private boolean isFullItem;
    private boolean isCopy;
    private boolean isFile;

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        setExpr(exprs[0]);
        isFullItem = parseResult.hasTag("full");
        isCopy = parseResult.hasTag("copy");
        isFile = matchedPattern == 1;
        if (parseResult.hasTag("item")) {
            Skript.warning("'nbt item compound' has been deprecated ... please just use 'nbt compound'");
        }
        return true;
    }

    @Override
    protected NBTCompound @NotNull [] get(@NotNull Event e, Object @NotNull [] source) {
        return get(source, object -> {
            NBTCompound compound = null;
            if (object instanceof Block block) {
                if (block.getState() instanceof TileState tileState) {
                    compound = new NBTCustomTileEntity(tileState);
                } else if (NBTApi.supportsBlockNBT()) {
                    compound = new NBTCustomBlock(block).getData();
                }
            } else if (object instanceof Entity entity) {
                compound = new NBTCustomEntity(entity);
            } else if (object instanceof ItemType itemType) {
                if (isFullItem) {
                    compound = NBTItem.convertItemtoNBT(itemType.getRandom());
                } else {
                    if (itemType.getMaterial() == Material.AIR) return null;
                    compound = new NBTCustomItemType(itemType);
                }
            } else if (object instanceof ItemStack itemStack) {
                if (isFullItem) {
                    return NBTItem.convertItemtoNBT(itemStack);
                } else {
                    if (itemStack.getType() == Material.AIR) return null;
                    compound = new NBTItem(itemStack, true);
                }
            } else if (object instanceof Slot slot) {
                ItemStack stack = slot.getItem();
                if (stack == null) return null;

                if (isFullItem) {
                    compound = NBTItem.convertItemtoNBT(stack);
                } else {
                    if (stack.getType() == Material.AIR) return null;
                    compound = new NBTCustomSlot(slot);
                }
            } else if (object instanceof String nbtString) {
                if (isFile) {
                    compound = NBTApi.getNBTFile(nbtString);
                } else {
                    compound = NBTApi.validateNBT(nbtString);
                }
            } else if (object instanceof Chunk chunk) {
                compound = new NBTChunk(chunk).getPersistentDataContainer();
            }
            if (compound != null) {
                if (isCopy) {
                    return new NBTContainer(compound.toString());
                } else {
                    return compound;
                }
            }
            return null;
        });
    }

    @Override
    public @NotNull Class<? extends NBTCompound> getReturnType() {
        return NBTCompound.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean d) {
        String full = this.isFullItem ? "full " : "";
        String copy = this.isCopy ? "copy " : "";
        return full + "nbt compound " + copy + "from " + getExpr().toString(e, d);
    }

}
