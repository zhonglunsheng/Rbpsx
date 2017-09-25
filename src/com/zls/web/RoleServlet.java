package com.zls.web;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zls.dao.RoleDao;
import com.zls.dao.UserDao;
import com.zls.model.PageBean;
import com.zls.model.Role;
import com.zls.util.DbUtil;
import com.zls.util.JsonUtil;
import com.zls.util.ResponseUtil;
import com.zls.util.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RoleServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RoleDao roleDao = new RoleDao();
	private UserDao userDao = new UserDao();
	private DbUtil dbUtil = new DbUtil();
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO 自动生成的方法存根
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String action = request.getParameter("action");
		if (action.equals("comBoList")) {
			comBoList(request,response);
		}else if (action.equals("list")) {
			listRole(request, response);
		}else if (action.equals("delete")) {
			deleteRole(request, response);
		}else if (action.equals("save")) {
			save(request,response);
		}else if (action.equals("auth")) {
			auth(request,response);
		}
	}
	
	/**
	 * 获取角色列表
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void comBoList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray array = new JSONArray();
		JSONObject object = new JSONObject();
		object.put("roleId", "");
		object.put("roleName", "--请选择--");
		array.add(object);
		Connection con = null;
		try {
			con=dbUtil.getCon();
			array.addAll(JsonUtil.formatRsToJsonArray(roleDao.listRole(con,null,null)));
			ResponseUtil.write(response, array);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
		
	}
	
	/**
	 * 获取角色列表信息
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void listRole(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String page = request.getParameter("page");
		String rows = request.getParameter("rows");
		String s_roleName = request.getParameter("s_roleName");
		PageBean pageBean = new PageBean(Integer.parseInt(page), Integer.parseInt(rows));
		JSONArray array = new JSONArray();
		JSONObject object = new JSONObject();
		Connection con = null;
		try {
			con = dbUtil.getCon();
			array = JsonUtil.formatRsToJsonArray(roleDao.listRole(con,pageBean,s_roleName));
			object.put("rows", array);
			object.put("total", roleDao.totalRole(con, pageBean, s_roleName));
			ResponseUtil.write(response, object);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
	}
	
	/**
	 * 删除用户
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void deleteRole(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String delIds = request.getParameter("delIds");
		Connection con = null;
		try {
			con=dbUtil.getCon();
			JSONObject result = new JSONObject();
			String[] str = delIds.split(",");
			for (int i = 0; i < str.length; i++) {
				if (userDao.existUserWithRoleId(con, str[i])) {
					result.put("errorIndex", i);
					result.put("errorMsg","角色下面有用户，不能删除！");
					ResponseUtil.write(response, result);
					return;
				}
			}
			int delNums = roleDao.removeRole(con, delIds);
			if (delNums>0) {
				result.put("success", true);
				result.put("delNums",delNums);
			}else{
				result.put("success", false);
				result.put("errorMsg", "删除失败");
			}
			ResponseUtil.write(response, result);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
	}
	
	/**
	 * 增加和修改角色
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String roleId = request.getParameter("roleId");
		String roleName = request.getParameter("roleName");
		String roleDescription = request.getParameter("roleDescription");
		Connection con = null;
		JSONObject object = new JSONObject();
		Role role = new Role(roleName, roleDescription);
		try {
			con=dbUtil.getCon();
			int successNum=0;
			if (StringUtil.isNotEmpty(roleId)) {
				role.setRoleId(Integer.parseInt(roleId));
				 successNum= roleDao.updateRole(con, role);
			}else if (roleDao.existRoleWithRoleName(con, roleName)){
				successNum=-1;
			}else {
				successNum=roleDao.saveRole(con, role);
			}
			
			if (successNum==-1) {
				object.put("success", false);
				object.put("errorMsg", "角色名已存在");
			}else if (successNum==1){
				object.put("success", true);
			}else {
				object.put("success", false);
				object.put("errorMsg", "保存失败");
			}
			ResponseUtil.write(response, object);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
	}
	
	protected void auth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String roleId = request.getParameter("roleId");
		String authIds = request.getParameter("authIds");
		Connection con = null;
		Role role = new Role(Integer.parseInt(roleId),authIds);
		try {
			JSONObject result = new JSONObject();
			con=dbUtil.getCon();
			int successNum = roleDao.updateRoleAuthIds(con, role);
			if (successNum>0) {
				result.put("success", true);
			}else{
				result.put("success", false);
				result.put("errorMsg", "角色权限设置失败");
			}
			ResponseUtil.write(response, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
