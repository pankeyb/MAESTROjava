/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.util;

import java.util.UUID;

/**
 *
 * @author sdelucero
 */
public class PhotoAttribute {

    private String name;
    private String value;
    private String uuid;

    public PhotoAttribute() {
        uuid = UUID.randomUUID().toString();
    }

    public PhotoAttribute(String name, String value) {
        this.name = name;
        this.value = value;
        uuid = UUID.randomUUID().toString();
    }
    public PhotoAttribute(String cn, String name, String value) {
        this.name = name;
        this.value = value;
        uuid = cn;
    }

    public PhotoAttribute(PhotoAttribute pa) {
        this.name = pa.getName();
        this.value = pa.getValue();
        this.uuid = pa.getUuid();
    }

    public String getUuid() {
        return uuid;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
