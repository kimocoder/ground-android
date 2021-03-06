/*
 * Copyright 2020 Google LLC
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

package com.google.android.gnd.persistence.geojson;

import static com.google.android.gnd.util.ImmutableListCollector.toImmutableList;
import static java8.util.stream.StreamSupport.stream;

import android.util.Log;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gnd.model.basemap.tile.Tile;
import com.google.android.gnd.model.basemap.tile.Tile.State;
import com.google.android.gnd.persistence.uuid.OfflineUuidGenerator;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoJsonParser {

  private static final String TAG = GeoJsonParser.class.getSimpleName();
  private final OfflineUuidGenerator uuidGenerator;
  private static final String FEATURES_KEY = "features";
  private static final String JSON_SOURCE_CHARSET = "UTF-8";

  @Inject
  GeoJsonParser(OfflineUuidGenerator uuidGenerator) {
    this.uuidGenerator = uuidGenerator;
  }

  /**
   * Returns the immutable list of tiles specified in {@param geojson} that intersect {@param
   * bounds}.
   */
  public ImmutableList<Tile> intersectingTiles(LatLngBounds bounds, File file) {
    try {
      String fileContents = FileUtils.readFileToString(file, Charset.forName(JSON_SOURCE_CHARSET));
      // TODO: Separate parsing and intersection checks, make asyc (single, completable).
      JSONObject geoJson = new JSONObject(fileContents);
      // TODO: Make features constant.
      JSONArray features = geoJson.getJSONArray(FEATURES_KEY);

      return stream(toArrayList(features))
          .map(GeoJsonTile::new)
          .filter(tile -> tile.boundsIntersect(bounds))
          .map(this::jsonToTile)
          .collect(toImmutableList());

    } catch (JSONException | IOException e) {
      Log.e(TAG, "Unable to parse JSON", e);
    }

    return ImmutableList.of();
  }

  /**
   * Converts a JSONArray to an array of JSONObjects. Provided for compatibility with java8 streams.
   * JSONArray itself only inherits from Object, and is not convertible to a stream.
   */
  private static List<JSONObject> toArrayList(JSONArray arr) {
    List<JSONObject> result = new ArrayList<>();

    for (int i = 0; i < arr.length(); i++) {
      try {
        result.add(arr.getJSONObject(i));
      } catch (JSONException e) {
        Log.e(TAG, "Ignoring error in JSON array", e);
      }
    }

    return result;
  }

  /** Returns the {@link Tile} specified by {@param json}. */
  private Tile jsonToTile(GeoJsonTile json) {
    // TODO: Instead of returning tiles with invalid state (empty URL/ID values)
    // Throw an exception here and handle it downstream.
    return Tile.newBuilder()
        .setId(uuidGenerator.generateUuid())
        .setUrl(json.getUrl().orElse(""))
        .setState(State.PENDING)
        .setPath(Tile.pathFromId(json.getId().orElse("")))
        .build();
  }
}
