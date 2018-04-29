package com.udacity.sandwichclub.utils;

import android.support.annotation.Nullable;

import com.udacity.sandwichclub.model.Sandwich;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonUtils {
    private static Logger logger = Logger.getLogger("JsonUtils");

    public static Sandwich parseSandwichJson(String json) {
        Map<String, Object> jsonObj = parseJsonObject(json);

        try {
            Map<String, Object> nameObj = (Map<String, Object>) jsonObj.get("name");

            String mainName = (String) nameObj.get("mainName");
            List<String> alsoKnownAs = (List<String>) nameObj.get("alsoKnownAs");
            String placeOfOrigin = (String) jsonObj.get("placeOfOrigin");
            String description = (String) jsonObj.get("description");
            String image = (String) jsonObj.get("image");
            List<String> ingredients = (List<String>) jsonObj.get("ingredients");

            return new Sandwich(mainName, alsoKnownAs, placeOfOrigin, description, image, ingredients);
        } catch (NullPointerException ex) {
            logger.log(Level.WARNING, "Failed to parse the sandwich JSON");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to extract data from the sandwich JSON with the following error: " + ex.getMessage());
        }
        return null;
    }

    /**
     * These methods will parse the given string to a generic JSON-like Map one character at a time
     *
     * I believe that this would work for most JSONs
     * (though I know it would fail to parse any kind of JSON array that wasn't just strings)
     */
    private static Map<String, Object> parseJsonObject(String s) {
        return parseJsonObject(new LinkedList<>(Arrays.asList(s.split("(?!^)"))));
    }

    @Nullable
    private static Map<String, Object> parseJsonObject(List<String> parts) {
        Map<String, Object> json = new HashMap<>();

        String key = "";
        StringBuilder workingString = new StringBuilder();
        List<String> workingArray = new ArrayList<>();

        Boolean openQuote = false;
        Boolean openArray = false;


        while (parts.size() > 0) {
            String workingPart = parts.remove(0);

            if (openQuote) {
                if (workingPart.equals("\"")) {
                    openQuote = false;
                    continue;
                } else if (workingPart.equals("\\")) {
                    // If something was escaped, make sure to grab it too
                    String next = parts.remove(0);
                    if (next.equals("\"") || next.equals("\'")) {
                        workingPart = next;
                    } else {
                        workingPart += next;
                    }
                }
                workingString.append(workingPart);
            } else {
                switch (workingPart) {
                    case "{":
                        // Skip the first open curly brace
                        if (!key.isEmpty()) {
                            json.put(key, parseJsonObject(parts));
                            key = "";
                        }
                        break;
                    case "}":
                        return json;
                    case "[":
                        openArray = true;
                        break;
                    case "]":
                        if (workingString.length() > 0) {
                            workingArray.add(workingString.toString());
                        }
                        json.put(key, workingArray);

                        key = "";
                        workingArray = new ArrayList<>();
                        workingString = new StringBuilder();
                        openArray = false;
                        break;
                    case "\"":
                        openQuote = true;
                        break;
                    case ":":
                        key = workingString.toString();

                        workingString = new StringBuilder();
                        break;
                    case ",":
                        // A comma is only important after following a string, other instances are
                        // covered by other characters
                        if (openArray) {
                            workingArray.add(workingString.toString());

                            workingString = new StringBuilder();
                        } else if (!key.isEmpty()) {
                            json.put(key, workingString.toString());

                            workingString = new StringBuilder();
                        }
                        break;
                    case " ":
                        break;
                }
            }
        }

        // If the json object didn't close, then something messed up
        return null;
    }
}
