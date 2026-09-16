package com.ashveil.save;

import com.ashveil.save.data.SaveData;
import com.ashveil.world.World;

import java.util.concurrent.*;

public class SaveService {
    private final SaveMapper saveMapper;
    private final SaveManager saveManager;
    private final ExecutorService ioExecutor;

    public SaveService(){
        saveMapper = new SaveMapper();
        saveManager = new SaveManager();

        ioExecutor = Executors.newSingleThreadExecutor();
    }

    public void requestSave(int slot, World world){
        if (world == null) throw new IllegalArgumentException("World can't be null.");

        SaveData snapshot = saveMapper.createSaveData(world);
        ioExecutor.execute(new Runnable() { //execute - posalji posao, ne treba mi rezultat
            @Override
            public void run() {
                saveManager.save(slot, snapshot);
            }
        });
    }

    public void shutdownAndWait(){
        ioExecutor.shutdown(); //nemoj primati nove zadatke, ali zavrsi postojece; ne ubija nit odmah

        try{
            if (!ioExecutor.awaitTermination(10, TimeUnit.SECONDS)) ioExecutor.shutdownNow();
            //sacekaj max 10 sekundi, ako traje duze od toga, onda je ubij odmah
        }
        catch (InterruptedException exception){
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public World loadWorld(int slot){
        SaveData saveData = loadSaveData(slot);
        if (saveData == null) return null;
        return saveMapper.createWorld(saveData);
    }

    private SaveData loadSaveData(int slot){
        Future<SaveData> loadTask = ioExecutor.submit(() -> saveManager.load(slot));
        //submit pozvanu savemanager.load stavlja u red executora
        try{
            return loadTask.get(); //ako je rezultat spreman, vraca ga odmah; u suprotnom - blokira trenutni thread dok load ne zavrsi
        }
        catch (InterruptedException exception){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Game loading was interrupted", exception);
        }
        catch (ExecutionException exception){
            throw new IllegalStateException("Failed to load game.", exception.getCause());
        }
    }

    public SaveSlotInfo getSlotInfo(int slot){
        Future<SaveSlotInfo> slotInfoTask = ioExecutor.submit(() -> saveManager.getSaveSlotInfo(slot));

        try{
            return slotInfoTask.get();
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Save slot inspection was interrupted.", exception);
        }
        catch (ExecutionException executionException){
            throw new IllegalStateException("Failed to inspect save slot.", executionException.getCause());
        }
    }

    public boolean deleteSlot(int slot){
        Future<Boolean> future = ioExecutor.submit(() -> saveManager.deleteSlot(slot));

        try{
            return future.get();
        }
        catch (InterruptedException exception){
            Thread.currentThread().interrupt();
            return false;
        }
        catch (ExecutionException exception){
            return false;
        }
    }

}
