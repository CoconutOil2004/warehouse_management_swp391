<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<t:layout title="Create Pick Wave">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item">
                        <a href="${pageContext.request.contextPath}/pick-wave?action=list">Pick Wave</a>
                    </li>
                    <li class="breadcrumb-item active" aria-current="page">Create Pick Wave</li>
                </ol>
            </nav>
            <a href="${pageContext.request.contextPath}/pick-wave?action=list"
               class="btn btn-outline-secondary">
                <i class="fas fa-arrow-left me-1"></i> Back to Pick Wave
            </a>
        </div>

        <!-- Filter GDN candidates -->
        <div class="card mb-4 shadow-sm">
            <div class="card-body">
                <h5 class="card-title mb-3">Choose GDN to create Pick Wave</h5>
                <p class="text-muted small mb-3">
                    Chỉ các GDN ở trạng thái <strong>PENDING</strong> (hoặc trạng thái bạn chọn) mới đủ điều kiện tạo Pick Wave.
                </p>
                <form action="${pageContext.request.contextPath}/pick-wave" method="get" class="row g-3">
                    <input type="hidden" name="action" value="create"/>
                    <div class="col-md-4">
                        <label class="form-label fw-bold">GDN Number</label>
                        <input type="text"
                               class="form-control"
                               name="gdnNumber"
                               value="${gdnNumber}"
                               placeholder="Search GDN number...">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-bold">Sales Order Number</label>
                        <input type="text"
                               class="form-control"
                               name="soNumber"
                               value="${soNumber}"
                               placeholder="Search SO number...">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label fw-bold">Status</label>
                        <select class="form-select" name="status">
                            <option value="PENDING" ${status == 'PENDING' || empty status ? 'selected' : ''}>PENDING</option>
                            <option value="DRAFT" ${status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                            <option value="ONGOING" ${status == 'ONGOING' ? 'selected' : ''}>ONGOING</option>
                            <option value="" ${status == '' ? 'selected' : ''}>-- All --</option>
                        </select>
                    </div>
                    <div class="col-md-1 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="fas fa-search"></i>
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- GDN selection & create wave -->
        <form action="${pageContext.request.contextPath}/pick-wave" method="post">
            <input type="hidden" name="action" value="create"/>
            <div class="card shadow-sm">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">
                        GDN candidates
                    </h5>
                    <button type="submit"
                            class="btn btn-primary"
                            <c:if test="${empty gdns}">disabled</c:if>>
                        <i class="fas fa-box-open me-1"></i>
                        Create Pick Wave &amp; Assign Tasks
                    </button>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr class="text-center">
                                    <th style="width: 60px;">Select</th>
                                    <th>GDN Number</th>
                                    <th>Sales Order</th>
                                    <th>Customer</th>
                                    <th>Status</th>
                                    <th>Created By</th>
                                    <th>Created At</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="g" items="${gdns}">
                                    <tr>
                                        <td class="text-center">
                                            <input type="radio"
                                                   name="gdnId"
                                                   value="${g.gdnId}"
                                                   required>
                                        </td>
                                        <td class="fw-semibold text-primary">
                                            ${g.gdnNumber}
                                        </td>
                                        <td>${g.soNumber}</td>
                                        <td>${g.customerName}</td>
                                        <td class="text-center">
                                            <span class="badge ${(g.status == 'PENDING' || g.status == 'DRAFT') ? 'bg-secondary' : (g.status == 'ONGOING' ? 'bg-warning text-dark' : 'bg-success')}">
                                                ${g.status}
                                            </span>
                                        </td>
                                        <td>${g.creatorName}</td>
                                        <td class="text-center">
                                            ${g.createdAtDisplay}
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty gdns}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">
                                            No eligible GDN found with current filters.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </form>
    </div>
</t:layout>

