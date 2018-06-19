package me.seferan.quickcraft.action;

import me.seferan.quickcraft.util.ModuleInfo;
import net.eq2online.console.Log;
import net.eq2online.macros.scripting.api.APIVersion;
import net.eq2online.macros.scripting.api.IMacro;
import net.eq2online.macros.scripting.api.IMacroAction;
import net.eq2online.macros.scripting.api.IReturnValue;
import net.eq2online.macros.scripting.api.IScriptActionProvider;
import net.eq2online.macros.scripting.api.ReturnValue;
import net.eq2online.macros.scripting.parser.ScriptAction;
import net.eq2online.macros.scripting.parser.ScriptContext;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.inventory.Container;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

@APIVersion(ModuleInfo.API_VERSION)
public class ScriptActionQuickCraft extends ScriptAction {

	public ScriptActionQuickCraft()
	{
		super(ScriptContext.MAIN, "quickcraft");		
	}
	
	public void onInit()
	{
		this.context.getCore().registerScriptAction(this);
	}
	
	public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
		ReturnValue retVal = new ReturnValue(-1);
		if (params.length > 0) {
			String recipeName = provider.expand(macro, params[0], false);
			ResourceLocation rs = new ResourceLocation(recipeName);
			IRecipe recipe = CraftingManager.getRecipe(rs);
			if (recipe == null) {
				// handle case where invalid recipe is passed in.
				Log.info("Invalid Recipe");
				return retVal;
			}

			GuiContainer gc = this.slotHelper.getGuiContainer();
			if (gc != null && gc instanceof GuiCrafting) {
				GuiCrafting gccrafting = (GuiCrafting) gc;
				Container inventorySlots = gccrafting.inventorySlots;
				GuiRecipeBook gcrecipebook = gccrafting.func_194310_f();

			} else if (gc != null && gc instanceof GuiInventory) {
				if (!recipe.canFit(2, 2)) {
					// handle case where recipe requires 3x3 grid.
					Log.info("Crafting Grid too small for recipe!");
					return retVal;
				}
				GuiInventory gcinv = (GuiInventory) gc;
				GuiRecipeBook gcrecipebook = gcinv.func_194310_f();
			} else {
				// handle case where we don't either of those crafting windows open.
				Log.info("Invalid Crafting Window");
				return retVal;
			}

			// Perform actual call to "click" on the recipe in the recipe book.
			// borrowed from mouseClicked of GuiRecipeBook.class
			this.mc.playerController.func_194338_a(this.mc.player.openContainer.windowId, recipe, true, this.mc.player);
		}
		return retVal;
	}
}
