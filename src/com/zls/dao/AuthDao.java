package com.zls.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.zls.model.Auth;
import com.zls.util.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * 管理操作权限
 * @author zhonglunsheng
 *
 */
public class AuthDao {
	
	/**
	 * 获取节点信息
	 * @param con
	 * @param parentId
	 * @return
	 * @throws Exception
	 */
		public JSONArray getAuthByParentId(Connection con,String parentId,String authIds)throws Exception{
			JSONArray jsonArray = new JSONArray();
			String sql = "select * from t_auth where parentId=? and authId in("+authIds+")";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, parentId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				JSONObject object = new JSONObject();
				object.put("id", rs.getInt("authId"));
				object.put("text", rs.getString("authName"));
				object.put("state", rs.getString("state"));
				object.put("iconCls", rs.getString("iconCls"));
				JSONObject attributeObject = new JSONObject();
				attributeObject.put("authPath", rs.getString("authPath"));
				object.put("attributes", attributeObject);
				jsonArray.add(object);
			}
			return jsonArray;
		}
		
		/**
		 * 递归遍历子节点
		 * @param con
		 * @param parentId
		 * @return
		 * @throws Exception
		 */
		public JSONArray getAuthsByParentId(Connection con,String parentId,String authIds)throws Exception{
			JSONArray jsonArray = this.getAuthByParentId(con, parentId,authIds);
			for(int i=0;i<jsonArray.size();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if ("open".equals(jsonObject.getString("state"))) {
					continue;
				}else{
					jsonObject.put("children", getAuthsByParentId(con, jsonObject.getString("id"),authIds));
				}
			}
			return jsonArray;
		}
		/**
		 * 获取选中的权限
		 * @param con
		 * @param parentId
		 * @param authIds
		 * @return
		 * @throws Exception
		 */
		public JSONArray getCheckedAuthByParentId(Connection con,String parentId,String authIds)throws Exception{
			JSONArray jsonArray = new JSONArray();
			String sql = "select * from t_auth where parentId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, parentId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				int authId = rs.getInt("authId");
				jsonObject.put("id", authId);
				jsonObject.put("text", rs.getString("authName"));
				jsonObject.put("state", rs.getString("state"));
				jsonObject.put("iconCls", rs.getString("iconCls"));
				if (StringUtil.exisStrArr(authId+"", authIds.split(","))) {
					jsonObject.put("checked", true);
				}
				JSONObject attributeObject = new JSONObject();
				attributeObject.put("authPath", rs.getString("authPath"));
				jsonObject.put("attributes", attributeObject);
				jsonArray.add(jsonObject);
			}
			return jsonArray;
		}
		
		/**
		 * 递归实现遍历所有节点
		 * @param con
		 * @param parentId
		 * @param authIds
		 * @return
		 * @throws Exception
		 */
		public JSONArray getCheckedAuthsByParentId(Connection con,String parentId,String authIds)throws Exception{
			JSONArray jsonArray = this.getCheckedAuthByParentId(con, parentId, authIds);
			for(int i=0;i<jsonArray.size();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if ("open".equals(jsonObject.getString("state"))) {
					continue;
				}else{
					jsonObject.put("children", getCheckedAuthsByParentId(con, jsonObject.getString("id"), authIds));
				}
			}
			return jsonArray;
		}
		
		/**
		 * 菜单管理
		 * @param con
		 * @param parentId
		 * @return
		 * @throws Exception
		 */
		public JSONArray getTreeGridAuthByParentId(Connection con,String parentId)throws Exception{
			JSONArray jsonArray = new JSONArray();
			String sql = "select * from t_auth where parentId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, parentId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				JSONObject jsonObject = new JSONObject();
				int authId = rs.getInt("authId");
				jsonObject.put("id", authId);
				jsonObject.put("text", rs.getString("authName"));
				jsonObject.put("state", rs.getString("state"));
				jsonObject.put("iconCls", rs.getString("iconCls"));
				jsonObject.put("authPath", rs.getString("authPath"));
				jsonObject.put("authDescription", rs.getString("authDescription"));
				jsonArray.add(jsonObject);
			}
			return jsonArray;
		}
		
		/**
		 * 递归实现遍历所有节点
		 * @param con
		 * @param parentId
		 * @param authIds
		 * @return
		 * @throws Exception
		 */
		public JSONArray getTreeGridAuthsByParentId(Connection con,String parentId)throws Exception{
			JSONArray jsonArray = this.getTreeGridAuthByParentId(con, parentId);
			for(int i=0;i<jsonArray.size();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if ("open".equals(jsonObject.getString("state"))) {
					continue;
				}else{
					jsonObject.put("children", getTreeGridAuthsByParentId(con, jsonObject.getString("id")));
				}
			}
			return jsonArray;
		}
		
		/**
		 * 判断当前节点是不是子节点
		 * @param con
		 * @param authId
		 * @return
		 * @throws Exception
		 */
		public boolean isLeaf(Connection con,String authId)throws Exception{
			String sql = "select * from t_auth where authId=? and state='open'";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, authId);
			return statement.executeQuery().next();
		}
		
		/**
		 * 计算子节点个数
		 * @param con
		 * @param parentId
		 * @return
		 * @throws Exception
		 */
		public int totalLeaf(Connection con,String parentId)throws Exception{
			String sql = "select count(*) from t_auth where parentId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, parentId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}else{
				return 0;
			}
		}
		
		/**
		 * 增加权限
		 * @param con
		 * @param auth
		 * @return
		 * @throws Exception
		 */
		public int saveAuth(Connection con,Auth auth)throws Exception{
			String sql ="insert into t_auth values(null,?,?,?,?,'open',?)";
			PreparedStatement pstmt=con.prepareStatement(sql);
			pstmt.setString(1, auth.getAuthName());
			pstmt.setString(2, auth.getAuthPath());
			pstmt.setInt(3, auth.getParentId());
			pstmt.setString(4, auth.getAuthDescription());
			pstmt.setString(5, auth.getIconCls());
			return pstmt.executeUpdate();
		}
		
		/**
		 * 更改state状态
		 * @param con
		 * @param authId
		 * @throws Exception
		 */
		public void updateStateByAuthId(Connection con,String state,String parentId)throws Exception{
			String sql = "update t_auth set state=? where authId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, state);
			statement.setString(2, parentId);
			statement.executeUpdate();
		}
		
		/**
		 * 修改权限信息
		 * @param con
		 * @param auth
		 * @return
		 * @throws Exception
		 */
		public int updateAuth(Connection con,Auth auth)throws Exception{
			String sql ="update t_auth set authName=?,authPath=?,authDescription=?,iconCls=? where authId=?";
			PreparedStatement pstmt=con.prepareStatement(sql);
			pstmt.setString(1, auth.getAuthName());
			pstmt.setString(2, auth.getAuthPath());
			pstmt.setString(3, auth.getAuthDescription());
			pstmt.setString(4, auth.getIconCls());
			pstmt.setInt(5, auth.getAuthId());
			return pstmt.executeUpdate();
		}
		
		/**
		 * 获取父节点id
		 * @param con
		 * @param authId
		 * @return
		 * @throws Exception
		 */
		public int getParentIdByAuthId(Connection con,String authId)throws Exception{
			String sql = "select parentId from t_auth where authId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, authId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}else{
				return 0;
			}
		}
		
		/**
		 * 删除权限
		 * @param con
		 * @param authId
		 * @return
		 * @throws Exception
		 */
		public int deleteAuth(Connection con,String authId)throws Exception{
			String sql = "delete from t_auth where authId=?";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, authId);
			return statement.executeUpdate();
		} 
}
