package com.sasha.reminecraft.util.entity;

import com.github.steveice10.mc.protocol.data.game.entity.attribute.Attribute;

import java.util.ArrayList;
import java.util.List;

public class EntityRotation extends Entity {
    public float yaw, pitch;
    public double motionX;
    public double motionY;
    public double motionZ;
    public int leashedID;
    public boolean isLeashed;
    public float headYaw;
    public List<Attribute> properties = new ArrayList<>();
}
