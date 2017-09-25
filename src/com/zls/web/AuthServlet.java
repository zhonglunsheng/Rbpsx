package com.zls.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.zls.dao.AuthDao;
import com.zls.dao.RoleDao;
import com.zls.model.Auth;
import com.zls.model.User;
import com.zls.util.DbUtil;
import com.zls.util.ResponseUtil;
import com.zls.util.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class AuthServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DbUtil dbUtil = new DbUtil();
	private AuthDao authDao = new AuthDao();
	private RoleDao roleDao = new RoleDao();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO 自动生成的方法存根
		this.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String action = request.getParameter("action");
		if (action.equals("menu")) {
			this.menuAction(request, response);
		}else if (action.equals("authMenu")) {
			this.authMenuAction(request,response);
		}else if (action.equals("authTreeGridMenu")) {
			this.authTreeGridMenAction(request,response);
		}else if (action.equals("save")) {
			this.saveAction(request,response);
		}else if (action.equals("delete")) {
			this.deleteAction(request,response);
		}	
	}
	
	/**
	 * 获取左边菜单
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void menuAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String parentId = request.getParameter("parentId");
		Connection con = null;
		try {
			con=dbUtil.getCon();
			HttpSession session = request.getSession();
			User user = (User)session.getAttribute("user");
			String authIds = roleDao.getAuthIdsById(con, user.getRoleId());
			JSONArray jsonArray = authDao.getAuthsByParentId(con, parentId,authIds);
			ResponseUtil.write(response, jsonArray);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			dbUtil.getClose(con);
		}
		
	}
	
	/**
	 * 获取权限菜单
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void authMenuAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String parentId = request.getParameter("parentId");
		String roleId = request.getParameter("roleId");
		Connection con = null;
		try {
			con=dbUtil.getCon();
			String authIds = roleDao.getAuthIdsById(con, Integer.parseInt(roleId));
			JSONArray jsonArray = authDao.getCheckedAuthsByParentId(con, parentId, authIds);
			ResponseUtil.write(response, jsonArray);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
	}
	
	/**
	 * 获取权限列表信息
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void authTreeGridMenAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String parentId = request.getParameter("parentId");
		Connection con = null;
		try {
			con=dbUtil.getCon();
			JSONArray jsonArray = authDao.getTreeGridAuthsByParentId(con, parentId);
			ResponseUtil.write(response, jsonArray);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
		
	}
	
	/**
	 * 添加修改节点
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void saveAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String authId = request.getParameter("authId");
		String authName = request.getParameter("authName");
		String parentId = request.getParameter("parentId");
		String iconCls = request.getParameter("iconCls");
		String authPath = request.getParameter("authPath");
		String authDescription=request.getParameter("authDescription");
		Auth auth = new Auth(authName, authPath, authDescription, iconCls);
		if (StringUtil.isNotEmpty(authId)) {
			auth.setAuthId(Integer.parseInt(authId));
		}else {
			auth.setParentId(Integer.parseInt(parentId));
		}
		Connection con = null;
		boolean isLeaf=false;
		try {
			JSONObject jsonObject = new JSONObject();
			con = dbUtil.getCon();
			int saveNums =0;
			if (StringUtil.isNotEmpty(authId)) {
				saveNums=authDao.updateAuth(con, auth);
			}else{
				isLeaf=authDao.isLeaf(con, parentId);
				if (isLeaf) {
					con.setAutoCommit(false);
					authDao.updateStateByAuthId(con, "close", parentId);
					saveNums = authDao.saveAuth(con, auth);
					con.commit();
				}else{
					saveNums=authDao.saveAuth(con, auth);
				}
			}
			if (saveNums>0) {
				jsonObject.put("success", true);
			}else{
				jsonObject.put("success", false);
				jsonObject.put("errorMsg", "保存失败");
			}
			ResponseUtil.write(response, jsonObject);
			
		} catch (Exception e) {
			if (isLeaf) {
				try {
					con.rollback();
				} catch (SQLException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
			}
		}finally{
			dbUtil.getClose(con);
		}
		
	}
	/**
	 * 删除权限，3种情况
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void deleteAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String authId = request.getParameter("authId");
		Connection con = null;
		int sonNums = 0;
		try {
			con=dbUtil.getCon();
			JSONObject jsonObject = new JSONObject();
			if (!authDao.isLeaf(con, authId)) {
				jsonObject.put("success", false);
				jsonObject.put("errorMsg", "该菜单栏下有子菜单不能删除");
			}else{
				int delNums=0;
				sonNums = authDao.totalLeaf(con, authId);
				if (sonNums==1) {
					con.setAutoCommit(false);
					int parentId = authDao.getParentIdByAuthId(con, authId);
					authDao.updateStateByAuthId(con, "open", parentId+"");
					delNums = authDao.deleteAuth(con, authId);
					con.commit();
				}else{
					delNums=authDao.deleteAuth(con, authId);
				}
				if (delNums>0) {
					jsonObject.put("success", true);
				}else{
					jsonObject.put("success", false);
					jsonObject.put("errorMsg", "删除失败");
				}
			}
			ResponseUtil.write(response, jsonObject);
		} catch (Exception e) {
			try {
				if (sonNums==1) {
					con.rollback();
				}
			} catch (SQLException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}finally{
			dbUtil.getClose(con);
		}
		
	}
	

}
