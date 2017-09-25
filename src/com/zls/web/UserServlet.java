package com.zls.web;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.zls.dao.RoleDao;
import com.zls.dao.UserDao;
import com.zls.model.PageBean;
import com.zls.model.User;
import com.zls.util.DbUtil;
import com.zls.util.JsonUtil;
import com.zls.util.ResponseUtil;
import com.zls.util.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class UserServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DbUtil dbUtil = new DbUtil();
	private UserDao userDao = new UserDao();
	private RoleDao roleDao = new RoleDao();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO 自动生成的方法存根
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String action = request.getParameter("action");
		if (action.equals("login")) {
			login(request, response);
		}else if (action.equals("logOut")) {
			logOut(request, response);
		}else if (action.equals("modifyPassword")) {
			modifyPassword(request, response);
		}else if (action.equals("list")) {
			list(request,response);
		}else if (action.equals("save")) {
			save(request,response);
		}else if (action.equals("delete")) {
			deleteUser(request, response);
		}
	}
	/**
	 * 用户登陆
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		String imageCode = request.getParameter("imageCode");
		
		HttpSession session = request.getSession();
		
		
		Connection con = null;
		
		if (StringUtil.isEmpty(userName)) {
			request.setAttribute("error", "用户名不能为空");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return;
		}
		
		if (StringUtil.isEmpty(userName)) {
			request.setAttribute("error", "密码不能为空");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return;
		}
		
		if (StringUtil.isEmpty(imageCode)) {
			request.setAttribute("error", "验证码不能为空");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return;
		}
		
		if (!imageCode.equals((String)session.getAttribute("sRand"))) {
			request.setAttribute("error", "验证码错误");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return;
		}
		
		User user = new User(userName, password);
		try {
			con=dbUtil.getCon();
			User ResultUser = userDao.login(con, user);
			if (ResultUser==null) {
				request.setAttribute("error", "用户名或密码错误");
				request.setAttribute("userName", userName);
				request.setAttribute("password", password);
				request.getRequestDispatcher("/login.jsp").forward(request, response);
			}else{
				String roleName = roleDao.getRoleNameById(con, ResultUser.getRoleId());
				ResultUser.setRoleName(roleName);
				session.setAttribute("user", ResultUser);
				response.sendRedirect("main.jsp");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			dbUtil.getClose(con);
		}
	}
	
	/**
	 * 安全退出系统
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void logOut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().invalidate();
		response.sendRedirect("login.jsp");
	}
	
	/**
	 * 更新密码
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void modifyPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("userId");
		String newPassword = request.getParameter("newPassword");
		JSONObject object = new JSONObject();
		User user = new User();
		user.setUserId(Integer.parseInt(userId));
		user.setPassword(newPassword);
		Connection con = null;
		try {
			con = dbUtil.getCon();
			int result = userDao.updatePassword(con, user);
			if(result>0){
				object.put("success", true);
			}else{
				object.put("success", false);
				object.put("errorMsg", "修改密码失败！");
			}
			ResponseUtil.write(response, object);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
	}
	
	/**
	 * 获取用户列表信息
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray array = new JSONArray();
		JSONObject object = new JSONObject();
		String page = request.getParameter("page");
		String rows = request.getParameter("rows");
		String s_userName = request.getParameter("s_userName");
		String s_roleId = request.getParameter("s_roleId");
		
		User user = new User();
		if (StringUtil.isNotEmpty(s_userName)) {
			user.setUserName(s_userName);
		}
		if (StringUtil.isNotEmpty(s_roleId)) {
			user.setRoleId(Integer.parseInt(s_roleId));
		}
		PageBean pageBean = new PageBean(Integer.parseInt(page), Integer.parseInt(rows));
		Connection con = null;
		try {
			con=dbUtil.getCon();
			array = JsonUtil.formatRsToJsonArray(userDao.listUser(con,pageBean,user));
			int total = userDao.totalUser(con,user);
			object.put("rows", array);
			object.put("total", total);
			ResponseUtil.write(response, object);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
		
	}
	
	/**
	 * 增加和修改用户
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void save(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("userId");
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		String roleId = request.getParameter("roleId");
		String userDescription = request.getParameter("userDescription");
		Connection con = null;
		JSONObject object = new JSONObject();
		User user = new User(userName,password,Integer.parseInt(roleId), userDescription);
		try {
			con=dbUtil.getCon();
			int successNum=0;
			if (StringUtil.isNotEmpty(userId)) {
				user.setUserId(Integer.parseInt(userId));
				 successNum= userDao.updateUser(con, user);
			}else if (userDao.existUserWithUserName(con, userName)){
				successNum=-1;
			}else {
				successNum=userDao.saveUser(con, user);
			}
			
			if (successNum==-1) {
				object.put("success", false);
				object.put("errorMsg", "用户名已存在");
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
	
	/**
	 * 删除用户
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void deleteUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String delIds = request.getParameter("delIds");
		JSONObject object = new JSONObject();
		Connection con = null;
		try {
			con=dbUtil.getCon();
			int successNum = userDao.removeUser(con, delIds);
			if (successNum==0) {
				object.put("success", false);
				object.put("errorMsg","删除失败");
			}else{
				object.put("success", true);
				object.put("delNums", successNum);
			}
			ResponseUtil.write(response, object);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbUtil.getClose(con);
		}
	}
}
