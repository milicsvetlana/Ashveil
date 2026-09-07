package com.ashveil.save;

import com.ashveil.save.data.SaveData;
import com.ashveil.world.World;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SaveService {
    private final SaveMapper saveMapper;
    private final SaveManager saveManager;
    private final ExecutorService saveExecutor;

    public SaveService(){
        saveMapper = new SaveMapper();
        saveManager = new SaveManager();

        saveExecutor = Executors.newSingleThreadExecutor();
    }

    public void requestSave(int slot, World world){
        if (world == null) throw new IllegalArgumentException("World can't be null.");

        SaveData snapshot = saveMapper.createSaveData(world);
        saveExecutor.execute(new Runnable() {
            @Override
            public void run() {
                saveManager.save(slot, snapshot);
            }
        });
    }

    public void shutdownAndWait(){
        saveExecutor.shutdown(); //nemoj primati nove zadatke, ali zavrsi postojece; ne ubija nit odmah

        try{
            if (!saveExecutor.awaitTermination(10, TimeUnit.SECONDS)) saveExecutor.shutdownNow();
            //sacekaj max 10 sekundi, ako traje duze od toga, onda je ubij odmah
        }
        catch (InterruptedException exception){
            saveExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
