package com.ashveil.ui.panels;

import com.ashveil.Config;
import com.ashveil.combat.HitCategory;
import com.ashveil.entities.Enemy;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.ui.inventory.InventoryDragData;
import com.ashveil.ui.inventory.InventorySlotUi;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import java.util.ArrayList;
import java.util.List;

//prikazuje svih 20 slotova
public class InventoryPanel extends MenuPanel {

    private final Inventory inventory;
    private int selectedSlotIndex = Config.SLOT_NOT_SELECTED;

    private final Table mainSlotsTable;
    private final Table hotbarTable;
    private final Table detailsTable;
    private final List<InventorySlotUi> slotViews;
    private final DragAndDrop dragAndDrop;

    public InventoryPanel(Skin skin, Inventory inventory) {
        super(skin);
        this.inventory = inventory;

        slotViews = new ArrayList<>();
        mainSlotsTable = new Table();
        hotbarTable = new Table();
        detailsTable = new Table();
        dragAndDrop = new DragAndDrop();
        dragAndDrop.setDragTime(0);
        dragAndDrop.setDragActorPosition(28f, -28f);

        createSlots(skin);
        createLayout(skin);
        refresh();
    }

    @Override
    public void refresh(){
        for (int i=0; i < Config.INVENTORY_SIZE; i++){
            slotViews.get(i).refresh(inventory.getSlot(i));
        }
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) {
            updateDetails();
        }
    }

    private void createSlots(Skin skin){
        for (int i=0; i < Config.INVENTORY_SIZE; i++){
            int slotIndex = i;

            InventorySlotUi slotUi = new InventorySlotUi(i, i < Config.HOTBAR_SIZE, skin);
            slotUi.addListener(new InputListener(){
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    selectSlot(slotIndex);
                }
            });

            registerDragAndDrop(slotUi, skin);

            slotViews.add(slotUi);
        }
    }

    private void registerDragAndDrop(InventorySlotUi slotUi, Skin skin){
        dragAndDrop.addSource(new DragAndDrop.Source(slotUi) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent inputEvent, float x, float y, int pointer) {
                int sourceIndex = slotUi.getSlotIndex();
                ItemStack sourceStack = inventory.getSlot(sourceIndex);
                if (sourceStack == null) return null;

                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(new InventoryDragData(sourceIndex));

                Image dragImage = new Image(skin.getDrawable("item-placeholder"));
                dragImage.setColor(InventorySlotUi.getTemporaryColor(sourceStack.getType()));
                Label dragQuantity = new Label("", skin);

                if (sourceStack.getQuantity() > 1) dragQuantity.setText(String.valueOf(sourceStack.getQuantity()));

                Stack dragActor = new Stack();

                Table imageLayer = new Table();
                imageLayer.add(dragImage).size(48);

                Table quantityLayer = new Table();
                quantityLayer.bottom().right();
                quantityLayer.add(dragQuantity).pad(3);

                dragActor.add(imageLayer);
                dragActor.add(quantityLayer);
                dragActor.setSize(56, 56);

                dragActor.setTouchable(Touchable.disabled);
                payload.setDragActor(dragActor);

                return payload;
            }
        });

        dragAndDrop.addTarget(new DragAndDrop.Target(slotUi) {
            @Override
            public boolean drag(
                DragAndDrop.Source source,
                DragAndDrop.Payload payload,
                float x,
                float y,
                int pointer
            ) {
                InventoryDragData dragData =
                    (InventoryDragData) payload.getObject();

                return dragData.getSourceSlotIndex() != slotUi.getSlotIndex();
            }

            @Override
            public void drop(
                DragAndDrop.Source source,
                DragAndDrop.Payload payload,
                float x,
                float y,
                int pointer
            ) {
                InventoryDragData dragData =
                    (InventoryDragData) payload.getObject();

                int sourceIndex = dragData.getSourceSlotIndex();
                int destinationIndex = slotUi.getSlotIndex();

                boolean moved = inventory.moveSlot(
                    sourceIndex,
                    destinationIndex
                );

                if (!moved) return;

                refresh();
                selectSlot(destinationIndex);
            }
        });

    }


    private void createLayout(Skin skin){
        setBackground(getSkin().getDrawable("inventory-panel-background"));
        pad(20);

        int localCounter = 0;
        for (int i=Config.HOTBAR_SIZE; i < Config.INVENTORY_SIZE; i++){
            localCounter++;
            mainSlotsTable.add(slotViews.get(i)).size(64).pad(5);
            if (localCounter == 5) {
                localCounter = 0;
                mainSlotsTable.row();
            }
        }

        for (int i=0; i < Config.HOTBAR_SIZE; i++){
            hotbarTable.add(slotViews.get(i)).size(64).pad(5);
        }

        Table inventorySection = new Table();

        inventorySection.add(new Label("Inventory", skin));
        inventorySection.row();
        inventorySection.add(mainSlotsTable);
        inventorySection.row();
        inventorySection.add(new Label("Hotbar", skin));
        inventorySection.row();
        inventorySection.add(hotbarTable);

        detailsTable.top().left();
        detailsTable.add(new Label("Empty slot.", skin));
        add(inventorySection).grow();
        add(detailsTable).width(260).growY().padLeft(30);
    }

    private void selectSlot(int slotIndex){
        if (selectedSlotIndex == slotIndex) return;
        if (slotIndex < 0 || slotIndex >= Config.INVENTORY_SIZE) return;
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) slotViews.get(selectedSlotIndex).setSelected(false);
        selectedSlotIndex = slotIndex;
        slotViews.get(selectedSlotIndex).setSelected(true);
        updateDetails();
    }

    private void updateDetails(){
        detailsTable.clearChildren();
        ItemStack item = inventory.getSlot(selectedSlotIndex);
        if (item == null) {
            detailsTable.add(new Label("Empty slot.", getSkin()));
            return;
        }
        detailsTable.add(new Label("Name: " + item.getType().getDisplayName(), getSkin())).left();
        detailsTable.row();
        detailsTable.add(new Label("Description: " + item.getType().getDescription(), getSkin())).padTop(20).left();
        detailsTable.row();
        if (item.getType().usesDurability()) detailsTable.add(new Label("Durability: " + item.getDurability() + " / " + item.getType().getMaxDurability(), getSkin())).left();
        detailsTable.row();
        if (item.getType().isStackable()) detailsTable.add(new Label("Quantity: " + item.getQuantity(), getSkin())).left();
        detailsTable.row();
        detailsTable.add(new Label("Damage: " + item.getType().getDamageProfile().getDamage(HitCategory.ENTITY), getSkin())).left();
    }

    @Override
    public void onHide(){
        clearSelection();
    }

    private void clearSelection() {
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) {
            slotViews.get(selectedSlotIndex).setSelected(false);
        }

        selectedSlotIndex = Config.SLOT_NOT_SELECTED;

        detailsTable.clearChildren();
        detailsTable.add(new Label("Empty slot.", getSkin()));
    }

}
