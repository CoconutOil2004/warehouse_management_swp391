<%@tag description="Layout" pageEncoding="UTF-8"%> 
<%@tag import="java.lang.String"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%> 
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%> 
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%> 

<%@attribute name="title" required="true" type="String"%> 
<%@attribute name="actions" fragment="true" required="false" %>

<html lang="vi">
  <head>
    <%@include file="/WEB-INF/views/layout/head.jspf" %>
  </head>

  <body id="page-top">
    <div id="wrapper">
      <%@include file="/WEB-INF/views/layout/sidebar.jspf" %>

      <div id="content-wrapper" class="d-flex flex-column">
        <div id="content">
          <%@include file="/WEB-INF/views/layout/header.jspf" %>

          <div class="container-fluid">
            <div
              class="d-sm-flex align-items-center justify-content-between mb-4"
            >
              <h1 class="h3 mb-0 text-gray-800">${title}</h1>
              <jsp:invoke fragment="actions" />
            </div>

            <jsp:doBody />
          </div>
        </div>

        <%@include file="/WEB-INF/views/layout/footer.jspf" %>
      </div>
    </div>

    <%@include file="/WEB-INF/views/layout/others.jspf" %>
    <%@include file="/WEB-INF/views/layout/script.jspf" %>
    <%@include file="/WEB-INF/views/layout/toast.jspf" %>
  </body>
</html>
