package com.ashveil.save;

import com.ashveil.save.data.SaveData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SaveManager {
    private final Json json;
    private final FileHandle saveDirectory;
    private final SaveValidator saveValidator;

    public SaveManager() {
        json = new Json();
        json.setUsePrototypes(false);
        json.setOutputType(JsonWriter.OutputType.json);
        saveDirectory = Gdx.files.local("saves");
        saveDirectory.mkdirs();
        saveValidator = new SaveValidator();
    }

    private FileHandle getSlotFile(int slot){
        validateSlot(slot);
        return saveDirectory.child("slot_" + slot + ".json");
    }

    private FileHandle getTempFile(int slot){
        validateSlot(slot);
        return saveDirectory.child("slot_" + slot + ".tmp");
    }

    private FileHandle getBackupFile(int slot){
        validateSlot(slot);
        return saveDirectory.child("slot_" + slot + ".bak");
    }

    private void validateSlot(int slot){
        if (slot < 1 || slot > 3) throw new IllegalArgumentException("Save slot must be between 1 and 3.");
    }

    public void save(int slot, SaveData saveData){
        if (saveData == null) throw new IllegalArgumentException("SaveData can't be null.");

        FileHandle slotFile = getSlotFile(slot);
        FileHandle tempFile = getTempFile(slot);
        FileHandle backupFile = getBackupFile(slot);

        String jsonText = json.prettyPrint(saveData);

        try {
            //novi save prvo kompletno pisemo u temp fajlu
            tempFile.writeString(jsonText, false, "UTF-8");
            //pre zamene cuvamo prethodni uspesan save kao jedan backup
            if (slotFile.exists()){
                Files.copy(slotFile.file().toPath(),
                           backupFile.file().toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
            }

            moveTempToSlot(tempFile, slotFile);
        }
        catch (IOException exception){
            tempFile.delete();
            throw new GdxRuntimeException("Failed to save game in slot " + slot + ".", exception);
            //ako dodje do neke greske s npr. permisijama, diska, i sl., brisemo nedovrseni temp i bacamo exception
        }
    }

    private void moveTempToSlot(FileHandle tempFile, FileHandle slotFile) throws IOException {
        try {
            Files.move(
                tempFile.file().toPath(),
                slotFile.file().toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        }
        catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                tempFile.file().toPath(),
                slotFile.file().toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    public SaveData load(int slot){
        FileHandle slotFile = getSlotFile(slot);
        SaveData saveData = readSaveDataFile(slotFile);
        if (saveData != null) return saveData;

        FileHandle backupFile = getBackupFile(slot);
        return readSaveDataFile(backupFile);
    }

    private SaveData readSaveDataFile(FileHandle file){
        if (!file.exists()) return null;
        try{
            String jsonText = file.readString("UTF-8");
            SaveData saveData = json.fromJson(SaveData.class, jsonText);

            if (!saveValidator.isValid(saveData)) return null;
            return saveData;
        }
        catch(Exception exception){
            return null;
        }
    }

    public SaveSlotInfo getSaveSlotStatus(int slot){
        return getSaveSlotInfo(slot);
    }

    public SaveSlotInfo getSaveSlotInfo(int slot){
        boolean hasPrimary = getSlotFile(slot).exists();
        boolean hasBackup = getBackupFile(slot).exists();

        if (!hasPrimary && !hasBackup) return SaveSlotInfo.empty(slot);

        SaveData saveData = load(slot);
        if (saveData == null) return SaveSlotInfo.invalid(slot);

        return SaveSlotInfo.valid(slot, saveData.savedAt, saveData.playTimeSeconds, saveData.currentAreaId, saveData.dayNight.dayCount, saveData.player.characterName);
    }

    public boolean deleteSlot(int slot){
        FileHandle slotFile = getSlotFile(slot);
        FileHandle backupFile = getBackupFile(slot);
        FileHandle tempFile = getTempFile(slot);

        boolean success = true;

        success &= deleteIfExists(slotFile);
        success &= deleteIfExists(backupFile);
        success &= deleteIfExists(tempFile);

        return success;
    }

    private boolean deleteIfExists(FileHandle file){
        if (!file.exists()) return true;
        return file.delete();
    }
}


