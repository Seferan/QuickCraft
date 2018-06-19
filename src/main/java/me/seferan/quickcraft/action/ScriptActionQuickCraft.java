package me.seferan.quickcraft.action;

import me.seferan.quickcraft.util.ModuleInfo;
import net.eq2online.macros.scripting.api.APIVersion;
import net.eq2online.macros.scripting.api.IMacro;
import net.eq2online.macros.scripting.api.IMacroAction;
import net.eq2online.macros.scripting.api.IReturnValue;
import net.eq2online.macros.scripting.api.IScriptActionProvider;
import net.eq2online.macros.scripting.api.ReturnValue;
import net.eq2online.macros.scripting.parser.ScriptAction;
import net.eq2online.macros.scripting.parser.ScriptContext;
import net.eq2online.util.Game;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

@APIVersion(ModuleInfo.API_VERSION)
public class ScriptActionQuickCraft extends ScriptAction {

	public ScriptActionQuickCraft() {
		super(ScriptContext.MAIN, "quickcraft");
	}

	public void onInit() {
		this.context.getCore().registerScriptAction(this);
	}

	public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams,
			String[] params) {
		ReturnValue retVal = new ReturnValue(-1);
		String recipeName = "";
		Boolean sendClick = false;
		if (params.length == 0) {
			Game.addChatMessage("Usage: QUICKCRAFT(recipeName, [sendSlotClick])");
			return retVal;
		}
		if (params.length > 0) {
			recipeName = provider.expand(macro, params[0], false);
		}
		if (params.length > 1) {
			String sendClickStr = provider.expand(macro, params[1], false).trim();
			sendClick = ("1".equals(sendClickStr)) || ("true".equalsIgnoreCase(sendClickStr));
		}

		ResourceLocation rs = new ResourceLocation(recipeName);
		IRecipe recipe = CraftingManager.getRecipe(rs);
		if (recipe == null) {
			// handle case where invalid recipe is passed in.
			Game.addChatMessage("Unknown Recipe: " + recipeName);
			return retVal;
		}

		GuiContainer gc = this.slotHelper.getGuiContainer();
		GuiRecipeBook gcrecipebook = null;
		if (gc != null && gc instanceof GuiCrafting) {
			GuiCrafting gccrafting = (GuiCrafting) gc;
			gcrecipebook = gccrafting.func_194310_f();
		} else if (gc != null && gc instanceof GuiInventory) {
			if (!recipe.canFit(2, 2)) {
				// handle case where recipe requires 3x3 grid.
				Game.addChatMessage("Recipe requires crafting table. (3x3)");
				return retVal;
			}
			GuiInventory gcinv = (GuiInventory) gc;
			gcrecipebook = gcinv.func_194310_f();
		} else {
			Game.addChatMessage("Open crafting screen before running QuickCraft");
			// handle case where we don't either of those crafting windows open.
			return retVal;
		}

		RecipeItemHelper mystackedContents = new RecipeItemHelper();
		// the following false parameter indicates whether to include "offhand". We do
		// not want this.
		mc.player.inventory.fillStackedContents(mystackedContents, false);
		Boolean canCraftItem = mystackedContents.canCraft(recipe, null);
		if (!canCraftItem) {
			// handle case the RecipeItemHelper says we can't craft.
			// this is likely due to not having the required items.
			Game.addChatMessage("Unable to craft item: " + recipeName);
			return retVal;
		}

		// Perform actual call to "click" on the recipe in the recipe book.
		// borrowed from mouseClicked of GuiRecipeBook.class
		this.mc.playerController.func_194338_a(this.mc.player.openContainer.windowId, recipe, true, this.mc.player);
		if (sendClick)
			this.slotHelper.containerSlotClick(0, 0, true);

		return retVal;
	}
}