package ru.zaza.skipfurnacetime;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(SkipFurnaceTime.MOD_ID)
public class SkipFurnaceTime {


    public static final String MOD_ID = "skipfurnacetime";

    public static final Logger LOGGER = LogUtils.getLogger();

    public SkipFurnaceTime() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getLevel().getBlockEntity(event.getPos()) instanceof FurnaceBlockEntity furnace) {
                LOGGER.info("Reacted to left click on furnace");
                Inventory playerInv = event.getEntity().getInventory();

                LOGGER.info("Getting item info from furnace");
                ItemStack inputItem = furnace.getItem(0);
                int inputCount = furnace.getItem(0).getCount();
                int outputCount = furnace.getItem(2).getCount();
                LOGGER.info("Input item: {}\nInput amount: {}\nOutput amount: {}", inputItem.getDescriptionId(), inputCount, outputCount);

                RecipeManager recipeManager = furnace.getLevel().getRecipeManager();
                RecipeType<SmeltingRecipe> smeltingType = RecipeType.SMELTING;

                LOGGER.info("Getting all furnace recipes, starting to find the right recipe");
                recipeManager.getAllRecipesFor(smeltingType).forEach(recipe -> {
                    try {
                        ItemStack result = recipe.value().getResultItem(event.getLevel().registryAccess());
                        if (recipe.value().getIngredients().get(0).getItems()[0].getDescriptionId().contains(inputItem.getDescriptionId())) {
                            LOGGER.info("Found recipe, skip the waiting for the melting");

                            ItemStack resultItem = new ItemStack(result.getItem());
                            resultItem.setCount(inputCount + outputCount);
                            furnace.setItem(0, ItemStack.EMPTY);
                            furnace.setItem(2, ItemStack.EMPTY);
                            playerInv.add(resultItem);

                            LOGGER.info("Set melting result to player inventory");
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error processing recipe: {}", e.getMessage());
                    }
                });

            }
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Mod initialized");
        }
    }
}
