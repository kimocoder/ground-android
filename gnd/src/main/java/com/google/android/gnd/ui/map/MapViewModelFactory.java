/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gnd.ui.map;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import com.google.android.gnd.model.GndDataRepository;
import com.google.android.gnd.system.LocationManager;
import javax.inject.Inject;

class MapViewModelFactory implements ViewModelProvider.Factory {

  private final GndDataRepository dataRepository;
  private final LocationManager locationManager;

  @Inject
  MapViewModelFactory(GndDataRepository dataRepository,
      LocationManager locationManager) {
    this.dataRepository = dataRepository;
    this.locationManager = locationManager;
  }

  @Override
  public <T extends ViewModel> T create(Class<T> modelClass) {
    if (!modelClass.isAssignableFrom(MapViewModel.class)) {
      throw new IllegalArgumentException("Invalid ViewModel class");
    }
    return (T) new MapViewModel(dataRepository, locationManager);
  }
}
