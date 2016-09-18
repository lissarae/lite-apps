package com.chimbori.liteapps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Generates a skeleton Lite App with as many fields pre-filled as possible.
 */
public class Scaffolder {
  /**
   * The lite-apps.json file (containing metadata about all Lite Apps) is used as the basis for
   * generating scaffolding for the Lite App manifests.
   */
  public static boolean createScaffolding() throws JSONException, IOException {
    JSONArray library = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.SRC_LITE_APPS_JSON)));
    for (int i = 0; i < library.length(); i++) {
      JSONArray apps = library.getJSONObject(i).getJSONArray(JSONConstants.Fields.APPS);
      for (int j = 0; j < apps.length(); j++) {
        JSONObject app = apps.getJSONObject(j);
        String appName = app.getString(JSONConstants.Fields.NAME);

        // Create the root directory if it doesn’t exist yet.
        File liteAppDirectoryRoot = new File(FileUtils.SRC_ROOT_DIR, appName);
        if (!liteAppDirectoryRoot.exists()) {
          liteAppDirectoryRoot.mkdirs();
        }

        // If the manifest.json exists, read it before modifying, else create a new JSON object.
        File manifestJson = new File(liteAppDirectoryRoot, FileUtils.MANIFEST_JSON_FILE_NAME);
        JSONObject root;
        if (manifestJson.exists()) {
          root = new JSONObject(FileUtils.readFully(new FileInputStream(manifestJson)));
          if (!root.has(JSONConstants.Fields.UNDER_DEVELOPMENT)) {
            // If this file is not under active development (i.e. ready for release),
            // then don’t attempt to modify the manifest.
            continue;
          }
        } else {
          root = new JSONObject();
        }

        // Constant fields, same for all apps.
        root.put(JSONConstants.Fields.MANIFEST_VERSION, 1);
        root.put(JSONConstants.Fields.LANG, JSONConstants.Values.EN);
        root.put(JSONConstants.Fields.ICONS, new JSONArray().put(new JSONObject().put("src", "icon.png")));
        root.put(JSONConstants.Fields.UNDER_DEVELOPMENT, true);

        // Fields that are correctly populated using the data available in the original JSON file.
        root.put(JSONConstants.Fields.NAME, appName);
        root.put(JSONConstants.Fields.START_URL, app.getString(JSONConstants.Fields.URL));
        root.put(JSONConstants.Fields.MANIFEST_URL, String.format("https://hermit.chimbori.com/lite-apps/%s.hermit", appName));

        // Fields that are reasonable defaults, but need to be modified by hand before final release.
        root.put(JSONConstants.Fields.THEME_COLOR, "#fe7a4d");
        root.put(JSONConstants.Fields.SECONDARY_COLOR, "#fe7a4d");

        try (PrintWriter writer = new PrintWriter(manifestJson)) {
          writer.print(root.toString(2));
        }
      }
    }
    return true;
  }

  public static void main(String[] arguments) {
    try {
      Scaffolder.createScaffolding();
    } catch (JSONException | IOException e) {
      e.printStackTrace();
    }
  }
}