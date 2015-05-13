<%-- 
    Document   : index
    Created on : May 13, 2015, 11:11:09 AM
    Author     : Eduardo Moranchel <emoranchel@asmatron.org>
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Sensors</title>
  </head>
  <body>
    <h1>Sensors</h1>
    <ul>
      <c:forEach items="${sensors.sensors}" var="sensor">
      <li>${sensor}</li>
      </c:forEach>
    </ul>
  </body>
</html>
