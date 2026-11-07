package com.lims.dao;

import com.lims.util.DBContext;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class ManagerInventoryDAO {
    private static final Logger LOG = Logger.getLogger(ManagerInventoryDAO.class.getName());

    public boolean isManager(int userId) {
        String sql = "SELECT COUNT(*) FROM USERS u JOIN USER_ROLES ur ON ur.UserID=u.UserID JOIN ROLES r ON r.RoleID=ur.RoleID WHERE u.UserID=? AND u.Status='ACTIVE' AND LOWER(r.RoleName)='manager'";
        try (Connection c=DBContext.getConnection(); PreparedStatement p=c.prepareStatement(sql)) { p.setInt(1,userId); try(ResultSet r=p.executeQuery()){return r.next()&&r.getInt(1)>0;} }
        catch(SQLException e){LOG.log(Level.SEVERE,"manager role",e);return false;}
    }

    public Map<String,Object> dashboard() {
        Map<String,Object> m=new LinkedHashMap<>();
        String sql="SELECT (SELECT COUNT(*) FROM INVENTORY) TotalUnits,"+
          "(SELECT COUNT(*) FROM INVENTORY WHERE Status='IN_STOCK') AvailableUnits,"+
          "(SELECT COUNT(*) FROM INVENTORY WHERE Status='RESERVED') ReservedUnits,"+
          "(SELECT COUNT(*) FROM INVENTORY WHERE Status='DAMAGED') DamagedUnits,"+
          "(SELECT COUNT(*) FROM INVENTORY WHERE Status='EXPORTED') ExportedUnits,"+
          "(SELECT COUNT(DISTINCT ProductID) FROM INVENTORY WHERE Status<>'EXPORTED') ProductCount,"+
          "(SELECT COUNT(*) FROM WAREHOUSE_LOCATIONS) LocationCount,"+
          "(SELECT COUNT(*) FROM IMPORT_RECEIPTS WHERE Status='PENDING_MANAGER') PendingImports,"+
          "(SELECT COUNT(*) FROM EXPORT_RECEIPTS WHERE Status='PENDING_MANAGER') PendingExports,"+
          "(SELECT COUNT(*) FROM INVENTORY_CHECKS WHERE Status='PENDING_MANAGER') PendingStocktakes,"+
          "(SELECT COUNT(*) FROM IMPORT_RECEIPT_DETAILS d JOIN IMPORT_RECEIPTS r ON r.ImportReceiptID=d.ImportReceiptID WHERE r.Status='COMPLETED' AND r.ApprovedDate>=DATEFROMPARTS(YEAR(GETDATE()),MONTH(GETDATE()),1)) ImportedThisMonth,"+
          "(SELECT COUNT(*) FROM EXPORT_RECEIPT_DETAILS d JOIN EXPORT_RECEIPTS r ON r.ExportReceiptID=d.ExportReceiptID WHERE r.Status='COMPLETED' AND r.ApprovedDate>=DATEFROMPARTS(YEAR(GETDATE()),MONTH(GETDATE()),1)) ExportedThisMonth";
        try(Connection c=DBContext.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) { if(r.next()){ResultSetMetaData md=r.getMetaData();for(int i=1;i<=md.getColumnCount();i++)m.put(md.getColumnLabel(i),r.getInt(i));} }
        catch(SQLException e){LOG.log(Level.SEVERE,"manager dashboard metrics",e);}
        return m;
    }

    public List<Map<String,Object>> productStock() {
        String sql="SELECT p.ProductID,p.ProductCode,p.ProductName,b.BrandName,l.LineName,COUNT(i.InventoryID) TotalUnits,"+
          "SUM(CASE WHEN i.Status='IN_STOCK' THEN 1 ELSE 0 END) AvailableUnits,"+
          "SUM(CASE WHEN i.Status='RESERVED' THEN 1 ELSE 0 END) ReservedUnits,"+
          "SUM(CASE WHEN i.Status='DAMAGED' THEN 1 ELSE 0 END) DamagedUnits,"+
          "SUM(CASE WHEN i.Status='EXPORTED' THEN 1 ELSE 0 END) ExportedUnits " +
          "FROM PRODUCTS p JOIN PRODUCT_LINES l ON l.ProductLineID=p.ProductLineID JOIN BRANDS b ON b.BrandID=l.BrandID LEFT JOIN INVENTORY i ON i.ProductID=p.ProductID " +
          "GROUP BY p.ProductID,p.ProductCode,p.ProductName,b.BrandName,l.LineName ORDER BY p.ProductCode";
        return query(sql,null,null,null);
    }

    public List<Map<String,Object>> locationStock() {
        String sql="SELECT Location,COUNT(*) TotalUnits,SUM(CASE WHEN Status='IN_STOCK' THEN 1 ELSE 0 END) AvailableUnits,SUM(CASE WHEN Status='RESERVED' THEN 1 ELSE 0 END) ReservedUnits,SUM(CASE WHEN Status='DAMAGED' THEN 1 ELSE 0 END) DamagedUnits FROM INVENTORY WHERE Status<>'EXPORTED' GROUP BY Location ORDER BY Location";
        return query(sql,null,null,null);
    }

    public List<Map<String,Object>> inventory(String search,String location,String status) {
        String sql="SELECT i.InventoryID,p.ProductCode,p.ProductName,b.BrandName,l.LineName,i.SerialNumber,i.Location,i.Status,i.UpdatedAt FROM INVENTORY i JOIN PRODUCTS p ON p.ProductID=i.ProductID JOIN PRODUCT_LINES l ON l.ProductLineID=p.ProductLineID JOIN BRANDS b ON b.BrandID=l.BrandID WHERE (?='' OR i.SerialNumber LIKE ? OR p.ProductCode LIKE ? OR p.ProductName LIKE ?) AND (?='' OR i.Location=?) AND (?='ALL' OR i.Status=?) ORDER BY i.Location,p.ProductCode,i.SerialNumber";
        String q=search==null?"":search.trim(),loc=location==null?"":location.trim(),st=status==null?"ALL":status.trim().toUpperCase();
        return query(sql,"%"+q+"%",loc,st);
    }

    public List<String> locations(){List<String> out=new ArrayList<>();String sync="INSERT INTO WAREHOUSE_LOCATIONS(LocationName) SELECT DISTINCT i.Location FROM INVENTORY i WHERE NULLIF(LTRIM(RTRIM(i.Location)),'') IS NOT NULL AND NOT EXISTS(SELECT 1 FROM WAREHOUSE_LOCATIONS l WHERE l.LocationName=i.Location)";try(Connection c=DBContext.getConnection();PreparedStatement p=c.prepareStatement(sync)){p.executeUpdate();}catch(SQLException e){LOG.log(Level.SEVERE,"sync warehouse locations",e);}try(Connection c=DBContext.getConnection();PreparedStatement p=c.prepareStatement("SELECT LocationName FROM WAREHOUSE_LOCATIONS ORDER BY LocationName");ResultSet r=p.executeQuery()){while(r.next())out.add(r.getString(1));}catch(SQLException e){LOG.log(Level.SEVERE,"warehouse locations",e);}return out;}

    public List<Map<String,Object>> managedLocations(){
        String sql="SELECT l.LocationID,l.LocationName,l.CreatedAt,COUNT(i.InventoryID) UnitCount,SUM(CASE WHEN i.Status='IN_STOCK' THEN 1 ELSE 0 END) AvailableUnits,SUM(CASE WHEN i.Status='RESERVED' THEN 1 ELSE 0 END) ReservedUnits,SUM(CASE WHEN i.Status='DAMAGED' THEN 1 ELSE 0 END) DamagedUnits FROM WAREHOUSE_LOCATIONS l LEFT JOIN INVENTORY i ON i.Location=l.LocationName AND i.Status<>'EXPORTED' GROUP BY l.LocationID,l.LocationName,l.CreatedAt ORDER BY l.LocationName";
        return query(sql,null,null,null);
    }

    public String locationName(int id){try(Connection c=DBContext.getConnection();PreparedStatement p=c.prepareStatement("SELECT LocationName FROM WAREHOUSE_LOCATIONS WHERE LocationID=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):null;}}catch(SQLException e){LOG.log(Level.SEVERE,"warehouse location lookup",e);return null;}}

    public boolean saveLocation(int id,String name){
        try(Connection c=DBContext.getConnection()){
            c.setAutoCommit(false);
            if(id==0){try(PreparedStatement p=c.prepareStatement("INSERT INTO WAREHOUSE_LOCATIONS(LocationName) VALUES(?)")){p.setString(1,name);p.executeUpdate();}}
            else{
                String old;
                try(PreparedStatement p=c.prepareStatement("SELECT LocationName FROM WAREHOUSE_LOCATIONS WITH(UPDLOCK,HOLDLOCK) WHERE LocationID=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next()){c.rollback();return false;}old=r.getString(1);}}
                try(PreparedStatement p=c.prepareStatement("UPDATE WAREHOUSE_LOCATIONS SET LocationName=? WHERE LocationID=?")){p.setString(1,name);p.setInt(2,id);if(p.executeUpdate()!=1){c.rollback();return false;}}
                if(!old.equals(name))try(PreparedStatement p=c.prepareStatement("UPDATE INVENTORY SET Location=?,UpdatedAt=GETDATE() WHERE Location=? AND Status<>'EXPORTED'")){p.setString(1,name);p.setString(2,old);p.executeUpdate();}
            }
            c.commit();return true;
        }catch(SQLException e){LOG.log(Level.WARNING,"save warehouse location",e);return false;}
    }

    /** Returns DELETED, NOT_FOUND, or IN_USE. */
    public String deleteLocation(int id){
        try(Connection c=DBContext.getConnection()){
            c.setAutoCommit(false);String name;
            try(PreparedStatement p=c.prepareStatement("SELECT LocationName FROM WAREHOUSE_LOCATIONS WITH(UPDLOCK,HOLDLOCK) WHERE LocationID=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next()){c.rollback();return "NOT_FOUND";}name=r.getString(1);}}
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM INVENTORY WHERE Location=? AND Status<>'EXPORTED'")){p.setString(1,name);try(ResultSet r=p.executeQuery()){if(r.next()&&r.getInt(1)>0){c.rollback();return "IN_USE";}}}
            try(PreparedStatement p=c.prepareStatement("DELETE FROM WAREHOUSE_LOCATIONS WHERE LocationID=?")){p.setInt(1,id);if(p.executeUpdate()!=1){c.rollback();return "NOT_FOUND";}}
            c.commit();return "DELETED";
        }catch(SQLException e){LOG.log(Level.WARNING,"delete warehouse location",e);return "IN_USE";}
    }

    public boolean updateLocation(int inventoryId,String location){
        String sql="UPDATE INVENTORY SET Location=?,UpdatedAt=GETDATE() WHERE InventoryID=? AND Status<>'EXPORTED' AND EXISTS(SELECT 1 FROM WAREHOUSE_LOCATIONS WHERE LocationName=?)";
        try(Connection c=DBContext.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,location);p.setInt(2,inventoryId);p.setString(3,location);return p.executeUpdate()==1;}catch(SQLException e){LOG.log(Level.SEVERE,"update inventory location",e);return false;}
    }

    private List<Map<String,Object>> query(String sql,String search,String location,String status){
        List<Map<String,Object>> out=new ArrayList<>();
        try(Connection c=DBContext.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            if(search!=null){p.setString(1,search.isEmpty()?"":search);p.setString(2,search);p.setString(3,search);p.setString(4,search);p.setString(5,location);p.setString(6,location);p.setString(7,status);p.setString(8,status);}
            try(ResultSet r=p.executeQuery()){ResultSetMetaData md=r.getMetaData();while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=md.getColumnCount();i++)row.put(md.getColumnLabel(i),r.getObject(i));out.add(row);}}
        }catch(SQLException e){LOG.log(Level.SEVERE,"inventory query",e);}return out;
    }
}
