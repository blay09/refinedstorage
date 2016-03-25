package refinedstorage.tile;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import refinedstorage.inventory.InventorySimple;
import refinedstorage.tile.settings.ICompareSetting;
import refinedstorage.util.InventoryUtils;

public class TileExporter extends TileMachine implements ICompareSetting {
    public static final String NBT_COMPARE = "Compare";

    private InventorySimple inventory = new InventorySimple("exporter", 9, this);

    private int compare = 0;

    @Override
    public int getEnergyUsage() {
        return 2;
    }

    @Override
    public void updateMachine() {
        TileEntity tile = worldObj.getTileEntity(pos.offset(getDirection()));

        if (tile instanceof IInventory) {
            IInventory connectedInventory = (IInventory) tile;

            if (ticks % 5 == 0) {
                for (int i = 0; i < inventory.getSizeInventory(); ++i) {
                    ItemStack slot = inventory.getStackInSlot(i);

                    if (slot != null) {
                        ItemStack toTake = slot.copy();

                        toTake.stackSize = 64;

                        ItemStack took = getController().take(toTake, compare);

                        if (took != null) {
                            ItemStack remaining = TileEntityHopper.putStackInInventoryAllSlots(connectedInventory, took, getDirection().getOpposite());

                            if (remaining != null) {
                                getController().push(remaining);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getCompare() {
        return compare;
    }

    @Override
    public void setCompare(int compare) {
        markDirty();

        this.compare = compare;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey(NBT_COMPARE)) {
            compare = nbt.getInteger(NBT_COMPARE);
        }

        InventoryUtils.restoreInventory(inventory, 0, nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger(NBT_COMPARE, compare);

        InventoryUtils.saveInventory(inventory, 0, nbt);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        compare = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        buf.writeInt(compare);
    }

    public IInventory getInventory() {
        return inventory;
    }
}
