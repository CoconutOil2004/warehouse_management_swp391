<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="dto.PurchaseOrderListDTO"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%
    List<PurchaseOrderListDTO> pos =
        (List<PurchaseOrderListDTO>) request.getAttribute("pos");
    Integer currentPageObj = (Integer) request.getAttribute("page");
    Integer totalPagesObj  = (Integer) request.getAttribute("totalPages");
%>
<t:layout title="Purchase Order List">
    <jsp:attribute name="actions">
        <div class="d-flex align-items-center">
            <form action="${pageContext.request.contextPath}/purchase-orders" method="post" class="m-0 mr-2">
                <input type="hidden" name="action" value="new">
                <t:button type="submit" color="success">Add Purchase Order</t:button>
                </form>
                <form action="${pageContext.request.contextPath}/purchase-orders" method="get" class="m-0">
                <input type="hidden" name="action" value="import">
                <t:button type="submit" color="success">Import Purchase Order</t:button>
                </form>
            </div>
    </jsp:attribute>

    <jsp:body>
        <c:set var="columns" value='${["STT", "PO Number", "Supplier", "Expected Date", "Status", "Imported By", "Imported At", "Action"]}' />
        <t:table id="poTable" columns="${columns}">
            <jsp:attribute name="head">
                <form hx-get="${pageContext.request.contextPath}/purchase-orders" hx-target="#wrapper" hx-select="#wrapper" hx-swap="outerHTML" hx-push-url="true" class="m-0">
                    <!-- ROW 1: keyword + status -->
                    <div class="form-row mb-2">
                        <div class="col-md-6">
                            <label class="mb-1 font-weight-bold">Search</label>
                            <input type="text" name="keyword" class="form-control" placeholder="PO number, supplier..." value="${param.keyword}">
                        </div>
                        <div class="col-md-3">
                            <label class="mb-1 font-weight-bold">Status</label>
                            <select name="status" class="form-control">
                                <option value="">-- All --</option>
                                <option value="CREATED"   ${param.status == 'CREATED' ? 'selected' : ''}>CREATED</option>
                                <option value="IMPORTED"  ${param.status == 'IMPORTED' ? 'selected' : ''}>IMPORTED</option>
                                <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                                <option value="CLOSED"    ${param.status == 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <t:button type="submit" color="primary" cssClass="mr-2">Search</t:button>
                            <a href="${pageContext.request.contextPath}/purchase-orders" class="btn btn-outline-secondary">Reset</a>
                        </div>
                    </div>
                    <!-- ROW 2: Date filters -->
                    <div class="form-row">
                        <div class="col-md-3">
                            <label class="mb-1 font-weight-bold">Expected from</label>
                            <input type="date" name="expectedFrom" class="form-control" value="${param.expectedFrom}">
                        </div>
                        <div class="col-md-3 font-weight-bold">
                            <label class="mb-1">Expected to</label>
                            <input type="date" name="expectedTo" class="form-control" value="${param.expectedTo}">
                        </div>
                    </div>
                </form>
            </jsp:attribute>

            <jsp:attribute name="foot">
                <t:pagination page="${page}" pages="${totalPages}" size="${size}" total="${total}"
                              url="${pageContext.request.contextPath}/purchase-orders"
                              include="[name='keyword'], [name='status'], [name='expectedFrom'], [name='expectedTo']" />
            </jsp:attribute>

            <jsp:body>
                <c:if test="${not empty pos}">
                    <c:forEach var="po" items="${pos}" varStatus="status">
                        <tr>
                            <td class="text-center">${status.index + 1 + (page - 1) * size}</td>
                            <td><strong>${po.poNumber}</strong></td>
                            <td>${po.supplierName}</td>
                            <td>${po.expectedDeliveryDate}</td>
                            <td>${po.status}</td>
                            <td>${po.importedByUsername}</td>
                            <td>${po.importedAt}</td>
                            <td class="text-center">
                                <form action="${pageContext.request.contextPath}/purchase-orders" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="detail">
                                    <input type="hidden" name="id" value="${po.poId}">
                                    <t:button type="submit" size="sm" variant="outline" color="primary">View</t:button>
                                    </form>
                                    <button type="button" class="btn btn-sm btn-outline-danger"
                                            data-bs-toggle="modal" data-bs-target="#deleteModal${po.poId}">
                                    Delete
                                </button>

                                <t:alert id="deleteModal${po.poId}">
                                    <jsp:attribute name="title"> Confirm Delete </jsp:attribute>
                                    <jsp:attribute name="desciption">
                                        Are you sure you want to delete purchase order
                                        <strong>${po.poNumber}</strong>? This action cannot be undone.
                                    </jsp:attribute>
                                    <jsp:attribute name="action">
                                        <button type="button" class="btn btn-danger" data-bs-dismiss="modal"
                                                onclick="document.getElementById('deleteForm${po.poId}').submit()">
                                            Delete
                                        </button>
                                    </jsp:attribute>
                                </t:alert>
                                <form id="deleteForm${po.poId}" action="${pageContext.request.contextPath}/purchase-orders"
                                      method="post" class="d-none">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${po.poId}">
                                    <input type="hidden" name="page" value="${page}">
                                </form>
                                <form action="${pageContext.request.contextPath}/purchase-orders" method="get" style="display:inline;">
                                    <input type="hidden" name="action" value="edit">
                                    <input type="hidden" name="id" value="${po.poId}">
                                    <input type="hidden" name="page" value="${page}">
                                    <t:button type="submit" size="sm" variant="outline" color="primary">Edit</t:button>
                                    </form>
                                </td>
                            </tr>
                    </c:forEach>
                </c:if>
                <c:if test="${empty pos}">
                    <tr>
                        <td colspan="8" class="text-center text-muted">No purchase orders found</td>
                    </tr>
                </c:if>
            </jsp:body>
        </t:table>
    </jsp:body>
</t:layout>