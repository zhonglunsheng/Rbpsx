package com.zls.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.zls.model.PageBean;
import com.zls.model.User;
import com.zls.util.StringUtil;

public class UserDao {
	/**
	 * 用户登录
	 * @param con
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public User login(Connection con ,User user)throws Exception{
		User resultUser = null;
		String sql = "select * from t_user where userName=? and password=?";
		PreparedStatement statement = con.prepareStatement(sql);
		statement.setString(1, user.getUserName());
		statement.setString(2, user.getPassword());
		ResultSet rs = statement.executeQuery();
		while(rs.next()){
			resultUser = new User();
			resultUser.setUserId(rs.getInt("userId"));
			resultUser.setPassword(rs.getString("password"));
			resultUser.setUserName(rs.getString("userName"));
			resultUser.setUserType(rs.getInt("userType"));
			resultUser.setRoleId(rs.getInt("roleId"));
			resultUser.setUserDescription(rs.getString("userDescription"));
		}
		return resultUser;
	}
	
	/**
	 * 更新用户密码
	 * @param con
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public int updatePassword(Connection con,User user)throws Exception{
		String sql = "update t_user set password=? where userId=?";
		PreparedStatement statement = con.prepareStatement(sql);
		statement.setString(1, user.getPassword());
		statement.setInt(2, user.getUserId());
		return statement.executeUpdate();
	}
	
	/**
	 * 获取用户列表
	 * @param con
	 * @return
	 * @throws Exception
	 */
	public ResultSet listUser(Connection con,PageBean pageBean,User user)throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append("select * from t_user,t_role where t_user.roleId=t_role.roleId");
		if (StringUtil.isNotEmpty(user.getUserName())) {
			sb.append(" and t_user.userName like '%"+user.getUserName()+"%'");
		}
		if (user.getRoleId()!=0) {
			sb.append(" and t_user.roleId="+user.getRoleId()+"");
		}
		if (pageBean!=null) {
			sb.append(" limit ?,?");
		}
		PreparedStatement statement = con.prepareStatement(sb.toString());
		statement.setInt(1, pageBean.getStart());
		statement.setInt(2, pageBean.getRows());
		return statement.executeQuery();
	}
	
	/**
	 * 统计用户个数
	 * @param con
	 * @return
	 * @throws Exception
	 */
	public int totalUser(Connection con,User user)throws Exception{
		int total = 0;
		StringBuffer sb = new StringBuffer("select count(*) from t_user");
		if (StringUtil.isNotEmpty(user.getUserName())) {
			sb.append(" and userName like '%"+user.getUserName()+"%'");
		}
		if (user.getRoleId()!=0) {
			sb.append(" and roleId="+user.getRoleId()+"");
		}
		PreparedStatement statement = con.prepareStatement(sb.toString().replaceFirst("and", "where"));
		ResultSet rs = statement.executeQuery();
		while(rs.next()){
			total = rs.getInt(1);
		}
		return total;
	}
	
	/**
	 * 更新用户信息
	 * @param con
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public int updateUser(Connection con,User user)throws Exception{
		String sql = "update t_user set password=?,roleId=?,userDescription=? where userId=?";
		PreparedStatement statement = con.prepareStatement(sql);
		statement.setString(1, user.getPassword());
		statement.setInt(2, user.getRoleId());
		statement.setString(3, user.getUserDescription());
		statement.setInt(4, user.getUserId());
		return statement.executeUpdate();
	}
	
	/**
	 * 增加用户
	 * @param con
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public int saveUser(Connection con,User user)throws Exception{
		String sql = "insert into t_user values(null,?,?,2,?,?)";
		PreparedStatement statement = con.prepareStatement(sql);
		statement.setString(1, user.getUserName());
		statement.setString(2, user.getPassword());
		statement.setInt(3, user.getRoleId());
		statement.setString(4, user.getUserDescription());
		return statement.executeUpdate();
	}
	
	/**
	 * 判断用户名是否存在
	 * @param con
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public boolean existUserWithUserName(Connection con,String userName)throws Exception{
		String sql = "select * from t_user where userName=?";
		PreparedStatement statement = con.prepareStatement(sql);
		statement.setString(1, userName);
		return statement.executeQuery().next();
	}
	
	/**
	 * 判断角色下是否有用户
	 * @param con
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	public boolean existUserWithRoleId(Connection con,String roleId)throws Exception{
		String sql = "select * from t_user where roleId=?";
		PreparedStatement statement = con.prepareStatement(sql);
		statement.setString(1, roleId);
		return statement.executeQuery().next();
	}
	
	/**
	 * 删除用户
	 * @param con
	 * @param delIds
	 * @return
	 * @throws Exception
	 */
	public int removeUser(Connection con,String delIds)throws Exception{
		String sql = "delete from t_user where userId in ("+delIds+")";
		PreparedStatement statement = con.prepareStatement(sql);
		return statement.executeUpdate();
	} 
}
