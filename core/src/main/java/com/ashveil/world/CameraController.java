package com.ashveil.world;

import com.ashveil.Config;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class CameraController {
    public OrthographicCamera camera;

    public CameraController(){
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    public void update(float playerX, float playerY, float delta) {

        float mapWidth = Config.WORLD_WIDTH * Config.TILE_DRAW_SIZE;
        float mapHeight = Config.WORLD_HEIGHT * Config.TILE_DRAW_SIZE;

        float targetX = Math.max(Config.SCREEN_WIDTH / 2f,
                     Math.min(playerX, mapWidth - Config.SCREEN_WIDTH / 2f));

        float targetY = Math.max(Config.SCREEN_HEIGHT / 2f,
                     Math.min(playerY, mapHeight - Config.SCREEN_HEIGHT / 2f));

        //brzina kojom kamera prati igraca, mnozenje s delta cini pracenje nezavisnim od FPS
        //alpha ogranicavamo na 1 da kamera ne bi presla slucajno preko ciljne pozicije u nekom trenutku
        float alpha = Math.min(1f, Config.CAMERA_SMOOTHING * delta);

        camera.position.x += (targetX - camera.position.x) * alpha;
        camera.position.y += (targetY - camera.position.y) * alpha;

        camera.update();
    }
}
