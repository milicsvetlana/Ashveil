package com.ashveil.world;

import com.ashveil.Config;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class CameraController {
    public OrthographicCamera camera;

    public CameraController(){
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    public void update(float playerX, float playerY) {

        float mapWidth = Config.WORLD_WIDTH * Config.TILE_DRAW_SIZE;
        float mapHeight = Config.WORLD_HEIGHT * Config.TILE_DRAW_SIZE;

        float targetX = Math.max(Config.SCREEN_WIDTH / 2f,
                     Math.min(playerX, mapWidth - Config.SCREEN_WIDTH / 2f));

        float targetY = Math.max(Config.SCREEN_HEIGHT / 2f,
                     Math.min(playerY, mapHeight - Config.SCREEN_HEIGHT / 2f));

        camera.position.x += (targetX - camera.position.x) * 0.2f;
        camera.position.y += (targetY - camera.position.y) * 0.2f;

        camera.update();
    }
}
