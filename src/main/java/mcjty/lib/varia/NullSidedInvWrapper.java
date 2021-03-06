package mcjty.lib.varia;

import mcjty.lib.compat.CompatItemHandlerModifiable;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Works on a ISidedInventory but just passes null to the side so only
 * one instance is needed for side == null as well as the six sides
 */
public class NullSidedInvWrapper implements CompatItemHandlerModifiable {
    private final ISidedInventory inv;

    // This version allows a 'null' facing
    public NullSidedInvWrapper(ISidedInventory inv) {
        this.inv = inv;
    }

    public static int getSlot(ISidedInventory inv, int slot) {
        int[] slots = inv.getSlotsForFace(null);
        if (slot < slots.length) {
            return slots[slot];
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NullSidedInvWrapper that = (NullSidedInvWrapper) o;

        if (inv != null ? !inv.equals(that.inv) : that.inv != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return inv != null ? inv.hashCode() : 0;
    }

    @Override
    public int getSlots() {
        return inv.getSlotsForFace(null).length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int i = getSlot(inv, slot);
        return i == -1 ? ItemStackTools.getEmptyStack() : inv.getStackInSlot(i);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

        if (ItemStackTools.isEmpty(stack)) {
            return ItemStackTools.getEmptyStack();
        }

        slot = getSlot(inv, slot);

        if (slot == -1) {
            return stack;
        }

        if (!inv.isItemValidForSlot(slot, stack) || !inv.canInsertItem(slot, stack, null)) {
            return stack;
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot);

        int m;
        if (ItemStackTools.isValid(stackInSlot)) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
                return stack;
            }

            m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit()) - ItemStackTools.getStackSize(stackInSlot);

            if (ItemStackTools.getStackSize(stack) <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    ItemStackTools.incStackSize(copy, ItemStackTools.getStackSize(stackInSlot));
                    inv.setInventorySlotContents(slot, copy);
                }

                return ItemStackTools.getEmptyStack();
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.splitStack(m);
                    ItemStackTools.incStackSize(copy, ItemStackTools.getStackSize(stackInSlot));
                    inv.setInventorySlotContents(slot, copy);
                    return stack;
                } else {
                    ItemStackTools.incStackSize(stack, -m);
                    return stack;
                }
            }
        } else {
            m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
            if (m < ItemStackTools.getStackSize(stack)) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    inv.setInventorySlotContents(slot, stack.splitStack(m));
                    return stack;
                } else {
                    ItemStackTools.incStackSize(stack, -m);
                    return stack;
                }
            } else {
                if (!simulate) {
                    inv.setInventorySlotContents(slot, stack);
                }
                return ItemStackTools.getEmptyStack();
            }
        }

    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        int i = getSlot(inv, slot);
        inv.setInventorySlotContents(i, stack);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStackTools.getEmptyStack();
        }

        int slot1 = getSlot(inv, slot);

        if (slot1 == -1) {
            return ItemStackTools.getEmptyStack();
        }

        ItemStack stackInSlot = inv.getStackInSlot(slot1);

        if (ItemStackTools.isEmpty(stackInSlot)) {
            return ItemStackTools.getEmptyStack();
        }

        if (!inv.canExtractItem(slot1, stackInSlot, null)) {
            return ItemStackTools.getEmptyStack();
        }

        if (simulate) {
            if (ItemStackTools.getStackSize(stackInSlot) < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                ItemStackTools.setStackSize(copy, amount);
                return copy;
            }
        } else {
            int m = Math.min(ItemStackTools.getStackSize(stackInSlot), amount);
            return inv.decrStackSize(slot1, m);
        }
    }

    @Override
    public int getSlotMaxLimit() {
        return 64;
    }
}