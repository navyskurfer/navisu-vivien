/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.server.impl;

import java.util.Date;

/**
 *
 * @author Serge
 */
public interface Reader {

    public void read();

    public String getData();

	public Date getAtlDate();
	public Date getMedDate();

}
