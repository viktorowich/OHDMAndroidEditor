package android.ohdm.de.editor.geometry.PolyObject;

import org.apache.commons.lang3.NotImplementedException;
import org.osmdroid.views.MapView;

public class PolyObjectFactory {

    public static PolyObject buildObject(PolyObjectType type,MapView view){

        PolyObject polyObject = null;

        switch (type){
            case POLYGON:
                polyObject = new PolyGon(view);
                break;
            case POLYLINE:
                polyObject = new PolyLine(view);
                break;
            case POINT:
                polyObject = new PolyPoint(view.getContext());
                break;
            default:
                throw new NotImplementedException(type+" not implemented");
        }
        return polyObject;
    }
}