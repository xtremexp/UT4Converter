package org.xtx.ut4converter.t3d;

import org.xtx.ut4converter.MapConverter;
import org.xtx.ut4converter.geom.Rotator;
import org.xtx.ut4converter.ucore.UPackageRessource;

import javax.vecmath.Vector3d;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class T3DSimpleProperty {

    private final String propertyName;

    /**
     * Default will be originalName
     * but we might need to have name different
     * than original
     */
    private String propertyNameConverted;

    /**
     * Used if we want to copy this property with another name.
     * Used on write
     */
    private String clonePropertyName;

    private final Object propertyClass;

    /**
     * If true then this property is scalable.
     */
    private boolean scalable;

    private Object propertyValue;

    private final boolean isList;
    /**
     *
     */
    private T3DRessource.Type ressourceType;


    /**
     * Register simple property from single float, string, ...
     * @param propertyName Property name
     * @param propertyClass Property class
     * @param defaultValue Default value
     */
    public T3DSimpleProperty(final String propertyName, final Object propertyClass, final Object defaultValue, boolean isList) {
        this.propertyName = propertyName;
        this.propertyNameConverted = this.propertyName;
        this.propertyClass = propertyClass;
        this.isList = isList;
    }

    /**
     * Register simple property ressource
     * @param propertyName Property name
     * @param ressourceType Ressource type
     */
    public T3DSimpleProperty(final String propertyName, final T3DRessource.Type ressourceType, boolean isList) {
        this.propertyName = propertyName;
        this.propertyNameConverted = this.propertyName;
        this.propertyClass = UPackageRessource.class;
        this.ressourceType = ressourceType;
        this.isList = isList;
    }

    public boolean readPropertyFromT3dLine(final String line, final MapConverter mapConverter){

        // property ever parsed before and not a list
        if(this.propertyValue != null && !this.isList){
            return false;
        }

        boolean hasLineProp;

        if(this.isList){
            if(this.propertyValue == null) {
                this.propertyValue = new LinkedList<>();
            }
            hasLineProp = line.toLowerCase().startsWith(this.propertyName.toLowerCase() + "(");
        } else {
            hasLineProp = line.toLowerCase().startsWith(this.propertyName.toLowerCase() + "=");
        }

        Object value = null;

        if(hasLineProp){
            if(propertyClass == String.class){
                value = T3DUtils.getString(line);
            }
            else if(propertyClass == Short.class){
                value = T3DUtils.getShort(line);
            }
            else if(propertyClass == Float.class){
                value = T3DUtils.getFloat(line);
            }
            else if(propertyClass == Boolean.class){
                value = T3DUtils.getBoolean(line);
            }
            else if(propertyClass == Integer.class){
                value = T3DUtils.getInteger(line);
            }
            else if(propertyClass == Double.class){
                value = T3DUtils.getDouble(line);
            }
            else if(propertyClass == Vector3d.class){
                value = T3DUtils.getVector3d(line, 0d);
            }
            else if(propertyClass == Rotator.class){
                value = T3DUtils.getVector3dRot(line);
            }
            else if(propertyClass == UPackageRessource.class){
                value = T3DUtils.getUPackageRessource(mapConverter, line, this.ressourceType);
            }
        }

        if(value != null) {
            if (this.propertyValue instanceof List) {
                final List<Object> theList = (List<Object>) this.propertyValue;
                theList.add(value);
            } else {
                this.propertyValue = value;
            }
        }


        return value != null;
    }

    void writeProperty(final StringBuilder sbf, final MapConverter mapConverter){

        if(this.propertyValue != null) {

            if(this.propertyValue instanceof List) {
                final List<Object> values = (List<Object>) this.propertyValue;

                int idx = 0;

                for(final Object value : values){
                    sbf.append("\t\t").append(propertyNameConverted).append("(").append(idx).append(")=");
                    writeValueProperty(sbf, mapConverter, value);
                    idx ++;
                }
            } else {
                sbf.append("\t\t").append(propertyNameConverted).append("=");
                writeValueProperty(sbf, mapConverter, this.propertyValue);

                if(clonePropertyName != null){
                    sbf.append("\t\t").append(clonePropertyName).append("=");
                    writeValueProperty(sbf, mapConverter, this.propertyValue.toString());
                }
            }
        }
    }

    private void writeValueProperty(StringBuilder sbf, MapConverter mapConverter, Object value) {
        if (value instanceof String) {
            sbf.append("\"").append(value).append("\"\n");
        } else if (value instanceof UPackageRessource) {

            final UPackageRessource packageRessource = (UPackageRessource) value;

            if(ressourceType == T3DRessource.Type.SOUND) {
                sbf.append("SoundCue'").append((packageRessource).getConvertedName(mapConverter)).append("'\n");
            }
            // MESHES will be converted to staticmeshes
            else if(ressourceType == T3DRessource.Type.STATICMESH || ressourceType == T3DRessource.Type.MESH) {
                sbf.append("StaticMesh'").append((packageRessource).getConvertedName(mapConverter)).append("'\n");
            } else if(ressourceType == T3DRessource.Type.TEXTURE) {
                sbf.append("Material'").append((packageRessource).getConvertedName(mapConverter)).append("'\n");
            }
        } else {
            sbf.append(value).append("\n");
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public T3DRessource.Type getRessourceType() {
        return ressourceType;
    }

    public void clonePropertyAs(String clonePropertyName) {
        this.clonePropertyName = clonePropertyName;
    }
}
