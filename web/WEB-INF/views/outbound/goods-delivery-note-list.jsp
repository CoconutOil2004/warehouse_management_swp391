<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<t:layout title="Goods Delivery Note List">
    <div class="container-fluid">
        <!-- Filter Form -->
        <div class="card mb-4 shadow-sm">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/goods-delivery-note" method="get"
                    class="row g-3">
                    <input type="hidden" name="action" value="list">
                    <div class="col-md-3">
                        <label class="form-label font-weight-bold">GDN Number</label>
                        <input type="text" class="form-control" name="gdnNumber" value="${param.gdnNumber}"
                            placeholder="Search GDN number...">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label font-weight-bold">Sales Order Number</label>
                        <input type="text" class="form-control" name="soNumber" value="${param.soNumber}"
                            placeholder="Search SO number...">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label font-weight-bold">Status</label>
                        <select class="form-control" name="status">
                            <option value="">-- All --</option>
                            <option value="CREATED" ${param.status=='CREATED' ? 'selected' : '' }>CREATED</option>
                            <option value="PICKING" ${param.status=='PICKING' ? 'selected' : '' }>PICKING</option>
                            <option value="PACKING" ${param.status=='PACKING' ? 'selected' : '' }>PACKING</option>
                            <option value="SHIPPING" ${param.status=='SHIPPING' ? 'selected' : '' }>SHIPPING</option>
                            <option value="CANCELLED" ${param.status=='CANCELLED' ? 'selected' : '' }>CANCELLED</option>
                            <option value="DONE" ${param.status=='DONE' ? 'selected' : '' }>DONE</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <t:button type="submit" color="primary" cssClass="w-100">
                            Filter
                        </t:button>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                            class="btn btn-secondary w-100">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <div class="mb-3 d-flex justify-content-end">
            <form action="${pageContext.request.contextPath}/goods-delivery-note" method="get" class="m-0">
                <input type="hidden" name="action" value="create">
                <t:button type="submit" color="success" cssClass="shadow-sm">
                    Create New GDN
                </t:button>
            </form>
        </div>

        <div class="card shadow-sm rounded">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-bordered table-hover table-striped align-middle mb-0">
                        <thead class="thead-dark">
                            <tr class="text-center">
                                <th>ID</th>
                                <th>GDN Number</th>
                                <th>Sales Order</th>
                                <th>Customer</th>
                                <th>Status</th>
                                <th>Created By</th>
                                <th>Created At</th>
                                <th>Shipment</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="gdn" items="${gdns}">
                                <tr>
                                    <td class="text-center">${gdn.gdnId}</td>
                                    <td class="font-weight-bold text-primary">${gdn.gdnNumber}</td>
                                    <td>${gdn.soNumber}</td>
                                    <td>${gdn.customerName}</td>
                                    <td class="text-center">
                                        <c:set var="gdnStatusBadge" value="badge-secondary" />
                                        <c:choose>
                                            <c:when test="${gdn.status == 'CREATED'}"><c:set var="gdnStatusBadge" value="badge-secondary" /></c:when>
                                            <c:when test="${gdn.status == 'PICKING'}"><c:set var="gdnStatusBadge" value="badge-warning" /></c:when>
                                            <c:when test="${gdn.status == 'PACKING'}"><c:set var="gdnStatusBadge" value="badge-info" /></c:when>
                                            <c:when test="${gdn.status == 'SHIPPING'}"><c:set var="gdnStatusBadge" value="badge-primary" /></c:when>
                                            <c:when test="${gdn.status == 'DONE'}"><c:set var="gdnStatusBadge" value="badge-success" /></c:when>
                                            <c:when test="${gdn.status == 'CANCELLED'}"><c:set var="gdnStatusBadge" value="badge-danger" /></c:when>
                                        </c:choose>
                                        <span class="badge badge-pill ${gdnStatusBadge}">
                                            ${gdn.status}
                                        </span>
                                    </td>
                                    <td>${gdn.creatorName}</td>
                                    <td class="text-center">
                                        ${gdn.createdAtDisplay}
                                    </td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${not empty gdn.lastShipmentStatus}">
                                                <span class="badge ${
                                                    gdn.lastShipmentStatus == 'DELIVERED' ? 'bg-success text-white' :
                                                    (gdn.lastShipmentStatus == 'CANCELLED' ? 'bg-danger text-white' :
                                                    'bg-info text-white')}">
                                                    ${gdn.lastShipmentStatus}
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted small">-</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <div class="d-flex justify-content-center align-items-center gap-2">
                                            <form action="${pageContext.request.contextPath}/goods-delivery-note" method="get" class="m-0">
                                                <input type="hidden" name="action" value="detail">
                                                <input type="hidden" name="id" value="${gdn.gdnId}">
                                                <t:button type="submit" size="sm" variant="outline" color="primary">
                                                    View
                                                </t:button>
                                            </form>
                                            <c:if test="${gdn.status == 'CREATED' || gdn.status == 'PICKING' || gdn.status == 'PACKING'}">
                                    <%-- SHIPPING, CANCELLED, DONE: no Edit --%>
                                                <form action="${pageContext.request.contextPath}/goods-delivery-note" method="get" class="m-0">
                                                    <input type="hidden" name="action" value="edit">
                                                    <input type="hidden" name="id" value="${gdn.gdnId}">
                                                    <t:button type="submit" size="sm" variant="outline" color="warning">
                                                        Edit
                                                    </t:button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty gdns}">
                                <tr>
                                    <td colspan="8" class="text-center py-4 text-muted">No Goods Delivery Notes found.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="card-footer py-3">
                <t:pagination page="${page}" pages="${totalPages}" size="${size}" total="${total}"
                    url="${pageContext.request.contextPath}/goods-delivery-note"
                    include="[name='gdnNumber'], [name='soNumber'], [name='status']" />
            </div>
        </div>
    </div>
</t:layout>
