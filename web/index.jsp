<%-- 
////////////////////////////////////////////////////////////////////////////////////////////////////
// index.jsp
// Copyright (c) 2016 Pdfix. All Rights Reserved.
////////////////////////////////////////////////////////////////////////////////////////////////////

--%>


<%@page import="java.util.*"%>
<%@page import="utils.PdfixUtils"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  String id = "";
  Map<String, String[]> params = request.getParameterMap();
  if (params.containsKey("id")) {
    id = params.get("id")[0];
  }

  if (id.length() > 0) {
    PdfixUtils.PrepareFileForView(request, id);
  }

  String webRoot = request.getRequestURL().toString();
  if (webRoot.length() > 0) {
    int pos = webRoot.lastIndexOf("/");
    webRoot = webRoot.substring(0, pos + 1);
  }
%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>PDFix webview</title>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
    <link rel="apple-touch-icon" sizes="57x57" href="img/apple-touch-icon-57x57.png">
    <link rel="apple-touch-icon" sizes="60x60" href="img/apple-touch-icon-60x60.png">
    <link rel="apple-touch-icon" sizes="72x72" href="img/apple-touch-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="76x76" href="img/apple-touch-icon-76x76.png">
    <link rel="apple-touch-icon" sizes="114x114" href="img/apple-touch-icon-114x114.png">
    <link rel="apple-touch-icon" sizes="120x120" href="img/apple-touch-icon-120x120.png">
    <link rel="apple-touch-icon" sizes="144x144" href="img/apple-touch-icon-144x144.png">
    <link rel="apple-touch-icon" sizes="152x152" href="img/apple-touch-icon-152x152.png">
    <link rel="apple-touch-icon" sizes="180x180" href="img/apple-touch-icon-180x180.png">
    <link rel="icon" type="image/png" href="img/favicon-16x16.png" sizes="16x16">
    <link rel="icon" type="image/png" href="img/favicon-32x32.png" sizes="32x32">
    <link rel="icon" type="image/png" href="img/favicon-96x96.png" sizes="96x96">
    <link rel="icon" type="image/png" href="img/android-chrome-192x192.png" sizes="192x192">
    <meta name="msapplication-square70x70logo" content="img/smalltile.png" />
    <meta name="msapplication-square150x150logo" content="img/mediumtile.png" />
    <meta name="msapplication-wide310x150logo" content="img/widetile.png" />
    <meta name="msapplication-square310x310logo" content="img/largetile.png" />
    <meta name="description" content="PDFix is changing the way the world interacts with PDFs with our API. Our focus is on logical content extraction which brings semantics to the PDF content and makes it truly responsive.">
    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <!-- Bootstrap -->
    <!-- Bootstrap customization-->
    <link href="bootstrap_custom.css" rel="stylesheet">


    <script src="jquery.min.js"></script>

    <script type="text/javascript">
      function createCookie(name, value, days) {
        if (days) {
          var date = new Date();
          date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
          var expires = "; expires=" + date.toGMTString();
        } else
          var expires = "";
        document.cookie = name + "=" + value + expires + "; path=/";
      }

      function readCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
          var c = ca[i];
          while (c.charAt(0) == ' ')
            c = c.substring(1, c.length);
          if (c.indexOf(nameEQ) == 0)
            return c.substring(nameEQ.length, c.length);
        }
        return null;
      }

      function eraseCookie(name) {
        createCookie(name, "", -1);
      }
      function toggleView(tablet) {
        document.getElementById("device").setAttribute("class", tablet ? "tablet" : "phone");
        document.getElementById("pdfview").setAttribute("class", tablet ? "tabletview" : "phoneview");
        createCookie("device", tablet ? "tablet" : "phone", 1);
        return false;
      }

      function pdfixSubmitForm() {
        var doc = document.getElementById('pdfview').contentWindow;
        doc.submitForm('../../../SubmitForm', false, true, null, false);
        document.getElementById('tmpview').src = 'GetFile';
        return false;
      }
    </script>
  </head>
  <body>
    <%@include file="includes/navigation.jsp" %>
    <%@include file="includes/container.jsp" %>
    <%@include file="includes/footer.html" %>
    
    <script type="text/javascript">
      var device = readCookie("device");
      if (device)
        toggleView(device == "tablet");
    </script>
    

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>

    <!-- analytics -->
    <script>
      (function (i, s, o, g, r, a, m) {
        i['GoogleAnalyticsObject'] = r;
        i[r] = i[r] || function () {
          (i[r].q = i[r].q || []).push(arguments)
        }, i[r].l = 1 * new Date();
        a = s.createElement(o),
                m = s.getElementsByTagName(o)[0];
        a.async = 1;
        a.src = g;
        m.parentNode.insertBefore(a, m)
      })(window, document, 'script', 'https://www.google-analytics.com/analytics.js', 'ga');

      ga('create', 'UA-77127933-2', 'auto');
      ga('send', 'pageview');

    </script>
  </body>
</html>
