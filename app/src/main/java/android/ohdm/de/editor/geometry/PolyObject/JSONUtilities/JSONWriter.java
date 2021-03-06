package android.ohdm.de.editor.geometry.PolyObject.JSONUtilities;

import android.ohdm.de.editor.geometry.PolyObject.PolyObject;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

import java.util.HashMap;
import java.util.List;

public class JSONWriter {

    private static final String TAG = "JSONWriter";

    private static final String TAGS = "tags";
    private static final String GEOMETRICOBJECT = "geometricObjects";
    private static final String TAGDATES = "tagDates";
    private static final String EXTERNAL_SOURCE_ID_STR = "externalSourceId";
    private static final int EXTERNAL_SOURCE_ID = 2;
    private static final String VALID = "valid";
    private static final String VALID_FROM = "0001-01-01";
    private static final String VALID_TO = "3000-01-01";
    private static final int SRID = 4326;

    private static final String MULTILINESTRING = "multilinestring";
    private static final String MULTIPOINT = "multipoint";
    private static final String MULTIPOLYGON = "multipolygon";

    private JSONWriter(){

    }

    public static JSONObject createJSONObjectFromPolyObject(PolyObject polyObject) {

        JSONObject jsonObject = new JSONObject();

        JSONObject geometry = createGeometryObject(polyObject);
        JSONObject tags = createTagDatesObject(polyObject);
        JSONObject tagDates = new JSONObject();
        JSONObject validDates = createValidDatesObject();

        JSONArray geometryArray = new JSONArray();
        JSONArray tagDatesArray = new JSONArray();

        try {
            tagDates.put(TAGS, tags);
            tagDates.put(VALID,validDates);
            tagDatesArray.put(0, tagDates);

            geometry.put(VALID,validDates);
            geometryArray.put(0, geometry);

            jsonObject.put(TAGDATES, tagDatesArray);
            jsonObject.put(GEOMETRICOBJECT, geometryArray);
            jsonObject.put(EXTERNAL_SOURCE_ID_STR, EXTERNAL_SOURCE_ID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, jsonObject.toString());

        return jsonObject;
    }

    private static JSONObject createValidDatesObject(){
        JSONObject validDatesObject = new JSONObject();

        try {
            validDatesObject.put("since",VALID_FROM);
            validDatesObject.put("until",VALID_TO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return validDatesObject;
    }

    private static JSONObject createGeometryObject(PolyObject polyObject) {

        JSONObject jsonObject = new JSONObject();
        String geometry;

        try {

            switch (polyObject.getType()) {
                case POINT:
                    geometry = convertPolyPointToMultiPoint(polyObject).toString();
                    jsonObject.put(MULTIPOINT, geometry);
                    break;
                case POLYGON:
                    geometry = convertPolyGonToMultiPolygon(polyObject).toString();
                    jsonObject.put(MULTIPOLYGON, geometry);
                    break;
                case POLYLINE:
                    geometry = convertPolyLineToMultiLineString(polyObject).toString();
                    jsonObject.put(MULTILINESTRING, geometry);
                    break;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private static MultiPolygon convertPolyGonToMultiPolygon(PolyObject polyObject) {

        Point[] points = convertGeoPointListToPoints(polyObject.getPoints());

        for(int i=0; i<points.length-1; i++){
            Log.d(TAG,"points "+i+": "+points[i]);
        }


        //TODO: remove hack below
        Point[] points2 = new Point[points.length+1];

        for(int i=0; i<points.length; i++){
            points2[i] = points[i];
        }
        points2[points2.length-1] = points[0];

        for(int i=0; i<points2.length; i++){
            Log.d(TAG,"points2 "+i+": "+points2[i]);
        }

        LinearRing linearRing = new LinearRing(points2);
        Polygon polygon = new Polygon(new LinearRing[]{linearRing});
        MultiPolygon multiPolygon = new MultiPolygon(new Polygon[]{polygon});
        multiPolygon.setSrid(SRID);

        return multiPolygon;
    }

    private static MultiLineString convertPolyLineToMultiLineString(PolyObject polyObject) {
        Point[] points = convertGeoPointListToPoints(polyObject.getPoints());
        LineString lineString = new LineString(points);
        MultiLineString multiLineString = new MultiLineString(new LineString[]{lineString});
        multiLineString.setSrid(SRID);

        return multiLineString;
    }

    private static MultiPoint convertPolyPointToMultiPoint(PolyObject polyObject) {
        Point[] points = convertGeoPointListToPoints(polyObject.getPoints());
        //Only the "last" point in a PolyPoint is the actual Point. the other points are there for the undo function
        MultiPoint multiPoint = new MultiPoint(new Point[]{points[points.length - 1]});
        multiPoint.setSrid(SRID);

        return multiPoint;
    }

    private static Point[] convertGeoPointListToPoints(List<GeoPoint> geoPoints) {

        Point[] points = new Point[geoPoints.size()];

        for (int i = 0; i < geoPoints.size(); i++) {
            GeoPoint geoPoint = geoPoints.get(i);
            Point point = new Point(geoPoint.getLongitude(), geoPoint.getLatitude(), geoPoint.getAltitude());
            points[i] = point;
        }

        return points;
    }

    private static JSONObject createTagDatesObject(PolyObject polyObject) {

        HashMap<String, String> tagDates = polyObject.getTags();
        JSONObject tagDatesObject = new JSONObject();

        try {
            for (String key : tagDates.keySet()) {
                tagDatesObject.put(key, tagDates.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tagDatesObject;
    }
}
