package org.nigelsmall.util;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JSON {

    /**
     * Parse the supplied text as a JSON object; might validly be empty so
     * fail gracefully in that case
     *
     * @param json the JSON json to parse
     * @return a String:Object collection
     * @throws JSONException when all hope is gone...
     */
    public static Map<String,Object> toObject(String json)
    throws JSONException
    {
        if(json == null || json.length() == 0) {
            return null;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return (Map<String, Object>)mapper.readValue(json, Object.class);
            } catch(ClassCastException e) {
                throw new JSONException("Unable to cast JSON to Map<String,Object>", e);
            } catch(IOException e) {
                throw new JSONException("Unable to read JSON", e);
            }
        }
    }

}
