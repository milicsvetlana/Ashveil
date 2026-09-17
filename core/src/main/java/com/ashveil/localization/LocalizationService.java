package com.ashveil.localization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

public class LocalizationService {
    private I18NBundle bundle;

    public LocalizationService(String language){
        setLanguage(language);
    }

    public void setLanguage(String language){
        Locale locale;

        if ("Srpski".equals(language)) {
            locale = Locale.of("sr");
        }
        else {
            locale = Locale.ENGLISH;
        }

        bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/messages"), locale);
    }

    public String get(String key){
        return bundle.get(key);
    }
}
