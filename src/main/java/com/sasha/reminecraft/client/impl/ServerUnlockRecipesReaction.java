package com.sasha.reminecraft.client.impl;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerUnlockRecipesPacket;
import com.sasha.reminecraft.client.IPacketReactor;
import com.sasha.reminecraft.client.ReClient;

public class ServerUnlockRecipesReaction implements IPacketReactor<ServerUnlockRecipesPacket> {
    @Override
    public boolean takeAction(ServerUnlockRecipesPacket pck) {
        ReClient.ReClientCache.INSTANCE.wasRecipeBookOpened = pck.getOpenCraftingBook();
        ReClient.ReClientCache.INSTANCE.wasFilteringRecipes = pck.getActivateFiltering();
        switch (pck.getAction()) {
            case ADD:
                for (Integer recipe : pck.getRecipes()) {
                    if (ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.add(recipe);
                }
                break;
            case REMOVE:
                for (Integer recipe : pck.getRecipes()) {
                    if (!ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.remove(recipe);
                }
                break;
            case INIT:
                for (Integer alreadyKnownRecipe : pck.getAlreadyKnownRecipes()) {
                    if (ReClient.ReClientCache.INSTANCE.recipeCache.contains(alreadyKnownRecipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.add(alreadyKnownRecipe);
                }
                for (Integer recipe : pck.getRecipes()) {
                    if (!ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.remove(recipe);
                }
                break;
            default:
                for (Integer alreadyKnownRecipe : pck.getAlreadyKnownRecipes()) {
                    if (ReClient.ReClientCache.INSTANCE.recipeCache.contains(alreadyKnownRecipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.add(alreadyKnownRecipe);
                }
                for (Integer recipe : pck.getRecipes()) {
                    if (!ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.remove(recipe);
                }
        }
        return true;
    }
}
