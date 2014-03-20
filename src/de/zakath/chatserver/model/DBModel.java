/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.zakath.chatserver.model;

import java.sql.*;
import java.util.logging.*;

/**
 *
 * @author cw
 */
class DBModel
{

    private static Connection _conn;
    private static Statement s;

    static
    {
        try
        {
            _conn = DriverManager.getConnection("jdbc:mysql://localhost/chatserverdb?user=root");
            s = _conn.createStatement();
        } catch (SQLException ex)
        {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }

    public static ResultSet executeQuery(String query)
    {
        try
        {
            return s.executeQuery(query);
        } catch (SQLException ex)
        {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    
    public static int executeUpdate(String query)
    {
        try
        {
            return s.executeUpdate(query);
        } catch (SQLException ex)
        {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public static boolean execute(String query)
    {
        try
        {
            return s.execute(query);
        } catch (SQLException ex)
        {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public static Statement getStatement()
    {
        try
        {
            return _conn.createStatement();
        } catch (SQLException ex)
        {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static PreparedStatement getPreparedStatement(String query)
    {
        try
        {
            return _conn.prepareStatement(query);
        } catch (SQLException ex)
        {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
