package com.sasha.reminecraft.util.sound;

/**
 * Created by Sasha at 6:23 PM on 12/22/2018
 */
public interface ISound {

    ISound getSoundById(int id);

    ISound getSoundByMinecraftTranslationKey(String key);

    String getMinecraftTranslationKeyFromSound(ISound sound);

    int getIdFromSound(ISound sound);

}
