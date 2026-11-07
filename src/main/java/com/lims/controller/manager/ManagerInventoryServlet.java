package com.lims.controller.manager;

import com.lims.controller.BaseServlet;

import com.lims.dao.ManagerInventoryDAO;
import com.lims.model.User;
import com.lims.service.AuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@WebServlet(name="ManagerInventoryServlet",urlPatterns={"/app/manager/dashboard","/app/manager/inventory"})
public class ManagerInventoryServlet extends BaseServlet {
    private final ManagerInventoryDAO dao=new ManagerInventoryDAO();
    private final AuditLogService audit=new AuditLogService();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=getCurrentUser(req);if(user==null||!dao.isManager(user.getUserId())){resp.sendError(403,"Manager role required");return;}
        req.setAttribute("user",user);req.setAttribute("notice",req.getParameter("notice"));req.setAttribute("error",req.getParameter("error"));
        if(req.getServletPath().endsWith("/dashboard")){
            dao.locations();req.setAttribute("activeManagerMenu","dashboard");req.setAttribute("stats",dao.dashboard());req.setAttribute("productStock",dao.productStock());req.setAttribute("locationStock",dao.locationStock());forward(req,resp,"manager/dashboard.jsp");
        }else{
            String q=clean(req.getParameter("q")),location=clean(req.getParameter("location")),status=clean(req.getParameter("status"));if(status.isEmpty())status="ALL";
            req.setAttribute("activeManagerMenu","inventory");req.setAttribute("search",q);req.setAttribute("selectedLocation",location);req.setAttribute("statusFilter",status);req.setAttribute("locations",dao.locations());req.setAttribute("managedLocations",dao.managedLocations());List<Map<String,Object>> rows=dao.inventory(q,location,status);req.setAttribute("inventory",rows);req.setAttribute("productStock",dao.productStock());req.setAttribute("locationStock",dao.locationStock());if("csv".equalsIgnoreCase(req.getParameter("format"))){writeCsv(resp,rows);return;}forward(req,resp,"manager/inventory.jsp");
        }
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        req.setCharacterEncoding("UTF-8");User user=getCurrentUser(req);if(user==null||!dao.isManager(user.getUserId())){resp.sendError(403,"Manager role required");return;}
        String action=clean(req.getParameter("action"));
        if("saveLocation".equals(action)){
            String name=clean(req.getParameter("locationName"));int id=parseId(req.getParameter("locationId"));if(id<0||name.isEmpty()||name.length()>50){resp.sendRedirect(url(req,"error","Tên vị trí bắt buộc và tối đa 50 ký tự.","","","ALL"));return;}
            String old=id==0?null:dao.locationName(id);if(id>0&&old==null){resp.sendRedirect(url(req,"error","Không tìm thấy vị trí cần sửa.","","","ALL"));return;}
            if(!dao.saveLocation(id,name)){resp.sendRedirect(url(req,"error","Không lưu được vị trí. Tên này có thể đã tồn tại.","","","ALL"));return;}
            audit.logAction(user.getUserId(),user.getUsername(),id==0?"CREATE":"UPDATE","WAREHOUSE_LOCATIONS",id==0?name:String.valueOf(id),old==null?null:Map.of("locationName",old),Map.of("locationName",name),getClientIP(req),req.getHeader("User-Agent"));
            resp.sendRedirect(url(req,"notice",id==0?"Đã thêm vị trí kho.":"Đã sửa vị trí kho và cập nhật vị trí các máy đang lưu tại đó.","","","ALL"));return;
        }
        if("deleteLocation".equals(action)){
            int id=parseId(req.getParameter("locationId"));String old=id<1?null:dao.locationName(id);if(old==null){resp.sendRedirect(url(req,"error","Không tìm thấy vị trí cần xóa.","","","ALL"));return;}
            String result=dao.deleteLocation(id);if(!"DELETED".equals(result)){resp.sendRedirect(url(req,"error","Chỉ xóa được vị trí đang trống.","","","ALL"));return;}
            audit.logAction(user.getUserId(),user.getUsername(),"DELETE","WAREHOUSE_LOCATIONS",String.valueOf(id),Map.of("locationName",old),null,getClientIP(req),req.getHeader("User-Agent"));resp.sendRedirect(url(req,"notice","Đã xóa vị trí kho.","","","ALL"));return;
        }
        String idText=req.getParameter("inventoryId"),location=clean(req.getParameter("location")),q=clean(req.getParameter("q")),filterLocation=clean(req.getParameter("filterLocation")),status=clean(req.getParameter("status"));
        String message;try{int id=Integer.parseInt(idText);if(id<1||location.isEmpty()||location.length()>50)throw new IllegalArgumentException();
            if(!dao.updateLocation(id,location))throw new IllegalStateException("Không thể đổi vị trí. Máy có thể đã xuất kho.");
            audit.logAction(user.getUserId(),user.getUsername(),"UPDATE_LOCATION","INVENTORY",idText,null,java.util.Map.of("location",location),getClientIP(req),req.getHeader("User-Agent"));message="Đã cập nhật vị trí lưu kho.";
        }catch(IllegalArgumentException ex){message="Mã máy hoặc vị trí lưu kho không hợp lệ (tối đa 50 ký tự).";resp.sendRedirect(url(req,"error",message,q,filterLocation,status));return;}
        catch(IllegalStateException ex){resp.sendRedirect(url(req,"error",ex.getMessage(),q,filterLocation,status));return;}
        resp.sendRedirect(url(req,"notice",message,q,filterLocation,status));
    }

    private String url(HttpServletRequest req,String key,String value,String q,String location,String status){return req.getContextPath()+"/app/manager/inventory?"+key+"="+enc(value)+"&q="+enc(q)+"&location="+enc(location)+"&status="+enc(status);}
    private void writeCsv(HttpServletResponse resp,List<Map<String,Object>> rows)throws IOException{
        resp.setCharacterEncoding("UTF-8");resp.setContentType("text/csv;charset=UTF-8");resp.setHeader("Content-Disposition","attachment; filename=lims-inventory-report.csv");
        PrintWriter out=resp.getWriter();out.write('\ufeff');out.println("ProductCode,ProductName,Brand,ProductLine,SerialNumber,Location,Status,UpdatedAt");
        for(Map<String,Object> row:rows){out.println(csv(row.get("ProductCode"))+","+csv(row.get("ProductName"))+","+csv(row.get("BrandName"))+","+csv(row.get("LineName"))+","+csv(row.get("SerialNumber"))+","+csv(row.get("Location"))+","+csv(row.get("Status"))+","+csv(row.get("UpdatedAt")));}out.flush();
    }
    private String csv(Object value){String s=value==null?"":String.valueOf(value);return "\""+s.replace("\"","\"\"")+"\"";}
    private String enc(String x){return URLEncoder.encode(x==null?"":x,StandardCharsets.UTF_8);}
    private int parseId(String x){try{return Integer.parseInt(x);}catch(Exception e){return -1;}}
    private String clean(String x){return x==null?"":x.trim();}
}
