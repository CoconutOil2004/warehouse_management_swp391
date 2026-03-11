<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags/" %>

<t:layout title="My Profile">
    <jsp:body>
        <!-- Personal Information Section -->
        <div class="card shadow mb-4">
            <div class="card-header py-3 d-flex justify-content-between align-items-center">
                <h6 class="m-0 font-weight-bold text-primary">Personal Information</h6>
                <button type="button" class="btn btn-sm btn-primary shadow-sm" data-toggle="modal" data-target="#editProfileModal">
                    <i class="fas fa-edit fa-sm text-white-50 mr-1"></i> Edit Profile
                </button>
            </div>
            <div class="card-body">
                <div class="row">
                    <!-- Image on Left -->
                    <div class="col-md-3 text-center">
                        <img class="img-profile rounded-circle mb-3" 
                             src="${pageContext.request.contextPath}/assets/img/undraw_profile.svg" 
                             style="width: 10rem; height: 10rem;">
                        <h5 class="font-weight-bold text-dark mb-1">${user.fullName}</h5>
                        <p class="text-muted">@${user.username}</p>
                    </div>
                    
                    <!-- Info on Right -->
                    <div class="col-md-9">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="small font-weight-bold text-gray-500">Email Address</label>
                                <div class="h6 text-gray-800">${user.email}</div>
                            </div>

                            <div class="col-md-6 mb-3">
                                <label class="small font-weight-bold text-gray-500">Phone Number</label>
                                <div class="h6 text-gray-800">${not empty user.phone ? user.phone : 'N/A'}</div>
                            </div>

                            <div class="col-md-6 mb-3">
                                <label class="small font-weight-bold text-gray-500">Status</label>
                                <div>
                                    <span class="badge ${user.status == 'ACTIVE' ? 'badge-success' : 'badge-danger'} px-3 py-2">
                                        ${user.status}
                                    </span>
                                </div>
                            </div>
                            
                            <div class="col-md-6 mb-3">
                                <label class="small font-weight-bold text-gray-500">Warehouse</label>
                                <div class="h6 text-gray-800">
                                    <c:choose>
                                        <c:when test="${not empty user.warehouseId}">
                                            Warehouse #${user.warehouseId}
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted font-italic">No warehouse assigned</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Edit Profile Modal -->
        <div class="modal fade" id="editProfileModal" tabindex="-1" role="dialog" aria-labelledby="editProfileModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title text-primary font-weight-bold" id="editProfileModalLabel">Edit Personal Information</h5>
                        <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                    </div>
                    <form action="${pageContext.request.contextPath}/profile" method="POST">
                        <div class="modal-body">
                            <div class="form-group">
                                <label class="small font-weight-bold text-gray-600">Full Name</label>
                                <input type="text" class="form-control" name="fullName" value="${user.fullName}" required>
                            </div>
                            <div class="form-group">
                                <label class="small font-weight-bold text-gray-600">Email Address</label>
                                <input type="email" class="form-control" name="email" value="${user.email}" required>
                            </div>
                            <div class="form-group">
                                <label class="small font-weight-bold text-gray-600">Phone Number</label>
                                <input type="text" class="form-control" name="phone" value="${user.phone}">
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                            <button class="btn btn-primary" type="submit">Save Changes</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- Assigned Roles & Permissions Section -->
        <div class="card shadow mb-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Assigned Roles & Permissions</h6>
            </div>
            <div class="card-body">
                <c:if test="${empty rolePermissionsMap}">
                    <div class="text-center py-5">
                        <i class="fas fa-user-shield fa-3x text-gray-300 mb-3"></i>
                        <p class="text-gray-500 mb-0">No roles assigned to this account.</p>
                    </div>
                </c:if>

                <c:if test="${not empty rolePermissionsMap}">
                    <div class="accordion" id="rolesAccordion">
                        <c:forEach items="${rolePermissionsMap}" var="entry" varStatus="status">
                            <div class="card mb-2">
                                <div class="card-header p-0" id="heading${status.index}">
                                    <h2 class="mb-0">
                                        <button class="btn btn-link btn-block text-left d-flex justify-content-between align-items-center" 
                                                type="button" 
                                                data-toggle="collapse" 
                                                data-target="#collapse${status.index}" 
                                                aria-expanded="${status.first ? 'true' : 'false'}" 
                                                aria-controls="collapse${status.index}">
                                            <span>
                                                <i class="fas fa-user-tag text-primary mr-2"></i>
                                                <strong>${entry.key.name}</strong>
                                                <small class="text-muted ml-2">${entry.key.description}</small>
                                            </span>
                                            <i class="fas fa-chevron-down"></i>
                                        </button>
                                    </h2>
                                </div>

                                <div id="collapse${status.index}" 
                                     class="collapse ${status.first ? 'show' : ''}" 
                                     aria-labelledby="heading${status.index}" 
                                     data-parent="#rolesAccordion">
                                    <div class="card-body">
                                        <h6 class="small font-weight-bold text-uppercase text-gray-500 mb-3">Permissions</h6>
                                        <div class="d-flex flex-wrap">
                                            <c:choose>
                                                <c:when test="${not empty entry.value}">
                                                    <c:forEach items="${entry.value}" var="perm">
                                                        <span class="badge badge-secondary p-2 mr-2 mb-2" title="${perm.name}">
                                                            ${perm.code}
                                                        </span>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted small font-italic">No specific permissions assigned to this role.</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </div>
    </jsp:body>
</t:layout>

<style>
    .gap-2 { gap: 0.5rem; }
    .last-child-none:last-child { display: none; }
</style>
