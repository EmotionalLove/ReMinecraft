package com.sasha.reminecraft.reaction.client;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerUnlockRecipesPacket;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.reaction.IPacketReactor;

public class ServerUnlockRecipesReaction implements IPacketReactor<ServerUnlockRecipesPacket> {
    @Override
    public boolean takeAction(ServerUnlockRecipesPacket packet) {
        ReClient.ReClientCache.INSTANCE.wasCraftingRecipeBookOpened = packet.getOpenCraftingBook();
        ReClient.ReClientCache.INSTANCE.wasSmeltingRecipeBookOpened = packet.getOpenSmeltingBook();
        ReClient.ReClientCache.INSTANCE.wasFilteringCraftingRecipes = packet.getActivateCraftingFiltering();
        ReClient.ReClientCache.INSTANCE.wasFilteringSmeltingRecipes = packet.getActivateCraftingFiltering();
        switch (packet.getAction()) {
            case ADD:
                for (String recipe : packet.getRecipes()) {
                    if (ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.add(recipe);
                }
                break;
            case REMOVE:
                for (String recipe : packet.getRecipes()) {
                    if (!ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.remove(recipe);
                }
                break;
            case INIT:
                for (String alreadyKnownRecipe : packet.getAlreadyKnownRecipes()) {
                    if (ReClient.ReClientCache.INSTANCE.recipeCache.contains(alreadyKnownRecipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.add(alreadyKnownRecipe);
                }
                for (String recipe : packet.getRecipes()) {
                    if (!ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.remove(recipe);
                }
                break;
            default:
                for (String alreadyKnownRecipe : packet.getAlreadyKnownRecipes()) {
                    if (ReClient.ReClientCache.INSTANCE.recipeCache.contains(alreadyKnownRecipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.add(alreadyKnownRecipe);
                }
                for (String recipe : packet.getRecipes()) {
                    if (!ReClient.ReClientCache.INSTANCE.recipeCache.contains(recipe)) continue;
                    ReClient.ReClientCache.INSTANCE.recipeCache.remove(recipe);
                }
        }
        return true;
    }
}
