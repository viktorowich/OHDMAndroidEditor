package android.ohdm.de.editor.Geometry.PolyObject;

import android.content.Context;
import android.graphics.Color;
import android.ohdm.de.editor.Geometry.ExtendedOverlay.ExtendedPolygonOverlay;
import android.util.Log;

import org.osmdroid.bonuspack.overlays.OverlayWithIW;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

public class ExPolyPoint extends PolyObject {

    private static final long serialVersionUID = 0L;

    private transient ExtendedPolygonOverlay polygon;
    private transient boolean selected = false;
    private transient boolean editing = false;
    private transient List<PolyObjectClickListener> listeners = new ArrayList<PolyObjectClickListener>();
    private List<GeoPoint> points = new ArrayList<GeoPoint>();

    ExPolyPoint(Context context){
        super(PolyObjectType.POINT);
        create(context);
    }

    @Override
    protected void create(Context context) {
        polygon = new ExtendedPolygonOverlay(context);
        polygon.subscribe(this);

        polygon.setFillColor(Color.BLUE);
        polygon.setStrokeWidth(4);
    }

    @Override
    public OverlayWithIW getOverlay() {
        return polygon;
    }

    @Override
    public void setPoints(List<GeoPoint> points) {
        this.points = points;

        if(this.points.size() >= 1) {
            int lastPoint = this.points.size()-1;
            int radius = 20;

            polygon.setPoints(Polygon.pointsAsCircle(this.points.get(lastPoint), radius));
        }
    }

    public List<GeoPoint> getPoints(){
        return this.points;
    }

    @Override
    public void removeLastPoint() {
        if(!points.isEmpty()){
            points.remove(points.size()-1);
        }

        setPoints(this.points);
    }

    @Override
    public void addPoint(GeoPoint geoPoint) {
        points.add(geoPoint);

        setPoints(this.points);
    }

    @Override
    public boolean isClickable() {
        return polygon.isClickable();
    }

    @Override
    public void setClickable(boolean clickable) {
        polygon.setClickable(clickable);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;

        if(selected){
            polygon.setFillColor(Color.RED);
        }else{
            polygon.setFillColor(Color.BLUE);
        }
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean isEditing() {
        return editing;
    }

    @Override
    public void setEditing(boolean editing) {
        this.editing = editing;

        if(editing){
            polygon.setFillColor(Color.GREEN);
        }else{
            polygon.setFillColor(Color.BLUE);
        }
    }

    @Override
    public void onClick() {
        for(PolyObjectClickListener listener : listeners){
            listener.onClick(this);
        }
    }

    @Override
    public void subscribe(PolyObjectClickListener listener) {
        listeners.add(listener);
    }

    @Override
    public void remove(PolyObjectClickListener listener) {
        listeners.remove(listener);
    }
}