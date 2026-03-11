<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%
    Integer currentPageObj = (Integer) request.getAttribute("page");
    Integer totalPagesObj  = (Integer) request.getAttribute("totalPages");
%>
<t:layout title="Sale Order List">
    <jsp:attribute name="actions">
        <div class="d-flex align-items-center">
            <form action="${pageContext.request.contextPath}/sales-orders" method="get" class="m-0 mr-2">
                <input type="hidden" name="action" value="create">
                <t:button type="submit" color="success">Create Sale Order</t:button>
                </form>
                <form action="${pageContext.request.contextPath}/sales-orders" method="get" class="m-0">
                <input type="hidden" name="action" value="import">
                <t:button type="submit" color="success">Import Sale Order</t:button>
                </form>
            </div>
    </jsp:attribute>

    <jsp:body>
        <c:if test="${param.msg == 'cannotdelete'}">
            <div class="alert alert-warning alert-dismissible fade show" role="alert">
                Cannot delete Sale Order with status CLOSED.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:set var="columns" value='${["STT", "SO Number", "Customer", "Request Ship Date", "Ship To Address", "Imported By", "Status", "Action"]}' />
        <t:table id="soTable" columns="${columns}">
            <jsp:attribute name="head">
                <form hx-get="${pageContext.request.contextPath}/sales-orders" hx-target="#wrapper" hx-select="#wrapper" hx-swap="outerHTML" hx-push-url="true" class="m-0">
                    <input type="hidden" name="action" value="list"/>
                    <!-- ROW 1: soNumber, customer, status, actions -->
                    <div class="form-row mb-2">
                        <div class="col-md-6">
                            <label class="mb-1 font-weight-bold">Search</label>
                            <input type="text" name="keyword" class="form-control" placeholder="SO number, customer..." value="${param.keyword}">
                        </div>
                        <div class="col-md-3">
                            <label class="mb-1 font-weight-bold">Status</label>
                            <select class="form-control" name="status">
                                <option value="">-- All --</option>
                                <option value="CREATED"  ${param.status == 'CREATED'  ? 'selected' : ''}>CREATED</option>
                                <option value="IMPORTED" ${param.status == 'IMPORTED' ? 'selected' : ''}>IMPORTED</option>
                                <option value="CANCELLED"${param.status == 'CANCELLED'? 'selected' : ''}>CANCELLED</option>
                                <option value="CLOSED"   ${param.status == 'CLOSED'   ? 'selected' : ''}>CLOSED</option>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <t:button type="submit" color="primary" cssClass="mr-2">Search</t:button>
                            <a href="${pageContext.request.contextPath}/sales-orders?action=list" class="btn btn-outline-secondary">Reset</a>
                        </div>
                    </div>
                    <!-- ROW 2: Date filters -->
                    <div class="form-row">
                        <div class="col-md-3">
                            <label class="mb-1 font-weight-bold">Ship date (from)</label>
                            <input type="date" class="form-control" name="fromDate" value="${param.fromDate}">
                        </div>
                        <div class="col-md-3">
                            <label class="mb-1 font-weight-bold">Ship date (to)</label>
                            <input type="date" class="form-control" name="toDate" value="${param.toDate}">
                        </div>
                    </div>
                </form>
            </jsp:attribute>

            <jsp:attribute name="foot">
                <t:pagination page="${page}" pages="${totalPages}" size="${size}" total="${total}"
                              url="${pageContext.request.contextPath}/sales-orders"
                              include="[name='keyword'], [name='status'], [name='fromDate'], [name='toDate']" />
            </jsp:attribute>

            <jsp:body>
                <c:if test="${not empty sos}">
                    <c:forEach var="so" items="${sos}" varStatus="loop">
                        <tr>
                            <td class="text-center">${loop.index + 1}</td>
                            <td><strong>${so.soNumber}</strong></td>
                            <td>${so.customerName}</td>
                            <td>${so.requestedShipDate}</td>
                            <td>${so.shipToAddress}</td>
                            <td>${so.importedByUsername}</td>
                            <td>${so.status}</td>
                            <td class="text-center">
                                <form action="${pageContext.request.contextPath}/sales-orders" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="detail">
                                    <input type="hidden" name="id" value="${so.soId}">
                                    <t:button type="submit" size="sm" variant="outline" color="primary">View</t:button>
                                    </form>
                                <c:if test="${so.status != 'CLOSED'}">
                                    <form action="${pageContext.request.contextPath}/sales-orders" method="post" style="display:inline;" onsubmit="return confirm('Delete SO ${so.soNumber}?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${so.soId}">
                                        <input type="hidden" name="page" value="${page}">
                                        <t:button type="submit" size="sm" variant="outline" color="danger">Delete</t:button>
                                    </form>
                                </c:if>
                                <c:if test="${so.status == 'CREATED'}">
                                    <form action="${pageContext.request.contextPath}/sales-orders" method="get" style="display:inline;">
                                        <input type="hidden" name="action" value="edit">
                                        <input type="hidden" name="id" value="${so.soId}">
                                        <input type="hidden" name="page" value="${page}">
                                        <t:button type="submit" size="sm" variant="outline" color="primary">Edit</t:button>
                                        </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>
                <c:if test="${empty sos}">
                    <tr>
                        <td colspan="8" class="text-center text-muted">No sale orders found</td>
                    </tr>
                </c:if>
            </jsp:body>
        </t:table>
    </jsp:body>
</t:layout>