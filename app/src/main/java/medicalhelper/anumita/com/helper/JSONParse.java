package medicalhelper.anumita.com.helper;

import org.json.JSONObject;

/**
 * Created by Nevon on 3/9/2018.
 */

public class JSONParse {

    public String Parse(JSONObject json) {
        try
        {
            return json.getString("Value");
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }
}
