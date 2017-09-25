package com.zls.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import com.zls.model.PageBean;
import com.zls.model.Role;
import com.zls.util.StringUtil;

public class RoleDao {
		/**
		 * ͨ���û���roleId��ý�ɫ��
		 * @param con
		 * @param roleId
		 * @return
		 * @throws Exception
		 */
		public String getRoleNameById(Connection con,int roleId)throws Exception{
			String roleName=null;
			String sql = "select roleName from t_role where roleId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, roleId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				roleName=rs.getString("roleName");
			}
			return roleName;
		}
		
		/**
		 * ͨ���û���roleId���Ȩ��ids
		 * @param con
		 * @param roleId
		 * @return
		 * @throws Exception
		 */
		public String getAuthIdsById(Connection con,int roleId)throws Exception{
			String authIds=null;
			String sql = "select authIds from t_role where roleId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, roleId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				authIds=rs.getString("authIds");
			}
			return authIds;
		}
		
		/**
		 * ��ȡ��ɫ�б�
		 * @param con
		 * @return
		 * @throws Exception
		 */
		public ResultSet listRole(Connection con,PageBean pageBean,String roleName)throws Exception{
			StringBuffer sb = new StringBuffer("select * from t_role");
			if (StringUtil.isNotEmpty(roleName)) {
				sb.append(" where roleName='"+roleName+"'");
			}
			if (pageBean!=null) {
				sb.append(" limit "+pageBean.getStart()+","+pageBean.getRows()+"");
			}
			PreparedStatement statement = con.prepareStatement(sb.toString());
			return statement.executeQuery();
		}
		
		/**
		 * ��ȡ��ɫ����
		 * @param con
		 * @param pageBean
		 * @param roleName
		 * @return
		 * @throws Exception
		 */
		public int totalRole(Connection con,PageBean pageBean,String roleName)throws Exception{
			int total=0;
			StringBuffer sb = new StringBuffer("select count(*) from t_role");
			if (StringUtil.isNotEmpty(roleName)) {
				sb.append(" where roleName='"+roleName+"'");
			}
			PreparedStatement statement = con.prepareStatement(sb.toString());
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				total=rs.getInt(1);
			}
			return total;
		}
		
		/**
		 * ɾ����ɫ
		 * @param con
		 * @param delIds
		 * @return
		 * @throws Exception
		 */
		public int removeRole(Connection con,String delIds)throws Exception{
			String sql = "delete from t_role where roleId in ("+delIds+")";
			PreparedStatement statement = con.prepareStatement(sql);
			return statement.executeUpdate();
		} 
		
		/**
		 * ���ӽ�ɫ
		 * @param con
		 * @param user
		 * @return
		 * @throws Exception
		 */
		public int saveRole(Connection con,Role role)throws Exception{
			String sql = "insert into t_role values(null,?,'',?)";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, role.getRoleName());
			statement.setString(2, role.getRoleDescription());
			return statement.executeUpdate();
		}
		
		/**
		 * ���½�ɫ��Ϣ
		 * @param con
		 * @param user
		 * @return
		 * @throws Exception
		 */
		public int updateRole(Connection con,Role role)throws Exception{
			String sql = "update t_role set roleDescription=? where roleId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, role.getRoleDescription());
			statement.setInt(2, role.getRoleId());
			return statement.executeUpdate();
		}
		
		/**
		 * ���½�ɫȨ����Ϣ
		 * @param con
		 * @param user
		 * @return
		 * @throws Exception
		 */
		public int updateRoleAuthIds(Connection con,Role role)throws Exception{
			String sql = "update t_role set authIds=? where roleId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, role.getAuthIds());
			statement.setInt(2, role.getRoleId());
			return statement.executeUpdate();
		}
		
		/**
		 * �жϽ�ɫ���Ƿ����
		 * @param con
		 * @param user
		 * @return
		 * @throws Exception
		 */
		public boolean existRoleWithRoleName(Connection con,String roleName)throws Exception{
			String sql = "select * from t_role where roleName=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, roleName);
			return statement.executeQuery().next();
		}
}
