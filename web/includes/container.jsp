<div class="container">
  <div class="centered"> 
    <div class="phone" id="device">
      <%
        String url = "webview.html";
        if (id.length() != 0) {
          url = "files/" + id + "/fixed/index.html";
        }
      %>
      <iframe class="phoneview" id="pdfview" name="doc" src="<%=url%>"></iframe>
      <iframe id="tmpview" name="tmpview"> </iframe>
    </div>    
  </div>  
</div> 
