package android.ohdm.de.editor;

import android.ohdm.de.editor.Geometry.PolyObject.PolyObject;
import android.ohdm.de.editor.Geometry.PolyObject.PolyObjectFactory;
import android.ohdm.de.editor.Geometry.PolyObject.PolyObjectType;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class JSONReader {

    private static final String TAG = "JSONReader";
    private static final String OHDMAPI = "http://141.45.146.152:8080/OhdmApi/geographicObject/";
    private static final String GEOMETRICOBJECT = "geometricObjects";
    private static final String TAGDATES = "tagDates";
    private static final String TAGS = "tags";

    private static final String MULTILINESTRING = "multilinestring";
    private static final String MULTIPOINT = "multipoint";
    private static final String MULTIPOLYGON = "multipolygon";

    public static PolyObject getPolyObjectById(int objectId, MapView mapView) {

        String id = String.valueOf(objectId);
        PolyObject polyObject = null;
        PolyObjectType type;
        List<GeoPoint> geoPoints;
        HashMap<String,String> tagDates;

        JSONObject jsonObject = getJSONObject(OHDMAPI + id);

        if(jsonObject != null) {

            try {

                JSONArray geometricObject = jsonObject.getJSONArray(GEOMETRICOBJECT);
                JSONArray tagDatesObject = jsonObject.getJSONArray(TAGDATES);

                //TODO: können auch mehrere Geometrien sein
                JSONObject geom = (JSONObject) geometricObject.get(0);
                JSONObject tags = (JSONObject) tagDatesObject.get(0);

                type = getPolyObjectType(geom);
                geoPoints = getGeoPoints(geom);
                tagDates = getTagDates((JSONObject)tags.get(TAGS));

                polyObject = PolyObjectFactory.buildObject(type, mapView);
                polyObject.setPoints(geoPoints);
                polyObject.setTags(tagDates);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polyObject;
    }

    private static HashMap<String,String> getTagDates(JSONObject tags) {

        Iterator<String> keys = tags.keys();
        HashMap<String,String> parsedTagDates = new HashMap<String,String>();

        while(keys.hasNext()){
            String key = keys.next();

            try {
                String value = tags.getString(key);
                Log.d(TAG,"key: "+key+", value: "+value);
                parsedTagDates.put(key,value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return parsedTagDates;
    }

    private static JSONObject getJSONObject(String objectUrl) {

        JSONObject geoObject = null;

        try {
            URL url = new URL(objectUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type",
                    "application/json; charset=utf-8");

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            try {

                geoObject = new JSONObject(response.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return geoObject;
    }

    private static PolyObjectType getPolyObjectType(JSONObject geom) {

        PolyObjectType type = null;

        try {
            if (!geom.get(MULTIPOLYGON).toString().equals("null")) {
                type = PolyObjectType.POLYGON;
            } else if (!geom.get(MULTILINESTRING).toString().equals("null")) {
                type = PolyObjectType.POLYLINE;
            } else if (!geom.get(MULTIPOINT).toString().equals("null")) {
                type = PolyObjectType.POINT;
            } else {
                Log.e(TAG, "Unknown Geometric.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(type!=null)Log.i(TAG, "Type is: " + type.toString());

        return type;

    }

    private static List<GeoPoint> getGeoPoints(JSONObject geom) {

        List<GeoPoint> geoPoints = null;
        PolyObjectType type = getPolyObjectType(geom);

        try {
            switch (type) {
                case POINT:
                    geoPoints = extractGeoPointsFromMultiPoint(geom.get(MULTIPOINT).toString());
                    break;
                case POLYLINE:
                    geoPoints = extractGeoPointsFromMultiLineString(geom.get(MULTILINESTRING).toString());
                    break;
                case POLYGON:
                    geoPoints = extractGeoPointsFromMultipolygon(geom.get(MULTIPOLYGON).toString());
                    break;
                default:
                    Log.e(TAG, "Unknown PolyObjectType");
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return geoPoints;
    }

    private static List<GeoPoint> extractGeoPointsFromMultiPoint(String input) {

        List<GeoPoint> geoPoints = null;

        PGgeometry geometry;

        try {
            geometry = new PGgeometry(input);
            MultiPoint multipoint = (MultiPoint) geometry.getGeometry();

            //TODO: können auch mehrere points sein (werden hier nicht korrekt als mehrere points behandelt)
            geoPoints = convertPointsToGeoPoints(multipoint.getPoints());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return geoPoints;
    }

    private static List<GeoPoint> extractGeoPointsFromMultiLineString(String input) {

        List<GeoPoint> geoPoints = null;

        PGgeometry geometry;

        try {
            geometry = new PGgeometry(input);
            MultiLineString lineString = (MultiLineString) geometry.getGeometry();

            //TODO: können auch mehrere lines sein
            geoPoints = convertPointsToGeoPoints(lineString.getLines()[0].getPoints());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return geoPoints;
    }

    private static List<GeoPoint> extractGeoPointsFromMultipolygon(String input) {

        List<GeoPoint> geoPoints = null;

        PGgeometry geometry;
        try {
            geometry = new PGgeometry(input);
            MultiPolygon polygon = (MultiPolygon) geometry.getGeometry();

            //TODO: können auch mehrere polygone UND mehrere "ringe" (in den Polygonen) sein
            geoPoints = convertPointsToGeoPoints(polygon.getPolygons()[0].getRing(0).getPoints());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return geoPoints;
    }

    private static List<GeoPoint> convertPointsToGeoPoints(Point[] points){

        List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

        for (Point point : points) {
            GeoPoint geoPoint = new GeoPoint(point.getY(),point.getX(),point.getZ());
            Log.i(TAG,"read point: "+geoPoint.toString());
            geoPoints.add(geoPoint);
        }

        return geoPoints;
    }
}
