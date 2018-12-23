package com.sasha.reminecraft.util.sound;

import com.sasha.reminecraft.util.sound.impl.AmbientCaveSound;
import com.sasha.reminecraft.util.sound.impl.BlockAnvilBreakSound;
import com.sasha.reminecraft.util.sound.impl.BlockAnvilDestroySound;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sasha at 6:23 PM on 12/22/2018
 */
public abstract class AbstractSound implements ISound {

    public static List<ISound> soundRegistry = new ArrayList<>();
    static {
        soundRegistry.add(new AmbientCaveSound(0, "ambient.cave"));
        soundRegistry.add(new BlockAnvilBreakSound(1, "block.anvil.break"));
        soundRegistry.add(new BlockAnvilDestroySound(2, "block.anvil.destroy"));
        soundRegistry.add(new BlockAnvilDestroySound(3, "block.anvil.fall"));
    }

    private int id;
    private String translationKey;

    public AbstractSound(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    @Override
    public ISound getSoundById(int id) {
        return null;
    }

    @Override
    public ISound getSoundByMinecraftTranslationKey(String key) {
        return null;
    }

    @Override
    public String getMinecraftTranslationKeyFromSound(ISound sound) {
        return null;
    }

    @Override
    public int getIdFromSound(ISound sound) {
        return 0;
    }
}
