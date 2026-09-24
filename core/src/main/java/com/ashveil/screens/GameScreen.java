package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.GameApp;
import com.ashveil.farming.Crop;
import com.ashveil.guidance.GameEvent;
import com.ashveil.guidance.GuidanceSystem;
import com.ashveil.guidance.GuideStep;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.rendering.HudRenderer;
import com.ashveil.rendering.WorldRenderer;
import com.ashveil.input.PlayerInput;
import com.ashveil.input.KeyBindings;
import com.ashveil.save.SaveService;
import com.ashveil.targeting.TargetMode;
import com.ashveil.targeting.TileTargetingSystem;
import com.ashveil.ui.*;
import com.ashveil.ui.chest.ChestUI;
import com.ashveil.ui.scroll.ScrollUi;
import com.ashveil.ui.worldmap.WorldMapUi;
import com.ashveil.world.*;
import com.ashveil.world.area.AreaID;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import jdk.jshell.spi.ExecutionControl;

import javax.swing.plaf.synth.SynthRootPaneUI;
import java.lang.reflect.MalformedParameterizedTypeException;

public class GameScreen implements Screen {

    private GameApp game;
    private WorldRenderer worldRenderer;
    private HudRenderer hudRenderer;
    private GameMenuUi gameMenuUi;
    private World world;
    private CameraController cameraController;
    private GameOverlay activeOverlay;
    private KeyBindings keyBindings;
    private DeathTransitionState deathTransitionState;
    private float deathFadeAlpha;
    private ShapeRenderer fadeRenderer;
    private TileTargetingSystem tileTargetingSystem;
    private Stage overlayStage;
    private ChestUI chestUi;
    private PauseMenuUi pauseMenuUi;
    private SaveService saveService;
    private final int saveSlot;
    private Skin uiSkin;

    private Stage guidanceStage;
    private GuidanceSystem guidanceSystem;
    private GuidanceUi guidanceUi;
    private boolean guidanceMessagePending;
    private float guidanceMessageDelay;
    private boolean contextualGuidanceVisible;
    private WorldMapUi worldMapUi;

    private ScrollUi scrollUi;
    private ItemType openedScroll;
    private boolean scrollOpenedThisFrame;

    public GameScreen(GameApp game, int saveSlot){
        this(game, saveSlot, new World());
    }

    public GameScreen(GameApp game, int saveSlot, World world){
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");
        if (world == null) throw new IllegalArgumentException("World cannot be null.");
        this.game = game;
        this.saveSlot = saveSlot;
        this.world = world;
        saveService = game.getSaveService();
        worldRenderer = new WorldRenderer(world.getTileMap());
        cameraController = new CameraController();
        hudRenderer = new HudRenderer();
        uiSkin = game.getUiSkin();
        worldMapUi = new WorldMapUi(uiSkin, world);
        gameMenuUi = new GameMenuUi(uiSkin, world.getAvailableRecipes(), world, world.getPlayer().getInventory(), this::handleSuccessfulCraft, this::handleCraftingOpened);
        activeOverlay = GameOverlay.NONE;
        keyBindings = new KeyBindings();
        deathTransitionState = DeathTransitionState.NONE;
        deathFadeAlpha = 0;
        fadeRenderer = new ShapeRenderer();
        tileTargetingSystem = new TileTargetingSystem(cameraController, world.getTileMap());
        overlayStage = new Stage(new ScreenViewport());
        guidanceStage = new Stage(new ScreenViewport());
        guidanceSystem = world.getGuidanceSystem();
        guidanceUi = new GuidanceUi(uiSkin);
        guidanceMessagePending = false;
        guidanceMessageDelay = 0f;

        Table guidanceRoot = new Table();
        guidanceRoot.setFillParent(true);
        guidanceRoot.top().left();

        guidanceRoot.add(guidanceUi).width(1000f).height(300f).padTop(22f).padLeft(22f);

        guidanceStage.addActor(guidanceRoot);

        chestUi = null;
        //prosledjujemo closepause kao runnable callback. ne sluzi za novu nit, vec samo prosledjuje akciju
        //koja pausemenuui moze kasnije pozvati
        pauseMenuUi = new PauseMenuUi(uiSkin, this::closePause, this::openSettingsFromPause, this::exitToMainMenu);

        scrollUi = new ScrollUi(uiSkin);
        openedScroll = null;
        scrollOpenedThisFrame = false;

        if (guidanceSystem.shouldShowCurrentMessage()) showCurrentGuidanceMessage();
        else if (guidanceSystem.getActiveContextualStep() != null) showActiveContextualMessage();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        handleCancelBackInput();
        handleGuidanceInput();
        updateGuidanceMessageDelay(delta);
        updateContextualGuidance();
        tileTargetingSystem.update();

        if (Gdx.input.isKeyJustPressed(keyBindings.getToggleOverlayKey())) {
            toggleMenu();
        }

        PlayerInput playerInput;

        if (activeOverlay == GameOverlay.NONE) playerInput = readPlayerInput();
        else playerInput = getNeutralPlayerInput();

        if (deathTransitionState != DeathTransitionState.NONE) updateDeathTransition(delta);
        else if (activeOverlay == GameOverlay.PAUSE) overlayStage.act(delta);
        else {
            boolean waitingForMovement = guidanceSystem.getCurrentStep() == GuideStep.MOVEMENT;
            boolean waitingForWood = guidanceSystem.getCurrentStep() == GuideStep.COLLECT_WOOD;

            float playerXBeforeUpdate = world.getPlayer().getX();
            float playerYBeforeUpdate = world.getPlayer().getY();
            int woodBeforeUpdate = waitingForWood ? world.getPlayer().getInventory().getQuantity(ItemType.WOOD) : 0;
            int healthBeforeUpdate = world.getPlayer().getCurrentHp();

            world.update(delta, playerInput);

            if (activeOverlay == GameOverlay.WORLD_MAP){
                worldMapUi.act(delta);
                handleWorldMapRequests();
            }

            if (world.isWorldMapOpenRequested()){
                world.clearWorldMapOpenRequest();
                openWorldMap();
            }

            ItemType requestedScroll = world.getScrollReadRequested();
            if (requestedScroll != null){
                world.clearScrollReadRequests();
                openScroll(requestedScroll);
            }

            if (waitingForMovement){boolean playerMoved = Math.abs(world.getPlayer().getX() - playerXBeforeUpdate) > 0.001f
                                                          || Math.abs(world.getPlayer().getY() - playerYBeforeUpdate) > 0.001f;
                if (playerMoved){handleGuidanceEvent(GameEvent.PLAYER_MOVED);}
            }
            if (waitingForWood){
                int woodAfterUpdate = world.getPlayer().getInventory().getQuantity(ItemType.WOOD);
                if (woodAfterUpdate > woodBeforeUpdate && woodAfterUpdate >= Config.GUIDANCE_WOOD_TARGET)
                    handleGuidanceEvent(GameEvent.WOOD_COLLECTED);
            }
            if (guidanceSystem.getCurrentStep() == GuideStep.DUSK_WARNING && world.getDayNightCycle().justBecameDusk()){
                handleDuskGuidance();
            }
            int healthAfterUpdate = world.getPlayer().getCurrentHp();
            if (healthAfterUpdate < healthBeforeUpdate && healthAfterUpdate > 0){
                guidanceSystem.activateContextualStep(GuideStep.HEALING);
            }

            if (world.getDayNightCycle().justBecameDay()) saveService.requestSave(saveSlot, world);

            if (world.getPlayer().isDead()) startDeathTransition();
            else {
                if (activeOverlay == GameOverlay.MENU) {
                    handleMenuInput();
                    gameMenuUi.act(delta);
                }
                else if (activeOverlay == GameOverlay.CHEST) {
                    handleChestInput();
                    overlayStage.act(delta);
                }
                else if (activeOverlay == GameOverlay.SCROLL){
                    handleScrollInput();
                    overlayStage.act(delta);
                }
                else if (activeOverlay == GameOverlay.NONE) {
                    if (world.getActiveChest() != null) openChest();
                    else handleTargetActionInput();
                }

                cameraController.update(world.getPlayer().getCenterX() * Config.SCALE,
                                        world.getPlayer().getCenterY() * Config.SCALE,
                                               worldRenderer.getMapRenderWidth(),
                                               worldRenderer.getMapRenderHeight(), delta);
            }
        }

        worldRenderer.render(world, cameraController);

        if (world.getTargetMode() != TargetMode.NONE){
            boolean targetValid = world.isCurrentTargetValid(tileTargetingSystem.getTileX(), tileTargetingSystem.getTileY(),
                                                           tileTargetingSystem.getWorldX(), tileTargetingSystem.getWorldY());
            worldRenderer.renderTargetPreview(cameraController, tileTargetingSystem.getWorldX(),
                                              tileTargetingSystem.getWorldY(), targetValid);
        }

        hudRenderer.render(world.getPlayer(), world.getDayNightCycle());

        if (activeOverlay == GameOverlay.MENU) gameMenuUi.draw();

        guidanceStage.act(delta);
        guidanceStage.draw();

        if (activeOverlay == GameOverlay.CHEST || activeOverlay == GameOverlay.PAUSE || activeOverlay == GameOverlay.SCROLL)
            overlayStage.draw();
        if (deathTransitionState != DeathTransitionState.NONE) renderDeathFade();

        if (activeOverlay == GameOverlay.WORLD_MAP) worldMapUi.draw();
    }

    private void toggleMenu(){
        if (activeOverlay == GameOverlay.MENU) activeOverlay = GameOverlay.NONE;
        else if (activeOverlay == GameOverlay.NONE) activeOverlay = GameOverlay.MENU;

        if (activeOverlay == GameOverlay.MENU) {
            gameMenuUi.onOpen();
            Gdx.input.setInputProcessor(gameMenuUi.getStage());
            world.cancelTargeting();
        } else {
            gameMenuUi.onClose();
            Gdx.input.setInputProcessor(null);
        }
    }

    private void closeChest(){
        if (activeOverlay != GameOverlay.CHEST) return;
        overlayStage.clear();
        chestUi = null;
        world.closeChest();
        activeOverlay = GameOverlay.NONE;
        Gdx.input.setInputProcessor(null);

        handleGuidanceEvent(GameEvent.CHEST_CLOSED);
    }

    private void openPause(){
        if (activeOverlay != GameOverlay.NONE) return;
        overlayStage.clear();

        Image dimOverlay = new Image(uiSkin.getDrawable("screen-dim"));
        dimOverlay.setFillParent(true);
        dimOverlay.setColor(0f, 0f, 0f, 0.5f);
        overlayStage.addActor(dimOverlay);

        Table overlayRoot = new Table();
        overlayRoot.setFillParent(true);
        overlayRoot.add(pauseMenuUi).width(700f).height(500f);

        overlayStage.addActor(overlayRoot);
        activeOverlay = GameOverlay.PAUSE;
        Gdx.input.setInputProcessor(overlayStage);

        world.cancelTargeting();
    }

    private void closePause(){
        if (activeOverlay != GameOverlay.PAUSE) return;

        overlayStage.clear();
        activeOverlay = GameOverlay.NONE;
        Gdx.input.setInputProcessor(null);
    }

    private void openSettingsFromPause(){
        if (activeOverlay != GameOverlay.PAUSE) return;
        Gdx.app.postRunnable(() -> game.showSettingsFromPause(this));
    }

    private void exitToMainMenu(){
        if (activeOverlay != GameOverlay.PAUSE) return;
        saveGame();
        Gdx.app.postRunnable(game::showMainMenuLoading);
    }

    private void openScroll (ItemType scrollType){
        if (activeOverlay != GameOverlay.NONE) return;

        openedScroll = scrollType;

        String keyPrefix = getScrollLocalizationPrefix(scrollType);
        String title = game.getLocalizationService().get(keyPrefix + ".title");
        String body = game.getLocalizationService().get(keyPrefix + ".body");

        scrollUi.showScroll(title, body);

        markScrollRead(scrollType);

        overlayStage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.add(scrollUi).width(760f).height(900f);

        overlayStage.addActor(root);

        activeOverlay = GameOverlay.SCROLL;
        scrollOpenedThisFrame = true;
        world.cancelTargeting();
        Gdx.input.setInputProcessor(overlayStage);
    }

    private String getScrollLocalizationPrefix(ItemType scrollType){
        return switch (scrollType){
            case SCROLL_I -> "scroll.first";
            case SCROLL_II -> "scroll.second";
            case SCROLL_III -> "scroll.third";

            default -> throw new IllegalStateException("Item is not a lore scroll.");
        };
    }

    private void markScrollRead(ItemType scrollType){
        switch (scrollType){
            case SCROLL_I -> world.getProgressionState().markScrollIRead();
            case SCROLL_II -> world.getProgressionState().markScrollIIRead();
            case SCROLL_III -> world.getProgressionState().markScrollIIIRead();

            default -> throw new IllegalArgumentException(
                "Item is not a lore scroll."
            );
        }
    }

    private void closeScroll(){
        if (activeOverlay != GameOverlay.SCROLL) return;

        overlayStage.clear();
        openedScroll = null;
        scrollOpenedThisFrame = false;

        activeOverlay = GameOverlay.NONE;
        Gdx.input.setInputProcessor(null);
    }

    public void saveGame(){
        saveService.requestSave(saveSlot, world);
    }

    private PlayerInput readPlayerInput(){
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(keyBindings.getMoveUpKey())) moveY += 1f;
        if (Gdx.input.isKeyPressed(keyBindings.getMoveDownKey())) moveY -= 1f;
        if (Gdx.input.isKeyPressed(keyBindings.getMoveLeftKey())) moveX -= 1f;
        if (Gdx.input.isKeyPressed(keyBindings.getMoveRightKey())) moveX += 1f;

        boolean primaryActionPressed = Gdx.input.isKeyJustPressed(keyBindings.getPrimaryActionKey());
        boolean interactPressed = Gdx.input.isKeyJustPressed(keyBindings.getInteractKey());
        boolean useItemPressed = Gdx.input.isKeyJustPressed(keyBindings.getUseItemKey());
        boolean dropItemPressed = Gdx.input.isKeyJustPressed(keyBindings.getDropItemKey());

        boolean controlPressed = Gdx.input.isKeyPressed(keyBindings.getDropWholeStackModifierKey());
        boolean dropWholeStack = dropItemPressed && controlPressed;

        boolean dashPressed = Gdx.input.isKeyJustPressed(keyBindings.getDashKey());
        int selectedHotbarSlot = -1;

        for (int i = 0; i < keyBindings.getHotbarSize(); i++) {
            if (Gdx.input.isKeyJustPressed(keyBindings.getHotbarKey(i))) {
                selectedHotbarSlot = i;
                break;
            }
        }

        return new PlayerInput(moveX, moveY, primaryActionPressed, interactPressed, useItemPressed, dropItemPressed, dropWholeStack,
            dashPressed, selectedHotbarSlot);
    }
    //input koji saljemo worldu dok smo u inventory/chest meniju, da bi svet i dalje radio ali Player ne bi
    //mogao da se seta, udara i sl.
    private PlayerInput getNeutralPlayerInput(){
        return new PlayerInput(0f, 0f, false, false, false,
            false, false, false, -1);
    }

    private void handleMenuInput(){
        if (Gdx.input.isKeyJustPressed(keyBindings.getPreviousMenuTabKey())) gameMenuUi.showPreviousTab();
        if (Gdx.input.isKeyJustPressed(keyBindings.getNextMenuTabKey())) gameMenuUi.showNextTab();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuUpKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveUpKey())) {
            gameMenuUi.moveSelectionUp();
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuDownKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveDownKey())) {
            gameMenuUi.moveSelectionDown();
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuLeftKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveLeftKey())) {
            gameMenuUi.moveSelectionLeft();
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuRightKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveRightKey())) {
            gameMenuUi.moveSelectionRight();
        }

        if (Gdx.input.isKeyJustPressed(keyBindings.getPrimaryActionKey())) gameMenuUi.confirmSelection();
    }

    private void handleChestInput(){
        if (Gdx.input.isKeyJustPressed(keyBindings.getInteractKey())) closeChest();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuUpKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveUpKey()))
            chestUi.moveSelectionUp();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuDownKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveDownKey()))
            chestUi.moveSelectionDown();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuLeftKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveLeftKey()))
            chestUi.moveSelectionLeft();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuRightKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveRightKey()))
            chestUi.moveSelectionRight();

        if (Gdx.input.isKeyJustPressed(keyBindings.getPrimaryActionKey()))chestUi.confirmSelection();
    }

    private void handleCancelBackInput(){
        if (!Gdx.input.isKeyJustPressed(keyBindings.getCancelBackKey())) return;

        if (activeOverlay == GameOverlay.WORLD_MAP){
            closeWorldMap();
            return;
        }

        if (activeOverlay == GameOverlay.MENU){
            toggleMenu();
            return;
        }

        if (activeOverlay == GameOverlay.CHEST){
            closeChest();
            return;
        }

        if (activeOverlay == GameOverlay.PAUSE){
            closePause();
            return;
        }

        if (activeOverlay == GameOverlay.SCROLL){
            closeScroll();
            return;
        }

        if (world.getTargetMode() != TargetMode.NONE){
            world.cancelTargeting();
            return;
        }

        if (activeOverlay == GameOverlay.NONE) openPause();
    }

    private void handleTargetActionInput(){
        if (!Gdx.input.isButtonJustPressed(keyBindings.getTargetActionButton())) return;
        if (world.getTargetMode() == TargetMode.NONE) return;

        int tileX = tileTargetingSystem.getTileX();
        int tileY = tileTargetingSystem.getTileY();

        boolean waitingForPlanting = guidanceSystem.getCurrentStep() == GuideStep.PLANTING;
        boolean hadPlantBefore = world.getFarmingSystem().getPlant(tileX, tileY) != null;

        world.handleTargetAction(tileTargetingSystem.getTileX(), tileTargetingSystem.getTileY(), tileTargetingSystem.getWorldX(), tileTargetingSystem.getWorldY());

        if (waitingForPlanting && !hadPlantBefore && world.getFarmingSystem().getPlant(tileX, tileY) instanceof Crop){
            handleGuidanceEvent(GameEvent.SEED_PLANTED);
        }
    }

    @Override public void resize(int i, int i1) {
        hudRenderer.resize(i, i1);
        gameMenuUi.resize(i, i1);
        overlayStage.getViewport().update(i, i1, true);
        guidanceStage.getViewport().update(i, i1, true);
        worldMapUi.resize(i, i1);
    }

    private void startDeathTransition(){
        deathTransitionState = DeathTransitionState.FADING_OUT;
        deathFadeAlpha = 0f;
        if (activeOverlay == GameOverlay.CHEST) closeChest();
        else if (activeOverlay == GameOverlay.MENU){
            gameMenuUi.onClose();
            activeOverlay = GameOverlay.NONE;
            Gdx.input.setInputProcessor(null);
        }
        else if (activeOverlay == GameOverlay.WORLD_MAP){
            closeWorldMap();
        }
        else if (activeOverlay == GameOverlay.SCROLL){
            closeScroll();
        }
        world.cancelTargeting();
    }

    private void updateDeathTransition(float delta){
        if (deathTransitionState == DeathTransitionState.FADING_OUT){
            deathFadeAlpha += delta / Config.DEATH_FADE_DURATION;

            if (deathFadeAlpha >= 1f){
                deathFadeAlpha = 1f;
                world.respawnPlayer();
                deathTransitionState = DeathTransitionState.FADING_IN;
            }
        }
        else if (deathTransitionState == DeathTransitionState.FADING_IN){
            deathFadeAlpha -= delta / Config.DEATH_FADE_DURATION;

            if (deathFadeAlpha <= 0){
                deathFadeAlpha = 0;
                deathTransitionState = DeathTransitionState.NONE;
            }
        }
    }

    private void renderDeathFade(){
        fadeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT));
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        fadeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        fadeRenderer.setColor(0f, 0f, 0f, deathFadeAlpha);
        fadeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        fadeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void openChest(){
        if (activeOverlay != GameOverlay.NONE) return;
        if (world.getActiveChest() == null) return;
        chestUi = new ChestUI(uiSkin, world.getPlayer().getInventory(), world.getActiveChest().getChestInventory());

        overlayStage.clear();

        Table overlayRoot = new Table();
        overlayRoot.setFillParent(true);
        overlayRoot.add(chestUi);

        overlayStage.addActor(overlayRoot);
        activeOverlay = GameOverlay.CHEST;
        Gdx.input.setInputProcessor(overlayStage);
        world.cancelTargeting();
    }

    private void showCurrentGuidanceMessage(){
        GuideStep currentStep = guidanceSystem.getCurrentStep();

        if (currentStep == null) {
            guidanceUi.hideMessage();
            return;
        }

        String message = game.getLocalizationService().get(currentStep.getMessageKey());
        String controlHint = getGuidanceControlHint(currentStep);
        guidanceUi.showMessage(message, controlHint);
    }

    private String getGuidanceControlHint(GuideStep step){
        return switch (step){
            case MOVEMENT -> "[WASD] Move";
            case STARTER_CHEST -> "[E] Interact";
            case PLANTING -> "[F] Use selected item   |   [Left Click] Choose block";
            case COLLECT_WOOD -> "[K] Use selected tool   |   [E] Pick up";
            case OPEN_CRAFTING -> "[TAB] to open menu | [Q] / [E] to switch tabs";
            default -> "";
        };
    }

    private void handleGuidanceInput(){
        if (!guidanceUi.isMessageVisible()) return;
        if (!Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) return;

        if (contextualGuidanceVisible){
            guidanceSystem.acknowledgeActiveContextualStep();
            contextualGuidanceVisible = false;
            guidanceUi.hideMessage();
            return;
        }

        GuideStep completedStep = guidanceSystem.getCurrentStep();

        boolean stepChanged = guidanceSystem.acknowledgeCurrentMessage();
        guidanceUi.hideMessage();

        if (stepChanged) scheduleNextGuidanceMessage(completedStep);
    }

    private void handleGuidanceEvent(GameEvent event){
        GuideStep completedStep = guidanceSystem.getCurrentStep();

        boolean stepChanged = guidanceSystem.handleEvent(event);
        if (!stepChanged) return;

        scheduleNextGuidanceMessage(completedStep);
    }

    private void scheduleNextGuidanceMessage(GuideStep completedStep){
        if (completedStep == GuideStep.CRAFT_EQUIPMENT) return;
        float delay = 0.18f;

        if (completedStep == GuideStep.STARTER_CHEST) delay = 1f;
        if (completedStep == GuideStep.OPEN_CRAFTING) delay = 0f;

        guidanceMessagePending = true;
        guidanceMessageDelay = delay;
    }

    private void updateGuidanceMessageDelay(float delta){
        if (!guidanceMessagePending) return;

        guidanceMessageDelay -= delta;

        if (guidanceMessageDelay > 0f) return;
        boolean craftingGuidanceInMenu = activeOverlay == GameOverlay.MENU
                                        && guidanceSystem.getCurrentStep() == GuideStep.CRAFT_EQUIPMENT;

        if (activeOverlay != GameOverlay.NONE && !craftingGuidanceInMenu) return;
        //ako npr. tad igrac otvori pause ili nesto slicno, da ne iskoci preko toga

        guidanceMessagePending = false;
        showCurrentGuidanceMessage();
    }

    private void handleSuccessfulCraft(){
        if (guidanceSystem.getCurrentStep() != GuideStep.CRAFT_EQUIPMENT) return;
        handleGuidanceEvent(GameEvent.ITEM_CRAFTED);
    }

    private void handleDuskGuidance(){
        guidanceSystem.handleEvent(GameEvent.DUSK_STARTED);

        if (activeOverlay == GameOverlay.NONE){
            showCurrentGuidanceMessage();
            return;
        }

        guidanceMessagePending = true;
        guidanceMessageDelay = 0f;
    }

    private void handleCraftingOpened(){
        if (guidanceSystem.getCurrentStep() != GuideStep.OPEN_CRAFTING) return;

        boolean messageAlreadyVisible = guidanceUi.isMessageVisible();

        GuideStep completedStep = guidanceSystem.getCurrentStep();
        boolean stepChanged = guidanceSystem.handleEvent(GameEvent.CRAFTING_OPENED);

        if (stepChanged){
            scheduleNextGuidanceMessage(completedStep);
            return;
        }
        if (!messageAlreadyVisible){
            guidanceMessagePending = false;
            stepChanged = guidanceSystem.acknowledgeCurrentMessage();
            if (stepChanged) scheduleNextGuidanceMessage(completedStep);
        }
    }

    private void handleWorldMapRequests(){
        if (activeOverlay != GameOverlay.WORLD_MAP) return;

        if (worldMapUi.isCloseRequested()){
            worldMapUi.clearCloseRequest();
            closeWorldMap();
            return;
        }

        AreaID destination = worldMapUi.getTravelRequestedArea();
        if (destination == null) return;

        worldMapUi.clearTravelRequest();
        boolean travelled = world.travelToArea(destination);
        if (!travelled) return;

        syncAreaView();
        closeWorldMap();
    }

    private void updateContextualGuidance(){
        if (contextualGuidanceVisible) return;
        if (guidanceSystem.getActiveContextualStep() == null) return;
        if (guidanceUi.isMessageVisible()) return;
        if (activeOverlay != GameOverlay.NONE) return;

        showActiveContextualMessage();
    }

    private void showActiveContextualMessage(){
        GuideStep contextualStep = guidanceSystem.getActiveContextualStep();

        if (contextualStep == null) return;
        contextualGuidanceVisible = true;

        String message = game.getLocalizationService().get(contextualStep.getMessageKey());
        guidanceUi.showMessage(message, "");
    }

    private void syncAreaView(){
        worldRenderer.setTileMap(world.getTileMap());
        tileTargetingSystem.setTileMap(world.getTileMap());
    }

    private void openWorldMap(){
        if (activeOverlay != GameOverlay.NONE) return;

        activeOverlay = GameOverlay.WORLD_MAP;
        world.cancelTargeting();
        worldMapUi.onOpen();

        Gdx.input.setInputProcessor(worldMapUi.getStage());
    }

    private void closeWorldMap(){
        if (activeOverlay != GameOverlay.WORLD_MAP) return;
        activeOverlay = GameOverlay.NONE;
        Gdx.input.setInputProcessor(null);
    }

    private void handleScrollInput(){
        if (activeOverlay != GameOverlay.SCROLL) return;

        if (scrollOpenedThisFrame){
            scrollOpenedThisFrame = false;
            return;
        }

        if (Gdx.input.isKeyJustPressed(keyBindings.getUseItemKey()) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            closeScroll();
        }
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        hudRenderer.dispose();
        world.dispose();
        if (Gdx.input.getInputProcessor() == gameMenuUi.getStage()) {
            Gdx.input.setInputProcessor(null);
        }
        gameMenuUi.dispose();
        fadeRenderer.dispose();
        overlayStage.dispose();
        guidanceStage.dispose();
        worldMapUi.dispose();
        scrollUi.dispose();
    }

    @Override public void show(){
        if (activeOverlay == GameOverlay.PAUSE || activeOverlay == GameOverlay.CHEST || activeOverlay == GameOverlay.SCROLL){
            Gdx.input.setInputProcessor(overlayStage);
        }
        else if (activeOverlay == GameOverlay.MENU){
            Gdx.input.setInputProcessor(gameMenuUi.getStage());
        }
        else if (activeOverlay == GameOverlay.WORLD_MAP){
            Gdx.input.setInputProcessor(worldMapUi.getStage());
        }
        else {
            Gdx.input.setInputProcessor(null);
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        if (Gdx.input.getInputProcessor() == overlayStage ||
            Gdx.input.getInputProcessor() == gameMenuUi.getStage() ||
            Gdx.input.getInputProcessor() == worldMapUi.getStage()){
            Gdx.input.setInputProcessor(null);
        }
    }
}
