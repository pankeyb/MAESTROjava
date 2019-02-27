/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import java.sql.SQLException;

/**
 *
 * @author sdelucero
 */
public interface EditImageContent {
    public byte[] getEditImageContent(String uuid)  throws SQLException ;
    public void editChanged();
}
