package android.ohdm.de.editor.Geometry.ExtendedOverlay;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.views.MapView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExtendedPolygonOverlay extends Polygon implements ExtendedOverlayClickPublisher, Serializable {

    private static final long serialVersionUID = 0L;

    private boolean editable = false;
    private boolean clickable = false;
    private boolean clicked = false;

    private List<ExtendedOverlayClickListener> listeners = new ArrayList<ExtendedOverlayClickListener>();

    public ExtendedPolygonOverlay(Context context){
        super(context);
        Log.i("ExtendedPolygonOverlay", "created!");
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){

        boolean tapped = super.contains(event);

        if(tapped) {
            if (isClickable()) {
                notifyListeners();
            }
        }

        return tapped;
    }

    private void notifyListeners(){
        for(ExtendedOverlayClickListener listener : listeners){
            listener.onClick();
        }
    }

    @Override
    public void subscribe(ExtendedOverlayClickListener listener) {
        listeners.add(listener);
    }

    @Override
    public void remove(ExtendedOverlayClickListener listener) {
        listeners.remove(listener);
    }
}