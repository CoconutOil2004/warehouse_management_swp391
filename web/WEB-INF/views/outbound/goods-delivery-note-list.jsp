<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<t:layout title="Goods Delivery Note List">
    <div class="container-fluid">
        <c:if test="${not empty param.created}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                Successfully created ${param.created} GDN(s).
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            </div>
        </c:if>

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
                            <option value="PENDING" ${param.status=='PENDING' ? 'selected' : '' }>PENDING</option>
                            <option value="DRAFT" ${param.status=='DRAFT' ? 'selected' : '' }>DRAFT</option>
                            <option value="ONGOING" ${param.status=='ONGOING' ? 'selected' : '' }>ONGOING</option>
                            <option value="CONFIRMED" ${param.status=='CONFIRMED' ? 'selected' : '' }>CONFIRMED</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="fas fa-search"></i> Filter
                        </button>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                            class="btn btn-secondary w-100">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <div class="mb-3 d-flex justify-content-end">
            <a href="${pageContext.request.contextPath}/goods-delivery-note?action=create"
                class="btn btn-success shadow-sm">
                <i class="fas fa-plus"></i> Create New GDN
            </a>
        </div>

        <div class="table-responsive shadow-sm rounded">
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
                                <span class="badge badge-pill ${(gdn.status == 'PENDING' || gdn.status == 'DRAFT') ? 'badge-secondary' : (gdn.status == 'ONGOING' ? 'badge-warning' : 'badge-success')}">
                                    ${gdn.status}
                                </span>
                            </td>
                            <td>${gdn.creatorName}</td>
                            <td class="text-center">
                                ${gdn.createdAtDisplay}
                            </td>
                            <td class="text-center">
                                <div class="d-flex justify-content-center align-items-center gap-2">
                                    <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}"
                                        class="btn btn-sm btn-info shadow-sm text-white d-flex align-items-center justify-content-center"
                                        style="width: 85px; height: 32px;" title="View Details">
                                        <i class="fas fa-eye me-1"></i> View
                                    </a>
                                    <c:if test="${gdn.status == 'PENDING' || gdn.status == 'DRAFT' || gdn.status == 'ONGOING'}">
                                        <a href="${pageContext.request.contextPath}/goods-delivery-note?action=edit&id=${gdn.gdnId}"
                                            class="btn btn-sm btn-warning shadow-sm d-flex align-items-center justify-content-center"
                                            style="width: 85px; height: 32px;" title="Edit">
                                            <i class="fas fa-edit me-1"></i> Edit
                                        </a>
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

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <nav class="mt-4">
                <ul class="pagination justify-content-center">
                    <c:forEach var="i" begin="1" end="${totalPages}">
                        <li class="page-item ${page == i ? 'active' : ''}">
                            <a class="page-link shadow-sm"
                                href="${pageContext.request.contextPath}/goods-delivery-note?action=list&page=${i}&gdnNumber=${param.gdnNumber}&soNumber=${param.soNumber}&status=${param.status}">${i}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </div>
</t:layout>
