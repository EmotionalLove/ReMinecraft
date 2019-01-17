package com.sasha.reminecraft.util.entity;

import com.github.steveice10.mc.protocol.data.game.entity.EquipmentSlot;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class EntityEquipment extends EntityRotation {
    public ArrayList<PotionEffect> potionEffects = new ArrayList<>();
    public HashMap<EquipmentSlot, ItemStack> equipment = new HashMap<>();

    {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, null);
        }
    }


}
