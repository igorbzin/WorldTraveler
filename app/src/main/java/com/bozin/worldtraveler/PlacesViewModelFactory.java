package com.bozin.worldtraveler;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class PlacesViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {
    /**
     * Creates a {@code AndroidViewModelFactory}
     *
     *
     * @param application an application to pass in {@link AndroidViewModel}
     */

    private Application application;


    public PlacesViewModelFactory(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PlacesViewModel(application);
    }


}
