package com.ashveil.ui.worldmap;

import com.ashveil.world.WorldMapAccess;
import com.ashveil.world.area.AreaID;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.EnumMap;
import java.util.Map;

public class WorldMapUi implements Disposable {
    private final Stage stage;
    private final Skin skin;
    private final WorldMapAccess worldMapAccess;

    private static final float DESIGN_WIDTH = 3840f;
    private static final float DESIGN_HEIGHT = 2160f;

    private final Texture backgroundTexture;
    private final Image backgroundImage;

    private final Texture lockedMarkerTexture;
    private final Texture availableMarkerTexture;
    private final Texture currentMarkerTexture;
    private final Texture selectedMarkerTexture;

    private final Map<AreaID, Image> markers;
    private AreaID selectedAreaId;

    public WorldMapUi(Skin skin, WorldMapAccess worldMapAccess){
        if (skin == null) throw new IllegalArgumentException("Skin cannot be null");
        if (worldMapAccess == null) throw new IllegalArgumentException("World map access cannot be null.");

        this.skin = skin;
        this.worldMapAccess = worldMapAccess;
        this.stage = new Stage(new FitViewport(DESIGN_WIDTH, DESIGN_HEIGHT));

        backgroundTexture = new Texture(Gdx.files.internal("ui/world-map/world-map.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        backgroundImage = new Image(backgroundTexture);

        lockedMarkerTexture = new Texture(Gdx.files.internal("ui/world-map/world-map-marker-locked.png"));
        availableMarkerTexture = new Texture(Gdx.files.internal("ui/world-map/world-map-marker-available.png"));
        currentMarkerTexture = new Texture(Gdx.files.internal("ui/world-map/world-map-marker-current.png"));
        selectedMarkerTexture = new Texture(Gdx.files.internal("ui/world-map/world-map-marker-selected.png"));

        lockedMarkerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        availableMarkerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        currentMarkerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        selectedMarkerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        markers = new EnumMap<>(AreaID.class);
        selectedAreaId = null;
        createLayout();
    }

    private void createLayout(){
        backgroundImage.setBounds(0f, 0f, DESIGN_WIDTH, DESIGN_HEIGHT);
        stage.addActor(backgroundImage);

        Image mainIslandMarker = createMarker(AreaID.MAIN_ISLAND, currentMarkerTexture, 1750f, 1020f, 260f, 260f);
        Image windyPlainsMarker = createMarker(AreaID.WINDY_PLAINS, availableMarkerTexture, 2680f, 1400f, 220f, 220);
        Image darkrootIsleMarker = createMarker(AreaID.DARKROOT_ISLE, lockedMarkerTexture, 850f, 980f, 220f, 220);
        Image veilscarPassageMarker = createMarker(AreaID.VEILSCAR_PASSAGE, lockedMarkerTexture, 2660f, 650f, 220f, 220f);

        stage.addActor(mainIslandMarker);
        stage.addActor(windyPlainsMarker);
        stage.addActor(darkrootIsleMarker);
        stage.addActor(veilscarPassageMarker);

        refreshMarkers();
    }

    private Image createMarker(AreaID areaID, Texture texture, float x, float y, float size1, float size2){
        Image marker = new Image(texture);

        marker.setSize(size1, size2);
        marker.setPosition(x, y);

        marker.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if (getMarkerState(areaID) != WorldMapMarkerState.AVAILABLE) return;

                selectedAreaId = areaID;
                refreshMarkers();
            }
        });

        markers.put(areaID, marker);

        return marker;
    }

    private WorldMapMarkerState getMarkerState(AreaID areaID){
        if (areaID == worldMapAccess.getCurrentAreaId()) return WorldMapMarkerState.CURRENT;
        if (!worldMapAccess.isAreaUnlocked(areaID)) return WorldMapMarkerState.LOCKED;
        if (areaID == selectedAreaId) return WorldMapMarkerState.SELECTED;
        return WorldMapMarkerState.AVAILABLE;
    }

    private Texture getMarkerTexture(WorldMapMarkerState state){
        return switch (state){
            case LOCKED -> lockedMarkerTexture;
            case AVAILABLE -> availableMarkerTexture;
            case CURRENT -> currentMarkerTexture;
            case SELECTED -> selectedMarkerTexture;
        };
    }

    private void refreshMarkers(){
        for (Map.Entry<AreaID, Image> entry : markers.entrySet()){
            AreaID areaID = entry.getKey();
            Image marker = entry.getValue();

            WorldMapMarkerState state = getMarkerState(areaID);
            Texture texture = getMarkerTexture(state);

            marker.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
        }
    }

    public void onOpen(){
        selectedAreaId = null;
        refreshMarkers();
    }
    public void act(float delta){
        stage.act(delta);
    }
    public void draw(){stage.draw();}
    public void resize(int width, int height){stage.getViewport().update(width, height,true);}

    public Stage getStage(){return stage;}

    @Override
    public void dispose(){
        stage.dispose();
        backgroundTexture.dispose();
        lockedMarkerTexture.dispose();
        availableMarkerTexture.dispose();
        currentMarkerTexture.dispose();
        selectedMarkerTexture.dispose();
    }
}
