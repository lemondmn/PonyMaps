package mx.edu.ubicatec.ponymaps.utils

import android.content.res.Resources
import mx.edu.ubicatec.ponymaps.R
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class JsonParser(var resources: Resources) {

    private fun getJSONFromAssets(file: Int): String? {
        //var file = R.raw.distances
        var json: String? = null
        val charset: Charset = Charsets.UTF_8
        try {

            val myUsersJSONFile = resources.openRawResource(file)
            val size = myUsersJSONFile.available()
            val buffer = ByteArray(size)
            myUsersJSONFile.read(buffer)
            myUsersJSONFile.close()
            json = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    private fun getJSONObject(file: Int) {

        try {
            // As we have JSON object, so we are getting the object
            //Here we are calling a Method which is returning the JSON object

           var obj = JSONObject(getJSONFromAssets(file))
            /*
            // fetch JSONArray named edges by using getJSONArray
            val usersArray = obj.getJSONArray("edges")

            for (i in 0 until usersArray.length()) {
                // Create a JSONObject for fetching single User's Data
                val edge = usersArray.getJSONObject(i)
                // Fetch id store it in variable
                val origen = edge.getString("origen")
                val destino = edge.getString("destino")
                val dist = edge.getInt("dist")

                // Now add all the variables to the data model class and the data model class to the array list.
                val edg = Edges(origen, destino, dist)

                // add the details in the list
                edges.add(edg)
            }
            */

        } catch (e: JSONException) {
            //exception
            e.printStackTrace()
        }

    }

}