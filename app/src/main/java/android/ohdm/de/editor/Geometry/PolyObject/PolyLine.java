package android.ohdm.de.editor.Geometry.PolyObject;

import android.content.Context;
import android.graphics.Color;
import android.ohdm.de.editor.Geometry.PolyObject.ExtendedOverlay.ExtendedPolylineOverlay;
import android.ohdm.de.editor.Geometry.TagDates;

import org.osmdroid.bonuspack.overlays.OverlayWithIW;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PolyLine extends PolyObject implements Serializable {

    private static final long serialVersionUID = 4209360273818925922L;

    private transient ExtendedPolylineOverlay polyline;

    private List<GeoPoint> points = new ArrayList<GeoPoint>();

    public PolyLine(Context context){
        super(PolyObjectType.POLYLINE);
        this.internId = UUID.randomUUID();
        create(context);
    }

    @Override
    protected void create(Context context) {
        polyline = new ExtendedPolylineOverlay(context);

        polyline.subscribe(this);

        polyline.setColor(Color.BLUE);
        polyline.setWidth(4);
        polyline.setPoints(points);
    }

    @Override
    public OverlayWithIW getOverlay() {
        return polyline;
    }

    @Override
    public void setPoints(List<GeoPoint> points) {
        this.points = points;
        polyline.setPoints(points);
    }

    public List<GeoPoint> getPoints(){
        return this.points;
    }

    @Override
    public void removeLastPoint() {
        if(!points.isEmpty()){
            points.remove(points.size()-1);
        }
        polyline.setPoints(this.points);
    }

    @Override
    public void addPoint(GeoPoint geoPoint) {
        points.add(geoPoint);
        polyline.setPoints(this.points);
    }

    @Override
    public void setClickable(boolean clickable) {
        polyline.setClickable(clickable);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;

        if(this.selected) {
            polyline.setColor(Color.RED);
        }else {
            polyline.setColor(Color.BLUE);
        }
    }

    @Override
    public void setEditing(boolean editing) {
        this.editing = editing;

        if(this.editing) {
            polyline.setColor(Color.GREEN);
        }else {
            polyline.setColor(Color.BLUE);
        }
    }

    @Override
    public void onClick(Object clickObject) {
        for(PolyObjectClickListener listener : listeners){
            listener.onClick(this);
        }
    }

    @Override
    public void removeSelectedCornerPoint() {
        //TODO
    }
}
