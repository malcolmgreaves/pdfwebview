<%-- 
////////////////////////////////////////////////////////////////////////////////////////////////////
// toolbar.jsp
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////

--%>

<%
    String id = (String)session.getAttribute("id");
//    response.sendRedirect("./");
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script> </script>
        <link rel="stylesheet" type="text/css" href="style.css">     
    </head>
    <body height="100%">

      <div class="toolbar">
      <a href="./index.jsp" target="_parent" class="button">back</a>
      <a href="files/<%=id%>/fixed/index.html" target="doc" class="button">fixed</a>
      <a href="files/<%=id%>/responsive/index.html" target="doc" class="button">responsive</a>
      <a href="files/<%=id%>/<%=id%>.docx" target="doc" class="button">docx</a>
      <a href="SignFile?id=<%=id%>" target="sign" class="button">sign</a>
      <a onclick="parent.document.getElementById('doc-frame').contentWindow.submitForm('../../../SubmitForm', false, true, null, false);" 
         class="button">submit form data</a>
      </div>
    </body>

</html>