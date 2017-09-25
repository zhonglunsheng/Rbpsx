<%@page import="com.zls.model.User"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" type="text/css" href="jquery-easyui-1.3.3/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="jquery-easyui-1.3.3/themes/icon.css">
<script type="text/javascript" src="jquery-easyui-1.3.3/jquery.min.js"></script>
<script type="text/javascript" src="jquery-easyui-1.3.3/jquery.easyui.min.js"></script>
<script type="text/javascript" src="jquery-easyui-1.3.3/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript">
	<!--左边菜单栏操作!--> 
	$(function(){
		$("#tree").tree({
			lines:true,
			url:'auth?action=menu&parentId=-1',
			onLoadSuccess:function(){
				$("#tree").tree('expandAll');
			},
			onClick:function(node){
				if(node.attributes.authPath){
					openTab(node);
				}else if(node.id==16){
					logOut();
				}else if(node.id==15){
					openPasswordModifyDialog();
				}
			}
		});
		
		<!--退出系统!--> 
		function logOut(){
			$.messager.confirm('系统提示','你确定要退出系统吗?',function(r){
				if (r) {
					window.location.href='user?action=logOut';
				}
			});
		}
		
		function openPasswordModifyDialog(){
			url="user?action=modifyPassword";
			$("#dlg").dialog("open").dialog("setTitle","修改密码");
		}
		
		<!--打开标签!--> 
		function openTab(node){
			if ($("#tabs").tabs("exists",node.text)) {
				$("#tabs").tabs("select",node.text);
			}else{
				var content="<iframe frameborder=0 scrolling='auto' style='width:100%;height:100%' src="+node.attributes.authPath+"></iframe>";
				$("#tabs").tabs("add",{
					title:node.text,
					iconCls:node.iconCls,
					closable:true,
					content:content
				});
			}
		}
	});
	
	
	
	function modifyPassword(){
		$("#fm").form("submit",{
			url:url,
			onSubmit:function(){
				var oldPassword=$("#oldPassword").val();
				var newPassword=$("#newPassword").val();
				var newPassword2=$("#newPassword2").val();
				if(!$(this).form("validate")){
					return false;
				}
				if(oldPassword!='${user.password}'){
					$.messager.alert('系统提示','用户名密码输入错误！');
					return false;
				}
				if(newPassword!=newPassword2){
					$.messager.alert('系统提示','确认密码输入错误！');
					return false;
				}
				return true;
			},
			success:function(result){
				/* var result = eval('('+result+')');
				if(result.success){
					$.messager.alert('系统提示','密码修改成功，下一次登录生效！');
					closePasswordModifyDialog();
				}else{
					$.messager.alert('系统提示',result.errorMsg);
					return;
				}  */
				$.messager.alert('系统提示','密码修改成功，下一次登录生效！');
				closePasswordModifyDialog();
			}
		});
	}
	
	function closePasswordModifyDialog(){
		$("#dlg").dialog("close");
		$("#oldPassword").val("");
		$("#newPassword").val("");
		$("#newPassword2").val("");
	}
	
</script>
<%
	if(session.getAttribute("user")==null){
		response.sendRedirect("login.jsp");
		return;
	}
%>
<title>系统主界面</title>
</head>
<body class="easyui-layout">
<div region="north" style="height:68px">
	<div style="padding:0px;margin: 0px;background-color: #EOECFF;">
		<table>
			<tr>
				<td><img src="images/mainlogo.png"/></td>
				<td valign="bottom">欢迎：${user.userName }&nbsp;&nbsp;[${user.roleName }]</td>
			</tr>
		</table>
	</div>
</div>
<div region="center">
	<div class="easyui-tabs" fit="true" border="false" id="tabs">
		<div title="首页" data-options="iconCls:'icon-home'">
			<div align="center" style="padding-top: 100px;">
				<font color="red" size="10">欢迎使用</font>
			</div>
		</div>
	</div>
</div>
<div region="west" style="width: 160px;padding:5px;" title="导航菜单" split="true">
<ul id="tree" class="easyui-tree"></ul>
</div>
<div region="south" style="height：25px;padding:5px;" align="center">
	版权所有 2017 lipop博客 <a href="http://blog.i20forever.cn" target="_blank">blog.i20forever.cn</a>
</div>
<div id="dlg" class="easyui-dialog" style="width: 400px;height: 220px;padding: 10px 20px" 
 closed="true" buttons="#dlg-buttons" data-options="iconCls:'icon-modifyPassword'">
 <form id="fm" method="post">
 	<table cellspacing="4px;">
 		<tr>
 			<td>用户名：</td>
 			<td><input type="hidden" name="userId" id="userId" value="${user.userId }"><input type="text" name="userName" id="userName" readonly="readonly" value="${user.userName }" style="width: 200px;" /></td>
 		</tr>
 		<tr>
 			<td>原密码：</td>
 			<td><input type="password" class="easyui-validatebox" name="oldPassword" id="oldPassword" style="width: 200px;" required="true" /></td>
 		</tr>
 		<tr>
 			<td>新密码：</td>
 			<td><input type="password" class="easyui-validatebox" name="newPassword" id="newPassword" style="width: 200px;" required="true"  /></td>
 		</tr>
 		<tr>
 			<td>确认新密码：</td>
 			<td><input type="password" class="easyui-validatebox" name="newPassword2" id="newPassword2" style="width: 200px;" required="true" /></td>
 		</tr>
 	</table>
 </form>
</div>
<div id="dlg-buttons">
	<a href="javascript:modifyPassword()" class="easyui-linkbutton" iconCls="icon-ok">保存</a>
	<a href="javascript:closePasswordModifyDialog()" class="easyui-linkbutton" iconCls="icon-cancel">关闭</a>
</div>
</body>
</html>