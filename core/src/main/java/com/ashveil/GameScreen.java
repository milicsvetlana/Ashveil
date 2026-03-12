package com.ashveil;

import com.ashveil.entities.Player;
import com.ashveil.entities.ZombieEnemy;
import com.ashveil.world.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;

import static com.ashveil.Config.SCREEN_HEIGHT;
import static com.ashveil.Config.SCREEN_WIDTH;

public class GameScreen implements Screen {

    private GameApp game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private HudRenderer hudRenderer;
    private World world;
    private CameraController cameraController;

    public GameScreen(GameApp game){
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        cameraController = new CameraController();
        world = new World();
        hudRenderer = new HudRenderer();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        world.update(delta);
        if (world.getPlayer().isDead()){
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }
        cameraController.update(world.getPlayer().getX() * Config.SCALE, world.getPlayer().getY() * Config.SCALE);
        shapeRenderer.setProjectionMatrix(cameraController.camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y=0; y < Config.WORLD_HEIGHT; y++){
            for (int x=0; x < Config.WORLD_WIDTH; x++){
                TileType tile = world.getTileMap().getTile(x, y);
                if (tile == TileType.GRASS){
                    shapeRenderer.setColor(0.2f, 0.6f, 0.2f, 1f);
                }
                else if (tile == TileType.WATER){
                    shapeRenderer.setColor(0.1f, 0.3f, 0.8f, 1f);
                }
                else if (tile == TileType.SAND){
                    shapeRenderer.setColor(0.9f, 0.8f, 0.5f, 1f);
                }
                shapeRenderer.rect(
                    x * Config.TILE_DRAW_SIZE,
                    y * Config.TILE_DRAW_SIZE,
                    Config.TILE_DRAW_SIZE,
                    Config.TILE_DRAW_SIZE
                );
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        shapeRenderer.rect(world.getPlayer().getX() * Config.SCALE,
                            world.getPlayer().getY()  * Config.SCALE,
                            Config.TILE_DRAW_SIZE,
                            Config.TILE_DRAW_SIZE);
        shapeRenderer.setColor(1f, 0f, 0f, 1f);

        for (ZombieEnemy z : world.getZombies()){
            shapeRenderer.rect(z.getX()  * Config.SCALE,
                                z.getY() * Config.SCALE,
                                Config.TILE_DRAW_SIZE,
                                Config.TILE_DRAW_SIZE);
        }

        shapeRenderer.setColor(0.5f, 0.5f, 0f, 1f);
        for (WorldItem i : world.getGroundItems()){
            shapeRenderer.rect(i.getX()  * Config.SCALE,
                i.getY() * Config.SCALE,
                Config.TILE_DRAW_SIZE,
                Config.TILE_DRAW_SIZE);
        }
        shapeRenderer.end();
        if (world.getDayNightCycle().isNight()){
            shapeRenderer.setProjectionMatrix(
                new Matrix4().setToOrtho2D(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT)
            );
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0.3f, 0.5f);
            shapeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        hudRenderer.render(world.getPlayer(), world.getDayNightCycle());
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudRenderer.dispose();
    }

    @Override public void resize(int i, int i1) {}
    @Override public void show(){}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
