package tk.shanebee.bee.api;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.slot.Slot;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import tk.shanebee.bee.SkBee;
import tk.shanebee.bee.api.reflection.SkReflection;
import tk.shanebee.bee.api.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NBTApi {

    public boolean validateNBT(String nbtString) {
        if (nbtString == null) return false;
        try {
            new NBTContainer(nbtString);
        } catch (Exception ex) {
            sendError(nbtString, ex);
            return false;
        }
        return true;
    }

    private void sendError(String error, Exception exception) {
        Util.skriptError("&cInvalid NBT: &b" + error + "&c");
        if (SkBee.getPlugin().getPluginConfig().SETTINGS_DEBUG && exception != null) {
            exception.printStackTrace();
        }
    }

    // This is just to force load the api!
    public void forceLoadNBT() {
        Util.log("&aLoading NBTApi...");
        NBTItem loadingItem = new NBTItem(new ItemStack(Material.STONE));
        loadingItem.mergeCompound(new NBTContainer("{}"));
        Util.log("&aSuccessfully loaded NBTApi!");
    }

    // INVENTORY SLOT
    public void setNBT(Slot slot, String value) {
        if (!validateNBT(value)) return;
        ItemStack item = slot.getItem();
        if (item != null) {
            slot.setItem(setNBT(item, value));
        }
    }

    public void addNBT(Slot slot, String value) {
        if (!validateNBT(value)) return;
        ItemStack item = slot.getItem();
        if (item != null) {
            slot.setItem(addNBT(item, value));
        }
    }

    public String getNBT(Slot slot) {
        ItemStack item = slot.getItem();
        if (item != null) {
            return getNBT(item);
        }
        return null;
    }

    public String getFullNBT(Slot slot) {
        ItemStack item = slot.getItem();
        if (item != null)
            return getFullNBT(item);
        return null;
    }

    // ITEM NBT
    public void setNBT(ItemType itemType, String value) {
        if (!validateNBT(value)) return;
        ItemStack itemStack = new ItemStack(itemType.getMaterial());

        NBTItem item = new NBTItem(itemStack);
        item.mergeCompound(new NBTContainer(value));
        SkReflection.setMeta(itemType, item.getItem().getItemMeta());
    }

    public ItemStack setNBT(ItemStack itemStack, String value) {
        if (validateNBT(value)) {
            ItemStack stack = new ItemStack(itemStack.getType());
            NBTItem item = new NBTItem(stack);
            item.mergeCompound(new NBTContainer(value));
            itemStack.setItemMeta(item.getItem().getItemMeta());
        }
        return itemStack;
    }

    public void addNBT(ItemType itemType, String value) {
        if (!validateNBT(value)) return;
        ItemStack stack = itemType.getRandom();
        if (stack == null) return;

        NBTItem item = new NBTItem(stack);
        item.mergeCompound(new NBTContainer(value));
        SkReflection.setMeta(itemType, item.getItem().getItemMeta());
    }

    public ItemStack addNBT(ItemStack itemStack, String value) {
        if (validateNBT(value)) {
            NBTItem item = new NBTItem(itemStack);
            item.mergeCompound(new NBTContainer(value));
            itemStack.setItemMeta(item.getItem().getItemMeta());
        }
        return itemStack;
    }

    public String getNBT(ItemType itemType) {
        ItemStack itemStack = itemType.getRandom();
        return getNBT(itemStack);
    }

    public String getNBT(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        NBTItem item = new NBTItem(itemStack);
        return item.toString();
    }

    public String getFullNBT(ItemType itemType) {
        ItemStack itemStack = itemType.getRandom();
        return getFullNBT(itemStack);
    }

    public String getFullNBT(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        return NBTItem.convertItemtoNBT(itemStack).toString();
    }

    public ItemType getItemTypeFromNBT(String nbt) {
        if (!validateNBT(nbt)) return null;
        return new ItemType(getItemStackFromNBT(nbt));
    }

    public ItemStack getItemStackFromNBT(String nbt) {
        if (!validateNBT(nbt)) return null;
        NBTContainer container = new NBTContainer(nbt);
        return NBTItem.convertNBTtoItem(container);
    }

    // ENTITY NBT
    public void setNBT(Entity entity, String newValue) {
        addNBT(entity, newValue);
    }

    public void addNBT(Entity entity, String newValue) {
        if (entity == null || entity.isDead()) return;
        if (!validateNBT(newValue)) return;
        NBTEntity nbtEntity = new NBTEntity(entity);
        nbtEntity.mergeCompound(new NBTContainer(newValue));
    }

    public String getNBT(Entity entity) {
        if (entity == null || entity.isDead()) return null;
        NBTEntity nbtEntity = new NBTEntity(entity);
        return nbtEntity.toString();
    }

    // TILE ENTITY NBT
    public void setNBT(Block block, String newValue) {
        if (!validateNBT(newValue)) return;
        addNBT(block, newValue);
    }

    public void addNBT(Block block, String newValue) {
        if (!validateNBT(newValue)) return;
        NBTTileEntity tile = new NBTTileEntity(block.getState());
        try {
            tile.mergeCompound(new NBTContainer(newValue));
        } catch (NbtApiException ignore) {}
    }

    public String getNBT(Block block) {
        NBTTileEntity tile = new NBTTileEntity(block.getState());
        try {
            return tile.getCompound().toString();
        } catch (NbtApiException ignore) {
            return null;
        }
    }

    // FILE NBT
    public String getNBT(String fileName) {
        File file = getFile(fileName);
        if (file == null) return null;
        NBTFile fileNBT = null;
        try {
            fileNBT = new NBTFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileNBT == null) return null;
        return fileNBT.toString();
    }

    public void addNBT(String file, String value) {
        if (!validateNBT(value)) return;
        File file1 = getFile(file);
        if (file1 == null) return;

        try {
            NBTFile nbtFile = new NBTFile(file1);
            nbtFile.mergeCompound(new NBTContainer(value));
            nbtFile.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNBT(String file, String value) {
        addNBT(file, value);
    }

    // Internal use to get file NBT
    private File getFile(String fileName) {
        fileName = !fileName.endsWith(".dat") ? fileName + ".dat" : fileName;
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        } else {
            return file;
        }
    }

    // TAGS
    public Object getTag(String tag, String nbt) {
        if (nbt == null) return null;
        NBTCompound compound = new NBTContainer(nbt);
        NBTType type = compound.getType(tag);
        switch (type) {
            case NBTTagString:
                return compound.getString(tag);
            case NBTTagInt:
                return compound.getInteger(tag);
            case NBTTagIntArray:
                UUID uuid = compound.getUUID(tag);
                if (uuid != null) {
                    return uuid;
                } else {
                    return Collections.singletonList(compound.getIntArray(tag));
                }
            case NBTTagFloat:
                return compound.getFloat(tag);
            case NBTTagShort:
                return compound.getShort(tag);
            case NBTTagDouble:
                return compound.getDouble(tag);
            case NBTTagEnd:
                //return compound.toString(); // let's leave this here just in case
                return null;
            case NBTTagLong:
                return compound.getLong(tag);
            case NBTTagByte:
                return compound.getByte(tag);
            case NBTTagByteArray:
                return compound.getByteArray(tag);
            case NBTTagCompound:
                return compound.getCompound(tag).toString();
            case NBTTagList:
                List<Object> list = new ArrayList<>();
                for (NBTCompound comp : compound.getCompoundList(tag)) {
                    list.add(comp.toString());
                }
                list.addAll(compound.getDoubleList(tag));
                list.addAll(compound.getFloatList(tag));
                list.addAll(compound.getIntegerList(tag));
                list.addAll(compound.getStringList(tag));
                list.addAll(compound.getLongList(tag));
                return list;
            default:
                return "null -> type: " + type.toString();
        }
    }

}
