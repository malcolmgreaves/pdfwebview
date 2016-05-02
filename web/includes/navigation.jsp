<nav class="navbar navbar-default">
  <div class="container-fluid">
    <div class="navbar-header">
      <a class="navbar-brand" href="./">
        <img src="img/Pdfix_logo_small.png"/>
      </a>
    </div>
    <ul class="nav navbar-nav ">
      <li class="dropdown">
        <form method="POST" id="fileUpload" action="UploadFile" enctype="multipart/form-data">
          <a href="#" class="btn btn-primary dropdown-toggle"  data-toggle="dropdown">Open PDF... 
            <span class="caret"></span></a>
          <ul class="dropdown-menu">
            <li class="dropdown-header">Articles and Magazines</li>
            <li><a href="?id=1462133368975">Planting_more_trees.pdf</a></li>
            <li><a href="?id=1462134103193">Trainingplan_Longdistance.pdf</a></li>
            <li><a href="?id=1462133136420">Easy_Vegan_Recipes.pdf</a></li>
            <li><a href="?id=1462134695742">Communique.pdf</a></li>
            <li><a href="?id=1462132939180">Africas_Growth_Prospects.pdf</a></li>
            <li><a href="?id=1462132729722">Case_Histories.pdf</a></li>
            <li><a href="?id=1462133043589">Climate_Change.pdf</a></li>
            <li><a href="?id=1462133081384">Doing_Business.pdf</a></li>
            <li><a href="?id=1462133230180">Global_Ecotourism.pdf</a></li>
            <li><a href="?id=1462133276515">Men.pdf</a></li>
            <li><a href="?id=1462133315094">National_travel.pdf</a></li>
            <li><a href="?id=1462134161909">Trekking.pdf</a></li>
            <li class="divider"></li>
            <li class="dropdown-header">PDF Forms</li>
            <li><a href="?id=1462206097160">PdfFormExample.pdf</a></li>
            <li><a href="?id=1462197866540">Request_to_Fill_Vacancy_Form.pdf</a></li>
            <li><a href="?id=1462197945958">International_Visitor_Request_Form.pdf</a></li>
            <li><a href="?id=1462197369145">Academic_Records_Request.pdf</a></li>
            <li><a href="?id=1462197203362">SocialSecurity.pdf</a></li>
            <li><a href="?id=1462198240780">Recreation_Center_Request.pdf</a></li>
            <li><a href="?id=1462197416518">W-8BEN.pdf</a></li>
            <li class="dropdown-header">Upload your file (up to 10MB and 10 pages)</li>
            <li><a href="" onclick="$('#exampleInputFile').trigger('click'); return false;"><b>Upload...</b></a></li>
          </ul>

          <input type="file" name="file_uploaded" id="exampleInputFile" style="display: none"
                 onchange="$('#fileUpload').submit();"/>
          <button type="submit" id="submitBtn" action="Upload" name="upload" class="btn btn-default form-control" style="display: none">Submit</button>
        </form>     
      </li> 
      <li class="dropdown pdfix-dropdown">
        <button href="#" class="btn btn-default dropdown-toggle"  data-toggle="dropdown">Device... 
          <span class="caret"></span></button>
        <ul class="dropdown-menu">
          <li class="dropdown-header">Select preferred device</li>
          <li><a href="#" onclick="return toggleView(false);">Phone</a></li>
          <li><a href="#" onclick="return toggleView(true);">Tablet</a></li>
          <li><a href="#" onclick="window.open(document.getElementById('pdfview').contentWindow.location.href, '_blank', 'height=600,location=0,menubar=0,scrollbars=1,status=0,toolbar=0,width=600');return false;">Desktop</a></li>
        </ul>
      </li> 
      <li>
        <div class="btn-group pdfix-toolbar">
          <a type="button" class="btn btn-default" onclick="return toggleView(false);">Phone</a>
          <a type="button" class="btn btn-default" onclick="return toggleView(true);">Tablet</a>
          <a type="button" class="btn btn-default" onclick="window.open(document.getElementById('pdfview').contentWindow.location.href, '_blank', 'height=600,location=0,menubar=0,scrollbars=1,status=0,toolbar=0,width=600');return false;">Desktop</a>
        </div>
      </li>
      <%if (id.length() > 0) {%>
      <li class="dropdown pdfix-dropdown">
        <button href="#" class="btn btn-default dropdown-toggle"  data-toggle="dropdown">Layout... 
          <span class="caret"></span></button>
        <ul class="dropdown-menu">
          <li class="dropdown-header">Select document view</li>
          <li><a target="doc" href="<%=webRoot%>files/<%=id%>/fixed/index.html">Origial Layout</a></li>
          <li><a target="doc" href="<%=webRoot%>files/<%=id%>/responsive/index.html">Responsive Layout</a></li>
        </ul>
      </li> 
      <li>
        <div class="btn-group pdfix-toolbar">
          <a type="button" class="btn btn-default" target="doc" href="<%=webRoot%>files/<%=id%>/fixed/index.html">Original</a>
          <a type="button" class="btn btn-default" target="doc" href="<%=webRoot%>files/<%=id%>/responsive/index.html">Responsive</a>
        </div>
      </li>
      <li class="dropdown pdfix-dropdown">
        <button href="#" class="btn btn-default dropdown-toggle"  data-toggle="dropdown">Actions... 
          <span class="caret"></span></button>
        <ul class="dropdown-menu">
          <li class="dropdown-header">Perform an operation on PDF...</li>
          <li><a target="tmpview" href="<%=webRoot%>GetDocx?id=<%=id%>">Convert to Microsoft Word</a></li>
          <li><a target="tmpview" href="<%=webRoot%>SignFile?id=<%=id%>">Apply Watermark and Digital Signature</a></li>
          <li><a href="" onclick="return pdfixSubmitForm();">Submit Form Data</a></li>
        </ul>
      </li> 
      <li>
        <div class="btn-group pdfix-toolbar">
          <a type="button" class="btn btn-default" target="tmpview" href="<%=webRoot%>GetDocx?id=<%=id%>">Convert to DOCX</a>
          <a type="button" class="btn btn-default" target="tmpview" href="<%=webRoot%>SignFile?id=<%=id%>">Sign / Watermark</a>
          <a type="button" class="btn btn-default" href="" onclick="return pdfixSubmitForm();">Submit Form Data</a>
        </div>
      </li>
            <%}%>
      <li class="dropdown ">
        <button href="#" class="btn btn-default dropdown-toggle"  data-toggle="dropdown">About... 
          <span class="caret"></span></button>
        <ul class="dropdown-menu">
          <li class="dropdown-header">PDF Webview powered by PDFix API</li>
          <li><a href="http://PDFix.net" target="_blank">http://PDFix.net</a></li>
        </ul>
      </li> 
    </ul>

  </div>
</nav>